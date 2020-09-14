---
layout: post
title: Google File System
date: 2017-05-09 21:00:00 -0800
categories: paper-summary
tag: GFS
---

* content
{:toc}




Google File System (GFS) is a distributed file system running on inexpensive commodity hardware and providing good performance in terms of scalability, reliability, availability. This summary will highlight a several system designs that made such good performance possible.  

__Background - Application Level Assumptions__  
1. GFS is built on top of inexpensive commodity hardware. Failure is common and thus constant monitoring and fault tolerance is necessary.
2. GFS stores files in size of multi-GB. System block size has to be revisited to improve IO performance.
3. Access pattern includes scan, stream in terms of READ and append in terms of WRITE. Random READ/WRITE is slow but rare. In this sense, data item can be viewed as immutable.
4. Hundreds of requests could occur concurrently on a single file. Producer-consumer queues are necessary model to grow files.  

__Architecture - A GFS cluster consists of a single master and many chunkservers.__
__Master__ maintains all the __metadata__ in memory, including: namespace, access control information, mapping of files to chunks and locations of chunks. Among them, __chunk locations__ are polled from all the chunkservers directly into memory when it starts. There is no point in trying to maintain a consistent view of this information on the master. But __namespace and mapping of files__ to chunks are also kept persistent by maintain operation log on disk. __Operation log__ is periodically “refreshed” from the top when a system “checkpoint” is flushed to disk. These persistent data are also replicated on other machines. Master maintains __checkpoint__ of its machine state using a compact B-tree. It is running in a separate thread from the one running operation log. It is periodically flushed to disk as it is so-called “checkpoint” so that when master goes down, it can quickly recover its machine state by loading the checkpoint into memory and replay the log operation from the top. Master also controls __global activities__ such as chunk migration, orphan chunk collection, lease management.  

Files are divided into fixed-size chunks stored in __chunkservers__. A large __chunksize__ is important to performance improvement because: 1. Even though clients are asking for different data items, it’d be more likely that they place their requests on the same chunk of data given a large chunksize. 2. There’d be less metadata to maintain in master memory. Replicas are distributed among the cluster to ensure __availability__. The data is pushed linearly along a carefully selected chain of chunkservers that are “closest” to each in network topology. It utilized the full bandwidth to ensure the efficiency of data replication. This, plus master’s recoverability, improved the __fault tolerance__ of the cluster.  

During server-client activities, client initialize the request sent to master and the master replies with the location of the data item requested. Client then directly request to specific chunkserver, most likely the closest one, for reading/writing. The metadata including the chunk location and data it contains is temporarily cached on client side in case of further “similar” requests. Because there’s only one master, we want to minimize its traffic so that it would become the bottleneck of the cluster’s performance.
