---
layout: post
title:  Network Fundamentals Review
date:   2019-06-22 13:42:00 +0800
categories: reference
tag: network
---

* content
{:toc}




## Open System Interconnection Model by Layers

__Physical Layer__  
convert bits to electrical, radio, optical signal.  

__Data Link Layer__  
1. MAC(medium access control), define how devices gain access to medium.
2. LLC(logical link control), define flow control protocol, which allows receiver to control sending rate.  

__Network Layer__  
Provide functionalities to transfer data from one node to another.
Act like a postman, segment large amount of data into several packets and deliver them to their destinations.

__Transport Layer__  
Add flow control, error control and reliability control over network layer.  
TCP/UDP implemented on this layer.  

__Session Layer__  
Manage a set of connections between nodes.
Provide full-duplex(phone call), half-duplex(press-and-talk) or simplex conversations.
Provide session checkpointing and recovery.

__Presentation Layer__
Logistics layer before application layer.
1. format data into what is acceptable to the application layer.
2. translate request/response syntax for application layers.
3. compress data if needed.

__Application Layer__  
User facing software that facilitates open network communications between nodes.

# HTTP

## What happens after you send a HTTP request via browser?
1. Browser parses the domain and look up its IP in local mapping.
2. If not found, browser send request to local DNS service for domain IP.
3. After retrieve IP, the original HTTP request is sent over TCP connection.
4. Three way handshaking (syn, syn-ack, ack).
5. The response may contain html, javascript, css, etc.
If a url reference to a javascript resource in the cloud is returned,
a new TCP connection will be initialized to fetch the javascript resources.

## REST vs. HTTP
HTTP is a communication protocol while REST stands for a way things are done using HTTP.

## Headers
1. CORS(cross origin resource sharing) eligibility is verified by "preflight",
where client sends a OPTION request to server and server responded with allow domain and methods.
2. Authorization, Basic format: (username:password).
Base64 encoding, for example, represent 3 Ascii chars (24 bits) by 4 base64 chars.
3. Forwarded, list the original places this request is forwarded from.
