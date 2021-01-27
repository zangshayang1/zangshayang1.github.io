---
layout: post
title:  Algorithms - Uncategorized
date:   2020-09-24 22:00:00 -0700
categories: study-notes
tag: algorithms
---

* content
{:toc}



Last Update: 2021-01-21

# Strings

## Longest Substring Without Duplicate
```python
'''
Classic two pointers.
'''
class LongestSubstringWithoutDup:
  def solution(self, s):
    ans = 0
    dups = {}
    i, j = 0, 0
    while j < len(s):
      # need to jump?
      if s[j] in dups:
        # if i exceeds destination already
        if i >= dups[s[j]] + 1:
          pass
        # jumping only makes sense otherwise
        else:
          i = dups[s[j]] + 1

      # update global answer either jump or not
      ans = max(ans, j - i + 1)
      # update the last seen idx on either new or existing char
      dups[s[j]] = j
      # advance j till the end -> O(n)
      j += 1
```

## All Interleave Strings
```python
'''
Permutations maybe?
Given two strings as below:
  ab
  cd
Return all the possible strings interleaving these two:
  abcd
  acbd
  acdb
  cdab
  cadb
  cabd
'''
class AllInterleaveStrings:
  def solution(self, s1, s2):
    rst = []
    pref = ""
    self._helper(s1, s2, pref, rst)
    return rst

  def _helper(self, s1, s2, pref, rst):
    if s1 == "" or s2 == "":
      rst.append(pref + s1 + s2)
      return ;

    self._helper(s1[1:], s2, pref + s1[0], rst)
    self._helper(s1, s2[1:], pref + s2[0], rst)
    return ;
```
# Numbers

## Multiply String Numbers
```python
'''
Given two integer numbers in string representation.
Return their multiplied result in string representation.
'''
class MultiplyStringNumbers:

  def solution(self, num1, num2):
    if num1 == '0' or num2 == '0':
      return '0'

    # Imagine manually working on a multiplication
    # From right to left, use one digit from short_num to multiple-through
    # all the digits from long_num. Store the results and apply additions later.
    if len(num1) > len(num2):
      long_num, short_num = num1, num2
    else:
      long_num, short_num = num2, num1

    global_result = '0'
    for i in range(len(short_num) - 1, -1, -1):
      sd = self._digitChar2digitInt(short_num[i])
      carry = 0
      local_result = ''
      for j in range(len(long_num) - 1, -1, -1):
        ld = self._digitChar2digitInt(long_num[j])
        remainder = (sd * ld + carry) % 10
        carry = (sd * ld + carry) // 10
        local_result += str(remainder)

      # don't forget the last 'carry'
      if carry > 0:
        local_result += str(carry)

      # turn the string number around
      local_result = local_result[::-1]

      # pad zeros as i move from right to left on the short_num
      for _ in range(len(short_num) - 1 - i):
        local_result += '0'

      # string number addition itself is another problem.
      global_result = self._addStringNumbers(global_result, local_result[::-1] * 10 * i)

    return global_result

  def _addStringNumbers(self, num1, num2):
    i, j = len(num1) - 1, len(num2) - 1
    s = ''
    carry = 0
    while i > -1 and j > -1:
      d1, d2 = self._digitChar2digitInt(num1[i]), self._digitChar2digitInt(num2[j])
      remainder = (d1 + d2 + carry) % 10
      carry = (d1 + d2 + carry) // 10
      s += str(remainder)
      i -= 1
      j -= 1

    while i > -1:
      d1 = self._digitChar2digitInt(num1[i])
      remainder = (d1 + carry) % 10
      carry = (d1 + carry) // 10
      s += str(remainder)
      i -= 1

    while j > -1:
      d2 = self._digitChar2digitInt(num2[j])
      remainder = (d2 + carry) % 10
      carry = (d2 + carry ) // 10
      s += str(remainder)
      j -= 1

    if carry > 0:
      s += str(carry)

    # print("{} + {} = {}".format(num1, num2, s[::-1]))
    return s[::-1]

  def _digitChar2digitInt(self, char):
    return ord(char) - ord('0')
```

## Count Primes
```python
'''
Given integer n, return the number of primes that are less than n.
'''
class CountPrimes:
  def sieves(n):
    if n < 2: return 0

    primes = [True for _ in range(n)]
    primes[0], primes[1] = False, False
    counter = 0
    for i in range(2, n):
      if primes[i]:
        counter += 1
        j = i
        while j * i < n:
          primes[j * i] = False
          j += 1

    return counter
```

# Chars/Strings/Bits/Bytes

## Is A The Permutation Of B
```python
'''
Given two strings A and B.
Check if one is the permutation of the other.
'''
class IsPermutation:
  def solution(self, A, B):
    if len(A) != len(B):
      return False

    # Assume the given chars are in ext. Ascii table.
    # One can bucket chars into an array, which is faster than using HashMap.
    array = [0 for _ in range(256)]
    for a in A:
      array[ord(a)] += 1
    for b in B:
      array[ord(b)] += 1

    for i in range(256):
      if array[i] != 0:
        return False

    return True
```

## Is A The Rotation Of B
```python
'''
Given two strings A and B.
Check if one is the rotation of the other.

For example: A = waterbottle, B = erbottlewat, return true;
'''
class IsRotation:
  def solution(self, A, B):
    # Most straightforward solution is to move the first char in A to the last,
    # one by one, and see if any of the transformation results in B.

    # Now I present an inspiring solution:
    # When a flat list repeats itself, it makes a cycle.
    return _isSubstr(A, B + B)

  '''
  A good test case is:
    string = "mississippi"
    sub = "issip"
  '''
  def _isSubstr(self, sub, string):
    if sub == "":
      return True

    if len(sub) > len(string):
      return False

    # classic two pointers
    i, j = 0, 0
    while i < len(string) and j < len(sub):
      if string[i] != sub[j]:
        i += 1
        continue
      # find a match of the first char of sub in string
      while j < len(sub) and i + j < len(string) and sub[j] == string[i + j]:
        j += 1
      if j == len(sub):
        return True
      else:
        # restart to match the next char in string with the first char in sub.
        i += 1
        j = 0

    return False
```

# Find Kth In Matrix

## Square Matrix In Spiral Order
```python
'''
Given a positive integer n.
Return a 2D matrix filled with 1, 2, ..., n^2 in spiral order.
'''
class SquareMatrixInSpiralOrder:

  def classic(n):
    dirs = [(0, 1), (1, 0), (0, -1), (-1, 0)]
    M = [[None for _ in range(n)] for _ in range(n)]
    visited = [[False for _ in range(n)] for _ in range(n)]

    i = 0
    x, y = 0, -1
    # when i is conditionally incremented
    # think twice on the termination condition.
    # easy to go into infinite loop
    while i < n ** 2:
      for d in dirs:
        dx, dy = d
        for _ in range(n):
          if -1 < x + dx < n and -1 < y + dy < n and not visited[x + dx][y + dy]:
            x = x + dx
            y = y + dy
            i += 1
            M[x][y] = i
            visited[x][y] = True
          else:
            break

    return M

  def improved(n):
    # Note the order of these direction tuples is diff from # Ref: SquareMatrixInSpiralOrder:classic
    dirs = [(1, 0), (0, -1), (-1, 0), (0, 1)]
    M = [[None for _ in range(n)] for _ in range(n)]
    # Optimized in space complexicity

    # fill the first row separately and start from downward
    # so that the below # observation holds
    for i in range(n):
      M[0][i] = i + 1

    i = n
    x, y = 0, n - 1
    counter = -1 # count the number of turns already made
    while i < n ** 2:
      for d in dirs:
        dx, dy = d
        counter += 1
        for _ in range(1, n - counter // 2): # observation: shrink by 1 after every 2 turns
          if -1 < x < n and -1 < y < n:
            x = x + dx
            y = y + dy
            i += 1
            M[x][y] = i
          else:
            break

    return M
```

## Kth Smallest In Partially Sorted Matrix
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

## Matrix Rotation
```python
'''
Given a square matrix, rotate it 90 degrees.

A brain teaser.
'''
class MatrixRotation:
  def solution(self, M):
    l = len(M)
    m = l // 2

    while m > 0:
      i = l // 2 - m

      for j in range(i, l - 1 - i):
        top_left_i, top_left_j = i, j
        bottom_left_i, bottom_left_j = l-1-j, i
        bottom_right_i, bottom_right_j = l-1-i, l-1-j
        top_right_i, top_right_j = j, l-1-i

        tmp = M[top_left_i][top_left_j]
        M[top_left_i][top_left_j] = M[bottom_left_i][bottom_left_j]
        M[bottom_left_i][bottom_left_j] = M[bottom_right_i][bottom_right_j]
        M[bottom_right_i][bottom_right_j] = M[top_right_i][top_right_j]
        M[top_right_i][top_right_j] = tmp

      m -= 1

    return M
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

# Best Time To Trade Stock

## Best Time To Trade Stock
```python
class BestTimeToTradeStock():

    '''
    Buy and sell only 1 time.

    Goal is to maximize profit.
    '''
    def oneBuySell(self, prices):
        # sanity check
        if len(prices) < 2: return 0
        # init
        local_max_profit, global_max_profit = 0, 0
        buy_low_price = prices[0]

        for i in range(1, len(prices)):
          if prices[i] < buy_low_price:
            # update buying price as it goes down
            buy_low_price = prices[i]
          else:
            # make local profit as it goes up
            local_max_profit = prices[i] - buy_low_price

          # update global profit
          global_max_profit = max(global_max_profit, local_max_profit)

        return global_max_profit

    '''
    Buy and sell as many times as you want.

    Goal is to maximize profit.
    '''
    def multipleBuySell(self, prices):
      # sanity check
      if len(prices) < 2: return 0
      # the problem became simpler when one can buy as many times as possible
      max_profit = 0
      i = 1
      while i < len(prices):
        # as long as prices keep going up, always buy in at previous and sell at today
        if prices[i] > prices[i - 1]:
          max_profit += prices[i] - prices[i - 1]
          i += 1
        # else, skip today and start looking at from the next day
        else:
          i += 1

      return max_profit

    '''
    Buy and sell as many times as you want with a transaction fee.

    Goal is to maximize profit.
    '''
    def multipleBuySellWithFee(self, prices, fee):
      # sanity check
      if len(prices) < 2: return 0
      # Walk through the following logic with input [1, 4, 2, 10]
      max_profit, min_cost = 0, prices[0]
      i = 1
      while i < len(prices):
        if prices[i] < min_cost:
          # update min_cost
          min_cost = prices[i]
        elif prices[i] > min_cost + fee:
          # accumulate profit when applicable
          max_profit += prices[i] - (min_cost + fee) # max_profit = 4 - (1 + 2)
          # adjust min_cost to be last sale price with a credit that will
          # only be realized when a future transaction is made
          min_cost = prices[i] - fee # min_cost = 4 - 2
        else:
          continue

          # if the next price is 2, 3, 4
          #   max_profit will not increase
          #   min_cost will not change
          #   just pass to 10 and then
          #     max_profit = [4 - (1 + 2)] + [10 - ((4 - 2) + 2)] = 7
          #     same as one transaction: (prices[3] - prices[0] - fee)
          # if the next price is 5... (6, 7, 8, 9)
          #   max_profit = [4 - (1 + 2)] + [5 - (2 + 2)] = 2
          #   min_cost = 5 - 2 = 3
          #   when it comes to 10
          #     max_profit = 2 + [10 - ((5 - 2) + 2)] = 7
          #     same as above
          # if the next price is 1 however
          #   min_cost will get updated to 1 (as 1 < 2)
          #   and when it comes to 10
          #     max_profit = [4 - (1 + 2)] + [10 - (1 + 2)] = 8
          #     finally the two transactions makes more than (prices[3] - prices[0] - fee)

    '''
    Two chances to buy and sell. One at a time.

    Goal is to maximize profit.
    '''
    def twoBuySell(self, prices):
      # sanity check
      if len(prices) < 2: return 0

      # tracks max profit from left to right
      left = [0 for _ in range(prices)]
      # tracks max profit from right to left
      right = [0 for _ in range(prices)]

      valley, peak = prices[0], prices[-1]

      # the following dp is essentially the same thing
      # as # Ref: BestTimeToTradeStock.oneBuySell()
      for i in range(1, len(prices)):
        valley = min(valley, prices[i])
        left[i] = max(left[i - 1], prices[i] - valley) # buy at valley, sell at i

      for i in range(len(prices) - 2, -1, -1):
        peak = max(peak, prices[i])
        right[i] = max(right[i + 1], peak - prices[i]) # buy at i, sell at peak

      # Why Coming From Two Ends?
      # So that at any point i, left[i] covers max_profit before i
      # and right[i] covers max_profit beyond i

      max_profit = 0
      for i in range(len(prices)):
        max_profit = max(max_profit, left[i] + right[i])

      return max_profit

      '''
      K buy and sells are allowed.

      Goal is to maximize the profit.
      '''
      def kBuySell(self, prices, k):
        # the way to solve the problem is to provide a profit chart
        # where chart[i][j] is the profit when making j transactions by (i+1) day
        # and chart[len(prices)][k] is the profit when making k transactions throughout the stock trading days
        # now the problem is how to compute chart[i][j]
        # the max profit made by conducting j transactions over prices[0:i+1] = one the bigger value of the following two:
        # 1. the max profit made by conducting j transactions over prices[0:i], chart[i-1][j]
        # 2. the max profit made by conducting j-th transaction on day i, local_chart[i][j], which is one of the bigger values of the following two:
        # 2.1 the max profit made by conducting j-th transaction on day i-1 plus the price diff between day i and day i-1, local_chart[i-1][j]+ prices[i]-prices[i-1] (move sell day from i-1 to i)
        # 2.2 the max profit made by combining the current global max profit chart[i-1][j-1] plus the profit made by (buying at day i-1 selling at day i if prices[i]-prices[i-1]>0) or (buying at the same day i if prices[i]-prices[i-1]<=0)        
        if len(prices) < 2: return 0

        # when you can buy and sell as many times as you want
        if k >= len(prices): return self.multipleBuySell(prices)

        m = len(prices)
        # localmax[i][j] denotes the profit of da   y i when making j transactions with the last transaction being made on day i
        localmax = [[0 for _ in range(k + 1)] for _ in range(m)]
        # globalmax[i][j] denotes the profit of day i when making j trasactions without the last transaction necessarily being made on day i
        globalmax = [[0 for _ in range(k + 1)] for _ in range(m)]

        for i in range(1, m):
          diff = prices[i] - prices[i - 1]
          for j in range(1, k + 1):
            # current localmax = max(the globalmax ending yesterday with j-1 transactions + today's profit if any
            #                        the localmax ending yesterday with j transactions + today's gain or loss)
            localmax[i][j] = max(globalmax[i - 1][j - 1] + max(diff, 0), localmax[i - 1][j] + diff)
            # current globalmax = max(the globalmax ending yesterday with the same amount of transactions made,
            #                         the current localmax)
            globalmax[i][j] = max(globalmax[i - 1][j], localmax[i][j])

        return globalmax[m-1][k]
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
