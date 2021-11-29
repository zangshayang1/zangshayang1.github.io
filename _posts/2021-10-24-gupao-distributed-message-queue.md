---
layout: post
title: GuPao - Distributed Message Queue
date:   2021-10-24 18:30:00 -0700
categories: reference
tag: java
---



* content

{:toc}





Last Update: 11-13-2021



# Why Message Queue? 

In a distributed system, components talk to each other following some protocol. They can be REST/RPC/MQ. 

__There are different type of message queue protocols, such as__

1. AMQP advanced message queue protocol - high reliability
2. MQTT message queuing telemetry transport - lightweight, written in C, doesn't support persistence
3. OpenMessage - from Chinese community, used by Apache RocketMQ
4. kafka -> tcp based binary protocol, support big data volume

__Each of these protocols have different implementations__

1. RabbitMQ implments AMQP, born from financial industry highlighting reliability. 

__Five core concepts when it comes to message queue implementations__

1. protocol
2. persistence mechanism - RabbitMQ, Kafka, RocketMQ all use file system as their persistent layer
3. distribute mechanism (push / pull)
4. reliability
5. availability



# RabbitMQ

![]({{ '/styles/images/gupao-distributed-message-queue/rabbitmq-concepts-overview.png' | prepend: site.baseurl }})

## Unique Concept

Virtual Host - A machine can start multiple vitual hosts, each of which is an isolated RabbitMQ virtual host.

Channel - an abstract lightweight data exchange channel on top of TCP connection

Exchange - routing component in front of message queues.

* direct routing by exact-match key
* topic routing by fuzz-match key
* fannout distribution by subscription
* headers routing by kv pairs (exact match any of the pairs) defined in HTTP request headers.

## Architecture

* Module layer
  * provide client facing API
* Session layer
  * improve reliability (synchronization mechanism and error handling)
  * convert client message to binary stream compatible with the Transport layer
* Transport layer
  * handling binary data stream io
  * provide error handling

## Persistence & Memory Management

__Persistence__

* Persistent messages will be flushed to disk once received. 

* Non-persistent messages will be flushed to disk once in a while when allocated memory reached "paginatioin" size.

__Memory Management__

* Use `vm_memory_high_watermark.relative=0.4` to limit RabbitMQ's memory consumption to be 40% of system memory.
* By default, when memory consumption reached 50% of the allocation limit, in this case, 20% of system memory, content in the memory will be flushed to disk. 
* When memory consumption reached 100% of the allocation limit, it will stop accepting new messages. 

## Availability

Mirror Queue with 3 modes

* all - one replica on each node across the cluster
* exactly - make exactly X number of replicas distributed across the cluster
* nodes - store replica(s) on specified node(s)

## Reliability

__General Message Delivery Mode__

`Producer -> RabbitMQ Broker -> Exchange -> Queue -> Consumer`

### Producer

On the producer side, a `ConfirmCallbackService` and a `ReturnCallbackService` will be registered. 

* `ConfirmCallbackService` will be invoked by RabbitMQ Broker to indicate delivery success or failure.
*  `ReturnCallbackService` will be invoked if a message fails to be routed from anb exchange to a queue. 

```java
@RunWith(SpringRunner.class)
@SpringBootTest(classes = RabbitmqApplication.class)
public class Producer {
  
  @Autowired
  private RabbitTemplate rabbitTemplate;
  @Autowired
  private ConfirmCallbackService confirmCallbackService;
  @Autowired
  private ReturnCallbackService returnCallbackService;

  @Test
  public void test() {
    /**
    * 确保消息发送失败后可以重新返回到队列中
    */
    rabbitTemplate.setMandatory(true);
    /**
    * 消息投递到队列失败回调处理
    */
    rabbitTemplate.setReturnCallback(returnCallbackService);
    /**
    * 消息投递确认模式
    */
    rabbitTemplate.setConfirmCallback(confirmCallbackService);
    //发送消息
    rabbitTemplate.convertAndSend("confirmTestExchange", "info",
    "hello,ConfirmCallback你好");
  }
}
```

#### Confirm Callback Implementation

```java
package com.rabbitmq.config;

import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
* 实现ConfirmCallback接口
*/
@Component
public class ConfirmCallbackService implements
  RabbitTemplate.ConfirmCallback {
    /**
    * @param correlationData 相关配置信息
    * @param ack exchange交换机 是否成功收到了消息。true 成功，false代表失败
    * @param cause 失败原因
    */
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
      if (ack) {
	      //接收成功
  	    System.out.println("成功发送到交换机<===>");
      } else {
    	  //接收失败
      	System.out.println("失败原因:===>" + cause);
	      //TODO 做一些处理:消息再次发送等等
    }
  }
}
```

#### Return Callback Implementation

```java
package com.rabbitmq.config;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class ReturnCallbackService implements RabbitTemplate.ReturnCallback {
  /**
  * @param message 消息对象
  * @param replyCode 错误码
  * @param replyText 错误信息
  * @param exchange 交换机
  * @param routingKey 路由键
  */
  @Override
  public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
    System.out.println("消息对象===>:" + message);
    System.out.println("错误码===>:" + replyCode);
    System.out.println("错误信息===>:" + replyText);
    System.out.println("消息使用的交换器===>:" + exchange);
    System.out.println("消息使用的路由key===>:" + routingKey);
    //TODO ===>做业务处理
  }
}
```

---

### Consumer

On the consumer side, there are 3 `AcknowledgeMode`

* NONE
* AUTO
* MANUAL

Enabling AcknowledgeMode.MANUAL maximizes system reliability. 

```java
@Component
@RabbitListener(queues = "confirm_test_queue")
public class Consumer {
  
  @RabbitHandler
  public void processHandler(String msg, Channel channel, Message message) throws IOException {
    long deliveryTag = message.getMessageProperties().getDeliveryTag();
    
    try {
      System.out.println("消息内容===>" + new String(message.getBody()));
      //TODO 具体业务逻辑
      //手动签收[参数1:消息投递序号,参数2:批量签收]
      channel.basicAck(deliveryTag, true);
    } catch (Exception e) {
	    //拒绝签收[参数1:消息投递序号,参数2:批量拒绝,参数3:是否重新加入队列]
  	  channel.basicNack(deliveryTag, true, true);
    }
  }
}
```

## DLQ

Cases when a message will go into DLQ in RabbitMQ

* when a message is failed to be consumed
* queue size is over the limit
* message is expired (TTL)



# Kafka

Previous [Kafka Anatomy](https://zangshayang1.github.io/study-notes/2018/06/22/kafka-anatomy/) 

Different from traditional message queue, Kafka supports large volume of concurrent requests (> 10k/s) as well as dynamic scaling, in terms of number of topics and cluster size. Kafka stores data in disk. Sequential access pattern allows for good READ performance. Sequential access speed on a commodity HDD is close to random access speed on a SSD (500MB/s). For each topic, WRITE pattern is the following

* If a partition is specified in the record, use it
* If no partition is specified but a key is present choose a partition based on a hash of the key
* If no partition or key is present choose the sticky partition that changes when the batch is full.

Reference: https://github.com/apache/kafka/blob/trunk/clients/src/main/java/org/apache/kafka/clients/producer/internals/DefaultPartitioner.java

## Unique Concept

__Partition__

* a partition is the smallest unit of parallelism
* number of partitions are usually greater or at least equal to the number of consumers in a consumer group
* a partition is a scaling factor, higher number of partitions will allow higher number of parallelism (low-latency, high throughput) 
* when the number of partitions is greater or equal to the number of different keys ever possible in a topic, they make a __GroupMsgByKey__ separation 

__Offset__ 

* mark where the next consuming starts from
* per topic per partition per consumer group
* more details see [Maintain Offset](#Maintain-Offset).

__Replica__

* Each partition has a configurable number of replica(s).

* Leader replica accepts WRITE data, synchronize data to follower replicas. Follower replica serve data to READ request.
* If leader replica is unavailable, new leader will be elected from followers.
* Replica strategy is adopted to ensure high availability. It only makes sense when no two identical replicas reside in the same broker.

* In-sync Replica (ISR)  is the replica(s) that most closely follow leader

__Record__

* key - distribution routing key
* value - payload
* timestamp - retention

### Characteristics

* Low latency

* High throughput

* High availability

### Use Case

* Click stream data collection

* Log stream data collection

## Kafka Deployment

### Notes On Zookeeper Deployment

```tex
# ./conf/zoo_sample.cfg

tickTime=2000
dataDir=/var/zookeeper/data
dataLogDir=/var/zookeeper/logs
clientPort=2181
initLimit=5
syncLimit=2
server.1=zoo1:2888:3888
server.2=zoo2:2888:3888
server.3=zoo3:2888:3888
```

#### Configurations

* The entry __initLimit__ is timeouts ZooKeeper uses to limit the length of time the ZooKeeper servers in quorum have to connect to a leader. 

* The entry __syncLimit__ limits how far out of date a server can be from a leader.

* With both of these timeouts, you specify the unit of time using __tickTime__. In this example, the timeout for initLimit is 5 ticks at 2000 milleseconds a tick, or 10 seconds.

* The entries of the form *server.X* list the servers that make up the ZooKeeper service. 

* When the server starts up, it knows which server it is by looking for the file __myid__ in the data directory. That file has the contains the server number, in ASCII. Manually creating the __myid__ file is necessary during initial setup. 

Finally, note the two port numbers after each server name: "2888" and "3888". Peers use the former port to connect to other peers. Such a connection is necessary so that peers can communicate, for example, to agree upon the order of updates. More specifically, a ZooKeeper server uses this port to connect followers to the leader. When a new leader arises, a follower opens a TCP connection to the leader using this port. Because the default leader election also uses TCP, we currently require another port for leader election. This is the second port in the server entry.

If you want to run standalone mode for evaluation, specify the servername as __localhost__ with unique quorum & leader election ports (i.e. 2888:3888, 2889:3889, 2890:3890 in the example above). Of course separate __dataDir__ and distinct __clientPort__ are also necessary (in the above replicated example, running on a single __localhost__, you would still have three config files).

__Reference__: [ZK Official Start Guide](https://zookeeper.apache.org/doc/current/zookeeperStarted.html)

#### Frequently Used Commands

```shell
# start a zk node
./bin/zkServer.sh start

# check zk status
./bin/zkServer.sh status
```

---

### Notes On Kafka Deployment

```tex
# ./config/server.properties

# minimum set of configurations that need to be modified to set up a Kafka cluster
broker.id=0
listeners=PLAINTEXT://localhost:9092
log.dirs=/data/kafka/kafka-logs
zookeeper.connect=localhost:2181,192.168.18.24:2181,192.168.18.27:2181
```

#### Configurations

* The entry __broker.id__ is similar to __myid__ in Zookeeper.

* The entry __listeners__ specify local host ip and port.

* The entry __log.dirs__ is self-explaintory. 
* The entry __PLAINTEXT__ is the default communication protocol, it can be HTTP, HTTPS, SSL, etc.

* The entry __zookeeper.connect__ lists all the zk nodes to ensure the maximum availability.

When deploying Kafka in cluster mode, the above configurations need to be provided in each broker. __logs.dirs__ need to be created before starting each broker node. When deploying Kafka in standalone mode, listeners=PLAINTEXT://localhost:9092, PLAINTEXT://localhost:9093, PLAINTEXT://localhost:9094. 

#### Frequently Used Commands

```shel
# start kafka broker after ZK has been deployed
./bin/kafka-server-start.sh config/server.properties

# create topic
./bin/kafka-topics.sh --create --topic test --bootstrap-server localhost:9092 --partitions 18 --replication-factor 3

# describe topic
./bin/kafka-topics.sh --describe --topic test --bootstrap-server localhost:9092

# establish a producer session
./bin/kafka-console-producer.sh --topic test --bootstrap-server localhost:9092

# establish a consumer session
./bin/kafka-console-consumer.sh --topic test --bootstrap-server localhost:9092 --offset earliest/latest
```

## Interceptor & Partitioner

### Producer

Producer will cache the messages to be sent locally up to a configurable   `batch.size=16k`  or configurable timeout  `linger.ms=5`, and have a background IO thread to flush the data to kafka topics once in a while for performance benefit. Producer can be bootstrapped by connecting to any broker in a Kafka cluster because Kafka maintains a piece of logic (currently inside of KafkaController) that redirects WRITE requests from a producer to the correct broker where the leader partition of a topic resides. 

### Interceptor

```java
public class Producer {

    public static void main(String[] args) throws Exception {

        Properties props = new Properties();
        props.put("bootstrap.servers", "192.168.100.249:9092");
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        // Add interceptors in a chain
        List<String> interceptors = new ArrayList<>();
        interceptors.add("com.study.kafka.interceptor.TimeInterceptor");
        interceptors.add("com.study.kafka.interceptor.CounterInterceptor");
        props.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, interceptors);

	      // Send
        String topic = "test";
        Producer<String, String> producer = new KafkaProducer<>(props);
        for (int i = 0; i < 10; i++) {
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, "message" + i);
            producer.send(record);
        }

        // Closing a producer will also invoke close() hook defined in a interceptor
        producer.close();
    }
}
```

```java
package com.study.kafka.interceptor;

/*
* Add a timestamp prefix in the record value.
*/
public class TimeInterceptor implements ProducerInterceptor<String, String> {

    @Override
    public void configure(Map<String, ?> configs) {}

    @Override
    public ProducerRecord<String, String> onSend(ProducerRecord<String, String> record) {
      	// an input record comes with designated topic/partition/timestamp
      	// it's given a chance to change its designated topic/partition/timestamp
        return new ProducerRecord(
	          record.topic(),
  	        record.partition(), 
    	      record.timestamp(), 
      	    record.key(),
        	  System.currentTimeMillis() + "," + record.value().toString());
    }

    @Override
    public void onAcknowledgement(RecordMetadata metadata, Exception exception) {}

    @Override
    public void close() {}
}
```

```java
package com.study.kafka.interceptor;

public class CounterInterceptor implements ProducerInterceptor<String, String> {
    private int errorCounter = 0;
    private int successCounter = 0;

    @Override
    public void configure(Map<String, ?> configs) {}

    @Override
    public ProducerRecord<String, String> onSend(ProducerRecord<String, String> record) {
        return record;
    }

    @Override
    public void onAcknowledgement(RecordMetadata metadata, Exception exception) {
      	// Maintain two global variables over the course.
        if (exception == null) {
            successCounter++;
        } else {
            errorCounter++;
        }
    }

    @Override
    public void close() {
      	// Reveal them at closing.
        System.out.println("Successful sent: " + successCounter);
        System.out.println("Failed sent: " + errorCounter);
    }
}
```

---

### Partitioner

```java
package com.study.kafka.partition;

public class MyPartitioner implements Partitioner {
    private final AtomicInteger counter = new AtomicInteger(new Random().nextInt());
    private Random random = new Random();

    @Override
    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
        List<PartitionInfo> partitioners = cluster.partitionsForTopic(topic);
        int numPartitions = partitioners.size();
      	// redistribute records
        int res;
        if (keyBytes == null) {
            res = random.nextInt(numPartitions);
        } else {
            res = Math.abs(key.hashCode()) % numPartitions;
        }
        return res;
    }

    @Override
    public void close() {}

    @Override
    public void configure(Map<String, ?> map) {}
}
```

## Maintain Offset 

Before version 0.9, kafka relies on ZK to store "consumer-group-to-topic-to-partition" mapping metadata plus committed offset data (per topic, per partition, par consumer group). The structure looks like the following:

```tex
zk_host://consumers/{group_id}/offsets/{topic}/{broker_id-partition_id} -> offset
```

But this solution posed a scalability issue because ZK is not good at concurrent writing. 

Why not using Redis? Compared to ZK, Redis offers better performance but compromises on consistency. 

Starting from version 0.9, kafka stores committed offset data to an internal topic `__consumer_offset` with default 50 partitions.

```tex
[consumer_group,test_topic,11]::[OffsetMetadata[55166421,NO_METADATA],CommitTime 1502060076305,ExpirationTime 1502146476305]
```

`GroupCoordinator` and `GroupMetadataManager` are involved in 

* appending consumed offsets from consumer groups 
* providing latest offsets to consumer groups

If a consumer group is started after the offset expiration time, the coordinator won’t find any offsets and Kafka will rely on the  `auto.offset.reset` property to know if it needs to start from `earliest` or `latest`. 

![]({{ '/styles/images/gupao-distributed-message-queue/kafka-group-coordinator.png' | prepend: site.baseurl }})

_Reference_ 

* [_https://jaceklaskowski.gitbooks.io/apache-kafka/content/kafka-coordinator-group-GroupCoordinator.html_](https://jaceklaskowski.gitbooks.io/apache-kafka/content/kafka-coordinator-group-GroupCoordinator.html)
* [_https://www.sderosiaux.com/articles/2017/08/07/looking-at-kafka-s-consumers-offsets/#in-code-groupcoordinator-and-groupmetadatamanager_](https://www.sderosiaux.com/articles/2017/08/07/looking-at-kafka-s-consumers-offsets/#in-code-groupcoordinator-and-groupmetadatamanager)

## Data Persistence

A partition directory on disk stores `index files, log files, timeindex files, snapshot files, leader-epoch-checkpoint files, partition.metadata files` __for each data segment__. When a data segment reaches its configurable size upper bound, a new segment will be created to store incoming data.

![]({{ '/styles/images/gupao-distributed-message-queue/kafka-segment.png' | prepend: site.baseurl }})

### WRITE

Writing is done in a appending mode so that

* READ operation would never conflict with WRITE operation
* look up operation can be optimized based on sequential data structure

Its performance is improved by adopting __page cache__ and __RAID__ (Redundant Array of Independent Disks). A well balanced RAID provides 600MB/s disk IO throughput by parallelizing the task while a single SSD has ~400MB/s. 

### READ

__Given an offset, how to locate the message?__

An index entry is a pair of (consumer offset, physical offset). 

A physical offset can be located with a consumer offset. A consistent view of active segment blocks _([sparse indexing](https://en.wikipedia.org/wiki/Database_index#Sparse_index))_ is maintained to provide quick global look up.

![]({{ '/styles/images/gupao-distributed-message-queue/kafka-read-by-index.png' | prepend: site.baseurl }})

The right __message__ can be located according to the physical offset. 

A __message__ contains

* 8-byte physical offset
* 4-byte message size
* 4-byte CRC32 check sum
* 1-byte protocol version
* 1-byte kafka version
* 4-byte key length
* K-byte key (configurable)
* flexible sized value

## Scalability

__How to compute the number of partitions needed? Re-partition is NOT recommended.__

Partitions = max(Traffic TPS / Producer TPS, Traffic TPS / Consumer TPS)

_Traffic: theoretical traffic that can be generated by the system._

__Commonly used upperbounds per cluster__

* 30,000 partitions
* 2,000 topics



# RocketMQ
