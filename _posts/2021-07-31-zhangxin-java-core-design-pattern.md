---
layout: post
title: Zhangxin Java Core - Design Pattern
date:   2021-07-31 16:30:00 -0700
categories: reference
tag: java
---

* content
{:toc}



Updated: 2021-07-31


# Principles

__OOP Principles__
1. Encapsulation
2. Abstraction
3. Inheritance
4. Polymorphism

__"SOLID" Design Principles__
1. Single Responsibility Principle: A class should have only 1 responsibility.
2. Open-Close Principle: Open to extension but closed to modification.
3. Liskov Subsitution Principle: Objects in a program should be replaceable with instances of their subclasses without altering the correctness of that program.
4. Interface Segregation Principle: A client should not be forced to depend on interfaces that they do not use.
5. Dependency Inversion Principle: Higher level module should depend on abstraction of lower level module. Details should depend on abstraction.

# Patterns

Design patterns can be categorized into three types:
1. Creational pattern: Factory, Singleton, Builder, Prototype, etc.
2. Structural pattern: Bridge, Decorator, Proxy, etc.
3. Behavior pattern: Chain of Responsibility, Pub-Sub, Strategy, Template, etc.

__Strategy Pattern__ aka. Policy pattern

Scenario: E-commerce platform had 100 different promotions. How to calculate the actual price after applying one of the promotions? 

![]({{ '/styles/images/zhangxin-java-core/strategy-pattern.png' | prepend: site.baseurl }})

_Reference: https://refactoring.guru/design-patterns/strategy_

__Decorator Pattern__

Scenario: In the Strategy Pattern scenario, what if multiple promotions can be applied to an order at the same time?

![]({{ '/styles/images/zhangxin-java-core/decorator-pattern.png' | prepend: site.baseurl }})

_Reference: https://refactoring.guru/design-patterns/decorator_

__Responsibility Chain Pattern__

Scenario: When web app handles web requests, a request will go through interception, decryption, authentication, validation etc... before actually being handled. How to decouple and enable flexible combination?

![]({{ '/styles/images/zhangxin-java-core/responsibility-chain-pattern.png' | prepend: site.baseurl }})

_Reference: https://refactoring.guru/design-patterns/chain-of-responsibility_

__Proxy Pattern__

A proxy controls access to the original object, allowing you to perform something either before or after the request gets through to the original object.

![]({{ '/styles/images/zhangxin-java-core/proxy-pattern.png' | prepend: site.baseurl }})

_Reference: https://refactoring.guru/design-patterns/proxy_

Dynamic Proxy Note: 
1. [Reflection based Dynamic Proxy Demo](https://github.com/zangshayang1/zhangxin/blob/master/JavaCore/src/main/com/zhangxin/javacore/design/dynamicproxy/reflect/DynamicProxyDemo.java)
2. [CGlib based Dynamic Proxy Demo](https://github.com/zangshayang1/zhangxin/blob/master/JavaCore/src/main/com/zhangxin/javacore/design/dynamicproxy/cglib/CglibDemo.java)

__Factory Method Pattern__

Scenario: Logistic platform had 10 different trunks and 5 different ships to deliver goods. The type of transportation keeps growing. How to make all of these concrete implementations of transportation agnostic to client? 

![]({{ '/styles/images/zhangxin-java-core/factory-pattern-1.png' | prepend: site.baseurl }})
![]({{ '/styles/images/zhangxin-java-core/factory-pattern-2.png' | prepend: site.baseurl }})

_Reference: https://refactoring.guru/design-patterns/factory-method_

__Abstract Factory Pattern__

![]({{ '/styles/images/zhangxin-java-core/abstract-factory-pattern.png' | prepend: site.baseurl }})

_Reference: https://refactoring.guru/design-patterns/abstract-factory_

__Pub-Sub Pattern__

![]({{ '/styles/images/zhangxin-java-core/pub-sub-pattern.png' | prepend: site.baseurl }})

_Reference: https://refactoring.guru/design-patterns/observer_

__State Pattern__

Scenario: A vending machine receives user request, checks stock, asks for payment, distribute item.

![]({{ '/styles/images/zhangxin-java-core/state-pattern.png' | prepend: site.baseurl }})

_Reference: https://refactoring.guru/design-patterns/state_

__Bridge Pattern__

_This problem occurs because we’re trying to extend the shape classes in two independent dimensions: by form and by color. That’s a very common issue with class inheritance._

_The Bridge pattern attempts to solve this problem by switching from inheritance to the object composition. What this means is that you extract one of the dimensions into a separate class hierarchy, so that the original classes will reference an object of the new hierarchy, instead of having all of its state and behaviors within one class._

![]({{ '/styles/images/zhangxin-java-core/bridge-pattern-example.png' | prepend: site.baseurl }})

![]({{ '/styles/images/zhangxin-java-core/bridge-pattern.png' | prepend: site.baseurl }})

_Reference: https://refactoring.guru/design-patterns/bridge_
