// Simple in-memory cache for API responses
const cache = new Map();
const CACHE_TTL = 5 * 60 * 1000; // 5 minutes default TTL

export const apiCache = {
  // Get item from cache
  get: (key) => {
    const item = cache.get(key);
    if (!item) return null;

    // Check if item is expired
    if (Date.now() > item.expiry) {
      cache.delete(key);
      return null;
    }

    return item.data;
  },

  // Set item in cache with optional TTL
  set: (key, data, ttl = CACHE_TTL) => {
    cache.set(key, {
      data,
      expiry: Date.now() + ttl,
    });
  },

  // Remove item from cache
  remove: (key) => {
    cache.delete(key);
  },

  // Clear entire cache or by pattern
  clear: (pattern) => {
    if (!pattern) {
      cache.clear();
      return;
    }

    // Clear by key pattern
    for (const key of cache.keys()) {
      if (key.includes(pattern)) {
        cache.delete(key);
      }
    }
  },
};
