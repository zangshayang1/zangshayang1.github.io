---
layout: post
title: DataStructure - Priority Queue
date: 2017-02-05 16:20:00 -0800
categories: study-notes
tag: data-structure
---

* content
{:toc}



## Priority Queue

Last update: 20170504

#### Generals:
1. Priority Queue is such a data structure that associates objects with priorities (in whatever sortable format.)
2. Operations include:  
* create such a queue from a given list of values
* find and pop min/max - element with highest priority
* change elements' priority and keep the queue updated

__Classic Use Case:__ [Dijkstra's Algorithm](https://zangshayang1.github.io/study-notes/2017/02/08/greedy-algorithm/#greedy-algorithm-to-solve-graph-problems) where, in a G = (V, E), abs(V) times of "find and pop" operation is performed and abs(E) times "change and update" operation is performed. Because abs(E) >> abs(V) and the following binary heap implementation provides very slow "change and update" operation, the following provides a tailored implementation and an alternative algorithm to do the same thing.  

``` python
class PriorityQueue():
  def __init__(self):
    self.pq = {} # new HashMap()

  def heapify(self, elements): # O(n)
    for element in elements:
      self.pq[id(element)] = element
    return ;

  def popMin(self): # O(n)
    minEle = PQElement(None, Integer.MAX_VALUE) # placeholder for item and its priority
    minKey = None
    for key in self.pq:
      element = self.pq[key] # whatever it is
      if element.priority >= minEle.priority:
        continue
      minEle.item = element.item
      minEle.priority = element.priority
      minKey = key
    del self.pq[key]
    return minEle

  def add(self, PQElement):
    self.pq[id(PQElement)] = PQElement
    return ;

  def updatePriorityFor(self, element, priority): # O(1)
    self.pq[id(element)].priority = priority
    return ;
```

```
# Pseudocode
def Edge-based-Dijkstra(): # linear to the abs(E)
  initialize a D = {startVertex : 0} mapping each vertex to its distance to the start vertex
  initialize a PQ = PriorityQueue().heapify(List(edges outgoing from startVertex))
  initialize curtVertex = startVertex
  while curtVertex is not terminalVertex:
    nextEdge = pq.popMin().item # pq.popMin() return PQElement obj - O(V)
    nextVertex = nextEdge.to() # edge from curtVertex to some vertex
    if nextVertex in D:
      curtVertex = nextVertex
      continue
    for edge in nextVertex.outgoingEdges():
      priority = D[curtVertex] + edge.length
      PQ.add(PQElement(edge, priority)) # O(1)
      curtVertex = nextVertex
return D[curtVertex]
```


### Binary Heap Implementation of Priority Queue

#### Notes

1. __O(logn) for all add/delete/min operations and O(n) for heapify()__  
2. __Binary Heap is an implicit data structure, meaning:__  
  * No extra space required beyond what is needed to store an array.  
  * Extra information is how items are order in the array.  
3. __No "change and update" operation__ is implemented because it is going to take: (unless a HashMap is used to track its position in the array.)  
* O(logn) to find the element
* O(1) to change its priority
* O(logn) to update its place in the queue

---
#### Implementation Generals

__Heapify(),__ which, given a list of sortable items, can be implemented by:
1. Calling __\_siftDown()__ from the last item to the first item gives O(n) complexity, while O(nlogn) for calling __\_siftup()__ from the other direction.  
2. Recursively calling heapify() on left tree rooted at the 2nd item and right tree rooted at the 3rd item. (At last, 1st item needs to be sifted downward to its appropriate position.) Refer to [divide and conquer.](https://zangshayang1.github.io/study-notes/2017/02/28/divide-and-conquer/#construct-a-binary-heap-recursively)

__Insert(),__ is done by adding item to the end of the array and sift it up.

__MinPop(),__ is done by replacing the value of the 1st item with the value of the last item in the array and call siftDown() on the item.  

We talked about __Update(),__ in the __Note__ above.

#### Siftdown is more "efficient" than siftup

__To clarify:__
In building a binary heap, the average cost of calling siftdown() is much lower than siftup(). But a single siftup operation cost logn while a single siftdown operation costs 2 * logn, where n is the number of total nodes.  

__Intuitively:__
Review [the sum of depth and height](https://zangshayang1.github.io/study-notes/2017/05/07/binary-tree/#binary-tree) of a binary tree, it is obvious that the sum of depth of each node is greater than the sum of height of each node. The former corresponds to the potential number of "upward swaps" required while the latter corresponds to the potential number of "downward swaps" required.

__The following can be proved by induction:__

$$ \sum_{node\;v} [\;number\;of\;up-swap] = n\log{n} - O(n) $$

$$ E[\;number\;of\;up-swap] = O(\log{n}) $$

$$ \sum_{node\;v} [\;number\;of\;down-swap] = n - O(\log{n}) $$

$$ E[\;number\;of\;down-swap] = O(1) $$

__Formal proof:__  
_given an array of length N. N is also the total number of nodes on the implicit tree, whose root has a height of floor(log(N))._  
_For any node at height of j, at most j siftDown() operations needed to find its correct place._  
_For any node at height of j, aka at depth of [(floor(logN) - j]. There are:_ $$ 2^{floor(logN) - j} $$ _such nodes at the same level._  

$$ 2^{floor(logN) - j} < O(\frac {n} {2^j}) $$  

$$ Total Time \le O(\sum_{j=0}^{logN} j \frac {n} {2^j}) \le O(N \sum_{j=0}^{\infty} \frac {j} {2^j}) = O(n) $$  

---

#### Implementation Details

``` python
class MinHeap(object):
  # minHeap - k < 2k + 1 and k < 2k + 2 - support heapify(), push(), pop(), top(), isEmpty(), len() methods etc.
  def __init__(self, nums):
    self.size = len(nums)
    self.array = [num for num in nums]
    self.heapify()

  def heapify(self):  
    if self.size == 0:
      return ;

    # _siftUp N elements takes O(NlogN)
    # _siftDown N elements takes O(N)
    for i in xrange(self.size - 1, -1, -1):
      self._siftDown(i)
    return ;

  def _siftUp(self, k):
    # when k == 3 || 4, (k - 1) / 2 == 1 holds for both values
    while k > 0:
      # if k > 0 but it can no longer siftup: break
      if (k - 1) / 2 < 0 or self.array[(k - 1) / 2] <=
      self.array[k]:
        break

      self.array[(k - 1) / 2], self.array[k] = self.array[k], self.array[(k - 1) / 2]
      k = (k - 1) / 2

      # reversely:
      # if (k - 1) / 2 >= 0 and self.array[(k - 1) / 2] > self.array[k]:
      # 	self.array[(k - 1) / 2], self.array[k] = self.array[k], self.array[(k - 1) / 2]
      # 	k = (k - 1) / 2
      # else:
      # 	break

    return ;

  def _siftDown(self, k):
    while 0 <= k < self.size:
      idx_of_min = k # assume k is the smallest among itself and its children
      if 2 * k + 1 < self.size and self.array[2 * k + 1] < self.array[idx_of_min]:
        idx_of_min = 2 * k + 1
      if 2 * k + 2 < self.size and self.array[2 * k + 2] < self.array[idx_of_min]:
        idx_of_min = 2 * k + 2
      if idx_of_min == k: # no need to change
        return ;
      else:
        self.array[k], self.array[idx_of_min] = self.array[idx_of_min], self.array[k]
        k = idx_of_min
    return ;


  def add(self, num):
    # add cost - O(logn)
    self.size += 1
    self.array.append(num)
    self._siftUp(self.size - 1)
    return ;

  def popMin(self):
      # a minHeap pops out the min value
      return self.delete(0)


  def delete(self, idx):
    # delete operation takes in the idx of the element you want to delete
    # different from adding element taking in its value
    # if finding the element takes more than O(1), delete operation would take more than O(nlogn)
    # after find the element, it's either going to be siftDown or Up.
    # delete cost - O(logn)
    ret = self.array[idx]
    self.array[idx] = self.array[self.size - 1]
    del self.array[self.size - 1]
    self.size -= 1
    if idx == 0 or self.array[idx] > self.array[(idx - 1) / 2]:
      self._siftDown(idx)
    else:
      self._siftUp(idx)
    return ret
```

### K-ary Heap

If we __rethink about the Dijkstra's__ Algorithm in G(V, E):  
1. abs(V) times minPop() required. Can we improve this? Not really, since sorting itself cannot beat O(nlogn). Each siftDown() operation after minPop() is necessary and it takes O(logn).  
2. abs(E) times siftUp() is required(priority-decrease).
3. Can we improve the average cost of siftDown()?

__Answer is Yes:__ We have seen that the average cost of siftDown is lower than siftUp in binary heap __thanks to its 1-parent-2-children structure__. Can we further lower the average cost of siftDown by expanding it to [K-ary Heap](https://en.wikipedia.org/wiki/D-ary_heap)? Yes, but it also increase the cost of single siftDown operation as shown below.

$$ siftDown => O(k \cdot \log_{k}{n}) $$

$$ siftUp => O(\log_{k}{n}) $$

In this case, Dijkstra takes:  

$$ O(V \cdot k\log_{k}{V} + E \cdot \log_{k}{V}) $$.

The optimal can be achieved when we strike a balance between these two terms, namely:  

$$ k = {\frac{E}{V}} $$

Now Dijkstra takes:  

$$ O({\frac{E \cdot \log_{2}{V}}{\log_{2}{\frac{E}{V}}}}) $$

Where the numerator is the running time with binary heap and the denominator is the speed up factor we can achieve by optimizing K according to the given graph. _Beautiful!_



Last update: 20170515

### Fibonacci Heap

#### Structure Intuition

The name came from the following heap __structure__: __Imagine__ 16 vertices holding values from 1 to 16. They can be viewed as 16 trees and each only has one root node. Now, every two of them merge into a 2-node tree with the smaller one being the root. Now you have 8 trees. Keep merging till they become one tree with the smallest value being the root.  

<img src="{{ '/styles/images/priorityQueue/fibonacciHeap.jpg' }}" width="100%" />

__Interesting property:__ every node's _ith_ child has at most _(i-1)_ children (this node's grandchildren). With the following invariants, it also holds that every node's _ith_ child has at least _(i-2)_ children.  

#### Invariants  

__The invariants__ are:  
1. We will mark the node (such as 5) when one of its children is removed and the node itself is not a root. If the other children is also subject to removal, the marked parent will spin off to become a root itself. (Of course, after 5 left, node 1 will be marked.)  
2. We will merge two trees (such as those rooted at 1 and 5), if they have the same number of children.  

With the above __invariants__ in mind, let's look at its __operations__:  

#### Insert - amortized O(1)

Create a new root node for the inserted value.

#### Modify - decrease priority - amortized O(1)

Quote from Wiki: _"Operation decrease key will take the node, decrease the key and if the heap property becomes violated (the new key is smaller than the key of the parent), the node is cut from its parent. If the parent is not a root, it is marked. If it has been marked already, it is cut as well and its parent is marked. We continue upwards until we reach either the root or an unmarked node. Now we set the minimum pointer to the decreased value if it is the new minimum. In the process we create some number, say k, of new trees. Each of these new trees except possibly the first one was marked originally but as a root it will become unmarked. One node can become marked. Therefore, the number of marked nodes changes by −(k − 1) + 1 = − k + 2. Combining these 2 changes, the potential changes by 2(−k + 2) + k = −k + 4. The actual time to perform the cutting was O(k), therefore (again with a sufficiently large choice of c) the amortized running time is constant."_

#### Min Pop - amortized O(logn)

If you refer to the following __implementation details:__, you will know that "find minimum" is trivial.  

Once find, we need to delete it, which leaves its children unrooted (because minimum must be one of the root nodes).  
These children then become new roots, which may be subject to MERGE.  
Let's take a look at the merge operation:  

```
Make an empty array A with size of $$ \log{n} $$ to rearrange each root. (the size will be enough thanks to this step.)  
Given a list L storing all the roots.
While L is not empty:
  remove a root r1 from L
  if A[the number of children of r1] is empty:
    A[the number of children of r1] = r1
  else:
    r2 = A[the number of children of r1]
    make A[the number of children of r1] empty
    merge r1 and r2 with the smaller one being the new root and add the new root to L
```

Eventually, everything in L will be transferred to A, one spot for one root.

#### Implementation Details

1. roots are connected via doubly LinkedList.
2. maintain one pointer to the minimum root.
3. each value is encapsulated in an object, which also includes:
* pointers to its children
* a boolean flag (marked true if one of its children is removed and it is not a root)
* number of children (for merge use)

__NOTE:__  
Although Fibonacci is faster in theory, but with all the implementation overhead, it's often outperformed by K-nary heap in practice.  

#### Potential function for Fibonacci Heap

Just in case you ever try to do an amortized analysis on the operations above, use:

$$ \Phi $$ = # of roots + 2 * true flags  


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
-->
