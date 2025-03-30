//  in-memory cache for API responses with user isolation
const cache = new Map();
const CACHE_TTL = 5 * 60 * 1000;

const getUserPrefixedKey = (key) => {
  const token = localStorage.getItem("access_token");
  const userPrefix = token ? `user_${token.substring(0, 8)}` : "anonymous";
  return `${userPrefix}:${key}`;
};

export const apiCache = {
  // Get item from cache
  get: (key) => {
    const prefixedKey = getUserPrefixedKey(key);
    const item = cache.get(prefixedKey);
    if (!item) return null;

    // Check if item is expired
    if (Date.now() > item.expiry) {
      cache.delete(prefixedKey);
      return null;
    }

    return item.data;
  },

  // Set item in cache with optional TTL
  set: (key, data, ttl = CACHE_TTL) => {
    const prefixedKey = getUserPrefixedKey(key);
    cache.set(prefixedKey, {
      data,
      expiry: Date.now() + ttl,
    });
  },

  // Remove item from cache
  remove: (key) => {
    const prefixedKey = getUserPrefixedKey(key);
    cache.delete(prefixedKey);
  },

  // Clear entire cache or by pattern (for current user only)
  clear: (pattern) => {
    const userPrefix = getUserPrefixedKey("").split(":")[0];

    if (!pattern) {
      // Clear all cache entries for current user
      for (const key of cache.keys()) {
        if (key.startsWith(userPrefix)) {
          cache.delete(key);
        }
      }
      return;
    }

    // Clear by key pattern
    const prefixedPattern = `${userPrefix}:${pattern}`;
    for (const key of cache.keys()) {
      if (key.includes(prefixedPattern)) {
        cache.delete(key);
      }
    }
  },

  // Force clear entire cache (all users)
  clearAll: () => {
    cache.clear();
  },
};
