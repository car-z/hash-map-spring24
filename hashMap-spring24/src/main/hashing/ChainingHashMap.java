package hw7.hashing;

import hw7.Map;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

public class ChainingHashMap<K, V> implements Map<K, V> {

  //array of LinkedLists of Entries as underlying implementation of Hash Table
  private LinkedList<Entry<K,V>>[] data;
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

  /**
   * default constructor for ChainingHasMap.
   */
  public ChainingHashMap() {
    this.data = (LinkedList<Entry<K,V>>[]) new LinkedList[5];
    this.capacity = 5;
    this.primes = new int[]{5, 11, 23, 47, 97, 197, 397, 797, 1597, 3203, 6421, 12853, 25717, 51437, 102877, 205759};
    this.primeCounter = 0;
    this.loadFactor = 0.75;
    this.size = 0;
  }

  @Override
  public void insert(K k, V v) throws IllegalArgumentException {
    if (k == null || find(k) != null) {
      throw new IllegalArgumentException();
    }
    Entry<K,V> newEntry = new Entry<K,V>(k,v);
    //call another helper function to perform actual insertion of the new Entry
    insert(data,newEntry);
    size++;
    //if threshold loadFactor is surpassed, rehash
    if (calculateLoadFactor() >= loadFactor) {
      rehash();
    }
  }

  /**
   * private overloaded function to insert a new key-value pair (Entry) into the correct LinkedList in the array.
   * @param structure the underlying array implementation to insert the new pair into
   * @param newEntry the new key value pair to be inserted
   */
  private void insert(LinkedList<Entry<K,V>>[] structure, Entry<K,V> newEntry) {
    int index = getIndex(newEntry.key);
    if (structure[index] == null) {
      structure[index] = new LinkedList<Entry<K,V>>();
    }
    //add the new key-value pair to the LinkedList at the corresponding index in data array
    structure[index].addLast(newEntry);
  }

  /**
   * private helper function to perform rehashing of the hash table.
   */
  private void rehash() {
    //find new, larger capacity for underlying array and allocate such an array
    growCapacity();
    LinkedList<Entry<K,V>>[] newHashTable = (LinkedList<Entry<K,V>>[]) new LinkedList[capacity];
    //for each LinkedList within the old array of LinkedLists
    for (LinkedList<Entry<K, V>> list : data) {
      if (list != null) {
        //for each Entry within each LinkedList
        int numElements = list.size();
        for (int i = 0; i < numElements; i++) {
          //remove an Entry from the old LinkedList
          Entry<K, V> removedEntry = list.remove();
          //call insert to insert the removed Entry into the new underlying array
          insert(newHashTable,removedEntry);
        }
      }
    }
    data = newHashTable;
  }

  /**
   * private helper function to calculate the new capacity for the underlying array being rehashed.
   */
  private void growCapacity() {
    primeCounter++;
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
    V value = target.value;
    int index = getIndex(k);
    //call remove() function for LinkedList at data[index] to remove the specified key-value pair
    data[index].remove(target);
    size--;
    return value;
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

  /**
   * private helper function to calculate index position of given key based on its hash code.
   * @param key the key to calculate the index of
   * @return the index position of key in an array of this.capacity length
   */
  private int getIndex(K key) {
    return Math.abs(key.hashCode() % capacity);
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
    if (data[index] != null) {
      //if LinkedList at index exists but has no Entry elements, return null
      if (data[index].isEmpty()) {
        return null;
      }
      //iterate through LinkedList at index to see if matching Entry to key exists
      for (int i = 0; i < data[index].size(); i++) {
        if (key.equals(data[index].get(i).key)) {
          return data[index].get(i);
        }
      }
    }
    //Execution reaches here only if there is no matching Entry in the LinkedList at the corresponding index
    return null;
  }

  /**
   * private helper function to calculate the load factor for this hash table.
   * @return the load factor for this hash table.
   */
  private double calculateLoadFactor() {
    return (double) size / capacity;
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public Iterator<K> iterator() {
    return new ChainingHashMapIterator();
  }

  /**
   * private class that implements Iterator to create an iterator for the ChainingHashMap.
   */
  private class ChainingHashMapIterator implements Iterator<K> {
    //index of underlying array being iterated over
    private int dataIndex;
    //the number of elements which have been iterated
    private int numElement;
    //the index for the LinkedList being iterated over
    private int linkedListIndex;

    ChainingHashMapIterator() {
      dataIndex = 0;
      numElement = 0;
      linkedListIndex = 0;
    }

    @Override
    public boolean hasNext() {
      return numElement < size;
    }

    @Override
    public K next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      //keep incrementing index position of array of LinkedLists (dataIndex) if no Entries exist at dataIndex
      while (data[dataIndex] == null || data[dataIndex].isEmpty()) {
        dataIndex++;
      }
      //while loop exited, meaning Entry has been found within LinkedList at dataIndex
      numElement++;
      Entry<K,V> entry = data[dataIndex].get(linkedListIndex);
      linkedListIndex++;
      //if current LinkedList at dataIndex has been fully iterated over, advance dataIndex & reset linkedListIndex to 0
      if (linkedListIndex >= data[dataIndex].size()) {
        dataIndex++;
        linkedListIndex = 0;
      }
      return entry.key;
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
