---
layout: post
title: Zhangxin Java Framework - MyBatis
date:   2021-08-01 16:30:00 -0700
categories: reference
tag: java
---

* content
{:toc}



Updated: 2021-08-14

### Background and Setup

__What is MyBatis?__  
An ORM (object relational mapper) implementation, built on top of JDBC.

__Configuration__ requires:
1. declare dependency in pom.xml
2. global configuration: [mybatis-config.xml](https://github.com/zangshayang1/zhangxin/blob/master/JavaFramework/MyBatis/src/main/resources/mybatis-config.xml)

__Modeling__ requires:
1. [mapper file](https://github.com/zangshayang1/zhangxin/blob/master/JavaFramework/MyBatis/src/main/resources/mapper/UserMapper.xml) mapping data record in database to java data model
2. [pojo model](https://github.com/zangshayang1/zhangxin/blob/master/JavaFramework/MyBatis/src/main/java/com/zhangxin/framework/mybatis/pojo/User.java)
 
### Overview

#### Component Design 
1. user interface layer
2. core process layer
3. fundamental support layer

![]({{ '/styles/images/zhangxin-framework-mybatis/mybatis-component-design.png' | prepend: site.baseurl }})

#### Configuration Loading

![]({{ '/styles/images/zhangxin-framework-mybatis/mybatis-configuration-loading.png' | prepend: site.baseurl }})

#### Core Processing Logic

![]({{ '/styles/images/zhangxin-framework-mybatis/mybatis-core-logic.png' | prepend: site.baseurl }})

#### Executor Enhancement and Hierarchy

__Executor enhancement__ is done during configuration loading, it is:
1. Preceded with `CachingExecutor` using __Decorator Pattern__ [source](https://github.com/mybatis/mybatis-3/blob/master/src/main/java/org/apache/ibatis/session/Configuration.java#L678-L680).
2. Adding [configured plugins](https://github.com/zangshayang1/zhangxin/blob/master/JavaFramework/MyBatis/src/main/resources/mybatis-config.xml#L33-L35) into `InterceptorChain`.

![]({{ '/styles/images/zhangxin-framework-mybatis/mybatis-executor-enhancement.png' | prepend: site.baseurl }})

![]({{ '/styles/images/zhangxin-framework-mybatis/mybatis-executor-hierarchy.png' | prepend: site.baseurl }})

### Reflection Module

__What is MyBatis Reflection Module?__  
Reflection module is built on top of `java.lang.reflect.*`. 

__What is in the module?__  
[Reflector/MetaClass/MetaObject](https://github.com/zangshayang1/zhangxin/blob/master/JavaFramework/MyBatis/src/test/java/com/zhangxin/framework/mybatis/ReflectionModuleDemo.java) handles post query result process in a "reflective" way.

### TypeHandler Module

Specific type of TypeHandler or customized TypeHandler can be supplied to [each result in resultMap section](https://github.com/zangshayang1/zhangxin/blob/master/JavaFramework/MyBatis/src/main/resources/mapper/UserMapper.xml#L11-L18).

`TypeHandlerRegistry` initializes a mapping from different Java/JDBC types to corresponding handlers.

![]({{ '/styles/images/zhangxin-framework-mybatis/mybatis-typehandler-hierarchy.png' | prepend: site.baseurl }})

```java
public abstract class BaseTypeHandler<T> extends TypeReference<T> implements TypeHandler<T> {
  
  public abstract void setNonNullParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) 
    throws SQLException;

  // define null parameter handling and exception handling for all sub classes
  @Override
  public void setParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException {
    if (parameter == null) {
      if (jdbcType == null) {
        throw new TypeException("JDBC requires that the JdbcType must be specified for all nullable parameters.");
      }
      try {
        ps.setNull(i, jdbcType.TYPE_CODE);
      } catch (SQLException e) {
        throw new TypeException("Error setting null for parameter #" + i + " with JdbcType " + jdbcType + " . "
              + "Try setting a different JdbcType for this parameter or a different jdbcTypeForNull configuration property. "
              + "Cause: " + e, e);
      }
    } else {
      try {
        setNonNullParameter(ps, i, parameter, jdbcType);
      } catch (Exception e) {
        throw new TypeException("Error setting non null for parameter #" + i + " with JdbcType " + jdbcType + " . "
              + "Try setting a different JdbcType for this parameter or a different configuration property. "
              + "Cause: " + e, e);
      }
    }
  }    
}
```

### Logging Module

__The logging module has:__
1. a `Log` interface to define logging operations supported in MyBatis
2. different implementations of `Log` interface using different loggers, such as Slf4j, Log4j2, etc.
3. a `LogFactory` that initializes and provides access to the actual loggers.

```java
public interface Log {

    boolean isDebugEnabled();

    boolean isTraceEnabled();

    void error(String s, Throwable e);

    void error(String s);

    void debug(String s);

    void trace(String s);

    void warn(String s);
}

public class Slf4jImpl implements Log {
    // implementation omitted
}

public class Log4j2Impl implements Log {
    // implementation omitted
}

public class StdOutImpl implements Log {
    // implementation omitted
}

public final class LogFactory {

    private static Constructor<? extends Log> logConstructor;

    static {
        useSlf4jLogging();
        // ...
    }

    private LogFactory() {}

    public static Log getLog(Class<?> clazz) {
        return getLog(clazz.getName());
    }

    private static Log getLog(String logger) {
        try {
            return logConstructor.newInstance(logger);
        } catch (Throwable t) {
            throw new LogException("Error creating logger for logger " + logger + ".  Cause: " + t, t);
        }
    }

    public static synchronized void useCustomLogging(Class<? extends Log> clazz) {
        setImplementation(clazz);
    }

    public static synchronized void useSlf4jLogging() {
        setImplementation(org.apache.ibatis.logging.slf4j.Slf4jImpl.class);
    }

    public static synchronized void useLog4J2Logging() {
        setImplementation(org.apache.ibatis.logging.log4j2.Log4j2Impl.class);
    }

    public static synchronized void useStdOutLogging() {
        setImplementation(org.apache.ibatis.logging.stdout.StdOutImpl.class);
    }

    private static void setImplementation(Class<? extends Log> implClass) {
        try {
            Constructor<? extends Log> candidate = implClass.getConstructor(String.class);
            Log log = candidate.newInstance(LogFactory.class.getName());
            if (log.isDebugEnabled()) {
                log.debug("Logging initialized using '" + implClass + "' adapter.");
            }
            logConstructor = candidate;
        } catch (Throwable t) {
            throw new LogException("Error setting Log implementation.  Cause: " + t, t);
        }
    }
}
```

__Another hightlight in the logging module design is how they use Dynamic Proxy pattern to hide logging syntax.__


```java
public abstract class BaseExecutor implements Executor {
    // implementation omitted...

    protected Connection getConnection(Log statementLog) throws SQLException {
        Connection connection = transaction.getConnection();
        if (statementLog.isDebugEnabled()) {
        return ConnectionLogger.newInstance(connection, statementLog, queryStack);
        } else {
        return connection;
        }
    }
}

public final class ConnectionLogger extends BaseJdbcLogger implements InvocationHandler {

    @Override
    public Object invoke(Object proxy, Method method, Object[] params) {
        // hide logging syntax before executing target method
        if (isDebugEnabled()) {
            debug(" Preparing: " + removeExtraWhitespace((String) params[0]), true);
        }
        // executing target method
        PreparedStatement stmt = (PreparedStatement) method.invoke(connection, params);
        // return a logging version of PreparedStatement
        stmt = PreparedStatementLogger.newInstance(stmt, statementLog, queryStack);
        return stmt;
    }

    // Provides a logging version of a Connection via Dynamic Proxy pattern
    public static Connection newInstance(Connection conn, Log statementLog, int queryStack) {
        InvocationHandler handler = new ConnectionLogger(conn, statementLog, queryStack);
        ClassLoader cl = Connection.class.getClassLoader();
        return (Connection) Proxy.newProxyInstance(cl, new Class[]{Connection.class}, handler);
    }
}

public final class PreparedStatementLogger extends BaseJdbcLogger implements InvocationHandler {
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] params) throws Throwable {
        // hide logging syntax before executing target method
        if (isDebugEnabled()) {
            debug("Parameters: " + getParameterValueString(), true);
        }
        clearColumnInfo();
        if ("executeQuery".equals(method.getName())) {
            // executing target method
            ResultSet rs = (ResultSet) method.invoke(statement, params);
            // return a logging version of ResultSet
            return rs == null ? null : ResultSetLogger.newInstance(rs, statementLog, queryStack);
        } else {
            return method.invoke(statement, params);
        }
    }

    // Provides a logging version of a PreparedStatement via Dynamic Proxy pattern
    public static PreparedStatement newInstance(PreparedStatement stmt, Log statementLog, int queryStack) {
        InvocationHandler handler = new PreparedStatementLogger(stmt, statementLog, queryStack);
        ClassLoader cl = PreparedStatement.class.getClassLoader();
        return (PreparedStatement) Proxy.newProxyInstance(cl, new Class[]{PreparedStatement.class, CallableStatement.class}, handler);
    }
}

public final class ResultSetLogger extends BaseJdbcLogger implements InvocationHandler {

    @Override
    public Object invoke(Object proxy, Method method, Object[] params) throws Throwable {
        // implementation omitted...
    }

    // Provides a logging version of a ResultSet via Dynamic Proxy pattern
    public static ResultSet newInstance(ResultSet rs, Log statementLog, int queryStack) {
        InvocationHandler handler = new ResultSetLogger(rs, statementLog, queryStack);
        ClassLoader cl = ResultSet.class.getClassLoader();
        return (ResultSet) Proxy.newProxyInstance(cl, new Class[]{ResultSet.class}, handler);
  }
}
```

### Binding Module and Dynamic Mapper

__What does it do?__  
For each data model, we define a set of database query operations (using `sql` like language) in a corresponding mapper xml resources (for exmaple: [UserMapper](https://github.com/zangshayang1/zhangxin/blob/master/JavaFramework/MyBatis/src/main/resources/mapper/UserMapper.xml)). To programmatically apply those operations, we define a set of query methods in a mapper interface, where those method names must match with what are provided in the mapper xml. 

__Mapper: CRUD Operation Programmatic Interface__

![]({{ '/styles/images/zhangxin-framework-mybatis/mybatis-mapper.png' | prepend: site.baseurl }})

__Implementation Details__
1. `Configuration` scans mapper.xml and knows how to talk to database.
2. `MapperProxyFactory` creates dynamic proxy class that implements `Mapper` interface (using `java.lang.reflect.Proxy`).
3. `MapperProxy` implements `java.lang.reflect.InvocationHandler` and provides invocation of a set of `MapperMethod`.
4. Each `MapperMethod` associates with a `SqlCommand` that's mapped to one of the database operations.

![]({{ '/styles/images/zhangxin-framework-mybatis/mybatis-mapper-dynamic-proxy.png' | prepend: site.baseurl }})

```java
/*
* During configuration loading stage, user defined mappers are loaded into MapperRegistry.
* Each type of data model is mapped to a MapperProxyFactory,
* which dynamically generates a proxy that implements the user-defined Mapper interface.
*/
public class MapperRegistry {

  private final Map<Class<?>, MapperProxyFactory<?>> knownMappers = new HashMap<>();

  @SuppressWarnings("unchecked")
  public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
    final MapperProxyFactory<T> mapperProxyFactory = (MapperProxyFactory<T>) knownMappers.get(type);
    if (mapperProxyFactory == null) {
      throw new BindingException("Type " + type + " is not known to the MapperRegistry.");
    }
    try {
      return mapperProxyFactory.newInstance(sqlSession);
    } catch (Exception e) {
      throw new BindingException("Error getting mapper instance. Cause: " + e, e);
    }
  }
}

/*
* MapperProxyFactory use java.lang.reflect.* to dynamically generate 
* a proxy that implements the user-defined Mapper interface.
*/
public class MapperProxyFactory<T> {

  private final Class<T> mapperInterface;
  private final Map<Method, MapperMethodInvoker> methodCache = new ConcurrentHashMap<>();

  public MapperProxyFactory(Class<T> mapperInterface) {
    this.mapperInterface = mapperInterface;
  }

  public Class<T> getMapperInterface() {
    return mapperInterface;
  }

  public T newInstance(SqlSession sqlSession) {
    final MapperProxy<T> mapperProxy = new MapperProxy<>(sqlSession, mapperInterface, methodCache);
    return newInstance(mapperProxy);
  }

  @SuppressWarnings("unchecked")
  protected T newInstance(MapperProxy<T> mapperProxy) {
    return (T) Proxy.newProxyInstance(mapperInterface.getClassLoader(), new Class[] { mapperInterface }, mapperProxy);
  }
}

/*
* MapperProxy implements InvocationHandler invoke method that execute 
* the specific database query operations modeled by MapperMethod class.
*/
public class MapperProxy<T> implements InvocationHandler, Serializable {

    private final SqlSession sqlSession;
    private final Class<T> mapperInterface;
    private final Map<Method, MapperMethodInvoker> methodCache;

    public MapperProxy(SqlSession sqlSession, Class<T> mapperInterface, Map<Method, MapperMethodInvoker> methodCache) {
        this.sqlSession = sqlSession;
        this.mapperInterface = mapperInterface;
        this.methodCache = methodCache;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            if (Object.class.equals(method.getDeclaringClass())) {
                return method.invoke(this, args);
            } else {
                return cachedInvoker(method).invoke(proxy, method, args, sqlSession);
            }
        } catch (Throwable t) {
            throw ExceptionUtil.unwrapThrowable(t);
        }
    }
}

public class MapperMethod {

  private final SqlCommand command;
  private final MethodSignature method;

  public MapperMethod(Class<?> mapperInterface, Method method, Configuration config) {
    this.command = new SqlCommand(config, mapperInterface, method);
    this.method = new MethodSignature(config, mapperInterface, method);
  }

  public Object execute(SqlSession sqlSession, Object[] args) {
    Object result;
    switch (command.getType()) {
      case INSERT: {
        // implementation omitted ....
        break;
      }
      case UPDATE: {
        // implementation omitted ....
        break;
      }
      case DELETE: {
        // implementation omitted ....
        break;
      }
      case SELECT:
        // implementation omitted ....
        break;
      case FLUSH:
        // implementation omitted ....
        break;
      default:
        throw new BindingException("Unknown execution method for: " + command.getName());
    }

    return result;
  }
}
```

### Caching Module

MyBatis uses Decorator Pattern to design its caching layer. 
1. MyBatis provides a `Cache` interface as its behavior contract. 
2. The `Cache` interface also used for external extension, such as [RedisCache implementation](https://github.com/mybatis/redis-cache/blob/master/src/main/java/org/mybatis/caches/redis/RedisCache.java).
3. MyBatis provides a default `PerpetualCache` implementation of the Cache interface, as well as a dozen of cache decorators, such as LRU, FIFO, Transactional, Blocking, Synchronized, etc.
4. MyBatis provides a `CacheBuilder` to facilitate the use of the decorative building process of the caching layer. 

```java
/**
* Cache interface defined in MyBatis
*/
public interface Cache {

    // get the identifier of this cache
    String getId();

    void putObject(Object key, Object value);

    Object getObject(Object key);

    Object removeObject(Object key);

    void clear();

    int getSize();

    // Any locking needed by the cache must be provided internally by the cache provider.
    default ReadWriteLock getReadWriteLock() {
        return null;
    }
}

/**
* MyBatis provides a naive implementation of Cache interface wrapping around a HashMap. 
*/
public class PerpetualCache implements Cache {
    // implementation omitted ...
}

/**
* MyBatis provides a Blocking Cache Decorator.
*/
public class BlockingCache implements Cache {

    private long timeout;
    private final Cache delegate;
    private final ConcurrentHashMap<Object, CountDownLatch> locks;

    public BlockingCache(Cache delegate) {
        this.delegate = delegate;
        this.locks = new ConcurrentHashMap<>();
    }

    // implementation omitted ...
}

/**
* MyBatis provides a LRU Cache Decorator.
*/
public class LruCache implements Cache {

    private final Cache delegate;
    private Map<Object, Object> keyMap;
    private Object eldestKey;

    public LruCache(Cache delegate) {
        this.delegate = delegate;
        setSize(1024);
    }

    // implementation omitted ...
}

/**
* MyBatis provides a FIFO Cache Decorator.
*/
public class FifoCache implements Cache {

    private final Cache delegate;
    private final Deque<Object> keyList;
    private int size;

    public FifoCache(Cache delegate) {
        this.delegate = delegate;
        this.keyList = new LinkedList<>();
        this.size = 1024;
    }

    // implementation omitted ...
}

/**
* MyBatis provides a Transactional Cache Decorator.
*/
public class TransactionalCache implements Cache {

    private final Cache delegate;
    private boolean clearOnCommit;
    private final Map<Object, Object> entriesToAddOnCommit;
    private final Set<Object> entriesMissedInCache;

    public TransactionalCache(Cache delegate) {
        this.delegate = delegate;
        this.clearOnCommit = false;
        this.entriesToAddOnCommit = new HashMap<>();
        this.entriesMissedInCache = new HashSet<>();
    }

    // implementation omitted ...
}

/**
* MyBatis provides a `CacheBuilder` to facilitate the use of the decorative building process of the caching layer. 
*/
public class CacheBuilder {
    private final String id;
    private Class<? extends Cache> implementation;
    private final List<Class<? extends Cache>> decorators;
    private Long clearInterval;
    private Properties properties;

    public CacheBuilder(String id) {
        this.id = id;
        this.decorators = new ArrayList<>();
    }

    public CacheBuilder implementation(Class<? extends Cache> implementation) {
        this.implementation = implementation;
        return this;
    }

    public CacheBuilder addDecorator(Class<? extends Cache> decorator) {
        if (decorator != null) {
            this.decorators.add(decorator);
        }
        return this;
    }

    public Cache build() {
        // the following implementation is a simplified version of the source
        setDefaultImplementations();
        Cache cache = newBaseCacheInstance(implementation, id);
        setCacheProperties(cache);
        
        for (Class<? extends Cache> decorator : decorators) {
            cache = newCacheDecoratorInstance(decorator, cache);
            setCacheProperties(cache);
        }
        return cache;
    }

    private Cache newCacheDecoratorInstance(Class<? extends Cache> cacheClass, Cache base) {
        Constructor<? extends Cache> cacheConstructor = getCacheDecoratorConstructor(cacheClass);
        try {
            return cacheConstructor.newInstance(base);
        } catch (Exception e) {
            throw new CacheException("Could not instantiate cache decorator (" + cacheClass + "). Cause: " + e, e);
        }
    }
}
```

### Data Source Module

Data Source is a higher level abstraction over database connection. MyBatis provides implementation of `UnpooledDataSource` and `PooledDataSource`.

![]({{ '/styles/images/zhangxin-framework-mybatis/mybatis-datasource-hierarchy.png' | prepend: site.baseurl }})

```java
/**
* DataSource interface is provided in package javax.sql
* 
* The method with signature "getConnection" attempts 
* to establish a connection with the data source that
* this object represents.
*/
public interface DataSource  extends CommonDataSource, Wrapper {

    Connection getConnection(String username, String password) throws SQLException;
}

public interface DataSourceFactory {

    void setProperties(Properties props);

    DataSource getDataSource();
}

public class UnpooledDataSourceFactory implements DataSourceFactory {

    protected DataSource dataSource;
    public UnpooledDataSourceFactory() { this.dataSource = new UnpooledDataSource(); }

    @Override
    public DataSource getDataSource() {
        return dataSource;
    }

    @Override
    public void setProperties(Properties properties) {
        // ...
    }
}

/**
* UnpooledDataSource is a simple implementation of DataSource interface
* provided by MyBatis.
*/
public class UnpooledDataSource implements DataSource {

    private ClassLoader driverClassLoader;
    private Properties driverProperties;
    private static Map<String, Driver> registeredDrivers = new ConcurrentHashMap<>();

    private String driver;
    private String url;
    private String username;
    private String password;

    private Integer defaultTransactionIsolationLevel;
    private Integer defaultNetworkTimeout;

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return doGetConnection(username, password);
    }
}

/**
* PooledDataSourceFactory is exactly the same as UnpooledDataSourceFactory
* except that this.dataSource is a PooledDataSource;
*/
public class PooledDataSourceFactory extends UnpooledDataSourceFactory {

    public PooledDataSourceFactory() {
        this.dataSource = new PooledDataSource();
    }
}

/**
* PooledDataSource implements DataSource interface as well. 
* It's just a composition of an UnpooledDataSource with pool-related fields.
*/
public class PooledDataSource implements DataSource {

    private final PoolState state = new PoolState(this);

    private final UnpooledDataSource dataSource;

    // OPTIONAL CONFIGURATION FIELDS
    protected int poolMaximumActiveConnections = 10;
    protected int poolMaximumIdleConnections = 5;
    protected int poolMaximumCheckoutTime = 20000;
    // ... implementation omitted 

    public PooledDataSource() {
        dataSource = new UnpooledDataSource();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        // pop the next available PooledConnection from the maintained connection pool
        return popConnection(username, password).getProxyConnection();
    }
}

class PooledConnection implements InvocationHandler {
    private static final Class<?>[] IFACES = new Class<?>[] { Connection.class };

    private final PooledDataSource dataSource;
    private final Connection realConnection;
    private final Connection proxyConnection;

    public PooledConnection(Connection connection, PooledDataSource dataSource) {
        this.realConnection = connection;
        this.dataSource = dataSource;
        this.proxyConnection = (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(), IFACES, this);
    }

    // this method returns a dynamically generated proxy connection (implementing Connection interface)
    public Connection getProxyConnection() {
        return proxyConnection;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // implementation omitted ...
    }
}
```

### Transaction Module

MyBatis provided a JdbcTransaction implementation and a ManagedTransaction implementation, which is intended to be overriden by other packages that use MyBatis.

```java
/**
 * Wraps a database connection.
 * Handles the connection lifecycle that comprises: its creation, preparation, commit/rollback and close.
 */
public interface Transaction {

  Connection getConnection() throws SQLException;

  void commit() throws SQLException;

  void rollback() throws SQLException;

  void close() throws SQLException;

  Integer getTimeout() throws SQLException;
}

public interface TransactionFactory {

    default void setProperties(Properties props) {
        // NOOP 
    }

    Transaction newTransaction(Connection conn);
}

public class JdbcTransaction implements Transaction {

    protected Connection connection;
    protected DataSource dataSource;
    protected TransactionIsolationLevel level;
    protected boolean autoCommit;

    public JdbcTransaction(Connection connection) {
        this.connection = connection;
    }

    // implementation omitted ...
}

public class JdbcTransactionFactory implements TransactionFactory {

    @Override
    public Transaction newTransaction(Connection conn) {
        return new JdbcTransaction(conn);
    }
}
```

### Plugin Module

__What is a plugin? What is an interceptor?__
MyBatis allows for customized logic written in an interceptor, which is invoked before and/or after the executions of the following methods. A plugin is composed of a target (Executor/ParameterHandler/ResultSetHandler/StatementHandler) and an interceptor that further specifies the method signatures and the customized logic.
1. Executor
  * update
  * query
  * flushStatements
  * commits
  * rollback
  * getTransaction
  * close
  * isClosed
2. ParameterHandler
  * getParameterObject
  * setParameters
3. ResultSetHandler
  * handleResultSets
  * handleOutputParameters
4. StatementHandler
  * prepare
  * parameterize
  * batch
  * update
  * query

__How to use a plugin?__

1. Implement an [interceptor](https://github.com/zangshayang1/zhangxin/blob/master/JavaFramework/MyBatis/src/main/java/com/zhangxin/framework/mybatis/interceptor/MyInterceptor.java).
2. Configure a plugin by providing an interceptor implementation in [mybatis-config.xml](https://github.com/zangshayang1/zhangxin/blob/master/JavaFramework/MyBatis/src/main/resources/mybatis-config.xml#L33-L35).

__How it works internally? It's beautifully designed.__

```java
/*
* An interface defining the behavior of an interceptor
*/
public interface Interceptor {
    // this method is intended for customized logic
    Object intercept(Invocation var1) throws Throwable;

    // this method takes a target (Executor/ParameterHandler/ResultSetHandler/StatementHandler)
    // and returns a dynamically generated proxy of the same type
    // the above process is common and thus encapsulated into the Plugin.wrap() static method
    default Object plugin(Object target) { return Plugin.wrap(target, this); }

    default void setProperties(Properties properties) {}
}

/*
* An Plugin class implementing InvocationHandler.invoke() method.
*/
public class Plugin implements InvocationHandler {

    private final Object target;
    private final Interceptor interceptor;
    private final Map<Class<?>, Set<Method>> signatureMap;

    private Plugin(Object target, Interceptor interceptor, Map<Class<?>, Set<Method>> signatureMap) {
        this.target = target;
        this.interceptor = interceptor;
        this.signatureMap = signatureMap;
    }

    public static Object wrap(Object target, Interceptor interceptor) {
        // get specified method signatures in implemented interceptor's annotations.
        Map<Class<?>, Set<Method>> signatureMap = getSignatureMap(interceptor);
        Class<?> type = target.getClass();
        Class<?>[] interfaces = getAllInterfaces(type, signatureMap);
        if (interfaces.length > 0) {
            // dynamically generate a proxy using java.lang.reflect.*
            return Proxy.newProxyInstance(
                type.getClassLoader(),
                interfaces,
                new Plugin(target, interceptor, signatureMap));
        }
        return target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            Set<Method> methods = signatureMap.get(method.getDeclaringClass());
            // invoke the interceptor instead when the target method is invoked
            if (methods != null && methods.contains(method)) {
                return interceptor.intercept(new Invocation(target, method, args));
            }
            // otherwise, invoke the target method directly
            return method.invoke(target, args);
        } catch (Exception e) {
            throw ExceptionUtil.unwrapThrowable(e);
        }
    }
}

/*
* A facilitating class handling a chain of interceptors during configuration 
* and target (Executor/ParameterHandler/ResultSetHandler/StatementHandler) initialization
*/
public class InterceptorChain {

    private final List<Interceptor> interceptors = new ArrayList<>();

    /*
    * This method is used at executor dynamic proxy generating time
    * Same applies to Executor/ParameterHandler/ResultSetHandler/StatementHandler
    *
    * public Executor newExecutor(Transaction transaction, ExecutorType executorType) {
    *     ...
    *     Executor executor = (Executor) interceptorChain.pluginAll(executor);
    *     ...
    *     return executor;
    * }
    */ 
    public Object pluginAll(Object target) {
        for (Interceptor interceptor : interceptors) {
            target = interceptor.plugin(target);
        }
        return target;
    }

    /*
    * This method is used at configuration parsing time
    * 
    * public class Configuration {        
    *     ...
    *     public void addInterceptor(Interceptor interceptor) {
    *         interceptorChain.addInterceptor(interceptor);
    *     }
    * }
    */
    public void addInterceptor(Interceptor interceptor) {
        interceptors.add(interceptor);
    }

    public List<Interceptor> getInterceptors() {
        return Collections.unmodifiableList(interceptors);
    }
}
```


### Integration with Spring

__Configuration and Demo__
1. [spring-config.xml](https://github.com/zangshayang1/zhangxin/blob/master/JavaFramework/MyBatis/src/main/resources/spring-config.xml)
2. [Running example](https://github.com/zangshayang1/zhangxin/blob/master/JavaFramework/MyBatis/src/test/java/com/zhangxin/framework/mybatis/SpringIntegrationDemo.java)

__Adaptor and Enhancer__
1. `org.mybatis.spring.SqlSessionFactoryBean` makes SqlSessionFactory available in Spring IOC.  
2. `org.mybatis.spring.mapper.MapperFactoryBean` makes user-defined mappers available in Spring IOC.  
3. `org.mybatis.spring.SqlSessionTemplate` provides thread safety on top of `DefaultSqlSession`. Its implementation used `ThreadLocal` rather than `synchronized` or `lock` to provide performance benefit in addition to thread safety. Implementation skeleton below.

```java
public class SqlSessionTemplate implements SqlSession, DisposableBean {

    private final SqlSessionFactory sqlSessionFactory;
    private final SqlSession sqlSessionProxy;

    public SqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
        this.sqlSessionProxy = (SqlSession) Proxy.newProxyInstance(
            SqlSessionFactory.class.getClassLoader(), 
            new Class[]{SqlSession.class}, 
            new SqlSessionTemplate.SqlSessionInterceptor());
    }  

    private class SqlSessionInterceptor implements InvocationHandler {
        private SqlSessionInterceptor() {}

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            SqlSession sqlSession = SqlSessionUtils.getSqlSession(SqlSessionTemplate.this.sqlSessionFactory);

            Object unwrapped;
            try {
                Object result = method.invoke(sqlSession, args);
                if (!SqlSessionUtils.isSqlSessionTransactional(
                    sqlSession, SqlSessionTemplate.this.sqlSessionFactory)) {
                    
                    sqlSession.commit(true);
                }
                unwrapped = result;
            } catch (Throwable var11) {
                // handling omitted ...
                throw (Throwable)unwrapped;
            } finally {
                if(sqlSession != null) {
                    SqlSessionUtils.closeSqlSession(sqlSession, SqlSessionTemplate.this.sqlSessionFactory);
                }
            }
            return unwrapped;
        }
    }
}

public final class SqlSessionUtils {
    // implementation omitted ...

    public static SqlSession getSqlSession(SqlSessionFactory sessionFactory) {
        SqlSessionHolder holder = (SqlSessionHolder) TransactionSynchronizationManager.getResource(sessionFactory);
        SqlSession session = sessionHolder(holder);
        if(session != null) {
            return session;
        } else {
            // new thread new session
            session = sessionFactory.openSession();
            registerSessionHolder(sessionFactory, session);
            return session;
        }
    }
}

public abstract class TransactionSynchronizationManager {

    // Use ThreadLocal to control thread safety
    private static final ThreadLocal<Map<Object, Object>> resources = new NamedThreadLocal("Transactional resources");

    @Nullable
    public static Object getResource(Object key) {
        Object actualKey = TransactionSynchronizationUtils.unwrapResourceIfNecessary(key);
        Object value = doGetResource(actualKey);
        return value;
    }

    @Nullable
    private static Object doGetResource(Object actualKey) {
        Map<Object, Object> map = (Map) resources.get();
        if(map == null) {
            return null;
        } else {
            Object value = map.get(actualKey);
            // handling omitted ...
            return value;
        }
    }    
}
```