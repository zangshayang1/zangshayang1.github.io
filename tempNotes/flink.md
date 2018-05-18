flink mode:
Local Mode
Local mode is a pseudo distributed mode which runs all the daemons in the single jvm. It uses AKKA framework for parallel processing which underneath uses multiple threads.

Standalone Cluster Mode
In this setup, different daemons runs on different jvms on a single machine or multiple machines. This mode often used when we want to run only Flink in our infrastructure.

YARN
This mode makes flink run on YARN cluster management. This mode often used when we want to run flink on our existing hadoop clusters.


hadoop port convention
8020/9000 - namenode
50075 - datanode
50070 - UI


manage application deployed on Yarn from client node:
> yarn application -list
> yarn application -kill appId

discover namenode (set in hadoop-cluster-node://home/.../hadoop-xxx/etc/hadoop/hdfs-site.xml) from hadoop client node:
> hdfs getconf -namenodes
