---
layout: post
title:  Algorithms - Tree
date:   2020-10-06 22:00:00 -0700
categories: study-notes
tag: algorithms
---

* content
{:toc}


Last Update: 2020-11-12

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

## Level Order Traversal
```python
class LevelOrderTraversal:

  def solution(self, root):
    if root is None:
      return ;

    currLevel = []
    nextLevel = [root]
    while currLevel or nextLevel:
      if len(currLevel) == 0:
        currLevel = nextLevel
        nextLevel = []
        for node in currLevel:
          print(node)

      curr = currLevel.pop(0)
      if curr.left:
        nextLevel.append(curr.left)
      if curr.right:
        nextLevel.append(curr.right)

    return ;
```

# Median

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

## Binary Tree Types
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
  A perfect tree is a complete tree where the last level is completely filled.
  '''
  def isPerfectTree(self, root):
    if root is None:
      return False

    counter = 1
    currLevel = []
    nextLevel = [root]
    while currLevel or nextLevel:
      if not currLevel:
        if len(nextLevel) != counter:
          return False
        currLevel = nextLevel
        nextLevel = []
        counter = 2 * counter

      node = currLevel.pop(0)
      if node.left:
        nextLevel.append(node.left)
      if node.right:
        nextLevel.append(node.right)

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

  '''
  A binary search tree satisfies:
  1. From the root, the left subtree is a binary search tree and the maximum value
  from left subtree is smaller than root value.
  2. From the root, the right subtree is a binary search tree and the minimum value
  from right subtree is greater than root value.
  '''
  def isBinarySearchTree(self, root):
    if root is None:
      return False

    isBst, _, _ = self._isBstHelper(root)
    return isBst

  def _isBstHelper(self, root):
    if root is None:
      # base case, return isBst=True, minVal=2^31-1, maxVal=-2^31
      return True, 2**31-1, -2**31

    leftIsBst, leftMin, leftMax = self._isBstHelper(root.left)
    rightIsBst, rightMin, rightMax = self._isBstHelper(root.right)

    if leftIsBst and rightIsBst and leftMax < root.val and rightMin > root.val:
      return True, min(root.val, leftMin), max(root.val, rightMax)
    else:
      return False, -1, -1
```

## Sum Of Left Leaves
```python
'''
This problem is simple but inspiring. It sheds some light on how information flow
in a binary tree.

The problem is, given a tree, return the sum of all left leaves value.

The question here is: how does a leaf node know if it is a left leaf?

The solution introduces a way to pass info from parent to child, which is necessary
to solve this problem because a node itself and its substrees don't know if it is
a left node or right node.

Meanwhile, parent node doesn't know if its child is a leaf or not. It is determined
at current node level so that part of info is gathered differently.

However, there are some other cases where the needed info is not going to be complete
until you traverse through the current node and its substrees. For example,
# Ref: LowestCommonAncestor.v3 tells how info can be passed in a bottom-up fashion.
'''
class SumOfLeftLeaves:

  def solution(self, root):
    if root is None:
      return 0

    return self._helper(root.left, True) + self._helper(root.right, False)

  def _helper(self, root, isLeftChild):
    if root is None:
      return 0

    if root.left is None and root.right is None and isLeftChild:
      return root.val

    return self._helper(root.left, True) + self._helper(root.right, False)
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

## Find Path To Target
```python
'''
Given a binary tree and a target node, assuming the target node is unique if it exists,
return the path from root to the target.

NOTE: this problem reveals another solution to LowestCommonAncestor problem in that,
if the paths to two target nodes are determined, the last common node is the lowest
common ancestor.
'''
class FindPaths:

  '''
  V1: assuming the target node always exists
  '''
  def findPathToTargetV1(self, root, target):
    path = []
    self._v1_helper(root, target, path)
    return path

  def _v1_helper(self, root, target, path):
    if root is None:
      return

    if root.val == target.val:  
      path.append(root)
      return

    path.append(root)
    self._v1_helper(root.left, target, path)
    self._v1_helper(root.right, target, path)
    return

  '''
  V2: without the v1 assumption, classic dfs with backtracking.
  '''
  def findPathToTargetV2(self, root, target):
    path = []
    self._v2_helper(root, target, path)
    return path

  def _v2_helper(self, root, target, path):
    if root is None:
      return False

    if root.val == target.val:
      path.append(root)
      return True

    path.append(root)
    leftFound = self._v2_helper(root.left, target, path)
    rightFound = self._v2_helper(root.right, target, path)

    if not leftFound and not rightFound:
      path.pop(-1) # backtracking
      return False
    else:
      return True

  '''
  Variant: Find Paths To All Leaves
  '''
  def findPathsToLeaves(self, root):
    paths = []
    self._variant_helper(root, [], paths)
    return paths

  def _variant_helper(self, root, path, paths):
    if root is None:
      return ;

    if root.left is None and root.right is None:
      path.append(root)
      paths.append(path)
      return ;

    path.append(root)
    self._variant_helper(root.left, path, paths)
    self._variant_helper(root.right, path, paths)

    return ;
```

## Boundary Of Binary Tree
```python
'''
A very interesting problem. Given a binary tree, return its nodes on the boundaries
in the order of left boundary, leave boundary and right boundary, counter-clock wise.
NOTE: No duplicate (root, leftmost node, rightmost node) allowed in the returned result.

To further define left/right boundary:
1. If root does have left/right child, its left/right boundary is defined
as a path from the root to the leftmost/rightmost node.
2. If root does not have left/right child, its left/right boundary is defined
as the root node only.
'''
class BinaryTreeBoundaries:

  def solution(self, root):
    if root is None:
      return []

    # handle single-node tree
    singleNodeBoundary = [root]

    # all _collectFunc() below return [] when there is no substree
    leftBoundary = self._collectLeft(root.left)
    rightBoundary = self._collectRight(root.right)
    leftLeaves = self._collectLeaves(root.left)
    rightLeaves = self._collectLeaves(root.right)

    # python in-place reverse
    rightBoundary.reverse()

    boundaries = singleNodeBoundary + leftBoundary[:-1] + leftLeaves + rightLeaves + rightBoundary[1:]

    return [node.val for node in boundaries]

  def _collectLeft(self, root):
    left = []

    while root:
      left.append(root)
      # keep appending the left child till there is no left child
      if root.left:
        root = root.left
        continue
      # if there is no right child, you've reached the leftmost leaf
      # otherwise, switch to its right child and start explore left subtree again
      root = root.right

    return left

  def _collectRight(self, root):
    right = []

    while root:
      right.append(root)

      if root.right:
        root = root.right
        continue

      root = root.left

    return right

  def _collectLeaves(self, root):
    if root is None:
      return []

    if root.left is None and root.right is None:
      return [root]

    left = self._collectLeaves(root.left)
    right = self._collectLeaves(root.right)

    return left + right

  def _collectLeavesIteratively(self, root):
    if root is None:
      return []

    leaves = []
    stk = [root]
    # Preorder traversal
    while stk:
      curr = stk.pop(-1)
      if curr.left is None and curr.right is None:
        leaves.append(curr)
      if curr.right:
        stk.append(curr.right)
      if curr.left:
        stk.append(curr.left)

    return leaves
```

## Diameter of Binary Tree
```python
'''
Diameter of a tree is the maximum distance between any two nodes in the tree.
The two nodes are either:
1. both in the left subtree
2. both in the right subtree
3. one in the left subtree and another in the right subtree
'''
class BinaryTreeDiameter:

  def solution(self, root):
    return max(self.helper(root)) - 1

  '''
  For the following helper() function:
  The first returned value is the diameter of the root tree.
  The second returned value is the height of the root tree.
  '''
  def helper(self, root):
    # A single node tree has diameter 0 and height 0
    if root is None:
      return -1, -1

    leftMaxDist, leftHeight = self.helper(root.left)
    rightMaxDist, rightHeight = self.helper(root.right)

    rootMaxDist = leftHeight + rightHeight + 2
    rootHeight = max(leftHeight, rightHeight) + 1

    return max(leftMaxDist, rightMaxDist, rootMaxDist), rootHeight
```

## Maximum Path Sum
```python
class MaximumPathSum:

  def fromRootToAnyAllPositive(self, root):
    if root is None:
      return 0

    left = self.fromRootToAnyAllPositive(root.left)
    right = self.fromRootToAnyAllPositive(root.right)
    return max(left, right) + root.val

  def fromRootToAnyWithNegative(self, root):
    if root is None:
      return 0

    left = self.fromRootToAnyWithNegative(root.left)
    right = self.fromRootToAnyWithNegative(root.right)
    return max(left, right, 0) + root.val

  def fromAnyToAnyAllPositive(self, root):
    return self.helper1(root)[0]

  '''
  The first returned value is the any-to-any maximum path sum
  The second returned value is the root-to-any maximum path sum
  '''
  def helper1(self, root):
    if root is None:
      return 0, 0

    leftAA, leftRA = self.helper1(root.left)
    rightAA, rightRA = self.helper1(root.right)

    rootAA = max(leftAA, rightAA, leftRA + rightRA + root.val)
    rootRA = max(leftRA, rightRA) + root.val

    return rootAA, rootRA

  def fromAnyToAnyWithNegative(self, root):
    if root is None:
      return 0

    return self.helper2(root)[0]

  def helper2(self, root):
    if root is None:
      return - 2**32, - 2**32

    leftAA, leftRA = self.helper2(root.left)
    rightAA, rightRA = self.helper2(root.right)

    rootAA = max(leftAA, rightAA, max(leftRA, 0) + max(rightRA, 0) + root.val)
    rootRA = max(max(leftRA, 0), max(rightRA, 0)) + root.val

    return rootAA, rootRA
```

## Construct Binary Tree From Preorder and Inorder Sequence
```python
class ConstructFromSequences:

  def solution(self, preorder, inorder):

    assert len(preorder) == len(inorder)

    if len(preorder) == 0:
      return None

    rootVal = preorder[0]
    root = TreeNode(rootVal)
    i = inorder.index(rootVal)

    leftInorder = inorder[:i]
    rightInorder = inorder[i+1:]

    leftPreorder = preorder[1: i+1]
    rightPreorder = preorder[i+1:]

    root.left = self.solution(leftPreorder, leftInorder)
    root.right = self.solution(rightPreorder, rightInorder)

    return root
```

## Convert Tree To 2D Array Horizontally/Vertically
```python
class ConvertTreeTo2DArray:

  '''
  Basically level order traversal.
  Iterative solution can be found # Ref: LevelOrderTraversal
  The below recursive solution can be expanded to "vertical" version.
  Essentially, iterative -> BFS, recursive -> DFS.
  '''
  def horizontally(self, root):
    arrays = []
    self._horizontal_helper(root, 0, arrays)
    return arrays

  def _horizontal_helper(self, root, level, arrays):
    if root is None:
      return None

    if len(arrays) == level:
      arrays.append([])

    currList = arrays[level]
    currList.append(root.val)
    self._horizontal_helper(root.left, level + 1, arrays)
    self._horizontal_helper(root.right, level + 1, arrays)
    return ;

  '''
  Sort of binary tree vertical traversal.

  Two hints:
  1. Use dictionary/map to store KV<column, List<TreeNode>> because column variable
  doesn't always increment.
  2. In a DFS solution, maintain a variable to store node depth because final result
  require top down vertical order but DFS doesn't go in that order.
  '''
  def vertically_recursively(self, root):
    map = {}
    self._vertical_recursive_helper(root, 0, 0, map)

    arrays = []
    for column in sorted(map):
      # sort by depth
      sorted_list_of_val_depth_pairs = sorted(map[column], key = lambda x: x[1])
      arrays.append([v for v, d in sorted_list_of_val_depth_pairs])

    return arrays

  def _vertical_recursive_helper(self, root, column, depth, map):
    if root is None:
      return None

    if column not in map:
      map[column] = []

    map[column].append((root.val, depth))
    self._vertical_recursive_helper(root.left, column - 1, depth + 1, map)
    self._vertical_recursive_helper(root.right, column + 1, depth + 1, map)
    return ;

  '''
  Vertical traversal iterative solution is easier to understand.
  '''
  def vertically_iteratively(self, root):
    if root is None:
      return []

    adict = {}
    currLevel = [(root, 0)]
    nextLevel = []
    while currLevel or nextLevel:
      if not currLevel:
        currLevel = nextLevel
        nextLevel = []

      node, column = currLevel.pop(0)
      if column not in adict:
        adict[column] = []

      adict[column].append(node.val)

      if node.left:
        nextLevel.append((node.left, column - 1))
      if node.right:
        nextLevel.append((node.right, column + 1))

    arrays = []
    for column in sorted(adict):
      arrays.append(adict[column])

    return arrays
```

## Longest Consecutive Sequence
```python
'''
From any parent node to any child node, find the length of the longest path
where node values are consecutive numbers, ascending.

Input:
   1
    \
     3
    / \
   2   4
        \
         5
Output:
3
'''
class LongestConsecutiveSequence:

  def solution(self, root):

    _, g, _ = self._helper(root)

    return g

  '''
  The helper function returns local_max, global_max, node_val of current node
  and its subtrees.
  '''
  def _helper(self, root):

    if root is None:
      return 0, 0, None

    ll, lg, lv = self._helper(root.left)
    rl, rg, rv = self._helper(root.right)

    if lv == root.val + 1: # update left_local if root.val is a consecutive number to last left_value
      ll += 1
    else: # else start counting the current root as the first of a new sequence
      ll = 1

    if rv == root.val + 1:
      rl += 1
    else:
      rl = 1

    lg = max(ll, lg) # update left_global with new left_local
    rg = max(rl, rg)

    return max(ll, rl), max(lg, rg), root.val

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
