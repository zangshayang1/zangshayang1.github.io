---
layout: post
title: DataStructure - ICS261
date: 2017-04-03 13:00:00 -0700
categories: Study-Notes
tag: dataStructure
---
* content
{:toc}


__RECOMMEND:__ [Fundamental Data Structure](https://en.wikipedia.org/wiki/Book:Fundamental_Data_Structures)



#### Stack

Last update: 20170403

__Please refer to__ [Stack](https://zangshayang1.github.io/study-notes/2017/04/03/stack/)

#### Array

Last Modified: 201704012

__Please refer to__ [Array](https://zangshayang1.github.io/study-notes/2017/04/12/array/)

#### Dictionary - HashMap & Hashing

Last Modified: 201704012

__Please refer to__ [HashMap](https://zangshayang1.github.io/study-notes/2017/04/12/hashmap/)

#### Set - BloomFiter/CuckooFilter/InvertedBF

Last update: 20170419

__Please refer to__ [Set](https://zangshayang1.github.io/study-notes/2017/04/19/set/)

#### KthMajorityVote - Counting BloomFiter - CMS - MinHash

Last update: 20170419

__Please refer to__ [Streaming DataStructure](https://zangshayang1.github.io/study-notes/2017/04/24/streamingDataStructure/)

#### Priority Queue

Last update: 20170502

__Please refer to__ [Priority Queue](https://zangshayang1.github.io/study-notes/2017/02/06/priority-queue)

#### Binary Search Tree

Last update: 20170515

__Please refer to__ [Binary Search Tree](https://zangshayang1.github.io/study-notes/2017/05/07/binary-tree/)

#### B Tree

Last update: 20170515

__Please refer to__ [B tree](https://zangshayang1.github.io/study-notes/2017/05/16/B-tree/)

## Tries

### Suffix Tree

__Problem:__ Given a sequence of chars S, find a subsequence q.

__Naive solution:__  
1. Run $$ length(S) \cdot length(q) $$ times of character-wise comparison. Space efficient but slow.
2. Cut S into $$ length(S) \cdot \frac{length(S)}{length(q)} $$ pieces of keys and put them in a HashTable. Quick but spatiolly not efficient.  

__Suffix Array -> Suffix Tree:__  

[Suffix Array](https://en.wikipedia.org/wiki/Suffix_array) can be constructed by special algorithm in linear time. With this data structure, which costs the same space as the input, we can do a binary search to find the target in $$ length(q) \cdot \log{length(S)} $$.  

<img src="{{ '/styles/images/tries/suffixArray.jpg' }}" width="100%" />  

[Suffix Tree](https://en.wikipedia.org/wiki/Suffix_tree) can be constructed from suffix array in linear time.  
A few things:  
1. Internal nodes store pointers only.
2. External nodes store corresponding starting positions.
3. Edges store (shared) suffix along the paths.

<img src="{{ '/styles/images/tries/suffixTree.jpeg' }}" width="50%" />  

__App:__ Suffix Array has good performance in finding the exact match. suffix tree can be viewed as an extension of suffix array as it has good performance with tolerance to mismatch.  

### Tries

__Dictionary Problem:__ query of a word in a collection of words. HashMap is not very useful here because it is easy to run out of memory if the collection is large.

[Trie](https://en.wikipedia.org/wiki/Trie) is the go-to data structure to provide spatially and temporally efficient query. Originally, every node in a trie holds one character. That generates a lot of node that only has one child. So we use one node holding a string to substitude a chain of one-child nodes holding one character, which makes it a __compressed trie__.  

In case we have a large size of alphabet, we would need to go through a large number of comparisons for each step. This is not efficient so we can use a HashMap to store pointers to children rather than using an array.  

## Range Query Problem

Last Update: 20170527

### 1D Range Query

__Problem:__ Given a collection of numbers and a range (L, R), return those that are within the range.  

If the input is __static__, we can sort them in an array and binary search find the indices of two boundary points.

However if the input is __dynamic__, array might not be a good data structure to use. With __regular binary search tree__, we can have insertion/deletion/find in O(logn). So if we find the two boundary nodes, L & R, how many values are in between? However, if we can associate a __rank__ field with each node which indicates the corresponding indices of the node value in the sorted array, _the answer = (the rank of R) - (the rank of L) + 1_.  

[Augmented Binary Search Tree](https://en.wikipedia.org/wiki/Order_statistic_tree)  

```
# To compute rank, we need to compute a "size" for each node during construction
X.size = X.left.size + X.right.size + 1
external.size = 0

# compute rank for node X
def rank(X, root):
  if X is external: return 0
  if X < root: return rank(X, root.left)
  if X == root: return X.left.size + 1
  else: return X.left.size + 1 + rank(X, root.right)

# Given a rank, return the corresponding node value
# simulate array access in O(logn)
def get(rank, root):
  if rank == root.left.size + 1: return root.val
  if rank < root.left.size + 1: return get(rank, root.left)
  else: return get(rank - root.left.size - 1, root.right)
```

If we associate different fields with each node, we can compute different statics quickly, and thus different application. For example - __range min-priority query problem:__ Given that each node is associated with a priority, return the lowest priority among the nodes whose values range from L to R.  

```
# let each node has a min field, computed during construction
X.min = min(X.left.min, X.priority, X.right.min)

def minPriority(L, R, root):
  if root is external or (L = -inf and R = +inf):
    return root.min
  if R < root.val: return minPriority(L, R, root.left)
  if L > root.val: return minPriority(L, R, root.right)
  else: return min(
                   minPriority(L, +inf, root.left),
                   root.priority,
                   minPriority(-inf, R, root.right)
                   )
                   # this part is quite elegant
```

[Other type of range queries](https://en.wikipedia.org/wiki/Range_query_(data_structures))  

### 2D Range Query

Let's discuss two types of [2D range query](https://en.wikipedia.org/wiki/Range_searching) problems:  
1. Find points within an axis-aligned rectangle.
2. Find points above some line in a plane.  

To solve __axis-aligned rectangle problem__, we can use two augmented binary search trees along two axises.

To solve __half plane reporting problem__, we can use: [fractional cascading](https://en.wikipedia.org/wiki/Fractional_cascading#Applications). A little analysis would tell:  
1. It takes $$ O(\log{n} + outputSize)$$ to find all valid points on one polygon.
2. It takes $$ O(\log{n} \cdot outputSize) $$ to return all valid points in the plane.  

From half plane reporting problem, we can abstract the fundamental subproblem: [fractional cascading](https://en.wikipedia.org/wiki/Fractional_cascading#Example). By:  
1. merging half of the elements in the last list to the previous one.
2. for each element, associate it with its position in current list and the position in the next list.
We can achieve a solution for such a problem in O(n) space and O(logn + outputSize) time complexity.  

<!--
buffer
buffer
buffer
buffer
buffer
buffer
buffer
buffer
buffer
buffer
buffer
buffer
buffer
buffer
buffer
buffer
buffer
buffer
buffer
buffer
buffer
buffer
buffer
buffer
-->
