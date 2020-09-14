---
layout: post
title: ICS260 - Divide and Conquer
date: 2017-02-28 13:20:00 -0800
categories: study-notes
tag: algorithms
---

* content
{:toc}



## Divide and Conquer Theorem

In the example of __Merge Sort__:  
During divide-phase, the algorithm splits the problem into half and recursively call itself on the two subproblems.   
During conquer-phase, the algorithm does some operations that take O(n) to merge the sorted sub results into real higher-level results.  

Mathematically:  

$$ T(n) = 2 T(\frac{n}{2}) + O(n) $$

$$ T(n) = n \log{n} $$


Now let constants:  
1. a >= 1 denoting the number of recursive calls we need at each recursion
2. b >= 1 denoting the number of subproblems
3. f(n) denoting the asymptotical cost we need to pay at each recursion.  

The above became:  

$$ T(n) = a T(\frac{n}{b}) + f(n) $$

#### The master theorem says:  

1. Operation cost is __upper bounded__:  

$$ if\;f(n) = O(n^{\log_{b}^{a - \epsilon}})\;\;for\;some\;constant\;\epsilon > 0 $$

$$ T(n) = \Theta(n^{\log_{b}^{a}}) $$

2. Operation cost is __sandwiched__:  

$$ if\;f(n) = \Theta(n^{\log_{b}^{a}}) $$

$$ T(n) = \Theta(n^{\log_{b}^{a}} \log{n})$$

3. Operation cost is __lower bounded__:  

$$ if\;f(n) = \Omega(n^{\log_{b}^{a + \epsilon}})\;\;for\;some\;constant\;\epsilon > 0 $$

$$ T(n) = \Theta(f(n)) $$

__Interpretation:__
1. If the operation cost is upper bounded by $$ n^{\log_{b}^{a}} $$, recursion cost matters.  

2. If the operation cost is equal to $$ n^{\log_{b}^{a}} $$, both recursion and operation costs matter the same.

3. If the operation cost is lower bounded by $$ n^{\log_{b}^{a}} $$, operation cost matters.  

Taking __Merge Sort__ as an example, __intuitively__:

1. If operation cost is __linearly__ decreasing as we divide the problem (case2), T(n) is the operation cost at each level times the number of levels.  

2. If operation cost is __superlinearly__ decreasing as we divide the problem (case3), T(n) is dominated by the operation cost at the first level where the number of instance is n.  

3. If operation cost is __sublinearly__ decreasing as we divide the problem (case1), T(n) is dominated by the operation cost of each recursive call while the number of levels doesn't matter.  

#### The simplified method says:  

Having a problem with cost:  

$$ T(n) = a T(\frac{n}{b}) + \Theta(n^k) \; where \; k >= 1 $$

1. If a > b^k, recursive work dominates:
$$ T(n) = \Theta(n^{\log_{b}^{a}}) $$

2. If a = b^k, both matters:
$$ T(n) = \Theta(n^{\log_{b}^{a}} \log{n})$$

3. If a < b^k, the only non-recursive call dominates:
$$ T(n) = \Theta(n^k) $$

---

## Algorithm Problems

#### construct a binary heap recursively  

_NOTE: we've talked about running "heapify()" iteratively in the post "Priority Queue" in O(n)._

Here is the recursive one, which runs in:  

$$ T(n) = 2 T(\frac{n}{2}) + O(\log{n}) $$

$$ T(n) = O(n) $$

``` python
class MinHeap(object):

  def __init__(self, nums):
		self.size = len(nums)
		self.array = [num for num in nums]
		self.heapify()

  def heapify(self):
    self._recHeapify(self.array, 0)
    # 0 index the first entry in self.array
    # aka the root if you think the array as a binary tree.
    return ;

  def _recHeapify(self, a, k):
    if 2 * k + 1 < len(a):
      self._recHeapify(a, 2 * k + 1) # take care of the left child
    if 2 * k + 2 < len(a):
      self._recHeapify(a, 2 * k + 2) # take care of the right child
    # the above is to divide till the last leaf is reached.
    self._siftDown(k)
    # the above is to conquer
    return ;

  def _siftDown(self, k):
    """
    refer to post "Priority Queue" for implementation details.
    """
    return ;
```

---

__Thought: The above and the following problems show to me that: Applying divide and conquer algorithm to solve a problem is like solving the problem on a binary tree. Solve the left part, solve the right part and use the results from those two parts to solve the whole problem from the root.__

---

#### Planar Closest Pair Problem

Input: a list of points: [{Point(x, y)}]  
Output: the distance between the closest pair of points

Algorithm:  
1. recursively find the closest distance of the left half
2. recursively find the closest distance of the right half
3. find the closest pair with one point in left and one in right and compare their distance with what we found above.


``` python
def closestPair(points):
  # base case
  if len(points) <= 3:
    dist1 = dist(points[0], points[1])
    dist2 = dist(points[1], points[2])
    dist3 = dist(points[0], points[2])
    return min(dist1, dist2, dist3)

  # O(nlogn)
  Y = [sorted points in increasing order along y-axis]

  mid_x = sum[points.x] / len(points) # float

  # divide into left half and right half by mid_x
  left = [p if p.x <= mid_x for p in points]
  right = [p if p.x > mid_x for p in points]

  # recursive calls to further divide them,
  # which returns the closest distance found
  left_delta = closestPair(left)
  right_delta = closestPair(right)

  # restrict searching area to points
  # whose x is within [mid_x - delta, mid_x + delta]
  delta = min(left_delta, right_delta)
  left = [p if p.x > mid_x - delta for p in left]
  right = [p if p.x < mid_x + delta for p in right]

  # the following is the whole essence of the problem, which takes O(n)
  # so the problem can be solved in O(nlogn)
  for i in range(len(Y)):
    # starting from the point whose y value is the smallest
    # find one that is in left part of the searching area defined above
    if Y[i] not in left:
      continue
    # compare p with the next 6 points in Y found in right part of the searching area
    p = Y[i]
    j = 1 # pointer to the next Point in Y
    count = 0
    while i + j < len(Y) and count < 6:
      # why 6? TODO: add graph
      # but at least you know why the base case consists of 3 points.
      if Y[i + j] not in right:
        j += 1
        continue
      count += 1 # found one
      if dist(p, Y[i + j]) < delta:
        delta = dist(p, Y[i + j])
      j += 1

  return delta
```

---

### Warm up

__Stock problem:__ Given an array of n days' stock prices, only one transaction can be made, find the optimal solution.

__Key observation:__ If the optimal one holds the stock at day (n/2), that means the purchasing time is before that date and selling time is after that date. Else, the optimal one happens either between day 1 to day (n/2) or day (n/2) to the last day. This way, we reduced the problem in half and two recursive calls are needed each time.  

__Formulation:__   
```
solution(arr) = max(
                  solution(left half of arr),
                  solution(right half of arr),
                  max(right half) - min(left half)
                  )
```

$$ T(n) = 2 T(\frac{n}{2}) + O(n) $$

---

### Challenges

---

#### Find median

__Problem Description:__ If given two sorted arrays A and B, what is the best algorithm to find the median? Merge and Count takes O(n). What if they are not sorted? Sort and Merge takes O(nlogn).  
What if they are sorted but it takes a lot of time to access each number? You are given a function call to query the Kth smallest number in either array and your goal is to find the median with as few queries as possible.

__Key Observation:__
1. If the median in A equals to the median in B, then it is the MEDIAN for the unioned array.
2. If A's median < B's median, we know that the left half of A are all smaller than the MEDIAN and the right half of B are all bigger than the MEDIAN. So we can discard those two halfs.
3. Else, we can discard the other two halfs.

__Solution:__
```
global A, B

def median(n, a, b):
  // a, b mark the starting point for each array
  if n == 1: return min(A[a + 1], B[b + 1])
  k = celi(n / 2)
  if A[a + k] == B[b + k]:
    return A[a + k]
  elif A[a + k] < B[b + k]:
    return median(n - n/2, a + n/2, b)
  else:
    return median(n - n/2, a, b + n/2)
```
---
#### Equivalence Testing

__Problem Description:__ Given a set of elements, each of them has a value. You have no way to access their values but you are given a equivalentTest(a, b) function that returns True if a's value == b's value and False otherwise. How to find out if more than half of the elements have the same value?

__Key Observation:__ If there are more than half elements that share the same value and if you divide the elements into two sets of equal size, then the following must be True: If the size of the two sets is (n/2), one of them contains __more than__ (n/4) elements with the same value.__ __Otherwise,__ such value doesn't exist. The problem keeps dividing itself into half until there is only one element in the input array.  

__Solution:__
```
def superMajority(A<elements>):
  if len(A) == 1: return (True, A[0], 1)

  (left, x, left_num) = superMajority(A[ : len(A)/2])
  if left is True:
    right_num = # of elements in A[len(A)/2 : ] whose value == x
    if left_num + right_num > len(A) / 2:
      return (True, x, left_num + right_num)

  (right, x, right_num) = superMajority(A[len(A)/2 : ])
  if right is True:
    left_num = # of elements in A[ : len(A/2)] whose value == x:
    if left_num + right_num > len(A) / 2:
      return (True, x, left_num + right_num)

  return (False, NULL, NULL)
```
---

#### Upper Envelope Problem

__Problem Description:__ Suppose that you have n lines in a 2D spaces where no three lines crossover the same point.  

$$ \{\; L_i : y_i = ax_i + b \;|\; 1 \leq i \leq n \;\} $$

__If:__  

$$ there \; \exists \; k \; and \; X $$  

__For:__  

$$ \forall \; i \neq k \; and \; 1 \leq i \leq n $$  

$$ \forall \; x \in X $$  

__Having:__  

$$ y_k > max(y_i )$$  

Then we say k is __visible__.
Now the problem is, given a set of lines, find those visible lines.

__Key Observation:__
1. If you draw a few lines you will see that when x walks from negative infinity to positive infinity, those that becomes visible are having higher slope values than those that previously visible. In other words, their slopes are monotonically increasing.
2. When x walks from negative infinity to positive infinity, if the current visible line is i and it will crossover with line j at the next crossover point. line j will be the next visible line.

__Solution:__
```
def upperEnvelope(lines - sorted):
  if len(lines) == 1: return line[0]

  n = len(lines)
  left = upperEnvelope(lines[ : n / 2])
  right = upperEnvelope(lines[n / 2: ])
  // the above is divide

  // the below is conquer
  lb, rb = -1, 1

  // left is a line on which you can call .yhat_at() to return the corresponding y value.
  while left.yhat_at(lb) <= right(lb):
    // find the left and right boundaries where we can find a crossover point for sure
    lb = 2 * lb

  while right.yhat_at(rb) <= left(rb):
    rb = 2 * rb

  for i in range(lb, rb):
    if left.yhat_at(i) <= right.yhat_at(i):
      // return a line object constructed in the following way
      return Line(left = left,
                  right = right,
                  crossover = i
                  )

```



<!-- #################################### -->
<!-- #################################### -->
<!-- #################################### -->
<!-- #################################### -->
<!-- #################################### -->
<!-- #################################### -->
<!-- #################################### -->
<!-- #################################### -->
<!-- #################################### -->
<!-- #################################### -->
<!-- #################################### -->
<!-- #################################### -->
<!-- #################################### -->
<!-- #################################### -->
<!-- #################################### -->
<!-- #################################### -->
<!-- #################################### -->
<!-- #################################### -->
<!-- #################################### -->
<!-- #################################### -->
<!-- #################################### -->
<!-- #################################### -->
<!-- #################################### -->
<!-- #################################### -->
