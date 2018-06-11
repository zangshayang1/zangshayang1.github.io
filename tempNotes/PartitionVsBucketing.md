### What is partition

Partitioning data is often used for distributing load horizontally, this has performance benefit, and helps in organizing data in a logical fashion. Example: if we are dealing with a large employee table and often run queries with WHERE clauses that restrict the results to a particular country or department . For a faster query response Hive table can be PARTITIONED BY (country STRING, DEPT STRING). Partitioning tables changes how Hive structures the data storage and Hive will now create subdirectories reflecting the partitioning structure like

.../employees/country=ABC/DEPT=XYZ.

### Why use bucketing

If query limits for employee from country=ABC, it will only scan the contents of one directory country=ABC. This can dramatically improve query performance, but only if the partitioning scheme reflects common filtering. Partitioning feature is very useful in Hive, however, a design that creates too many partitions may optimize some queries, but be detrimental for other important queries. Other drawback is having too many partitions is the large number of Hadoop files and directories that are created unnecessarily and overhead to NameNode since it must keep all metadata for the file system in memory.

### What is bucketing

Bucketing is another technique for decomposing data sets into more manageable parts. For example, suppose a table using date as the top-level partition and employee_id as the second-level partition leads to too many small partitions. Instead, if we bucket the employee table and use employee_id as the bucketing column, the value of this column will be hashed by a user-defined number into buckets. Records with the same employee_id will always be stored in the same bucket. Assuming the number of employee_id is much greater than the number of buckets, each bucket will have many employee_id. While creating table you can specify like CLUSTERED BY (employee_id) INTO XX  BUCKETS; where XX is the number of buckets . Bucketing has several advantages. The number of buckets is fixed so it does not fluctuate with data. If two tables are bucketed by employee_id, Hive can create a logically correct sampling. Bucketing also aids in doing efficient map-side joins etc.

### Why use bucketing

1. Bucket Sampling : e.g SELECT * FROM T_USER_LOG_BUCKET TABLESAMPLE (BUCKET 1 OUT OF 4 AT USER_ID).... It will select the data from the first buckets of each partition from T_USER_LOG_BUCKET table otherwise in normal select we can't select data in such distributed and evenly manner.

2. Optimize Join Operation: We can optimize the join by bucketing the similar column values in one bucket so that during bucket to bucket join, hive can minimize the processing steps and reduce the data needed to parse and compare for join.
