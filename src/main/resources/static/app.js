// Web Push Client Application
console.log('[App] JavaScript file loaded');

let swRegistration = null;
let vapidPublicKey = null;

// Initialize
document.addEventListener('DOMContentLoaded', async () => {
    console.log('[App] DOMContentLoaded event fired');
    await initializeApp();
});

async function initializeApp() {
    updateDeviceInfo();

    // Check service worker support
    if (!('serviceWorker' in navigator)) {
        updateStatus('이 브라우저는 서비스 워커를 지원하지 않습니다.', 'error');
        return;
    }

    if (!('PushManager' in window)) {
        updateStatus('이 브라우저는 푸시 알림을 지원하지 않습니다.', 'error');
        return;
    }

    try {
        console.log('[App] Registering service worker...');
        // Register service worker
        swRegistration = await navigator.serviceWorker.register('/sw.js');
        console.log('[App] Service Worker registered:', swRegistration);

        console.log('[App] Fetching VAPID public key...');
        // Get VAPID public key
        const response = await fetch('/api/push/subscription/vapid-public-key');
        if (!response.ok) {
            throw new Error(`VAPID 키 조회 실패: ${response.status}`);
        }
        const data = await response.json();
        vapidPublicKey = data.publicKey;
        console.log('[App] VAPID public key received:', vapidPublicKey.substring(0, 20) + '...');

        // Check subscription status
        await updateSubscriptionStatus();

        // Add event listeners
        const subscribeBtn = document.getElementById('subscribeBtn');
        const unsubscribeBtn = document.getElementById('unsubscribeBtn');
        const sendTestBtn = document.getElementById('sendTestBtn');

        console.log('[App] Subscribe button element:', subscribeBtn);
        console.log('[App] Subscribe button disabled:', subscribeBtn.disabled);

        subscribeBtn.addEventListener('click', () => {
            console.log('[App] Subscribe button clicked!');
            subscribeUser();
        });
        unsubscribeBtn.addEventListener('click', () => {
            console.log('[App] Unsubscribe button clicked!');
            unsubscribeUser();
        });
        sendTestBtn.addEventListener('click', () => {
            console.log('[App] Send test button clicked!');
            sendTestNotification();
        });

        console.log('[App] Event listeners registered');
        console.log('[App] Initialization complete');

    } catch (error) {
        console.error('[App] Initialization error:', error);
        updateStatus('초기화 중 오류가 발생했습니다: ' + error.message, 'error');
    }
}

async function updateSubscriptionStatus() {
    try {
        const subscription = await swRegistration.pushManager.getSubscription();
        const subscribeBtn = document.getElementById('subscribeBtn');
        const unsubscribeBtn = document.getElementById('unsubscribeBtn');

        if (subscription) {
            updateStatus('✅ 알림이 구독되어 있습니다!', 'success');
            subscribeBtn.classList.add('hidden');
            unsubscribeBtn.classList.remove('hidden');
        } else {
            updateStatus('알림을 구독하려면 아래 버튼을 클릭하세요.', 'info');
            subscribeBtn.disabled = false;
            subscribeBtn.classList.remove('hidden');
            unsubscribeBtn.classList.add('hidden');
        }
    } catch (error) {
        console.error('Error checking subscription:', error);
        updateStatus('구독 상태 확인 중 오류가 발생했습니다.', 'error');
    }
}

async function subscribeUser() {
    try {
        console.log('[App] Requesting notification permission...');
        const permission = await Notification.requestPermission();
        console.log('[App] Permission result:', permission);

        if (permission !== 'granted') {
            updateStatus('알림 권한이 거부되었습니다.', 'error');
            return;
        }

        console.log('[App] Subscribing to push notifications...');
        // Subscribe to push
        const subscription = await swRegistration.pushManager.subscribe({
            userVisibleOnly: true,
            applicationServerKey: urlBase64ToUint8Array(vapidPublicKey)
        });

        console.log('[App] Push subscription created:', subscription);

        // Send subscription to server
        console.log('[App] Sending subscription to server...');
        const response = await fetch('/api/push/subscription/subscribe', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                endpoint: subscription.endpoint,
                keys: {
                    p256dh: arrayBufferToBase64(subscription.getKey('p256dh')),
                    auth: arrayBufferToBase64(subscription.getKey('auth'))
                },
                userAgent: navigator.userAgent,
                deviceType: getDeviceType()
            })
        });

        console.log('[App] Server response status:', response.status);

        if (response.ok) {
            updateStatus('✅ 알림 구독이 완료되었습니다!', 'success');
            await updateSubscriptionStatus();
        } else {
            const errorText = await response.text();
            throw new Error(`서버에 구독 정보 전송 실패: ${response.status} - ${errorText}`);
        }

    } catch (error) {
        console.error('[App] Subscription error:', error);
        updateStatus('구독 중 오류가 발생했습니다: ' + error.message, 'error');
    }
}

async function unsubscribeUser() {
    try {
        const subscription = await swRegistration.pushManager.getSubscription();

        if (subscription) {
            await subscription.unsubscribe();
            updateStatus('구독이 해지되었습니다.', 'info');
            await updateSubscriptionStatus();
        }
    } catch (error) {
        console.error('Unsubscribe error:', error);
        updateStatus('구독 해지 중 오류가 발생했습니다.', 'error');
    }
}

async function sendTestNotification() {
    const title = document.getElementById('title').value;
    const body = document.getElementById('body').value;
    const url = document.getElementById('url').value;

    if (!title || !body) {
        alert('제목과 내용을 입력해주세요.');
        return;
    }

    try {
        const deviceType = getDeviceType();
        console.log('[App] Sending notification with deviceType:', deviceType);

        const response = await fetch('/api/push/message/send', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                title,
                body,
                url: url || '/',
                icon: '/icon-192x192.png',
                deviceType: null,  // 모든 구독자에게 발송
                ttl: 60,  // 60초 (긴급 푸시)
                urgency: 'high'  // 높은 우선순위
            })
        });

        if (response.ok) {
            alert('테스트 알림이 전송되었습니다!');
        } else {
            throw new Error('알림 전송 실패');
        }
    } catch (error) {
        console.error('Send notification error:', error);
        alert('알림 전송 중 오류가 발생했습니다: ' + error.message);
    }
}

// Utility functions
function updateStatus(message, type) {
    const statusDiv = document.getElementById('status');
    statusDiv.textContent = message;

    // Tailwind CSS classes for different status types
    const baseClasses = 'p-4 rounded-xl mb-6 text-sm sm:text-base border';
    let typeClasses = '';

    switch(type) {
        case 'success':
            typeClasses = 'bg-green-50 text-green-700 border-green-200';
            break;
        case 'error':
            typeClasses = 'bg-red-50 text-red-700 border-red-200';
            break;
        case 'info':
        default:
            typeClasses = 'bg-blue-50 text-blue-700 border-blue-200';
            break;
    }

    statusDiv.className = `${baseClasses} ${typeClasses}`;
}

function updateDeviceInfo() {
    const deviceType = getDeviceType();
    const userAgent = navigator.userAgent;
    document.getElementById('deviceInfo').innerHTML = `
        타입: <strong>${deviceType}</strong><br>
        User-Agent: ${userAgent.substring(0, 100)}...
    `;
}

function getDeviceType() {
    const ua = navigator.userAgent;
    if (/android/i.test(ua)) return 'android';
    if (/iPad|iPhone|iPod/.test(ua)) return 'ios';
    return 'desktop';
}

function urlBase64ToUint8Array(base64String) {
    const padding = '='.repeat((4 - base64String.length % 4) % 4);
    const base64 = (base64String + padding)
        .replace(/\-/g, '+')
        .replace(/_/g, '/');

    const rawData = window.atob(base64);
    const outputArray = new Uint8Array(rawData.length);

    for (let i = 0; i < rawData.length; ++i) {
        outputArray[i] = rawData.charCodeAt(i);
    }
    return outputArray;
}

function arrayBufferToBase64(buffer) {
    const bytes = new Uint8Array(buffer);
    let binary = '';
    for (let i = 0; i < bytes.byteLength; i++) {
        binary += String.fromCharCode(bytes[i]);
    }
    return window.btoa(binary);
}
