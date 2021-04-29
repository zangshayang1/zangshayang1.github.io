---
layout: post
title: DataStructure - Streaming DataStructure
date: 2017-04-24 13:00:00 -0700
categories: study-notes
tag: data-structure
---
* content
{:toc}




Last Modified: 201704024

## Streaming Data Structure

__Characteristics:__ [Streaming algorithm](https://en.wikipedia.org/wiki/Streaming_algorithm) computes some non-trivial properties of the input sequence in a single path using constant space.

### Warm up - Majority Vote Algorithm

__Problem:__ Find the majority out of a sequence of values.

__Tentative Solutions:__
1. HashTable, taking O(n) time and O(n) space.
2. Double loop with global optimal, taking O(n^2) time and O(1) space.

__Majority Vote Algorithm:__

``` python
def majorityVote(seq):
  majority = Pair(value == None, counter = 0)

  for x in seq:
    if majority.counter == 0:
      majority.value = x
    if majority.value == x:
      majority.counter += 1
    else:
      majority.counter -= 1

  return majority.value
```

__What the above algorithm says is:__ If there is no majority (counter == 0), whatever comes along next become the majority.

__Note:__ Given any input sequence without a clear majority, such as (1, 1, 2, 2, 3), the algorithm will not return anyone with the highest count.

__Claim:__
1. If there is a clear majority in input sequence of n elements, there are at most (n/2) times of counter decrements.

2. $$ O_{x} - {\frac{n}{2}} $$ <= estimate(x) <= $$ O_{x} $$ ($$ O_{x} $$: number of actual occurrences of elements x)

$$ estimate(x) = {\begin{cases}  majority.counter & {\text{ if }} x = majority.value \\ 0 & {\text{ otherwise }} \end{cases}} $$

__Generalization of Majority Vote Algorithm - estimate the number of occurrences of each element within some error range.__

For example, given a sequence of input, find the most majority one, the second to the most majority, the third ..., the kth.

``` python
def kthMajorityVote(seq, k):

  # some special data structure is needed for this algorithm to work
  # assuming we have such abstract data structure that:
  # 1. holds k pairs of (value, counter) subject to in-place modification
  # 2. O(1) lookup time
  # 3. return the next available pair whose counter == 0
  ads = AbstractDataStructure()

  for x in seq:
    if x not in ads and ads.next().counter == 0:
      ads.next().value = x
    if x in ads:
      increment counters of all pairs in ads
    else:
      decrement counters of all pairs in ads

  return ads
  # now ads holds counter info of each value in seq that we can use to estimate the actuall occurrences of each value.
```

__Claim:__ With the same estimate function, we have:  

$$ O_{x} - {\frac{n}{k}} $$ <= estimate(x) <= $$ O_{x} $$  

Meaning: we have an estimated occurrences within (n/k) error range for each element in sequence.

#### Counting BloomFilter

1. Instead of using bitArray, CBF uses IntArray to accumulate the frequencies of keys checking in and out. (size = const * number of keys)
2. With a set of K hash functions, every key corresponds to K different cells.
3. Insertion(item) increments corresponding cells.
4. Deletion(item) decrements corresponding cells.
5. Membership(item) tests if all corresponding cells are non-zero.

#### Count Min Sketch

[Count Min Sketch](https://en.wikipedia.org/wiki/Count%E2%80%93min_sketch) __is built based on CBF with 2 differences:__
1. Essentially both can be implemented with 1D vector. But for the sake of clarity, CMS is often implemented with 2D matrix.
2. With fewer total cells, this data structure can gives us better approximation of frequencies of items, given that they constantly checks in and out in the input stream.

__Implementation Details:__
1. A 2D array to store a counter in each cell: table = int[w][k], where:  
* $$ w = {\frac{e}{\epsilon}} $$
* $$ \epsilon $$ defines the estimate accuracy.  
* e is natural logarithm base.  
* $$ k = \ln({\frac{1}{\delta}}) $$  
* $$ \delta $$ defines the probability of NOT getting that accuracy.  

2. Again, k hash functions, mapping each key to [one cell in each row for every row in the table].

``` python
# with k, table, hashFunctions() defined as above
def update(key, x):
  # checkin: x positive
  # checkout: x negative
  for i in range(k):
    table[i][hash_i(key)] += x
  return ;

def approxFreq(key):
  # get the estimated frequencies of the key
  return min([table[i][hash_i(key)] for i in range(k)])
```

__Analysis:__
1. The _approxFreq(key)_ defined above returns an estimated frequency of the key, which = [actual frequency] + [noise from other keys].
2. Let n be the length of the CMS, which is the number of remaining items. $$ E[noise] \leq {\frac{n}{w}} $$
3. [Markov's Inequality](https://en.wikipedia.org/wiki/Markov%27s_inequality) brings it the following: The probability of [noise in one row $$ \geq \epsilon n $$] $$ \leq {\frac{1}{e}} $$
4. So: The probability of [such big noise in all rows] <= $$ {\frac{1}{e}}^{k} = \delta $$ This is the probability of NOT getting that pre-defined accuracy.

__Note:__
The idea behind the design of the data structure is the space taken by all the cells (w * k) should be much smaller than the what we need to store exactly everything in the input stream.

__Use Case: compute document similarity__

1. In __"Bag of Words" model__, each document corresponds to a long vector, storing the frequencies of all the words in our vocabulary.
2. __Cosine Similarity__ between doc A and doc B is computed as:  
$$ \frac{\overrightarrow{\rm A} \cdot \overrightarrow{\rm B}}{\sqrt{ \| \overrightarrow{\rm A} \| \cdot \| \overrightarrow{\rm v} \|}} $$  
which is also the angle [0, 90] of the two vectors in vector space.  

3. With __vector generalization of CMS__, for each document, let words stream through CMS, now each document corresponds to a CMS table.
4. Approximate similarity between docA and docB by computing:  
* dotProduct(row_i_of_cmsA, row_i_of_cmsB) for each row
* return the minimum value from them.  
5. The returned value will be an estimate of the dotProduct(vectorA, vectorB) in "Bag of Words" model. The probability of getting the estimate within [actual, actual + $$ \epsilon $$ cmsA.length cmsB.length], where cmsA.length is the sum of the absolute value of each cell in that row in cmsA.

__Note:__ When we compute the cosine similarity between two documents, we used their norm2 lengths. However, when we approximate the similarity between two documents __where a lot of words occur only once__, the accuracy deviation is proportional to the number of words, which is large.

__Review and Compare IBF, MajorityStream, CMS__

1. IBF allows stream in and out, returns very few remainings eventually.  
2. MajorityStream allows stream in only, uses constant space and always underestimates frequencies of any item.    
3. CMS allows stream in and out, gives overestimated frequencies of any item in a relatively efficient way of using limited space.

### MinHash

The use of CMS in computing documents similarity help reduce the computation complexity and space taken because:  
1. Without CMS, each document corresponds to a words-frequencies table and you have to compare it with other words-frequencies tables.
2. With CMS, you reduce the space usage to constant (no matter how long the doc is, the size of CMS doesn't change) and further simplify the computation.

Another way to measure documents similarity is __Jaccard Similarity__:    
$$ Jaccard(A, B) = \frac{\| A \cap B \|}{ \| A \cup B \| } $$

where A is a set of words occurred in docA and B is a set of words occurred in docB.  
This way, we only need to store boolean value for each word instead of frequencies. __Our question__ is: any way to reduce the space usage of the SET data structure in this case?  

__Consider the following case:__  
1. Stream all the words from docA and docB through some hash function.
2. Represent the set of "words-frequencies" by the minimum hash value throughout the stream.
3. If the minimum hash value from streamA == the minimum hash value from streamB, the estimated similarity(A, B) = 1. Otherwise, 0.
4. This way, a random hash function makes a random estimator of their similarity.
5. __claim:__ Expected[estimated similarity] approximate J(A, B).

__Multi-hash version MinHash__
1. uses K hash functions.
2. represents the set of "words-frequencies" by a collection of K minimum hash values.
3. similarity = the number of overlapped values in collectionA and collectionB / K

__Single-hash version MinHash__
1. uses 1 hash function.
2. represents the set of "words-frequencies" by a collection of t minimum hash values. (coming out of the same hash)
3. similarity = [the number of overlapped values in collectionA and collectionB] / t
