---
layout: post
title: DataStructure - UnionFind
date: 2017-06-10 13:00:00 -0700
categories: study-notes
tag: data-structure
---
* content
{:toc}



### Union-find Data Structure

__Classic example of usage - finding the minimum spanning tree using Kruskal's Algorithm.__  

With union-find data structure, we can:  
1. test which group this input node belongs to - find(x)
2. union two groups of nodes - union(A, B)

Two improvements to make the data structure efficient:  
1. Represent a group in the form of a tree, augmented with a size field (indicating the size of the subtree rooted at the current node) and make the smaller one be one of the children of the bigger one during union(A, B). _This ensures that the depth of the tree A coming out of union(A, B) will grow only logarithmically rather than linearly._
2. Every find(x) operation will splay node x to be one of the direct children of the root.

__Implementation Details:__
Refer to [Kruskal's Algorithm](https://zangshayang1.github.io/study-notes/2017/02/08/greedy-algorithm/#minimum-spanning-tree---prims-algorithm-and-kruskals-algorithm).

__Time Bound:__  

Perform M find()/union() operations on an union-find data structure of the above implementation storing N elements, gives the following time complexity (nearly constant):  

$$ O(min\;i\;|\;A(i, \lfloor \frac{M}{N} \rfloor) > N ) $$

Where A is [Ackermann function](https://en.wikipedia.org/wiki/Ackermann_function).  
