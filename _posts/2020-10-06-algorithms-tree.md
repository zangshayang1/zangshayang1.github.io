---
layout: post
title:  Algorithms - Tree
date:   2020-10-06 22:00:00 -0700
categories: study-notes
tag: algorithms
---

* content
{:toc}


Last Update: 2020-10-18

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

## BinaryTreeTypes
```python
class BinaryTreeTypes:
  '''
  A balanced binary tree is height-balanced.
  '''
  def isBalancedTree(self, root):
    if root is None:
      return False

    isBalanced, _ = self._balanceAndHeight(root)
    return isBalanced

  def _balanceAndHeight(self, root):
    if root is None:
      return True, -1

    leftIsBalanced, leftHeight = self._balanceAndHeight(root.left)
    rightIsBalanced, rightHeight = self._balanceAndHeight(root.right)

    if leftIsBalanced and rightIsBalanced and abs(leftHeight - rightHeight) <= 1:
      return True, max(leftHeight, rightHeight) + 1
    else:
      return False, max(leftHeight, rightHeight) + 1

  '''
  In a complete binary tree, every level except the last, is completely filled and
  all nodes in the last level are as far left as possible.

  The following solution is built on top of the key observation: A complete binary tree
  can be efficiently represented using an array, meaning there will be 0 space wasted
  representing NULL node.
  '''
  def isCompleteTree(self, root):
    if root is None:
      return False

    queue = [root]

    while queue:
      curr = queue.pop(0)
      if curr is None:
        break

      queue.append(curr.left)
      queue.append(curr.right)

    while queue:
      if queue.pop(0) is not None:
        return False

    return True

  '''
  In a full binary tree, every node has either 0 or 2 children.
  '''
  def isFullTree(self, root):
    if root is None:
      return False

    if root.left is None and root.right is None:
      return True

    if root.left is None or root.right is None:
      return False

    return self.isFullTree(root.left) and self.isFullTree(root.right)

  '''
  In a degenerate tree, every node has at most 1 child, which is essentially a linked list.

  This problem requires that you make in-place change to a given binary tree and convert it
  to a degenerate tree with every node only having right child.
  '''
  def convertToDegenerateTree(self, root):
    if root is None:
      return ;

    # recursion terminates when root is a leaf node
    self.convertToDegenerateTree(root.left)
    self.convertToDegenerateTree(root.right)

    tmp = root.right
    root.right = root.left
    root.left = None

    while root.right:
      root = root.right

    root.right = tmp

    return ;
```


# Advanced

## Lowest Common Ancestor
```python
class LowestCommonAncestor:

  '''
  V1 assumes:
  1. that every TreeNode has a parent pointer
  2. that a and b must exist in the given tree
  '''
  def v1(self, root, a, b):
    if root is None:
      return None

    astk = []
    while a:
      astk.append(a)
      a = a.parent

    bstk = []
    while b:
      bstk.append(b)
      b = b.parent

    lastCommon = None
    while a and b:
      a, b = astk.pop(-1), bstk.pop(-1)
      if a == b:
        lastCommon = a
      else:
        break

    return lastCommon

  '''
  V2 assumes that a and b must exist in the given tree.

  Otherwise, when only one node exists in the given tree, this algorithms fails to return None.
  '''
  def v2(self, root, a, b):
    if root is None:
      return None

    if root == a or root == b:
      return root

    left = self.v2(root.left, a, b)
    right = self.v2(root.right, a, b)

    if left and right:
      return root
    if left:
      return left
    if right:
      return right

    return None

  '''
  V3 assumes nothing, meaning:
  1. No parent pointer.
  2. The given node a and b don't necessarily exist in the given tree.
  '''
  def v3(self, root, a, b):

    lca, isLca = self._v3_helper(root, a, b)
    if isLca:
      return lca
    else:
      return None

  def _v3_helper(self, root, a, b):
    if root is None:
      return None, False

    left, leftIsLca = self._v3_helper(root.left, a, b)
    right, rightIsLca = self._v3_helper(root.right, a, b)

    if leftIsLca:
      return left, leftIsLca
    if rightIsLca:
      return right, rightIsLca

    if left and right:
      return root, True

    # comparing root with A and B must be done in Conquer step
    # because returned info include if it is an LCA
    # As long as one of left subtree and right subtree contains a or b, root is LCA
    if root == A or root == B:
      return root, left or right

    if left:
      return left, False

    if right:
      return right, False

    return None, False
```

## Boundary Of Binary Tree
```python

```

# Hard

# Resources

```python
class TreeNode(object):
    def __init__(self, val):
        self.val = val
        self.parent = None
        self.left = None
        self.right = None
```
