---
layout: post
title: DataStructure - Range Minima Problem
date: 2017-05-27 13:00:00 -0700
categories: study-notes
tag: dataStructure
---
* content
{:toc}




## Range Query Problem

### 1D Range Query

__Problem:__ Given a collection of numbers and a range (L, R), return those that are within the range.  

If the input is __static__, we can sort them in an array and binary search find the indices of two boundary points.

However if the input is __dynamic__, array might not be a good data structure to use. With __regular binary search tree__, we can have insertion/deletion/find in O(logn).  

__Problem continue:__ If we find the two boundary nodes, L & R, how many values are in between?  

BST can no longer gives us O(logn) performance and array implementation cannot provide dynamic updates. This problem introduces [Augmented Binary Search Tree](https://en.wikipedia.org/wiki/Order_statistic_tree) where we can associate a __rank__ field with each node which essentially indicates the position of the node if their values are sorted. _The answer to the problem = (the rank of R) - (the rank of L) + 1_.  

```
# To compute rank, we need to compute a "size" for each node during construction
X.size = X.left.size + X.right.size + 1
external.size = 0

# compute rank for node X
# note the how we define rank and size differently
def rank(X, root):
  if X is external: return 0
  if X < root: return rank(X, root.left)
  if X == root: return X.left.size + 1
  # note how we recursively compute rank based on size.
  # and how it affects the get() method defined below.
  else: return root.left.size + 1 + rank(X, root.right)

# Given a rank, return the corresponding node value
# simulate array access in O(logn)
def get(rank, root):
  if rank == root.left.size + 1: return root.val
  if rank < root.left.size + 1: return get(rank, root.left)
  else: return get(rank - root.left.size - 1, root.right)
```

If we associate different fields with each node, we can compute different statics quickly, and thus different application.  

As an extension, for example - __range min-priority query problem:__ Given that each node is associated with a priority, return the lowest priority among the nodes whose values range from L to R.  

```
# let each node has a min field, computed during construction
external.min = +inf
X.min = min(X.left.min, X.priority, X.right.min)

def minPriority(L, R, root):
  # X.min field gives the min value of the whole tree rooted at X.
  # when the range is wide open
  if root is external or (L = -inf and R = +inf):
    return root.min
  if R < root.val: return minPriority(L, R, root.left)
  if L > root.val: return minPriority(L, R, root.right)
  # global min is in between:
  #   the left tree with the left boundary
  #   the right tree with the right boundary
  #   the root itself.
  else: return min(
                   minPriority(L, +inf, root.left),
                   root.priority,
                   minPriority(-inf, R, root.right)
                   )
                   # this part is quite elegant
```

Addition: [Other type of range queries](https://en.wikipedia.org/wiki/Range_query_(data_structures))  

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

### LCA and Cartesian Tree

Last update: 20170607  

#### Background

__River network problem:__ River mouth is the root, pointing at one or more children node, which point at some more. Each node stores the distance between itself and the root. Given two node X, Y, return their minimum distance.  

__Naively:__ Of course, one can use a 2D array to store every distance of such pair(X, Y) and returns the query in O(1). But it will be more space efficient if we can store the info in a Tree structure and still provide fast queries.    

__Problem Modeling:__
```
dist(X, Y) = dist(X, root) + dist(Y, root) - 2 * dist(LCA(X, Y), root)

* LCA: lowest common ancestor.
```

Note:
1. If we traverse the tree from the root, it takes O(logn) to find X.
2. If we have a mapping relation, we can find X in O(1).

__Problem Reduction:__ River network problem reduced to LCA problem. The river network problem forms a tree structure that is [cartesian tree](https://en.wikipedia.org/wiki/Cartesian_tree) in computer science. __Cartesian Tree__ can be built up from any sequence of numbers in linear time, which means _range minimum problem can be reduced to LCA problem_. Reversely, with [Euler tour](https://en.wikipedia.org/wiki/Eulerian_path), _LCA problem can be reduced to range minimum problem_ through the following process:

<img src="{{ '/styles/images/binaryTree/lca_rangeMin.jpg' }}" width="100%" />

__New Problem:__ Can we construct a cartesian tree out of a sequence input and find LCA efficiently?  

__Linear Time Construction of Cartesian Tree:__

Scan through the sequence and build it from left to right (init root using the first number):  

Essentially, the construction comes down to 3 possibilities:  
1. if the next number < root : goes on top of the tree.
2. if the next number > root and the previous number : goes to right of the previous number.
3. if the next number > root but it < the previous number : take over the place of the previous number and make the previous one be its left child.

<img src="{{ '/styles/images/cartesianTree/construct_cartesianTree.jpg' }}" width="100%" />

``` python
""" Example Input: A = [20, 12, 5, 17, 13] """

A = [new Node(A[i]) for i in range(len(A))]
root = A[0]
for i in range(1, len(A)):
  if A[i].val < root.val:
    A[i].left = root
    root = A[i]
  if A[i] > A[i-1]:
    A[i-1].right = A[i]
  if A[i] < A[i-1]:
    A[i-1].parent.right = A[i]
    A[i].left = A[i-1]
```

__LCA query:__

As I mentioned in the above "Problem Reduction" section, LCA can be reduced to range minima problem. Although it defeats the purpose, it provides us with the first __naive solution:__ build a 2D array of each pair of numbers, taking O(n^2) space but answering queries in O(1). We will revisit range minima problem as follows and come up with a solution taking O(n) time to preprocess, O(nlogn) space and O(1) to answer query.  

#### Revisit Range Minima Problem

__Idea__ Every range can be decomposed of a constant number of smaller, maybe overlapping, sub ranges. The minima to a query will be the minima of all these sub ranges. Can we use some additional space to store the minima of sub ranges at different "granularity" so that queries can be answered in constant time?  

__Dynamically generate minima table:__  

Example Input: A[n]  

Preprocess:  
``` Python
""" Let T[i, k] = min(A[i], A[i+1], ..., A[i + 2**k - 1]) """

for k in range(0, floor(math.log(n))):
  for i in range(0, n - 2**k ):
    if k == 0:
      T[i, k] = A[i]
    else:
      T[i, k] = min(T[i, k-1], T[i + 2 ** (k-1), k-1])
      # think about the above line of code
      # think about how we define T[i, k]
      # think about DP
```

Query:  
``` Python
def query(i, j):
  k = floor(math.log(j - i + 1))
  return min(T[i, k], T[j - 2**k + 1, k])
```

<img src="{{ '/styles/images/rangeMinQuery/subRangeMinQuery.jpg' }}" width="100%" />

__Generalization:__
Micro/Macro Structure Strategy, storing constant-amount of additional info for each block and provide O(n) space O(1) time solution.  

<img src="{{ '/styles/images/rangeMinQuery/microMacro1.jpg' }}" width="80%" />

<img src="{{ '/styles/images/rangeMinQuery/microMacro2.jpg' }}" width="100%" />

### Van Emde Boas tree

__Problem Intro:__ In an integer searching problem, use balanced BST gives O(logn) performance, but [Van Emde Boas tree](https://en.wikipedia.org/wiki/Van_Emde_Boas_tree) gives O(log(logn)) performance, which is exponentially better than BST. Van Emde Boas tree can be used to accelerate integer ONLY searching problem because it exploits the fact that it always takes one machine  word of size W to store one W-bit integer and thus we can perform bit manipulation on integers really fast.  

__Bit Manipulation Involved:__
1. get the upper half bits on a W-bit integer x: x >> W/2
2. get the lower half bits on a W-bit integer x: x & ((1 << W/2) - 1)

__Idea:__ Form a hierarchical storage of the input, ordered by the numerical value of upper half bits. So in the case of an input set of size S, there will be at most $$ \sqrt{S} $$ categories, each contains at most $$ \sqrt{S} $$ elements, which will be categorized into $$ \sqrt[{4}]{S} $$ categories recursively.

__Visualize:__

<img src="{{ '/styles/images/flatTree/flatTree.jpg' }}" width="100%" />

__Algorithm to find predecessor:__  

```
# example input: 1, 2, 3, 4, 5 -> constructing a Van Emde Boas tree T.
# example output: 2 = predecessorOf(3, T)

def predecessorOf(x, T):
  if max < x: return max

  uh = upperHalf(x)

  if uh in hashTable:
    t = hashTable[uh]
    # compare with the lowerHalf(x)
    if t.min < lowerHalf(x):
      # In the recursive call, the first thing to check is t.max
      return predecessorOf(lowerHalf(x), t) + uh

    # if the above if-clause doesn't meet, do the following:
    # if uh in hashTable, uh must be in the upperTable as well
    t = upperTable[uh]
      if predecessorOf(lowerHalf(x), t) is found:
        return t.max

  # if uh not found in the hashTable
  elif min < x:
    return min

  else:
    return NOT FOUND (some extreme value will do)
```
