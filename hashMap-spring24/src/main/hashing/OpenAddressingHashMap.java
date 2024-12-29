package hw7.hashing;

import hw7.Map;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class OpenAddressingHashMap<K, V> implements Map<K, V> {

  //array of Entries as underlying implementation of Hash Table
  private Entry<K,V>[] data;
  //array to hold the possible prime number capacities
  private final int[] primes;
  //counter to remember which prime number capacity has already been used
  private int primeCounter;
  //threshold to rehash
  private final double loadFactor;
  //number of existing elements in hash table
  private int size;
  //capacity of the underlying array
  private int capacity;
  //number of cells which are filled in hash table (includes existing elements & tombstones)
  private int filledCells;
  //Entry which represents a tombstone
  private final Entry<K,V> tombstone;


  /**
   * default constructor for OpenAddressingHashMap.
   */
  public OpenAddressingHashMap() {
    this.data = (Entry<K,V>[]) new Entry[5];
    this.capacity = 5;
    this.primes = new int[]{5, 11, 23, 47, 97, 197, 397, 797, 1597, 3203, 6421, 12853, 25717, 51437, 102877, 205759};
    this.primeCounter = 0;
    this.loadFactor = 0.75;
    this.size = 0;
    this.filledCells = 0;
    this.tombstone = new Entry<>(null,null);
  }

  @Override
  public void insert(K k, V v) throws IllegalArgumentException {
    if (k == null || find(k) != null) {
      throw new IllegalArgumentException();
    }
    Entry<K,V> newEntry = new Entry<K,V>(k,v);
    int index = getIndex(newEntry.key);
    //if there is empty space at index for the new key-value pair, insert it there
    if (data[index] == null || data[index].equals(tombstone)) {
      if (data[index] == null) {
        filledCells++;
        //cells which previously held tombstones are already counted in filledCells
      }
      data[index] = newEntry;
    } else {
      //call function to create chain for collisions
      handleCollision(this.data,newEntry);
    }
    size++;
    if (calculateLoadFactor() >= loadFactor) {
      rehash();
    }
  }

  /**
   * private helper function to handle collisions using linear probing.
   * @param structure the array to handle a collision for
   * @param newEntry the entry which needs to be inserted
   *                 Pre-condition: newEntry does not already exist in structure (!structure.has(newEntry))
   */
  private void handleCollision(Entry<K,V>[] structure, Entry<K,V> newEntry) {
    int index = -1;
    boolean inserted = false;
    int cap = structure.length;
    //iterate through entire array, as needed
    for (int i = 0; i < cap; i++) {
      index = (getIndex(newEntry.key) + i * i) % cap;
      //insert new element in first empty space
      if (structure[index] == null || structure[index].equals(tombstone)) {
        if (structure[index] == null) {
          filledCells++;
        }
        structure[index] = newEntry;
        inserted = true;
        break;
      }
    }
    //execution reaches here only if table is full (size = capacity)
    if (!inserted) {
      //must grow size of underlying array, then attempt to insert the pair again
      rehash();
      insert(newEntry.key,newEntry.value);
    }
  }

  /**
   * private helper function to perform rehashing of the hash table.
   */
  private void rehash() {
    int oldCapacity = capacity;
    //calculate new capacity of underlying array and allocate new array with that capacity
    growCapacity();
    Entry<K,V>[] newHashTable = (Entry<K,V>[]) new Entry[capacity];
    //iterate over every index in old underlying array and copy existing (non-deleted) elements over in correct indices
    for (int i = 0; i < oldCapacity; i++) {
      if (data[i] == null || data[i].equals(tombstone)) {
        continue;
      }
      int newIndex = getIndex(data[i].key);
      //check to see if collisions need to be handled and handle them
      if (newHashTable[newIndex] == null) {
        newHashTable[newIndex] = data[i];
      } else {
        handleCollision(newHashTable,data[i]);
      }
    }
    data = newHashTable;
    //reset filledCells to be equal to size (as there are no tombstones after rehashing)
    filledCells = size;
  }

  /**
   * private helper function to calculate the new capacity for the underlying array being rehashed.
   */
  private void growCapacity() {
    primeCounter++;
    //if end of the prime numbers array not reached, assign the capacity to be next largest prime number
    if (primeCounter < primes.length) {
      capacity = primes[primeCounter];
    } else {
      capacity = capacity * 2;
    }
  }

  @Override
  public V remove(K k) throws IllegalArgumentException {
    Entry<K,V> target = find(k);
    if (target == null) {
      throw new IllegalArgumentException();
    }
    //only executed if target is in hash table
    V value = target.value;
    remove(target);
    //NOTE: only size is decremented as the cell is still filled by a tombstone
    size--;
    return value;
  }

  /**
   * private helper function to help remove the target Entry from the hash table.
   * @param target the key-value pair (Entry) to be removed from the hash table
   *               Pre-condition: target exists and is non-deleted within the hash table
   */
  private void remove(Entry<K,V> target) {
    int index = getIndex(target.key);
    //if the Entry at the corresponding index of key is correct, set that index equal to tombstone
    if (data[index].equals(target)) {
      data[index] = tombstone;
    } else {
      //for loop to perform linear probing until target is found and set equal to tombstone
      for (int i = 1; i < capacity; i++) {
        index = (getIndex(target.key) + i * i) % capacity;
        if (data[index].equals(target)) {
          data[index] = tombstone;
          break;
        }
      }
    }
  }

  @Override
  public void put(K k, V v) throws IllegalArgumentException {
    Entry<K,V> target = find(k);
    if (target == null) {
      throw new IllegalArgumentException();
    }
    target.value = v;
  }

  @Override
  public V get(K k) throws IllegalArgumentException {
    Entry<K,V> target = find(k);
    if (target == null) {
      throw new IllegalArgumentException();
    }
    return target.value;
  }

  @Override
  public boolean has(K k) {
    return find(k) != null;
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public Iterator<K> iterator() {
    return new OpenAddressingHashMapIterator();
  }

  /**
   * private helper function to calculate index position of given key based on its hash code.
   * @param key the key to calculate the index of
   * @return the index position of key in array of this.capacity
   */
  private int getIndex(K key) {
    return Math.abs(key.hashCode() % capacity);
  }

  /**
   * private helper function to calculate the load factor for this hash table.
   * @return the load factor for this hash table.
   */
  private double calculateLoadFactor() {
    return (double) filledCells / capacity;
  }

  /**
   * private helper function to search for given key in the hash table.
   * @param key the key to be searched for.
   * @return reference to the Entry with given key if it exists, null if it does not exist or if key is null
   */
  private Entry<K,V> find(K key) {
    if (key == null) {
      return null;
    }
    int index = getIndex(key);
    int i = 0;
    //while search chain exists and have not searched whole array
    while (data[index] != null && i != capacity) {
      //matching Entry found!
      if (key.equals(data[index].key)) {
        return data[index];
        //keep iterating through search chain
      } else {
        i++;
        index = (getIndex(key) + i * i) % capacity;
      }
    }
    //execution only reaches here if search chain ends or if whole array is searched and no matching Entry is found
    return null;
  }

  /**
   * private class that implements Iterator to create an iterator for the OpenAddressingHashMap.
   */
  private class OpenAddressingHashMapIterator implements Iterator<K> {

    //index of underlying array of hash table that is being iterated over
    private int index;
    //the number of actual existing, non-deleted elements, that have been iterated over
    private int numElements;

    OpenAddressingHashMapIterator() {
      index = 0;
      numElements = 0;
    }

    @Override
    public boolean hasNext() {
      return numElements < size;
    }

    @Override
    public K next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      //if no element or deleted element at index, keep incrementing index until existing element is reached
      while (data[index] == null || data[index].equals(tombstone)) {
        index++;
      }
      K key = data[index].key;
      numElements++;
      index++;
      return key;
    }
  }

  //private class to store a pair: the key and its associated value
  private static class Entry<K,V> {
    K key;
    V value;

    Entry(K k, V v) {
      this.key = k;
      this.value = v;
    }
  }
}
