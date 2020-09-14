---
layout: post
title:  Leetcode Problems
date:   2020-09-13 15:00:00 +0800
categories: study-notes
tag: algorithms
---

* content
{:toc}


# Algorithms and Data Structure

## Sort

### Quick Sort

#### Classic
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
#### Variation - Quick Select
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

# Other Resource Definitions
