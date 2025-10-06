// Service Worker for Web Push Notifications

self.addEventListener('push', function(event) {
    console.log('[Service Worker] Push Received.');

    if (!event.data) {
        console.log('[Service Worker] Push event but no data');
        return;
    }

    const data = event.data.json();
    console.log('[Service Worker] Push data:', data);

    const title = data.title || 'New Notification';
    const options = {
        body: data.body || '',
        icon: data.icon || '/icon-192x192.png',
        badge: data.badge || '/badge-72x72.png',
        image: data.image,
        tag: data.tag || 'default-tag',
        requireInteraction: data.requireInteraction || false,
        data: {
            url: data.url || '/',
            sendLogId: data.sendLogId
        }
    };

    event.waitUntil(
        self.registration.showNotification(title, options)
    );
});

self.addEventListener('notificationclick', function(event) {
    console.log('[Service Worker] Notification click Received.');

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
        }).catch(err => console.error('Failed to track click event:', err));
    }

    // Open URL
    const urlToOpen = event.notification.data?.url || '/';
    event.waitUntil(
        clients.openWindow(urlToOpen)
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
