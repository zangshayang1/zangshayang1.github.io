The on-heap store refers to objects that will be present in the Java heap (and also subject to GC). On the other hand, the off-heap store refers to (serialized) objects that are managed by EHCache, but stored outside the heap (and also not subject to GC). As the off-heap store continues to be managed in memory, it is slightly slower than the on-heap store, but still faster than the disk store.

# JVM Memory Categories
Heap - The heap is where your Class instantiations or “Objects” are stored.
Thread stacks - Each thread has its own call stack. The stack stores primitive local variables and object references along with the call stack (list of method invocations) itself. The stack is cleaned up as stack frames move out of context so there is no GC performed here.
Metaspace - Metaspace stores the Class definitions of your Objects, and some other metadata.
Code cache - The JIT compiler stores native code it generates in the code cache to improve performance by reusing it.
Buffer pools - Many libraries and frameworks allocate buffers outside of the heap to improve performance. These buffer pools can be used to share memory between Java code and native code, or map regions of a file into memory.
OS memory - The operating system keeps heap and stacks for a Java process independent of the heap and stacks managed by the JVM itself. There is also memory consumed for each native library loaded (such as libjvm.so). This is usually very small.

### About -XX:maxDirectMemorySize=1G

maxDirectMemorySize 这个参数有跟没有的区别是：如果没有，就会根据读写需要不断allocate，直到OOM；如果有，就会在1G的时候trigger GC，清理一部分再allocate，上限是1G。

### About GC

In traditional JVM memory management, heap space is divided into Young and Old generations. The young generation consists of an area called Eden along with two smaller survivor spaces, as shown in Figure 1. Newly created objects are initially allocated in Eden. Each time a minor GC occurs, the JVM copies live objects in Eden to an empty survivor space and also copies live objects in the other survivor space that is being used to that empty survivor space. This approach leaves one of the survivor spaces holding objects, and the other empty for the next collection. Objects that have survived some number of minor collections will be copied to the old generation. When the old generation fills up, a major GC will suspend all threads to perform full GC, namely organizing or removing objects in the old generation. This execution pause when all threads are suspended is called Stop-The-World (STW), which sacrifices performance in most GC algorithms.

Java’s newer G1 GC completely changes the traditional approach. The heap is partitioned into a set of equal-sized heap regions, each a contiguous range of virtual memory (Figure 2). Certain region sets are assigned the same roles (Eden, survivor, old) as in the older collectors, but there is not a fixed size for them. This provides greater flexibility in memory usage. When an object is created, it is initially allocated in an available region. When the region fills up, JVM creates new regions to store objects. When minor GC occurs, G1 copies live objects from one or more regions of the heap to a single region on the heap, and select a few free new regions as Eden regions. Full GC occurs only when all regions hold live objects and no full-empty region can be found.

### References
[Tuning Java Garbage Collection for Apache Spark Applications](https://databricks.com/blog/2015/05/28/tuning-java-garbage-collection-for-spark-applications.html)
