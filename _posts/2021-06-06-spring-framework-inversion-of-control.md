---
layout: post
title: Spring Framework - Inversion of Control
date:   2021-06-06 14:00:00 -0700
categories: reference
tag: java
---

* content
{:toc}



### Inversion of Control Implementation

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

### Spring Framework Bean Instantiation Source Code Skeleton

![]({{ '/styles/images/spring-framework-inversion-of-control/bean-instantiation.JPG' | prepend: site.baseurl }})

```java
public abstract class AbstractApplicationContext extends DefaultResourceLoader
		implements ConfigurableApplicationContext {
    
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

### Different Ways of Importing Java Beans into IOC Container

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

### Importing Java Bean from Other Module

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