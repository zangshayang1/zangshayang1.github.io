---
layout: post
title: Docker on AWS EC2
date: 2018-05-09 21:00:00 -0700
categories: other-how-to
tag: docker
---
* content
{:toc}


## Install and Start Docker on EC2

```shell
> sudo yum install -y docker;
> sudo service docker start;
> sudo usermod -a -G docker ec2-user; # add this host to docker group
```

__NOTE:__
1. this ec2 is configured as Amazon Linux based AMI, which uses yum as package manager instead of apt-get.  
2. yum is a wrapper around RPM (Redhat Package Manager).


## Raw_ubuntu Docker Image

__with docker service up:__  
```shell
> vim /chch/docker_ubuntu/Dockerfile # write your own Dockerfile
> docker build -t szang1/raw_ubuntu_12.04 . # build it into a base image
> docker push szang1/raw_ubuntu_12.04 # push it to your docker repo
> docker run -d -i -t --name raw_ubuntu szang1/raw_ubuntu_12.04 # run it 
> docker exec -i -t raw_ubuntu bash # start an interactive bash console inside the container
```

__NOTE:__
1. Communication over socket.
2. Keep the container alive.
3. option -d makes the container running in the background.
```shell
> sudo service docker start # will run the script at /etc/init.d/docker to establish communication with docker daemon via socket at /var/run/docker.sock.
> tail -f dev/null # will keep a process alive till you kill it. For example, if you put CMD ["tail", "-f", "/dev/null"] in a Dockerfile and start the container, it will be executed to keep the container running. Otherwise, the container will terminate itself because no process is running.
> docker exec -i -t <running_container_name> <process> # allow you to interact with a container while it is runnning. Every time you run it, it creates a new/identical container; everytime you exit it, all the changes discarded.
```

## Aws Demo Simple Web App
```shell
> sudo yum install -y git;
> git clone https://github.com/awslabs/ecs-demo-php-simple-app;
> docker build -t szang1/amazon-ecs-sample . # build the image locally from the Dockerfile
> docker run -p 80:80 szang1/amazon-ecs-sample # run the image
> docker login -u szang1 -p [password]; # login docker registry
> docker push szang1/amazon-ecs-sample; # push the image just built to my repo on dockerhub
```

__NOTE:__
1. Dockerfile execute the following:  
a. specify base image ubuntu:12.04;  
b. install software supports for running a web server and PHP;  
c. add the src file to the web server's document root;  
d. configure the server and expose the port 80 on the container;  
e. start the web server.  
2. szang1 is the username of my account in dockerhub and I build this image as one of the entries under my account, namely amazon-ecs-sample.
3. mapping the port 80 on the container to the port 80 on the host so that when browser requests via ec2's port 80 the request will go into the container.
4. ec2 security groups and its inbound traffice rules must be configured accordingly in AWS console so that it allows HTTP requests via port 80.