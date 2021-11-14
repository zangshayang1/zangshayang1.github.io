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

### Unique Concept

Virtual Host - A machine can start multiple vitual hosts, each of which is an isolated RabbitMQ virtual host.

Channel - a lightweight data exchange channel on top of TCP connection

Exchange - routing component in front of message queues.

* direct routing by exact-match key
* topic routing by fuzz-match key
* fannout distribution by subscription
* headers routing by kv pairs (exact match any of the pairs) defined in HTTP request headers.

### Availability

Mirror Queue with 3 modes

* all - one replica on each node across the cluster
* exactly - make exactly X number of replicas distributed across the cluster
* nodes - store replica(s) on specified node(s)

### Persistence & Memory Management

__Persistence__

* Persistent messages will be flushed to disk once received. 

* Non-persistent messages will be flushed to disk once in a while when allocated memory reached "paginatioin" size.

__Memory Management__

* Use `vm_memory_high_watermark.relative=0.4` to limit RabbitMQ's memory consumption to be 40% of system memory.
* By default, when memory consumption reached 50% of the allocation limit, in this case, 20% of system memory, content in the memory will be flushed to disk. 
* When memory consumption reached 100% of the allocation limit, it will stop accepting new messages. 

### Architecture

* Module layer
  * provide client facing API
* Session layer
  * improve reliability (synchronization mechanism and error handling)
  * convert client message to binary stream compatible with the Transport layer
* Transport layer
  * handling binary data stream io
  * provide error handling

### Reliability

__General Message Delivery Mode__

`Producer -> RabbitMQ Broker -> Exchange -> Queue -> Consumer`

__Producer__

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

__Confirm Callback Implementation__

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

__Return Callback Implementation__

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

__Consumer__

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



### DLQ

Cases when a message will go into DLQ in RabbitMQ

* when a message is failed to be consumed
* queue size is over the limit
* message is expired (TTL)



