---
layout: post
title: ICS260 - Stable Marriage
date: 2017-02-07 16:20:00 -0800
categories: study-notes
tag: algorithms
---

* content
{:toc}



## Stable Marriage

#### Problem: How to compute a stable perfect matching?

Key Definitions:  

* matching?  
_Given M = {m1, m2, ... mn} and W = {w1, w2, ..., wn}, a matching is a subset of M x W (cartesian product), S, such that each element of M and each element of W appears in_ __AT MOST__ _one pair in S._

* perfect match?  
_In a perfect matching S, each element of M and each element of W appear in_ __EXACTLY ONE__ _pair in S._

* instability? stable perfect match?  
_In a matching S, instability means there exists a pair (m, w) that's not in S but each of m and w prefers the other to their current partners in S_

---

#### G-S Algorithm

```
While there’s a free man // indicating he has not proposed to every woman
  m = such man // non-deterministic
  w = the highest ranking women in m’s preference list to whom m has not yet proposed
  if w is free
    (m, w) becomes engaged
  else  // w is currently engaged to some other man m'
    if w prefers m to m'
      (m, w) becomes engaged
      m' becomes free
    else
      (m', w) remain unchanged
      m remain free
    endif
  endif
endwhile
```

---

#### Claims:  

__The sequence of partners to whom a man proposes gets worse; the sequence of partners to whom a woman is engaged gets better.__  

__The algorithm terminates after at most n^2 proposals.__  

__If a man m is free, there is some woman to whom he has not yet proposed.__  
_prove by contradiction:  
suppose that m is free and he has proposed to all women.  
then all women are engaged to some men.  
since |M| == |W|, these means all men are engaged._  

__They matching computed by the G-S algorithm is a perfect matching.__  
_prove by contradiction:  
suppose that the algorithm terminates without a perfect matching, meaning there is a free man m.  
according to the code, m must have proposed to every woman.  
according to the claim 3, it is impossible._  

__The matching computed by the G-S algorithm is a stable matching.__  
_prove by contradiction:  
suppose that there is an instability, meaning that the matching contains two pairs (m, w) and (m', w') such that m prefers w' to w and w' prefers m to m'.  
the last time m proposed, it was to w.  
if m proposed to w' some earlier time, w' rejected m and she prefers m' to m, contradiction.  
if m proposed to w' some later time, m doesn't prefer w' to w._  

---

#### Determinacy Analysis in G-S Algorithm

Q: The non-deterministic G-S algorithm however produces deterministic result, given any particular input. Why?

A: The G-S algorithm always pairs each man with his best valid partner.  

__claim: Stable matching problem has more than 1 possible outcomes; Different execution of G-S algorithm leads to different outcomes. However, if w is a valid partner of m, then (m, w) will appear at least once in certain valid outcome.__

_proof by contradiction:  
suppose at least one proposal from m to a valid partner w is rejected (either immediately or as a result of a broken engagement.), consider that the_ __first rejection__ _ever occurred during this execution E of G-S algorithm: m proposes to a valid partner w and w immediately rejects m because she prefers her current match m'.  
Because w is a valid partner of m, there exists some other execution E' of G-S algorithm where (m, w) are paired and m' is paired with some other woman w', which is stable._  
__However__, _when w rejected m for m' during the execution of E, that was the first rejection ever occurred. That means, at that point, m' hasn't been rejected. Clearly, m' is proposing at decreasing order and w' is a valid partner of m'. So m' must prefer w to w'.  
Since m' prefers w to w' and w prefers m' to m. The outcome from the execution of E' can't be stable, which is a contradiction.  
Therefore, our original proposal is false._    

---

#### Complexity and Implementation


Input: two 2D matrices (man, preference_list) and (woman, preference_list)  

Output: a stable perfect matching S.  

The algorithm terminates at most n^2 proposals, looping over n^2 times. If every operation in this loop is O(1), the overall complexity will be O(n^2).  

Specifically:  

1. find a free man - keep only free man on a linked list so it takes O(1) to access the next free man;  

+ It takes O(n) to convert an array into a LinkedList;  

2. for a given man m, find his next highest ranking woman w to whom m hasn't proposed - maintain a 1D array h, where h[m] == w; It takes O(n) to convert one man's preference_list into a LinkedList started with the highest ranking woman. Then it takes O(n^2) to convert all the men's preference_list to LinkedLists maintained within a 1D array.  

3. For a given woman w, find out if she's currently engaged or not - maintain a 1D array c, where c[w] == null if w is not engaged and c[w] == m' if w is engaged with m'.  

4. For a given woman w engaged with m', to whom m proposed, determine whether w prefers m to m' - this is tricky to do in O(1) because normally you'd need to reference to w's preference_list and find how m ranks to m'. __Solution: from woman's preference array, you need to create a "ranking" array r where r[w, m] = m's ranking in w's preference_list. This can be done in O(n^2).__  


__NOTE: I think the above thinking process is quite valuable because O(n^2) is the ultimate goal for this algorithm and an algorithm is no more than a bunch of logical operations put together to solve a problem. The optimization of it can be thought as designing data structures to make it O(1) for each operation.__  


---

##### Exercise1


Same problem. Under the circumstances that there are k (k < n) good men and k (k < n) women, (n - k) bad men and (n - k) bad women. Every man ranks good women higher than bad women. Every woman ranks good men higher than bad men. Show that in every stable matching, every good man is married to a good woman.  

_Prove:_  
_Suppose there is a good man m married to a bad woman w', then there must be a good woman w married to a bad man m'. Thus, (m, w') and (m', w)._  
_Since good ones are always preferred, we have m prefers w to w', w prefers m to m'. This leads to an instability._  
_This makes a contradiction._  

---

##### Exercise2

Generalize the problem by explicitly stating that certain men-women pairs are forbidden (listed in set F). Under these circumstances, a stable matching can be established if:  
* there's no usual kind of instability (above).  
* there's no such pair (m', w) and a single m, who has higher rank than m' in w's preference_list and also (m, w) $$ \notin $$ F.  
* there's no such pair (m, w') and a single w, who has higher rank than w' in m's preference_list and also (m, w) $$ \notin $$ F.  
* there's no single m and single w whose pair (m, w) $$ \notin $$ F.  

_G-S algorithm can be adapted to work out this case:_  

_the only change is an added restriction on "while condition"_  


```
while there is free man m who hasn't proposed to every woman w for which (m, w) not in F
  w = the highest ranking women in m's preference_list to which m hasn't proposed yet and not forbidden to propose.
  if w is free:
    (m, w) became engaged
  else if w prefers m to his current match m':
    (m, w) became engaged and m' became a free man
  else:
    (m', w) stays the same and m is still a free man
  endif
endwhile
return the set S of engaged pairs
```

_prove:_  
_If there's a usual kind of instability, please see_ __Claim No.5__.  
_Suppose there is a pair (m', w) and a single m who is ranked higher than m' in w's preference_list and (m, w) is not forbidden. m must've  proposed to w and w rejected m for m', which is contradictory._  
_Suppose there is a pair (m, w') and a single w who is ranked higher than w' in m's preference_list and (m, w) is not forbidden. Same as above._  
_Suppose there are a single m and a single w and (m, w) is not forbidden, then m must've proposed to w and w can't still be single._  

---

##### Exercise3

Further generalize the SMP (stable matching problem) to RAP (resident assigning problem). RAP states that: given a list of hospitals that accept graduating students as residents. Each hospital has some number of slots available whereas the number of students exceeds the capacity. Each hospital has a preference_list of students and each student has a preference_list of hospitals. Design an algorithm to find a stable match where there is no instability:  
* instability_1: student s is assigned to hospital h while s' is assigned to no hospital. But h prefers s' to s.  
* instability_2: student s is assigned to hospital h while s' is assigned to h'. But h prefers s' to s and s' prefers h to h'.  

_Analysis:  
In a typical stable matching problem, we have the same number of men and women. Now two differences between SMP and RAP: 1) hospitals typically have more than 1 slots available; 2) there is a surplus of students._ __We address 1) by creating the same number (say n) of slots as students. We address 2) by creating a NULL hospital. The slots in NULL hospital are ranked by every student as their least desired ones. All the slots are distributed across all the hospitals according to a 1D array SlotsOwner[n].__

_Set up preference_list:_  
_On one side, it's slotsPref[i][j], where slots of the same hospital have the same ranking of their preferred students._  
_One the other, it's studentsPref[i][j], where each student has their ranking of preferred slots. Those slots coming from preferred hospital have higher ranking than those coming from a less preferred hospital._  
_Finally all students prefer slots of non-NULL hospital to slots of NULL hospital._  

```
def RAP(hospPref, studentsPref, slotCount):

  (slotsPref, studentsPref, slotsOwner) = preprocess(hospPref, studentsPref, slotCount)

  match = GS(slotsPref, studentsPref)

  result = []

  for (slot, stud) in match:
    if slotsOwner[slot] == -1:
      continue
    result.append(slot, student)

  return result


def preprocess(hospPref, studentsPref, slotCount):

  # input:
  # @ studentsPref[n][m] indicates how n students rank m hospitals
  # @ hospPref[m][n] indicates how m hospitals rank n students
  # @ slotCount[m] indicates how many slots each hospital has

  # output:
  # @ slotsPref[n][n] indicates how n slots rank n students
  # @ studentsPrefSlots[n][n] indicates how n students rank n slots
  # @ slotsOwner[n] indicates which hospital each slot belongs to

  new slotsPref = int[n][n]
  new slotsOwner = int[n]
  slotTop = 0 # keep track of the top of the allocated slots
  for h in range(m):
    count = slotCount[h]
    for _ in range(count):
      slotsPref[slotTop] = hospPref[h]
      slotsOwner[slotTop] = h
      slotTop += 1
  while slotTop <= n: # set the remaining NULL
    slotsOwner[slotTop] = -1
    slotsPref[slotTop] = arbitrary permutation of range(n)
    slotTop += 1

  new hospMapslots = HashMap<int, List<int>>
  slotTop = 0
  for i in range(m):
    count = slotCount[i]
    new slots = int[count]
    for j in range(count):
      slots[j] = slotTop
      slotTop += 1
    hospMapslots[i] = slots

  new studentsPrefSlots = int[n][n]
  new frontline = int[n] # indicates where the next slot should go
  for i in range(n): # students
    for j in range(m): # ranking
      h = studentsPref[i][j] # which hospital at this rank?
      slots = hospMapslots[h] # which slots this hospital takes?
      for k in range(len(slots)):
        studentsPrefSlots[i][frontline[i] + k] = slots[k] # add one by one starting from the frontline
      frontline[i] += len(slots) # update frontline

  return (slotsPref, studentsPref, slotsOwner)

```

---

##### Exercise4

A variation of SMP, ship maintenance schedule problem. Safety rule: no two ships can be in the same port on the same day. Each ship has its own monthly schedule in terms of stopping at which port on which day of the month. Find an algorithm so that each ship can find a port to stop for maintenance for the rest of the month without violating the safety rule.  

__Analysis:__  
_There are n ships and n ports._  
_Eventually we want to find a set of (ship, port) pairs where there's no collision._  
_If ports accept ships that come early, it will be less flexible and more likely to collide later._  
_If ships decide on ports too late, it will be more likely that they miss the right port and thus collide.__  
_Therefore each port should prefer ships that arrive later than the earlier ones._  
_And each ship should prefer ports that are scheduled earlier than the later ones._  

__NOTE:__ the essence of this variation is the preference list is very implicit. While in RAP, the match of the number of slots and the number of students is very implicit.  

__Set up preference_list and run G-S algorithm to find a stable match:__  
_Each port ranks ships from latest to earliest.  
_Each ship ranks ports from earliest to latest._  

__Prove it is working:__  
_Suppose there is a collision where s' later arrived at p but (s, p) are already matched. s' ended up being matched with p'._    
_This indicates that:_  
1. _p ranked s' higher than s since s' arrived at p later than s._  
2. _s' ranked p higher than p' since s' arrived at p earlier than p'._  
_That makes an instability between (s, p) and (s', p')._  

---

##### Exercise5

Based on the classic stable matching problem, the question here is: Can a man or a woman be better off if he/she lies about their preferences?  

__Yes, please refer to the following example:__  

<!--
In the following example: site.baseurl is necessary for project-based Github page but my Github page is a repository-based, where there's no baseurl.
  <img src="{{ '/styles/images/zhifubao.PNG' | prepend: site.baseurl }}" alt="支付宝二维码付款给Freud" width="310" />
-->
<img src="{{ '/styles/images/stable-marriage/would-liar-be-better-off.png' }}" width="100%" />
