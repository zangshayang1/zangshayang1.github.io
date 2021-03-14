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

## Word Distance
```python
class WordDistance:

  '''
  Type 1

  Given a list of words and two words that appeared in this list more than once.
  Return the minimum indexing distance between these two words.
  '''
  def type1_dumb_solution(self, words_list, word1, word2):
    min_dist = 2 ** 31 - 1
    for i in range(len(words_list)):
      for j in range(len(words_list)):
        if words_list[i] == word1 and words_list[j] == word2:
          min_dist = min(min_dist, abs(i - j))

    return min_dist

  def type1_smart_solution(self, words_list, word1, word2):
    min_dist = 2 ** 31 - 1
    i1, i2 = -1, -1
    for i in range(len(words_list)):
      if words_list[i] == word1:
        i1 = i
      if words_list[i] == word2:
        i2 = i

      if i1 != -1 and i2 != -1:
        min_dist = min(min_dist, abs(i1 - i2))

    return min_dist

  '''
  Type 2

  Given two sorted lists of integers representing the positions of two
  frequently appeared words in a document.
  Return the closest distance of these two words in this document.
  '''
  def solution(self, list1, list2):

    if len(list1) == 0 or len(list2) == 0:
      return -1

    d = 2 ** 31 - 1
    i, j = 0, 0
    while i < len(list1) and j < len(list2):
      if list1[i] < list2[j]:
        d = min(d, list2[j] - list1[i])
        i += 1
      else:
        d = min(d, list1[i] - list2[j])
        j += 1

    return d
```
