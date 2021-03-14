---
layout: post
title:  Algorithms - Array
date:   2021-02-21 17:00:00 -0700
categories: study-notes
tag: algorithms
---

* content
{:toc}


Last Update: 2021-02-21

## Find Missing Number
```python
'''
Given an array of integers
Return the smallest positive integer that's missing.
'''
class MissingPositiveInteger:

  '''
  Assumption: All integers are distinct.
  '''
  def sort_solution_under_assumption(self, nums):
    if len(nums) == 0:
      return 1

    nums = Arrays.sort(nums)
    i = 0
    while i < len(nums):
      if nums[i] <= 0:
        i += 1
      else:
        break

    if i == len(nums):
      return 1

    incrementer = 1
    while i < len(nums):
      if nums[i] != incrementer:
        return incrementer

      i += 1
      incrementer += 1

    return nums[-1] + 1

  def sort_solution_no_assumption(self, nums):
    if len(nums) == 0:
      return 1

    nums = Arrays.sort(nums)
    i = 0
    while i < len(nums):
      if nums[i] <= 0:
        i += 1
      else:
        break

    if i == len(nums):
      return 1

    incrementer = 1
    while i < len(nums):
      if nums[i] > incrementer:
        return incrementer

      if nums[i] == incrementer:
        i += 1
        incrementer += 1
        continue

      if nums[i] < incrementer:
        i += 1
        continue

    return nums[-1] + 1

  '''
  Assumption: All integers are distinct.
  '''
  def index_solution_under_assumption(self, nums):
    if len(nums) == 0:
      return 1

    i = 0
    while i < len(nums):
      if nums[i] <= 0 or nums[i] > len(nums):
        nums[i] = -1 # mark not applicable
        i += 1
        continue

      if nums[i] == i + 1:
        i += 1
        continue

      curr = nums[i]
      tmp = nums[curr - 1]
      nums[curr - 1] = curr
      nums[i] = tmp

    # find first missing positive
    for j in range(len(nums)):  
      if nums[j] != j + 1:
        return j + 1

    return len(nums) + 1

  # A HARD solved in 10 lines.
  def index_solution_under_no_assumption(self, nums):

    n = len(nums)

    # put whatever value within [1, 2, ..., n] found in the nums
    # to the RIGHT index (value - 1) position
    for i in range(n):
      while nums[i] > 0 and nums[i] < n + 1 and nums[nums[i] -1] != nums[i]:
        swap(nums, i, nums[i] - 1)

    # then look for the first mismatch
    for i in range(n):
      if nums[i] != i + 1:
        return i + 1

    return n + 1
```
