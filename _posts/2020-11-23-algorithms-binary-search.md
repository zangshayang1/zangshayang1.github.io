---
layout: post
title:  Algorithms - Binary Search
date:   2020-11-23 20:08:00 -0700
categories: study-notes
tag: algorithms
---

* content
{:toc}


Last Update: 2020-11-23

## Binary Search Template
```python
class BinarySearchTemplate:

  def solution(self, A, t):

    assert len(A) > 0

    s, e = 0, len(A) - 1

    while s + 1 < e: # classic termination condition

      m = s + (e - s) // 2 # prevent INT_MAX overflow

      if t = A[m]:
        return m

      if t > A[m]:
        s = m
      else:
        e = m

    if t == A[s]: # classic edge case handling
      return s
    if t == A[e]:
      return e

    return -1 # find no match
```
