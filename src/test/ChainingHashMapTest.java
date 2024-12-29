package hw7;

import hw7.hashing.ChainingHashMap;

class ChainingHashMapTest extends MapTest {
  @Override
  protected Map<String, String> createMap() {
    return new ChainingHashMap<>();
  }

}