---
layout: post
title: About RPC (Remote Procedure Call)
date: 2017-05-10 21:00:00 -0800
categories: paper-summary
tag: RPC
---

* content
{:toc}



The __idea__ of remote procedure call (RPC) came from local procedure call, which is a mechanism for transferring control and data within a program running on a single computer. Likewise, RPC __aims__ to realize the passing of arguments/results between different processes in a distributed system. The __motivation__ of making RPC semantically clean but powerful and highly efficient as in low setup/initialization overhead, is to facilitate building of applications running on a distributed system.  

This paper (1984) described a lot about their hardware supports, which is not very useful any more since modern computers are equipped with hardwares of completely different levels. Therefore, this summary will be mainly focused on their __design__ of the RPC. One principle they held is the __semantics__ of RPC should be as close as possible to those of local procedure calls.  

A complete remote procedure call includes these __steps__:  
1. Suspend the caller(process) and invoke user-stub (a user side program)
2. User-stub packs a specification of the target procedure and the arguments into one or more packets.  
3. RPCRuntime on both sizes are responsible for transmitting and receiving packets.  
4. Server-stub unpacks and hand over the procedure and the arguments to the corresponding program on server side.  
5. The program processes the request and send it back to user end.  

The core __modules__ of RPC include:  
1. An interface modules that resides in server, user and intermedia database, defines a list of procedure names, together with the types of their arguments and results. Of course, different servers can provide different interfaces to the database. Different procedure calls from users can be thus directed to different servers.  
2. An exporter program that resides in server-stub, returns a result from a procedure call.
3. An importer that resides in user-stub, makes a procedure call from the interface.
4. A RPCRuntime module comes in with the system.  

More specifically, the intermedia database will sync with servers in terms of what type of procedures are currently available in the interface. For example, it maps the type of services as well as the names of services to server location so that the RPCRuntime on user end can pinpoint the server according to whichever procedure call is made. Another procedure known as dispatcher implemented in server-stub will respond to incoming remote procedure calls.  

RPC in nature is a request-response type of communication, where no bulk data transfer is necessary. Of course, the designers of the RPC wanted to minimize the overhead due to initializing/receiving a call. So instead of using available data transmitting __protocols__, they built a specialized protocol for RPC to improve its efficiency. During a simple call, if any of the call packets or results packets are lost, the caller will be responsible for resubmitting the procedure call until an acknowledgement is received. One last important principle is one process cannot initiate new call until it’s received the result of the old call.
