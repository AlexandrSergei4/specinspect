// Service Worker для PWA Салаладс
const CACHE_NAME = 'salalads-v1';

// Файлы для кэширования при установке
const PRECACHE_ASSETS = [
  '/',
  '/index.html',
  '/kmpapp.js',
  '/manifest.json'
];

// Установка Service Worker
self.addEventListener('install', (event) => {
  console.log('[SW] Installing...');
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then((cache) => {
        console.log('[SW] Precaching assets');
        return cache.addAll(PRECACHE_ASSETS);
      })
      .then(() => self.skipWaiting())
  );
});

// Активация - очистка старых кэшей
self.addEventListener('activate', (event) => {
  console.log('[SW] Activating...');
  event.waitUntil(
    caches.keys().then((cacheNames) => {
      return Promise.all(
        cacheNames
          .filter((name) => name !== CACHE_NAME)
          .map((name) => {
            console.log('[SW] Deleting old cache:', name);
            return caches.delete(name);
          })
      );
    }).then(() => self.clients.claim())
  );
});

// Стратегия: Network First, затем Cache (для API и динамического контента)
// Cache First для статических ресурсов
self.addEventListener('fetch', (event) => {
  const url = new URL(event.request.url);

  // Пропускаем Firebase API запросы - они всегда должны идти в сеть
  if (url.hostname.includes('firebaseio.com') ||
      url.hostname.includes('googleapis.com') ||
      url.hostname.includes('gstatic.com')) {
    return;
  }

  // Для статических ресурсов (.wasm, .js, шрифты) - Cache First
  if (event.request.url.match(/\.(wasm|js|css|ttf|woff2?|png|jpg|svg)$/)) {
    event.respondWith(
      caches.match(event.request)
        .then((cachedResponse) => {
          if (cachedResponse) {
            return cachedResponse;
          }
          return fetch(event.request).then((response) => {
            // Кэшируем новые статические ресурсы
            if (response.status === 200) {
              const responseClone = response.clone();
              caches.open(CACHE_NAME).then((cache) => {
                cache.put(event.request, responseClone);
              });
            }
            return response;
          });
        })
    );
    return;
  }

  // Для HTML - Network First
  event.respondWith(
    fetch(event.request)
      .then((response) => {
        // Кэшируем успешные ответы
        if (response.status === 200) {
          const responseClone = response.clone();
          caches.open(CACHE_NAME).then((cache) => {
            cache.put(event.request, responseClone);
          });
        }
        return response;
      })
      .catch(() => {
        // При ошибке сети - берем из кэша
        return caches.match(event.request);
      })
  );
});
