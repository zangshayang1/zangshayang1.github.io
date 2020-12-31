---
layout: post
title:  Algorithms - Graph
date:   2020-11-16 21:30:00 -0700
categories: study-notes
tag: algorithms
---

* content
{:toc}


Last Update: 2020-11-16

# Text Book

## Prim's On MST
```python
class PrimsOnMST:
  '''
  MST: Minimum spanning tree.

  Use AdjacencyMatrix to represent a graph G in a MST problem.

  This implementation of Prim's is in O(V^2) complexicity.
  '''
  def adjacencyMatrix(self, G):
    nov = len(G) # number of vertices
    V = [False for _ in range(nov)] # mark vertices that are reached in the MST
    P = [-1 for _ in range(nov)] # mark parent vertices that are in the MST

    # The single most important note here:
    # When i is a node out of V, D[i] is the lowest cost of the edge that points
    # from a node in V to a node out of V.
    # When i is a node in V, D[i] is the cost of the edge that connects the rest
    # of the MST to i.
    D = [Constant.INT_MAX for _ in range(nov)]

    D[0] = 0
    while True:

      min_idx = -1
      min_val = Constant.INT_MAX
      for i in range(nov):
        if !V[i] and D[i] < min_val:
          min_idx = i
          min_val = D[i]

      if min_idx == -1
        # if no node satisfy !V[i], break while as MST has been found
        # if no node satisfy D[i] < min_val, break while as MST doesn't exist
        break

      s = min_idx
      V[s] = True

      for t in range(nov):
        if G[s][t] = Constant.INT_MAX:
          continue

        if G[s][t] < D[t]:
          D[t] = G[s][t]
          P[t] = s

    # If D still contains INT_MAX, then G doesn't have a MST
    # Otherwise, sum(D) is the total cost and P indicates how the tree looks like
    return D, P
```

## Dijkstra's On SSSP
```python
class DijkstrasOnSSSP:

  '''
  SSSP: Single source shortest path to all nodes in a graph.

  Use AdjacencyList to represent a graph G in a SSSP problem.

  This implementation of Dijkstras gives (E + V)logV complexicity.
  '''
  def adjacencyList(self, s, G):
    nov = len(G) # number of vertices
    V = [False for _ in range(nov)] # mark vertices that are reached in the SSSP
    P = [-1 for _ in range(nov)] # mark the parent vertices that are in the SSSP

    # The single most important note here:
    # D[i] is the lowest cost from source node s to target node i.
    # Check Ref: # PrimsOnMST for definition differences.
    D = [Constant.INT_MAX for _ in range(nov)]

    # AdjacencyList s -> [(t1, w1), (t2, w2), ...]
    pq = MinPriorityQueue(lambda x : x[1])
    # init
    pq.push((s, 0))
    D[s] = 0

    while True:
      s, w = pq.pop() # pop() V times -> VlogV
      V[s] = True

      if sum(V) == nov: # visited every node in the graph
        break

      for t, w in G[s]:
        if V[t]:
          continue
        if D[s] + w < D[t]: # classic Dijkstra's formula
          D[t] = D[s] + w
          P[t] = s
          pq.push((t, D[t])) # push E times -> ElogV

    return sum(D)
```

## Kruskal's On MST
```python
class KruskalsOnMST:
  '''
  Given E -> List<Edge> # Ref: Edge class in Resource
  Given total number of nodes N in the graph, node represented by 1...n.

  Return total cost of the MST

  This implementation of Kruskal's gives O(ElogE) complexicity.
  '''
  def basedOnEdges(self, E, N):

    E = sorted(E, lambda x : x.w)

    uf = UnionFind(N)
    for e in E:
      uf.add(e.s)
      uf.add(e.t)

    cost = 0
    for e in E:
      if uf.find(e.s) != uf.find(e.t):
        tf.union(e.s, e.t)
        cost += e.w

    return cost
```

## Floyd's On APSP
```python
'''
APSP: All pairs shortest path problem.

Use AdjacencyMatrix to represent a graph in APSP problem.

Complexity: O(V^3)

Strength:
  1. Floyd's algorithm still works with negative weight edges.
  2. When there is negative cycle in a graph, the shortest path can be infinitely small.
  Floyd's algorithm can be used to detect if there is a negative cycle in a graph.

Algorithm Description:

For any pair of vertices in a graph, without a third vertex,
their initial distance is D_0[i][j] = w if there is an edge between them,
and D_0[i][j] = INF if there is no such an edge.

If consider a third vertex 1, their shortest distance is
D_1[i][j] = min(D_0[i][j], D_0[i][k] + D_0[k][j])

If consider a fourth vertex 2, their shortest distance is
D_2[i][j] = min(D_1[i][j], D_1[i][k] + D_1[k][j])

After considering all the vertices, D_N is the shortest path matrix between all pairs.
'''
class FloydsOnAPSP:
  def solution(self, G, N):

    D = [[Constant.INT_MAX for _ in range(N)] for _ in range(N)]

    for i in range(N):
      D[i][i] = 0

    for k in range(N):
      for i in range(N):
        for j in range(N):
          if D[i][k] == Constant.INT_MAX or D[k][j] == Constant.INT_MAX:
            continue
          D[i][j] = min(D[i][j], D[i][k] + D[k][j])

    for i in range(N):
      if D[i][i] < 0:
        return -1 # Negative edge cycle exists

    return D
```

# Practice

## Min Cost To Repair Connected Graph
```python
'''
Given a partially connected graph represented by List<Edge> E1
and all the edges that are available but currently disconnected represented by E2.

Given total number of nodes N in the graph, node represented by 1...n.

Return the min cost to repair(fully connect) the graph. If not repairable, return -1.
'''
class MinCostToRepairConnectedGraph:
  def solution(self, N, E1, E2):

    uf = UnionFind(N)
    for i in range(N):
      uf.add(i)

    completeness = 1
    for e in E1:
      uf.union(e.s, e.t)
      completeness += 1

    cost = 0
    E2 = sorted(E2, lambda x : x.w)
    for e in E2:
      if uf.find(e.s) == uf.find(e.t):
        continue
      uf.union(e.s, e.t)
      cost += e.w
      completeness += 1
      if completeness == N:
        return cost

    # not able to repair the graph before running out of egdes
    return -1
```

## Make Network Fully Connected
```python
'''
Given 1, 2 ... N computers and some of them are connected with each other.
A connection between 1, 2 is represented by [1, 2] (a weightless edge).

Given a list of such connections.

Return the number of operations needed to fully connect all the computers.
An operation include disconnecting an existing edge and use it to connect another
pair of computers.

When there is not enough cables, return -1.
'''
class MakeNetworkConnected:
  '''
  Two observations are keys to solve this problem:

  1. To connect N computers, there must be at least N-1 cables.
  2. The number of operations needed is the number of connected components - 1.
  '''
  def solution(self, N, E):
    # Sanity check
    if len(E) < N - 1:
      return -1

    # Create a graph
    G = {}
    for i in range(N):
      G[i] = []
    for e in E:
      G[e.s].append(e.t)
      G[e.t].append(e.s)

    counter = 0
    V = [False for _ in range(N)]
    for i in range(N):
      counter += self._dfs(G, i, V)

    return counter - 1

  '''
  Given a graph G, a start node i and a visited array

  When the start node is NOT visited, traverse through the graph,
  mark all the connected vertices as visited and return 1.

  When the start node is visited, return 0.
  '''
  def _dfs(self, G, i, V):
    if V[i]:
      return 0

    V[i] = True
    for j in G[i]:
      self._dfs(G, j, V)

    return 1
```

# Resource
```python
class Constant:

  INT_MAX = 2 ** 31 - 1

class Edge:
  def __init__(self, s, t, w):
    self.s = s
    self.t = t
    self.w = w

'''
The following implementation of UnionFind data structure is limited to be used
in graph algorithms when n vertices are represented by int values 0, 1, 2... n-1.
'''
class UnionFind:
  def __init__(self, size):
    self.parent = [-1 for _ in range(size)]
    self.rank = [0 for _ in range(size)]

  def add(self, v):
    self.parent[v] = v

  def find(self, v):
    if self.parent[v] == v:
      return v

    self.parent[v] = find(self.parent[v])
    return self.parent[v]

  def union(self, x, y):
    xr, yr = self.find(x), self.find(y)
    if xr == yr: return ;

    if self.rank[xr] > self.rank[yr]:
      self.parent[yr] = xr
    elif self.rank[xr] < self.rank[yr]:
      self.parent[xr] = yr
    else:
      self.parent[yr] = xr
      self.rank[xr] += 1

    return ;
```
