---
layout: post
title:  Spring Boot
date:   2018-05-27 22:08:00 +0800
categories: Study-Notes
tag: Springboot
---

* content
{:toc}


## NOTES

__How @service handle concurrent request__  
spring framework的官方文档并没有具体讨论，Service是怎样的一个scope，面对concurrent requests，有怎样的behavior。但Stackoverflow说，Service是Singleton scope，即只有一个instance，在JVM heap里面shared by all threads。其本身并没有thread-safe的要求，但只要设计成 stateless的，就可以多线程处理并发请求，没有问题。当不同线程通过service，call一个method的时候，他们会有各自的executionContext，只要service自身的state不变，就是safe的。Performance应该跟多个instances是一样的。  
[sping framework bean scope](https://docs.spring.io/spring/docs/current/spring-framework-reference/core.html#beans-factory-scopes-singleton)  
[stackoverflow-1](https://stackoverflow.com/questions/25617962/how-does-the-singleton-bean-serve-the-concurrent-request)  
[stackoverflow-2](https://stackoverflow.com/questions/15745140/are-spring-objects-thread-safe)  


__How @JsonAutoDetect & @JsonIgnoreProperties(ignoreUnknown=true) helps mapping a POJO to JSON and backward__  
In Spring, a POJO can be serialized to JSON and backward, only when getter() and setter() are defined for each private field.  
When the POJO doesn't satify the condition, annotate it with @JsonAutoDetect.   
When you want deserialize a JSON to the POJO but the JSON can be wrong or missing information, use @JsonIgnoreProperties.   

__Understand corePoolSize, maxPoolSize and queueCapacity__  
1. When a new request comes in, the server will start a new thread to take care of the request till the number of threads equals to __corePoolSize__;  
2. If more requests come in, the server will put the requests in queue till the queue is filled up;  
3. If more requests come in, the server will create a new thread up to maxPoolSize;
4. If more requests come in, the server will reject them.
