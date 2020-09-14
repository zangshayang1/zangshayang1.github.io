---
layout: post
title: Facebook Cassandra Design
date: 2017-04-28 17:00 -0700
categories: paper-summary
tag: distributed
---
* content
{:toc}



## Facebook Cassandra Design
__Summary from paper - Cassandra - A Decentralized Structured Storage System.__

This paper goes through the data model and system design behind Cassandra to the granularity  that I found very informative and not too detailed to grasp. Also, along with the paper about Google’s Spanner, it makes the whole picture about scalable distributed database design more and more clear.

__Data Model__  
From an abstract level, __Cassandra table__ is a distributed mapping of string keys to a highly structured object, which from a perspective of traditional “table” can be viewed as row and columns respectively. This way, every operation under a single row key is atomic no matter how many columns involved. Also, the design to group columns enables sorting of data items within the same group, which comes in handy in a lot of applications in Facebook.

__System Design__  
Cassandra partitions data across the cluster using __consistent hashing__. The main advantage of it is it makes the system easily scalable. But it also causes unbalanced load distribution due to asymmetric topology. Replication is critical to achieve high availability in distributed database. In case of random assignment of replicas, __Cassandra integrates Zookeeper__ to orchestrate this process. One thing that surprised me about Cassandra is the fact that every node is aware of every other node in the system and hence the range they are responsible for. This is done by a very efficient __anti-entropy gossip algorithm__ and it guarantees high durability and good partition tolerance in practice. What I also found interesting in Cassandra is how they implement __failure detection__. It turns out to be a dynamic process where failure suspicion level is computed gradually through gossip messages across nodes in the system. When it hits a pre-determined threshold, quorum will be adjusted for relevant operations.  
Another thing that really differentiates Cassandra from other distributed database system is its __local persistence__. Typical write operation involves a write into a commit log for durability and recoverability. __The update will be done in memory__ instead of being committed to disk. When the in-memory data structure exceeds a certain size limit, all the data will be flushed to disk. This way, write operation will be more efficient since all the updates are final. __Lookup is firstly done in memory__. If not found, data files will be checked in “most recent first” order. What’s worth noting here are: 1. a bloom filter is implemented to check the membership of the key before it actually checks up the file; 2. no lock is needed to read data item since data, once dumped into disk, will never be mutated.

__Implementation Modularity__  
The Cassandra process on a single machine is primarily comprised of the following abstractions: partitioning module, cluster membership module, failure detection module and storage engine module. All system control messages rely on UDP based messaging while the application related messages for replication and request routing relies on TCP.

To conclude, Cassandra implemented a distributed database with high availability and good scalability. Because it largely utilizes server memory to process write request, it is one of the few distributed database that offers scalable write performance.
