---
layout: post
title:  Algorithms - Tree
date:   2020-10-06 22:00:00 -0700
categories: study-notes
tag: algorithms
---

* content
{:toc}


Last Update: 2020-10-13

# Basics

## Get Tree Height
```python
'''
Tree height is defined as the length of the path from the root node to the farthest child node.
A single-node tree has height 0.
'''
class TreeHeight:

  def getTreeHeight(self, root):
    if root is None:
      return -1

    left = self.getTreeHeight(root.left)
    right = self.getTreeHeight(root.right)
    return max(left, right) + 1
```

## Preorder Traversal
```python
class PreorderTraversal:

  def recursively(self, root):

    if root is None:
      return ;

    print(root.val)
    self.recursively(root.left)
    self.recursively(root.right)

    return ;

  def iteratively(self, root):

    if root is None:
      return ;

    stk = [root]
    while stk:
        node = stk.pop(-1)
        print(node.val)
        if node.right:
            stk.append(node.right)
        if node.left:
            stk.append(node.left)

    return rst
```

## Inorder Traversal
```python
class InorderTraversal:

  def recursively(self, root):

    if root is None:
      return ;

    self.recursively(root.left)
    print(root.val)
    self.recursively(root.right)

    return ;

  def iteratively(self, root):

    if root is None:
      return ;

    stk = []
    node = root
    while True:
      # keep pushing left child to stack till it runs out left child, including root
      if node:
        stk.append(node)
        node = node.left
      # when at the end of left branch, consume the next node on top of the stack
      # don't forget to redirect pointer to the right child and start going down
      # its left branch looking for the node holding the next smallest value
      elif stk:
        node = stk.pop(-1)
        print(node.val)
        node = node.right
      # when current node is None and stk is empty, end the loop
      else:
        break

    return ;
```

## Postorder Traversal
```python
class PostorderTraversal:

  def recursively(self, root):

    if root is None:
      return ;

    self.recursively(root.left)
    self.recursively(root.right)
    print(root.val)

    return ;

  def iteratively(self, root):

    if root is None:
      return ;

    stk1 = [root]
    stk2 = []
    while stk1:
      node = stk1.pop(-1)
      stk2.append(node)
      if node.left:
        stk1.append(node.left)
      if node.right:
        stk1.append(node.right)

    while stk2:
      print(stk2.pop(-1))
```

# Intermedium

## L/R Invert Tree
```python
'''
Given a tree, return its inversion.
Invert the tree by swapping it's left subtrees and right subtrees recursively
'''
class InvertTree:

  '''
  Compare the below bottomUp and topDown approaches and feel their difference
  '''
  def recursively_bottomUp(self, root):
    if root is None:
      return None

    self.recursively(root.left)
    self.recursively(root.right)
    root.left, root.right = root.right, root.left

    return root

  def recursively_topDown(self, root):
    if root is None:
      return None

    root.left, root.right = root.right, root.left
    self.recursively(root.left)
    self.recursively(root.right)

    return root

  # Simply do it in pre order iteratively
  def iteratively(self, root):
    if root is None:
      return None

    stk = [root]
    while stk:
      node = stk.pop(-1)
      if node.right:
        stk.append(node.right)
      if node.left:
        stk.append(node.left)

      node.left, node.right = node.right, node.left

    return root
```

## Identical Tree
```python
'''
Given two trees
Return True if they are identical, False otherwise.
'''
class IdenticalTree:

  def recursively(self, a, b):

    if a is None and b is None:
      return True
    if a is None or b is None:
      return False

    root_identical = a.val == b.val
    left_identical = self.recursively(a.left, b.left)
    right_identical = self.recursively(a.right, b.right)

    return root_identical and left_identical and right_identical

  def iteratively(self, a, b):
    if a is None and b is None:
      return True
    if a is None or b is None:
      return False

    # Compare each node in preorder iteratively
    astack = [a]
    bstack = [b]
    # astack and bstack always have the same number of items
    while astack:
      a = astack.pop(-1)
      b = bstack.pop(-1)
      if a.val != b.val:
        return False

      if a.right is None and b.right is None:
        pass
      elif a.right is None or b.right is None:
        return False
      else:
        astack.append(a.right)
        bstack.append(b.right)

      if a.left is None and b.left is None:
        pass
      elif a.left is None or b.left is None:
        return False
      else:
        astack.append(a.left)
        bstack.append(b.left)

    return True
```

## Mirror Tree
```python
'''
A mirror tree is symmetric around its center.

Slightly different from # Ref: IdenticalTree.
'''
class MirrorTree:

  def recursively(self, a, b):
    if a is None and b is None:
      return True
    if a is None or b is None:
      return False

    if a.val != b.val:
      return False

    return self.recursively(a.left, b.right) and self.recursively(a.right, b.left)

  def iteratively(self, a, b):
    if a is None and b is None:
      return True
    if a is None or b is None:
      return False

    astk = [a]
    bstk = [b]
    while astk:
      a = astk.pop(-1)
      b = bstk.pop(-1)
      if a.left is None and b.right is None:
        pass
      elif a.left is None or b.right is None:
        return False
      else:
        astk.append(a.left)
        bstk.append(b.right)

      if a.right is None and b.left is None:
        pass
      elif a.right is None or b.left is None:
        return False
      else:
        astk.append(a.right)
        bstk.append(b.left)

    return True
```

## Create Mirror Tree
```python
'''
Given a tree, return a new tree that is symmetric to the given tree.
'''
class NewMirrorTree:

  def recursively(self, root):
    if root is None:
      return None

    rootCopy = TreeNode(root.val)
    rootCopy.left = self.recursively(root.right)
    rootCopy.right = self.recursively(root.left)
    return rootCopy

  def iteratively(self, root):
    if root is None:
      return None

    rootCopy = TreeNode(root.val)

    stk = [root]
    stkCopy = [rootCopy]
    while stk:
      node = stk.pop(-1)
      nodeCopy = stkCopy.pop(-1)

      if node.right:
        rightCopy = TreeNode(node.right.val)
        nodeCopy.left = rightCopy
        stk.append(node.right)
        stkCopy.append(rightCopy)

      if node.left:
        leftCopy = TreeNode(node.left.val)
        nodeCopy.right = leftCopy
        stk.append(node.left)
        stkCopy.append(leftCopy)

    return rootCopy
```

# Advanced

# Hard

# Resources

```python
class TreeNode(object):
    def __init__(self, val):
        self.val = val
        self.left = None
        self.right = None
```
