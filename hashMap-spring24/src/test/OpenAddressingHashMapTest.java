package hw7;

import hw7.hashing.OpenAddressingHashMap;

class OpenAddressingHashMapTest extends MapTest {
  @Override
  protected Map<String, String> createMap() {
    return new OpenAddressingHashMap<>();
  }

}