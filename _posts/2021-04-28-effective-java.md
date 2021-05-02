---
layout: post
title:  Effective Java
date:   2021-04-28 15:30:00 -0700
categories: reference
tag: java
---

* content
{:toc}


# Creating and Destroying Objects

## Consider Static Factory Methods Instead of Constructors

__Pros of Static Factory Methods__
* Having different names for different instantiations.
* Provide commonly used constant as a singleton.
* Different static factory methods or single parameterized static factory method can return different subtypes. For example:
  * `Collections.singletonList(T o)`
  * `Collections.emptySet()`
  * `Collections.unmodifiableMap(Map<K, V> m)`

__Cons of Static Factory Methods__
* A class without public constructor cannot be subclassed.

__Note__  
The above characteristics make static factory method a perfect candidate for playing the Provider role in Service-Provider pattern, where a Provider decouples Client and Service so that Client becomes unaware of the service implementation.

## Consider a Builder when Faced with Many Constructor Parameters

__Builder vs Telescoping vs JavaBean__  
* Builder pattern is better than telescoping pattern where the number of parameters equal to the number of constructors resulting in many hard-to-follow instantiations.
* Builder pattern is better than Java bean pattern where immutability must be broken.

__Hierarchical Builder__ can reduce duplicated code while providing great flexibility to extended classes. For example:
```java
// Builder pattern for class hierarchies
public abstract class Pizza {

  public enum Topping { HAM, MUSHROOM, ONION, PEPPER, SAUSAGE }

  final Set<Topping> toppingSet;

  Pizza(Builder<?> builder) {
    toppingSet = builder.toppingEnumSet.clone();
  }

  abstract static class Builder<T extends  Builder<T>> {
    EnumSet<Topping> toppingEnumSet = EnumSet.noneOf(Topping.class);

    public T addTopping(Topping topping) {
      toppingEnumSet.add(Objects.requireNonNull(topping));
      return self();
    }

    abstract Pizza build();

    // Subclasses must override this method to return "this".
    protected abstract T self();
  }
}


public class NyPizza extends Pizza {

  public enum Size { SMALL, MEDIUM, LARGE }

  private final Size size;

  private NyPizza(Builder builder) {
    super(builder);
    size = builder.size;
  }

  public static class Builder extends Pizza.Builder<Builder> {

    private final Size size;

    public Builder(Size size) {
      this.size = Objects.requireNonNull(size);
    }

    @Override
    public NyPizza build() {
      return new NyPizza(this);
    }

    @Override
    protected Builder self() { return this; }
  }
}


public class LaPizza extends Pizza {

  private final boolean sauceInside;

  private LaPizza(Builder builder) {
    super(builder);
    sauceInside = builder.sauceInside;
  }

  public static class Builder extends Pizza.Builder<Builder> {

    private boolean sauceInside = false;

    public Builder sauceInside() {
      sauceInside = true;
      return this;
    }

    @Override
    public LaPizza build() {
      return new LaPizza(this);
    }

    @Override
    protected Builder self() { return this; }
  }
}
```

## Enforce the Singleton Property with a Private Constructor or an Enum Type

Singletons typically represent either a stateless object such as a function or a system component that is intrinsically unique. We usually enforce the singleton property by having a private constructor.

## Enforce the Non-instantiability with a Private Constructor.

Typically a static utility class is non-instantiable. Having a private constructor enforces the property.

## Prefer Dependency Injection to Hardwiring Resources

Static utility classes and singletons are inappropriate for classes whose behavior is parameterized by an underlying resource. For example, SpellChecker depends on an underlying dictionary. What is required to support multiple instances of a class with each of them using the underlying resource desired by the client? A simple pattern that satisfy this requirement is to pass the resource into the constructor when creating a new instance. This is one form of dependency injection.

## Avoid Creating Unnecessary Objects

For example, don't concatenate strings by "+" operator in loops.

## Eliminate Obsolete Object References

The following snippet is from `ArrayList` implementation source code. Notice that as we remove item at some index, we will null out the reference by `elementData[--size] = null`. It is necessary because otherwise the reference becomes obsolete and escaped from garbage collection (causing memory leak).

```java
public class ArrayList<E> extends AbstractList<E>
  implements List<E>, RandomAccess, Cloneable, Serializable

  public E remove(int index) {
    rangeCheck(index);

    modCount++;
    E oldValue = elementData(index);

    int numMoved = size - index - 1;
    if (numMoved > 0)
        System.arraycopy(elementData, index+1, elementData, index,
                         numMoved);
    elementData[--size] = null; // clear to let GC do its work

    return oldValue;
}
```

## Avoid Finalizers and Cleaners

In Finalizers or Cleaners
* never do anything time critical
* never update persistent state in Finalizer

because there is no guarantee that a finalizer or cleaner will run promptly or run at all.

## Prefer try-with-resources to try-finally

Why? Exception thrown in `final` block will completely overwrite the exception thrown in `try` block but this issue doesn't occur in `try-with-resource`.

# Methods Common to All Objects

## Obey the General Contract When Overriding equals

Common Methods
* `equals`
* `hashCode`
* `toString`

General perception in Java is `==` checks if two references point to the same memory location while `equals` checks:
* two instances' face values if they are "Value Classes"
* same as `==` for "Non-value Classes".

When override "equals" method, the implementation must hold the following true:
* Reflexive
* Symmetric
* Transitive
* Consistent

## Always Override toString

Note
* While it isn't as critical as obeying the `equals` and `hashCode` contracts in previous item, provide a good toString implementation makes your class much more pleasant to use and makes systems using the class easier to debug.
* When practical, the `toString` method should return all the interesting info contained in the object.
* Whether or not you decide to specify the format, you should clearly document you intentions.
* Whether or not you specify the format, provide programmatic access to the info contained in the value returned by `toString`.

## Override clone Judiciously

Note
* A class implementing `Cloneable` is expected to provide a public `clone` method.
* Immutatble classes should never provide a `clone` method.
* Always call `super.clone()` for replication of the info contained in `super`, rather than calling super's constructor unless this class is final.
* Always return a deep copy.
* If you want clone functionality on a class that doesn't implement `Cloneable`, use Copy Constructor (aka Conversion Constructor). For example
  * `public HashMap(Map<? extends K, ? extends V> m)`
  * `public ArrayList(Collection<? extends E> c)`
  * `public HashSet(Collection<? extends E> c)`

## Consider Implementing Comparable

Most Value Classes implement `Comparable` interface so that its instances can be easily sorted/searched/used in comparison-based collections.

# Classes and Interfaces

## Minimize the Accessibility of Classes and Members

The single most important factor that distinguish a well-designed component from a poorly-designed one is the degree to which the component hides its implementation details from other components cleanly, separating its public APIs from implementation.

* For top-level classes/interfaces, if they are not part of the public API, make them package-private.
* All members should stay private unless:
  * the member is made package-private for testing purpose
  * the member is made protected for subclasses

## In Public Classes, Use Accessor Methods, not Public Fields.

Public classes should not directly expose mutable fields.

## Minimize Mutability

Classes should be immutable unless there is a very good reason not to.

__Pros of Immutable Classes__
* Inherently thread-safe
* Provide atomicity for free
* Simple
* It creates flexibility with static factory method
* Shared frequently used singletons reduce memory footprint

__Cons of Immutable Classes__
* Multi-step operations generate one object at each step. That's why we use `stringBuilder` to concatenate strings.
  * `stringBuilder` is not thread-safe while `stringBuffer` is

## Favor Composition Over Inheritance

Inheriting from ordinary concrete classes across packages is dangerous. Inheritance makes component fragile. Even for abstract classes that are designed for extension, it often became inevitable that subclasses evolve along with superclass, which defeats the purpose of encapsulation.

If you just want to "share" functionalities, favor composition over inheritance as composition provides flexibility and robustness. Use inheritance only when a subclass is __really__ a subtype of superclass.

## Design and Document for Inheritance or else Prohibit it

## Prefer Interfaces to Abstract Classes

Interface is not as hierarchical as abstract class, providing flexibility and robustness. For example, `public class HashMap<K,V> extends AbstractMap<K,V>` where the abstract class provide skeleton implementation to ease the implementation burden for subclasses (Template Method pattern).

## Design Interfaces for Posterity

`Default Method` was added to interface in Java 8. It enables implementation of subroutines in an interface that are common to implementors, easing the burden of implementation of these subclasses.

However it introduced breaking changes as well. For example, `Collection` interface has default implementation of `removeIf` that takes in a `Predicate` and call implementor's `remove` method and `iterator` method. But this default implementation is not synchronized while this interface is implemented by `SynchronizedCollection`. Now that `SynchronizedCollection` has the obligation to override `removeIf` to ensure synchronization when its subclasses call `removeIf`. So the release of the interface with such default method will bring in implementations that might not behave as expected if not overriden.

## Use Interface Only to Define Types

## Prefer Class Hierarchies to Tagged Classes

## Favor Static Nested Classes Over Nonstatic

__Nested Classes__  
A nested class should exist only to serve its outer class. Otherwise, declare a top-level class instead.

Examples of static nested class:
```java
public class Calculator {
  public static enum Operation { PLUS, MINUS, MULTIPLY, DIVIDE }  
}

public class HashMap<K, V> extends AbstractMap<K, V> {
  static class Node<K,V> implements Map.Entry<K,V> {}
}
```

Examples of non-static nested class:
```java
public class MySet<E> extends AbstractSet<E> {

  private class MyIterator implements Iterator<E> {}

  @Override
  public Iterator<E> iterator() {
    return new MyIterator();
  }
}
```

Static instance can live independent to outer class instance, different from non-static ones, which reduces memory footprint especially during GC. Otherwise, if it only makes sense to reference the inner instance from outer an instance, use non-static member class.

## Limit Source Files to a Single Top-level Class

One source file contains one and only one top-level class.

# Generics

## Don't Use Raw Types

Generic type info will be erased during Runtime.

Generics were introduced in Java 5. Before it, one can put anything into a raw type `List` without Compile Exception, which then results in Runtime Exception when you iterate over the `List` and cast them into some desired type. For example:

```java
private final Collection stamps = new ArrayList<>();

stamps.add(new Stamp());
stamps.add(new Coin());

Iterator i = stamps.iterator();
while (i.hasNext()) {
  Stamp stamp = (Stamp) i.next(); // throw Runtime ClassCastException
  ...
}
```

Typed `List<T>` makes the program safe and cleaner. For example:
```java
private final Collection stamps = new ArrayList<>();

stamps.add(new Stamp());
stamps.add(new Coin()); // Compile Error: incompatible types
```

Why program still permit the existence of raw type?
* Because a lot of legacy code are using them.

What if you want a List that can hold different type of objects?
* Use `List<Object>`

When to use wild card?
* Use `List<?>` when element type is unknown.

Two exceptions of "Don't Use Raw Types":
* `.class` literals permit primitives, array types and raw types. But it doesn't allow generics, such as:
  * `List.class          // valid`
  * `int.class           // valid`
  * `String[].class      // valid`
  * `List<String>.class  // invalid`
* It's illegal to use "instanceof" operator on generics other than unbound wild types, such as:
  * `o instanceof Set    //valid`
  * `o instanceof Set<?> //valid`
  * `o instanceof Set<E> //invalid`

However, casting into raw types is absolutely wrong:
```java
if (o instanceof Set) {
  Set<?> s1 = (Set<?>) o // valid
  Set s2 == (Set) o // invalid
}
```

## Eliminate Unchecked Warnings

If you can't eliminate every unchecked warning but you can prove that the code that provoked the warning is `typesafe`, then suppress the warning with an `@SuppressWarnings("unchecked")` annotation.

## Prefer List to Array

__Covariant vs Invariant__  
Arrays are covariant meaning `Subtype[]` is a subtype of `Supertype[]`, while Lists are invariant meaning `List<Subtype>` is __not__ a subtype of `List<Supertype>`. Invariant is safer as in:
* `List<Object> list = new ArrayList<Long>() // Compile error`
* `Object[] array = new String[] // does not complain`

__Reifiable vs Non-reifiable__  
Arrays are Reifiable meaning their type info is fully available at Runtime, while Generics are implemented by Erasure meaning their type info will be erased at Runtime (only checked at Compile time). Erasure allows generic types to interoperate freely with legacy code, ensuring a smooth transition after Java 5 is released. For the above reason, it's illegal to have generic array, which makes program unsafe.

Illustrative Example:
```java
/**
 * List<Stamp> stamps = List.of(new Stamp(), new Stamp(), new Stamp());
 * Stamp stamp = (Stamp) new Chooser(stamps).choose()
 */
public class Chooser {

  private final Object[] choiceArray;

  public Chooser(Collection choices) {
    choiceArray = choices.toArray();
  }

  public Object choose() {
    Random rnd = ThreadLocalRandom.current();
    return choiceArray[rnd.nextInt(choiceArray.length)];
  }
}

/**
 * List<Stamp> stamps = List.of(new Stamp(), new Stamp(), new Stamp());
 * Stamp stamp = new Chooser(stamps).choose()
 */
public class Chooser<T> {

  private final List<T> choiceList;

  public Chooser(Collection<T> choices) {
    choiceList = new ArrayList<>(choices);
  }

  public T choose() {
    Random rnd = ThreadLocalRandom.current();
    return choiceList.get(rnd.nextInt(choiceList.size()));
  }
}
```

For more, please check # Ref: Combine Generics and Varargs Judiciously.

## Favor Generic Types

How to work around generic array creation error?

```java
public class Stack<E> {

  private E[] elements;
  private int size = 0;
  private static final int DEFAULT_INITIAL_CAPACITY = 16;

  public Stack() {
    elements = new E[DEFAULT_INITIAL_CAPACITY]; // Generic array creation error
  }

  public void push(E e) {
    ensureCapacity();
    elements[size++] = e;
  }

  public E pop() {
    if (size == 0) throw new EmptyStackException();

    E result = elements[size--];
    elements[size] = null;
    return result;
  }
}
```

Solution 1: Object array + Suppress warnings

```java
public class Stack<E> {

  ...

  // The elements array will contain only E instances from push().
  // This is sufficient to ensure type safety.
  // But the Runtime type of the array will always be Object[]!
  @SuppressWarnings("unchecked")
  public Stack() {
    elements = (E[]) new Object[DEFAULT_INITIAL_CAPACITY];
  }

  ...
}
```

Solution 2: Type casting + Suppress warnings

```java
public class Stack<E> {

  private Object[] elements;
  ...

  public Stack() {
    elements = new Object[DEFAULT_INITIAL_CAPACITY];
  }

  ...

  @SuppressWarnings("unchecked")
  public E pop() {
    if (size == 0) throw new EmptyStackException();

    E result = (E) elements[--size];
    elements[size] = null;
    return result;
  }
}
```

In generally, `List` is preferred to `array` but some provided generic types are actually built on top of `arrays`, such as `ArrayList, HashMap`. So it's better to understand how it's done internally.

## Favor Generic Methods

Writing generic method:
```java
// raw type method
public static Set union(Set s1, Set s2) {
  Set result = new HashSet(s1); // unchecked warning
  result.addAll(s2); // unchecked warning
  return result;
}

// generic type method
public static <E> Set<E> union(Set<E> s1, Set<E> s2) {
  Set<E> result = new HashSet<>(s1);
  result.addAll(s2);
  return result;
}
```

The use of wild card will be discussed in [Use Bounded Wild Card to Increase API Flexibility](#Use-Bounded-Wild-Card-to-Increase-API-Flexibility).

Generic singleton factory method is used for function singleton. For example:
```java
public class Collections {

  private Collections() {}

  @SuppressWarnings("unchecked")
  public static <T> Comparator<T> reverseOrder() {
      return (Comparator<T>) ReverseComparator.REVERSE_ORDER;
  }

  /**
   * @serial include
   */
  private static class ReverseComparator
      implements Comparator<Comparable<Object>>, Serializable {

      private static final long serialVersionUID = 7207038068494060240L;

      static final ReverseComparator REVERSE_ORDER = new ReverseComparator();

      public int compare(Comparable<Object> c1, Comparable<Object> c2) {
          return c2.compareTo(c1);
      }

      private Object readResolve() { return Collections.reverseOrder(); }

      @Override
      public Comparator<Comparable<Object>> reversed() {
          return Comparator.naturalOrder();
      }
  }
}
```

Recursive type bound showcased in the generic `max` function:
```java
// Using a recursive type bound to express mutual comparability
public static <E extends Comparable<E>> E max(Collection<E> c) {
  if (c.isEmpty()) throw new IllegalArgumentException();

  E result = null;
  for (E e : c) {
    if (result == null || e.compareTo(result) > 0) {
      result = Objects.requireNonNull(e);
    }
  }
  return result;
}
```

## Use Bounded Wild Card to Increase API Flexibility

Recall the static union example in [Favor Generic Method](#Favor-Generic-Methods)? Consider the following use case, notice how the use of wild card in the function signature increased its flexibility.
```java
Integer extends Number;
Double extends Number;

Set<Integer> s1 = Set.of(1, 3, 5);
Set<Double> s2 = Set.of(2.0, 4.0, 6.0);
Set<Number> s3 = union(s1, s2)

// Implementation
public static <E> Set<E> union(Set<? extends E> s1, Set<? extends E> s2) {
  Set<E> result = new HashSet<>(s1);
  result.addAll(s2);
  return result;
}
```

If a parameterized type represents a T producer, use `<? extends T>`. If it represents a T consumer, use `<? super T>`. For example:
```java
public class Queue<E> {

  // the following implementations are omitted
  public Queue();
  public void push(E e);
  public E pop();
  public boolean isEmpty();

  public void pushAll(Iterable<? extends E> src) {
    for (E e : src) {
      push(e);
    }
  }

  public void popAll(Collection<? super E> dst) {
    while (!isEmpty()) {
      dst.add(pop());
    }
  }
}

// Use
Queue<Number> q = new Queue<>();

List<Integer> integers = List.of(1, 3, 5);
List<Double> doubles = List.of(2.0, 4.0, 6.0);

// without <? extends E> it can only take List<Number> as source
q.pushAll(integers)
q.pushAll(doubles)

// without <? super E> it can only take List<Number> as sink
List<Object> numbers = List.of();
q.popAll(numbers)

// numbers: [1, 3, 5, 2.0, 4.0, 6.0]
```

Recall the recursive type bound example (implementation of `max`) in [Favor Generic Method](#Favor-Generic-Methods)? The following revised version provides better flexibility as in:
* T doesn't necessarily implement Comparable as long as T's super implements Comparable
* max() accepts wild-typed element collection as long as the element type extends T

```java
public static <T extends Comparable<? super T>> T max(Collection<? extends T> c) {
  if (c.isEmpty()) throw new IllegalArgumentException();

  E result = null;
  for (E e : c) {
    if (result == null || e.compareTo(result) > 0) {
      result = Objects.requireNonNull(e);
    }
  }
  return result;
}
```


# Bottom line
# Bottom line
# Bottom line
# Bottom line
