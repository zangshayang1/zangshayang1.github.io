---
layout: post
title:  Kafka Anatomy
date:   2018-06-22 22:08:00 +0800
categories: Study-Notes
tag: kafka
---

* content
{:toc}



### What is kafka
__Kafka__ is a message system, built for distributed system.   

### About topic
A "topic" is the highest abstraction of a message repository that focuses around the "topic".  

### About partition (on abstraction level and physical level).
A "partition" of some topic is the lowest abstraction of a message repository that belongs to this topic. A topic can have abitrary number of partitions. The abstraction is designed to: 1) facilitate load balancing across data center when messages come to this topic; 2) allow multiple downstream processes to consume messages in parallel. _In fancy terms: a partition is the smallest unit of parallelism, avoiding coordinations between consumers competing for some partition._  
Physically, a partition consists of a set of log files of approximately 1GB size each, residing on a node. For high availability, There are 3 copies of the same partition. One of them is a "leader" that actually accepts messages from producers and gives away messages to consumers, and the other two are copies. The nodes that are suppose to store these coordinate the replication for themselves.  
  
### About producer (network/cache optimization exploited in kafka).   
A "producer" publishes messages to one or more topics. Of course, multiple producers can publish to one same topic. If a "partition-key" is not specified, these messages will go to different partitions within the same topic in "round-robin" fashion. If it is specified as <key, payload>, they will go to (hash(key) % partition_num)th partition.    
Messages come in via network, kafka exploits "sendfile API" that already exist in all unix os to transfer bytes from a socket channel to a file channel, or vice versa when they are consumed. (Typically, it goes like this: file -> page cache -> application buffer -> kernel buffer -> socket).    
When messages accumulate in page cache, they are flushed to disk after a configurable number of messages received or certain amount of time elapsed. And only after a message is flushed to disk, it can go to consumer.    
In a mature kafka system, the lag between messages arrive and messages leaving is small, which means kafka can utilize write-through caching and read-ahead caching that are heuristic caching strategies implemented in normal os, to improve data transfer efficiency in exactly this kind of scenario.    
  
### About consumer (its relation with partitions and message offset)  
A "consumer group" of one or more topic is what logically consumes all the messages from the topic(s). Much like producer, developers define the behavior of a consumer program. A consumer group consists of several consumers.   
1. If the number of partitions of a topic equal to the number of consumers within a consumer group G, there will be one-to-one mapping relation between each consumer and each partition.   
2. If the number of partitions of a topic is less than the number of consumers in G, there will be (#consumer - #partition) idle consumers.
3. If the number of partitions of a topic is more than the number of consumers in G, which is normally the most common situation, each consumer consumes from a set of partitions and they are assigned to each consumer without overlapping.
4. If there is another consumer group P, subscribing the same topic. It is independent and not interfering with other groups.
5. A consumer group can subscribe multiple topics.
Each message stream provides an iterator interface that a consumer can consume from. In case that no more message is available, the iterator blocks until new messages are ready. Under the hood, a consumer issues asynchronous pull requests to fetch a buffer of data in size of hundreds of KB, for an application to consume. Each pull request contains an offset of messages where the consumption starts and an acceptable number of bytes. These two numbers sum up to a bigger offset which the next pull request will begin with.  
  
### About broker (and how it utilize Zookeeper)
A broker is a node storing a number of partitions that are not supposed to be copies of one another. They could be within the same topic or different ones depending on how "distributed" the logical partitions are and the infrastracture.  
Thanks to __Zookeeper__:  
1. path watcher made it easy to detect addition/removal of brokers.
2. ephemeral path made it easy to detech addtion/removal of consumers.
3. replication mechanism ensures high availability.  
Also each broker maintains:  
1. for each partition, an in-memory index data structure that maps the first message offset to the file that starts with that offset. So when a consumer pulls with a target offset, the message can be quickly fetched.
2. for each consumer group, an __ownership registry__ and an __offset registry__. The former stores a path with value being the id of the corresponding consumer, for each subscribed partition. The latter stores the last consumed message offset for each subscribed partition.
3. an __consumer registry__ that stores mapping relation between consumer, which consumer group it belongs to and the topic(s) it subscribes. 
4. an __broker registry__ that stores mapping relation between broker host address and the set of topics/partitions it contains.  
  
### Rebalancing Algorithm (make more sense when you finish all those above, especially those about brokers.)

__Load rebalancing is triggered when brokers/consumers are added or removed.__

<img src="{{ '/styles/images/kafka-anatomy/kafka-anatomy-rebalancing-algorithm.png' }}" width="70%" />

### Retainability and Availability
Each broker retains messages in its physical form as "segment" files for a configurable amount of time. 7 days in default setting.  
When a broker goes down and all the partitions become unavailable, consumer will block till, for each missing leader partition, a new leader is elected from the remaining replicas.  


### Further reading (TODO)
[kafka Implementation](https://kafka.apache.org/documentation/#implementation)  
[kafka in a nutshell](https://sookocheff.com/post/kafka/kafka-in-a-nutshell)  
