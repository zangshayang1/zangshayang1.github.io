---
layout: post
title: Zhangxin Java Core
date:   2021-06-20 15:00:00 -0700
categories: reference
tag: java
---

* content
{:toc}



Updated: 2021-06-20

# Collection

__Array__ is a "primitive" data structure. __Vector__ is a thread-safe version of array.

__List__
* LinkedList
* ArrayList (indexing + dynamic expansion)

__Map (Set)__
* HashMap (implementation = array + linkedlist (converted to red black tree when it's overloaded from depth and length))
* ConcurrentHashMap (thread-safe)
* TreeMap (red black tree)

# Red Black Tree

__Why we need Red Black Tree when there is AVL tree? AVL's self rebalancing algorithm is quite expensive.__

__AVL__
1. self-balancing binary search tree
2. maintains the invariant (via self rotation) which is:  
3. for any node in this tree, math.abs(the height of its left subtree - the height of its right subtree) <= 1

### 2-3-4 Tree

__2-3-4 Tree__
* a self-balancing data structure
* a B-tree of order 4. 

In a 2-3-4 Tree:
* A 2-node is a node that has one data element, it must have 2 children unless it is a leaf node
* A 3-node is a node that has two data elements, it must have 3 children unless it is a leaf node
* A 4-node is a node that has three data elements, it must have 4 children unless it is a leaf node

Note: within each node, data elements are sorted by its comparable key.

__How to map a 2-3-4 Tree to a RBT?__
* A 2-node turns into black node.
* A 3-node turns into either
  * a black node and a left red child
  * a black node and a right red child
* A 4-node turns into a black node with two red children.

__insert operation__ (JavaCore2: 30:00)

__rebalance__ (JavaCore2: 58:00)

__How to delete a target node in a BST?__ - JavaCore3 30:00
If the target node is a leaf node: Directly delete.  
If the target node has one child: Replace the target node with its child node.  
If the target node that has a left child and a right child? Either:  
1. Replace the target node with its predecessor and remove the predecessor (the node with the largest value found in target node left subtree).
2. Replace the target node with its successor and remove the successor (the node with the smallest value found in target node right subtree). Successor is used by TreeMap implementation. 


### RBT Implementation

What invariants does a RBT maintain? 
1. A node is either red or black
2. Root node must be black
3. A red node can't have a red parent or a red child
4. Starting from any node, all the unsuccessful search path (from the root node to a leaf node) must pass through the exact same number of black nodes.

Operations to maintain the invariants
1. left rotation
2. right rotation
3. switch color

(Red Black Tree Implementation)[https://github.com/zangshayang1/zhangxin/blob/master/JavaCore/src/main/com/zhangxin/javacore/rbtree/RBTree.java]

# Reflection

__JVM 3 stages: Compile -> Loading -> Runtime__

![]({{ '/styles/images/zhangxin-java-core/jvm-three-stages.png' | prepend: site.baseurl }})

__3 Stages during loading (load binary code of required classes into method area):__ 
* loading (byte stream io into method area)
* linking
    * verification: verify binary source code
    * preparation: init default value or static fields
    * resolution: resolve symbolic references
* initialization
    * init static fields and their values for each class instance (thread-safe)

![]({{ '/styles/images/zhangxin-java-core/loading-stage.png' | prepend: site.baseurl }})

### Reflection Details

[Dynamic Loading](https://github.com/zangshayang1/zhangxin/blob/master/JavaCore/src/main/com/zhangxin/javacore/reflection/DynamicLoading.java)

[Static Initialization](https://github.com/zangshayang1/zhangxin/blob/master/JavaCore/src/main/com/zhangxin/javacore/reflection/StaticInitialization.java)

[Class Basics](https://github.com/zangshayang1/zhangxin/blob/master/JavaCore/src/main/com/zhangxin/javacore/reflection/ClassBasics.java)

[Reflection APIs](https://github.com/zangshayang1/zhangxin/blob/master/JavaCore/src/main/com/zhangxin/javacore/reflection/ReflectionAPIs.java)

[Reflection Performance](https://github.com/zangshayang1/zhangxin/blob/master/JavaCore/src/main/com/zhangxin/javacore/reflection/ReflectionPerformance.java)

[Override Accessibility](https://github.com/zangshayang1/zhangxin/blob/master/JavaCore/src/main/com/zhangxin/javacore/reflection/Accessibility.java)

# Generics

[Generic Class](https://github.com/zangshayang1/zhangxin/blob/master/JavaCore/src/main/com/zhangxin/javacore/generics/Inheritance.java)

[Generic Method](https://github.com/zangshayang1/zhangxin/blob/master/JavaCore/src/main/com/zhangxin/javacore/generics/GenericMethod.java)

[Wildcard](https://github.com/zangshayang1/zhangxin/blob/master/JavaCore/src/main/com/zhangxin/javacore/generics/WildCard.java)

[Erasure](https://github.com/zangshayang1/zhangxin/blob/master/JavaCore/src/main/com/zhangxin/javacore/generics/Erasure.java)


# Annotation

[Annotation Basics](https://github.com/zangshayang1/zhangxin/blob/master/JavaCore/src/main/com/zhangxin/javacore/annotations/MyAnnotation.java)

[Four Meta Annotations](https://github.com/zangshayang1/zhangxin/blob/master/JavaCore/src/main/com/zhangxin/javacore/annotations/FourMetaAnnotation.java)

[Annotation Use With Reflect](https://github.com/zangshayang1/zhangxin/blob/master/JavaCore/src/main/com/zhangxin/javacore/annotations/UseWithReflection.java)

# Design Pattern