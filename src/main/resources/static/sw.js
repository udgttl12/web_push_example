// Service Worker for Web Push Notifications

console.log('[Service Worker] Script loaded and registered');

self.addEventListener('install', function(event) {
    console.log('[Service Worker] Install event');
    self.skipWaiting();
});

self.addEventListener('activate', function(event) {
    console.log('[Service Worker] Activate event');
    event.waitUntil(self.clients.claim());
});

self.addEventListener('push', function(event) {
    console.log('[Service Worker] Push event received!', event);

    const parseData = () => {
        if (!event.data) {
            console.log('[Service Worker] No data in push event');
            return { title: '푸시 수신됨', body: '데이터가 없는 푸시를 받았습니다.' };
        }

        // 1) JSON 파싱 시도
        try {
            const jsonData = event.data.json();
            console.log('[Service Worker] Push data (JSON):', jsonData);
            return jsonData;
        } catch (jsonError) {
            console.log('[Service Worker] Failed to parse as JSON:', jsonError.message);

            // 2) 텍스트 폴백
            try {
                const text = event.data.text();
                console.log('[Service Worker] Push data (Text):', text);
                return { title: '알림', body: text };
            } catch (textError) {
                console.error('[Service Worker] Failed to parse as text:', textError.message);
                return { title: '알림', body: '푸시 메시지를 파싱할 수 없습니다.' };
            }
        }
    };

    const data = parseData();
    const title = data.title || '알림';
    const options = {
        body: data.body || '',
        icon: data.icon || '/icon-192x192.png',
        badge: data.badge || '/badge-72x72.png',
        image: data.image,
        tag: data.tag || 'default',
        renotify: !!data.renotify,
        requireInteraction: data.requireInteraction ?? false,
        data: {
            clickUrl: data.url || data.clickUrl || '/',
            sendLogId: data.sendLogId,
            ...data.data
        },
        timestamp: Date.now()
    };

    console.log('[Service Worker] Showing notification:', title, options);

    event.waitUntil(
        self.registration.showNotification(title, options)
    );
});

self.addEventListener('notificationclick', function(event) {
    console.log('[Service Worker] Notification click received.');

    event.notification.close();

    // Track click event
    if (event.notification.data && event.notification.data.sendLogId) {
        fetch('/api/push/event/track', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                sendLogId: event.notification.data.sendLogId,
                eventType: 'notification_click'
            })
        }).catch(err => console.error('[Service Worker] Failed to track click event:', err));
    }

    // Open URL (clickUrl 또는 url 필드 지원)
    let urlToOpen = event.notification.data?.clickUrl || event.notification.data?.url || '/';

    // 상대 경로를 절대 경로로 변환 (Edge 호환성)
    if (!urlToOpen.startsWith('http://') && !urlToOpen.startsWith('https://')) {
        urlToOpen = self.location.origin + urlToOpen;
    }

    console.log('[Service Worker] Opening URL:', urlToOpen);

    event.waitUntil(
        clients.openWindow(urlToOpen).catch(err => {
            console.error('[Service Worker] Failed to open window:', err);
            // 폴백: 현재 origin으로 이동
            return clients.openWindow(self.location.origin);
        })
    );
});

self.addEventListener('notificationclose', function(event) {
    console.log('[Service Worker] Notification closed.');

    // Track close event
    if (event.notification.data && event.notification.data.sendLogId) {
        fetch('/api/push/event/track', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                sendLogId: event.notification.data.sendLogId,
                eventType: 'notification_close'
            })
        }).catch(err => console.error('Failed to track close event:', err));
    }
});
