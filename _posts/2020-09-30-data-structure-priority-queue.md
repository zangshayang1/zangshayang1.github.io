---
layout: post
title:  Data Structure - Priority Queue
date:   2020-09-30 22:00:00 -0700
categories: study-notes
tag: data structure
---

* content
{:toc}


# Binary Heap

## Min Heap Implementation
```python
'''
Priority Queue data structure should support the following operations:
1. pop() - O(logn) pop min element or max element
2. push() - O(logn) add element
3. length() - O(1) return number of remaining elements
4. peek() - O(1) return the min/max element without removal

Binary Heap is one way it's implemented.

The invariant for a min binary heap is:
for elements in an array [1, 2, 5, 7, 13, 6, 9 ...], A[k] < A[2k + 1] and A[k] < A[2k + 2]
'''
class PriorityQueueItemInterface:
  '''
  compare function to support item level comparison
  return 1 when this > item
  return 0 when this == item
  return -1 when this < item
  '''
  def compare(self, item): -> Int
    pass

class MinHeap:

  def __init__(self, elements: PriorityQueueItemInterface):
    self.elements = elements
    self.size = len(elements) # mark the end of valid elements
    self.bound = self.size # mark the upper bound of the allocated array
    self._heapify()

  # Why _siftUp the last element? Why not _siftDown first element?
  # The leave level stores 50% of all the nodes.
  # There is a 50% chance that no operation is needed on the pushed element.
  # while _siftDown first element is most likely needed.
  def push(self, element): -> None
    # Check the necessity for doubling down memory allocation
    if self.size == self.bound:
      self.bound = 2 * self.size + 1
      for _ in range(self.size + 1):
        self.elements.append(None)

    self.elements[self.size] = element
    self.size += 1
    self._siftUp(self.size - 1)

    return ;

  # Time complexity: O(logn)
  def pop(self): -> PriorityQueueItemInterface
    tmp = self.elements[0]
    self.elements[0] = self.elements[self.size - 1]
    self.size -= 1
    self._siftDown(0)

    return tmp;

  def length(self): -> int
    return self.size ;

  def peek(self): -> PriorityQueueItemInterface
    return self.elements[0]

  # A sub-optimal implementation to heapify is applying _siftUp to all the elements
  # which gives O(nlogn).
  # Why is this the optimal implementation?
  # Assuming that we are apply _siftDown to a node at height h
  # at most h step is needed to place this node at the right position
  # Same number applies all other nodes at the same height.
  # There are 2^(logn) nodes in total and 2^(logn)/2^h nodes at this height.
  # 2^(logn)/2^h <= n / 2^h
  # Time Complexity: O(n*h / 2^h) = O(n) when n and h are close to infinity
  def _heapify(self):
    for i in range(self.size - 1, -1, -1):
      self._siftDown(i)

  # Same as # Ref: _siftUp(self, i)
  def _siftDown(self, i):
    minidx = i
    l, r = 2 * i + 1, 2 * i + 2
    if l < self.size and self.elements[l].compare(self.elements[minidx]) < 0:
      minidx = l
    if r < self.size and self.elements[r].compare(self.elements[minidx]) < 0:
      minidx = r

    if minidx != i:
      self._swap(self.elements, i, minidx)
      self._siftDown(minidx)

    return ;

  # Time complexity: O(logn) - textbook
  def _siftUp(self, i):
    p = (i - 1) // 2
    if p >= 0 and self.elements[p].compare(self.elements[i]) > 0:
      self._swap(self.elements, i, p)
      self._siftUp(p)

    return ;

  def _swap(self, nums, i, j):
      nums[i], nums[j] = nums[j], nums[i]
```

# Correctness Check

```python

import random

class MyInt(PriorityQueueItemInterface):
  def __init__(self, value):
    self.value = value

  def compare(self, otherInt):
    if self.value > otherInt.value:
      return 1
    if self.value == otherInt.value:
      return 0
    if self.value < otherInt.value:
      return -1

class MinBinaryHeapImplementationCheck:

  def __init__(self):
    self.seq = [MyInt(random.randrange(1000)) for _ in range(1000)]
    self.sseq = sorted(self.seq, key = lambda x : x.value)

  def check1(self):
    pq = MinHeap(self.seq)
    for i in range(len(self.seq)):
      if pq.pop().compare(self.sseq[i]) != 0:
        print(self.sseq[i].value)
        raise Exception("Data Structure Correctness Check: Failed")

  def check2(self):
    pq = MinHeap([])
    for i in range(0, len(self.seq)):
      pq.push(self.seq[i])

    pq.push(MyInt(-1))
    if pq.pop().compare(MyInt(-1)) != 0:
      raise Exception("Data Structure Correctness Check: Failed")
```
