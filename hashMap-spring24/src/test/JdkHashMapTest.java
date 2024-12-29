package hw7;

import hw7.hashing.JdkHashMap;

@SuppressWarnings("All")
public class JdkHashMapTest extends MapTest {
  @Override
  protected Map<String, String> createMap() {
    return new JdkHashMap<>();
  }
}