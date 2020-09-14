---
layout: post
title: Google Spanner Architecture
date: 2017-04-28 17:00 -0700
categories: paper-summary
tag: distributed
---
* content
{:toc}




## Google Spanner Architecture

__Summary from paper - Google’s Spanner: Globally Distributed Database__  

This paper gives us an introduction to google Spanner, a globally distributed database. From a high level, it goes through: 1. its engineering designs and what kind of features are enabled by the designs; 2. its TrueTime API and how it ensures the consistency of the distributed database; 3. it also showcases how transaction consistency is guaranteed in different situations and its overall performance.

__System Design__  
From the perspective of top down abstraction at a physical level, __a Spanner deployment is a universe consisting of globally distributed data centers.__ Each data center is comprised of one or more zones that are physically isolated. For any application, data replication must be made between multiple zones to ensure high availability in case of hardware failure. __Each zone includes 1 zone master, 1 location proxy and thousands of servers.__ Location proxy maps user requests to specific servers according to users’ location. Zone master does the bookkeeping in terms of assigning data to servers and coordinates the communication between servers. It seems that location proxy does much fewer things than zone master. Despite their functions are different, it is possible that they can be handled by one master node. Here two nodes are deployed, I guess it is because one of them can take care of the traffic when the other fails. __On top of zones, there are 1 universe master and 1 placement driver.__ Universe master serves as a monitor of entire Spanner. Placement driver orchestrates the replication of data across different zones.  
__From the perspective of bottom up abstraction at a software level__, each server implements thousands of instances of __tablet__, storing information in such a data model that maps primary key column to non-primary-columns with a timestamp, which is what differentiates tablets in Spanner from that in Bigtable. __Along with the timestamp, each server implements a single Paxos state machine as well as a lock table to ensure transaction consistency within the group.__ A transaction manager is also implemented each server, specifically for facilitating communications between different servers. Another layer of abstraction, __directory__, containing a set of continuous keys, has the same replication configuration, which means replicas can be made directory by directory with abstract atomicity.  
Put together, these implementations made it possible for Spanner to have good performance in terms of consistency, availability and partition tolerance.

__System Clock and Transactions__  
System time references GPS and atomic clocks to overcome the deficiency that each of them has different failure modes. The smallest unit is an interval with bounded uncertainty, which compensates network delays. __Each data center has a time master and each server implements a time slave daemon that polls from multiple time masters.__ Time masters reference each other. This structure ensures a globally consistent clock.  
Assigning timestamp to RW transactions can be made anywhere between all locks have been acquired and any lock has been released with on invariant: whichever transaction starts early should have a commit time that is earlier than others’. 2PL locking and 2PL commits are implemented for RW transactions. Assigning timestamp to RO transactions is done before executing it. Then a snapshot read of that timestamp is done at any replicas that are sufficiently up-to-date.
