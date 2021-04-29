---
layout: post
title: DataStructure - Binary Tree
date: 2017-05-06 16:00:00 -0800
categories: study-notes
tag: data-structure
---

* content
{:toc}




## Binary Tree

__Review:__
1. The __depth__ of a node is the number of edges from the node to the tree's root node.
2. The __height__ of a node is the number of edges from the node to the farthest leave.

For a perfect binary tree with L levels:  

1. Number of nodes in total: $$ 2^{L} - 1 $$  
2. Depth: $$ 0 \leq d < L $$  
3. Number of nodes in at each depth: $$ 2^{d} $$  
4. Sum of depth of each node: $$ \sum_{0 \leq d < L} d \cdot 2^{d} $$  
5. Height: $$ 0 \leq h < L $$  
6. Sum of height of each node: $$ \sum_{0 \leq d < L} h \cdot 2^{L-1-h} $$  

## Binary Search Tree

It is generated when applying binary search on a list of random numbers. They are represented by __internal__ nodes while gaps are represented by __external__ nodes.  

Inorder traversal through a binary search tree recovers all the numbers in sorted order.  

### AVL Tree (balanced by height)

### Red-black Tree (balanced by color)

### Weak AVL tree

#### ranking rules
1. Each node has an integer ranking >= 0
2. External node ranks 0
3. If a node has two external children, it ranks 1
4. For any other node, its rank = children's rank + (1 or 2)

__Claims:__  
1. For a node with rank r, it has at most $$ 2^{r} $$ external nodes. (obvious)
2. For a node with rank r, it has at least $$ 2^{\lceil \frac{r}{2} \rceil} $$ external nodes. (only equal when r == 1)
3. height <= rank = $$ 2 \log_{2}{n} $$

#### Operations

__Insert X__
1. find the external gap where X should be
2. make it an internal node and give it rank 1.
3. walk up to root and adjust each rank to max(old_rank, new_child_rank + 1)
4. If stuck, due to incompatible situations, do local __reconstruction__. (talk more later)

__Reconstruction:__
Ranking adjustment gets __stucked__ when:
1. Node X has two external children and ranks 1.
2. One external child becomes an internal node and ranks 1.
3. X has to go from 1 to 2, which might be equal to X's parent.
4. However, X's parent __cannot__ go from 2 to 3 because X's parent has another child ranking 0.  

Local reconstruction means:  
1. Lower the rank of X's parent by 1 and let it be the other child of X.


### Weighted Binary Tree

The __problem__ is: For some applications, perfectly balanced binary search tree doesn't provide best performance.  

__Intuitively,__ these applications don't have uniformly distributed queries. For example, they might query the same "range" represented by an external node over and over again. If that's the case, interestingly:  
1. On a perfectly balanced BST with n nodes, it takes $$ O(\log{n}) $$ average time.  
2. On a [degenerate tree](https://en.wikipedia.org/wiki/Binary_tree#Types_of_binary_trees), it takes O(1) average time.  

$$ O(1) = \sum_{i} \frac{i}{2^{i}} $$  

__Solution:__  
If we have prior knowledge about the query distribution, we should use __weighted binary tree__,  
with weight of each key being the probability of the key being accessed,  
so that:  
time to access that key: (w is the weight)  

$$ O(\log{\frac{\sum_{k} w_{k}}{w_{k}}}) $$

Expected time to access each key: (p is the probability)  

$$ E = O(\sum p_{i} \log{\frac{1}{p_{i}}}) $$

__What if we don't have such prior knowledge about query distribution?__

### Splay Tree

__Intuitively,__ splay tree puts whatever key that is more frequently accessed upfront so it's viewed as a general purpose binary search tree that can speedup non-uniform access.  

If we say __"Splay"__ a node, it just means putting the whole tree rooted at this node to root (certainly not balanced any more).

#### Operations

__Search, Insert__ operations are the same as what we do to normal binary search tree, except that we always splay the target node at the end of these operations.  

__Delete(X)__ is involved with __"split"__ and __"concatenate"__.  
1. search on X, splay it.
2. cut off the root and return the two children as two new roots.
3. find the smaller root and splay the last node in that tree.
4. concatenate the other root as the right child of the just splayed root.

__Splay__:  

<img src="{{ '/styles/images/binaryTree/splay.jpg' }}" width="100%" />  

#### Performance

[Static optimality](https://en.wikipedia.org/wiki/Splay_tree#Performance_theorems)


<!--
buffer
buffer
buffer
buffer
buffer
buffer
buffer
buffer
buffer
buffer
buffer
buffer
buffer
buffer
buffer
buffer
buffer
buffer
buffer
buffer
buffer
buffer
buffer
buffer
-->
