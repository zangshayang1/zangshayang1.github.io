## SPARK-SCALA-DATAMOUNT

### mount data from S3 to Databricks bucket and show:
dbutils.fs.mount("s3a://"+ ACCESSY_KEY_ID + ":" + SECERET_ACCESS_KEY + "@" + bucket,mount_folder)
display(dbutils.fs.mounts())

### read data, show sample data (20), display data schema (columns and types) and total count of records
val pagecountsEnAllDF = spark.read.parquet("/mnt/wikipedia-readonly/pagecounts/staging_parquet_en_only/")
pagecountsEnAllDF.show(20)
pagecountsEnAllDF.printSchema
pagecountsEnAllDF.count()

## SPARK-SCALA-PAGECOUNT

### transformation
val pagecountsEnAllDFSumDF = pagecountsEnAllDF
  .select("project", "requests")   // transformation
  .groupBy("project")              // transformation
  .sum()                           // transformation

pagecountsEnAllDFSumDF
  .orderBy("sum(requests)")        // transformation
  .show()                          // action

### equal test
val pagecountsEnWikipediaDF = pagecountsEnAllDF
  .filter($"project" === "en")

### count() and show() are actions while groupBy(), orderBy(), filter() ... are transformations
### transformations return dataframe and they can only be triggered by action.
### but display() can render dataframe.
pagecountsEnWikipediaDF
  .orderBy($"requests".desc)   // transformation
  .show(25, false)             // action

val mostPopularEnWikipediaDF = pagecountsEnWikipediaDF
  .orderBy($"requests".desc)
  .limit(25)

display(mostPopularEnWikipediaDF)

### filter() on column object - google spark column recommended - with 1. regex-rlike; 2.sql-like; 3. column.startswith() method.
val pagecountsEnWikipediaArticlesOnlyDF = pagecountsEnWikipediaDF
  .filter($"article".rlike("""^((?!Special:)+)"""))

val pagecountsEnWikipediaArticlesOnlyDF3 = pagecountsEnWikipediaDF
  .filter(!$"article".like("Special:%"))

val pagecountsEnWikipediaArticlesOnlyDF2 = pagecountsEnWikipediaDF
  .filter(!$"article".startsWith("Special:"))

### collect a list of selected records for further process
val articles = pagecountsEnWikipediaArticlesOnlyDF
 .filter($"article".startsWith("Apache_"))
 .orderBy($"requests".desc)
 .collect()

for (article <- articles) {
  println(">> " + article)
}

### sql functions to collect a list of selected records
### get row object using .first() and get primitive long type value using .getLong(0) with the position for desire value specified.
import org.apache.spark.sql.functions._
val mediaTotal = pagecountsEnWmDF
  .select( sum($"requests") )
  .first()
  .getLong(0)


## SPARK-SCALA-PAGEVIEWS

### load data: 1. use first row as header; 2. infer data types; 3. set deliminater
val pageviewsDF = spark.read
  .option("header", "true")
  .option("inferSchema", "true")
  .option("delimiter", "\t")
  .csv("/mnt/wikipedia-readonly/pageviews/pageviews_by_second.tsv")

### check the number of partitions in a dataframe by: 1. convert df to rdd; 2. get array of partitions; 3. size().
pageviewsDF.rdd.partitions.size

### check the number of cores in the current cluster
spark.conf.get("spark.master")

### load data into memory of local cluster, otherwise data will be read and partitioned from remote storage every time an action is called.
### but .cache() is a lazy operation which only takes effect with an action.
df.cache()
df.count()

### Query Plan Optimizer
df.filter($"someColumn_name" === "someValue").count()
// the way it is done is through SparkSQL query optimizer - catalyst optimizer - you can check details by:
df.filter($"someColumn_name" === "someValue").explain(true)



## SPARK-SCALA-INTERNALS

https://databricks.com/blog/2015/06/22/understanding-your-spark-application-through-visualization.html



For a spark job, data is partitioned to #(dataSize / blockSize) partitions spreaded across a cluster of executors.
When the job is triggered by an action, scheduler starts to optimize the execution plan:
 1. the job will be chopped into multiple stages (according to SHUFFLEs needed for all kinds of operations in the job).
 2. in each stage, one task will be created for each partition across the cluster and each processor will execute one task a time so they can be parallelized
 3. SHUFFLE will be done when all the preceding tasks are finished and then the next batch of parallelizable tasks will kick off.
For example, if we have 10 partitions of data and 2 executors with 2 processors in each executor.
 Ideally, each executor will take 5 partitions, and 2 tasks can be executed in parallel because there are 2 processors in each executor.
 For a mapping operation, 10 tasks will be created, 2 tasks can be parallelized in each executor so 4 tasks in total across the mini-cluster.
 It will take 3 batches to finish all 10 tasks and then SHUFFLE will start.
 For a reducing operation, a certain number of tasks will be created according to the output SHUFFLE operation.
"A stage corresponds to a collection of tasks that all execute the same code, each on a different subset of the data. Each stage contains a sequence of transformations that can be completed without shuffling the full data." (http://blog.cloudera.com/blog/2015/03/how-to-tune-your-apache-spark-jobs-part-1/)

### Configure partition size during SHUFFLE

According to the above, a job execution can be accelerated if the number of partitions is a multiple of the number of processors. This is not the case in the above example, what can we do? "At each stage boundary, data is written to disk by tasks in the parent stages and then fetched over the network by tasks in the child stage. Because they incur heavy disk and network I/O, stage boundaries can be expensive and should be avoided when possible. The number of data partitions in the parent stage may be different than the number of partitions in the child stage. Transformations that may trigger a stage boundary typically accept a numPartitions argument that determines how many partitions to split the data into in the child stage."(http://blog.cloudera.com/blog/2015/03/how-to-tune-your-apache-spark-jobs-part-1/)

## SPARK-SCALA-PAGEVIEWS-2

### set up a schema and use it, which will be faster than using "inferSchema"(2 passes)

import org.apache.spark.sql.types._

val schema = StructType(
  List(
    StructField("timestamp", StringType, true),
    StructField("site", StringType, true),
    StructField("requests", IntegerType, true)
  )
)

val initialDF = spark.read
  .option("delimiter", "\t")
  .option("header", "true")
  .schema(schema)
  .csv("dbfs:/databricks-datasets/wikipedia-datasets/data-001/pageviews/raw/pageviews_by_second.tsv")

### Spark SQL Functions to Extract Timestamp Info
import org.apache.spark.sql.functions.dayofyear

val pageviewsByDayOfYearDF = pageviewsDF
 .groupBy(dayofyear($"timestamp")
 .alias("DayOfYear"))
 .sum()
 .orderBy($"DayOfYear".desc)

### Spark Databricks base R's Transformation to Add Column

val pageviewsForDay110 = pageviewsDF
 .withColumn("DayOfYear", dayofyear($"timestamp"))
 .filter($"DayOfYear" === 110)
 .orderBy("timestamp", "site")

### GroupBy outputs a GroupedData object which can then be passed into Agg.
dfCleaned.select($"article", $"views", date_format($"timestamp", "MM-W").as("month-week"))
         .groupBy($"article", $"month-week")
         .agg(sum($"views"))
         .withColumnRenamed("sum(views)", "total-views")

## SPARK-SCALA-CLICKSTREAM

### Move From DF to SQL
clickstreamDF2.createOrReplaceTempView("clickstream") // create a table from df
display( spark.sql("DESC clickstream") ) // DESC - describe clause

## SPARK-SCALA-RDD-DF-DATASET

### Create RDD from file.gz
val pagecountsRDD = sc.textFile("dbfs:/mnt/wikipedia-readonly/pagecounts/staging/")
// automatically handle the file with suffix gz

### Create Dataset from file.gz
val pagecountsDS = sqlContext.read.text("dbfs:/mnt/wikipedia-readonly/pagecounts/staging/").as[String]
// different from creating RDD

### Assign Data to a Storage Level
import org.apache.spark.storage.StorageLevel._
pagecountsRDD.setName("pagecountsRDD").persist(MEMORY_AND_DISK).count // cache() is done implicitly
pagecountsDS.persist(MEMORY_AND_DISK).count
"""
Worthnoting here, RDD takes much more space in memory than DataFrame/DataSet because the former is represented as Java Object managed by JVM MEMORY MGR while the latter goes through a SPARK MEMORY MGR(SMM).
From a high level, SMM exploits the fact that Spark knows each computation stage and how data flow, which gives more control over garbage collection.
From a midlle level, SMM explots the fact that Spark knows the type of each column in DataFrame and the parametric type of a DataSet.
From a low level, SMM converts most Spark operations to operate directly against binary data rather than Java objects.
This is built on sun.misc.Unsafe, an advanced functionality provided by the JVM that exposes C-style memory access.
"""
pagecountsRDD.unpersist()

### Parse RDD
// Without parsing, there are all strings in RDD.
// Define a parsing function that takes in a line string and returns the 4 fields on each line, correctly TYPED!
def parse(line:String) = {
  val fields = line.split(' ') //Split the original line with 4 fields according to spaces
  (fields(0), fields(1), fields(2).toInt, fields(3).toLong) // return the 4 fields with their correct data types
}
val pagecountsParsedRDD = pagecountsRDD.map(parse)

### Filter RDD
val enPagecountsRDD = pagecountsParsedRDD.filter { case (project, _ , _ , _ ) => project == "en" }

// Collect all RDDs
enPagecountsRDD.collect().foreach{println}
// sample 10 random RDDs
enPagecountsRDD.takeSample(true, 10).foreach(println)

### Map
val kvPairRDD = pagecountsParsedRDD.map { case (project, _ , request , _ ) => (project, request) }
val kvPairRDD = pagecountsParsedRDD.map( x => (x._1, x._3))
val kvPairDS = pagecountsParsedDS.map { case (project, _ , request , _ ) => (project, request) }
val kvPairDS = pagecountsParsedDS.map( x => (x._1, x._3))

### .map() VS .foreach()
// Major difference is .foreach() doesn't return anything while .map() does.
enPagecountsRDD.take(5).foreach( tuple => println(tuple._3) )
val something = enPagecountsRDD.map(tuple => tuple._3)

### DataSet Operations VS DataFrame Operation

-------- DataSet ---------
// Start by creating (key, value) pairs from the project prefix and the number of requests.
val kvPairs = enPagecountsDS8Partitions.map { case (project, _, requests, _) => (project, requests) }
// Next, use the typed transformation (i.e., Dataset transformation) groupByKey().
val grouped = kvPairs.groupByKey { case (key, value) => key }
// "grouped" is a KeyValueGroupedDataset. We can use the reduceGroups() method on that object.
val sums = grouped.reduceGroups { (keyValue1, keyValue2) =>
  // Each argument is of the form: (key, value). We want to sum the corresponding values.
  val (key, value1) = keyValue1  // Scala extractor
  val (_, value2) = keyValue2

  // Return the key again, with the summed values.
  (key, value1 + value2)
}
// Finally, extract just the keys and values and print.
sums.map { case (key, (keyAgain, value)) => (key, value) }
    .take(10)
    .foreach { case (project, totalRequests) =>
      println(s"""Project "$project" saw $totalRequests requests during the hour.""")
    }

-------- DataFrame ---------
enPagecountsDS8Partitions
      .map { case (project, _, requests, _) => (project, requests) }
      .toDF()
      .groupBy($"_1")
      .agg(sum("_2") as "totalRequests")
      .orderBy($"totalRequests".desc)
      .take(10)
      .foreach { row =>
         println(s"""Project "${row(0)}" saw ${row(1)} requests during the hour.""")
      }


Before using .toDF()
      val sqlContext = //create sqlContext

      import sqlContext.implicits._

      val df = RDD.toDF()
