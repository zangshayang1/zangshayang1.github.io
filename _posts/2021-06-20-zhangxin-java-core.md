---
layout: post
title: Zhangxin Java Core
date:   2021-06-20 15:00:00 -0700
categories: reference
tag: java
---

* content
{:toc}



Updated: 2021-07-04

# Collection

__Array__ is a "primitive" data structure. __Vector__ is a thread-safe version of array.

__List__
* LinkedList
* ArrayList (indexing + dynamic expansion)

__Map (Set)__
* HashMap (implementation = array + linkedlist (converted to red black tree when it's overloaded from depth and length))
* ConcurrentHashMap (thread-safe)
* TreeMap (red black tree)

### Red Black Tree

__Why we need Red Black Tree when there is AVL tree? AVL's self rebalancing algorithm is quite expensive.__

__AVL__ - JavaCore1: 35:00 min (AVL rotation)
1. self-balancing binary search tree
2. maintains the invariant (via self rotation) which is:  
3. for any node in this tree, math.abs(the height of its left subtree - the height of its right subtree) <= 1

__What makes a Red Black Tree? 2-3-4 Tree.__

__2-3-4 Tree__
* a self-balancing data structure
* a B-tree of order 4. 

In a 2-3-4 Tree:
* A 2-node is a node that has one data element, it must have 2 children unless it is a leaf node
* A 3-node is a node that has two data elements, it must have 3 children unless it is a leaf node
* A 4-node is a node that has three data elements, it must have 4 children unless it is a leaf node

Note: within each node, data elements are sorted by its comparable key.

__How to map a 2-3-4 Tree to a RBT?__
* A 2-node turns into black node.
* A 3-node turns into either
  * a black node and a left red child
  * a black node and a right red child
* A 4-node turns into a black node with two red children.

__insert operation__ (JavaCore2: 30:00)

__rebalance__ (JavaCore2: 58:00)

__How to delete a target node in a BST?__ - JavaCore3 30:00
If the target node is a leaf node: Directly delete.

If the target node has one child: Replace the target node with its child node.

If the target node that has a left child and a right child? Either:
1. Replace the target node with its predecessor and remove the predecessor (the node with the largest value found in target node left subtree).
2. Replace the target node with its successor and remove the successor (the node with the smallest value found in target node right subtree). Successor is used by TreeMap implementation. 


__RBT__

What invariants does a RBT maintain? 
1. A node is either red or black
2. Root node must be black
3. A red node can't have a red parent or a red child
4. Starting from any node, all the unsuccessful search path (from the root node to a leaf node) must pass through the exact same number of black nodes.

Operations to maintain the invariants
1. left rotation
2. right rotation
3. switch color

(Red Black Tree Implementation)[https://github.com/zangshayang1/zhangxin/blob/master/JavaCore/src/main/com/zhangxin/javacore/rbtree/RBTree.java]

# Reflection

__JVM 3 stages: Compile -> Loading -> Runtime__

![]({{ '/styles/images/zhangxin-java-core/jvm-three-stages.png' | prepend: site.baseurl }})

__3 Stages during loading (load binary code of required classes into method area):__ 
* loading (byte stream io into method area)
* linking
    * verification: verify binary source code
    * preparation: init default value or static fields
    * resolution: resolve symbolic references
* initialization
    * init static fields and their values for each class instance (thread-safe)

![]({{ '/styles/images/zhangxin-java-core/loading-stage.png' | prepend: site.baseurl }})

### Reflection Details

[Dynamic Loading](https://github.com/zangshayang1/zhangxin/blob/master/JavaCore/src/main/com/zhangxin/javacore/reflection/DynamicLoading.java)

[Static Initialization](https://github.com/zangshayang1/zhangxin/blob/master/JavaCore/src/main/com/zhangxin/javacore/reflection/StaticInitialization.java)

[Class Basics](https://github.com/zangshayang1/zhangxin/blob/master/JavaCore/src/main/com/zhangxin/javacore/reflection/ClassBasics.java)

[Reflection APIs](https://github.com/zangshayang1/zhangxin/blob/master/JavaCore/src/main/com/zhangxin/javacore/reflection/ReflectionAPIs.java)

[Reflection Performance](https://github.com/zangshayang1/zhangxin/blob/master/JavaCore/src/main/com/zhangxin/javacore/reflection/ReflectionPerformance.java)

[Override Accessibility](https://github.com/zangshayang1/zhangxin/blob/master/JavaCore/src/main/com/zhangxin/javacore/reflection/Accessibility.java)

# Generics

[Generic Class](https://github.com/zangshayang1/zhangxin/blob/master/JavaCore/src/main/com/zhangxin/javacore/generics/Inheritance.java)

[Generic Method](https://github.com/zangshayang1/zhangxin/blob/master/JavaCore/src/main/com/zhangxin/javacore/generics/GenericMethod.java)

[Wildcard](https://github.com/zangshayang1/zhangxin/blob/master/JavaCore/src/main/com/zhangxin/javacore/generics/WildCard.java)

[Erasure](https://github.com/zangshayang1/zhangxin/blob/master/JavaCore/src/main/com/zhangxin/javacore/generics/Erasure.java)


# Annotation

[Annotation Basics](https://github.com/zangshayang1/zhangxin/blob/master/JavaCore/src/main/com/zhangxin/javacore/annotations/MyAnnotation.java)

[Four Meta Annotations](https://github.com/zangshayang1/zhangxin/blob/master/JavaCore/src/main/com/zhangxin/javacore/annotations/FourMetaAnnotation.java)

[Annotation Use With Reflect](https://github.com/zangshayang1/zhangxin/blob/master/JavaCore/src/main/com/zhangxin/javacore/annotations/UseWithReflection.java)

# Concurrency

### Inspirational Questions

__Why use multi-threading in a single core machine?__  
CPU can idle during IO

__When would a thread yeild CPU?__  
When thread is blocked, waiting, sleeping, terminating.. etc.

__Does more thread always help?__  
No, multi-threading eat up memory quickly, which might decrease performance. Cost includes:
1. Context switching 
2. Creating/Destroy a thread
3. Multi-threading consumes both heap memory (from JVM) and stack memory (from system). Default maximum stack memory consumption: 1MB per thread. So for a high-concurrency app, you cannot allocate too much memory to heap because there will be not enough system memory for thread creation.

__How to set the appropriate number of threads?__  
1. For computation intensive task, use 1-2 times of the number of CPU cores available.
2. For IO intensive task, 10 - 100 times of the number of CPU cores available. Tomcat default uses 200 maximum threadpool size. 
3. In most scenarioes, it doesn't make sense to have thousands of threads running. 

__What is behind concurrent programming?__  
communication between threads, aka wait/notify. 

__How do we approach concurrent programming problem?__  
* Which part require concurrency? 
* Which parties require communication? 
* Who should wait? Who should notify? 

__What are the ways to implement concurrent programming?__  
1. synchronized, wait, notify
2. Lock/await/singal, LockSupport
3. java.util.concurrent (JUC) API: CountDownLatch, CyclicBarrier, Phaser, Semaphore.


### Java ThreadPool Hierarchy

```java
public interface Executor {

  void execute(Runnable command);
}


public interface ExecutorService extends Executor {

  void shutdown();
  
  List<Runnable> shutdownNow();
  
  boolean isShutdown();
  
  boolean isTerminated();
  
  boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException;
  
  <T> Future<T> submit(Callable<T> task);
  
  <T> Future<T> submit(Runnable task, T result);
  
  Future<?> submit(Runnable task);

  <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) 
    throws InterruptedException;

  <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks,
    long timeout, TimeUnit unit) throws InterruptedException;

  <T> T invokeAny(Collection<? extends Callable<T>> tasks) 
    throws InterruptedException, ExecutionException;
  <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) 
    throws InterruptedException, ExecutionException, TimeoutException;
}


public abstract class AbstractExecutorService implements ExecutorService {
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
    public Future<?> submit(Runnable task)
}


public class ThreadPoolExecutor extends AbstractExecutorService {
  
  public ThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
                            long keepAliveTime, TimeUnit unit,
                            BlockingQueue<Runnable> workQueue,
                            ThreadFactory threadFactory,
                            RejectedExecutionHandler handler) {}
}


public class ScheduledThreadPoolExecutor extends ThreadPoolExecutor
  implements ScheduledExecutorService {

  // the "period" starts counting when runnable begins
  public ScheduledFuture<?> scheduleAtFixedRate(
    Runnable command, long initialDelay, long period, TimeUnit unit) {}

  // the "delay" starts counting when runnable finishes   
  public ScheduledFuture<?> scheduleWithFixedDelay(
    Runnable command, long initialDelay, long delay, TimeUnit unit) {}
}

public class ForkJoinPool extends AbstractExecutorService {
  // maximum parallelism == number of available processors
  public ForkJoinPool() {}

  // ForkJoinPool is usually used to perform computation-intensive task
  // if there has to be IO task involved, maximum parallelism is configurable
  public ForkJoinPool(int parallelism) {}
}


// utility class
public class Executors {
  // ThreadPoolExecutor
  // corePoolSize == maximumPoolSize
  // workQueueSize == Integer.MAX_VALUE
  public static ExecutorService newFixedThreadPool(int nThreads) {}

  // ThreadPoolExecutor
  // corePoolSize == 0
  // maximumPoolSize == Integer.MAX_VALUE
  // workQueueSize == Integer.MAX_VALUE
  public static ExecutorService newCachedThreadPool() {}

  // ForJoinPool
  // parallelism == available processors
  public static ExecutorService newWorkStealingPool() {}
}
```


### ThreadPool Implementation 

[ThreadPool Implementation](https://github.com/zangshayang1/zhangxin/blob/master/JavaCore/src/main/com/zhangxin/javacore/concurrent/pool/FixedSizeThreadPool.java) is based on the following three types of __BlockingQueue APIs:__
1. throw exception if queue is full/empty
  * add(e)   
  * remove()
2. return boolean/null if queue is full/empty
  * offer(e)
  * poll() 
3. blocking/waiting if queue is full/empty
  * put(e)
  * take()


__Read Source Code And Comment__
```java
public class ThreadPoolExecutor extends AbstractExecutorService {

  /**
   * The main pool control state, ctl, is an atomic integer packing
   * two conceptual fields
   *   workerCount, indicating the effective number of threads
   *   runState,    indicating whether running, shutting down etc
   */
  private final AtomicInteger ctl = new AtomicInteger(ctlOf(RUNNING, 0));

  /*
  * Proceed in 3 steps:
  *
  * 1. If fewer than corePoolSize threads are running, try to
  * start a new thread with the given command as its first
  * task.  The call to addWorker atomically checks runState and
  * workerCount, and so prevents false alarms that would add
  * threads when it shouldn't, by returning false.
  *
  * 2. If a task can be successfully queued, then we still need
  * to double-check whether we should have added a thread
  * (because existing ones died since last checking) or that
  * the pool shut down since entry into this method. So we
  * recheck state and if necessary roll back the enqueuing if
  * stopped, or start a new thread if there are none.
  *
  * 3. If we cannot queue task, then we try to add a new
  * thread.  If it fails, we know we are shut down or saturated
  * and so reject the task.
  */
  public void execute(Runnable command) {}


  /*
  * Extensible Components
  * 
  * 1. ThreadFactory
  * @param threadFactory the factory to use when the executor
  *        creates a new thread, default: new Executors.DefaultThreadFactory()
  * 
  * 2. RejectedExecutionHandler
  * @param rejectedExecutionHandler the handler to use when execution is blocked
  *        because the thread bounds and queue capacities are reached, 
  *        default: new AbortPolicy()
  */
  public ThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
                            long keepAliveTime, TimeUnit unit,
                            BlockingQueue<Runnable> workQueue,
                            ThreadFactory threadFactory,
                            RejectedExecutionHandler handler) {}

  /* 
  * More Extensible Components
  *
  * 3. Call back hooks that inherited class can implement
  */
  protected void beforeExecute(Thread t, Runnable r) { }
  protected void afterExecute(Thread t, Runnable r) { }
  protected void terminated() { }
}

```

### Thread State 

```java
public class Thread implements Runnable {
    // other implementation omitted ...

    public enum State {
        NEW, RUNNABLE, BLOCKED, WAITING, TIMED_WAITING, TERMINATED;
    }
}
```

__Thread Operations & State Transition__
1. [start()](https://github.com/zangshayang1/zhangxin/blob/master/JavaCore/src/main/com/zhangxin/javacore/concurrent/thread/state/ThreadStateDemo1_Sleep.java) makes thread go from NEW to RUNNABLE;
2. [sleep()](https://github.com/zangshayang1/zhangxin/blob/master/JavaCore/src/main/com/zhangxin/javacore/concurrent/thread/state/ThreadStateDemo1_Sleep.java) makes thread go TIMED_WAITING;
3. [join()](https://github.com/zangshayang1/zhangxin/blob/master/JavaCore/src/main/com/zhangxin/javacore/concurrent/thread/state/ThreadStateDemo2_Join.java) makes thread go WAITING/TIMED_WAITING
4. [wait()](https://github.com/zangshayang1/zhangxin/blob/master/JavaCore/src/main/com/zhangxin/javacore/concurrent/thread/state/ThreadStateDemo4_Wait.java) makes thread go WAITING or TIMED_WAITING
5. [waiting for IO](https://github.com/zangshayang1/zhangxin/blob/master/JavaCore/src/main/com/zhangxin/javacore/concurrent/thread/state/ThreadStateDemo5_IO.java) makes thread go RUNNABLE
6. [park()](https://github.com/zangshayang1/zhangxin/blob/master/JavaCore/src/main/com/zhangxin/javacore/concurrent/thread/state/ThreadStateDemo6_Park.java) makes thread go WAITING
7. [synchronized block](https://github.com/zangshayang1/zhangxin/blob/master/JavaCore/src/main/com/zhangxin/javacore/concurrent/thread/state/ThreadStateDemo3_Synchronized.java) allows the one entering it to go RUNNABLE and makes other threads BLOCKED


__Use interrupt() instead of stop()__

WHY? Because it is inherently unsafe. Stopping a thread causes it to unlock all the monitors that it has locked. More on ["Java Thread Primitive Deprecation"](https://docs.oracle.com/javase/8/docs/technotes/guides/concurrency/threadPrimitiveDeprecation.html) from Oracle.

Interrupting a WAITING/BLOCKED thread throws an `InterruptedException` immediately. Interrupting a RUNNABLE thread sets `interrupted state` to true and doesn't throw immediately. It will throw only after the thread goes into `WAITING/BLOCKED` states. Thread class static method `Thread.interrupted()` returns boolean value of `interrupted state` and reset the flag to false. If it happens before a thread goes into `WAITING/BLOCKED` states, the previous attempt to interrupt will go void. Thread class member method `isInterrupted()` returns `interrupted state` only.

```java
public class Thread implements Runnable {
    // other implementation omitted ...

    /**
     * Tests whether the current thread has been interrupted.  The
     * <i>interrupted status</i> of the thread is cleared by this method.  In
     * other words, if this method were to be called twice in succession, the
     * second call would return false (unless the current thread were
     * interrupted again, after the first call had cleared its interrupted
     * status and before the second call had examined it).
     *
     * @return  <code>true</code> if the current thread has been interrupted;
     *          <code>false</code> otherwise.
     */
    public static boolean interrupted() { return currentThread().isInterrupted(true); }

    /**
     * Tests whether this thread has been interrupted.  The <i>interrupted
     * status</i> of the thread is unaffected by this method.
     *
     * @return  <code>true</code> if this thread has been interrupted;
     *          <code>false</code> otherwise.
     */
    public boolean isInterrupted() { return isInterrupted(false); }

    /**
     * Tests if some Thread has been interrupted.  The interrupted state
     * is reset or not based on the value of ClearInterrupted that is
     * passed.
     */
    private native boolean isInterrupted(boolean ClearInterrupted);
}
```


### FutureTask vs Runnable/Callable

`FutureTask` is one higher level abstraction over Runnable/Callable. [This demons how to get result from future or cancel a future](https://github.com/zangshayang1/zhangxin/blob/master/JavaCore/src/main/com/zhangxin/javacore/concurrent/future/FutureDemo.java).

__Read Source Code And Comment__

```java
public class FutureTask<V> implements RunnableFuture<V> {

  // ommitting implementation details...

  /**
   *  A task thread is running this FutureTask 
   */
  public void run() {
    
    // ommitting implementation details...

    try {
      Callable<V> c = callable;
      if (c != null && state == NEW) {
        V result;
        boolean ran;
        try {
          result = c.call();
          ran = true;
        } catch (Throwable ex) {
          result = null;
          ran = false;
          setException(ex);
        }
        if (ran)
          // If c.call() finishes successfuly
          // Step in here to wake up whoever is waiting for result
          set(result); 
      }
    } finally {
      // ommitting implementation details...
    }
  }

  protected void set(V v) {
    if (UNSAFE.compareAndSwapInt(this, stateOffset, NEW, COMPLETING)) {
      outcome = v;
      UNSAFE.putOrderedInt(this, stateOffset, NORMAL); // final state
      finishCompletion();
    }
  }

  private void finishCompletion() {
    for (WaitNode q; (q = waiters) != null;) {
      if (UNSAFE.compareAndSwapObject(this, waitersOffset, q, null)) {
        // looping through all waiters...
        for (;;) {
          Thread t = q.thread;
          if (t != null) {
            q.thread = null;
            LockSupport.unpark(t); // this line wake up the waiting thread
          }
          WaitNode next = q.next;
          if (next == null)
            break;
          q.next = null; // unlink to help gc
          q = next;
        }
        break;
      }
    }
    done();
    callable = null;        // to reduce footprint
  }

  /**
   * A waiting thread is calling this method to get the result
   */
  public V get() throws InterruptedException, ExecutionException {
      int s = state;
      if (s <= COMPLETING)
          s = awaitDone(false, 0L); // waiting if not completed
      return report(s);
  }

  private int awaitDone(boolean timed, long nanos) throws InterruptedException {
    final long deadline = timed ? System.nanoTime() + nanos : 0L;
    WaitNode q = null;
    boolean queued = false;
    // The below infinite loop demonstrates a typical waiting-for-result implementation 
    for (;;) {
      if (Thread.interrupted()) {
        removeWaiter(q);
        throw new InterruptedException();
      }

      int s = state;
      if (s > COMPLETING) {
        if (q != null)
          q.thread = null;
        return s;
      }
      else if (s == COMPLETING)
        Thread.yield();
      else if (q == null)
        q = new WaitNode();
      else if (!queued)
        queued = UNSAFE.compareAndSwapObject(this, waitersOffset, q.next = waiters, q);
      else if (timed) {
        nanos = deadline - System.nanoTime();
        if (nanos <= 0L) {
          removeWaiter(q);
          return state;
        }
        // Make it timed wait and check again
        LockSupport.parkNanos(this, nanos);
      }
      else
        // Make it wait all the way until it's woken up by other threads
        LockSupport.park(this); 
    }
  }

}
```



### How to use ForkJoinPool?

[External Merge Sort implementation using ForkJoinPool](https://github.com/zangshayang1/zhangxin/tree/master/JavaCore/src/main/com/zhangxin/javacore/concurrent/forkjoin)

### How to make threads work together? 

__Interview Question:__ How to make three threads print 1, 2, 3, 4, 5, 6, 7... one number at a time in a round robin fashion?
1. [wait/notify](https://github.com/zangshayang1/zhangxin/blob/master/JavaCore/src/main/com/zhangxin/javacore/concurrent/coordination/WaitNotifyDemo.java)
2. [park/unpark](https://github.com/zangshayang1/zhangxin/blob/master/JavaCore/src/main/com/zhangxin/javacore/concurrent/coordination/LockSupportDemo.java)
3. [CAS](https://github.com/zangshayang1/zhangxin/blob/master/JavaCore/src/main/com/zhangxin/javacore/concurrent/coordination/CasDemo.java)

__LockSupport Park/Unpark implementation using Unsafe__

```java
public class LockSupport {

  // implementation details omitted...

  private static final long parkBlockerOffset;

  static {
    // Through Unsafe native methods, it gets the Thread's parkBlocker field memory address offset.
    // With the offset, it can do atomic CAS operation on any thread object's parkBlocker field.
    Class<?> tk = Thread.class;
    parkBlockerOffset = UNSAFE.objectFieldOffset(tk.getDeclaredField("parkBlocker"));
  }

  private static void setBlocker(Thread t, Object arg) {
    // Even though volatile, hotspot doesn't need a write barrier here.
    UNSAFE.putObject(t, parkBlockerOffset, arg);
  }

  public static void unpark(Thread thread) {
    if (thread != null)
      UNSAFE.unpark(thread);
  }

  public static void park(Object blocker) {
    Thread t = Thread.currentThread();
    setBlocker(t, blocker);
    UNSAFE.park(false, 0L);
    setBlocker(t, null);
  }    
}

/**
 * Native methods defined in Unsafe class
 */ 
public final class Unsafe {

  public native long objectFieldOffset(Field var1);
  public native void unpark(Object var1);
  public native void park(boolean var1, long var2);

  // can you put var4 directly into the address (var1's address + var2 offset)? 
  // yes, because you just put the address there for reference 
  // addresses of any object var4 take up the same memory space
  public native void putObject(Object var1, long var2, Object var4);
  // whereas in other primitive types, you can directly replace the value rather than address
  // it is legitimate to do so because they take up the same memory space
  public native void putBoolean(Object var1, long var2, boolean var4);
  public native void putByte(Object var1, long var2, byte var4);
  public native void putShort(Object var1, long var2, short var4);
  public native void putLong(Object var1, long var2, long var4);
  // ...
  
}

class Thread implements Runnable {

  // implementation details omitted...

  /**
   * The argument supplied to the current call to
   * java.util.concurrent.locks.LockSupport.park.
   * Set by (private) java.util.concurrent.locks.LockSupport.setBlocker
   * Accessed using java.util.concurrent.locks.LockSupport.getBlocker
   */
  volatile Object parkBlocker;
}

```

__Java Util Concurrent APIs and their scenarios__
1. [CountDownLatch](https://github.com/zangshayang1/zhangxin/blob/master/JavaCore/src/main/com/zhangxin/javacore/concurrent/coordination/CountDownLatchDemo.java) -> Start together (load testing) or finish together
2. [CyclicBarrier](https://github.com/zangshayang1/zhangxin/blob/master/JavaCore/src/main/com/zhangxin/javacore/concurrent/coordination/CyclicBarrierDemo.java) -> Multi-stage start together
3. Phaser -> Multi-stage variable number of threads in each stage (Too much details, ignored).
4. [Semaphore](https://github.com/zangshayang1/zhangxin/blob/master/JavaCore/src/main/com/zhangxin/javacore/concurrent/coordination/SemaphoreDemo.java) -> Threadpool resource control (rate limiter).





# QUESTION

为什么 volatile 可以防止指令优化重排？

Java VisualVM is a great tool to monitor java app.

Mutex vs Monitor vs Semaphore