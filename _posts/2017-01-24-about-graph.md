---
layout: post
title: ICS260 - About Graph
date: 2017-02-08 16:20:00 -0800
categories: study-notes
tag: algorithms
---

* content
{:toc}



Last Modified: 20170319

## Definitions and Representation

---

#### Key Definitions

1. G = (V, E): a __graph__ is a pair of vertices and edges. Vertices are nodes and an edge is a pair of nodes e = (u, v).
2. __Directed__ graph imposes an order on each edge while __undirected__ graph doesn't.
3. An __undirected__ graph is connected if, for every pair of nodes (u, v), there exists a path from u to v.
4. A __directed__ graph is __strongly connected__ if, for every pair of nodes (u, v), there exist a path from u to v and a path from v to u.
* __Other relevant: Path, Simple Path, Cycle, Degree of a node...__

5. The __distance__ from u to v is the minimum number of edges in a u-v path.
6. A __free tree__ is an undirected connected graph that doesn't contain a cycle.
7. A __rooted tree__ is a tree with a root node r and all the edges are directed away from the root.
* __Other relevant: parent, child, ancestor/descendant, proper ancestor/descendant, leaf...__

---

#### Graph Representation

__Undirected Graph Representation__  

Given G = (V, E), n = # of nodes, m = # of edges.

1. __Adjacency Matrix:__ Array A[n, n] where A[u, v] = 1 if (u, v) is an edge of G, else 0.
* O(1) lookup time for an edge;
* O(n^2) space needed;
* O(n) time to find all edges on a given node.

2. __Adjacency List:__ Array A[n] where A[v] contains a list of all the neighbor nodes to v.
* O(n+m) space needed;
* O(1) time to find all edges on a given node;

__Directed Graph Representation__  

1. __Modified Adjacency Matrix:__ Array A[n] where A[v] contains two lists of incoming edges and outgoing edges.  

---

## Traversing Algorithm and Common Problems


#### BFS

```
Given a undirected connected graph G under Adjacency list representation:

def BFS(graph, start_node, target_value):

  if !start_node: return ;

  # init
  visited = HashMap<node, False>()
  q = Queue<node>()

  q.enqueue(start_node)
  while !q.isEmpty():
    curt_node = q.dequeue()
    if curt_node.val == target_value: return curt_node;

    visited[curt_node] = True
    neighbors = graph[curt_node]
    for node in neighbors:
      if visited[node]:
        continue
      else:
        q.enqueue(node)

  return Null;

```

---

#### DFS

```
Given a undirected connected graph G under Adjacency list representation:

def DFS(graph, start_node, target_value):

  if !start_node: return ;

  # init
  visited = HashMap<node, False>()
  s = Stack<node>()

  s.push(start_node)
  while !s.isEmpty():
    curt_node = s.pop()
    if curt_node.val == target_value: return curt_node

    visited[curt_node] = True
    neighbors = graph[curt_node]
    for node in neighbors:
      if visited[node]:
        continue
      else:
        s.push(node)

    return Null;
```

---

#### Existence, Reachability and Connectivity

__Existence problem: given a graph and a pair of nodes in the graph, (v, u), if there exists such a path from u to v?__

__Reachability problem: given a graph and a node u, find all nodes which there exists a path from u to.__

__Connectivity problem: Run BFS/DFS on an unvisited node u and label them as connected till there is no unvisited node left.__ _A set of "connected components" containing start_node was generated._   

---

#### BFS tree and DFS tree  

In a graph G, starting from a node u, run DFS and we got a DFS tree T that includes all the nodes in G. Now run BFS and we got the same tree. Prove: G = T. In other words, prove G doesn't contain any edge that is not in T.  

__NOTE:__ Typically, edges in a BFS/DFS tree must be in the original graph but edges in the original graph are not necessarily in the tree.  


_Analysis:_  
1. _What is a DFS tree and how it is built?_  
2. _What is a BFS tree and how it is built?_  
3. _As long as there is an edge (u, v) in G, u and v are_ __at most__ _1-layer away in a BFS tree, regardless of the start_node._  
4. _As long as there is an edge (u, v) in G, u and v are either 1-layer away OR one of them is the ancestor of the other in a DFS tree T. If (u, v) is not in T, however, one of them must be the ancestor of the other._  

Prove by contradiction:  
1. Suppose there is an edge (u, v) in G but not in T.  
2. Analysis_3 says abs(layer(u) - layer(v)) <= 1.  
3. Analysis_4 says abs(layer(u) - layer(v)) > 1 (check on the definition of ancestor if confused).   
4. Contradictory.  

---

#### Bipartite Problem  

_Run BFS on a start_node and label nodes on odd layers "blue" and even layers "red". Return True if there is no red-red edge or blue-blue edge, else false._

__NOTE- Bipartite definition:__ $$ G = X \bigcup Y \;\;and\;\; X \bigcap Y = \emptyset $$ where each edge in G has one end in X and the other in Y.  

```
def bipartite(graph, start_node):

  # sanity check
  if !start_node or !graph: return ;

  # init data structure
  this_level = Queue<node>()
  next_level = Queue<node>()
  visited = HashMap<node, False>()

  # init start point
  counter = 1
  start_node.color("blue")
  this_level.add(start_node)  

  # level by level traversal
  # color node when dumping them from next_level to this_level
  while !this_level.isEmpty():
    curt_node = this_level.dequeue()
    visited[curt_node] = True
    for neighbor_node in graph[curt_node]:
      if visited[neighbor_node]:
        continue
      next_level.add(neighbor_node)
    if this_level.isEmpty():
      if counter % 2 == 1: # currently it is on odd level, the next this_level should contain "red" nodes.
        this_level = [node.color("red") for node in next_level]
      else:
        this_level = [node.color("blue") for node in next_level]
      next_level.clear()
      counter += 1

  # check for red-red or blue-blue edge
  for node in graph:
    for neighbor_node in graph[node]:
      if node.color == neighbor_node.color:
        return False
  return True

```

---

#### Strong Connectivity on Directed Graph:  

__Define strong connectivity: For every pair of nodes (u, v) in G, there is a path from u to v and a path from v to u.__  

1. _Pick a node in Graph and check if it can reach to every other node in the same graph._  
2. _Starting from the same node, test on_ $$ G^{REV} $$ _(reverse all the edges) if it can still reach to every other node._    
3. _If both tests pass, G is strongly connected, else return False._  

The algorithm takes O(m + n).  

---

#### A directed acyclic graph has topological ordering  

Topological ordering is the numbering of the vertices so that each edge is directed from a vertex with lower number to one with higher number.  

```
1. Find a node v with no incoming edges. Assign the next order number to v.
2. Delete v and all its outgoing edges.
3. Repeat the above steps till no such node v exists.
4. If all nodes have been deleted, a topological ordering has been found, else, the graph contains cycles.
```

__NOTE:__  
1. A directed acyclic graph must have at least one vertex that has no incoming edges.  
2. If there are multiple vertices without any incoming edges, their orderings can be randomly assigned but no two vertices can be assigned with the same ordering.  
3. Algorithm application includes: look for the shortest path in an acyclic graph; job scheduling with dependencies.

```
Implementation Details: graph represented in Adjacency list.

def topological_ordering(graph):

  # init
  inDegrees = HashMap<node, int>()
  labels = HashMap<node, int>()
  zeroDegrees = Queue<node>()
  counter = 0

  # O(n + m)
  for start_node in graph:
    inDegrees[start_node] = 0
    for end_node in graph[start_node]:
      if end_node in inDegrees:
        inDegrees[end_node] += 1
      else:
        inDegrees[end_node] = 1

  # find the node without incoming edge to start
  for node in inDegrees:
    if inDegrees[node] == 0:
      zeroDegrees.enqueue(node)

  # labeling
  while !zeroDegrees.isEmpty():
    curt_node = zeroDegrees.dequeue()
    labels[curt_node] = counter
    counter += 1
    neighbors = graph[curt_node]
    for node in neighbors:
      inDegrees[node] -= 1
      if inDegrees[node] == 0:
        zeroDegrees.enqueue(node)

  # check if all the node got labeled
  for node in graph:
    if ! node in labels: return Null;

  return labels
```

---

#### Butterfly Distinguish Problem  

A student is provided with a bunch of butterfly specimens pairs. Each butterfly either is of type A or type B. Each pair was labeled with "same" if this student thinks they are the same type or "different" if the student thinks they are from different types. Design an algorithms to tell if the student's labeling is consistent or not.

1. _Create a node for each specimen._  
2. _Add one edge to connect the pair of nodes with “different” judgement._  
3. _Create a dummy node for each “same” judgement, representing a fake specimen different from both of them. Add an edge from each of them to the dummy node._  
4. _Start a bipartite algorithm from a dummy node._  
5. _If it can be two-colored, it is consistent. Otherwise, it is inconsistent._  

---

#### Strong Components Labeling  

__Strong Component: A strongly connected subgraph of G where any two nodes u and v are mutually reachable from each other.__  

NOTE: Mutual reachability is an equivalence relation. A equivalence class is a strong component.  

__Algorithm Sketch__

```
1. DFS traversing the graph and label each node with a timestamp when it leaves DFS.  
2. Pick an unvisited node from a list of nodes sorted by their leaving time at a descending order and run DFS with all the edges reversed.  
3. Trace all the nodes in 2 and they make a strong component.  
4. Repeat 2 and 3 till all nodes are visited.
```

__Implementation Details__

```
def strong_components(graph, start_node):
  # graph is a directed graph, represented with two lists of edges for each node
  # graph = {
            'n1': {'incoming_edges':[], 'outgoing_edges':['n2', 'n3']},
            'n2': {'incoming_edges':['n1', 'n4'], 'outgoing_edges':['n5']}
            ...
            }
  # Maintain a global HashMap<node, timestamp>()
  # to store the timestamp when DFS leaves each node.
  leaveTime = HashMap<node, int>()
  visited = HashMap<node, False>()
  stack = Stack<node>() # for DFS

  stack.push(start_node)
  timestamp = 0

  # iterative DFS
  while !stack.isEmpty():
    node = stack.pop()
    visited[node] = True
    timestamp += 1
    stack_length = len(stack)

    # this is the DFS enter time point for this node
    # meaning from now on as long as the stack is longer
    # than the current stack length
    # the later added nodes are all its child or descendant

    neighbors = graph[node]['outgoing_edges']
    for next_node in neighbors:
      if visited[next_node]:
        continue
      stack.push(next_node)
      timestamp += 1

    if len(stack) > stack_length:
      continue # keep going on this DFS route
    else:
      timestamp += 1 # create leaving time point
      leaveTime[node] = timestamp

  # Prepare for reverse DFS traversal
  nodes_in_descent = [k for k in sorted(leaveTime.items(), key = lambda x:x [1])]
  visited = dict((k, False) for k in visited)
  # global storage of strong components found
  strong_components = []

  for node in nodes_in_descent:
    if visited[node]:
      continue # skill those already visited

    # init for DFS
    stack.push(node)
    # store a set of a list of nodes that make a strong component
    strong_component = []

    # iterative DFS
    while !stack.isEmpty():
      node = stack.pop()
      strong_component.append(node)
      visited[node] = True
      for next_node in graph[node]['incoming_edges']:
        if visited[next_node]:
          continue
        stack.push(next_node)

    strong_components.append(strong_component)

  return strong_components

```




---


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
