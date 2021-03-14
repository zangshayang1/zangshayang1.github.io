## Use TreeMap To Implement Ranked Cache

The implementation of TreeMap is based on Red-Black tree. The map is sorted according to the natural ordering of its keys, or by a Comparator provided at map creation time. This implementation provides guaranteed log(n) time cost for the containsKey, get, put and remove operations. Note that this implementation is not synchronized. Thread safety is typically accomplished by synchronizing on some object that naturally encapsulates the map. If no such object exists, the map should be "wrapped" using the Collections.synchronizedSortedMap method at creation time.
```
SortedMap m = Collections.synchronizedSortedMap(new TreeMap());
```

```java
public class RetainBestCache<K, V extends Rankable> {
  // Add any fields you need here
  private DataSource<K, V> ds;
  private int capacity;
  private TreeMap<String, V> cache;
  private HashMap<K, long> rankMap;

  /**
   * Constructor with a data source (assumed to be slow) and a cache size
   * @param ds the persistent layer of the the cache
   * @param capacity the number of entries that the cache can hold
   */
  public RetainBestCache(DataSource<K, V> ds, int capacity) { // space complexity: O(capacity)
    // Implementation here
    this.ds = ds;
    this.capacity = capacity;
    this.cache = new TreeMap<String, V>();
    this.rankMap = new HashMap<K, long>();
  }

  /**
   * Gets some data. If possible, retrieves it from cache to be fast. If the data is not cached,
   * retrieves it from the data source. If the cache is full, attempt to cache the returned data,
   * evicting the V with lowest rank among the ones that it has available
   * If there is a tie, the cache may choose any V with lowest rank to evict.
   * @param key the key of the cache entry being queried
   * @return the Rankable value of the cache entry
   */
  public V get(K key) {
    // Implementation here
    long rank = this.rankMap.get(key);
    String cacheKey = buildCacheKey(rank, key);

    if (this.cache.contains(cacheKey)) {
      V value = this.cache.get(cacheKey);
      return value;
    }

    V value = this.ds.get(key);
    addToCache(key, value);
    return value;
  }

  private void addToCache(K key, V value) {
    if (this.cache.size() == this.capacity) {
      this.evictFirst();
      this.capacity--;

      return ;
    }

    String cacheKey = buildKey(value.getRank(), key);
    this.cache.put(cacheKey, value);
    this.rankMap.put(key, value.getRank());
    this.capacity++;

    return ;
  }

  private void evictFirst() {
    Entry<String, V> entry = this.cache.firstEntry();
    String cacheKey = entry.getKey();
    K key = extractKey(cacheKey);
    this.cache.remove(cacheKey);
    this.rankMap.remove(key);

    return ;
  }

  // TODO: K must extends some Serializable
  private String extractKey(String key) {
    String s = key.split("~")[1]
    return deserialize(s, K.class);
  }

  // TODO: K must extends some Serializable
  private String buildCacheKey(long rank, K k) {
    return Long.toString(rank) + "~" + serialize(k);
  }
}

/*
 * For reference, here are the Rankable and DataSource interfaces.
 * You do not need to implement them, and should not make assumptions
 * about their implementations.
 */

public interface Rankable {
  /**
   * Returns the Rank of this object, using some algorithm and potentially
   * the internal state of the Rankable.
   */
  long getRank();
}

public interface DataSource<K, V extends Rankable> {
  V get (K key);
}
```
