---
layout: post
title: Zhangxin Java Core - Concurrent Programming
date:   2021-07-24 19:24:00 -0700
categories: reference
tag: java
---

* content
{:toc}



Updated: 2021-07-24

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


# Java ThreadPool

### ThreadPool Hierarchy

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

![]({{ '/styles/images/zhangxin-java-core/thread-pool-executor-algorithm.png' | prepend: site.baseurl }})

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

![]({{ '/styles/images/zhangxin-java-core/thread-state-lifecycle.jpeg' | prepend: site.baseurl }})

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

# Multi-threading Coordination

__Interview Question:__ How to make three threads print 1, 2, 3, 4, 5, 6, 7... one number at a time in a round robin fashion?
1. [wait/notify](https://github.com/zangshayang1/zhangxin/blob/master/JavaCore/src/main/com/zhangxin/javacore/concurrent/coordination/WaitNotifyDemo.java)
2. [park/unpark](https://github.com/zangshayang1/zhangxin/blob/master/JavaCore/src/main/com/zhangxin/javacore/concurrent/coordination/LockSupportDemo.java)
3. [CAS](https://github.com/zangshayang1/zhangxin/blob/master/JavaCore/src/main/com/zhangxin/javacore/concurrent/coordination/CasDemo.java)

### LockSupport

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

### Java Util Concurrent

__Java Util Concurrent APIs and their scenarios__
1. [CountDownLatch](https://github.com/zangshayang1/zhangxin/blob/master/JavaCore/src/main/com/zhangxin/javacore/concurrent/coordination/CountDownLatchDemo.java) -> Start together (load testing) or finish together
2. [CyclicBarrier](https://github.com/zangshayang1/zhangxin/blob/master/JavaCore/src/main/com/zhangxin/javacore/concurrent/coordination/CyclicBarrierDemo.java) -> Multi-stage start together
3. Phaser -> Multi-stage variable number of threads in each stage (Too much details, ignored).
4. [Semaphore](https://github.com/zangshayang1/zhangxin/blob/master/JavaCore/src/main/com/zhangxin/javacore/concurrent/coordination/SemaphoreDemo.java) -> Threadpool resource control (rate limiter).

There are 3 things worthnoting in a [simplified implementation of CountDownLatch](https://github.com/zangshayang1/zhangxin/blob/master/JavaCore/src/main/com/zhangxin/javacore/concurrent/coordination/MyCountDownLatch.java):
1. The use of infinite loop with conditional termination help to implement conditional waiting paradigm in concurrent programming that can also prevent psudo wakeup
2. The use of `SynchronizedList<Thread>` serving as a waiting pool
3. The use of CompareAndSet(CAS)

# Thread Safety

### Java Memory Model and Synchronization Protocol

__Java Memory Model__
1. global variable in main memory space
2. thread can only access its own working memory space
3. thread READ/WRITE global variable must first sync variable from main memory to working memory and then sync back. require that it goes through a synchronization protocol that sits between its own working memory space and main memory space.

The above model, in which working threads cannot directly access main memory, brings all kinds of thread safety concerns. Java Memory Model is part of __Java Language Norm__. They are logical concepts. Different JVM implementations must follow the same Java Language Norm. Eight atomic operations are defined in a synchronization protocol that sits between threads' working memory and main memory. The below eight atomic operations are again logical concepts. Different JVMs provide different implementations. 

__Synchronization Protocol - Eight Atomic Operations__
1. lock - restrict main memory variable access to a single thread
2. unlock
3. read - read variable value from main memory into registers
4. load - load variable value from registers to working memory
  * `read and load` sync variable from main memory to working memory.
5. use
6. assign
7. store - store variable value from working memory to registers
8. write - write variable value from registers to main memory
  * `store and write` sync variable from working memory to main memory.

__Synchronization Protocol - Eight rules__
1. (Read, load) or (store, write) operations are always paired.
2. When a variable value is updated in a thread, it must be sync'ed back to main memory.
3. When a variable value is not updated in a thread, it must not be sync'ed back to main memory.
4. A new shared variable can only come from main memory
5. No more than 1 thread can lock the same variable. One thread can `lock` one variable many times and unlocking the variable requires as many times `unlock` operation.
6. Before a variable is locked, its local cache in working thread must be cleared. 
7. A thread cannot `unlock` a variable that is NOT previously locked. A thread cannot `unlock` a variable that is previously locked by other threads. 
8. Before a variable is unlocked, its local cache in working thread must be sync'ed back to main memory.

### Synchronization Keywords and Locks

__synchronized semantics__
1. Right before entering `synchronized` block, it will clear thread-local cache of the locked variable and perform (read, load) operations to sync from main memory.
2. Right before exiting `synchronized` block, it will perform (store, write) operations on the locked variable to sync back to main memory.

__volatile semantics__
1. Right before READ `volatile` variables, (read, load) operations must be performed consecutively.
2. Right after WRITE `volatile` variables, (store, write) operations must be performed consecutively.

__Note__
1. Modern CPUs employ performance optimization that can result in out-of-order execution of operations. The order of execution can be guaranteed by [memory barrier](https://en.wikipedia.org/wiki/Memory_barrier). 
2. Appropriate use of `synchronized` keyword can guarantee thread safety but `volatile` can ONLY guarantee clean READ.
3. Use of `volatile` keyword can prevent reordering of operations in local code block. 

__The following example demonstrated why reordering of operations can be unsafe under multi-threading context:__
```java
// Thread A
content = init();
isInit = true;

// The above can be reordered as below when boolean variable "isInit" is not involved in init()
isInit = true;
content = init();

// Thread B
if (isInit) {
  content.method();
}

// when threads A and B are running concurrently
// it's likely that B throw NullPointerException due to the above reordering.
```

__volatile keyword ensures atomic READ/WRITE for a single address rather than an object__

```java
// volatile variable is thread safe when:
// 1. there is only one modifer
// 2. the variable points to a primitive data structure
volatile boolean flag; 
volatile int i;

// volatile variable is NOT thread safe when there are more than 1 modifiers

// volatile variable is NOT thread safe when
// the variable points to a container data structure where data references are stored
// for example, volatile list != all the integers inside are volatile
volatile List<Integer> alist = new ArrayList<>();
```

__Thread Safety Demo__
1. [How synchronized/volatile keyword impact visibility](https://github.com/zangshayang1/zhangxin/blob/master/JavaCore/src/main/com/zhangxin/javacore/concurrent/keyword/VisibilityDemo.java)
2. [Classic singleton implementation using volatile to prevent reordering of operations](https://github.com/zangshayang1/zhangxin/blob/master/JavaCore/src/main/com/zhangxin/javacore/concurrent/keyword/Singleton.java)
3. [When volatile keyword cannot guarantee thread safety](https://github.com/zangshayang1/zhangxin/blob/master/JavaCore/src/main/com/zhangxin/javacore/concurrent/keyword/VolatileThreadUnsafeDemo.java)

__READ/WRITE Atomicity__

Java Memory Mode defined 8 atomic operations. Among them, read/load/assign/use/store/write operations are used with `synchronized` keyword to ensure the atomicity of primitive data type READ/WRITE operations. 

__Order Of Execution__
Due to CPU performance optimization, execution is out-of-order under multi-threading context. But within a single thread, execution is as-if-serial.

JMM defined eight __happens-before__ principles: 
1. within a thread, if operation A's result is used in operation B, then A must happen before B.
2. on a same object, unlocking must happen before locking.
3. on volatile field, WRITE operation must happen before any subsequent READ operation.
4. thread start must happen before any operation defined in this thread.
5. all operation defined in a thread must happen before thread termination.
6. thread interruption must happen before this thread detects interruption.
7. an object instantiation must happen before its cleanup
8. if A happens before B and B happens before C, then A must happen before C.

__Why using lock when there is synchronized keyword?__

`Lock` provides lower-level granularity of control than `synchronized` keyword in that:
1. lock can be placed in try-catch block
2. lock can specify waiting time
3. lock can be interrupted
4. different `conditions` can be applied to lock/unlock different threads (lower-level granlarity of control) whereas synchronized threads
  * must `wait` for `notify` altogether
  * must release locks at the reverse order of them being acquired

Also, `lock` provide fair/unfair lock implementations while `synchronized` keyword implements unfair lock. A fair lock favors granting access to the longest-waiting thread.

__Lock interface and use pattern__

```java
public interface Lock {
  void lock();
  void lockInterruptibly() throws InterruptedException;
  boolean tryLock();
  boolean tryLock(long time, TimeUnit unit) throws InterruptedException;
  void unlock();
}

// ReentrantLock class implements the Lock interface. 
// It offers the same concurrency and memory semantics, as the implicit monitor lock 
// accessed using synchronized methods and statements, with extended capabilities.
public class ReentrantLock implements Lock {}

// Typical use pattern
class SharedResource {
  private final Lock lock = new ReentrantLock();

  public void fun() {
    lock.lock();  // block until condition holds
    try { // lock is always followed by try to avoid deadlock in case of an exception thrown.
      // ... method body
    } finally {
      lock.unlock()
    }
  }
}
```

__Lock Condition Pattern__
```java
public interface Lock {
  // ...

  // Returns a new Condition instance that is bound to this lock
  Condition newCondition();
}

public interface Condition {
  void await() throws InterruptedException;
  void signal();
}
```

__ReadWriteLock vs Lock__

`Lock` interface defines mutual exclusive lock but `ReadWriteLock` interface defines a lock that can be shared by many READ threads and exclusively acquired by a single WRITE thread. It is designed to improve performance under certain circumstances where READ concurrency > WRITE concurrency. But under extreme cases where READ concurrency >> WRITE concurrency, WRITE thread might not be able to acquire the lock at all. 

```java
public interface ReadWriteLock {
    // Returns the lock used for reading.
    Lock readLock();

    // Returns the lock used for writing.
    Lock writeLock();

// implementation
public class ReentrantReadWriteLock implements ReadWriteLock {}
```

__StampedLock__

`StampedLock` provides pessimistic read lock (assuming content has been changed when reading) and optimistic read (assuming content has NOT been changed when reading), and of course exclusive write lock. It's added in Java 8 to improve the performance of `ReadWriteLock` under the above extreme cases. 

__Lock Demo__
[Demo the use of Lock and Condition](https://github.com/zangshayang1/zhangxin/blob/master/JavaCore/src/main/com/zhangxin/javacore/concurrent/lock/ProducerConsumerQueue.java)

[Demo the use of ReadWriteLock](https://github.com/zangshayang1/zhangxin/blob/master/JavaCore/src/main/com/zhangxin/javacore/concurrent/lock/ReadWriterLockDemo.java)

[Demo the use of ReentrantLock](https://github.com/zangshayang1/zhangxin/blob/master/JavaCore/src/main/com/zhangxin/javacore/concurrent/lock/ReentrantLockDemo.java)

[Demo the use of StampedLock](https://github.com/zangshayang1/zhangxin/blob/master/JavaCore/src/main/com/zhangxin/javacore/concurrent/lock/StampedLockDemo.java)

__Lock Performance Comparison__

![]({{ '/styles/images/zhangxin-java-core/lock-performance-comparison.png' | prepend: site.baseurl }})

### Atomic Types

* AtomicInteger
* AtomicLong
* AtomicBoolean
* AtomicReference [demo](https://github.com/zangshayang1/zhangxin/blob/master/JavaCore/src/main/com/zhangxin/javacore/concurrent/atomic/AtomicReferenceDemo.java)
* AtomicIntegerArray
* AtomicLongArray
* AtomicReferenceArray
* AtomicIntegerFieldUpdater [demo](https://github.com/zangshayang1/zhangxin/blob/master/JavaCore/src/main/com/zhangxin/javacore/concurrent/atomic/AtomicFieldUpdaterDemo.java)
* AtomicLongFieldUpdater
* AtomicReferenceFieldUpdater

__When using atomic field updater:__
* the field must be accessible
* the field must be volatile
* the field cannot be static
* the field cannot be final
* the field cannot come from parent

__How to handle ABA Problem?__
[What is ABA Problem](https://en.wikipedia.org/wiki/ABA_problem)
* AtomicStampedReference [demo](https://github.com/zangshayang1/zhangxin/blob/master/JavaCore/src/main/com/zhangxin/javacore/concurrent/atomic/AtomicStampedReferenceDemo.java)
* AtomicMarkableReference

How to implement ReentrantLock using LockSupport and Atomic type? Thread Safety-3 59:00

# Distributed lock

__Why is it needed__
In high-concurrency E-commerce scenario, there will be a cluster handling customers' request but there is only a single node generating UUID for all of the orders. The entire cluster of nodes need to talk to the UUID generator where a distributed lock is required to ensure the quality of the UUID.

__How to provide exclusivity?__
2. sql-based database: primary key (whoever created the next record get the lock?)
  * single point failure
  * unable to handle high concurrency
  * no ttl for record -> whoever wrote the lock record died before remove the pk record -> deadlock
3. cache based redis -> setnx() returns true if set successful otherwise false.
  * whoever calls setnx died before remove the key -> deadlick -> how to set ttl -> too long or too short is equally bad
  * good performance able to support highest concurrency
4. file system (znode) based zookeeper:
  * easier to implement -> no need to consider ttl -> watch mechanism
  * reliable
  * performance not as good as redis -> support 10-20k concurrency

### Zookeeper

__Four Type of Znode__
1. persistent
2. persistent_sequential
3. ephemeral
4. ephemeral_sequential

```shell
> ./zookeeper/bin/zkCli.cmd 
> create /mynode <data> # create a PERSISTENT znode with data
> set /mynode <update_data> # update data in existing node
> create -s /mynode/ <data> # create a PERSISTENT_SEQUENTIAL znode: /mynode/0000000000
> create -s /mynode/ <data> # create a PERSISTENT_SEQUENTIAL znode: /mynode/0000000001
> delete /mynode/0000000001 # delete a znode 
> create -e /eph <data> # create a EPHEMERAL znode, which will be gone when this session terminates.
> create -s -e /mynode <data> # create a EPHEMERAL_SEQUENTIAL znode: /mynode/0000000002
> quit
```

__Use ZK to implement a distributed lock__

Whichever thread successfully creates an ephermeral znode gets the lock. Before it releases the lock, it will delete the znode. Note that creating persistent znode might turn out to be a deadlock if a thread died before it removes the znode. Whereas an ephermeral znode will automatically be deleted when the session disconnects. 

[Distributed Lock Implementation in High Concurrency Context](https://github.com/zangshayang1/zhangxin/blob/master/JavaCore/src/main/com/zhangxin/javacore/concurrent/distributed/HighConcurrencyScenario.java)

![]({{ '/styles/images/zhangxin-java-core/use-zk-as-distributed-lock.png' | prepend: site.baseurl }})

__Thundering Herd Problem__
[What is thundering herd problem?](https://en.wikipedia.org/wiki/Thundering_herd_problem)
 
# AQS - AbstractQueuedSynchronizer

Before digging into AQS, let's do some warm up: 
1. [Implement My Version of Reentrant Lock](https://github.com/zangshayang1/zhangxin/blob/master/JavaCore/src/main/com/zhangxin/javacore/concurrent/lock/MyReentrantLockImpl.java)
2. Compare ReentrantLock and Semaphore
  * both support fair/unfair mode.
  * both are blocking.
  * both behave the same: whoever acquires the locking resource(s) can proceed while others are blocked.
  * both are built on top of `AbstractQueuedSynchronizer` (same for CountDownLatch)
  * There is only 1 locking resource (exclusive) in ReentrantLock but there could be arbitrary number of locking resources (shared across threads) in Semaphore. 

__What is AQS?__  
Quote from Java source - It provides a framework for implementing blocking locks and related synchronizers (semaphores, events, etc) that rely on first-in-first-out (FIFO) wait queues. This class is designed to be a useful basis for most kinds of synchronizers that rely on a single atomic `int` value to represent state.

__How to use AQS__  
Quote from Java source - Subclasses must define the protected methods that change this state and define what that state means in terms of this object being acquired or released. Given these, the other methods in this class carry out all queuing and blocking mechanics. Subclasses can maintain other state fields, but only the atomic `int` value manipulated using methods `getState`, `setState` and `compareAndSetState` is tracked with respect to synchronization. Subclasses should be defined as non-public internal helper classes that are used to implement the synchronization properties of their enclosing class. 

![]({{ '/styles/images/zhangxin-java-core/aqs-core.png' | prepend: site.baseurl }})

__Support Two Modes__  
Quote from Java source - This class supports either or both a default __exclusive__ mode and a __shared__ mode. When acquired in exclusive mode, attempted acquires by other threads cannot succeed. Shared mode acquires by multiple threads may (but need not) succeed. Two modes share the same queue by use different nodes.

```java
public abstract class AbstractQueuedSynchronizer extends AbstractOwnableSynchronizer {

  // implementation omitted ...

  static final class Node {
    /** Marker to indicate a node is waiting in shared mode */
    static final Node SHARED = new Node();
    /** Marker to indicate a node is waiting in exclusive mode */
    static final Node EXCLUSIVE = null;

    /** waitStatus value to indicate thread has cancelled */
    static final int CANCELLED =  1;
    /** waitStatus value to indicate successor's thread needs unparking */
    static final int SIGNAL    = -1;
    /** waitStatus value to indicate thread is waiting on condition */
    static final int CONDITION = -2;
    /**
     * waitStatus value to indicate the next acquireShared should
     * unconditionally propagate
     */
    static final int PROPAGATE = -3;

    /**
     * Status field, taking on only the values:
     *   SIGNAL:     The successor of this node is (or will soon be)
     *               blocked (via park), so the current node must
     *               unpark its successor when it releases or
     *               cancels. To avoid races, acquire methods must
     *               first indicate they need a signal,
     *               then retry the atomic acquire, and then,
     *               on failure, block.
     *   CANCELLED:  This node is cancelled due to timeout or interrupt.
     *               Nodes never leave this state. In particular,
     *               a thread with cancelled node never again blocks.
     *   CONDITION:  This node is currently on a condition queue.
     *               It will not be used as a sync queue node
     *               until transferred, at which time the status
     *               will be set to 0. (Use of this value here has
     *               nothing to do with the other uses of the
     *               field, but simplifies mechanics.)
     *   PROPAGATE:  A releaseShared should be propagated to other
     *               nodes. This is set (for head node only) in
     *               doReleaseShared to ensure propagation
     *               continues, even if other operations have
     *               since intervened.
     *   0:          None of the above
     *
     * The field is initialized to 0 for normal sync nodes, and
     * CONDITION for condition nodes.  It is modified using CAS
     * (or when possible, unconditional volatile writes).
     */
}
```

__Provide Condition Implementation__  
Quote from Java source - This class defines a nested `ConditionObject` class that can be used as a `Condition` implementation by subclasses.

__Important to Note when Overriding the Protected Abstract Methods__  
Protected Abstract Methods:
* tryAcquire
* tryRelease
* tryAcquireShared
* tryReleaseShared
* isHeldExclusively

Important Notes:
1. use `getState`, `setState` and `compareAndSetState` to manipulate the atomic `int` state.
2. the implementation internal should be thread-safe and non-blocking
3. all the above methods return `boolean` except that `tryAcquireShared` returns `int` value: negative means failure; 0 means the current thread gets the only remaining resource; positive means the current thread gets one of the remaining resources and current thread needs to wake up the next one waiting in the queue.

__The Queue in AQS Doesn't Guarantee Fairness__  
Quote from Java source - While this is not guaranteed to be fair or starvation-free, earlier, queued threads are allowed to recontend before later queued threads, and each recontention has an unbiased chance to succeed against incoming threads. Throughput and scalability are generally highest for this default barging strategy. Fairness can be implemented by subclasses. 

__Core Logic of Acquiring a Lock__

![]({{ '/styles/images/zhangxin-java-core/acquire-lock-core-logic.png' | prepend: site.baseurl }})

# Synchronized

```java
public class SynchroinzedDemo {
    
    static int c;
    // static synchronized method locking at class level
    public static synchronized void add1(int a) { c += a; }

    // non-static synchronized method locking at the class instance
    public synchronized void add2(int a) { c += a; }

    // synchronized block locking at whatever object provided
    public void add3(int a){
      synchronized (this) { c += a; }
    }
}
```

---

### How Does It Work?

The above demo code (add3) compiles to the following:
```
public static synchronized void add1(int);
    descriptor: (I)V
    flags: ACC_PUBLIC, ACC_STATIC, ACC_SYNCHRONIZED
    Code:
      stack=2, locals=1, args_size=1
         0: getstatic     #2                  // Field c:I
         3: iload_0
         4: iadd
         5: putstatic     #2                  // Field c:I
         8: return

  public synchronized void add2(int);
    descriptor: (I)V
    flags: ACC_PUBLIC, ACC_SYNCHRONIZED
    Code:
      stack=2, locals=2, args_size=2
         0: getstatic     #2                  // Field c:I
         3: iload_1
         4: iadd
         5: putstatic     #2                  // Field c:I
         8: return

public void add3(int);
    descriptor: (I)V
    flags: ACC_PUBLIC
    Code:
      stack=2, locals=4, args_size=2
         0: aload_0
         1: dup
         2: astore_2
         3: monitorenter
         4: getstatic     #2                  // Field c:I
         7: iload_1
         8: iadd
         9: putstatic     #2                  // Field c:I
        12: aload_2
        13: monitorexit
        14: goto          22
        17: astore_3
        18: aload_2
        19: monitorexit
        20: aload_3
        21: athrow
        22: return
```

Java `monitor` provides exclusivity for `synchronized` keyword.

---

### What is Monitor?

Java `monitor` is a syntactic sugar provided at the language level. It wraps around mutex and semaphore (kernel resources that provide synchronization, aka: synchronization primitives). Its implementation is provided by JVM, written in C/C++.

```c++
// jdk8u_hotspot/blob/master/src/share/vm/runtime/objectMonitor.hpp

class ObjectMonitor {
  // implementation omitted ...

  // initialize the monitor, except the semaphore, all other fields
  // are simple integers or pointers
  ObjectMonitor() {
    _header       = NULL;
    _count        = 0;
    _waiters      = 0,
    _recursions   = 0;      // reentrance counter
    _object       = NULL;
    _owner        = NULL;
    _WaitSet      = NULL;   // head of the doubly linked list 
                            // leading waiting threads
    _WaitSetLock  = 0 ;
    _Responsible  = NULL ;
    _succ         = NULL ;
    _cxq          = NULL ;
    FreeNext      = NULL ;
    _EntryList    = NULL ;
    _SpinFreq     = 0 ;
    _SpinClock    = 0 ;
    OwnerIsThread = 0 ;
    _previous_owner_tid = 0;
  }

// more methods omitted ...

public:
  bool      try_enter (TRAPS) ;
  void      enter(TRAPS);
  void      exit(bool not_suspended, TRAPS);
  void      wait(jlong millis, bool interruptable, TRAPS);
  void      notify(TRAPS);
  void      notifyAll(TRAPS);

 private:
  void      AddWaiter (ObjectWaiter * waiter) ;
  int       TryLock (Thread * Self) ;
}
```

---

### Optimization

Starting from Java 1.4, `-XX:-UseSpinning` was introduced to allow naive spinning through `while (i++ < 10)` before `tryLock()` again instead of directly entering monitor and waiting for wake-up signal. So if blocking duration is very short, self-spinning can give better performance than entering monitor. Otherwise, it's a waste of resources. `-XX:-UseSpinning` became default since Java 1.6 as multi-core machines became mainstream. 

---

__Lock Removal__

```java
public class SynchronizedRemoveDemo {
  public static String createStringBuffer(String str1, String str2) {
    // Thread safety is built into StringBuffer
    StringBuffer stringBuffer = new StringBuffer();
    // .apend() is a synchronized method but stringBuffer is a local variable
    // so the execution is thread-safe even when .append() is not synchronized
    // JIT compiler can figure this out through escape analysis
    // and then remove the ACC_SYNCHRONIZED flag on .append()
    stringBuffer.append(str1);
    stringBuffer.append(str2);
    return stringBuffer.toString();
  }
}
```

---

__Lock Coarsening__

Usually, we want to minimize the synchronized block of code (exactly where concurrent execution will cause unsafety) in order to ensure the effectiveness of using concurrent processing. However, when a series of operations acquire/release the same lock frequently (using `synchronized` within a for loop), Lock Coarsening will apply as an optimization.

```java
public class SynchronizedCoarseningDemo {
    Object lock = new Object();

    public void beforeCoarsening1() {
      synchronized (lock) {
        //do something
      }
      synchronized (lock) {
        //do something else
      }
    }

    public void beforeCoarsening2(int size) {
      for (int i = 0; i < size; i++) {
        synchronized(lock){
          //do something
        }
      }
    }

    public void afterCoarsening1() {
      synchronized (lock) {
        //do something

        //do something else
      }
    }

    public void afterCoarsening2(int size) {
      synchronized (lock) {
        for (int i = 0; i < size; i++) {
          //do something
        }
      }
    }
}
```

---

__Light-Weight Lock__

When there is no or less contention, synchronization will use CAS to provide exclusivity instead of monitor. How? A "lockable" object takes up a consecutive memory space, which consists of:
* object body
* object head
  * class metadata address
  * array length if it is an object array
  * mark word
    * first tag indicate whether it is locked
    * second tag indicate whether it is locked by light weight CAS operation
    * third tag indicate whether it is locked by heavy weight monitor

![]({{ '/styles/images/zhangxin-java-core/object-mark-word.png' | prepend: site.baseurl }})

---

__Biased Lock__
`-XX:-UseBiasedLocking` is only used in very-few-contention situation.

---

__Lock Upgrade__

Biased Lock -> Light weight Lock -> Monitor

Reference: https://www.programmersought.com/article/407747922/

![]({{ '/styles/images/zhangxin-java-core/lock-upgrade.jpeg' | prepend: site.baseurl }})

---

# Thread Local

### What is ThreadLocal
This class provides thread-local variables. These variables differ from their normal counterparts in that each thread that accesses one (via its
{@code get} or {@code set} method) has its own, independently initialized
copy of the variable.  {@code ThreadLocal} instances are typically private
static fields in classes that wish to associate state with a thread (e.g.,
a user ID or Transaction ID).  

Use `ThreadLocal` to implement the concept of ["session" or "context" in a web app](https://github.com/zangshayang1/zhangxin/blob/master/JavaCore/src/main/com/zhangxin/javacore/concurrent/threadlocal/BusinessScenarioSimulator.java), where it becomes a shared state specific to a thread handling a request.


```java
public class Thread implements Runnable {
  // ThreadLocal values pertaining to this thread. 
  // This map is maintained by the ThreadLocal class.
  ThreadLocal.ThreadLocalMap threadLocals = null;
}

// How ThreadLocal is implemented
public class ThreadLocal<T> {

  static class ThreadLocalMap {
    /**
     * The entries in this hash map extend WeakReference, using
     * its main ref field as the key (which is always a
     * ThreadLocal object).  Note that null keys (i.e. entry.get()
     * == null) mean that the key is no longer referenced, so the
     * entry can be expunged from table.  Such entries are referred to
     * as "stale entries" in the code that follows.
     */
    static class Entry extends WeakReference<ThreadLocal<?>> {
      // The value associated with this ThreadLocal.
      Object value;

      // Each thread instance is associated with a ThreadLocalMap
      // that uses ThreadLocal as key to store specific value object of whatever type
      // One ThreadLocalMap can store as many thread local values as you want
      Entry(ThreadLocal<?> k, Object v) {
        super(k);
        value = v;
      }
    }
  }

  ThreadLocalMap getMap(Thread t) {
    return t.threadLocals;
  }

  public void set(T value) {
    Thread t = Thread.currentThread();
    ThreadLocalMap map = getMap(t);
    if (map != null)
      map.set(this, value);
    else
      createMap(t, value);
  }
  
  public T get() {
    Thread t = Thread.currentThread();
    ThreadLocalMap map = getMap(t);
    if (map != null) {
      ThreadLocalMap.Entry e = map.getEntry(this);
      if (e != null) {
        @SuppressWarnings("unchecked")
        T result = (T)e.value;
        return result;
      }
    }
    return setInitialValue();
  }
}
```

__4 Types of References In Java__
| --- | --- | --- |
| Type of Reference | GC before Reference Nullified | Usage |
| strong | never | object origin |
| soft | when memory is not sufficient | object cache | 
| weak | regular gc | object cache | 
| phantom | regular gc | track object gc process |
| --- | --- | --- |

__Memory Leak Risk__

Note that `ThreadLocalMap` uses `ThreadLocal` as its key. There is a potential risk of memory leak when the reference of `ThreadLocal` was out of scope (nullified) and `ThreadLocal` object was GC'ed. In that case, the entry in the map became stale but the reference of the entry value persisted and the value object was never GC'ed. It could lead to a serious issue when used with thread pool under high-concurrency context.

__Best Practice When Using ThreadLocal__

Therefore do follow the below best practice when using `ThreadLocal`:
```java
public App {
  private static final ThreadLocal<T> threadLocal = new ThreadLocal<>();

  public static void main(String[] argv) {
    try {
      threadLocal.set(object);
      // handle business logic
    } finally {
      threadLocal.remove();
    }
  }
}
```

---

# Concurrent Collections

### HashTable vs ConcurrentHashMap

`HashMap` is thread-unsafe. `HashTable` is thread-safe. Why create `ConcurrentHashMap`? Because ConcurrentHashMap has performance advantage over HashTable.  
HashTable became thread-safe by naively adding `sychronized` keyword on every method.  
ConcurrentHashMap became thread-safe by locking on every hash bucket in an "Array-LinkedList" implementation.  

---

### Blocking Queue Series

```java
// BlockingQueue Interface
// 1. throw exception if queue is full/empty
//   * add(e)   
//   * remove()
// 2. return boolean/null if queue is full/empty
//   * offer(e)
//   * poll() 
// 3. blocking/waiting if queue is full/empty
//   * put(e)
//   * take()

public class ArrayBlockingQueue<E> 
  extends AbstractQueue<E> implements BlockingQueue<E> {}

public class SynchronousQueue<E> 
  extends AbstractQueue<E> implements BlockingQueue<E> {}

public class LinkedBlockingQueue<E> 
  extends AbstractQueue<E> implements BlockingQueue<E> {}
```

---

### Concurrent Collections
```java
public class ConcurrentLinkedQueue<E> 
  extends AbstractQueue<E> implements Queue<E> {}

public class ConcurrentHashMap<K,V> 
  extends AbstractMap<K,V> implements ConcurrentMap<K,V> {}

public class ConcurrentSkipListSet<E>
  extends AbstractSet<E> implements NavigableSet<E> {}
```

---

### Synchronized Collections

```java
public class Collections {

  // the following implementation is naively based on synchronized keyword
  // so the performance is not as good as natively concurrent counterparts

  static class SynchronizedMap<K, V> implements Map<K, V> {}

  static class SynchronizedList<E> 
    extends SynchronizedCollection<E> implements List<E> {}

  static class SynchronizedSet<E>
    extends SynchronizedCollection<E> implements Set<E> {}

  public static <K,V> Map<K,V> synchronizedMap(Map<K,V> m) {}

  public static <T> List<T> synchronizedList(List<T> list) {}

  public static <T> Set<T> synchronizedSet(Set<T> s) {}
}
```