---
layout: post
title:  Spark Learning Notes
date:   2018-06-16 11:08:00 +0800
categories: Study-Notes
tag: spark
---

* content
{:toc}



### Understand Executor 
__where the abstraction sits in a range of growing computing power__  

cluster > node > executor > core > thread  
* _each executor is a JVM instance_  

### About --num-executors, --executor-memory, --executor-core 
__Given infrastracture, how to harness the power a spark application with these configurations.__  

Starting from 10 nodes, 16 cores and 64GB RAM in each node managed by YARN:  

On each node, 1 core and 1GB RAM is needed for hadoop daemons:  
  HDFS: NameNode, Secondary NameNode, DataNode  
  YARN: ResourceManager, NodeManager  
  
Now resources are down to 10 nodes, 15 cores and 63GB RAM in each node.  
  
For example:  
  We set aside 1 node for namenode1/RM1  
  We set aside 1 node for namenode2/RM2  
  We set aside 1 node for CLIENT where jobs are submitted.  
  
Now resources are down to 7 nodes, 15 cores and 63GB RAM in each node.  
  
Research shows an executor can carry 5 concurrent tasks before the overall performance degrades. So 5 cores per executor is a good number. Also 64GB RAM for each executor could potentially result in excessive garbage collection overheads.  
  
If we assume 5 cores per executor, there will be 3 executors in a node, and each executor has 21GB RAM.  
  
In reality, overhead memory will be provisioned besides spark.executor.memory according to the following formula:  
  
spark.yarn.executor.memoryOverhead = Max(384MB, 7% of spark.executor-memory)  
  
So if spark.executor.memory is set to 19.6, 7% of it would be 1.3G, total: 20.9G.  
  
Plus, YARN always takes the ceilling value of requested memory space, thus 21GB memory will be requested for each executor from YARN.  
  
default:  
  if mode == yarn:  
    spark.executor.cores = 1  
  elif mode == standalone:  
    spark.executor.cores = number of available cores on worker  
  
With small number of cores, such as 1 core per executor, running multiple tasks in the same JVM is difficult. Two disadvantages on this approach:  
  1. Each task has higher average JVM overhead. Average JVM overhead = total JVM overhead / number of tasks one executor can handle concurrently.  
  2. More executors require more replications of data/variables that are shared in a job.  
  
With large number of cores, such as 15 core per executor, there are two disadvantages on this approach as well:  
  1. HDFS throughput will be negatively affected.  
  2. Excessive RAM in a JVM leads to excessive garbage collection overheads.  
  
### Dynamic Allocation  
With spark.dynamicAllocation.enabled=true:  
  1. A job starts with (spark.dynamicAllocation.initialExecutors) number of executors.  
  2. If there are pending tasks waiting for more than (spark.dynamicAllocation.schedulerBacklogTimeout), new executor(s) will added at exponential rate starting from 1, till (spark.dynamicAllocation.maxExecutors) is reached.  
  3. If there are executors idle for more than (spark.dynamicAllocation.executorIdleTimeout), these executors will be removed, till (spark.dynamicAllocation.minExecutors) is reached.  
  
### Cluster Type
With running a spark app on a standalone cluster:  
  1. Save communication overheads, provide faster job-kick-off.  
  more on: https://spark.apache.org/docs/latest/spark-standalone.html  
  
With spark on a YARN cluster, it provides:  
  1. high availability via Zookeeper.  
  2. Kerberos-secured filesystem - HDFS.  
  
With spark on a MESOS cluster, TODO:  
  
### Deploy Mode
  
With spark app deployed in 'client' mode, the driver process runs inside of client process JVM. Under this deployment, if client is remote to the cluster, the result will come back very slow due to high network latency.  
  
With spark app deployed in 'cluster' mode, the driver process runs inside of application master JVM, which is not the same thing as namenode/RM in the context of hadoop environment. Application master JVM is spinned off with (spark.driver.memory) amount of memory just like application executor JVM is spinned off with (spark.executor.memory) amount of memory managed by YARN. Physically, these processes could be running on the same node or different ones. Particularly, the driver process runs within a cluster where executors are provisioned.  
  
### Memory Division  
  
__what the below term really means subjects to further research__  
  
spark executor full memory =  
  1. spark.yarn.execitor.memoryOverhead  
  2. spark.executor.memory =  
    1. spark.shuffle.memoryFraction  
    2. spark.storage.memoryFraction  
  
### Spark Driver
  
A driver is a process running in a JVM that orchestrates a spark job. The node it is running on is namely the 'master' of the application. (Note: different from the namenode/RM in a YARN cluster). The driver does the following:  
  1. Create sparkContext instance, which asks cluster RM for resources to launch executors.  
  2. Distribute application code (JAR or .py) to executors, such as 'main' jar and other jars added after '--jars' option.  
  3. Split the job into tasks and schedule them onto executors.  
  4. Orchestrate the execution of the job and monitor health of executors. (The 'driver' node must be addressable from 'executor' nodes.)  
  5. Preemptively restart tasks on a new node if a node crashes or performs much slower than other nodes.  
  6. Release resources after the job is done or crashed.  
  
### Launch Sample
```shell
$ ./bin/spark-submit --class my.main.Class \  
    --master yarn \  
    --deploy-mode cluster \
    --driver-memory 8g \
    --executor-memory 18g \
    --executor-core 5 \
    --jars my-other-jar.jar,my-other-other-jar.jar \
    my-main-jar.jar \
    app_arg1 app_arg2
```
  
### Understand Stage From Shuffles
A stage corresponds to a collection of tasks that execute the same code on different subsets of the data. It can be completed without data shuffling. For example:  

``` java
val tokenized = sc.textFile(args(0)).flatMap(_.split(' '))
val wordCounts = tokenized.map((_, 1)).reduceByKey(_ + _)
val filtered = wordCounts.filter(_._2 >= 1000)
filtered.collect()
```
1. Operations before completing .reduceByKey() are going to be done in stage 1.
2. .reduceByKey() will be performed on each tokenized partition before shuffling happens.
3. Shuffling occurs at stage boundary. Resulted data partitions are written to disk in parent stage and they are fetched over the network by tasks in the child stage where .reduceByKey() can continue and complete, which means both the locations and the numbers of data partitions changed during shuffling.
4. All the operations afterwards are going to be done in stage 2.

__Note:__  
Narrow transformation: map, filter, flatmap  
Wide transformation: groupBy, reduceByKey, join  

For more on tunning and performance:  
[How-to: Tune Your Apache Spark Jobs (Part 1)](http://blog.cloudera.com/blog/2015/03/how-to-tune-your-apache-spark-jobs-part-1/)  
[How-to: Tune Your Apache Spark Jobs (Part 2)](http://blog.cloudera.com/blog/2015/03/how-to-tune-your-apache-spark-jobs-part-2/)  
  
### References
[spoddutur: Distribution of Executors, Cores and Memory for a Spark Application running in Yarn](https://spoddutur.github.io/spark-notes/distribution_of_executors_cores_and_memory_for_spark_application.html)
[stackoverflow: How to tune spark executor number, cores and executor memory?](https://stackoverflow.com/questions/37871194/how-to-tune-spark-executor-number-cores-and-executor-memory)
[stackoverflow: What are workers, executors, cores in Spark Standalone cluster?](https://stackoverflow.com/questions/32621990/what-are-workers-executors-cores-in-spark-standalone-cluster)
[Mastering Spark](https://jaceklaskowski.gitbooks.io/mastering-apache-spark)
