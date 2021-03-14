---
layout: post
title:  Algorithms - BFS AND DFS
date:   2021-02-21 17:00:00 -0700
categories: study-notes
tag: algorithms
---

* content
{:toc}


Last Update: 2021-03-13

# Undefined

## Number Of Islands
```python
'''
Input: grid = [
  ["1","1","0","0","0"],
  ["1","1","0","0","0"],
  ["0","0","1","0","0"],
  ["0","0","0","1","1"]
]
Output: 3
'''
class NumberOfIslands:
  def solution(self, grid):
    counter = 0
    n_rows = len(grid)
    n_cols = len(grid[0])
    visited = [[False for _ in range(n_cols)] for _ in range(n_rows)]
    for i in range(n_rows):
      for j in range(n_cols):
        if grid[i][j] == "0" or visited[i][j]:
          continue

        counter += 1
        self._bfs(grid, i, j, visited) # or self._bfs(grid, i, j, visited)

    return counter

  def _dfs(self, grid, i, j, visited):
    n_rows = len(grid)
    n_cols = len(grid[0])

    visited[i][j] = True
    if i + 1 < n_rows and grid[i + 1][j] == "1" and not visited[i + 1][j]:
      self._dfs(grid, i + 1, j, visited)
    if i - 1 > -1 and grid[i - 1][j] == "1" and not visited[i - 1][j]:
      self._dfs(grid, i - 1, j, visited)
    if j + 1 < n_cols and grid[i][j + 1] == "1" and not visited[i][j + 1]:
      self._dfs(grid, i, j + 1, visited)
    if j - 1 > -1 and grid[i][j - 1] == "1" and not visited[i][j - 1]:
      self._dfs(grid, i, j - 1, visited)

    return

def _bfs(self, grid, i, j, visited):
    n_rows = len(grid)
    n_cols = len(grid[0])
    q = collections.deque([(i, j)])
    while q:
      i, j = q.popleft()
      visited[i][j] = True
      if i + 1 < n_rows and grid[i + 1][j] == "1" and not visited[i + 1][j]:
        q.append((i + 1, j))
      if i - 1 > -1 and grid[i - 1][j] == "1" and not visited[i - 1][j]:
        q.append((i - 1, j))
      if j + 1 < n_cols and grid[i][j + 1] == "1" and not visited[i][j + 1]:
        q.append((i, j + 1))
      if j - 1 > -1 and grid[i][j - 1] == "1" and not visited[i][j - 1]:
        q.append((i, j - 1))
```

## Inverse Depth Sum
```python
'''
Given a List<NestedInteger> # Ref: NestedInteger in Resources section.

Return a weighted sum. The weight of each number is inverse to the its depth.
'''
class InverseDepthSum:

  def bfs_solution(self, nested_integer_list):
    all_level_integers = []
    curr_level_integers = []
    curr_level = [nested_integer_list]
    next_level = []
    while curr_level or next_level:

      if not curr_level:
        all_level_integers.append([i for i in curr_level_integers])
        curr_level_integers = []
        curr_level = [l for l in next_level]
        next_level = []

      curr_list = curr_level.pop(0)
      for element in curr_list:
        if element.isInteger():
          curr_level_integers.append(element.getInteger())
        else:
          next_level.append(element.getList())

    if curr_level_integers:
      all_level_integers.append(curr_level_integers)

    s = 0
    w = len(all_level_integers)
    for each in all_level_integers:
      s += w * sum(each)
      w -= 1

    return s

  def dfs_solution(self, nested_integer_list):
    return self._dfs_helper(nested_integer_list, self._get_depth(nested_integer_list))

  def _get_depth(self, nested_integer_list):
    if all([nested_integer.isInteger() for nested_integer in nested_integer_list]):
      return 1

    filtered_lists = filter(lambda x: not x.isInteger(), nested_integer_list)

    return max([
      self._get_depth(nested_integer.getList()) for nested_integer in filtered_lists
    ]) + 1

  def _dfs_helper(self, nested_integer_list, weight):
    s = 0
    for nested_integer in nested_integer_list:
      if nested_integer.isInteger():
        s += nested_integer.getInteger() * weight
      else:
        s += self._dfs_helper(nested_integer.getList(), weight - 1)

    return s
```

## Partition Into Two Subsets With Equal Sum
```python
'''
Given a list of integers.
Return True, if it can be partitioned into 2 sub lists with equal sum values.
Return False otherwise.

Input: [1, 5, 11, 5]
Output: True
'''
class PartitionIntoTwoSubsetsWithEqualSum:

  def dfs_solution(self, nums):

    if sum(nums) % 2 != 0: return False

    # A key observation here
    s = sum(nums) / 2

    return self._dfs_helper(nums, 0, s, [])

  def _dfs_helper(self, nums, start, s, subset):

    if s == sum(subset): return True

    for i in range(start, len(nums)):
      subset.append(nums[i])

      if self._dfs_helper(nums, i + 1, s, subset):
        return True

      # backtracking
      subset.pop(-1)

    return False

  def dp_solution(self, nums):

    if sum(nums) % 2 != 0: return False

    s = sum(nums) / 2

    # define dp[i][j] as:
    # 1 if there exist a subset from the first i_th numbers that sum up to j
    # 0 if there does not exist such a subset
    dp = [[0 for _ in range(s + 1)] for _ in range(len(nums) + 1)]

    dp[0][0] = 1
    for i in range(1, len(dp)):
      for j in range(len(dp[0])):
        if dp[i - 1][j] == 1:
          dp[i][j] = 1
          continue
        if j - nums[i - 1] > -1 and dp[i - 1][j - nums[i - 1]] == 1:
          dp[i][j] = 1
          continue

    return dp[-1][-1] == 1
```

## Partition Into Four Subsets With Equal Sum
```python
'''
Given a list of integers.
Return True, if it can be partitioned into 4 sub lists with equal sum values.
Return False otherwise.

Input: [1,1,2,2,2]
Output: true
'''
class PartitionIntoFourSubsetsWithEqualSum:

  '''
  This is a natural and naive extension of
  # Ref: PartitionIntoTwoSubsetsWithEqualSum().dfs_solution
  '''
  def dfs_solution(self, nums):

    if not nums or sum(nums) % 4 != 0: return False

    t = sum(nums) // 4

    return self._dfs_helper(nums, 0, t, [[], [], []])

  def _dfs_helper(self, nums, start, target, subsets):
    if start == len(nums):
      ss1, ss2, ss3 = subsets
      if sum(ss1) == target and sum(ss2) == target and sum(ss3) == target:
        return True

    for i in range(start, len(nums)):
      for j in range(3):
        subsets[j].append(nums[i])
        if self._dfs_helper(nums, i + 1, target, subsets):
          return True
        # backtracking
        subsets[j].pop(-1)

    return False

  '''
  Improved DFS.
  '''
  def improved_dfs_solution(self, nums):

    if not nums or sum(nums) % 4 != 0: return False

    # improvement 1: using reversely sorted array help un-qualify a dfs search
    # as early as possible.
    nums = sorted(nums, reverse = True)

    # improvement 2: more efficient subset sum representation
    t = sum(nums) // 4

    return self._improved_dfs_helper(nums, 0, t, [0, 0, 0, 0])

  def _improved_dfs_helper(self, nums, index, target, sums):

    # improvement 3 - simpler termination condition
    # given the following key observations, it is sufficient to return True.
    # 1. sum(nums) is divisible by 4.
    # 2. target = sum(nums) // 4.
    # 3. successful distribution of all the numbers into 4 buckets without
    #   any of them bigger than target.
    if index == len(nums): return True

    for i in range(4):
      # improvement 4 - conditional advancement (filter)
      if sums[i] + nums[index] > target:
        continue

      sums[i] += nums[index]
      # improvement 5: if no number can be used twice, step to next element in dfs
      # rather than using another iteration - such as
      # # Ref: DistinctElementPermutation and # Ref: FactorCombinations
      if self._improved_dfs_helper(nums, index + 1, target, sums):
        return True
      # backtracking
      sums[i] -= nums[index]

    return False

  '''
  The origin problem is: LC 473: Matchsticks To Square

  With the following limits in place:
  1. The length sum of the given matchsticks is in the range of 0 to 10^9.
  2. The length of the given matchstick array will not exceed 15.

  The below solution exploited these limits to its essence.
  '''
  def bitmask_solution(self, nums):

    if not nums or sum(nums) % 4 != 0: return False

    # n, short for number of elements in nums
    n = len(nums)
    # t, short for target
    t = sum(nums) // 4

    # Quarter is a list of numbers
    #   where each number is a bitmask representing a set of numbers picked from
    #   input list which sums up to target value
    quarter = []

    # Half is a list of numbers where
    #   each number is a bit mask representing a set of numbers picked from
    #   input list which can be split into 2 subsets of numbers
    #   with each subset sums up to target
    half = []

    for i in range(1 << n):
      s = self._fromBitMaskToSum(i, nums)

      if s == t:
        for q in quarter:
          # current quarter set i has no overlap with previous quarter set q
          if i & q == 0:
            for h in half:
              # current half set (i|q) has no overlap with previous half set h
              if h & (i | q) == 0:
                return True

              half.append(i | q)

        quarter.append(i)

    return False

  def improved_bitmask_solution(self, nums):

    # n, short for number of elements in nums
    n = len(nums)
    # t, short for target
    t = sum(nums) // 4
    # a, short for "all", is a bitmask representing that all the numbers are used
    a = (1 << n) - 1 # +-*/ takes precedence over bitwise operation otherwise

    # Quarter is a list of numbers
    #   where each number is a bitmask representing a set of numbers picked from
    #   input list which sums up to target value
    quarter = []

    # Improvement:
    # Half is a boolean list where
    #   half[i] -> True means i, taken as a bitmask, represents a set of numbers
    #     picked from input list which can be split into 2 subsets of numbers
    #     with each subset sums up to target
    #   half[i] -> False means otherwise.
    #
    # Array indexing is more efficient than looking for a non-overlapping
    #   match by scanning the entire array.
    #
    # Exercise: think about why we cannot do the same thing to quarter[]
    half = [False for _ in range(1 << n)]

    for i in range(1 << n):
      s = self._fromBitMaskToSum(i, nums)

      if s == t:
        for q in quarter:
          # current quarter set i has no overlap with previous quarter set q
          if i & q == 0:
            # set the current half set (i|q) to True
            half[i | q] = True
            # look for the complementary half set
            if half[a ^ (i | q)]:
              return True

        quarter.append(i)

    return False


  def _fromBitMaskToSum(self, mask, nums):
      s = 0
      for i in range(len(nums)):
          if mask & (1 << i) > 0:
              s += nums[i]
      return s
```  

# Permutations and Combinations

## Distinct Element Permutation
```python
class DistinctElementPermutation:
  def solution(self, nums):
    global_result = []
    visited = {k: False for k in nums}
    self._permute(nums, global_result, [], visited)
    return global_result

  def _permute(self, nums, global_result, local_result, visited):
    if len(local_result) == len(nums):
      global_result.append([l for l in local_result])
      return

    for i in range(len(nums)):
      if visited[nums[i]]:
        continue

      local_result.append(nums[i])
      visited[nums[i]] = True
      self._permute(nums, global_result, local_result, visited)
      visited[nums[i]] = False
      local_result.pop(-1)

    return
```

## Factor Combinations
```python
'''
In # Ref: DistinctElementPermutation, every recursive subroutine started from the beginning of the candidate list.
In # Ref: FactorCombinations, every recursive subroutine started from the current element of the candidate list.
In # Ref: PartitionEqualSubsetSum, every recursive subroutine started from the next element of the candidate list.

Given a positive integer n, return all possible combinations of its factors.
'''
class FactorCombinations:

  def solution(self, n):
    global_result = []
    self._dfs(n, 2, global_result, [])
    return global_result

  def _dfs(self, n, start, global_result, local_result):
    if n < 2 and len(local_result) > 1:
      global_result.append([l for l in local_result])
      return ;

    for i in range(start, n + 1):
      if n % i == 0:
        local_result.append(i)
        self._dfs(n // i, i, global_result, local_result)
        local_result.pop(-1)

    return ;
```

# Game Board

## Knight Tour
```python
'''
Given a N x N chess board and a starting position (0, 0).
Return True if a knight can tour around the entire board (step on every cell
without repetition), False otherwise.
'''
class KnightTour:

  def dfs_solution(self, N):
    delta_X = [1, 1, -1, -1, 2, 2, -2, -2]
    delta_Y = [2, -2, 2, -2, 1, -1, 1, -1]
    return self._dfs_helper(N, 0, 0, 1, delta_X, delta_Y)

  def _dfs_helper(self, N, x, y, steps, delta_X, delta_Y):
    if steps == N * N:
      return True

    for i in range(8):
      if not self._on_board(N, x + delta_X[i], y + delta_Y[i]):
        continue

      if self._dfs_helper(N, x + delta_X[i], y + delta_Y[i], steps + 1, delta_X, delta_Y):
        return True

    return False

  def _on_board(self, N, x, y):
    return x >= 0 and x < N and y >= 0 and y < N
```

## N Queens Problem
```python
'''
Given N, return the number of distinct ways to place N queens on a N x N chess board.
'''
class NQueens:

  def dfs_solution(self, N):
    # every '.' stand for a position in a N x N board
    board = [['.' for _ in range(N)] for _ in range(N)]
    # use set for dedup
    placements = set()
    # starting row index: 0
    self._dfs_helper(0, N, board, placements)

    return len(placements)

  # For NQueens problem
  # Each row must have one and only one queen.
  # We only need to search for the right column index to place a queen at each row.
  # Next queen must be placed at the next row.
  # A solution can be found by going from top row to bottom row.
  def _dfs_helper(self, row, N, board, placements):
    if row == N:
      placements.add(self._serialize(board))
      return ;

    for j in range(N):
      if not self._safe(N, board, row, j):
        continue

      board[row][j] = 'Q'
      self._dfs_helper(row + 1, N, board, placements)

      # backtracking
      board[row][j] = '.'

    return ;

  def _safe(self, N, board, x, y):
      for i in range(N):
        if board[i][y] == 'Q':
          return False

      for j in range(N):
        if board[x][j] == 'Q':
          return False

      # diagonal
      for d in range(- N + 1, N):
        if self._on_board(x + d, y + d, len(board)) and board[x + d][y + d] == 'Q':
          return False
        if self._on_board(x + d, y - d, len(board)) and board[x + d][y - d] == 'Q':
          return False

      return True

  def _on_board(self, x, y, L):
    return x >= 0 and x < L and y >= 0 and y < L

  def _serialize(self, board):
    return ','.join([''.join(row) for row in board])
```

# Resources
```java
Interface NestedInteger:

  """
  @return True if this NestedInteger holds a single integer, rather than a nested list.
  :rtype bool
  """
  public isInteger();

  """
  @return the single integer that this NestedInteger holds, if it holds a single integer
  Return None if this NestedInteger holds a nested list
  :rtype int
  """
  public getInteger();

  """
  @return the nested list that this NestedInteger holds, if it holds a nested list
  Return None if this NestedInteger holds a single integer
  :rtype List[NestedInteger]
  """
  public getList();
```
