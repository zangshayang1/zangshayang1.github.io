---
layout: post
title:  CommandLine Notes
date:   2018-05-13 01:08:00 +0800
categories: reference
tag: CommandLine
---

* content
{:toc}



Last Update: 2018-05-13

## Maven
```shell
# Forces a check for missing releases and updated snapshots on remote repositories
> mvn clean install -U

# Package using spark20 mvn build profile
> mvn package -P spark20

# resume mvn clean verify from where it fails
> mvn clean verify -rf :[module name]

# manually install mvn plugin
> mvn install:install-file -Dfile=/Users/szang/Downloads/formatter-maven-plugin-2.0.1.jar -DgroupId=net.revelc.code.formatter -DartifactId=formatter-maven-plugin -Dversion=2.0.1 -Dpackaging=jar

# install compile time dependency and install self modules before running test
> mvn clean install -DskipTests=true
> mvn clean test

# compile only beam-sdks-java-core module in beam project
> mvn --projects :beam-sdks-java-core clean install
```

## Java
```shell
# check if my first UDF is actually built in the jar
jar -tf xxxx.jar | grep [udf_mainClass]

# how to find java home
> /usr/libexec/java_home -v 1.8

# How to pass arguments to JVM vs JavaMainProgram in case of using spark-submit
# -Xms: init memory pool; -Xmx: maximum memory pool
> spark-submit --class className -Xms256m -Xmx1g some.jar arg0 arg1 ..

# Run java using java -jar VS using java -cp
> java -cp target/xxxx.jar mainClass [options]
> java -jar target/xxxx.jar [option]
# NOTES: xxxx.jar file is an archived collection of all the *.class required to run the program, which you can unzip and find mainEntry class file, library class files and all the metadata.
# NOTES: using -cp simply tells JVM to load all the class files at that location and start the program from given 'mainClass'.
# NOTES: using -jar invokes looking for library class file locations and 'mainClass' configured in META-INF/MANIFEST.MF.

```


## Mac issues
```shell
# reboot camera
> sudo killall VDCAssistant

# reboot sound
> ps aux | grep 'coreaudio[d]' | awk '{print $2}' | xargs sudo kill
```


## Vim
1. hjkl navigation  

2. web navigation  

3. under navigation mode, 30i-Esc creates 30 continuous '-'.  

4. beginning of the line: 0; end of the line: $.  

5. beginning of the file: gg; end of the file: G; x line of the file: xG.  

6. search for pattern: /pattern, with n points to the next  occurrence and N points to the previous one.  

7. start a newline below cursor: o; start a newline above cursor: O.  

8. delete the line: dd; delete the word forward: de; delete the word backward: db.  

9. repeat the previous command: .  

10. undo: u.  

11. place your cursor and try the following in navigation mode: v e d p => visual mode, select, delete(also copy), paste.  


## Network
```shell
# mtr comibnes ping and traceroute
> mtr [host] -rw -c 50

# keep pinging the host for up to 50 times in quiet mode
> ping [host] -c 50 -q

# try to connect to the host using telnet protocol see if the connect is through or there is any firewall blocking
> telnet [host] [port]

# see what is going on on this port
> ps -ef | grep 7600

# kill whatever occupies the port
> ps -ef | grep 7607 | cut -d ' ' -f 2 | xargs kill

# map domain name [myaws.io] to IP locally
> sudo vim /etc/hosts # aa.bbb.ccc.ddd myaws.io
```


## Daily Use
```shell
# manage third-party softwares installed via homebrew by:
> brew services list/start/stop...

# alias ll
> alias ll="ls -lhG"

# list at reverse order of last modified time
> ls -lrt

# short cut for ps aux | grep
> pgrep 

# jvm process
> jps
```


### Search
```shell
# do a 5-depth search from root dir / and list files whose names contain "master"
> find / -maxdepth 5 -name "*master*"

# list open the net IP address and Port number of open files; then find a match.
> lsof -n -P | grep 9000
```


### Batch Operation
```shell
# batch copy from list
> cat copy.list | xargs -iX cp X /target/dir

# batch mv of the same type recursively
> find . -name "*.jpg" -exec mv {} /targetDir  \;
```


## SSH
```shell
# keep remote login alive
> ssh -o ServerAliveInterval=60 [host]

# generate public/private rsa key pair without password specified
> ssh-keygen -t rsa -P ""

# set authorization for public keys
> cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys
> chmod 700 ~/.ssh
> chmod 600 ~/.ssh/authorized_keys
```


## Hadoop
```shell
# list running apps on yarn
> yarn application -list

# kill app on yarn
> yarn application -kill application_id
```


## Other
```shell
# won't return anything because of the "$", you need to put the backslash in front of the "$"
> grep "insert overwrite table ${workingDB}.dw_users_info_prev_day_w" *

```


## Spark Shell
```shell
# list all declared variables in spark-shell
$intp.allDefinedNames # it is an interpret variable
```


## SpringBoot
```shell
# How to run spring-boot app locally using spring-boot:run maven goal
> mvn spring-boot:run -Dspring.profiles.active=local
```

### SpringBoot Notes
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
