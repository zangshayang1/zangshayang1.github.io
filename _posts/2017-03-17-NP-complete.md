---
layout: post
title: ICS260 - NP Complete
date: 2017-03-17 17:30:00 -0800
categories: study-notes
tag: algorithms
---

* content
{:toc}



Last Modified: 20170318

If an algorithm is efficient, it means it scales polynomially with the size of the input.

From a pragmatic perspective, NP-complete essentially means: it is computationally hard for all practical purpose although we can't prove it.  


### Decision Problems

Many problems can be "reduced" to yes-no __decision problems__.  

For example, optimization problem - Find the minimum number of colorings of an undirected graph (only connected vertices can share the same color). It can be reduced to - given k colors, can you color the entire graph? If yes, how about (k-1)? And so on...  

A __yes instance__ of such problem includes the input, graph and k, and the output: yes. A __no instance__ has the output being: no. The optimization problem can be solved by looking for the "yes instance" with the smallest k.

### Hamiltonian Cycle

Definition: If a graph has a simple cycle that visits every node, the graph has a hamiltonian cycle.  


### Polynomial Time Reducibility

If problem Y can be solved by performing polynomially many computational steps plus polynomially many calls to a function that solves problem X, we say that:  

$$ Y \leq_{p}X $$  

Namely, Y is polynomially reducible to X.   

Observation: If X can be solved in O(polynomial), Y can be solved in O(polynomial). If X cannot be solved in O(polynomial), Y neither.  

Deduction: If Y can be solved in O(polynomial), X can be solved in at most O(polynomial). If Y cannot be solved in O(polynomial), __X neither__ (because Y is polynomially __reducible__ to X).  

Namely, X is as hard as Y.  

### Independent Set and Vertex Cover

Given a graph G:  
__Independent Set(IS)__ is a set of vertices where no two vertices are connected.  
__Vertex Cover(VC)__ is a set of vertices whose edges cover all the edges in G.  

Typically, when we refer to IS and VC problems, we are talking about optimization problems: __maximize independent set and minimize vertex cover__. They can be "reduced" to __decision problems__:  
1. Does G have an IS of k vertices?  
2. Does G have an VC of k vertices?  

__The next:__ Given that 1 is a known NP-complete problem, __PROVE__ that 2 is an NP-complete problem.  

<img src="{{ '/styles/images/NP-complete/IS-VC-reduction.png' }}" width="100%" />


__Proof:__  

$$ IS \leq_pVC $$  

1. __From__ a general instance of IS as such: Does G(V, E) have an IS of k vertices? (It only takes O(polynomial) to check if the answer is "yes" or no.) __Construct__ a specific instance of VS as such: Does G'(V', E') have an VC of k' vertices, where G' = G (V' = V and E' = E) and k' = size(V') - k?  

2. __Show__ the construction takes O(polynomial), which is obvious in this case. (It also only takes O(polynomial) to check if the answer is "yes" or "no".)  

3. __Prove__ the IS instance is a "yes-instance" __iff__ the VC instance is a "yes-instance" by proving:  
+ if IS instance is a "yes-instance", VC instance is a "yes-instance".  
+ if VC instance is a "yes-instance", IS instance is a "yes-instance".  

__NOTE:__ essentially, it is trying to prove that IS is polynomially reducible to X in terms of "problem construction" and VC is as hard as IS in terms of "problem solution".  

_If G(V, E) has an independent set S of size k, then for every edge (u, v) in G: at most one of u and v is in S. In G' with the same set of vertices S', the above means: For every edge (u', v') in G', at least one of u' and v' is in (G' - S'). There we have a VC-yes-instance._  

_Vice versa._  

In fact:

$$ IS \leq_pVC $$  
$$ VC \leq_pIS $$  

### P, NP and NP-complete

__The class P__ identifies a class of problems solvable in O(polynomial).  

__The class NP__ identifies a class of problems that have a __"certifier"__ that can run in O(polynomial) and output "yes" or "no" to the corresponding decision problem. But finding a solution for the problem itself is computationally inefficient (not in O(polynomial)) or such a solution hasn't been discovered.  

_Of course, problems in P all have such "certifier" and thus all belongs to NP._  

__The class NP-complete__ identifies problems that:  
1. belongs to NP.  
2. every problem in NP is polynomially reducible to the problem.  

_For the problems that belongs to NP and not NP-complete, there is just not a proof yet._  



### SAT and 3SAT  

__SAT__ short for boolean satisfiability problem, says: given N boolean variables and a collection of clauses. Each clause consists of a number of such variables connected by __OR__. Find an boolean assignment for each variable such that all the clauses are True.  

__3SAT__ is the same problem with limited 3 boolean variables in every clause. Both of them are fundamental combinatorial search problems.  

__NOTE:__ In fact, they are computationally __hard__ problems in that: we have to make N decisions to assign each of them as True or False such that a __set__  of the constraints are satisfied. If the problem were to satisfy each of the constraints in isolation, that'd be easy.  


### Reduce 3SAT to IS  

__NOTE:__  
If we have X variables, how many clauses can we possibly construct without duplicates?  

$$ C_{x}^{3} $$

If we negate a variable and that adds one more clause, how many are there?  

$$ C_{2x}^{3} $$

Now __THINK:__ What is the maximum number of clauses that can be allowed so that a truth assignment to make all of them True is guaranteed?  

1. __From__ a general instance of 3SAT as such: if all of the k clauses (xyz) satisfiable? __Construct__ a specific instance of IS as such: Each variable is a vertex and each clause is an triangle graph made of 3 such vertices. Create __k__ such groups as there are k clauses. Add edges to connect every two vertices in the same group. Add edges between vertices if one is the negation of the other.  

2. __Show__ the construction takes O(polynomial), in fact it goes the same rate as the number of variables increases.

3. __Prove__ the 3SAT instance is a "yes-instance" __iff__ the IS instance is a "yes-instance" by proving:  

+ if 3SAT instance is a "yes-instance", IS instance is a "yes-instance".  

_if 3SAT instance is a yes-instance, each triangle in the graph must contain one vertex that's evaluated to be True. Let S be a set containing all such vertices and I claim S is an independent set because there is no edges between any two of them. If there is, they should be evaluated conflict with one being True and the other being False._

+ if IS instance is a "yes-instance", 3SAT instance is a "yes-instance".  

_if IS instance is a yes-instance, there must be an independent set S with a size of k and no two vertices in S are connected. Since there are k triangles in the graph and every two vertices in one triangle are connected, then the following must be True: Exactly one vertex came from each triangle. If such a set of vertices are evaluated as True, then we found an truth assignment so that the 3SAT instance is True._  


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
