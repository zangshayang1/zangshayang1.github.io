---
layout: post
title:  Algorithms - Sort
date:   2020-09-13 15:00:00 +0800
categories: study-notes
tag: algorithms
---

* content
{:toc}



# Quick Sort

## Classic Quick Sort
---
```python
class ClassicQuickSort:

  def quickSort(self, nums):

    def swap(A, i, j):
      tmp = A[i]
      A[i] = A[j]
      A[j] = tmp

  	# Think about the following array when dealing with quick sort
  	# [. . . l . . k . . . p]
  	# l marks the first element that is bigger than pivot, to be swapped with p very soon
  	# k marks the current element
  	# p marks the pivot element
    def partition(A, i, j):
      l, p = i, j
      for k in range(i, j):
      	if A[k] <= A[p]:
          swap(A, k, l)
          l += 1
      swap(A, l, p)
      return l

    def helper(A, s, e):
      # In recursion, when p points to the first or the last element in A[s, e + 1]
      # then either (p - 1) < s or (p + 1) > e will hold
      # so the terminating condition should include 's > e'.
      if s >= e:
      	return ;
      p = partition(A, s, e)
      helper(A, s, p - 1)
      helper(A, p + 1, e)
      return ;

    if len(nums) == 0:
      return nums;

    helper(nums, 0, len(nums) - 1)

    return nums;
```
---
## Variation - Quick Select
---
```python
class QuickSelect:

  def findKthLargest(self, nums, k):

    def partition(A, i, j):
      l, p = i, j
      for k in range(i, j):
        # find Kth largest -> swap if bigger
        # find Kth smallest -> swap if smaller
        if A[k] > A[p]:
          swap(A, k, l)
            l += 1
      swap(A, l, p)
      return l

    def swap(A, i, j):
      tmp = A[i]
      A[i] = A[j]
      A[j] = tmp

    def helper(A, s, e, k):
      # In recursion, when p points to the first or the last element in A[s, e+1]
      # then either (p - 1) < s or (p + 1) > e will hold
      # But in the case of (p - 1) < s, one of the following must hold:
      #   1. k equals to p
      #   2. k falls into the right hand side of p
      # so 'helper(A, s, p - 1, k)' will never be executed.
      # Same thing for the case of (p + 1) > e.
      # So the terminating condition doesn't need to include 's > e'.
      if s == e:
        return A[s]

      p = partition(A, s, e)

      # binary search methodology
      if p == k:
        return A[p]
      if p < k:
        return helper(A, p + 1, e, k)
      else:
        return helper(A, s, p - 1, k)

    assert len(nums) > 0

    return helper(nums, 0, len(nums) - 1, k - 1)
```
---
### Variation - Sort RGB Color
---
```python
class SortColorRGB:
  '''
  Given a sequence of RGB colored balls, 0 - R, 1 - G, 2 - B.
  Output sorted sequence.

  Input: [2, 0, 2, 1, 1, 0]
  Output: [0, 0, 1, 1, 2, 2]

  Input: [1, 2, 0]
  Output: [0, 1, 2]
  '''
  def sort(self, nums):

    def swap(nums, i, j):
      tmp = nums[i]
      nums[i] = nums[j]
      nums[j] = tmp

    assert len(nums) > 0

    l, r = 0, len(nums) - 1
    i, pv = 0, 1
    # This algorithm is inspired by quick sort partition by pivot
    while l <= i <= r:
      # during this while loop, l always points to 0 or the first 1 from the left
      # when l and i point at 2 at the beginning of this while loop
      # the element will be swapped till l and i not pointing at 2 any more
      # after that, the elements swapped to the back will either be 0 or 1
      # that's why i always advances
      if nums[i] < pv:
        swap(nums, i, l)
        l += 1
        i += 1
      # when i points at 2, it will be swapped with the element r is pointing at
      # the elements behind r is definitely 2
      # but the elements swapped to the front can be anything
      # that's why i doesn't advance
      elif nums[i] > pv:
        swap(nums, i, r)
        r -= 1
      # when nums[i] == 1, nothing change, advance i
      else:
        i += 1

    return ;
```
---


# Merge Sort

### Classic Merge Sort
---
```python
class ClassicMergeSort:

  def mergeSort(self, nums):

    def mergeSortHelper(A, s, e):
      if s == e:
        return [A[s]]

      m = (s + e) // 2
      L = mergeSortHelper(A, s, m)
      R = mergeSortHelper(A, m + 1, e)

      S = [-1 for _ in range(len(L) + len(R))]

      i, j = 0, 0
      while i < len(L) and j < len(R):
        if L[i] <= R[j]:
          S[i + j] = L[i]
          i += 1
        else:
          S[i + j] = R[j]
          j += 1

      while i < len(L):
        S[i + j] = L[i]
        i += 1

      while j < len(R):
        S[i + j] = R[j]
        j += 1

      return S

    assert len(nums) > 0

    return mergeSortHelper(nums, 0, len(nums) - 1)
```
---
### Variation - Merge Sort LinkedList
---
```python
class MergeSortLinkedList:

  def mergeSort(self, head):

    def halve(head):
      slow, fast, last = head, head, None
      while fast is not None and fast.next is not None:
        fast = fast.next.next
        last = slow
        slow = slow.next
      last.next = None
      return head, slow

    if head is None or head.next is None:
      return head

    l, r = halve(head)
    l = mergeSort(l)
    r = mergeSort(r)
    s = ListNode(-1)
    sp = s
    while l is not None and r is not None:
      if l.val < r.val:
        s.next = l
        l = l.next
        s = s.next
      else:
        s.next = r
        r = r.next
        s = s.next

    if l is not None:
      s.next = l
    if r is not None:
      s.next = r

    return sp.next
```
---
### Variation - Merge K Sorted LinkedList
---
```python
class MergeKSortedLinkedList:

  '''
  Given a list of sorted linkedlist head
  Output a merged sorted linkedlist head

  Input:
    [
      1->4->5,
      1->3->4,
      2->6
    ]

  Output:
    1->1->2->3->4->4->5->6
  '''
  def mergeKLists(self, lists):

    assert len(lists) > 0

    if len(lists) == 1:
      return lists[0]

    m = len(lists) // 2
    l = self.mergeKLists(lists[:m])
    r = self.mergeKLists(lists[m:])

    s = ListNode(-1)
    sp = s # pointing at mocked head
    while l is not None and r is not None:
      if l.val < r.val:
        s.next = l
        l = l.next
        s = s.next
      else:
        s.next = r
        r = r.next
        s = s.next

    # how it handles remaining elements in LinkedList is diff from in array
    if l is not None:
      s.next = l
    if r is not None:
      s.next = r

    return sp.next
```
---

# Counting Sort

### Counting Sort - Array indexing and manipulation
---
```python
class CountingSort:

  def countingSort(self):

```
---

# Insertion Sort



# References

### LinkedList ListNode
```python
class ListNode:
  def __init__(self, x):
    self.val = x
    self.next = None
```
