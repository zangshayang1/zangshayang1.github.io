---
layout: post
title: ICS260 - Asymptotical Analysis
date: 2017-02-07 19:39:00 -0800
categories: study-notes
tag: algorithms
---

* content
{:toc}



__Asymptotical Upper Bound__  

If:  

$$ There\;\exists\; constants\;C\;and\;N,\;\; For\;\forall n \geq N,\;T(n) \leq C g(n) $$

We have:  

$$ T(n) = O(g(n)) $$

__Asymptotical Lower Bound__  

If:  

$$ There\;\exists\; constants\;C\;and\;N,\;\; For\;\forall n \geq N,\;T(n) \geq C g(n) $$

We have:  

$$ T(n) = \Omega(g(n)) $$

__Growth Rate Comparison__  

$$ log(n) < n^{a}\;(0 < a < 1) < n\;(linear) = 2^{log(n)} < n^{a}\;(a > 1\;polynomial) < 2^{n}\;(exponential) < n!\;(factorial) < n^{n} $$
