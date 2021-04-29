---
layout: post
title:  Data Structure - B+ Tree
date:   2020-09-30 22:00:00 -0700
categories: study-notes
tag: data-structure
---

* content
{:toc}


# B+ Tree

B+ tree is different from B tree as in:
1. B+ tree stores values only in leaf nodes, all internal nodes stores keys and pointers.
2. B+ tree leaf nodes are doubly linked to make range query easier.

B+ tree has boundaries as in:
1. B+ tree of order x can store the maximum number of x keys in a node.
2. B+ tree of order x should store the minimum number of x/2 keys in a node.
3. B+ tree of order x and height h can store the maximum number of (n^h) keys.
4. B+ tree of order x and height h should store the minimum number of 2* (n/2)^(h-1) of keys.

```python
class KeyValues:
  def __init__(self, key, values = []):
    self.key = key
    self.values = values

  def append(self, value):
    self.values.append(value)

class KeyPointers:
  def __init__(self, key, left = None, right = None):
    self.key = key
    self.left = left
    self.right = right

class Node:
  def __init__(self, order, items):
    self.order = order
    self.items = items

  def __len__(self):
    return len(items)

class InnerNode():
  def __init__(self, order, items = []):
    super(order, items)
    self.isLeaf = False

  def isFull(self):
    return self.order == len(self)

  def insert(self, keyPointers):
    if self.isFull():
      raise Exception('Insertion failed because the InnerNode is full.')

    self.items.append(keyPointers)

    for i in range(len(self.items) - 1, 0, -1):
      curr, prev = self.items[i], self.items[i - 1]

      if curr.key == prev.key:
        raise Exception("TODO")
      elif curr.key < prev.key:
        self.items[i], self.items[i - 1] = self.items[i - 1], self.items[i]
        if i + 1 >= len(self.items):
          past = self.items[i + 1]
          past.left = curr.right
          prev.right = curr.left
      else:
        prev.right = curr.left
        continue

class LeafNode(Node):
  def __init__(self, order, items = []):
    ''' items: List<KeyValues> '''
    super(order, items)
    self.isLeaf = True

  def isFull(self):
    return self.order == len(self)

  def find(self, key):
    for i in range(len(self.items)):
      if self.items[i].key == key:
        return self.items[i].value

  def insert(self, key, value):
    if self.isFull():
      raise Exception('Insertion failed because the LeafNode is full.')

    leaf.items.append(KeyValues(key, [value]))

    for i in range(len(self.items) - 1, 0, -1):
      curr, prev = self.items[i], self.items[i - 1] # KeyValues
      if curr.key == prev.key:
        del self.items[i]
        prev.append(value)
      elif curr.key < prev.key:
        self.items[i], self.items[i - 1] = self.items[i - 1], self.items[i]
      else:
        break

  def split(self):
    mid = self.order // 2
    key = self.items[mid].key
    left = LeafNode(this.order, self.items[:mid])
    right = LeafNode(this.order, self.items[mid:])
    innerNode = InnerNode(self.order, [KeyPointers(key, left, right)])
    return innerNode

class BplusTree:
  def __init__(self, order):
    self.order = order
    self.root = LeafNode(order)

  def _find(self, key):
    ''' find the LeafNode that contains the given key '''
    parent = None
    node = self.root
    while not node.isLeaf:
      i = 0
      while i < len(node.items):
        if key < node.items[i].key:
          parent = node
          node = node.items[i].left
          break
        else:
          i += 1

      if i == len(node.items):
        parent = node
        node = node.items[-1].right

    return parent, node

  def insert(self, key, value):
    parent, leaf = self._find(key)
    if not leaf.isFull():
      leaf.insert(key, value)
      return ;

    # split
    innerNode = leaf.split()
    left = innerNode.items[0].left
    right = innerNode.items[0].right

    # insert target kv
    if key < innerNode.items[0].key:
      left.insert(key, value)
    else:
      right.insert(key, value)

    # adjust parent pointers
    if not parent.isFull():
      parent.insert(innerNode.items[0])
      return ;

    if parent.left == leaf:
      parent.left = innerNode
    else:
      parent.right = innerNode

    return ;


  def retrieve(self, key):
    parent, leaf = self._find(key)
    return leaf.find(key)
```
