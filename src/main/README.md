# Discussion
For my implementation of OpenAddressingHashMap, I used quadratic probing as opposed to linear probing. 
At first, I had used linear probing but upon running the JmhRuntimeTest on both the linear and quadratic probing methods,
I determined that for the purposes of the search engine, quadratic probing was better. The implementation of OpenAddressingHashMap
using quadratic sorts performed faster on every single text file than the implementation using linear probing. There were, however, some 
tradeoffs with space used when comparing quadratic and linear probing. For example for apache.txt, quadratic probing used less space (121055680 bytes for the
implementation using quadratic probing as opposed to the 122090756 bytes for the implementation using linear probing). However, for newegg.txt, quadratic probing used more space 
(71036824 bytes as opposed to 69126200 bytes). Whether or not quadratic probing used more or less space than linear probing depends on the exact contents contained in each file, but for every 
file tested quadratic performing built the map faster, so I determined that quadratic probing was the better probing strategy to use for OpenAddressingHashMap. 

Furthermore, I tested decreasing the threshold load factor value for OpenAddressingHashMap from 0.75 to 0.5. The performance for the implementation with a threshold load factor of 0.5 either performed marginally
faster than the implementation with a threshold load factor of 0.75, or in the case of random164.txt the implementation with a threshold of 0.5 performed significantly slower (397.831 ms/op as compared to 370.597 ms/op
for a threshold load factor of 0.75). Furthermore, for random164.txt the implementation using a load factor of 0.5 used significantly more space, by an order of 10. Thus, I determined that a threshold load factor of 0.75
(like used in Java's built in hash table) is more optimal than a lower threshold load factor like 0.5. 

When comparing, OpenAddressingHashMap (with quadratic probing and a threshold load factor of 0.75) with ChainingHashMap (with LinkedList as the auxiliary data structure and an equivalent threshold load factor), OpenAddressingHashMap
stands out as the better implementation for a search engine. This is because for every single text file, the OpenAddressingHashMap was able to build the map faster and use less memory. This result is especially pronounced in 
larger text files. For example, for random164.txt the OpenAddressingHashMap implementation is 35.192 seconds faster and uses more than 100 million fewer bytes than the ChainingHashMap implementation. For a search engine, search 
speed is incredibly important (with faster being better) and efficient memory (with less being better) is equally significant to be able to quickly process and parse through billions of keywords and URLs. 
Thus, the implementation that is faster and uses less memory is the better implementation and thus the OpenAddressingHashMap is the better implementation to use for this search engine. 