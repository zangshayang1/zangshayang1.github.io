---
layout: post
title:  Algorithms - Priority Queue
date:   2020-09-30 22:00:00 -0700
categories: study-notes
tag: algorithms
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
3. size() - O(1) return number of remaining elements
4. peek() - O(1) return the min/max element without removal

Binary Heap is one way it's implemented.

The invariant for a min binary heap is:
for elements in an array [1, 2, 5, 7, 13, 6, 9 ...], A[k] < A[2k + 1] and A[k] < A[2k + 2]
'''
class MinHeap:

  def __init__(self, nums):
    self.nums = nums
    self.size = len(nums) # mark the end of valid elements
    self.bound = self.size # mark the upper bound of the allocated array
    self._heapify()

  # Why _siftUp the last element? Why not _siftDown first element?
  # The leave level stores 50% of all the nodes.
  # There is a 50% chance that no operation is needed on the pushed element.
  # while _siftDown first element is most likely needed.
  def push(self, element):
    # Check the necessity for doubling down memory allocation
    if self.size == self.bound:
      for _ in self.size:
        self.nums.append(None)
      self.bound = self.bound * 2 + 1

    self.nums[self.size] = element
    self.size += 1
    self._siftUp(self.size - 1)

    return ;

  # Time complexity: O(logn)
  def pop(self):
    tmp = self.nums[0]
    self.nums[0] = self.nums[self.size - 1]
    self.size -= 1
    self._siftDown(0)

    return tmp;

  def size(self):
    return self.size ;

  def peek(self):
    return self.nums[0]

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
    for i in range(len(self.nums) - 1, -1, -1):
      self._siftDown(i)

  # Same as # Ref: _siftUp(self, i)
  def _siftDown(self, i):
    minidx = i
    l, r = 2 * i + 1, 2 * i + 2
    if l < self.size and self.nums[l] < self.nums[minidx]:
      minidx = l
    if r < self.size and self.nums[r] < self.nums[minidx]:
      minidx = r

    if minidx != i:
      swap(self.nums, i, minidx)
      self._siftDown(minidx)

    return ;

  # Time complexity: O(logn) - textbook
  def _siftUp(self, i):
    p = (i - 1) // 2
    if p >= 0 and self.nums[p] > self.nums[i]:
      swap(self.nums, i, p)
      self._siftUp(p)

    return ;
```

# Helper Function

```python
def swap(nums, i, j):
  tmp = nums[i]
  nums[i] = nums[j]
  nums[j] = tmp
```
