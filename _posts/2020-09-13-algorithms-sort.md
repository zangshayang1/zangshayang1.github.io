---
layout: post
title:  Algorithms - Sort
date:   2020-09-13 15:00:00 -0700
categories: study-notes
tag: algorithms
---

* content
{:toc}



# Quick Sort

## Classic Quick Sort

```python
class ClassicQuickSort:

  def quickSort(self, nums):

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

## Variant - Quick Select

```python
class QuickSelect:

  def findKthLargest(self, nums, K):

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

    def helper(A, s, e, K):
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
      if p == K:
        return A[p]
      if p < K:
        return helper(A, p + 1, e, K)
      else:
        return helper(A, s, p - 1, K)

    assert len(nums) > 0

    return helper(nums, 0, len(nums) - 1, K - 1)
```

## Variant - Sort RGB Color

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



# Merge Sort

## Classic Merge Sort

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

## Variant - Merge Sort LinkedList

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

## Variant - Merge K Sorted LinkedList

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


# Insertion Sort

## Classic Insertion Sort

```python
class ClassicInsertionSort:

  def insertionSort(self, nums):

    assert len(nums) > 1
    # invariant: the elements before i are always sorted
    for i in range(1, len(nums)):
      j = i
      while j > 0 and nums[j - 1] > nums[j]:
        swap(nums, j, j - 1)
        j -= 1

    return nums;
```

## Variant - Insertion Sort LinkedList

```python
class InsertionSortLinkedList:

  def insertSort(self, head):

    p1 = head
    dummy = ListNode(-1)
    while p1 is not None:
      p2 = dummy.next
      prev_p2 = dummy
      while p2 is not None and p1.val > p2.val:
        # find the first occurrence of p2.val >= p1.val
        # as p2 is walking up (ascending)
        prev_p2 = p2
        p2 = p2.next

      p1_next = p1.next
      prev_p2.next = p1
      p1.next = p2
      p1 = p1_next

    return dummy.next
```

# Wiggle Sort

## Classic Wiggle Sort Without Same Elements
```python
class WiggleSort_1:
  '''
  Input: [6, 1, 3, 7, 8, 5, 4, 2]
  Output: [1, 6, 3, 8, 7, 4, 5, 2]
  Requirement: output[0] < output[1] > output[2] < output[3] ...
  Assuming all inputs are valid (can be wiggle sorted according to the requirement)

  Easy shot: sort and switch
  [6, 1, 3, 7, 8, 5, 4, 2] -> [1, 2, 3, 4, 5, 6, 7, 8] -> [1, 3, 2, 5, 4, 7, 6, 8]

  But this algorithm is obviously not ideal because it spends effort in lowering
  the randomness and then manually introduce some randomness to meet the requirement.

  The following algorithm is as simple as it can get and very efficient.
  '''
  def wiggleSort(self, nums):

    assert len(nums) > 1

    for i in range(1, len(nums)):
      if i % 2 == 1 and nums[i] < nums[i - 1]:
        swap(nums, i, i - 1)
      if i % 2 == 0 and nums[i] > nums[i - 1]:
        swap(nums, i, i - 1)

    return nums
```

## Variant Wiggle Sort With Same Elements
```python
'''
Hint 1
If A < B < M < X < Y, how to sort a random sequence of [X, A, M, M, B, Y, M]
into a partially sorted sequence [larger than M, M..., smaller than M] in O(n)?

QuickSelect and ColorSort are both O(n) algorithms.

Hint 2
Wiggle sort [L, L, M, M, M, S, S] can produce [S, L, S, L, M, M, M] (incorrect)
or it can produce [M, L, M, L, S, M, S] (correct)
The 1st approach interleaves S and L first and then fill up the remaining with M,
resulting in the highest probability of M adjacent to another M.
The 2nd approach put L on odd index from left, put S on even index from right,
and finally fill up the remaining with M to ensure M is separate from another M.
Given that all inputs are valid (can be wiggle sorted according to the requirement),
this arrangement always returns the correct result.

[L1, L2, M1, M2, M3, S1, S2 ] => [M2, L1, M3, L2, S1, M1, S2]

   i      | 0 1 2 3 4 5  6
 2*i+1    | 1 3 5 7 9 11 13
(2*i+1)%3 | 1 0 2 ...
(2*i+1)%5 | 1 3 0 2 4 ...
(2*i+1)%7 | 1 3 5 0 2 4  6 ...
'''
class WiggleSort_2:

  def wiggleSort(self, nums):

    # Ref: Quick Select
    def findMedian(nums, s, e, M):
      # terminating condition
      if s == e:
        return nums[s]
      # partition
      l, p = s, e
      for k in range(s, e + 1):
        # l marks the 1st number smaller than pivot
        # put bigger-than-pivot numbers on the left
        if nums[k] > nums[p]:
          swap(nums, l, k)
          l += 1
      swap(nums, l, p)
      # recursion
      if l == M:
        return nums[l]
      if l > M:
        return findMedian(nums, s, l - 1, M)
      else:
        return findMedian(nums, l + 1, e, M)

    def index_map(idx, length):
      return (2 * idx + 1) % (length | 1)

    # Ref: Sort RGB Color
    def colorSort(nums, median):
      i, l, r = 0, 0, len(nums) - 1
      while l <= i <= r:
        if nums[i] > median:
          swap(nums, i, l)
          i += 1
          l += 1
        elif nums[i] < median:
          swap(nums, i, r)
          r -= 1
        else:
          i += 1

      return nums

    assert len(nums) > 1

    N = len(nums)
    midx = N // 2

    median = findMedian(nums, 0, N - 1, midx)
    nums = colorSort(nums, median)

    copy = [x for x in nums]
    for i in range(N):
      nums[index_map(i, N)] = copy[i]

    return nums
```


# Resources

```python
class ListNode:
  def __init__(self, x):
    self.val = x
    self.next = None
```

# Helper Function

```python
def swap(nums, i, j):
  tmp = nums[i]
  nums[i] = nums[j]
  nums[j] = tmp
```
