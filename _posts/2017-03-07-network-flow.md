---
layout: post
title: ICS260 - Network Flow Problem
date: 2017-03-07 21:30:00 -0800
categories: study-notes
tag: algorithms
---

* content
{:toc}




## Define Maximum Network Flow Problem

__Network Flow:__ A directed graph G = (V, E) with the following properties:  
1. each edge has a non-negative integer value denoting its capacity.
2. there is a source node with outgoing edges __only__ and a terminal(sink) node with incoming edges __only__.
3. every node is an incident on at least one edge.  

__s-t flow:__ A function that assigns an integer number to each edge so that:  
1. __capacity condition:__ 0 <= number <= edge capacity
2. __conservation condition:__ every node except s and t having: sum of numbers on incoming edges equal to the sum of numbers on outgoing edges

__the value of a s-t flow__ is the sum of numbers on the outgoing edges of source node, also equivalent to the sum of numbers on the incoming edges of sink node.

__max network flow problem:__ Given a network flow, find the maximum flow value of the network.  

## Ford-Fulkerson Algorithm

__high level description:__  
Set flow(e) = 0 for all the edges in given graph.  
While there is a __s-t augment path__ in the current __residual graph__:   
Augment flow(e) for all edges along the path by:
+ increase the flow on forward edges
+ decrease the flow on backward edges

__residual graph:__ A graph generated based on the original network flow graph and the flow function with the following properties:
1. Denoted by Gf, containing all the vertices in the original network flow graph G.
2. For each edge e = (u, v) in G, Gf contains (u, v) with capacity = (capacity(e) in G - flow(e)).
3. For each edge e = (u, v) in G, Gf contains (v, u) with capacity = flow(e).

__s-t augment path:__ A simple path from s to t. It is used to increment flow by some value X where X is the minimum capacity in Gf of all the edges in the augment path.

__Note:__ this algorithm must terminate after at most C iterations, where C is the sum of each edge's capacity. It is because after each iteration, if an augment path can be found, the flow must increment by at least 1 while there are at most C such iterations.

## Define Minimum-Cut Problem

__s-t cut:__ two sets of vertices A and B with the following properties, G is a network flow:

$$ s \in A $$

$$ t \in B $$

$$ A \cup B = G(V) $$

$$ A \cap B = \emptyset $$

__cut capacity:__ the sum of capacities of edges going out of A.

__max-flow min-cut theorem:__ the value of the maximum flow is equal to the capacity of the minimum cut.

__proof:__
1. Given G, the value of any flow <= the capacity of any cut, because flow(e) <= capacity(e).
2. Suppose the max-flow is found and Gf has no augmenting path. Let A be a set of all nodes reachable from s in Gf. B = G(V) - A. Now the maximum flow value equals to the capacity of the cut (A, B).

__Algorithm to find minimum cut:__

Run Ford-Fulkerson Algorithm until no more augmenting path can be found in Gf.
Starting from s, move forward along all unsaturated edges as well as backward along all edges with positive capacity. Let A be the set of all nodes reachable from s and B = G(V) - A. Min-cut found.

## Analysis of Max Flow Problem

Given a network flow graph G(V, E) with capacities C(e), the biggest iterations is (m is the number of edges, n is the number of vertices):

$$ \sum_{i}^{m} C(e_{i}) $$

If we hold m and n constant, the number of network flow representations can be exponentially growing as we grow C, which means the worst case running time of F-F Algorithm is exponential without specifying how to choose augmenting path.

__The optimization is:__
Always choosing the shortest augmenting path __(F-F with BFS)__ brings us strongly polynomial running time:

$$ O(mn) $$


#### Blood Supply and Demand Model

__Problem description:__ Consider blood types: A, B, O and AB.
 1. O can be applied to any patient.
 2. A can be applied to patient of A or AB.
 3. B can be applied to patient of B or AB.
 4. AB can be applied to patient of AB only.  

Now if you have certain supplies of each type of blood as well as demands for each type of blood. Design an algorithm to help you determine if the supplies meet the demands.  

__Solution:__  
Let supplies be:  

$$ S_{A},\; S_{B},\; S_{O},\; S_{AB} $$  

Let demands be:  

$$ D_{A},\; D_{B},\; D_{O},\; D_{AB} $$  

1. Create a node for each type of blood in supplies as well as each type of blood in demands.
2. Create a source node s and a terminal node t.
3. Draw edges from s to each supply node with the amount of supply being the capacity. Draw edges to t from each demand node with the amount of demand being the capacity. Draw edges from each supply node to each demand node with capacity being infinite.  

Now compute the max-flow. If and only if all the edges going to __t__ is saturated, the supplies can meet the demands.  


## Other Mathematical Applications

1. __Maximum Cardinality Bipartite Matching__

2. __Maximum Set of Edge-Disjoint Paths between Two Vetices__

3. __Edge Connectivity in an Undirected Graph__

4. __Circulation with Demands__

5. __Circulation with Demands and Low Bounds__

6. __Minimum Cost of Flow__



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
