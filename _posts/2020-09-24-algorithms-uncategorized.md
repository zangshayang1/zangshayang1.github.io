---
layout: post
title:  Algorithms - Uncategorized
date:   2020-09-24 22:00:00 -0700
categories: study-notes
tag: algorithms
---

* content
{:toc}


# Find Kth In Matrix

## Kth Smallest Element in Row-wise and Column-wise Sorted Matrix
```python
'''
Given a partially sorted 2D matrix.
Each row, Matrix[i][j] < Matrix[i][j+1]
Each column, Matrix[i][j] < Matrix[i+1][j]

Find kth smallest element.
'''
class KthSmallestInSortedMatrix:

  def findKthSmallest(M, K):

    assert len(M) > 0 and len(M[0]) > 0
    assert K > 0

    x, y = 0, 0
    visited = [[False for _ in range(len(M[0]))] for _ in range(len(M))]

    # Ref: Algorithms: PriorityQueue MinHeap implementation
    # Below syntax is technically incorrect, just for clear illustration purpose
    pq = MinHeap([], key = lambda tuple : tuple[2])

    pq.push((x, y, M[x][y]))
    visited[x][y] = True
    # Use PriorityQueue to maintain a list of candidate elements - a frontier of the smallest
    # by traversing to the right or the below, where the next smallest elements are.
    # Keep pop the current smallest element till K = 1, then the next element is what we want.
    # Of course you don't want to double count any element.
    while K > 1:
      element = pq.pop()
      x, y, v = element

      if x + 1 < len(M) and not visited[x + 1][y]:
        pq.push((x + 1, y, M[x + 1][y]))
        visited[x + 1][y] = True

      if y + 1 < len(M[0]) and not visited[x][y + 1]:
        pq.push((x, y + 1, M[x][y + 1]))
        visited[x][y + 1] = True

      k -= 1

    return pq.pop()[2]
```

# Merge intervals

## Merge Sorted Intervals 1
```python
'''
Given [Interval(1, 2), Interval(2, 3), Interval(4, 5)]
Return [Interval(1, 3), Interval(4, 5)]

A list of sorted intervals is sorted by Interval object "start" field.
'''
class MergeSortedIntervals1:

  def merge(intervals):

    assert len(intervals) > 0

    rst = []
    for i in range(1, len(intervals)):
      if intervals[i - 1].end < intervals[i].start:
        rst.append(intervals[i - 1])
      else:
        intervals[i].start = min(intervals[i - 1].start, intervals[i].start)
        intervals[i].end = max(intervals[i - 1].end, intervals[i].end)

    rst.append(intervals[-1])

    return rst
```

## Insert New Interval
```python
'''
Given [Interval(1,2), Interval(5,9)] and new Interval(2, 5)
Return [Interval(1, 9)]
'''
class InsertNewInterval:

  def insert(intervals, newInterval):
    idx = 0
    rst = []
    for interval in intervals:
      if interval.end < newInterval.start:
        rst.append(interval)
        idx += 1
      elif interval.start > newInterval.end:
        rst.append(interval)
      else:
        newInterval.start = min(interval.start, newInterval.start)
        newInterval.end = max(interval.end, newInterval.end)

    rst.insert(idx, newInterval)

    return rst
```

## Merge Sorted Intervals 2
```python
'''
Given [Interval(1,2), Interval(3,4)] and [Interval(2,3), Interval(5,6)]
Return [Interval(1, 4), Interval(5, 6)]
'''
class MergeSortedIntervals2:

  # O((m+n)log(m+n)) solution would be:
  # merge the two list, sort them and apply # Ref: MergeSortedIntervals1

  # O(m + n) solution would be the following:
  # a more general version of # Ref: InsertNewInterval
  def merge(intervals1, intervals2):

    rst = []
    i, j = 0, 0
    while i < len(intervals1) and j < len(intervals2):
      if intervals1[i].end < intervals2[j].start:
        rst.append(intervals1[i])
        i += 1
      elif interval1[i].start > intervals2[j].end:
        rst.append(intervals2[j])
        j += 1
      else:
        # when intervals1[i] is tailing longer than intervals2[j]
        # 1. intervals1[i + 1].start > intervals1[i].end is guaranteed as input is valid
        # 2. intervals1[i].start is expanded accordingly while intervals1[i].end stays the same
        # 3. j advances, leaving the current intervals2[j] out of the picture
        if intervals1[i].end >= intervals2[j].end:
          intervals1[i].start = min(intervals1[i].start, intervals2[j].start)
          j += 1
        else:
          intervals2[j].start = min(intervals1[i].start, intervals2[j].start)
          i += 1

    if i < len(intervals1):
      rst += intervals1[i:]
    if j < len(intervals2):
      rst += intervals2[j:]

    return rst
```

# Meeting Room

## Can Attend Meeting
```python
'''
Given a list of interval object representing the start and the end of meetings
Find out if you can attend all of them
'''
class CanAttendMeeting:

  def canAttendMeeting(intervals):
    intervals = sorted(intervals, key = lambda x : x.start)
    flag = True
    for i in range(1, len(intervals)):
        prev, curr = intervals[i - 1], intervals[i]
        if prev.end > curr.start:
            flag = False

    return flag
```

## Min Meeting Rooms
```python
class MinMeetingRooms:

  def minMeetingRoom(intervals):

    START, END = 0, 1
    timestamps = []
    for interval in intervals:
      timestamps.append((interval.start, START))
      timestamps.append((interval.end, END))

    # Note the implementation of 'compare' function and how it's used in sorting tuples
    def compare(a, b):
      if a[0] != b[0]:
          return a[0] - b[0]
      return a[1] - b[1]

    timestamps = sorted(timestamps, cmp = compare)

    # 'START' and 'END'
    # 1. serve as a label for incremental/decremental change
    # 2. ensure sort stability
    counter, rst = 0, 0
    for ts in timestamps:
      if ts[1] == START:
          counter += 1
      else:
          counter -= 1
      rst = max(rst, counter)

    return rst
```

# Best Meeting Point

## Best Meeting Point 1D
```python
'''
Given 1D array with 0s representing space and 1s representing people.
Find the best meeting point where the sum of steps taken by all the people
traveling to the point is minimized.
'''
class BestMeetingPoint1D:

  def bestMeetingPoint(A):
    assert len(A) > 0

    indices = []
    for i in range(len(A)):
      if A[i] == 1:
        indices.append(i)

    # if length is odd, median in a sorted array is the best meeting point
    # if length is even, either one in the "middle" will do, why?
    # [A, 0, B, 0, 0, 0, C, 0, 0, 0, 0, 0, D]
    # SUM_B = dist(A, B) + dist(B, C) + dist(B, D)
    # SUM_C = dist(A, C) + dist(B, C) + dist(C, D)
    # Obviously, dist(A, B) + dist(B, D) = dist(A, C) + dist(C, D), So SUM_B = SUM_C
    # Actually any point between [B, C] is the best meeting point
    return indices[len(indices) // 2]
```

## Best Meeting Point 2D
```python
'''
Given a 2D matrix with 0s and 1s.
Find the best meeting point to minimize people's traveling distances.
Distance is calculated as Manhattan Distance, meaning no diagonal step can be taken.
'''
class BestMeetingPoint2D:

  def bestMeetingPoint(M):

    assert len(M) > 0 and len(M[0]) > 0

    # project to X-axis and Y-axis respectively
    x_indices, y_indices = [], []

    for i in range(len(M)):
      for j in range(len(M[0])):
        if M[i][j] == 1:
          x_indices.append(i)

    for j in range(len(M[0])):
      for i in range(len(M)):
        if M[i][j] == 1:
          y_indices.append(i)

    return (x_indices[len(x_indices) // 2], y_indices[len(y_indices) // 2])
```

## Best Meeting Point Unsorted Variant
```python
'''
Given a list of unsorted indices indicating people's position.
Find the best meeting point in O(n).

In previous problems, peoples' indices are collected in a way that they are
naturally sorted, so finding median is as easy as O(1).
'''
class BestMeetingPointUnsorted:

  def bestMeetingPoint(nums):

    # Now the problem becomes how to find median in O(n).
    # Ref: Algorithms: Sort - QuickSelect
    def findMedian(nums, s, e, K):
      if s == e:
        return nums[s]

      l, p = s, e
      for i in range(s, e):
        if nums[i] < nums[p]:
          swap(nums, i, l)
          l += 1
      swap(l, p)

      if l == K:
        return nums[l]
      elif l < K:
        return findMedian(nums, l + 1, e, K)
      else:
        return findMedian(nums, s, l - 1, K)

    assert len(nums) > 0

    return findMedian(nums, 0, len(nums) - 1, len(nums) // 2)
```

# Resources

```python
class Interval:
  def __init__(self, start, end):
    self.start = start
    self.end = end
```

# Helper Function

```python
def swap(nums, i, j):
  tmp = nums[i]
  nums[i] = nums[j]
  nums[j] = tmp
```
