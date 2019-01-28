---
layout: post
title: ICS260 - Greedy Algorithm on Graph
date: 2017-02-08 23:20:00 -0800
categories: Study-Notes
tag: algorithms
---

* content
{:toc}



Last Modified: 20190126

__Greedy Algorithm: Make decision based on some local optima and hoping it can achieve global optima in the end.__  

---

#### Interval Scheduling Problem

Input: A set of jobs with start and end time.  

Output: Maximum number of jobs in a __sequence__ so that there is no overlap.  

Algorithm: Earliest Finish Time First O(nlogn). _You goal is to maximize the number of non-overlapping jobs within the time frame._  

---

#### Interval Coloring Problem

Input: A set of jobs with start and end time.  

Output: Minimum number of colors needed to label __all__ the jobs so that there is no overlap between same-color jobs.  

Algorithm: Earliest Start Time First O(nlogn). _All the jobs need to be labeled sooner or later._

NOTE:  
1. Sort by start time as well as __end__ time - O(nlogn) _So it only takes O(1) to find the next earliest finishing job, its actual finishing time and assigned color. Every time, you only need to compare with the next earliest finishing job. Otherwise, this algorithm takes O(n^2)._  
2. __Take/Add__ next available color from a LinkedList - O(1)  
3. Assign the next available color to a job when it overlaps with all previously scheduled jobs.  
4. Assign the color ready for reuse to a job when the current job starts later than the finished one.  

_Why not using earliest finishing time?_  
_Intuitively, earliest starting time first strategy allows you to start earlier, which saves time that would've wasted if you use earliest finishing time first strategy. Even the former doesn't guarantee you earlier ending time in any case, that's fine. Just assign the next available color to the next job. It is inevitable that you do them in parallel._

---

#### Minimize Lateness Problem

Input: A set of jobs with required processing time and deadline.  

Output: A sequence of jobs so that the lateness is minimized.  

For example: If the finish time of the last job exceeds its deadline by 10, lateness += 10.  

Algorithm: Earliest Deadline First O(nlogn).  

---

## Greedy Algorithm to Solve Graph Problems

---

#### Single Source Shortest Path Problem: Dijkstra's Algorithm

__Input:__ G(V, E) with non-negative costs on edges.  

__Output:__ The shortest path from s to every other node in G (Naturally including target node if asked).  

__Idea behind this algorithm:__  
Let's say you are looking for the shortest path from starting node s to target node t. You've come to node t.parent p and its distance to t is 5. There is also a path of distance 2 from p to some other node n. Would it be possible that n will lead you to t with distance less than 3 so that Taking path p->n->x->t is actually shorter than p->t? What do you do to memorize the distance of existing path p->t and at the same time keep exploring p's children node? The key of the algorithm is __to keep increasing the reachability of the tree from s at the next lowest cost__ till we figure out the shortest path from s to t and any other node.   

``` python
def dijkstra(s, t, graph):
  # @ s: start_node
  # @ t: target_node
  # @ graph: matrix representation with connectivity and distances info between nodes

  # Distance info storage
  D = [INF for _ in range(len(graph))]
  D[s] = 0

  # Parent info storage
  P = [-1 for _ in range(len(graph))]
  # An array with the length equal to the number of nodes is enough to store
  # the shortest path from source to target
  
  # Visit info storage
  V = [False for _ in range(len(graph))]
  # It could be an array or a hashset
  
  while True:
    # find the next minimum distance to expand reachability
    # where the node has not been visited
    idx = None
    minDist = INF
    for i in range(len(D)):
      if i not in V and D[i] < minDist:
        idx = i
        minDist = D[i]
    
    # stopping point
    if idx is None:
      break
    
    # add to Visited set
    V.add(idx)
    
    # update D and P
    neighbors = graph[idx]
    for i in range(len(neighbors)):
      # Note: graph[i][i] = INF, no self pointing edge
      if i in V or neighbors[i] >= D[i]: 
        continue
      # update the distance info from s to i
      D[i] = D[i] + neighbors[i]
      P[i] = idx
  
  return D, P
```

<img src="{{ '/styles/images/greedy-algo-on-graph/DijkstraDemo.gif' }}" width="50%" />

__Improvement:__  
As an improvement of the above algorithm, we can use PriorityQueue to store distance info. It takes O(1) to find the next minimum distance from a PriorityQueue and O(logn) to maintain the data structure. So the overall complexity will be reduced from O(n*n) to O(nlogn).  

```python
def dijkstra(s, t, graph):
  # @ s: start_node
  # @ t: target_node
  # @ graph: matrix representation with connectivity and distances info between nodes

  # Distance info storage
  D = PriorityQueue<distance, node>
  D.push(0, s)

  # Parent info storage
  P = [-1 for _ in range(len(graph))]
  # An array with the length equal to the number of nodes is enough to store
  # the shortest path from source to target
  
  # Visit info storage
  V = [False for _ in range(len(graph))]
  # It could be an array or a hashset
  
  # Stopping criteria
  while !D.isEmpty(): 
    # find the next minimum distance to expand reachability
    # where the node has not been visited
    dist, idx = D.popMin()
    if idx in V:
      continue
    
    # add to Visited set
    V.add(idx)
    
    # update D and P
    neighbors = graph[idx]
    for i in range(len(neighbors)):
      # Note: graph[i][i] = INF, no self pointing edge
      if i in V or neighbors[i] == INF: 
        continue
      # update the distance info from s to i
      D.push(neighbors[i], i)
      P[i] = idx
  
  return D, P
```

NOTE:  
1. To find the shortest path from s to t, start from t and follow the parent pointer.  
2. The complexity is O((m + n)logn)
3. Use Fibonacci heap can further decrease the complexity.  

---

#### Minimum Spanning Tree - Prim's Algorithm and Kruskal's Algorithm

__Definition:__  
1. Output T is a tree.  
2. All the nodes in input G can be found in output T.  

__The above Dijkstra's algorithm can be applied to MST problem, which became the following Prim's Algorithm. The improved Dijkstra's algorithm became the foundation of the following Kruskal's Algorithm to solve MST problem faster.__

__Prim's Algorithm:__  
Start with a seed node s. Repeatedly grow T by adding the cheapest edge connecting (some node in T) and (some node in G but not yet in T). Prim's Algorithm differs from Dijkstra's Algorithm in that: a node's distance is defined as how far it is from the start node in Dijkstra's Algorithm while distance is define as how far it is from any node already in T to any node not yet in T in Prim's Algorithm because they have different focuses on the output. Dijkstra's Algorithm was used to find the shortest path from s to t while Prim's Algorithm was used to find the MST, even though they are related.


``` python
def prims(s, graph):
  # @ s: start_node
  # @ graph: matrix representation with connectivity and distances info between nodes

  # THE ONLY DIFFERENCE BETWEEN DIJKSTRA AND PRIMS
  # is what the following distance info storage represents in this algorithm
  D = [INF for _ in range(len(graph))]
  D[s] = 0

  # Parent info storage
  P = [-1 for _ in range(len(graph))]
  # An array with the length equal to the number of nodes is enough to store
  # the shortest path from source to target
  
  # Visit info storage
  V = [False for _ in range(len(graph))]
  # It could be an array or a hashset
  
  while True:
    # find the next minimum distance to expand reachability
    # where the node has not been visited
    idx = None
    minDist = INF
    for i in range(len(D)):
      if i not in V and D[i] < minDist:
        idx = i
        minDist = D[i]
    
    # stopping point
    if idx is None:
      break
    
    # add to Visited set
    V.add(idx)
    
    # update D and P
    neighbors = graph[idx]
    for i in range(len(neighbors)):
      # Note: graph[i][i] = INF, no self pointing edge
      if i in V or neighbors[i] >= D[i]: 
        continue
      # THE ONLY DIFFERENCE BETWEEN DIJKSTRA AND PRIMS
      # is the following update function
      D[i] = neighbors[i]
      P[i] = idx
  
  return D, P
```

---


__Kruskal’s Algorithm__  
It does two things:  
1. Initialize a graph T with every node from G but no edge at all.  
2. Connect nodes in T with edges in G sorted in order of increasing weight without __causing any circle__.  

__Note:__
1. Skipping edges that cause any circle eventually gives us a tree. A minimum spanning tree.
2. To efficiently detect cycle, a Union-Find data structure comes in handy. If two nodes are connected already, they share the same root in this UF data structure. If so, any edge that could cause a circle can be detected and avoided in O(logn) time. The most important take away is this Union-Find data structure.  

``` python
"""
Define a Union-Find Data structure. Also known as Disjoint Set.
"""
class Vertex():
    def __init__(self, val):
        self.val = val
        
class DisjointSet():
    def __init__(self):
        self.roots = [] # store actual vertex
        self.parents = [] # store parent idx
        self.ranks = [] # store vertex rank

    def add(self, v):
        self.roots.append(Vertex(v))
        self.parents.append(-1)
        self.ranks.append(0)
        
    def unite(self, x, y):
        rx = self.find(x)
        ry = self.find(y)
        if self.ranks[rx] > self.ranks[ry]:
            self.parents[ry] = rx
        elif self.ranks[rx] < self.ranks[ry]:
            self.parents[rx] = ry
        else:
            self.parents[ry] = rx
            self.ranks[rx] += 1
    
    def find(self, x):
        for i in range(len(self.roots)):
            if self.roots[i].val == x:
                if self.parents[i] == -1:
                    return i
                p = self.parents[i]
                return self.find(self.roots[p].val)

"""
kruskal's algorithm using the Union-Find() data structure defined as above.
"""
class Edge():
    def __init__(self, source, target, weight):
        self.source = source
        self.target = target
        self.weight = weight
        
def kruskals(graph, s):
  # @ s: start_node
  # @ graph: matrix representation with connectivity and distances info between nodes

  # Distance info storage
  D = PriorityQueue<Edge<s, t, w>>
  for i in range(len(graph)):
    for j in range(i + 1, len(graph[i])):
      D.push(Edge(i, j, graph[i][j]))
  
  # Connectivity/circle info storage
  DJS = DisjointSet()
  for i in range(len(graph)):
    DJS.add(i)
  
  # Parent info storage
  P = [-1 for _ in range(len(graph))]
  
  # stopping criteria
  while !D.isEmpty():
    # find the next minimum edge 
    edge = D.popMin()
    # check for circlebility
    if DJS.find(edge.s) == DJS.find(edge.t):
      continue
    # store parent info
    P[t] = s
    # connect them in DJS
    DJS.unite(edge.s, edge.t)
  # return MST representation
  return P
```

__NOTE:__ For a second, I was wondering why we can't use a "visited" array to detect cycle like what we did in Prim's. There are ultimate differences between these two algorithms, the union-find data structure is necessary for this algorithm to work.  

__A very natural use case of Kruskal's Algorithm: K-clustering:__  

How to find the K-clustering for a bunch of points. Between every two clusters is the distance maximized.  
1. Now we see each point as a vertice.  
2. Run Kruskal's Algorithm k steps before it is naturally terminated.  
3. After it is terminated, a MST is found. But k steps before it, a k-clustering is found.  

#### All Pair Shortest Path Problem With Negative Edges

__Input:__  
A graph with negative weights. There might be circles. 

__Output:__
1. If there is no negative circles, output a shortest distance matrix, D[i][j] indicating the shortest distance between node i and node j.
2. If there is a negative circle, output nothing.

__Floyd Algorithm__:
The idea behind the algorithm is simple yet powerful. It takes O(n^3) time complexity and O(n^3) space complexity, with the latter being reduced to O(n^2) using dynamic programming.  
For any two nodes, i and j:
1. If i == j, D[i][j] = 0.
2. If i != j, D[i][j] = min(D[i][j], D[i][k] + D[k][j]) for k is connected with i and j.

```python
def floyd(G):
    # G[i][j] is the distance from Vi to Vj
    V = len(G)
    for k in range(V):
        for i in range(V):
            if G[i][k] == INF: continue
            for j in range(V):
                if G[k][j] == INF: continue
                G[i][j] = min(G[i][j], G[i][k] + G[k][j])
    return G
```







<!-- ###########################################################################
######### Cushion ##############################################################
################################################################################
################################################################################
################################################################################
################################################################################
################################################################################
################################################################################
################################################################################
################################################################################
################################################################################
################################################################################
################################################################################
################################################################################
################################################################################
################################################################################
################################################################################
################################################################################
################################################################################
################################################################################
################################################################################
################################################################################
################################################################################
################################################################################
################################################################################
################################################################################
################################################################################
################################################################################
################################################################################
################################################################################
################################################################################
################################################################################
################################################################################
################################################################################
-->
