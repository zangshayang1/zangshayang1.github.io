---
layout: post
title: Hadoop-Spark-Storm-Mesos-DataFlow
date: 2017-05-13 21:00:00 -0800
categories: Paper-summary
tag: distributed-computing
---

* content
{:toc}


## A Survey About Big Data Process

### Abstract  
Being able to process massive data is becoming the new standard for not only IT/Internet companies but  almost all companies that are powered by information technology. Given the huge demand, developers and communities are actively seeking for better solutions. Distributed data processing frameworks have been rapidly evolving. This paper will go from the initial framework that deals with data in batch to the latest work that focuses on unbounded data stream. If you can understand how the past had been evolved, you will be more likely to see where the future is heading to.  

### Introduction
Not very long ago, big data is still a buzz word and the data rest on disks in a typically distributed system. MapReduce, a framework designed to tackle heavy computation job in a distributed system that came from Google, became one of the classic approaches to process massive datasets. Hadoop implemented MapReduce and soon became the most popular open source framework to process big data in batch. Later, Hadoop not only stood for its core implementation of MapReduce but also represented an ecosystem including many other supporting frameworks such as system monitor (Zookeeper), distributed file system(HDFS) and SQL-like database(Hive). Strong community supports for all these components have been established and they are still playing essential roles in today’s big data world. At that stage, almost all big data tasks were done in batch. What’s different from traditional data processing is just it has to be done in a distributed system due to its massive volume.  

Soon after that, a group of people from Berkeley developed Spark, a framework to process big data in a more efficient way. One thing that fundamentally set it apart from Hadoop is that Spark supports data processing in mini-batch. The batch size could be so tiny that it almost looks like it processes data in a stream. That is where large-scale realtime data processing started.  

Meanwhile, Mesos came out of the same group and gained popularity as a lightweight distributed resources manager so that people can run various distributed computing frameworks in one cluster.  

Storm framework came from a different background with a native design to support processing streaming data in a even more efficient way. Later in this paper, I will make a coarse comparison between Spark and Storm.  

All the frameworks with obvious capacities as well as limitations. At beginning, it is these frameworks that made large-scale realtime data processing possible in real life applications. However, in today’s world, demands from real life applications have been further pushed, so far as to an extend where developers started to rethink about fundamentally different data models that can shrink the limitations of future framework as much as we need. That’s where DataFlow came in.  

### Sections

This paper will look into the evolution of distributed data processing frameworks. As they follow below:  
1. Section A will give an overview on the architecture of Hadoop and the core members in Hadoop ecosystem.
2. Section B will focus on the fundamental differences between Spark and Hadoop and how that helps Spark outperforms Hadoop in many tasks.
3. Section C is dedicated to describe how Storm works. The description will be based on the concepts that occurred in previous sections, and unfolded by comparing Storm with Hadoop and Spark.
4. Section D will review a lightweight resources manager — Mesos that makes it possible to run various frameworks in the same cluster.
5. Section E will introduce an innovative data model — DataFlow Model challenged the convention data model used by all the above frameworks and how the new data model can bring down the limitations for big data processing in the future.

### A. Hadoop

Hadoop is modeled after Google’s MapReduce framework. Its core components include this MapReduce module and Hadoop File System (HDFS). These two components are relatively straightforward so I will not go into details about them. Hadoop was originally designed as an independent distributed computing framework, so it came with its own resource manager - YARN, which stands for “Yet Another Resource Negotiator”. It has such a strange name because it is a replace of previously integrated resource manager. Resources management is an interesting topic. In fact, I will go through a more lightweight resources management layer (Mesos) in the next part. So I will not focus on YARN right now.  

Compared with other modern frameworks, Hadoop has less intuitive semantics. Although we don’t have to programmatically distribute computing tasks, the use of Hadoop still involves a lot of interactions based on the understanding its low level architecture. Therefore, it would be helpful to know the following concepts in Hadoop: 1. The master node, (aka: name node), runs on a layer of master node software while other slave nodes (aka: data nodes) run on a layer of data node software. Before ZooKeeper was introduced to Hadoop ecosystem, Hadoop is built under a single-master architecture because it was easier. All the data nodes will report to the master node about what kind of computation resources and data they have. The master keeps track of their locations. 2. Programmers need to write a driver program for each computing job and submit the job to JobTracker. JobTracker will then communicate with the name node for the locations of relevant data and then push the job to as close data node as possible. Every data node has a TaskTracker that received tasks from JobTracker and assigns a “slot” to each task. Slot is an abstraction of computing resources in a data node.  

On top of everything above, it would be more appropriate to view Hadoop as an ecosystem now rather than just a distributed computing framework. The ecosystem includes: 1. ZooKeeper, a distributed system monitor as well as a coordinator. 2. Hive: a data warehouse that provides SQL-like language supports. 3. HBase: a BigTable-like NoSQL database…And many others.  

In terms of performance, for an entire MapReduce task, both system IO and network bandwidth become the factors slowing down the process. For a single Map or Reduce task, the bottleneck is just the system IO because the output stays in local machine. Spark significantly accelerated the process by caching the output in memory.  

One last thing that I’d like to emphasize is Hadoop’s acyclic data flow model, which ensures the fault tolerance of the system. Since every intermediate result can be computed based on the information stored in its parents, but it also limits the type of computations this data flow model allows. In the next part, we will talk in details about how Spark 1. accelerated a distributed computing task; 2. extended the capacity of the framework without jeopardizing its fault tolerance.  

### B. Spark and More  

Closely following Hadoop, let me spare those routines explaining what Spark is and directly dive in its core. The fundamental improvement Spark made is a new abstraction of data model - Resilient  Distributed Dataset (RDD). It encapsulated the distributed dataset so that they can be cached in memory. Programmatically speaking, now with Spark, you have an object holding and representing the physically distributed datasets. In addition, you can perform parallel operations on them without even realizing that these are distributed across the cluster. The obvious advantage of this data model is now you don’t have to dump them to disk every time when a Map/Reduce has completed. Instead, you can send those intermediate results directly through the network to where the next step will be carried out on them. This explained why Spark is a lot faster than Hadoop. Taking it one step further, Hadoop runs its job through an acyclic data flow, which doesn’t mean that iterative tasks cannot be performed on the same dataset with Hadoop. But it means you will need to assign same type tasks to downstream nodes and all the latency from system IO and network bandwidth will be carried. If you want to do the same task on the same node in Hadoop, it would be even slower than the previous strategy because the “chunky batch processing design” would block the flow of data and downstream computation, rendering less parallelism when it comes to the executions of system IO and computation of the entire job. However, the abstraction of RDD solves the problem again. The limited size of RDD allows data flow in a more parallel way both to downstream nodes as well as disk. It also made iterative computation easy because all the computations can be done in memory without being bottlenecked by system IO or network bandwidth. You may argue that by no means can a single-node machine hold all the data in memory. Yes, but you don’t have to. The main memory of modern commodity machines can hold a fair amount of RDD, which again created a flow of data for downstream consumption as if all of them had been iterated over and over instantly. This very naturally leads us to the next two important concepts in Spark: Job topology and lazy scheduling. As a programmer, when a Spark job is created, you didn’t create a specific assignment of tasks and resources. Instead, you created a topology in terms of how data flow. The JobTracker doesn’t have a clue about the work load, the resources needed or the optimal scheduling until the job is instantiated and starts to run.  

This notion gave birth to SparkStreaming, a native module that makes Spark job running on tiny RDDs and thus creates an illusion that data come in and out in realtime, which is followed by yet another framework — Storm.  

Before going to Storm, I’d like to give a quick overview of “Spark Ecosystem” which I think would be the foundation of a lot data-driven web-based applications in the future. Beside SparkStreaming module, Spark also provides: 1. A naturally integrated machine learning library — MLib. 2. A graph-parallel computation library — GraphX. 3. SQL-like language support. In fact, as I am writing paper, Spark is gradually shifting away from the RDD abstraction to a so-called DataSet abstraction to have a better integration with SparkSQL. What’s worth noting is Spark is written in Scala and running on JVM. Java has been the dominating language used in a lot of enterprise applications. It is not a surprise that most of the distributed coming frameworks support Java API. However, Scala is gradually gaining popularity in data-driven web-based applications thanks to the fact that Scala has both functional programming characteristics as well as object oriented characteristics. Its functional programming characteristics allows programmers to write syntactically simple but powerful parallel data processing scripts. Its object oriented characteristics allows programmers to write scalable applications. Another quick developing area is web-based services. Traditional Single Object Access Protocol is used to develop and deploy web-based services. Driven by the market, today’s web-based services are highly modularized and lightweight, as shown by the buzz word “microservice” everywhere. All of these put Scala in a central place where all kinds of frameworks both in the frontend and the backend are being developed around Scala, such as Spark(backend),  Akka(reative streaming), Play(frontend). That is why I believe that Scala will become the go to language for developing enterprise applications of next generation and Spark will power their backend.  


### C. Storm  

Storm is also a distributed computing frameworks that only handles data stream. It is faster than SparkStreaming. It also uses ZooKeeper as the monitor and coordinator of the distributed system and its architecture is a lot like Hadoop even though Storm uses cooler names. For example, the master node runs a daemon called “Nimbus” that is essentially doing the same thing as “JobTracker” in Hadoop. Nimbus is responsible for assigning tasks to slaves and monitoring for failures. Each slave node runs a daemon called “Supervisor”, which is essentially doing the same thing as “TaskTracker” in Hadoop.  

Storm uses data model similar to Spark. But Storm defines a stronger semantics in terms of its job topologies. For example, working nodes are divided into two disjoint sets - spouts and bolts, where the former stands for the source of data stream and the latter stands for the conventional working nodes. It is also a very natural move because, in Spark, data stream is more of an illusion created by mini-batch RDDs while real streaming data are the only eligible input format in Storm.  

Like what has been done in Spark, each job is abstracted into a topology and programmers submit topologies to “Nimbus”. From the perspective of using Storm as a database, interestingly, a topology can be viewed as a logical query plan issued against a database.  

Lastly, I’d like to touch upon Summingbird in the context of Storm. Summingbird is separate logical planner that can map generated “logical query plans” to a variety of stream process ing and batch processing systems.  


### C. Mesos  

Mesos was introduced as “a thin resource sharing layer that enables fine-grained sharing across diverse cluster computing frameworks”[1]. Put in layman’s term, it is essentially a resource manager that helps to assign resources to tasks that run in a distributed system. The motivation behind Mesos is to allow different distributed computing frameworks, such as Hadoop and Spark, to run on the same cluster without resource or scheduling conflicts. The overriding design principle behind Mesos is its API has to be lightweight yet powerful enough to accommodate future distributed computing frameworks. That’s why Mesos doesn’t take care of scheduling for any specific framework. Instead, its scheduling mechanism is conceptually very straightforward.  

Specifically, as an example, Mesos works as follows: 1. In a cluster, only one machine is the “working master” while a handful of others are registered through ZooKeeper as “backup masters” so that new masters can be elected when the working master fails. 2. Slaves report their resources to master once a master is elected. This way, those “backup masters” don’t have to sync with the “working master” all the time. The master will maintain a resource table subject to be updated in case of slave failures. 3. Distributed computing tasks as well as frameworks used are registered through Mesos API. 4. Resource allocation policies can be arbitrarily defined and plugged in Mesos through its API. 5. Upon the execution of computation tasks Mesos will make offers of resources to those “fine-grained” tasks directly. 6. Tasks can either accept the offer and start running or it can reject the offer because the resources are not sufficient.  

What’s worthnoting is: 1. the resources offering scheduling seems not optimal from an algorithmic point of view. But in practice, its performance is surprisingly close to optimal, because each entire distributed computing task is composed of many smaller tasks running in parallel, resources offering/decline/acceptance are rapidly going leaving small margin in terms of performance saturation. 2. OS container technologies are heavily involved for resource isolation. For example, Linux container provides system-level virtualization. Understanding this is important to visualize the concept of resources rearrangement. 3. Some optimization had been performed to speedup the process, such as filter mechanism. Once offering started, each task will keep track of rejected offer so that repeated rejection can be made fast. This is important to the scalability of the whole system.  


### E. Beam/DataFlow Model

So far we have seen big data processing in a distributed system from batch processing to stream processing. In reality, when we are faced with massive data coming in, the problem is no long how to process these data. Today, the problem has became what I want to discover from the massive dataset and even if I have a clear goal, would the current framework that I am using be flexible enough to handle all kinds of possible data mining tasks? For example, I don’t know how long the sliding window should be. Should I use fix-size window or should I window by the number of events? Should I group by some kind of data features before letting it flow through the cluster? What if something happened to users and data stop streaming in for a period of time? It is gradually revealed that the limitation of our current approach to process data at scale comes from the mindset: I have to use a certain type of framework to carry out a certain type of job because I want optimized performance. This way, we will never be able to truly optimize our data processing pipeline. The DataFlow model is then proposed to solve this problem.  

A few principles behind the design of DataFlow mode are critical: First and foremost, the DataFlow model assumes that all data are unbounded. Second, data don’t lie on some axis defined by how we choose to window them. Instead, window should be an attribute that we can drop or add, just like the notion of grouping data.  

Along with the above design principles, the data model not only includes two core fields: (key, value), but also allows the attachment of event-timestamp and window fields. Event-timestamp is generated during the event while window can be added or dropped anytime. The two primitive operations are “element-wise do” and “GroupByKey”. Other operations can be built on top of these two. Essentially, this model is more comprehensive than previous model in that it allows future distributed computing framework to aggregate and analyze data in arbitrary whatever way they want. Previously, only one direction is supported but now programmers can go back and forth along the temporal dimension.  

Google has implemented DataFlow model with FlumeJava being the underlying data pipeline and MillWheel being the underlying streaming engine. It now stands as an essential part of Apache Beam project. A few other companies are also involved.  