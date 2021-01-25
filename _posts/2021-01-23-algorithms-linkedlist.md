---
layout: post
title:  Algorithms - LinkedList
date:   2021-01-23 15:00:00 -0700
categories: study-notes
tag: algorithms
---

* content
{:toc}


Last Update: 2021-01-23

# Basics

## Find LinkedList Mid Node
```python
class FindMidNode:
  def solution(self, head):
    dummy = ListNode(-1, head)
    slow, fast = dummy, dummy

    while fast and fast.next:
      fast = fast.next.next
      slow = slow.next

    # Given [1, 2, 3, 4, 5], return ListNode(3)
    # Given [1, 2, 3, 4, 5, 6], return ListNode(4)
    if fast is None:
      return slow
    if fast.next is None:
      return slow.next
```

## Reverse a LinkedList
```python
class ReverseLinkedList:
  def solution(self, head):
    pre, cur = None, head

    while cur:
      nxt = cur.next
      cur.next = pre

      pre = cur
      cur = nxt

    return pre
```

# Median

## Remove Kth Node From End
```python
class RemoveKthNode:
  def solution(self, head, K):

    cur = head
    while K > 0:
      if cur is None:
        raise Exception('Kth node doesn\'t exist.')

      cur = cur.next
      K -= 1

    pre, k_away = None, head
    while cur:
      pre = k_away
      k_away = k_away.next
      cur = cur.next

    if pre is None:
      return head.next
    else:
      pre.next = k_away.next
      return head
```

## Is Parlindrome LinkedList
```python
class IsParlindromeLinkedList:
  def solution(self, head):
    if head is None: return True

    slow, fast = ListNode(-1, head), ListNode(-1, head)
    stk = []
    while fast and fast.next:
      fast = fast.next.next
      slow = slow.next
      stk.append(slow)

    if fast is None:
      # the LinkedList has odd number of nodes
      # slow -> middle unique one; stack top -> middle unique one;
      stk.pop(-1)

    slow = slow.next
    while slow:
      if slow.val != stk.pop(-1).val:
        return False
      slow = slow.next

    return len(stk) == 0
```

## Find Intersection
```python
'''
Given two LinkedList heads as below:
1 -> 2 -> 3 -> 4
        /
1 -> 2 -

Return the intersection node: ListNode(3)
'''
class FindIntersection:
  def solution(self, head1, head2):
    stk1, stk2 = [], []
    while head1:
      stk1.append(head1)
      head1 = head1.next

    while head2:
      stk2.append(head2)
      head2 = head2.next

    prev = None
    while stk1 and stk2:
      a, b = stk1.pop(-1), stk2.pop(-1)
      if a != b:
        break
      prev = a

    return prev
```

## Arithmetic Sum Of Two LinkedList
```python
'''
Given:
  2 -> 7 -> 8
  3 -> 5 -> 4
Return:
  5 -> 2 -> 3 -> 1
'''
class SumOfLinkedList:
  def solution(self, h1, h2):
    rst = ListNode(-1, None)
    ptr = rst

    carryOne = False
    while h1 and h2:

      val = h1.val + h2.val + 1 if carryOne else h1.val + h2.val
      carryOne = val > 9
      val = val - 10 if carryOne else val

      ptr.next = ListNode(val, None)
      ptr = ptr.next
      h1 = h1.next
      h2 = h2.next

    while h1:
      val = h1.val + 1 if carryOne else h1.val
      carryOne = val > 9
      val = val - 10 if carryOne else val

      ptr.next = ListNode(val, None)
      ptr = ptr.next
      h1 = h1.next

    while h2:
      val = h2.val + 1 if carryOne else h2.val
      carryOne = val > 9
      val = val - 10 if carryOne else val

      ptr.next = ListNode(val, None)
      ptr = ptr.next
      h2 = h2.next

    if carryOne:
      ptr.next = ListNode(1, None)

    return rst.next
```

# Resources

```python
class ListNode:
  def __init__(self, val, next):
    self.val = val
    self.next = next

class Utils:

  @staticmethod
  def deepcopy(head):
    copy = ListNode(-1, None)
    ptr = copy
    while head:
      ptr.next = ListNode(head.val, None)
      head = head.next
      ptr = ptr.next

    return copy.next

```
