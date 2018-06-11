Building a Hadoop Cluster involves the following steps:

Build Reference: https://dwbi.org/etl/bigdata/183-setup-hadoop-cluster
Understand Reference: http://bradhedlund.com/2011/09/10/understanding-hadoop-clusters-and-the-network/

1. Cloud Instances:
  1 NameNode
  x DataNode (x >= 1)

2. Prerequisite:
  JVM on all nodes
  Hadoop on all nodes

3. Connect via SSH:
  Generate a ssh-key for each node and add RSA public key of the NameNode to
  ~/.ssh/authorized_keys of each node (including NameNode and DataNode coz
  Hadoop will need to connect all of them).

4. Alias in /etc/hosts
  ip0 NameNode
  ip1 DataNode1
  ip2 DataNode2
  ...
  (By now, you should be able to ssh into DataNode from NameNode with, for example: $ ssh DataNode1)

5. Configure JVM and Hadoop

  a1. $JAVA_HOME needs to be set in ~/.bashrc
  a2. Hadoop Env Variables need to be set in ~/.bashrc, such as:
    $HADOOP_HOME
    $HADOOP_MAPRED_HOME
    $HADOOP_COMMON_HOME
    $HADOOP_HDFS_HOME
    $YARN_HOME
    ...

  b. Several XMLs under hadoop_install_path/etc/hadoop/ need to be configured.
    core-site.xml - specify FS, IO and NameNode address
    hdfs-site.xml - specify hdfs directory for NameNode (include checkpointing) and DataNode
    mapred-site.xml - MAPRED specifications.
    yarn-site.xml - YARN specifications.
  c. List NameNode(s) (usually = 1) in hadoop_install_path/etc/hadoop/masters.
     List DataNodes (usually > 1) in hadoop_install_path/etc/hadoop/slaves.
  d. Set $JAVA_HOME in hadoop_install_path/etc/hadoop/hadoop-env.sh [critical]

6. Try
  > cd hadoop_install_path/sbin
  > ./start-dfs.sh
  > ./start-yarn.sh


Build Hive RM on EdgeNode

Reference: https://dwbi.org/etl/bigdata/188-install-hive-in-client-node-of-hadoop-cluster

1. Use another instance as EdgeNode:
  configure it with JVM, Hadoop, related env variables, SSH and /etc/hosts file.
  now when hadoop-cluster is set up-running, EdgeNode serves as a standalone instance accepting client side request,
  for example: set up Hive application as RM on EdgeNode accepting data READING/WRITING request.

2. Configure /hive_install_path/conf/hive-env.sh:
    $HADOOP_HOME
    $HIVE_CONF_DIR
   Set Env variables in ~/.bashrc:
    $HIVE_HOME
    $PATH
    $CLASSPATH
    etc...

3. Set up /user/hive/warehouse DIR structures in HDFS and chmod its access.

4. Set up /hive_work DIR on EdgeNode to store hive metadata (metastore)
    > cd /hive_work
    > $HIVE_HOME/bin/schematool -dbType derby -initSchema
    // this is to initialize an embeded metastore derby for testing purpose
    // derby disallows concurrent query going through
    // this is why we need to set up MySQL below as an external metastore

5. TRY
    > hive // should enter hive console

6. Set up MySQL

  download install mysql-server with whatever package manager you have in your OS or do it manually
  > (sudo) service mysqld start
  // start the server before you can install MySQL
  // service is a utility that comes with CentOS
  > sudo mysql_secure_installation
  // this installation program comes with the downloaded
  > mysql --version
  // check to see if it is running correctly
  > mysql -u root -p [pswd]
  // enter mysql console as a root user

7. Configure MySQL as Hive Metastore

Reference: https://dwbi.org/etl/bigdata/190-configuring-mysql-as-hive-metastore

  a. The goal is having Hive use some MySQL database as Metastore
  b. CREATE DATABASE metastore;
  c. CREATE and GRANT ALL ON metastore.* TO the following users:
      'hive'@'localhost' IDENTIFIED BY 'pswd' - allow the connection over unix socket as opposed to TCP/IP socket. Note: '127.0.0.1' is IPv4 loopback and '::1' is IPv6 loopback.
      'hive'@hiveserver2.ip IDENTIFIED BY 'pswd'
      'hive'@'%' IDENTIFIED BY 'pswd' - accepting all coming request via TCP/IP socket
     FLUSH PRIVILEGES;

    * creating an account with different hosts allows the user to access from different hosts.

  d. Put MySQL JDBC driver jar under /hive_install_path/lib/ so that Hive can talk to MySQL database.

  e. Use /hive_install_path/conf/hive-default.xml.template to create a "hive-site.xml" (exact the name) file to specify remote MySQL Metastore configurations, where the following info are provided:
      ConnectionURL
      ConnectionDriver
      ConnectionUserName
      ConnectionPassword
      hive.metastore.uris
      hive.exec.local.scratchdir - as opposed to data persistent storage path: hdfs:/user/hive/warehouse
      hive.scratch.dir.permission
      ...
  f. Initialize Metastore schema
      > hive_install_path/bin/schematool -dbType mysql -userName hive -passWord [pswd] -initSchema

  g. Serve Metastore up
      > hive_install_path/bin/hive --service metastore

  h. Put hive-jdbc-<version>-standalone.jar under /hive_install_path/lib/
     so that Beeline, a JDBC client, can be used to connect to Hive

  i. Connect to Hive (connects directly to HDFS and Metastore) from Beeline in embeded mode (Beeline client and Hive installation both reside on the same host)
      > hive_install_path/bin/beeline
      > !connect jdbc:hive2:// hive [pswd]

  * i. Connect to hive from Beeline in remote mode (TCP connectivity required)
      > hive_install_path/bin/hiveserver2 - serve up
      > !connect jdbc:hive2://hiveserver2.jp:10000/<db> hive [pswd]

    * why beeline depends on hadoop

Next step: separate Beeline client and Hive installation and see how it works and how it relates to user at different hosts.
           Spark


HiveQL

> create table flight(
>   year INT,
>   month INT,
>   day INT,
>   day_of_week INT,
>   dep_time INT,
>   crs_dep_time INT,
>   arr_time INT,
>   crs_arr_time INT,
>   unique_carrier STRING,
>   flight_num INT,
>   tail_num INT,
>   actual_elapsed_time INT,
>   air_time INT,
>   arr_delay INT,
>   dep_delay INT,
>   origin STRING,
>   dest STRING,
>   taxi_in INT,
>   taxi_out INT,
>   cancelled INT,
>   cancellation_code STRING,
>   diverted INT,
>   carrier_delay STRING,
>   weather_delay STRING,
>   nas_delay STRING,
>   security_delay STRING,
>   late_aircraft_delay STRING
>   )
>   partitioned by(dt STRING) -> closely following the table definition
>   clustered by(unique_carrier) into 128 buckets
>   row format delimited
>   fields terminated by ','; -> at the end compatible with the data.csv format

> load data
> local inpath './localdata/2008.csv' -> local relative to hive application, without keyword "local", it will look for the file on HDFS - "> load data inpath 'hdfs:/user/data/...' overwrite into table ... "
> overwrite into table flight
> partition (dt='partition0'); -> set partition at the time of loading/insertion

> select count(*) from flight; -> SQL-like query is implemented as MR job, might fail if there is not enough virtual memory.

SparkSQL

spark> df = spark.read.csv("hdfs://namenode.ip/test/2008.csv")
spark> df.count()

  * you can construct a schema for the dataset and pass the schema into READ clause
  * > df.select(...) -> SparkSQL

spark> import org.apache.spark.sql._
spark> val sqlContext = SQLContext(sc)
        // sc : sparkcontext
spark> sqlContext.sql("CREATE TABLE sparkSQL_at_localCluster(name STRING, country STRING, area_code INT, code STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ','")
        // data will be stored at local cluster managed by YARN
        // spark cluster as a computation engine doesn't come with entire Hadoop framework
spark> sqlContext.sql("CREATE EXTERNAL TABLE sparkSQL_at_remoteCluter(name STRING, country STRING, area_code INT, code STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE LOCATION 'hdfs://namenode.ip/user/hive/warehouse/some_database'")
        // data will be stored at remote client managed by Hadoop(YARN)

       * if the above two tables are created during the same Spark Session, both of them will be cached in memory. Therefore, even though their physical storage is completed separated, they can't share the same table name in the same database.
