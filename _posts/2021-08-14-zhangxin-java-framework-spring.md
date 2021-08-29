---
layout: post
title: Zhangxin Java Framework - Spring
date:   2021-08-14 19:30:00 -0700
categories: reference
tag: java
---

* content
{:toc}



Updated: 2021-08-14

# Spring Design

### Spring Design - IOC Container (Bean Factory)

__What is IOC?__  
Inversion of control includes life cycle management of different types of objects and providing access to them.
IOC Container is also known as Bean Factory. 

```java
// regular
Vehicle v = new Lamboghini()

// IOC
Vehicle v = ioc.get('vehicle')
```

__What is the benefit of IOC?__  
Decouple the creation of an object from its specific implementation, promoting programming to interface.

__How it works?__  
1. `BeanDefinition` defines a bean and maps to external bean configuration files.
2. `BeanFactory` does the following:
  * allows bean registration and external access
  * maintains a mapping between bean names and bean definitions
  * maintains a mapping between bean types (including interface and superclass) and bean names
  * maintains a SingletonBeanMap between singleton-scope bean names and bean instances

![]({{ '/styles/images/zhangxin-framework-spring/spring-ioc-hierarchy.png' | prepend: site.baseurl }})

```java

public interface BeanDefinition {

    // Class<?> type = bd.getBeanClass();
    // Object instance = null;
    // if (type != null) {
    //     if (StringUtils.isBlank(bd.getFactoryMethodName())) {
    //         instance = this.createInstanceByConstructor(bd); // create by constructor
    //     } else {
    //         instance = this.createInstanceByStaticFactoryMethod(bd); // create by static factory method
    //     }
    // } else {
    //     instance = this.createInstanceByFactoryBean(bd); // create by non-static factory method
    // }
    // 
    // this.doInit(bd, instance); // initialization
    Class<?> getBeanClass();
    String getFactoryBeanName();
    String getFactoryMethodName();

    String getInitMethodName();
    String getDestroyMethodName();

    String SCOPE_SINGLETION = "singleton";
    String SCOPE_PROTOTYPE = "prototype";
    // Every time a prototype bean is requested from IOC container
    // a new instance will be returned 
    String getScope();
    boolean isSingleton();
    boolean isPrototype();

    // When serveral instances of the same type coexist in the container and there is no further info which one is requested
    // return the one with this label set to true
    boolean isPrimary();

    // for locating proper constructor
    List<?> getConstructorArgumentValues();

    // store prototype bean's constructor or factory method (once it's determined) to speed up future creation
	Constructor<?> getConstructor();
	void setConstructor(Constructor<?> constructor);
	Method getFactoryMethod();
	void setFactoryMethod(Method factoryMethod);

    // for instance level attribute
    List<PropertyValue> getPropertyValues();

    default boolean validate() {
        if (this.getBeanClass() == null) {
            if (StringUtils.isBlank(getFactoryBeanName()) || StringUtils.isBlank(getFactoryMethodName())) {
                return false;
            }
        }

        if (this.getBeanClass() != null && StringUtils.isNotBlank(getFactoryBeanName())) {
            return false;
        }

        return true;
}
}

public class BeanDefinitionRegistException extends Exception {
    // implementation omitted ... 

    // throw when BeanDefinition is invalid or beanName already existed during registration
}

public interface BeanDefinitionRegistry {

	void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) throws BeanDefinitionRegistException;

	BeanDefinition getBeanDefinition(String beanName);

	boolean containsBeanDefinition(String beanName);
}


public interface BeanFactory {
    // Get by bean name
	Object getBean(String name) throws Exception; 

    /**
    * Get by bean type, it requires that:
    * 1. A mapping between a bean type (including interface and superclass) to a set of bean names, Map<Class, Set<String>>
    * 2. The above mapping needs to be built between bean registrartion completion and bean instantiation
    * 3. A bean type can be acquired by the bean class if provided or by the return type of the corresponding factory method.
    * 4. When only one instance needs to be returned out of several of the same type, return the one with primary label set to true.
    */ 
	<T> T getBean(Class<T> type)throws Exception; 

    // Corresponding to the above comment 1.
	<T> Map<String,T> getBeansOfType(Class<T> type)throws Exception;

    // Corresponding to the above comment 3.
	Class<?> getType(String name) throws Exception;
}
```

### Spring Design - Dependency Injection 

__How To Create Beans Depending On Other Beans?__

Beans are created according to their `BeanDefinition`. If a bean is created using its class constructor, `BeanDefinition.getConstructorArgumentValues()` will return a list of arguments. If the constructor requires another bean as an argument, a `BeanReference` will be returned as a reference to the target bean. The following demonstrated how it's done through bean class constructor. Same process goes for static factory method and non-static factory method. 

```java
public class DefaultBeanFactoryImpl implements BeanFactory, BeanDefinitionRegistry, Closeable {

    // ... 

    private Object createInstanceByConstructor(BeanDefinition bd) throws Exception {
        
        Object[] args = getRealConstructorArgumentValues(bd);

        return this.determineConstructor(bd, args).newInstance(args);
    }

    /*
    * Get real arguement values based on original argument values from bean definition.
    */
    private Object[] getRealConstructorArgumentValues(BeanDefinition bd) throws Exception {

        List<?> originalValues = bd.getConstructorArgumentValues();

        Object[] realValues = new Object[originalValues.size()];
        int i = 0;
        for (Object v : originalValues) {
            realValues[i++] = getOneRealArgumentValue(v);
        }
        return realValues;
    }

    /*
    * Translate each argument value from bean definition to real value
    */
    private Object getOneRealArgumentValue(Object originalValue) throws Exception {
        Object realValue = null;
        if (originalValue != null) {
            if (originalValue instanceof BeanReference) {
                // handle BeanReference, get one by reference or create one recursively
                BeanReference br = (BeanReference) originalValue;
                // getBean() is a public method that returns a bean if it already existed; or create one otherwise.
                if (StringUtils.isNotBlank(br.getBeanName())) {
                    realValue = this.getBean(br.getBeanName());
                } else if (br.getType() != null) {
                    realValue = this.getBean(br.getType());
                }
            } else if (originalValue instanceof Object[]) {
                // handle array
            } else if (originalValue instanceof Collection) {
                // handle Collection
            } else if (originalValue instanceof Map) {
                // handle Map
            } else {
                // for other primitive or boxed primitive values
                realValue = originalValue;
            }
        }
        return realValue;
    }

    /*
    * Return the right constructor based on argument values from bean definition.
    */
    private Constructor<?> determineConstructor(BeanDefinition bd, Object[] args) throws Exception {

        Constructor<?> ct = null;

        if (args == null) {
            return bd.getBeanClass().getConstructor(null);
        }

        // if the bean has been previously created, bean definition keeps a record of what constructor is used
        ct = bd.getConstructor();
        if (ct != null) {
            return ct;
        }

        // find the right constructor by providing a Class<?>[] array
        Class<?>[] paramTypes = new Class[args.length];
        int j = 0;
        for (Object p : args) {
            paramTypes[j++] = p.getClass();
        }
        try {
            ct = bd.getBeanClass().getConstructor(paramTypes);
        } catch (Exception e) {
        }

        // if the above doesn't yeild a constructor
        // loop through all the constructors anc check if each real argument is assignable to each parameter type
        // including compatible interface or superclass
        if (ct == null) {
            outer:
            for (Constructor<?> ct0 : bd.getBeanClass().getConstructors()) {
                Class<?>[] paramterTypes = ct0.getParameterTypes();
                if (paramterTypes.length == args.length) {
                    for (int i = 0; i < paramterTypes.length; i++) {
                        if (!paramterTypes[i].isAssignableFrom(args[i].getClass())) {
                            continue outer; // continue outer loop
                        }
                    }
                    ct = ct0;
                    break outer;
                }
            }
        }

        if (ct != null) {
            bd.setConstructor(ct)
            return ct;
        } else {
            throw new Exception("No constructor can be found: " + bd);
        }
    }
}
```

---

__What If There Is a Cyclic Dependency?__

Use a `ThreadLocal<Set<String>>` to track its dependency creation footprint at each recursive invocation of `doGetBean` method for detection of cyclic dependency. 

```java
public class DefaultBeanFactoryImpl implements BeanFactory, BeanDefinitionRegistry, Closeable {

    // ... 

    protected Map<String, BeanDefinition> beanDefintionMap = new ConcurrentHashMap<>(256);

    private ThreadLocal<Set<String>> buildingBeansRecordor = new ThreadLocal<>();

    protected Object doGetBean(String beanName) throws Exception {
        Object instance = singletonBeanMap.get(beanName);

        if (instance != null) {
            return instance;
        }
        
        BeanDefinition bd = beanDefintionMap.get(beanName);(beanName);

        // get a set of bean names that lies in the dependency path of the original target bean generation from this thread
        Set<String> buildingBeans = buildingBeansRecordor.get();
        if (buildingBeans == null) {
            buildingBeans = new HashSet<>();
            buildingBeansRecordor.set(buildingBeans);
        }

        // throw on cyclic dependency
        if (buildingBeans.contains(beanName)) {
            throw new Exception(beanName + " has cyclic dependency！" + buildingBeans);
        }
        // note down dependency trace at "doGetBean" level
        buildingBeans.add(beanName);

        // create singleton - double check lock
        if(bd.isSingleton()) {
            synchronized (this.singletonBeanMap) {
                instance = this.singletonBeanMap.get(beanName);
                if(instance == null) {
                    instance = doCreateInstance(bd);
                    this.singletonBeanMap.put(beanName,instance);
                }
            }
        }
        else {
            instance = doCreateInstance(bd);
        }

        // remove dependency trace
        buildingBeans.remove(beanName);

        return instance;
    }
}
```

---

__What About Instance Level Attribute?__

```java
public class DefaultBeanFactoryImpl implements BeanFactory, BeanDefinitionRegistry, Closeable {

    // ...

    private ThreadLocal<Map<String, Object>> earlyExposeBuildingBeans = new ThreadLocal<>();    

    private ThreadLocal<Set<String>> buildingBeansRecordor = new ThreadLocal<>();    

    private Map<String, Object> singletonBeanMap = new ConcurrentHashMap<>(256);

    // protected Object doGetBean(String beanName) throws Exception {        
    //     // attempt to get bean from singletonBeanMap
    //     // attempt to get bean from earlyExposeBuildingBeans (for instance attributes cyclic dependency)
    //     // check on constructor cyclic dependency
    //     // add bean name to buildingBeansRecordor
    //     // create singleton or execute doCreateInstance(...)
    //     // remove bean name from buildingBeansRecordor
    //     return instance;
    // }    

    private Object doCreateInstance(String beanName,BeanDefinition bd) throws Exception {
        Class<?> type = bd.getBeanClass();
        Object instance = null;
        if (type != null) {
            if (StringUtils.isBlank(bd.getFactoryMethodName())) {
                // create instance by constructor
                instance = createInstanceByConstructor(bd);
            } else {
                // create instance by static factory method
                instance = createInstanceByStaticFactoryMethod(bd);
            }
        } else {
            // create instance by non-static factory method
            instance = createInstanceByFactoryBean(bd);
        }

        // After an instance is successfully created, add it to earlyExposeBuildingBeans threadlocal map
        // so that it can be used as property values for other beans.
        doEarlyExposeBuildingBeans(beanName, instance);

        // set properties for the instance according to the bean definition
        setPropertyValues(bd, instance);

        removeEarlyExposeBuildingBeans(beanName,instance);

        // execute init for instance
        doInit(bd, instance);

        return instance;
    }

    /*
    * Set property values for a created instance.
    * Property values come from bean definition. Method getOneRealArgumentValue(...) is reused here.
    * If property value is a bean reference, it will recursively invoke getBean public method.
    * From programming perspective, it is OK to have cyclic dependency between property values of two instances.
    * From execution perspective, it will cause stack overflow 
    * because it never returns an instance reference before the completion of this method setPropertyValues(...)
    * That's why we doEarlyExposeBuildingBeans(...) after instance creation and before calling setPropertyValues(...)
    */
    private void setPropertyValues(BeanDefinition bd, Object instance) throws Exception {
        for (PropertyValue pv : bd.getPropertyValues()) {
            Class<?> clazz = instance.getClass();
            Field p = clazz.getDeclaredField(pv.getName());
            // force accessibility
            p.setAccessible(true); 
            // getOneRealArgumentValue is previously defined to translate a bean reference to a real instance
            // which might encounter cyclic dependency issue again
            p.set(instance, getOneRealArgumentValue(pv.getValue()));
        }
    }

    private void doEarlyExposeBuildingBeans(String beanName, Object instance) {
        Map<String,Object> earlyExposeBuildingBeansMap = earlyExposeBuildingBeans.get();
        if(earlyExposeBuildingBeansMap == null) {
            earlyExposeBuildingBeansMap = new HashMap<>();
            earlyExposeBuildingBeans.set(earlyExposeBuildingBeansMap);
        }
        earlyExposeBuildingBeansMap.put(beanName, instance);
    }

    private void removeEarlyExposeBuildingBeans(String beanName, Object instance) {
        earlyExposeBuildingBeans.get().remove(beanName);
    }    
}
```

### Spring Design - AOP Enhancement

`Spring` borrowed AOP (aspect oriented programming )concepts such as Advice, Pointcut, Advisor, etc from `AspectJ` but it followed dynamic proxy pattern to apply post-instantiation enhancement to beans at RUNTIME. Whereas AspectJ does it at COMPILE time.

__AOP Example__  

I made an application containing all the business logic.

```java
package com.chch.myapp;

public class MyApp {
    
    public int run(int a, int b) {
        return a + b;
    }
}
```

Now I want to have an `aspect` that does logging for return value weaved into arbitrary function within my application codebase.

```java
package com.chch.myapp.aspects;

public class LogAfterReturnAspect {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    public void afterReturn(Object returnValue) throws Throwable {
        logger.info("value return was {}",  returnValue);
    }
}
```

I can incorporate the following bean configuration into `application.xml`. It will be read into `SpringApplicationContext` and the bean instantiation will happen in Spring `BeanFactory` (IOC Container). The functional enhancement will be weaved into the target function through this process via dynamic proxy pattern.

```xml
<bean id="myAppBeanId" class="com.chch.myapp.MyApp" />
<bean id="logAfterReturnAspectBeanId" 
    class="com.chch.myapp.aspects.LogAfterReturnAspect" />
<aop:config>
    <aop:aspect id="aspects" ref="doAfterReturningAspect">
       <aop:pointcut id="pointCut1" expression=
        "execution(* com.chch.myapp.MyApp+.*(..))"/>
       <aop:after-returning method="afterReturn" 
        returning="returnValue" pointcut-ref="pointCut1"/>
    </aop:aspect>
</aop:config>
```

---

__BeanFactory__ 

`BeanFactory` interface defines `registerBeanPostProcessor(BeanPostProcessor bpp)` behavior that allows `BeanPostProcessor` to be accessible within the factory. The factory implementation applies post bean enhancements via `BeanPostProcessor` after bean instantiation.

```java
public interface BeanFactory {
    
    // other method omitted ...

    void registerBeanPostProcessor(BeanPostProcessor bpp);

    List<T> getBeansOfTypeList(Class<T> clazz);
}


public interface BeanPostProcessor {

	default Object postProcessBeforeInitialization(Object bean, String beanName) throws Throwable {
		return bean;
	}

	default Object postProcessAfterInitialization(Object bean, String beanName) throws Throwable {
		return bean;
	}
}

public class DefaultBeanFactory implements BeanFactory, BeanDefinitionRegistry, Closeable {

    private Object doCreateInstance(String beanName, BeanDefinition bd) throws Throwable {
        
        // create bean instance with properties ... omitted ... refer to previous charpters if needed

        // pre-init enhancement
        instance = applyPostProcessBeforeInitialization(instance, beanName);

        doInit(bd, instance);

        // post-init enhancement
        instance = applyPostProcessAfterInitialization(instance, beanName);

        return instance;
    }

    private Object applyPostProcessBeforeInitialization(Object bean, String beanName) throws Throwable {
        for (BeanPostProcessor bpp : this.beanPostProcessors) {
            bean = bpp.postProcessBeforeInitialization(bean, beanName);
        }
        return bean;
    }

    private Object applyPostProcessAfterInitialization(Object bean, String beanName) throws Throwable {
        for (BeanPostProcessor bpp : this.beanPostProcessors) {
            bean = bpp.postProcessAfterInitialization(bean, beanName);
        }
        return bean;
    }
}
```

---

__Bean Post Processor__  

`BeanPostProcessor` is used to enhance beans' initialization and functionalities after an instance is created within BeanFactory. It does so by dynamically creating a proxy of the bean with enhancements. An enhancement (modeled as `PointcutAdvisor`) consists of:
1. where (modeled as `Pointcut`)
  * which type of beans
  * which methods
2. what (modeled as `Advice`)
  * before a method invocation
  * after a method invocation
  * before and after a method invocation
  * after an exception is throw during the invocation
  * after a method returns normally

```java
// BeforeAdvice
Object ret = null;
try {
    ret = func();
    // AfterReturnAdvice
 } catch (Exception e) {
    // ThrowsAdvice
} finally {
    // AfterAdvice
}

public interface PointcutAdvisor extends Advisor {
	Pointcut getPointcut();
}

public interface Pointcut {
	boolean matchsClass(Class<?> targetClass);
	boolean matchsMethod(Method method, Class<?> targetClass);
}

public interface Advice {}

public interface AfterAdvice extends Advice {
	void after(Object returnValue, Method method, Object[] args, Object target) throws Throwable;
}
public interface BeforeAdvice extends Advice {
	void before(Method method, Object[] args, Object target) throws Throwable;
}
public interface AroundAdvice extends Advice {
	Object invoke(Method method, Object[] args, Object target) throws Throwable;
}
public interface ThrowsAdvice extends Advice {
    void afterThrow(Method method, Object[] args, Object target, Exception ex) throws Throwable;
}
public interface AfterReturnAdvice extends Advice {
	void afterReturn(Object returnValue, Method method, Object[] args, Object target) throws Throwable;
}
```

A `BeanPostProcessor` implements: 
1. `AdvisorRegistry` interface so that it allows registration of advisors.
2. `BeanFactoryAware` interface so that it has access to a `BeanFactory`, which brings the access to all the advisors (an `Advisor` instance is also a bean generated from BeanDefinition).

Either way, the `BeanPostProcessor` iterates over all the advisors and check if there is matched advisor for a given bean. If there is, it will dynamically generate a proxy and apply the corresponding enhancement. Note that, multiple enhancement can be applied to a type of beans. This post bean enhancement is also known as `weaving` in spring. 

```java
public interface Advisor {
	String getAdviceBeanName();
	String getExpression();
}
public interface AdvisorRegistry {
	public void registAdvisor(Advisor ad);
	public List<Advisor> getAdvisors();
}

public interface BeanFactoryAware extends Aware {
	void setBeanFactory(BeanFactory bf);
}
```

![]({{ '/styles/images/zhangxin-framework-spring/spring-aop-weaving-bean-post-processor.png' | prepend: site.baseurl }})

![]({{ '/styles/images/zhangxin-framework-spring/spring-aop-weaving-logic.png' | prepend: site.baseurl }})

---

__Dynamic Proxy__  

The implementation of `BeanPostProcessor` creates dynamic proxy of the target bean. Dynamic proxy creation is faciliated by: 
1. [JDK "java.lang.reflect.Proxy" generates a proxy implementing one or more interface(s)](https://github.com/zangshayang1/zhangxin/blob/master/JavaCore/src/main/com/zhangxin/javacore/design/dynamicproxy/reflect/DynamicProxyDemo.java)
2. [Cglib generates a proxy inheriting a target class](https://github.com/zangshayang1/zhangxin/blob/master/JavaCore/src/main/com/zhangxin/javacore/design/dynamicproxy/cglib/CglibDemo.java)
Therefore, a `AopProxyFactory` is called in to `createAopProxy` using different approaches under different situations. The implementation of `AopProxy` is then responsible for:
1. provide access to a dynamically created proxy over target class or interface
2. implements an `invoke` method that incorporates enhancements defined in `PointcutAdvisor`, in addition to the original target's method.


```java
public class DefaultBeanPostProcessor implements AdvisorRegistry, BeanPostProcessor, BeanFactoryAware {

	private BeanFactory beanFactory;

	private List<Advisor> advisors;

	private Object createProxy(Object bean, String beanName, List<Advisor> matchAdvisors) throws Throwable {
		
		return AopProxyFactory.getDefaultAopProxyFactory()
            .createAopProxy(bean, beanName, matchAdvisors, beanFactory)
            .getProxy();
	}
}

public interface AopProxyFactory {

	AopProxy createAopProxy(Object bean, String beanName, List<Advisor> matchAdvisors, BeanFactory beanFactory) throws Throwable;

	static AopProxyFactory getDefaultAopProxyFactory() {
		return new DefaultAopProxyFactory();
	}
}

public interface AopProxy {
	Object getProxy();
}

public class JdkDynamicAopProxy implements AopProxy, InvocationHandler {
	
    private Object target;
	private List<Advisor> matchAdvisors;
	private BeanFactory beanFactory;

    @Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		// design pattern - chain of responsibility
        AopProxyUtils.applyAdvices(target, method, args, matchAdvisors, proxy, beanFactory);
        return method.invoke(target, args);
	}

	@Override
	public Object getProxy() {
        return Proxy.newProxyInstance(target.getClass().getClassLoader(), target.getClass().getInterfaces(), this);
	}
}

public class CglibDynamicAopProxy implements AopProxy, MethodInterceptor {
    // implementation omitted ...
}
```

# Spring Source

### Spring Source - Bean Instantiation

![]({{ '/styles/images/zhangxin-framework-spring/spring-bean-instantiation.JPG' | prepend: site.baseurl }})

---

```java
public abstract class AbstractApplicationContext extends DefaultResourceLoader implements ConfigurableApplicationContext {
    
    // ...

    @Override
    public void refresh() throws BeansException, IllegalStateException {
        synchronized (this.startupShutdownMonitor) {
            StartupStep contextRefresh = this.applicationStartup.start("spring.context.refresh");

            // Prepare this context for refreshing.
            prepareRefresh();

            // Tell the subclass to refresh the internal bean factory.
            ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();

            // Prepare the bean factory for use in this context.
            prepareBeanFactory(beanFactory);

            try {
                // Allows post-processing of the bean factory in context subclasses.
                postProcessBeanFactory(beanFactory);

                StartupStep beanPostProcess = this.applicationStartup.start("spring.context.beans.post-process");
                // Invoke factory processors registered as beans in the context.
                invokeBeanFactoryPostProcessors(beanFactory);

                // Register bean processors that intercept bean creation.
                registerBeanPostProcessors(beanFactory);
                beanPostProcess.end();

                // Initialize message source for this context.
                initMessageSource();

                // Initialize event multicaster for this context.
                initApplicationEventMulticaster();

                // Initialize other special beans in specific context subclasses.
                onRefresh();

                // Check for listener beans and register them.
                registerListeners();

                // Instantiate all remaining (non-lazy-init) singletons.
                finishBeanFactoryInitialization(beanFactory);

                // Last step: publish corresponding event.
                finishRefresh();
            }

            catch (BeansException ex) {
                if (logger.isWarnEnabled()) { 
                    logger.warn("Exception encountered during context initialization - " + 
                    "cancelling refresh attempt: " + ex);
                }

                // Destroy already created singletons to avoid dangling resources.
                destroyBeans();

                // Reset 'active' flag.
                cancelRefresh(ex);

                // Propagate exception to caller.
                throw ex;
            }

            finally {
                // Reset common introspection caches in Spring's core, since we
                // might not ever need metadata for singleton beans anymore...
                resetCommonCaches();
                contextRefresh.end();
            }
        }
    }
}
```

### Spring Source - Bean Post Process Hierarchy

__BeanPostProcessor__

```java
/**
 * Factory hook that allows for custom modification of new bean instances,
 * e.g. checking for marker interfaces or wrapping them with proxies.
 *
 * <p>ApplicationContexts can autodetect BeanPostProcessor beans in their
 * bean definitions and apply them to any beans subsequently created.
 * Plain bean factories allow for programmatic registration of post-processors,
 * applying to all beans created through this factory.
 */
public interface BeanPostProcessor {

	/**
	 * Apply this BeanPostProcessor to the given new bean instance <i>before</i> any bean
	 * initialization callbacks
     */
	default Object postProcessBeforeInitialization(Object bean, String beanName) throws Throwable {
		return bean;
	}

	/**
	 * Apply this BeanPostProcessor to the given new bean instance <i>after</i> any bean
	 * initialization callbacks 
     */
	default Object postProcessAfterInitialization(Object bean, String beanName) throws Throwable {
		return bean;
	}
}
```

__InstantiationAwareBeanPostProcessor__

```java
/**
 * Subinterface of {@link BeanPostProcessor} that adds a before-instantiation callback,
 * and a callback after instantiation but before explicit properties are set or
 * autowiring occurs.
 *
 * <p>Typically used to suppress default instantiation for specific target beans,
 * for example to create proxies with special TargetSources (pooling targets,
 * lazily initializing targets, etc), or to implement additional injection strategies
 * such as field injection.
 */
public interface InstantiationAwareBeanPostProcessor extends BeanPostProcessor {
	
    /**
	 * Apply this BeanPostProcessor <i>before the target bean gets instantiated</i>.
	 * The returned bean object may be a proxy to use instead of the target bean,
	 * effectively suppressing default instantiation of the target bean.
     */
	default Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
		return null;
	}

	/**
	 * Perform operations after the bean has been instantiated, via a constructor or factory method,
	 * but before Spring property population (from explicit properties or autowiring) occurs.
	 * <p>This is the ideal callback for performing custom field injection on the given bean
	 * instance, right before Spring's autowiring kicks in.
	 */
	default boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
		return true;
	}

	/**
	 * Post-process the given property values before the factory applies them
	 * to the given bean, without any need for property descriptors.
	 */
	default PropertyValues postProcessProperties(PropertyValues pvs, Object bean, String beanName)
			throws BeansException {

		return null;
	}
}
```

__SmartInstantiationAwareBeanPostProcessor__

```java
public interface SmartInstantiationAwareBeanPostProcessor extends InstantiationAwareBeanPostProcessor {

	/**
	 * Predict the type of the bean to be eventually returned from this
	 * processor's {@link #postProcessBeforeInstantiation} callback.
	 */
	default Class<?> predictBeanType(Class<?> beanClass, String beanName) throws BeansException {
		return null;
	}

	/**
	 * Determine the candidate constructors to use for the given bean.
	 */
	@Nullable
	default Constructor<?>[] determineCandidateConstructors(Class<?> beanClass, String beanName)
			throws BeansException {

		return null;
	}

	/**
	 * Obtain a reference for early access to the specified bean,
	 * typically for the purpose of resolving a circular reference.
	 * <p>This callback gives post-processors a chance to expose a wrapper
	 * early - that is, before the target bean instance is fully initialized.
	 */
	default Object getEarlyBeanReference(Object bean, String beanName) throws BeansException {
		return bean;
	}    
}
```

__AbstractAutoProxyCreator__

```java
/**
 * A {@link BeanPostProcessor} implementation
 * that wraps each eligible bean with an AOP proxy, delegating to specified interceptors
 * before invoking the bean itself.
 *
 * <p>This class distinguishes between "common" interceptors: shared for all proxies it
 * creates, and "specific" interceptors: unique per bean instance. 
 *
 * <p>Such auto-proxying is particularly useful if there's a large number of beans that
 * need to be wrapped with similar proxies, i.e. delegating to the same interceptors.
 * Instead of x repetitive proxy definitions for x target beans, you can register
 * one single such post processor with the bean factory to achieve the same effect.
 */
@SuppressWarnings("serial")
public abstract class AbstractAutoProxyCreator extends ProxyProcessorSupport
		implements SmartInstantiationAwareBeanPostProcessor, BeanFactoryAware {

	/**
	 * Return whether the given bean is to be proxied, what additional
	 * advices (e.g. AOP Alliance interceptors) and advisors to apply.
	 * @param beanClass the class of the bean to advise
	 * @param beanName the name of the bean
	 * @param customTargetSource the TargetSource returned by the
	 * {@link #getCustomTargetSource} method: may be ignored.
	 * Will be {@code null} if no custom target source is in use.
	 * @return an array of additional interceptors for the particular bean;
	 */        
	protected abstract Object[] getAdvicesAndAdvisorsForBean(Class<?> beanClass, String beanName,
			@Nullable TargetSource customTargetSource) throws BeansException;
}
```

__AbstractAdvisorAutoProxyCreator__

```java
/**
 * Generic auto proxy creator that builds AOP proxies for specific beans
 * based on detected Advisors for each bean.
 *
 * <p>Subclasses may override the {@link #findCandidateAdvisors()} method to
 * return a custom list of Advisors applying to any object. Subclasses can
 * also override the inherited {@link #shouldSkip} method to exclude certain
 * objects from auto-proxying.
 */
public abstract class AbstractAdvisorAutoProxyCreator extends AbstractAutoProxyCreator {
    // ...
}
```

__AspectJAwareAdvisorAutoProxyCreator__

```java
/**
 * A subclass of {@link AbstractAdvisorAutoProxyCreator}
 * that exposes AspectJ's invocation context and understands AspectJ's rules
 * for advice precedence when multiple pieces of advice come from the same aspect.
 */
public class AspectJAwareAdvisorAutoProxyCreator extends AbstractAdvisorAutoProxyCreator {
    // ...
}
```

__AnnotationAwareAspectJAutoProxyCreator__  

```java
/**
 * A subclass of {@link AspectJAwareAdvisorAutoProxyCreator} that processes all AspectJ
 * annotation aspects in the current application context, as well as Spring Advisors.
 *
 * <p>Any AspectJ annotated classes will automatically be recognized, and their
 * advice applied if Spring AOP's proxy-based model is capable of applying it.
 * This covers method execution joinpoints.
 */
public class AnnotationAwareAspectJAutoProxyCreator extends AspectJAwareAdvisorAutoProxyCreator {
    // ...
}
```

### Spring Source - Different Ways of Importing Java Beans into IOC Container

__Import From XML file__

```java
public class SpringIocFromXmlDemo {
    public static void main(String[] args) {
        
        // Spring IOC Initialization, taking Java bean definitions from resources/applicationContext.xml.
        ApplicationContext ac = new ClassPathXmlApplicationContext("applicationContext.xml");
        
        // "resources/applicationContext.xml" specifies the following Java bean definition
        //
        // <bean id="person" class="com.company.app.bean.Person">
        //     <property name="name" value="szang"></property>
        //     <property name="age" value="18"></property>
        // </bean>
        Person p = (Person)ac.getBean("person");
    }
}
```

---

__Import Using Spring Native Import Annotation__
```java
@Configuration
@Import({ Person.class, User.class })
public class SpringIocFromImportAnnotation {
    public static void main(String[] args) {

        // Spring IOC Initialization, taking Java bean definitions from @Import Annotation
        ApplicationContext ac = new AnnotationConfigApplicationContext(SpringIocFromImportAnnotation.class);

        Person p = (Person)ac.getBean(Person.class);
        User u = (User)ac.getBean(User.class);
    }
}
```

---

__Import From Customized Bean Registrar__
```java
@Configuration
@Import(MyImportRegistrar.class) // customized Java bean registrar
public class SpringIocFromImportAnnotation {
    public static void main(String[] args) {

        // omit ... same as the above
    }
}

// ImportBeanDefinitionRegistrar is an interface defined within Spring Framework
// Customized MyImportRegistrar allows better clarity and conditional logic during import
public class MyImportRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
        BeanDefinitionRegistry registry) {

        registry.registerBeanDefinition("person", new RootBeanDefinition(Person.class));
        registry.registerBeanDefinition("user", new RootBeanDefinition(User.class));
    }
}
```

---

__Import From External Module__

The following example shows, during a Spring app starting time, how to import Java beans from externals into the app IOC container. 

```java
// ----- Mvn Project 1 -----

// com/company/project1/App.java
@SpringBootApplication
public class App {
    public static void main(String[] args) {
        ApplicationContext applicationContext = SpringApplication.run(App.class, args);
        JavaBean javaBean = (JavaBean)applicationContext.getBean(JavaBean.class);
    }
}

// ----- Mvn Project 2 -----

// com/company/project2/bean/JavaBean.java
@Configuration
@ComponentScan
public class JavaBean {
}

// resources/META-INF/spring.factories
org.springframework.boot.autoconfigure.EnableAutoConfiguration=com.company.project2.bean.JavaBean

// the import can be conditional on if some other class is in this project's classpath or not
// com/company/project2/condition/ConditionClass.java
public class ConditionClass {
}

// the rules are defined in another property file
// resources/META-INF/spring-autoconfigure-metadata.properties
com.company.project2.bean.JavaBean.ConditionalOnClass=com.company.project2.condition.ConditionClass

// ----- Real World Use Case -----

// in org.springframework.boot:spring-boot-autoconfigure.jar:resources/META-INF/spring-autoconfigure-metadata.properties
// "RedisCacheConfiguration" is imported conditional on "RedisConnectionFactory"
org.springframework.boot.autoconfigure.cache.RedisCacheConfiguration.ConditionalOnBean=org.springframework.data.redis.connection.RedisConnectionFactory
```

### Spring Source - Bean Attribute Cyclic Dependency

__Complete Instance Instantiation with Attribute Cyclic Dependency__  
A's complete instantiation with attributes depends on B as B is an attribute of A. B's complete instantiation with attributes depends on A as A is an attribute of B. In this case, Spring will not go into a recursive black hole. It will:
1. expose A's factory in L3 cache once A's instantiation is partially done (pass contructor, without any attributes assigned)
2. expose A's early reference in L2 cache and remove A's factory from L3 cache
2. expose A in its entirety (all attributes assigned) in L1 cache and remove A's early reference from L2 cache.

__Under this algorithm, the follow will happen when there is a cyclic dependency:__
![]({{ '/styles/images/zhangxin-framework-spring/spring-ioc-cyclic-dependency-l3-cache.png' | prepend: site.baseurl }})

Image Reference: https://developpaper.com/why-does-spring-use-level-3-caching-to-solve-circular-dependency/

__Source Code Anatomy__
```java
public class DefaultSingletonBeanRegistry extends SimpleAliasRegistry implements SingletonBeanRegistry {
	/** Cache of singleton objects: bean name to bean instance. */
	private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(256);

	/** Cache of singleton factories: bean name to ObjectFactory. */
	private final Map<String, ObjectFactory<?>> singletonFactories = new HashMap<>(16);

	/** Cache of early singleton objects: bean name to bean instance. */
	private final Map<String, Object> earlySingletonObjects = new HashMap<>(16);

    /** Invoked in AbstractBeanFactory during "doCreateBean" process. */
	protected void addSingletonFactory(String beanName, ObjectFactory<?> singletonFactory) {
		Assert.notNull(singletonFactory, "Singleton factory must not be null");
		synchronized (this.singletonObjects) {
			if (!this.singletonObjects.containsKey(beanName)) {
				this.singletonFactories.put(beanName, singletonFactory);
				this.earlySingletonObjects.remove(beanName);
			}
		}
	}

    /** Invoked once a singleton has been completely instantiated 
    with attributes cyclic dependency resolved. */
	protected void addSingleton(String beanName, Object singletonObject) {
		synchronized (this.singletonObjects) {
			this.singletonObjects.put(beanName, singletonObject);
			this.singletonFactories.remove(beanName);
			this.earlySingletonObjects.remove(beanName);
		}
	}
    
    /** 
    * getSingleton from L3 cache structure to resolve cyclic dependency
    * L1: singletonObjects
    * L2: earlySingletonObjects
    * L3: singletonFactories
    * 
    * Add early reference to L2 cache and remove its factory from L3 cache 
    * once a singleton can be resolved from singletonFactory
    */
	protected Object getSingleton(String beanName, boolean allowEarlyReference) {
		Object singletonObject = this.singletonObjects.get(beanName);
		if (singletonObject == null && isSingletonCurrentlyInCreation(beanName)) {
			synchronized (this.singletonObjects) {
				singletonObject = this.earlySingletonObjects.get(beanName);
				if (singletonObject == null && allowEarlyReference) {
					ObjectFactory<?> singletonFactory = this.singletonFactories.get(beanName);
					if (singletonFactory != null) {
						singletonObject = singletonFactory.getObject();
						this.earlySingletonObjects.put(beanName, singletonObject);
						this.singletonFactories.remove(beanName);
					}
				}
			}
		}
		return singletonObject;
	}
}
```

### Spring Source - Application Context

__AnnotationConfigApplicationContext__  

```java
/**
 * Abstract implementation of the {@link org.springframework.context.ApplicationContext}
 * interface. Doesn't mandate the type of storage used for configuration; simply
 * implements common context functionality. Uses the Template Method design pattern,
 * requiring concrete subclasses to implement abstract methods.
 *
 * <p>In contrast to a plain BeanFactory, an ApplicationContext is supposed
 * to detect special beans defined in its internal bean factory:
 * Therefore, this class automatically registers
 * {@link BeanFactoryPostProcessors},
 * {@link BeanPostProcessors},
 * and {@link ApplicationListeners}
 * which are defined as beans in the context.
 */
public abstract class AbstractApplicationContext extends DefaultResourceLoader
		implements ConfigurableApplicationContext {
            // ...
}
```

---

__BeanDefinitionRegistryPostProcessor__

```java
/**
 * Extension to the standard {@link BeanFactoryPostProcessor} SPI, allowing for
 * the registration of further bean definitions before regular
 * BeanFactoryPostProcessor detection kicks in. In particular,
 * BeanDefinitionRegistryPostProcessor may register further bean definitions
 * which in turn define BeanFactoryPostProcessor instances.
 */
public interface BeanDefinitionRegistryPostProcessor extends BeanFactoryPostProcessor {

	/**
	 * Modify the application context's internal bean definition registry after its
	 * standard initialization. All regular bean definitions will have been loaded,
	 * but no beans will have been instantiated yet. This allows for adding further
	 * bean definitions before the next post-processing phase kicks in.
	 */
	void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException;
}
```

---

__PostProcessorRegistrationDelegate__  

```java
/**
 * Delegate for AbstractApplicationContext's post-processor handling.
 */
final class PostProcessorRegistrationDelegate {

    public static void invokeBeanFactoryPostProcessors(
        ConfigurableListableBeanFactory beanFactory, List<BeanFactoryPostProcessor> beanFactoryPostProcessors) {
            // ...
        }

	public static void registerBeanPostProcessors(
        ConfigurableListableBeanFactory beanFactory, AbstractApplicationContext applicationContext) {
            // ...
        }
}
```

---

__ConfigurationClassPostProcessor__

```java
/**
 * A {@link BeanFactoryPostProcessor} implementation, used for bootstrapping processing of
 * {@link Configuration @Configuration} classes.
 *
 * <p>Registered by default when using {@code <context:annotation-config/>} or
 * {@code <context:component-scan/>}. Otherwise, may be declared manually as
 * with any other BeanFactoryPostProcessor.
 *
 * <p>This post processor is priority-ordered as it is important that any
 * {@link Bean} methods declared in {@code @Configuration} classes have
 * their corresponding bean definitions registered before any other
 * {@link BeanFactoryPostProcessor} executes.
 */
public class ConfigurationClassPostProcessor implements BeanDefinitionRegistryPostProcessor,
		PriorityOrdered, ResourceLoaderAware, BeanClassLoaderAware, EnvironmentAware {
            // ...
}
```

### Spring Source - HttpServlet and Dispatcher

```java
public class MyDispatcherServlet extends HttpServlet {

    //store confis in resources/application.properties
    private Properties contextConfig = new Properties();

    //store names of classes scanned
    private List<String> classNames = new ArrayList<String>();

    // IOC container
    private Map<String, Object> ioc = new HashMap<String,Object>();

    // map URL to handler
    private Map<String, Method> handlerMapping = new HashMap<String,Method>();

    @Override
    public void init(ServletConfig config) throws ServletException {

        // Spring IOC Initialization

        // ------ IOC STAGE ------
        // 1. load config
        doLoadConfig(config.getInitParameter("contextConfigLocation"));
        // 2. scan class path
        doScanner(contextConfig.getProperty("scanPackage"));
        // 3. instantiate
        doInstantiation();

        // ------ DI STAGE ------
        // 4. autowire
        doAutowired();

        // ------ MVC STAGE ------
        // 5. init handler mapping
        doInitHandlerMapping();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // borrow existing implementation - doPost
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            doDispatch(req,resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write("500");
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replaceAll(contextPath, "").replaceAll("/+","/");
        
        // URL not found
        if(!this.handlerMapping.containsKey(url)){
            resp.getWriter().write("404");
            return;
        }

        Method method = this.handlerMapping.get(url);
        
        Map<String, String[]> paramsMap = req.getParameterMap();
        Class<?> [] paramTypes = method.getParameterTypes();
        
        // This method implementation is omitted due to too much details.
        Object [] paramValues = getParamValues(paramTypes, paramsMap) 

        String beanName = method.getDeclaringClass().getSimpleName();
        method.invoke(ioc.get(beanName), paramValues);
    }

    private void doLoadConfig(String contextConfigLocation) {
        // igonire java best practice of using try-with-resources for simplicity
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
        try {
            contextConfig.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            is.close();
        }
    }

    private void doScanner(String scanPackage) {
        // recursively find all the classes given package root
    }

    private void doInstantiation() {
        // omit try-catch handling for simplicity

        for (String className : classNames) {
            Class<?> clazz = Class.forName(className);

            // @Bean
            if (clazz.isAnnotationPresent(Bean.class)) {
                Object instance = clazz.newInstance();
                String beanName = clazz.getSimpleName
                
                // handle beans with same class but different annotation values
                Bean annotation = clazz.getAnnotation(Bean.class);
                if (!"".equals(annotation.value())) {
                    beanName = annotation.value();
                }

                ioc.put(beanName, instance);
                continue
            } 

            // handle @Service, @Controller... etc

            if (clazz.isAnnotationPresent(Service.class)) {
                // omit...
                continue
            }
            
            if (clazz.isAnnotationPresent(Conrtoller.class)) {
                // omit...
                continue
            }            
        }
    }

    private void doAutowired() {
        // omit try-catch handling for simplicity

        for (Map.Entry<String,Object> entry : ioc.entrySet()) {
            // getDeclaredFields returns all fields, including private/protected/package-private/public
            Field[] fields = entry.getValue().getClass().getDeclaredFields();
            for (Field field : fields) {
                if(!field.isAnnotationPresent(Autowired.class)){
                    continue;
                }
                
                Autowired autowired = field.getAnnotation(Autowired.class);
                String beanName = field.getType().getName();
                if(!"".equals(autowired.value())) {
                    beanName = autowired.value();
                }

                // force accessibility
                field.setAccessible(true);
                field.set(entry.getValue(), ioc.get(beanName));
            }
        }
    }

    private void doInitHandlerMapping() {

        for (Map.Entry<String,Object> entry : ioc.entrySet()) {
            Class<?> clazz = entry.getValue().getClass();

            // filter out non-controller classes
            if(!clazz.isAnnotationPresent(Controller.class)) { 
               continue; 
            }

            // get base url
            String baseUrl = "";
            if(clazz.isAnnotationPresent(RequestMapping.class)){
               RequestMapping requestMapping = clazz.getAnnotation(RequestMapping.class);
               baseUrl = requestMapping.value();
            }

            // iterate over methods
            for (Method method : clazz.getMethods()) {
                // filter out non-
                if(!method.isAnnotationPresent(RequestMapping.class)) {
                    continue;
                }
                RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
                String url = ("/" + baseUrl + "/" + requestMapping.value()).replaceAll("/+", "/"); // little regex trick
                handlerMapping.put(url, method);
            }
        }
    }
}
```

### Spring Source - Transaction Management

__What is Propagation Behavior in Transaction Management?__
Image there are two services X and Y. Both can directly talk to database. In some cases, X starts a transaction and invokes Y's public function, which can: 
1. start a new transaction
2. reuse X's transaction
3. execute without a transaction

The above is propagation behavior. JTI (Java Transaction Interface) had the following definitions:
1. __REQUIRED__ - reuse the old one if it already existed, create a new one otherwise.
2. __SUPPORT__ - reuse the old one if it already existed, execute without a transaction.
3. __MANDATORY__ - reuse the old one if it already existed, throw an exception otherwise.
4. __REQUIRES_NEW__ - always create a new one.
5. __NOT_SUPPORT__ - always execute without a transaction.
6. __NEVER__ - throw an exception if it already existed, execute without a transaction otherwise.

```java
/**
 * Interface that defines Spring-compliant transaction properties. Such as: 
 * ISOLATION, PROPAGATION, TIMEOUT, READONLY... etc
 *
 * <p>Note that isolation level and timeout settings will not get applied unless
 * an actual new transaction gets started.
 */
public interface TransactionDefinition {}

/**
 * Marker interface for Spring transaction manager implementations,
 * either traditional or reactive.
 */
public interface TransactionManager {}

/**
 * This is the central interface in Spring's imperative transaction infrastructure.
 * Applications can use this directly, but it is not primarily meant as an API:
 * Typically, applications will work with either TransactionTemplate or
 * declarative transaction demarcation through AOP.
 *
 * <p>For implementors, it is recommended to derive from the provided
 * {@link AbstractPlatformTransactionManager} class, 
 * which pre-implements the defined propagation behavior and takes care
 * of transaction synchronization handling. Subclasses have to implement
 * template methods for specific states of the underlying transaction,
 * for example: begin, suspend, resume, commit.
 *
 * <p>The default implementations of this strategy interface are
 * {@link JtaTransactionManager} and {@link DataSourceTransactionManager},
 * which can serve as an implementation guide for other transaction strategies.
 */
public interface PlatformTransactionManager extends TransactionManager {
	
    /**
	 * Return a currently active transaction or create a new one, according to
	 * the specified propagation behavior.
     * 
	 * <p>Note that parameters like isolation level or timeout will only be applied
	 * to new transactions, and thus be ignored when participating in active ones.
     * A proper transaction manager implementation
	 * should throw an exception when unsupported settings are encountered.
     *
	 * @param definition the TransactionDefinition instance (can be {@code null} for defaults),
	 * describing propagation behavior, isolation level, timeout etc.
	 * @return transaction status object representing the new or current transaction
	 */
	TransactionStatus getTransaction(@Nullable TransactionDefinition definition)
			throws TransactionException;

	/**
	 * Commit the given transaction, with regard to its status. If the transaction
	 * has been marked rollback-only programmatically, perform a rollback.
     *
	 * <p>Note that when the commit call completes, no matter if normally or
	 * throwing an exception, the transaction must be fully completed and
	 * cleaned up. No rollback call should be expected in such a case.
	 * <p>If this method throws an exception other than a TransactionException,
	 * then some before-commit error caused the commit attempt to fail.
     * The original exception will be propagated to the caller of this commit 
     * method in such a case.
	 * @param status object returned by the {@code getTransaction} method
	 */
	void commit(TransactionStatus status) throws TransactionException;

	/**
	 * Perform a rollback of the given transaction.
     * 
	 * <p><b>Do not call rollback on a transaction if commit threw an exception.</b>
	 * The transaction will already have been completed and cleaned up when commit
	 * returns, even in case of a commit exception.
	 * @param status object returned by the {@code getTransaction} method
	 */
	void rollback(TransactionStatus status) throws TransactionException;
}    

/**
 * Abstract base class that implements Spring's standard transaction workflow,
 * serving as basis for concrete platform transaction managers like
 * {@link JtaTransactionManager}.
 *
 * <p>This base class provides the following workflow handling:
 * <ul>
 * <li>determines if there is an existing transaction;
 * <li>applies the appropriate propagation behavior;
 * <li>suspends and resumes transactions if necessary;
 * <li>checks the rollback-only flag on commit;
 * <li>applies the appropriate modification on rollback
 * (actual rollback or setting rollback-only);
 * <li>triggers registered synchronization callbacks
 * (if transaction synchronization is active).
 * </ul>
 *
 * <p>Subclasses have to implement specific template methods for specific
 * states of a transaction, e.g.: begin, suspend, resume, commit, rollback.
 * The most important of them are abstract and must be provided by a concrete
 * implementation; for the rest, defaults are provided, so overriding is optional.
 */
public abstract class AbstractPlatformTransactionManager implements PlatformTransactionManager {
    // ... 

	@Override
	public final TransactionStatus getTransaction(@Nullable TransactionDefinition definition)
			throws TransactionException {

		// Use defaults if no transaction definition given.
		TransactionDefinition def = (definition != null ? definition : TransactionDefinition.withDefaults());

		Object transaction = doGetTransaction();
		boolean debugEnabled = logger.isDebugEnabled();

		if (isExistingTransaction(transaction)) {
			// Existing transaction found -> check propagation behavior to find out how to behave.
			return handleExistingTransaction(def, transaction, debugEnabled);
		}

		// Check definition settings for new transaction.
		if (def.getTimeout() < TransactionDefinition.TIMEOUT_DEFAULT) {
			throw new InvalidTimeoutException("Invalid transaction timeout", def.getTimeout());
		}

		// No existing transaction found -> check propagation behavior to find out how to proceed.
		if (def.getPropagationBehavior() == TransactionDefinition.PROPAGATION_MANDATORY) {
			throw new IllegalTransactionStateException(
					"No existing transaction found for transaction marked with propagation 'mandatory'");
		}
		else if (def.getPropagationBehavior() == TransactionDefinition.PROPAGATION_REQUIRED ||
				def.getPropagationBehavior() == TransactionDefinition.PROPAGATION_REQUIRES_NEW ||
				def.getPropagationBehavior() == TransactionDefinition.PROPAGATION_NESTED) {
			SuspendedResourcesHolder suspendedResources = suspend(null);
			if (debugEnabled) {
				logger.debug("Creating new transaction with name [" + def.getName() + "]: " + def);
			}
			try {
				return startTransaction(def, transaction, debugEnabled, suspendedResources);
			}
			catch (RuntimeException | Error ex) {
				resume(null, suspendedResources);
				throw ex;
			}
		}
		else {
			// Create "empty" transaction: no actual transaction, but potentially synchronization.
			if (def.getIsolationLevel() != TransactionDefinition.ISOLATION_DEFAULT && logger.isWarnEnabled()) {
				logger.warn("Custom isolation level specified but no actual transaction initiated; " +
						"isolation level will effectively be ignored: " + def);
			}
			boolean newSynchronization = (getTransactionSynchronization() == SYNCHRONIZATION_ALWAYS);
			return prepareTransactionStatus(def, null, true, newSynchronization, debugEnabled, null);
		}
	}
}

/**
 * An implementation of {@link PlatformTransactionManager}
 * for JTA (Java Transaction API), 
 * delegating to a backend JTA provider.
 *
 * <p>This transaction manager is appropriate for handling distributed transactions,
 * i.e. transactions that span multiple resources, and for controlling transactions on
 * application server resources (e.g. JDBC DataSources available in JNDI) in general.
 * For a single JDBC DataSource, DataSourceTransactionManager is perfectly sufficient,
 * and for accessing a single resource with Hibernate (including transactional cache),
 * HibernateTransactionManager is appropriate, for example.
 *
 * <p><b>For typical JTA transactions (REQUIRED, SUPPORTS, MANDATORY, NEVER), a plain
 * JtaTransactionManager definition is all you need, portable across all Java EE servers.</b>
 *
 * <p><b>Transaction suspension (REQUIRES_NEW, NOT_SUPPORTED) is just available with a
 * JTA TransactionManager being registered.</b> Common TransactionManager locations are
 * autodetected by JtaTransactionManager, provided that the "autodetectTransactionManager"
 * flag is set to "true" (which it is by default).
 *
 * <p>Note: Support for the JTA TransactionManager interface is not required by Java EE.
 * Almost all Java EE servers expose it, but do so as extension to EE. There might be some
 * issues with compatibility, despite the TransactionManager interface being part of JTA.
 * As a consequence, Spring provides various vendor-specific PlatformTransactionManagers,
 * which are recommended to be used if appropriate: {@link WebLogicJtaTransactionManager}
 * and {@link WebSphereUowTransactionManager}. For all other Java EE servers, the
 * standard JtaTransactionManager is sufficient.
 */
@SuppressWarnings("serial")
public class JtaTransactionManager extends AbstractPlatformTransactionManager
		implements TransactionFactory, InitializingBean, Serializable {
        
    // ...
}
```

---

__The Design of Annotation Based AOP Enhancement Around Transaction__

```java
/**
 * Describes a transaction attribute on an individual method or on a class.
 *
 * <p>When this annotation is declared at the class level, it applies as a default
 * to all methods of the declaring class and its subclasses. 
 * <p>This annotation type is generally directly comparable to Spring's
 * {@link RuleBasedTransactionAttribute} class.
 * {@link RuleBasedTransactionAttribute} extends {@link DefaultTransactionAttribute}
 * {@link DefaultTransactionAttribute} extends {@link DefaultTransactionDefinition}
 * 
 * In fact {@link AnnotationTransactionAttributeSource} will directly
 * convert the data to the latter class, so that Spring's transaction support code
 * does not have to know about annotations. 
 *
 * <p>This annotation commonly works with thread-bound transactions managed by a
 * {@link PlatformTransactionManager}, exposing a
 * transaction to all data access operations within the current execution thread.
 * <b>Note: This does NOT propagate to newly started threads within the method.</b>
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Transactional {

	@AliasFor("transactionManager")
	String value() default "";

	/**
	 * A <em>qualifier</em> value for the specified transaction.
	 * <p>May be used to determine the target transaction manager, matching the
	 * qualifier value (or the bean name) of a specific
	 * {@link TransactionManager}
	 */
	@AliasFor("value")
	String transactionManager() default "";

	/**
	 * The transaction propagation type.
	 */
	Propagation propagation() default Propagation.REQUIRED;

	/**
	 * The transaction isolation level.
	 */
	Isolation isolation() default Isolation.DEFAULT;

	/**
	 * The timeout for this transaction (in seconds).
	 */
	int timeout() default TransactionDefinition.TIMEOUT_DEFAULT;

	/**
	 * A boolean flag that can be set to {@code true} if the transaction is
	 */
	boolean readOnly() default false;

	/**
	 * Defines zero (0) or more exception {@link Class classes}, which must be
	 * subclasses of {@link Throwable}, indicating which exception types must cause
	 * a transaction rollback.
	 */
	Class<? extends Throwable>[] rollbackFor() default {};

	/**
	 * Defines zero (0) or more exception names (for exceptions which must be a
	 * subclass of {@link Throwable}), indicating which exception types must cause
	 * a transaction rollback.
	 */
	String[] rollbackForClassName() default {};

	/**
	 * Defines zero (0) or more exception {@link Class Classes}, which must be
	 * subclasses of {@link Throwable}, indicating which exception types must
	 * <b>not</b> cause a transaction rollback.
	 */
	Class<? extends Throwable>[] noRollbackFor() default {};

	/**
	 * Defines zero (0) or more exception names (for exceptions which must be a
	 * subclass of {@link Throwable}) indicating which exception types must <b>not</b>
	 * cause a transaction rollback.
	 */
	String[] noRollbackForClassName() default {};
}

/**
 * Enables Spring's annotation-driven transaction management capability, similar to
 * the support found in Spring's {@code <tx:*>} XML namespace. To be used on
 * {@link @Configuration}
 * classes to configure traditional, imperative transaction management or
 * reactive transaction management.
 *
 * In both of the scenarios above, {@code @EnableTransactionManagement} and {@code
 * <tx:annotation-driven/>} are responsible for registering the necessary Spring
 * components that power annotation-driven transaction management, such as the
 * {@link TransactionInterceptor} and the proxy- or AspectJ-based advice that weave the
 * interceptor into the call stack when {@code JdbcFooRepository}'s {@code @Transactional}
 * methods are invoked.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(TransactionManagementConfigurationSelector.class)
public @interface EnableTransactionManagement {

	/**
	 * Indicate whether subclass-based (CGLIB) proxies are to be created ({@code true}) as
	 * opposed to standard Java interface-based proxies ({@code false}). 
	 */
	boolean proxyTargetClass() default false;

	/**
	 * Indicate how transactional advice should be applied.
	 * Please note that proxy mode allows for interception of calls through the proxy
	 * only. 
	 */
	AdviceMode mode() default AdviceMode.PROXY;

	/**
	 * Indicate the ordering of the execution of the transaction advisor
	 * when multiple advices are applied at a specific joinpoint.
	 */
	int order() default Ordered.LOWEST_PRECEDENCE;
}

/**
 * <p>Derives from the {@link TransactionAspectSupport} class which
 * contains the integration with Spring's underlying transaction API.
 * TransactionInterceptor simply calls the relevant superclass methods
 * such as {@link #invokeWithinTransaction} in the correct order.
 */
public class TransactionInterceptor extends TransactionAspectSupport implements MethodInterceptor {

    // ...

	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		// Work out the target class: may be {@code null}.
		// The TransactionAttributeSource should be passed the target class
		// as well as the method, which may be from an interface.
		Class<?> targetClass = (invocation.getThis() != null ? AopUtils.getTargetClass(invocation.getThis()) : null);

		// Adapt to TransactionAspectSupport's invokeWithinTransaction...
		return invokeWithinTransaction(invocation.getMethod(), targetClass, invocation::proceed);
	}
}

/**
 * Strategy interface used by {@link TransactionInterceptor} for metadata retrieval.
 *
 * <p>Implementations know how to source transaction attributes, whether from configuration,
 * metadata attributes at source level (such as Java 5 annotations), or anywhere else.
 */
public interface TransactionAttributeSource {

	/**
	 * Determine whether the given class is a candidate for transaction attributes
	 * in the metadata format of this {@code TransactionAttributeSource}.
	 */
	default boolean isCandidateClass(Class<?> targetClass) {
		return true;
	}

	/**
	 * Return the transaction attribute for the given method,
	 * or {@code null} if the method is non-transactional.
	 */
	TransactionAttribute getTransactionAttribute(Method method, @Nullable Class<?> targetClass);

}

/**
 * <p>Uses the <b>Strategy</b> design pattern.
 *
 * Base class for transactional aspects, such as the {@link TransactionInterceptor}
 * or an AspectJ aspect.
 *
 * <p>This enables the underlying Spring transaction infrastructure to be used easily
 * to implement an aspect for any aspect system.
 *
 * <p>Subclasses are responsible for calling methods in this class in the correct order.
 * 
 * A {@link PlatformTransactionManager} implementation will perform the actual transaction
 * management, and a {@link TransactionAttributeSource} (e.g. annotation-based) is used
 * for determining transaction definitions for a particular class or method.
 */
public abstract class TransactionAspectSupport implements BeanFactoryAware, InitializingBean {
	/**
	 * General delegate for around-advice-based subclasses, delegating to several other template
	 * methods on this class. Able to handle {@link CallbackPreferringPlatformTransactionManager}
	 * as well as regular {@link PlatformTransactionManager} implementations and
	 * {@link ReactiveTransactionManager} implementations for reactive return types.
	 * @param method the Method being invoked
	 * @param targetClass the target class that we're invoking the method on
	 * @param invocation the callback to use for proceeding with the target invocation
	 * @return the return value of the method, if any
	 * @throws Throwable propagated from the target invocation
	 */
	@Nullable
	protected Object invokeWithinTransaction(Method method, @Nullable Class<?> targetClass,
			final InvocationCallback invocation) throws Throwable {
            
            // ...
    }    
}
```