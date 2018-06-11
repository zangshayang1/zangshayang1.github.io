# About Vcores
Abstraction is moved further into application space using Java Multithreading capabilities into Hadoop/YARN in form of vCores for improving resource utilization.

Number of vcores has to be set by an administrator in yarn-site.xml on each node, with the property name being: yarn.nodemanager.resource.cpu-vcores.
The decision of how much it should be set to is driven by the type of workloads running in the cluster and the type of hardware available. 
The general recommendation is to set it to the number of physical cores on the node, but administrators can bump it up if they wish to run additional containers on nodes with faster CPUs.


# Start namenode & datanode
$HADOOP_PREFIX/sbin/hadoop-daemon.sh --config $HADOOP_CONF_DIR --script hdfs start namenode
$HADOOP_PREFIX/sbin/hadoop-daemons.sh --config $HADOOP_CONF_DIR --script hdfs start datanode
