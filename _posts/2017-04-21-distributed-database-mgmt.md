---
layout: post
title: ICS223 Distributed Database Management
date: 2017-04-21 1:00:00 -0700
categories: Study-Notes
tag: distributed
---
* content
{:toc}



#### DBMS

__Data Model__ provides a level of abstraction between how data is physically stored versus how it is used by applications. For example, table -> records(row) -> field(columns)

__Key Component of DBMS:__  
1. Record manager
2. Index manager
3. Query optimizer
4. Query processor

__Transaction__ is consistent and failure resilient execution of database applications.  

__Transaction Implementation Keypoints:__
1. Concurrency control protocol to ensure serializability
* locking protocol
* timestamp protocol
* validation mechanism
2. Recovery algorithm
* shadow paging
* log-based: write-ahead log / redo log / undo log  
3. Atomic commit protocol (in case a transaction access distributed resources)
* two phase commit; three phase commit...

__Recoverability and Durability__  
1. If a transaction aborts before commit, data item should remain unchanged.  
* _no-steal policy_ says in-memory changes are not allowed to go to disk before commit.  
* _undo logging_ says a log of old value is generated in memory before the value gets updated in memory.   
2. If a transaction loses after commit in case of system failure, data item should reflect changes.  
* _force policy_ says in-memory changes are flushed to disk before commit.  
* _redo log_ says a log of new value for every WRITE operation is generated and flushed to disk before commit so we can replay it in case of failure.  


__Locking management__ can be implemented as a separate process, which maintains a lock table (most likely in-memory hashTable) indexed by the name of each data item. A lock on each data item includes lock state, lock type, request waiting queue, etc.  

__Transaction Processor Components__  

<img src="{{ '/styles/images/distributed-database-mgmt/transactionProcessorComponents.png' }}" width="100%" />


#### DBMS -> Transactions -> Concurrency -> Serializability

__Why DBMS?__
DBMS supports concurrent uses without jeopardizing data __consistency__ and tolerance of system failure so that a user sees a transaction as a consistent and __failure-resistent__ execution of applications.

__DB operations to transactions__
DB operations include READ/WRITE. Each operation is assumed atomic. A __transaction__ is a sequence of DB operations plus __transaction specific operations__, such as BEGIN/END and COMMIT/ABORT. From the perspective of concurrency control mechanism, every record is a DB is an object associated with domain values.  A __state__ of the DBMS is a mapping relation of each object with its domain values.

__Why serialized execution of transactions is desired?__
Concurrent executions of transactions lead to issues, for example:  
* AccountX has $1000 and AccountY has $2000.
* App1 transfer $100 from AccountX t AccountY.
* Concurrently, there is a chance that app2 sums both accounts to $3100.

__Why want concurrency?__
1. Minimize average response time(some large transactions take long time and other transactions will be waiting in an entirely serialized system).
2. Minimize CPU idle time so that system throughput can be maximized.

__Middle ground: Forgo serialized execution of transactions to increase concurrency while following serialized schedules to ensure serializability.__
1. A schedule is an interleaving of operations of multiple transactions.
2. Such a schedule is __serializable__ if it is __equivalent__ to some sequential execution of each transaction.

__How to define equivalence?__
1. Final state equivalence -> Final state serializability (FSR)
2. View equivalence -> View serializability: respective transactions in two different schedules READ the same data values
3. Conflict equivalence -> Conflict serializability:


#### Final State Serializability

__Graph characterization of final state equivalence to test FSR__

For some transaction T on objects x and y:  

Augment it with:
1. $$ T_{0} $$ consisting of WRITE only, one WRITE for each object (initial state).
2. $$ T_{e} $$ consisting of READ only, one READ for each object (final state).

Make each R/W operation a vertex.  

Draw an edge from READ to WRITE if they are in the same transaction, no matter which data item they are on.  

_OR_  

Draw an edge from WRITE to READ if they are in different transactions and target the same data item.

Delete vertices and edges that don't lead to the final state.  

__Claim:__ Two schedules are final-state equivalent if they produce the same graph after the above processes, which take O(n) to run, where n is the number of the operations. One schedule is final-state serializable if it produces the same graph as the serialized execution of transactions does.  

#### View Serializability(VSR)

__Claim:__ Two schedules are view equivalent if they produce the same graph before "deleting" anything during the same process as in "FSR". Testing view equivalence is NP-hard.  

__Compare:__ VSR ensures that each transaction's view of the DB is the same as they are in a serialized execution. FSR only ensures that transactions that have same impact on the final state of the DB.

#### Conflict Serializability(CSR)

__Operations are conflicting if:__
1. they are on the same object, such as R(x) and W(x)
2. they belong to different transactions
3. at least one of them is WRITE.

__Construct Serializability Graph - SG(V, E)__
1. Make a vertex for every transaction.
2. Draw an edge from $$ T_{x} $$ to $$ T_{y} $$ if:  
* $$ T_{x} $$ contains an operation $$ O_{x} $$
* $$ T_{y} $$ contains an operation $$ O_{y} $$
* $$ O_{x} $$ occurs earlier than $$ O_{y} $$ in schedule S.
* $$ O_{x} $$ and $$ O_{y} $$ are conflicting.

__Claim:__ A schedule is CSR iff the SG(V, E) constructed as above is acyclic. Testing if a graph contains a cycle takes O(n^2) where n is the number of vertices.

__VSR vs. CSR (quote from Wikipedia, complete and precise):__  
_View-serializability of a schedule is defined by equivalence to a serial schedule (no overlapping transactions) with the same transactions, such that respective transactions in the two schedules read and write the same data values ("view" the same data values)._  

_Conflict-serializability is defined by equivalence to a serial schedule (no overlapping transactions) with the same transactions, such that both schedules have the same sets of respective chronologically ordered pairs of conflicting operations (same precedence relations of respective conflicting operations)._  

#### Recoverability, Cascadeless, Strictness  

Define:__Reads from:__ (needs to be verified) in a schedule where T1 and T2 go concurrently, T1 __reads from__ T2 if a READ(x) in T1 occurs after WRITE(x) in T2 in this schedule.  

Might be helpful to confirm these standards in Wikipedia:[Recoverability, Cascadeless, Strictness](https://en.wikipedia.org/wiki/Schedule_(computer_science)#Recoverable)

For a schedule S:

__Recoverability__ is ensured if $$ T_{x} $$ reads from $$ T_{y} $$, then $$ T_{y} $$ commits before $$ T_{x} $$ commits.

__Cascadeless or ACA(avoid cascading aborts)__ is ensured if $$ T_{x} $$ reads from $$ T_{y} $$, then $$ T_{y} $$ commits before $$ T_{x} $$ reads.

__Strictness__ is ensured if $$ T_{x} $$ reads from $$ T_{y} $$ or if $$ T_{x} $$ overwrites $$ T_{y} $$, $$ T_{y} $$ commits before it does so.  

* Commercial DBMS ensures strictness.

#### Replications in Distributed System

__Why replica?__ Improve availability and performance.

To ensure distributed system consistency, our goal is __one copy serializability__, which means the distributed DBMS behaves like a serial processor of transactions on a one-copy database. Essentially, it is __synchronized replication__, which comes with huge cost.

Now we talk about __asynchronous replication__: One replica is updated immediately and others later.  

One important concept first - __1-copy serializable(1SR):__
1. Concurrent transactions don't create circles in SG.  
2. For each transaction T involved, READ(x) in T reads from the most recent updated copy of x, for every x in __ReadSet__ of T.  
* _ReadSet: a set of data item that a transaction reads._  

__Strategies:__  
1. Primary copy
2. Multi-master
3. Quorum consensus

__Primary copy: (1SR, not partition tolerable)__
1. Clients READ from secondaries and WRITE through primary node only.
2. Updates flow from primary copy to secondaries asynchronously.
3. WRITE is not permitted during partition.

__Multi-master: (non-1SR, partition tolerable)__
1. Clients READ/WRITE to different masters directly.
2. Updates between multiple masters are designed to be __commutative or conflicting updates are merged__.

__Quorum consensus: (1SR, partition tolerable)__ Each READ/WRITE operation has to obtain a certain amount of votes (more than half of the total votes) before execution.

__Commutative downstream updates:__
Thomas's Write Rule:
* Assign timestamp to each of clients' WRITE operation.
* Apply downstream updates to every replica with the latest WRITE.

#### Isolation Levels

Refer to [wiki isolation](https://en.wikipedia.org/wiki/Isolation_(database_systems)) for detailed isolation levels and read phenomena.  

1. __Serializable:__ Hold R/W lock till the end of the transaction. Hold range lock in READ operation to avoid _phantom read_.

2. __Repeatable Reads:__ Hold R/W lock till the end of the transaction.

3. __Read Committed:__ Hold W lock till the end of the transaction. R lock is released as soon as READ operation is done. _Non-repeatable read_ may occur.

4. __Read Uncommited:__ Only W lock is managed. _Dirty read_ may occur.

#### Snapshot Isolation

Before executing transaction T:
1. take a snapshot of committed data
2. execute READ/WRITE operation on the snapshot
3. updates are not visible to other concurrent transactions
4. __(first committer wins)__ commit WRITE only if there is no other transaction has preceding WRITE on the same data
5. otherwise, abort and rollback the current transaction

Benefit with SI:
1. High READ availability.
2. No uncommitted READ; No Non-repeatable READ.

Problem with SI:
1. It doesn't ensure Serializability. (In a serialized schedule, one transaction sees the updates from the other.)
2. [write skew anomaly](https://en.wikipedia.org/wiki/Snapshot_isolation).  

__Inconsistency is often not a showstopper, but a mere inconvenience. All we need to do is making sure that application logic doesn't lead to inconsistency or inconsistency can be worked around on application layers.__


#### Two phase RW locking

__2PL RW locking__ combines two-phase locking and share-exclusive locking.  

__2PL__ has two phases:  
1. expanding phase, new locks on the desired data item can be acquired but no locks can be released till the transaction terminates.  
2. shrinking phase, acquired locks can be released after the transaction terminates and no new locks can be acquired.  


__RW locking__ has
1. read (shared) locks that multiple clients can hold at the same time.  
2. write (exclusive) locks that only one client can hold while no one holds read lock on the same data item.  

2PL RW locking ensures __serializability__ but it is also subject to __irrecoverability, deadlock and starvation__.

__Irrecoverability__
picture reference http://www.edugrabs.com/2-phase-locking/  
<img src="{{ '/styles/images/distributed-database-mgmt/irrecoverability.png' }}" width="50%" />


__Deadlock__
picture reference http://www.edugrabs.com/2-phase-locking/  
<img src="{{ '/styles/images/distributed-database-mgmt/deadlock.png' }}" width="100%" />

__Starvation__
picture reference http://www.edugrabs.com/2-phase-locking/  
<img src="{{ '/styles/images/distributed-database-mgmt/starvation.png' }}" width="100%" />

__Relations between CSR, VSR and variations of 2PL lockings__  

* picture reference http://www.edugrabs.com/2-phase-locking/

<img src="{{ '/styles/images/distributed-database-mgmt/coverability-csr-vsr-2pl.png' }}" width="100%" />

---

#### Scheduling Protocol

__Scheduler__ takes write-ahead log as input and outputs a serializable (CSR) schedule to be applied to DB.  

__2PL Scheduling Protocol__ schedules operations under 2PL rules so basically:  
* For non-conflicting operations, their order doesn't matter.
* For conflicting operations, not only should they obey 2PL rules, the order that they came in should also be preserved.  

__Proof of correctness - schedules generated by 2PL protocol are CSR:__  
_Assume 2PL scheduling produced such a schedule that, if we construct a serialize graph (SG) for it, it will have a cycle._
_Let's say, T1, T2, ..., Tn formed such a cycle, which also means: T1 and T2 must contain conflicting operations, T2 and T3 must contain conflicting operations...and so on till Tn and T1 must contain conflicting operations._
_According to 2PL rules, T2 would not be able to acquire all the lock until T1 released all the lock. Same thing goes. T1 will not acquire all the locks until Tn released all the lock._ -> __Contradiction__  

__Distributed 2PL Scheduling__ says: transaction T doesn't release locks at any site it executes until it has acquired all the locks it needs at every site it executes on. It is implied that scheduler at each site synchronizes each other.

---

__Timestamp Ordering Protocol (MVCC timestamp ordering):__
1. Every time a transaction T comes in, it request a TimeStamp(T), which is unique and monotonically increasing.
2. Every data item x is associated with a last_read_timestamp and a last_write_timestamp. They are updated every time a READ/WRITE operation is executed.  
3. The protocal says: READ(x) operation in transaction T will be executed iff TimeStamp(T) >= x.last_write_timestamp. WRITE(x) operation in transaction T will be executed iff TimeStamp(T) >= x.last_read_timestamp.
* The "write" part is also known as [Thomas's Rule](https://en.wikipedia.org/wiki/Thomas_write_rule)

__Proof of Correctness:__
_Assume Timestamp Ordering Protocol produced such a schedule that, if we construct a serialize graph (SG) for it, it will have a cycle._
_Let's say, T1, T2, ..., Tn formed such a cycle. According to Timestamp Ordering, TS(1) < TS(2) < ... < TS(n) < TS(1)._ -> __Contradiction__

---

__Serializable Graph Test (SGT) Protocol:__
1. For every incoming operation O from some transaction T, draw an edge from all other transactions whose operations __precede O and conflict with O__, to T. (Create a vertex for T if it doesn't already exist)
2. If it creates a cycle, reject the operation O and abort the transaction T. Else, add the edge.
3. For those transactions that are done and don't have incoming edges, we can remove them. Else, keep it even if it committed already.

[wikipedia Reference](https://en.wikipedia.org/wiki/Precedence_graph)

__Variations:__

A __conservative__ variation tends to delay the operation to prevent the transaction from being aborted. An __aggressive__ variation tends to go on scheduling the operation at the risk of aborting the transaction later. They can be tailored according to prior knowledge about the application needs for better performance.  

Examples:  
1. Conservative 2PL:
* try to acquire all the locks for the entire transaction
* if succeed, execute it and release.
* else, release all the locks it acquired immediately and wait. (Different from 2PL, conservative 2PL never aborts a transaction)
2. Aggressive 2PL:
* a transaction T will be executed iff all the active transactions don't have any operations conflict with T

__Optimistic Concurrency Control__ includes 3 phases:
1. Read phase. Snapshot the last committed data items. Apply READ/WRITE operations as needed.
2. Validation phase. Decides if committing these updates will violate serializability.
3. Write phase. Commits updates to database.

During validation phase, for any two concurrent transactions - {Ti, Tj} with timestamp(Ti) < timestamp(Tj), serializability is guaranteed if any of the following is satisfied:  
1. Ti completes [write phase] before Tj starts [read phase].
2. $$ WriteSet(T_{i}) \cap ReadSet(T_{j}) = \emptyset $$ and Ti completes [write phase] before Tj starts [write phase].
3. $$ WriteSet(T_{i}) \cap WriteSet(T_{j}) = \emptyset $$ and $$ WriteSet(T_{i}) \cap ReadSet(T_{j}) = \emptyset $$ and Ti completes [write phase] before Tj starts [read phase].


#### Deadlock, Update Mode Locking and Live lock

To counter __deadlock__, you can:  
1. Prevent them by improving locking protocol.
2. Detect them, normally by _timeout_ and _wait-for graph_, and resolve them by aborting one or more transactions that cause the deadlock.  

__Detect__ deadlock by _timeouts_ or _wait-for graph_.  

__wait-for graph(WFG):__ is constructed by __draw edge from Ti to Tj if Ti waits Tj__ for a lock. A cycle in the graph corresponds to a deadlock. Scheduler __periodically__ checks for a cycle. Once a cycle is detected, the scheduler chooses a _victim_ transaction to abort using _victim selection strategy._  
* Detecting deadlock by constructing _wait-for graph_ is inefficient, slow and 99% of cases don't yield any deadlock. However, because deadlock wouldn't go away until some victim is aborted, we can still run the algorithm periodically (every 2min maybe) to reduce the overhead.  

__Distributed WFG__  
1. Path pushing algorithm
2. Global deadlock detection

__Path Pushing:__
1. If a site found a cycle in its local WFG, it selects a victim to be aborted.  
2. Else, the site lists all the transactions that are not in any cycle. For each transaction, the site traces back to where it might be blocked, _let's say Si traces T back to Sj_, and Si sends the path involving T to Sj. _This step is like broadcasting._  
3. Sj updates its local WFG and start iterating from step1.
* Note: this algorithm is deficient in that it is possible that two transactions that formed a cycle are both selected as victims at two sites. To improve, we should prioritize sites.  

The above note leads to __Global Deadlock Detection:__ Only one site will be the coordinator:
1. Receiving paths sent from other sites
2. Constructing a global WFG locally
3. Choosing victims to be aborted.  


__Prevent__ deadlock by:
1. using __conservative 2PL RW locking__, which acquires all the resources needed in a transaction before it starts to executing anything. But it leads to problems such as less resources utilization and less concurrency.  
* On the other side, __strict 2PL RW locking__ will execute whatever the current resources allow in a transaction, under the condition that all the write locks will be not be released until the transaction commits. Of course, strict 2PL ensures serializability but doesn't prevent deadlock. More strictly, __rigorous 2PL RW locking (strongly strict 2PL)__ will hold all the R/W locks till the transaction commits.
2. Using __update mode locking__, (especially made to prevent deadlock due to _conversion_), where the __only__ U lock is acquired before a transaction can WRITE so it will become the only transaction that is eligible to request for X lock.
* while one transaction holds the U lock, other transaction can still hold S lock. When no transaction is holding S lock, the one with U lock can safely acquire X lock and drop U lock.
* [deadlock due to conversion] refers to: Both T1 and T2 hold S lock and request for X lock. Both of them are waiting for the other to drop S lock but neither will do because none of them get X lock.
3. Using __non-blocking 2PL__, which says if the transaction cannot immediately get the requested lock, then it is aborted.
4. Using __priority-based scheduling__, which decides if the transaction __waits__ for the lock, or __dies__, or __kills__ other transaction based on their priorities associated. Often, an aborted transaction will restart with a higher priority, which causes __live lock__.  

Priority-based scheduling might cause __live lock__ due to cyclic killing and restarting.  
To counter __live lock__, use __timestamp-based scheduling__:  
1. Set monotonically increased timestamp to transactions and use them inversely as priorities. So higher-priority transaction comes in first.
2. Once set, priorities don't change.
3. Follow one of these two strategies:
* wait-or-die: IF ts(Ti) < ts(Tj), then Ti waits; ELSE Ti dies. (Tj holds the lock.)

__What's the frequency of deadlock?__
Let:  
1. the number of data item in our system be N.
2. the number of concurrently executed transactions be R.
3. the average size of each transaction be r.

Assume:
1. Rr << N (# of concurrent operations << # of total data items)  
2. Rr/2 is WRITE operation

Probability of an operation O in a transaction T is waiting for some lock, held by some other operation: $$ pw = \frac{Rr}{2N} $$  

Probability of none operation in a transaction T waits is $$ pnw = (1-pw)^{r} $$  

Probability of an transaction happens to wait in its lifetime: $$ PW(T) = 1 - pnw $$, which is approximated by $$ r \cdot pw = {\frac{Rr^{2}}{2N}} $$  

Probability of such x many transactions happen to wait at the same time is: $$ PW^{x} $$  
* As you can see from here, deadlock of length 2 is most likely to occur, with a probability of $$ PW^{2} = {\frac{R^{2} r^{4}}{4N^{2}}} $$  

#### Hot Spot Problem

__Scenario.1 - Thumbsup and Down__
Let's say a site maintains a number and allows users to concurrently vote for or against something indicated by the number. If we use 2PL in this case, it will not only be slow but also lose count when two votes coming in simultaneously.  

_Solution:_ Design compatible I(ncrement) and D(ecrement) locks instead of using exclusive X lock. To ensure atomicity, you still need to serialize the operations. One possible implementation would be: update the local copy for users to view immediate result and apply write-ahead log later to the primary copy every a few seconds.  

__Scenario. 2 - flight seats reservation__
If only 19 seats are left while two persons are trying to book 10 seats each, what now?  

_Solution:_ Escrow locking - for each data item with a higher/lower limit, maintain a range of values indicating their availability.  

<img src="{{ '/styles/images/cs223-dist-db-mgmt/escrow1.JPG' }}" width="50%" />

<img src="{{ '/styles/images/cs223-dist-db-mgmt/escrow2.JPG' }}" width="100%" />

#### System Performance

As the number of transactions goes higher, the throughput of the DBMS doesn't proportional increase because:  
1. Resource contention leads to thrashing, e.g. constantly transferring pages in and out of memory.  
2. Data contention leads to thrashing, e.g. constant blocking (basic 2PL) or restarting (non-blocking 2PL or optimistic scheduling).  
* with increasing amount of resources, non-blocking 2PL outperforms basic 2PL because the former restarts failed transactions, better use resources and further delay data content.  

* What does multi-programing level mean?

#### Structured Accessing

__Question arises from 2PL:__
1. 2PL can be characterized by expanding phase and shrinking phase.
2. 2PL is sufficient to ensure CSR, but is it necessary? Any way to relax it?  

__Structured Accessing - Path Protocol:__  
2PL is not necessary if we superimpose some structure on how the data can be accessed, for example: linearly. That is: Data items are ordered sequentially, $$ \{ x_{0}, x_{1}, x_{2}, ..., x_{n} \} $$. For an operation in transaction T, it can acquire the lock on $$ x_{i} $$ iff it has the lock on $$ x_{i-1} $$ unless i = 0.  
* If two transactions ever operate on a same item, whichever transaction gets the lock first gets all the locks first.  

__Generalization of Path Protocol - Tree Protocol:__
As you would expect, all the data items are structured as a tree, you need to acquire the lock on parent before you can acquire the lock on children, except the root item.  

__B-tree Original Development:__

READ operation  
```
S-lock the root
current_node = root
while (current_node is not a leaf)
  S-lock the appropriate child_node
  Release lock on current_node
  current_node = child_node
```

WRITE operation  
```
// U lock is compatible with S lock so READ is not affected during searching phase until eventual updating phase.
// U lock is incompatible with X lock so NO OTHER WRITE can be performed once it starts.

U-lock the root
current_node = root
while (current_node is not leaf)
  U-lock the appropriate child_node
  current_node = child_node
  if (current_node is safe) // meaning no merge/split
    release locks on all ancestors
  else
    convert all U locks to X locks top down
    // No READ is allowed in the case that
    // this WRITE would cause structural change
```


__Pi-tree Significant Improvement:__ # TODO


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
-->
