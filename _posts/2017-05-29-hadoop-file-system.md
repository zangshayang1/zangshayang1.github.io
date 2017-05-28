---
layout: post
title: Hadoop File System
date: 2017-05-28 13:00:00 -0800
categories: Paper-summary
tag: distributed-computing
---

* content
{:toc}



# Hadoop File System

This summary is a review of the essential design of Hadoop Distributed File System (HDFS) through the lens of practice.  

__HDFS__ is a remote file system. From the perspective of software, it consists of a master machine - NameNode and a bunch of worker machines - DataNode. From the perspective of hardware, it consists of a few racks of computers. Of course the number of racks and computers can scale as much as needed for “Internet-scale-global” applications. Right now, for simplicity, image we have two racks in total. One rack sits in Oregon and the other sits in Virginia.  

If you have hundreds of gigabytes of data sitting on your local disk and you want Hadoop to perform a large amount of computations for you quickly, you need to first transfer those data to HDFS. During file transferring, what has been done underneath the hood is the following:  

File is accumulating locally till its size hits a __configurable block size__, for example, 64MB. The local machine will make request to NameNode for a list of DataNodes that will actually store the data. This activity of transferring data will be noted as a transaction __log for recovery purpose__ that I will come back later. The data will flow from local machine to first DataNode chunk by chunk (limited by network bandwidth). And same chunk will flow from first DataNode to second DataNode and from second to third, provided the best practice that data are usually replicated by a factor of 3. This __pipeline transfer design__ can maximize the use of the available network bandwidth.  

Considering the fact that: 1. __Node failure is common but rack failure is rare;__ 2. Inter-rack data transfer doesn’t need to go through switches and thus faster than between two nodes in different racks. HDFS implemented the following __replica placement policy__: The first two replicas are placed in two different nodes on the “Oregon” rack. The third replica is placed in a node on “Virginia” rack. Advantage is writing replicas from the first node to the second is fast so that the primary replica can be made quickly. Disadvantage is reading availability will be slightly compromised.

Image the first two replicas are done and the third one is still going when the third DataNode goes down. The first two DataNode will maintain a __blockreport__ that indicates how many replicas exist for each block of data. The NameNode will periodically receive the __heartbeat of each DataNode along with the blockreport__, which implies that the DataNode is working properly. Otherwise, no new IO requests will be issue to the dead DataNodes. Also the NameNode will make new replicas whenever it sees the number of replicas is lower than threshold. The NameNode will maintain the metadata in memory including entire file system namespace, a file blockmap and checksum for each block of data (ensuring data integrity whenever fetched) for fast lookup and modification. These metadata is loaded up into memory when the NameNode restarts, which means, these metadata are also maintained on local disk. When a request comes in, an activity record will be appended to the log file and a modification will be made to in-memory metadata, which will be flushed to disk __periodically but not constantly__. In case a NameNode fails, all the in-memory metadata are lost, recovery can be done by reloading the existing metadata on disk and replaying all the activity logs. Of course, the log file will be renewed every time when a flush operation is done. This is __checkpoint recovery mechanism__.

All the above communications are built on TCP/IP, wrapped in RPCs. By design, the NameNode only responds to RPC requests issue by DataNode and users.
