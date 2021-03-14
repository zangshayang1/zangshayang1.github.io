---
layout: post
title:  Algorithms - Dynamic Programming
date:   2021-03-13 22:30:00 -0700
categories: study-notes
tag: algorithms
---

* content
{:toc}


Last Update: 2020-03-13


## Edit Distance
```python
'''
Given two strings, return the minimum edit distance.

Given:
  s1: horse
  s2: ros
Return: 3
'''
class EditDistance:
  def solution(self, s1, s2):
    dp = [[0 for _ in range(len(s2) + 1)] for _ in range(len(s1) + 1)]

    for i in range(len(s1) + 1):
        dp[i][0] = i
    for j in range(len(s2) + 1):
        dp[0][j] = j

    for i in range(1, len(s1) + 1):
        for j in range(1, len(s2) + 1):
            if s1[i - 1] == s2[j - 1]:
                dp[i][j] = dp[i - 1][j - 1]
            else:
                dp[i][j] = min(dp[i - 1][j - 1], dp[i - 1][j], dp[i][j - 1]) + 1

    return dp[-1][-1]
```

## Job Schedule
```python
'''
Given a list of jobs with their start times, end times and profits.
Return the maximum profit one can achieve by completing these jobs without time conflict.

Args:
  startTimes: a list of start times of corresponding jobs (integer)
  endTimes: a list of end times of corresponding jobs (integer)
  profits: a list of profits of corresponding jobs (integer)

Returns:
  maximum profit achievable
'''
class JobSchedule:
  def solution(self, startTimes, endTimes, profits):

    # sort by endTime, startTime and finally profit
    jobs = sorted(zip(startTimes, endTimes, profits), key=lambda x: (x[1], x[0], x[2]))
    endTimes = sorted(endTimes)

    # define dp array: mp[i] is the max profit up to i_th job
    # mp[0] = 0: when there is no job, there is no profit
    mp = [0 for _ in range(len(jobs) + 1)]

    for i in range(1, len(mp)):
      # current job
      s, e, p = jobs[i - 1]
      # find the last job that ends before current job start time s
      j = self._find_job_that_ends_before(s, endTimes)
      # job index j maps to max profit index j + 1
      # update mp[i] with the bigger value of:
      # 1. previous max profit mp[i-1] (not work on current job)
      # 2. the max profit up to (j+1)_th job + current job profit mp[j + 1] + p
      mp[i] = max(mp[i - 1], mp[j + 1] + p)

    return mp[-1]

  def _find_job_that_ends_before(self, target, endTimes):
    for i in range(len(endTimes)):
      if endTimes[i] > target:
        return i - 1

    return len(endTimes) - 1
```
