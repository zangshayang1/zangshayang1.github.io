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

### Consider Static Factory Methods Instead of Constructors

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

### Consider a Builder when Faced with Many Constructor Parameters

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

### Enforce the Singleton Property with a Private Constructor or an Enum Type

Singletons typically represent either a stateless object such as a function or a system component that is intrinsically unique. We usually enforce the singleton property by having a private constructor.

### Enforce the Non-instantiability with a Private Constructor.

Typically a static utility class is non-instantiable. Having a private constructor enforces the property.

### Prefer Dependency Injection to Hardwiring Resources

Static utility classes and singletons are inappropriate for classes whose behavior is parameterized by an underlying resource. For example, SpellChecker depends on an underlying dictionary. What is required to support multiple instances of a class with each of them using the underlying resource desired by the client? A simple pattern that satisfy this requirement is to pass the resource into the constructor when creating a new instance. This is one form of dependency injection.

### Avoid Creating Unnecessary Objects

There are two ways to create a string.
* `s1 = "haha"`
* `s2 = new String("haha")`
* `s3 = "haha"`
* `s4 = s3.intern()`

Let's zoom in here:
* Both variables `s1` and `s2` are declared in the call stack space.
* s1's value is created and stored in __String Constant Pool (SCP)__ while s2's value is created and stored in __main heap other than SCP__.
  * Till Java 7, SCP resides in a space called `PermGen` outside of the main heap. PermGen has fixed size (configurable by `XX:PermSize=512m -XX:MaxPermSize=512m`) and is ignored by GC. It often leads to "OOM".
  * After Java 7, SCP resides in main heap tracked by GC.
* During the declaration of `s3`, it found that `"haha"` exists in SCP and thus assign the address of the value to the variable `s3`. That's why `s1 == s3 return true`.
* During the declaration of `s2`, the constructor takes in a `"haha"`, which already exists in SCP (created during `s1 = "haha"`) and created a copy of the value in outside of SCP (still in main heap) and then assign the address of the copy to the variable `s2`. That's why `s1 == s2 return false`.
* What `s3.intern()` does is it looks up the SCP trying find a value equal to s3's value. If it finds one, return the value and assign its address to  variable `s4`. If it doesn't find one, create such value and assign its address to variable `s4`.

So...
* Calling `new String()` creates unnecessary duplicates.
* Repeated string concatenation by `+` also creates unnecessary objects.

### Eliminate Obsolete Object References

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
      System.arraycopy(elementData, index+1, elementData, index, numMoved);
    elementData[--size] = null; // clear to let GC do its work

    return oldValue;
}
```

### Avoid Finalizers and Cleaners

In Finalizers or Cleaners
* never do anything time critical
* never update persistent state in Finalizer

because there is no guarantee that a finalizer or cleaner will run promptly or run at all.

### Prefer try-with-resources to try-finally

Why? Exception thrown in `final` block will completely overwrite the exception thrown in `try` block but this issue doesn't occur in `try-with-resource`.

# Methods Common to All Objects

### Obey the General Contract When Overriding equals

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

### Always Override toString

Note
* While it isn't as critical as obeying the `equals` and `hashCode` contracts in previous item, provide a good toString implementation makes your class much more pleasant to use and makes systems using the class easier to debug.
* When practical, the `toString` method should return all the interesting info contained in the object.
* Whether or not you decide to specify the format, you should clearly document you intentions.
* Whether or not you specify the format, provide programmatic access to the info contained in the value returned by `toString`.

### Override clone Judiciously

Note
* A class implementing `Cloneable` is expected to provide a public `clone` method.
* Immutatble classes should never provide a `clone` method.
* Always call `super.clone()` for replication of the info contained in `super`, rather than calling super's constructor unless this class is final.
* Always return a deep copy.
* If you want clone functionality on a class that doesn't implement `Cloneable`, use Copy Constructor (aka Conversion Constructor). For example
  * `public HashMap(Map<? extends K, ? extends V> m)`
  * `public ArrayList(Collection<? extends E> c)`
  * `public HashSet(Collection<? extends E> c)`

### Consider Implementing Comparable

Most Value Classes implement `Comparable` interface so that its instances can be easily sorted/searched/used in comparison-based collections.

# Classes and Interfaces

### Minimize the Accessibility of Classes and Members

The single most important factor that distinguish a well-designed component from a poorly-designed one is the degree to which the component hides its implementation details from other components cleanly, separating its public APIs from implementation.

* For top-level classes/interfaces, if they are not part of the public API, make them package-private.
* All members should stay private unless:
  * the member is made package-private for testing purpose
  * the member is made protected for subclasses

### In Public Classes, Use Accessor Methods, not Public Fields.

Public classes should not directly expose mutable fields.

### Minimize Mutability

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

### Favor Composition Over Inheritance

Inheriting from ordinary concrete classes across packages is dangerous. Inheritance makes component fragile. Even for abstract classes that are designed for extension, it often became inevitable that subclasses evolve along with superclass, which defeats the purpose of encapsulation.

If you just want to "share" functionalities, favor composition over inheritance as composition provides flexibility and robustness. Use inheritance only when a subclass is __really__ a subtype of superclass.

### Design and Document for Inheritance or else Prohibit it

### Prefer Interfaces to Abstract Classes

Interface is not as hierarchical as abstract class, providing flexibility and robustness. For example, `public class HashMap<K,V> extends AbstractMap<K,V>` where the abstract class provide skeleton implementation to ease the implementation burden for subclasses (Template Method pattern).

### Design Interfaces for Posterity

`Default Method` was added to interface in Java 8. It enables implementation of subroutines in an interface that are common to implementors, easing the burden of implementation of these subclasses.

However it introduced breaking changes as well. For example, `Collection` interface has default implementation of `removeIf` that takes in a `Predicate` and call implementor's `remove` method and `iterator` method. But this default implementation is not synchronized while this interface is implemented by `SynchronizedCollection`. Now that `SynchronizedCollection` has the obligation to override `removeIf` to ensure synchronization when its subclasses call `removeIf`. So the release of the interface with such default method will bring in implementations that might not behave as expected if not overriden.

### Use Interface Only to Define Types

### Prefer Class Hierarchies to Tagged Classes

### Favor Static Nested Classes Over Nonstatic

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

### Limit Source Files to a Single Top-level Class

One source file contains one and only one top-level class.

# Generics

### Don't Use Raw Types

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

### Eliminate Unchecked Warnings

If you can't eliminate every unchecked warning but you can prove that the code that provoked the warning is `typesafe`, then suppress the warning with an `@SuppressWarnings("unchecked")` annotation.

### Prefer List to Array

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

For more on this topic, please refer to [Combine Generics and Varargs Judiciously](#combine-generics-and-varargs-judiciously).

### Favor Generic Types

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

### Favor Generic Methods

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

The use of wild card will be discussed in [Use Bounded Wild Card to Increase API Flexibility](#use-bounded-wild-card-to-increase-api-flexibility).

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

### Use Bounded Wild Card to Increase API Flexibility

Recall the static union example in [Favor Generic Method](#favor-generic-methods)? Consider the following use case, notice how the use of wild card in the function signature increased its flexibility.
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

Recall the recursive type bound example (implementation of `max`) in [Favor Generic Method](#favor-generic-methods)? The following revised version provides better flexibility as in:
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

### Combine Generics and Varargs Judiciously

Generics + Varargs = Heap Polution.

Varargs allows clients to pass a variable number of arguments to a method. Internally, an array is created to hold these arguments.

It's safe in the case: `public void safeMethod(String... strings)`.

But it becomes unsafe when used jointly with generics, such as `public void unsafeMethod(List<Integer>... integerLists)` Because arrays are covariant, the declaration `List<T>[]` will generate warnings at Compile time. Please refer to [Prefer List to Array](#prefer-list-to-array) for detailed explanations.

It is __unsafe__ when you modify the argument array:
```java
static void unsafeMethod(List<Integer>... integerLists) {
  List<String> strList = List.of("unsafe");
  Object[] objects = integerLists;
  objects[0] = strList; // Heap polution
  Integer i = integerLists[0].get(0) // ClassCastException
  ...
}
```

If you are sure the method is safe. Use `@SafeVarargs` to suppress the warning. The method will be safe if:
* it doesn't write into the argument array
* it doesn't expose a reference of the argument array

### Consider Typesafe Heterogeneous Containers

Common use of generics include `Collections` such as `Set<E>, Map<K, V>` and single-element containers such as `ThreadLocal<T> and Class<T>`.

__What is a heterogeneous container?__
```java
public class Favorites {

  private Map<Class<?>, Object> favorites = new HashMap<>();

  public <T> void putFavorite(Class<T> type, T instance) {
    favorites.put(Objects.requireNonNull(type), instance);
  }

  public <T> T getFavorite(Class<T> type) {
    return type.cast(favorites.get(type)); // Class dynamic casting
  }
}

// dynamic casting implementation
// throws Runtime Exception if the give instance's type doesn't match
public class Class<T> {

  // The method returns {@code true} if the specified
  // {@code Object} argument is non-null and can be cast to
  // reference type represented by this {@code Class} object without
  // raising a {@code ClassCastException.} It returns {@code false} otherwise.
  public native boolean isInstance(Object obj);

  @SuppressWarnings("unchecked")
  public T cast(Object obj) {
    if (obj != null && !isInstance(obj))
      throw new ClassCastException(cannotCastMsg(obj));
    return (T) obj;
  }
}
```

A limitation of such heterogeneous container is it cannot be used in generic collections. For example, `List<String>.class, Map<String, Integer>.class gives syntax errors.`

# Enums and Annotations

### Use enums Instead of int Constants

__What are enums?__ Enums are singletons (instance declared during class is defined). Enums are immutable. High-quality implementations of `Comparable` and `Serializable` are provided.

An advanced sample use:
```java
public enum Operation {
  PLUS("+") {
    public double apply(double x, double y) { return x + y; }
  },
  MINUS("-") {
    public double apply(double x, double y) { return x - y; }
  },
  TIMES("*") {
    public double apply(double x, double y) { return x * y; }
  },
  DIVIDE("/") {
    public double apply(double x, double y) { return x / y; }
  };

  private final String symbol;

  Operation(String symbol) {
    this.symbol = symbol;
  }

  public abstract double apply(double x, double y);

  @Override
  public String toString() { return symbol; }
}
```

### Use Instance Fields Instead of Ordinals

### Use EnumSet Instead of Bit Fields

If enum constants are frequently used in a set rather than individually, use `EnumSet` like this `text.applyStyle(EnumSet.of(Style.Bold, Style.Italic))`. Why? Internally, if the `EnumSet` has less than 64 constants, the entire set is represented by a single `long`. So the performance is boosted via bitwise operations.

### Use EnumMap Instead of Ordinal Indexing

If you want to group employees by their types:
* Don't use ordinal to index an array of Set
* Don't use HashMap
* Use `EnumMap` as the below example shows


```java
public class Employee {

  enum Type { FULLTIME, PARTTIME, CONTRACT }

  private String name;
  private Type type;
  public Employee(String name, Type type) {
    this.name = name;
    this.type = type;
  }
}

public Map<Employee.Type, Set<Employee>> groupByType(Collection<Employee> employees) {
  return employees.stream()
          .collect(groupingBy(
            e -> e.type, // classifier
            () -> new EnumMap<>(Type.class), // Map supplier
            Collectors.toSet() // collector
          ));
}
```

### Emulate Extensible enums with Interfaces

### Prefer Annotations to Naming Patterns

Prefer `@Test` to naming pattern `test_someFunction()`.

__How it is used?__
```java
// Marker annotation implementation
@Retention(RetentionPolicy.RUNTIME) // when it takes effect
@Target(ElementType.METHOD) // the scope it takes effect
public @interface Test {}

// Client use
public class SampleTest {
  @Test
  public void testSomeFunction() {
    // omitted
  }
}

// Runtime to process the marker annotations
public class RunMethodsWithTestAnnotation {
  public static void main(String[] args) throws Exception {
    int tests = 0;
    int passed = 0;
    for (String arg : args) {
      Class<?> testClass = Class.forName(arg);
      for (Method method : testClass.getDeclaredMethods()) {
        if (method.isAnnotationPresent(Test.class)) {
          tests++;
          try {
            method.invoke(null);
            passed++;
          } catch (InvocationTargetException wrappedExc) {
            Throwable exc = wrappedExc.getCause();
            System.out.println(method + " failed: " + exc);
          } catch (Exception exc) {
            System.out.println("Invalid @Test: " + method);
          }
        }
      }
      System.out.printf("Passed: %d, Failed: %d%n", passed, tests - passed);
    }
  }
}
```

__Can it be parameterized?__
```java
// Parameterized annotation implementation
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ExceptionTest {
  Class<? extends Throwable>[] values();
}

// Client use
public class SampleTest {
  @ExceptionTest({ ArithmeticException.class })
  public void testSomeFunction() {
    // omitted
  }

  @ExceptionTest({ IndexOutOfBoundsException.class, NullPointerException.class })
  public void testAnotherFunction() {
    // omitted
  }
}
```

### Consistently Use the @Override Annotation

`@Override` checks at Compile time that the annotated method does override some inherited method from super.

### Use Marker Interfaces to Define Types

__Marker annotation vs marker interface__
A marker interface is an interface that contains no methods or constants declarations. One can use [marker annotation](#prefer-annotations-to-naming-patterns) to achieve the same thing but marker interface still has its advantages.

__Pros of Marker Interfaces__
* It defines a type subject to Compile time check.
* It builds hierarchies using __polymorphism and inheritance__.

__Pros of Marker Annotations__
* A marker annotation can be applied to method or even field level while a marker interface can only be applied at class level.
* A marker annotation can be a part of the larger annotation facility, providing noticeable benefits over a group of marker interfaces.

# Lambdas and Streams

### Prefer Lambdas to Anonymous Classes

Anonymous class was the primary means to create a function object. Function object, such as Comparator, typically has only one public method. Verbosity of anonymous class makes functional programming in java not very appealing.

Since Java 8, functional interface was introduced and `lambda express` became the primary means to implement these interfaces. An example for illustration would be:
```java
public enum Operation {
  PLUS("+", (x, y) -> x + y),
  MINUS("-", (x, y) -> x - y),
  TIMES("*", (x, y) -> x * y),
  DIVIDE("/", (x, y) -> x / y);

  private final String symbol;
  private final DoubleBinaryOperator op;

  Operation(String symbol, DoubleBinaryOperator op) {
    this.symbol = symbol;
    this.op = op;
  }

  public double apply(double x, double y) {
    return op.applyAsDouble(x, y);
  }

  @Override
  public String toString() { return symbol; }
}
```

More on this topic, please refer to [Favor the Use of Standard Functional Interfaces](#favor-the-use-of-standard-functional-interfaces).

### Prefer Method References to Lambdas

| Method Ref Type | Example | Lambda Equivalent |
| :----: | :----: | :----: |
| Static | Integer::parseInt | str -> Integer.parseInt(str) |
| Bound | Instant.now()::isAfter | t-> Instant.now().isAfter(t) |
| Unbound | String::toLowerCase | str -> str.toLowerCase() |
| Class Constructor | TreeMap<K, V>::new | () -> new TreeMap<K, V> |
| Array Constructor | int[]::new | len -> new int[len] |

### Favor the Use of Standard Functional Interfaces

| Interface | Function | Example |
| :----: | :----: | :----: |
| UnaryOperator<T> | T apply(T t) | String::toLowerCase |
| BinaryOperator<T> | T apply(T t) | BigInteger::add |
| Predicate<T> | boolean test(T t)| Collection::isEmpty |
| Function<T, R> | R apply(T t) | Arrays::asList |
| Supplier<T> | T get() | Instant::now |
| Consumer<T> | void accept(T t) | System.out::println |

__When to create your own functional interface?__ The following example answers this question.

With `public interface ToIntBiFunction<T,U>`, why do we still need `public interface Comparator<T>`? The benefits are:
* it has a descriptive name and it benefits a __large__ amount of implementors
* it forces a strong contract to be implemented
* it comes with a suite of default methods that make this interface robust and versatile

### Use Stream Judiciously

Stream APIs provide succinct expressions for pipelined processing logic. This stage's output is the next stage's input.

__Stream vs Iterative__

Iterative code is versatile:
* able to read/write local variables
* able to perform conditional `switch/break/continue`
* able to raise exceptions

Stream API provides functional programming syntax, which is concise and easy-to-follow for some staged processing. More on this continue in the next point: [Prefer Side-effect-free Functions in Streams](#prefer-side-effect-free-functions-in-streams).

### Prefer Side-effect-free Functions in Streams

The most important part of the streams paradigm is to structure your computation as a sequence of transformations where the result of each stage is as close as possible to a __pure function__ of the result of the previous stage. A pure function is one whose result depends only on its input: it does not depend on any mutable state, nor does it update any state. In order to achieve this, any function objects that you pass into stream operations, both intermediate and terminal, should be free of side-effects.

Intermediate Operations
* filter
* map
* sort
* limit

Terminal Operations
* toMap
* toList
* toSet
* forEach (should only be used to print the result of a stream computation rather than performing actual computation, mis-used by many programmers including myself)

__An detailed example of "map collector"__ - `toMap`

Simplest form:
* `toMap(Function<? super T,? extends K> keyMapper, Function<? super T,? extends U> valueMapper)`

When there is a key collision, it throws `IllegalStateException`. If you want to resolve the conflict in the process, use:
* `toMap(Function<? super T,? extends K> keyMapper, Function<? super T,? extends U> valueMapper, BinaryOperator<U> mergeFunction)`

The last version of it parameterizes on what type of `map` you want to have as output:
* `toMap(Function<? super T,? extends K> keyMapper, Function<? super T,? extends U> valueMapper, BinaryOperator<U> mergeFunction, Supplier<M> mapSupplier)`

```java
// last-write-win policy example
toMap(KeyMapper, ValueMapper, (v1, v2) -> v2)

// Album example: map each artist to his/her best selling album
// {@code comparing} takes a function and returns a {@code Comparator} (a BiFunction)
// {@code maxBy} takes a {@code Comparator} and returns a {@code BinaryOperator}
albums.collect(toMap(
  a -> a.artistName,
  a -> a,
  maxBy(comparing(Album::sales))
))
```

__Note__ that `groupingBy` has similar design.

### Prefer Collection to Stream as a Return Type

`Collection` interface implement `Iterable` but `Stream` doesn't. So for public APIs, it'd be more flexible to return a collection than a stream, given that the collection can fit into memory.

One problem with `Collection` is that it has upper limit (`Integer.MAX_VALUE`) for number of elements stored, in which case the following return types can be used:
* `Iterable`
* `Stream Object`
* some customized smaller collection that can unwrap itself during Runtime

### Use Caution When Making Streams Parallel

Stream API provides `.parallel()` to convert a single-threaded streaming process into a multi-threaded one, which involves `spliting/reducing/combining/locking` etc. With the overhead, it provides __potential__ performance improvement (in a multi-core machine).

Internally, the parallelism is realized based on some heuristics. So there is NO guarantee that the converted streaming process will have consistent behavior or return correct results. In other words, the builtin heuristics might or might not cover the computation logic that you have written in `Stream API`. When it doesn't, unexpected behavior might occur.

Practically speaking, it rarely provides benefits over cost. However we need to understand that performance gains from parallelism are best on streams over `Arrays/IntIterables/LongIterables/ArrayList/HashMap/HashSet/ConcurrentHashMap` because:
  * they can be accurately and cheaply splitted into subsets.
  * they provide good-to-excellent __locality of references__ when processed sequentially. Good locality of reference means the referenced objects stay close to each other in memory. Among of the them, `primitive arrays` are the best. Poor locality means the executor threads spend more time waiting for data to be transferred from memory to CPU.
  * Intermediate operations are better candidates than terminal operations for parallelism. For example, `reduce()/sum()` are good while `collect()` is bad because it maintains a mutable state internally and combining collections are expensive.

# Methods

### Check Parameters for Validity

There are class-level documentation and method-level documentation. Class-level documentation applies to all methods under this class. In the space of validating arguments, if an argument is `null`, every method will throw `NullPointerException`, which should be documented at class level.

Some typical exceptions throw during argument validation:
* `IllegalArgumentException`
* `IndexOutOfBoundsException`
* `NullPointerException`

Some methods validating arguments:
* `Objects.requireNonNull()`
* `Preconditions.checkNotNull()`

Some annotations serve as a reminder:
* `@Nullable`
* `@NotNull`

### Make Defensive Copies When Needed

About defensive copies:
* Return defensive copies to clients so the client will never be able to modify the internals of the returned object
* Make defensive copies when you receive an object reference from client so any damage made to the object will not impact your program
* No defensive copy is needed when you use immutable objects

__Use Example of Defensive Copies__  

```java
/*
* An example of how attacking can be done without defensive copies
*/
public final class Period {

  private final Date start;
  private final Date end;

  public Period(Date start, Date end) {
    this.start = start;
    this.end = end;
  }

  public Date getStart() {
    return this.start;
  }

  public Date getEnd() {
    return this.end;
  }
}

// Example of attacking the internals of a "Period" instance
Date start = new Date();
Date end = new Date();
Period period = new Period(start, end);
end.setYear(78); // modified internals of "period"

/*
* An example of how correction can be done with defensive copies
*/
public final class Period {

  private final Date start;
  private final Date end;

  public Period(Date start, Date end) {
    this.start = new Date(start.getTime());
    this.end = new Date(end.getTime());
  }

  public Date getStart() {
    return new Date(this.start.getTime());
  }

  public Date getEnd() {
    return new Date(this.end.getTime());
  }
}
```

### Design Method Signatures Carefully

__Notes__
* Choose method names carefully (Refer to [Adhere to Generally Accepted Naming Conventions](#adhere-to-generally-accepted-naming-conventions))
* Don't over provision convenience methods.
* Avoid long parameter list
  * break a method into multiple __orthogonal__ methods
  * create helper class or model class to hold a group of parameters
* For parameter types, favor interfaces over classes.
* Prefer two-element `enum` to `boolean` parameters. Enum name makes better sense than boolean in function signatures

### Use Overloading Judiciously

The choice of which `overloaded` method to invoke is decided at Compile time. In other words, Runtime argument type doesn't change it. The choice of which `overriden` method to invoke is decided at Runtime.

__Rule of Thumb:__
1. Avoid confusing use of overloaded method. Overloading with different number of parameters is non-confusing.
2. Or the overloaded methods have radically different types of parameters, meaning they don't have any overlapped ancestor on their inheritance trees.
3. When you have to overload the constructor with the same number of parameters and similar type of parameters, try static factory methods with different names.

__Overloading Example__

```java
public class CollectionClassifier {

  public static String classify(Set<?> set) {
    return "Set";
  }

  public static String classify(List<?> list) {
    return "List";
  }

  public static String classify(Collection<?> collection) {
    return "Collection";
  }
}

// RUNTIME
Collection<?>[] collections = {
  new HashSet<String>(),
  new ArrayList<Integer>(),
  new HashMap<String, Integer>()
};
for (Collection<?> c : collections) {
  System.out.println(CollectionClassifier.classify(c))
}

// The above runtime prints out:
// Collection
// Collection
// Collection
```

__Overriding Example__
```java
class Wine {
  String name() { return "wine"; }
}

class SparklingWine extends Wine {
  @Override String name() { return "sparkling wine"; }
}

class Champagne extends SparklingWine {
  @Override String name() { return "champagne"; }
}

// RUNTIME
List<Wine> wineList = List.of(new Wine(), new SparklingWine(), new Champagne());
for (Wine w : wineList) {
  System.out.println(w.name());
}
// The above runtime prints out:
// wine
// sparkling wine
// champagne
```

### Use Varargs Judiciously

Varargs is useful when you need to pass a variable number of arguments at Runtime. However, one need to exercise care when using varargs in performance-critical situations as every invocation of a varargs method costs an array allocation and initialization.

### Return Empty Collections or Arrays, Not Nulls.

Title says it all.

When you return a lot of empty `Collections`, use the following shared immutable objects will give you better performance:
* `Collections.emptyList()`
* `Collections.emptyMap()`
* `Collections.emptySet()`

### Return Optionals Judiciously

Prefer returning `Optional` to returning `null`. Returning `null` implicitly requires checking on client side whereas returning `Optional` does that explicitly. However, using `Optional` does introduce performance overhead.

### Write JavaDoc Comments for All Exposed API Elements

[How To Write Doc Comments](https://www.oracle.com/technical-resources/articles/java/javadoc-tool.html) is the definitive guide on this topic.

Javadoc Utility generates documentation automatically from __src code doc comments__.

Some most-commonly used Javadoc Tags:
* `@param`
* `@return`
* `@throws`
* `{@code}` displays text in code font
* `{@implSpec}` describes the method behavior serving as a contract between this class and its subclass
* `{@literal}` supresses the HTML markup, such as "<", ">", "&", "|", etc.

The first sentence of each doc comment is the summary description. For methods, it's usually a verb phrase. For class, fields and interface, it's usually a noun phrase.

# General Programming

### Minimize the Scope of Local Variables

This is one of the things that most programmers should get uncomfortable when NOT doing so.
* minimize the scope of local variables
* keep methods small and focused

### Prefer for-each Loops to Traditional for Loops

I am not talking about `Stream forEach API`.

__for-each loop:__ `for (String s : new String[] {"haha", "hehe", "heihei"})`.

__traditional for loop:__ `for (int i = 0; i < a.length; i++)`.

### Know and Use the Libraries

Numerous features are added to Java libraries in every major release.

Some of the major libraries that every programmer should keep updating themselves with:
* `java.lang`
* `java.util (including java.util.collection, java.util.stream and java.util.concurrent)`
* `java.io`

### Avoid float and double If Exact Answers are Required

When accuracy is critical, use BigDecimal. It gives you control over rounding at the cost of performance overhead.

### Prefer Primitive Types to Boxed Primitives

Eight Primitives
* `boolean`: 1-bit
* `byte`: 8-bit signed integer
* `short`: 16-bit signed integer
* `int`: 32-bit signed integer
* `long`: 64-bit signed/unsigned integer (unsigned long was introduced in Java 8)
* `float`: single precision 32-bit floating point
* `double`: double precision 64-bit floating point
* `char`: 16-bit unicode character

Each primitive has a corresponding reference type, called boxed primitives:
* `Boolean`
* `Byte`
* `Short`
* `Integer`
* `Long`
* `Float`
* `Double`
* `Character`

Using primitives gives performance benefits. Boxing and Unboxing happens automatically during some operations, such as "<", ">", but NOT "==", which compares identities.

So when to use boxed primitives?
1. You can't put primitives into a `collection`
2. You can't use primitives in generic type parameterization
3. You can't declare a variable to be of type `ThreadLocal<int>`

### Avoid Strings Where Other Types are More Appropriate

String should not substitute for `enum` types.

String should not substitute for aggregate types. For the following example, by using a string as a compound key, you would have to write a method to parse each field from the compound key. Instead, you should create a nested static `CompoundKey` class

```java
class Person {
  private String name;
  private int age;
  private int shoeSize;
  private boolean isMarried;
  private String favoriteMovie;
  private String nationality;
  private String favoriteFruit;
  private String address;

  // ... implementation omitted
}

String compoundKey = "Shayang:Batman:41:Orange"

public String makeCompoundKey(Person p) {
  return new StringJoiner(":")
    .add(p.name)
    .add(p.favoriteMovie)
    .add(Integer.toString(p.shoeSize))
    .add(p.favoriteFruit)
    .toString()
}

public getFavoriteMovieFromCompoundKey(String key) {
  return key.split(":")[1];
}
```

### Beware the Performance of String Concatenation

String concatenation by `+` is slow when doing it repeatedly because each created string is immutable. Use `StringBuilder` when performance matters. `StringBuffer` is a thread-safe implementation of `StringBuilder`, which introduces unnecessary overhead when used in a single-threaded process.

### Refer to Objects by Their Interfaces

If appropriate interfaces exist, parameters, return types, variables, fields should all be declared using interface types. It will make your program more flexible to developers and robust to clients.

### Prefer Interfaces to Reflection

Reflexction offers programmatic access to arbitrary classes:
* Given any object, you can obtain `Constructor/Method/Field instances` representing the object's `constructors/methods/fields`
* Given the name of a specific class, you can instantiate an instance when its interface is known but its implementation class is unknown to you at Compile time. __The below example is actually the best practice to use reflection.__

```java
public class MainExample {

  // Reflective instantiation with interface access
  public static void main(String[] args) {

    // Translate the class name into a Class object
    Class<? extends Set<String>> cl = null;
    try {
      cl = (Class<? extends Set<String>>) Class.forName(args[0]);
    } catch (ClassNotFoundException e) {
      fatalError("Class not found");
    }

    // Get the constructor
    Constructor<? extends Set<String>> cons = null;
    try {
      cons = cl.getDeclaredConstructor();
    } catch (NoSuchMethodException e) {
      fatalError("No parameterless constructor");
    }

    // Instantiate the set
    Set<String> s = null;
    try {
      s = cons.newInstance();
    } catch (IllegalAccessException e) {
      fatalError("Constructor not accessible");
    } catch (InstantiationException e) {
      fatalError("Class not instantiable");
    } catch (InvocationTargetException e) {
      fatalError("Constructor threw " + e.getCause());
    } catch (ClassCastException e) {
      fatalError("Class doesn't implement Set");
    }

    // Exercise the instance
    s.addAll(Arrays.asList(args).subList(1, args.length));
  }

  private static void fatalError(String msg) {
    System.err.println(msg);
    System.exit(1);
  }
}
```

The above demo code involves 6 different Runtime exceptions and instantiates an object using 25 lines of code (extremely verbose). But if you want to work with a class unknown at Compile time, you should use reflection so far as to instantiate an object and access the object using some interface or super class that's known at Compile time.

__Cons of Using Java Reflection__
* lose Compile time type checking
* clumsy code to perform reflective access
* create 10 times performance burden

### Use Native Methods Judiciously

People used to use __Java Native Interface (JNI)__ to call native methods written in C/C++ for performance. They provide access to platform specific facilities such as registry and native libraries.

Now it is unnecessary as JVM became comparable in performance. Plus native memory usage is not tracked by `Garbage Collector` and thus native methods are not safe.

### Don't Optimize Prematurely

When you start off writing a program, design with encapsulation being the highest priority, rather than performance. However, avoid designs that limit performance because architectural performance bottleneck cannot be erased without rewriting the whole thing. Usually, good performance comes with good designs.

More specifically,
* implementation details can be optimized later.
* the components that specify interactions between components and/or with external dependencies are the most difficult to change, such as APIs and persistent data formats.
* using profiling tools and/or JMH (microbenchmarking framework) to measure the performance before any serious optimization.

### Adhere to Generally Accepted Naming Conventions

Definitive Guide: [The Java Language Specification](https://docs.oracle.com/javase/specs/).

In general:
* Instantiable classes are noun phrases such as `Thread, PriorityQueue`
* Non-instantiable utility classes are plural noun phrases, such as `Collections`
* Interfaces are noun phrases such as `Collection` or adjective phrases such as `Comparable`
* Methods are verb phrases, such as `getSize(), isEmpty()`
  * Type converting methods are like `toString(), toArray(), asList()`
  * Static factory methods names are like `.of(), .from(), .valueOf(), .getInstance(), .newInstance()`
  * Java bean `Getter & Setter` are simplified as `car.speed()` and `car.speed(60)`
* Naming conventions for fields and local variables are less obvious


Some examples

| Identifier Type | Example |
| :----: | :----: |
| Package or Module | org.junit.jupiter.api.com, com.google.common.collect |
| Class or Interface | Stream, FutureTask, LinkedHashMap, HttpClient |
| Method or Field | remove, groupingBy, getCrc |
| Constant Field | MIN_VALUE, NEGATIVE_INFINITY |
| Local Variable | i, denom, houseNum, concurrentTask |
| Type Parameter | T, E, K, V, X, R, T1, T2 |

Note
* T for arbitrary type
* E for element type of a collection
* K, V for key type and value type of a map
* X for exception
* R for return type
* T1, T2 for a sequence of arbitrary types

# Exceptions

### Use Exceptions Only for Exceptional Conditions

Exceptions are, as their name implies, to be used only for exceptional conditions; they should never be used for ordinary control flow. A well-designed API must not force its clients to use exceptions for ordinary control flow.

For example, what if `Iterable` doesn't have `boolean hasNext()` API? What would be the termination condition for an iteration?
```java
// Horrible abuse of exceptions. Don't ever do this!
try {
  while (true) {
    iterator.next()
  }
} catch (NoSuchElementException e) {
  // pass
}
```

Side note to introduce __state-dependent method and state-testing method__: To generalize this example, a class with a "state-dependent" method such as `iterator.next()` should have a separate "state-testing" method such as `iterator.hasNext()`. Note that such design doesn't work out in concurrent scenarios without external synchronizations. An alternative is to have a distinguished return value such as empty `Optional` for the state-dependent method.

### Use Checked Exceptions for Recoverable Conditions and Runtime Exceptions for Programming errors

__Notes__
* Throw checked exceptions when you demand them being caught and handled in the downstream process.
* Throw runtime exceptions when there is a programming error. For example, a client violates the API contract.
* When in doubt, throw runtime exceptions according to [Avoid Unnecessary Use of Checked Exceptions](#avoid-unnecessary-use-of-checked-exceptions).
* Error is reserved for the use by the JVM to indicate resource deficiency, invariant failures or other conditions that make it impossible to continue execution.

### Avoid Unnecessary Use of Checked Exceptions

__Checked exceptions__ forces programmers to deal with the problems, enhancing reliability. The cost of throwing checked exceptions are:
* it requires a client to deal with it
* it conflicts with stream API

Looking back at [Use Exceptions Only for Exceptional Conditions](#use-exceptions-only-for-exceptional-conditions), two alternatives are:
* return empty `Optional` rather than throwing
* "state-testing" based refactor

But either way, it loses the rich info that comes with the checked exceptions.

```java
// before refactor
try {
  obj.action(args)
} catch (CheckedException e) {
  // handle
}


// after refactor
if (obj.isActionable(args)) {
  obj.action(args)
} else {
  // handle
}
```

__When to use checked exceptions?__
* exceptional conditions can't be prevented by proper use of the API (enforced by Runtime exception).
* throwing checked exceptions can pass down useful info to make the downstream logic clearer.

### Favor the Use of Standard Exceptions

| Exception | Use |
| :----: | :----: |
| IllegalArgumentException | Non-null parameter value is inappropriate |
| IllegalStateException | Object state is inappropriate for method invocation |
| NullPointerException | Parameter value is null where prohibited |
| IndexOutOfBoundsException | Index parameter value is out of range |
| ConcurrentModificationException | Concurrent modification of an object has been detected where prohibited |
| UnsupportedOperationException | Object does not support method |
| ArithmeticException | Denominator is zero |
| NumberFormatException | Unable to convert a string into a number |

### Throw Exceptions Appropriate to the Abstraction

__What is exception propagation?__ and how does it hurt if not done appropriately?
```java
class B {

  // class implementation omitted

  protected void helper() throws CheckedExceptionB {
    // method implementation omitted
  }
}

class C {

  // class implementation omitted

  protected void helper() throws CheckedExceptionC {
    // method implementation omitted
  }
}


class A extends B {

  // class implementation omitted

  public void invokeApi() throws CheckedExceptionB {
    helper()
  }
}

// Client Side Code
try {
  new A().invokeApi()
} catch (CheckedExceptionB b) {
  // handle
}

// Server Side Change Breaks Client Side Code
class A extends C {

  // class implementation omitted

  public void invokeApi() throws CheckedExceptionC {
    helper()
  }
}
```

__How exception translation helps?__
```java
// Original Implementation
class A extends B {

  // class implementation omitted

  public void invokeApi() throws CheckedExceptionA {
    try {
      helper()
    } catch (CheckedExceptionB b) {
      throw new CheckedExceptionA(b) // exception chaining
    }
  }
}

// Client Side Code
try {
  new A().invokeApi()
} catch (CheckedExceptionA a) {
  // handle
}

// Changed Implementation Doesn't Break Client Side Code
class A extends C {

  // class implementation omitted

  public void invokeApi() throws CheckedExceptionA {
    try {
      helper()
    } catch (CheckedExceptionC c) {
      throw new CheckedExceptionA(c) // exception chaining
    }
  }
}
```

__Note:__ "exception chaining" helps pass low-level exception info to high-level exception. Most standard exceptions implementation has "chaining-aware" constructors.

### Document All Exceptions Thrown by Each Method

Use the JavaDoc `@throws` tag to document each __checked__ exception that a method can throw in terms of its conditions under which it throws.

### Include Failure-capture Information in Detail Messages

When throwing exceptions, include as much info contributing to the exceptional case as possible. For example, when you throw `IndexOutOfBoundsException`, you should include the lowerBound, upperBound and indexValue. All of them can go wrong.

A better way to achieve this is to require these essential info to be included in the exception `constructor`, as well as in its `toString()` method.

### Strive for Failure Atomicity

Generally, a failed method invocation should leave the object in the state that it was __prior to the invocation__. A method with this property is __failure-atomic__. The applicables of the property can range from exception handlings to data persistence.

Several ways to achieve this:
* Use immutable objects
* Perform checks before making changes so that it doesn't fail
* Perform operations on a temporary copy of the modifiable and then swap (atomic operation)
* Intercept the failure that occurs during an operation and roll back the persistent data structure

### Don't Ignore Exceptions

The title says if you catch an exception, don't leave the catch block empty. Otherwise, you are ignoring it. If you intentionally do so, name the exception variable "ignored" and leave a comment why it's ignored.

# Concurrency

### Synchronize access to shared mutable data

Can you see why the following program never terminates?
```java
// Broken program. Never terminates.
public class StopThread {

  private static boolean stopRequested;

  public static void main(String[] args) throws InterruptedException {
    Thread backgroundThread = new Thread(() -> {
      int i = 0;
      while (!stopRequested) {
        i++;
      }
    });

    backgroundThread.start();

    TimeUnit.SECONDS.sleep(1);

    stopRequested = true;
  }
}
```

The above program never terminates because, without explicit synchronization, JVM does the following optimization known as "hoisting":
```java
// before transformation
while (!stopRequested) {
  i++;
}

// after transformation
// JVM assumes nothing in the current thread changes the value of "stopRequested"
// then JVM decides not to evaluate the value of "stopRequested" at each iteration
if (!stopRequested) {
  while (true) {
    i++;
  }
}
```

Now things become different with synchronization.
```java
// Properly synchronized cooperative thread termination
public class StopThread {

  private static boolean stopRequested;

  private static synchronized void requestStop() {
    stopRequested = true;
  }

  private static synchronized boolean stopRequested() {
    return stopRequested;
  }

  public static void main(String[] args) throws InterruptedException {
    Thread backgroundThread = new Thread(() -> {
      int i = 0;
      while (!stopRequested()) {
        i++;
      }
    });

    backgroundThread.start();

    TimeUnit.SECONDS.sleep(1);

    requestStop();
  }
}
```

The `synchronized` keyword not only ensures "mutual exclusion" meaning that only a single thread executes the synchronized block of code at a time, it also ensures that any thread entering the synchronized block sees the effects of all previous modifications that were guarded by the same lock (communication effect).

Note that synchronization is not guaranteed to work unless both READ and WRITE operations are synchronized (as demonstrated in the above example).

Further note that in this use case, the `synchronized` method is atomic regardless of synchronization. In other words, the `synchronized` keyword here is used solely for its communication effect, not for mutual exclusion.

With that being said, the performance overhead can be reduced by using `volatile` keyword as it guarantees that any thread reading the `volatile` field will see the most recently written value as demonstrated below.

```java
// Cooperative thread termination with a volatile field
public class StopThread {

  private static volatile boolean stopRequested;

  public static void main(String[] args) throws InterruptedException {
    Thread backgroundThread = new Thread(() -> {
      int i = 0;
      while (!stopRequested) {
        i++;
      }
    });

    backgroundThread.start();

    TimeUnit.SECONDS.sleep(1);

    stopRequested = true;
  }
}
```

Carefully note that `volatile` works incredibly well in the above example because READ (`while (!stopRequested)`) and WRITE (`stopRequest = true`) operations are __atomic__. When one of them is not atomic, things will go wrong. For example:
```java
// Broken! It requires synchronization.
class SerialNumberGenerator {

  private static volatile int nextSerialNumber = 0;

  public static int generate() {
    return nextSerialNumber++;
  }
}
```

The problem is that increment operation `++` is not atomic. It reads the value, increment and write back the new value. So some other thread can interleave the operations. "Atomic Primitives" as defined in `java.util.concurrent.atomic` are designed for situations like this. Using "Atomic Primitives" provides better performance than using `synchronized` keyword as it is internally lock free.

```java
class SerialNumberGenerator {

  private static volatile AtomicLong nextSerialNumber = new AtomicLong();

  public static long generate() {
    return nextSerialNumber.getAndIncrement();
  }
}
```

### Avoid Excessive Synchronization

Some principles to write synchronized block.

__Principle 1:__ Inside a synchronized block, don't invoke a method that's designed to be overriden, or one provided by a client in the form of a function object. It's dangerous if the implementation is not under full control. For example, the implementation can start a new thread and try to acquire the lock from "this" class, which would fail if the orginal thread is holding the lock, which then turns into a dead lock situation.

__Principle 2:__ Do as little work as possible inside synchronized block.

__Principle 3:__ If you are debating between the following two designs:
* having a class with synchronized internals, such as `StringBuffer, ConcurrentHashMap`
* having a class that doesn't take care of synchronization but it allows client to place synchronization from external.

Take the second design unless you can achieve significantly higher concurrency with internal synchronization implementation, which is possible by using various techniques, such as "lock splitting", "lock stripping", "non-blocking concurrency control", etc but usually unnecessary.

Taking the first design will incur unnecessary overhead when the class is used where concurrency is not needed. However taking 2 always allows you to synchronize whenever needed.

### Prefer Executors, Tasks and Streams to Threads

A complete guide on this topic: Java Concurrency In Practice.

__ThreadPoolExecutor__ is the most general executor that allows you to basically config anything to meet your concurrent needs.

__SingleThreadExecutor__ implements "single threaded work queue" pattern.

__ScheduledThreadPoolExecutor__ allows you to schedule tasks to run periodically or at a particular time.

__CachedThreadPool__ starts as many threads as new tasks come in. It gives horrible throughput when traffic is more than what the server can handle. Because CPU spends most of its time doing context switching instead of actual work.

__FixedThreadPool__ comes to rescue under the above circumstances.

__ForkJoinPool__ allows executors to "steal" tasks from each other even across different threads, resulting in better CPU utilization. `Stream.parallel()` API is written on top of ForkJoinPool.

Note that `Callable` is just a `Runnable` capable of returning a value and/or throwing an exception.

### Prefer Concurrency Utilities to Wait and Notify

`String.intern()` (introduced in [Avoid Creating Unnecessary Objects](#avoid-creating-unnecessary-objects)) can be implemented using `ConcurrentMap` as below example shows:
```java
public class String {

  private static final ConcurrentMap<String, String> map = new ConcurrentHashMap<>();

  public static String intern(String s) {
    String previousValue = map.putIfAbsent(s, s);
    return previousValue == null? s : previousValue;
  }
}
```

`map.putIfAbsent()` is a "state-dependent" modify operation made __atomic__ by using `synchronized` keyword on its partial implementation. However `map.put()` is not synchronized. In concurrent scenarios it will incur racing condition where multiple threads are racing to update the same key. Beside `.putIfAbsent()`, `.reduce()` and `.compute()` implementations are also synchronized.

`ConcurrentHashMap` is one of the high-performance concurrent implementations of the stardard `Map` interface. Same thing goes for other `Collection` interfaces, such as `List, Queue` etc. These implementations manage their own synchronizations internally. Therefore, applying additional synchronization from external only slows it down.

These concurrent collections are 1/3 of the `java.util.concurrent` offerings, the other 2/3 of it are:
* Executor Framework ([Prefer Executors, Tasks and Streams to Threads](#prefer-executors,-tasks-and-streams-to-threads))
* synchronizers

Synchronizers include:
* `CountDownLatch` (most commonly used)
* `CyclicBarrier`
* `Phaser`

The following example helps to understand how `CountDownLatch` works. The example is often used to reproduce concurrency bugs as it can force arbitrary number of threads to perform some piece of logic concurrently.

```java
public class ConcurrentExecution {

  // Simple Framework for timing concurrent executions
  public static long time(Executor executor, int concurrency, Runnable runnable) throws InterruptedException {

    CountDownLatch ready = new CountDownLatch(concurrency);
    CountDownLatch start = new CountDownLatch(1);
    CountDownLatch done = new CountDownLatch(concurrency);

    for (int i = 0; i < concurrency; i++) {
      executor.execute(() -> {
        ready.countDown(); // acknowledge ready
        try {
          start.await(); // wait here till @{code start.countDown()}
          runnable.run();
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();;
        } finally {
          done.countDown(); // acknowledge done
        }
      });
    }

    ready.await(); // wait till all executors acknowledge ready

    long startNanos = System.nanoTime();

    start.countDown(); // fire

    done.await(); // wait till all executors acknowledge done

    return System.nanoTime() - startNanos;
  }
}
```

For legacy API, such as `wait(), notify(), notifyAll()`, they are replaced by the above `java.util.concurrent` offerings. In case you need to maintain legacy code, make sure you always invoke `.wait()` from within a while loop using the standard idiom. The loop serves to test the condition before and after waiting. Testing the condition before waiting and skipping if the condition already holds are necessary to ensure __liveness__. Testing the condition after waiting and waiting again if the condition does not hold are necessary to ensure __safety__.

```java
// The stardard idiom for using the wait method
synchronized (obj) {
  while (<condition does not hold>) {
    obj.wait() // release lock and reacquires on wakeup
    // perform action appropriate to condition
  }
}
```

Under this idiom. One should always use `notifyAll()` rather than `notify()`. Because it guarantees that you will wake the threads that need to be awakened. You might wake some other threads too but this won't affect the correctness of your program because these threads will check the condition for which they are waiting, finding it false and then it will continue waiting.

### Document Thread Safety

Thread Safety Levels
1. Immutable
2. Unconditionally thread safe (`AtomicInteger, ConcurrentHashMap`)
3. Conditionally thread safe (`collections.synchronizedMap` whose iterator requires external synchronization)
4. Not thread safe (`ArrayLsit, HashMap`)

### Use Lazy Initialization Judiciously

Using or not using lazy initialization is determined by whether it brings performance benefits. In concurrent programming, lazy initialization is tricky.

For __instance field lazy initialization__, use "synchronized accessor idiom":
```java
private FieldType field;

private synchronized FieldType getField() {
  if (field == null) {
    field = computeFieldValue();
  }
  return field;
}
```

For __static field lazy initialization__, use "holder class idiom":
```java
private static class FieldHolder {
  static final FieldType field = computeFieldValue();
}

private static FieldType getField() {
  return FieldHolder.field;
}
```

For __instance field lazy initialization__ with performance improvement, use "double-check idiom". Two variants of the idiom are:
* single-check idiom
* racy single-check idiom

### Don't Depend on the Thread Scheduler

The best way to write a robust, responsive, portable program is to ensure that the average number of `runnable` threads is not significantly greater than the number of processors.

The main technique for keeping the number of `runnable` threads low is to have each thread do some useful work and the wait for more. Threads should not run if they are not doing useful work.

The following example demonstrates what is __busy-wait__:
```java
// Awful CountDownLatch implementation - busy-waits incessantly
public class SlowCountDownLatch {

  private int count;

  public SlowCountDownLatch(int count) {
    if (count < 0) {
      throw new IllegalArgumentException(count + " < 0");
    }
    this.count = count;
  }

  public void await() {
    while (true) {
      synchronized (this) {
        if (count == 0) {
          return ;
        }
      }
    }
  }

  public synchronized void countDown() {
    if (count != 0) {
      count--;
    }
  }
}
```

# Serialization

### Prefer Alternatives to Java Serialization

__A fundamental problem with Java serialization__ is that its attack surface is too big to protect, and constantly growing: Object graphs are deserialized by invoking the `readObject` method on an `ObjectInputStream`. This method is essentially a magic constructor that can be made to instantiate objects of almost any type on the class path, so long as the type implements the `Serializable` interface. In the process of deserializing byte stream, this method can execute code from any of these types, so the code for all of these types is part of the attack surfaces.

Some problem examples:
* Java serialization is widely used by Java subsystems, such as __Remote Method Invocation (RMI)__, __Java Management Extension (JMX)__ and __Java Messaging System (JMS)__.
* Deserialization of untrusted input streams can result in __Remote Code Execution (RCE)__.
* Deserialization of 100-level nested `HashSet` instance can result in __Denial-of-Service attack (DOS)__, aka deserialzation bomb.

__What do you do?__ Don't use it.

__What do you use?__ JSON and Protobuf.

The purpose of serialization and deserialization is to fulfill __cross-platform high-performance translation between structured data and byte/char sequences__. The leading cross-platform structured data representations are __JSON and Protobuf__. What they have in common is that they are far simpler than Java serialization. They don't support automatic serialization and deserialization of arbitrary object graphs. Instead they support simple, structured data-objects consisting of a collection of attribute-value pairs. Only a few primitives and array data types are supported. This simple abstraction turns out to be sufficient for building extremely powerful distributed systems.

__JSON vs Protobuf__
* Originally JSON was designed for browser-server communication, and Protobuf was designed to storing and interchanging structured data between servers.
* JSON is text-based, whereas Protobuf is binary.
* JSON is exclusively a data representation, whereas Protobuf offers "schemas (types)" to document and enforces appropriate usage.

### Implement Serilizable with Great Caution

A major cost of implementing `Serialzable` is that it decreases the flexibility to change a class's implementation once it has been released.

A second cost of implemnting `Serializable` is that it increases the likelihood of bugs and security holes. 

A third cost of implementing `Serializable` is that it increases the testing burden associated with releasing a new version of a class.

Implementing `Serialzable` is not a decision to be undertaken lightly.

Classes designed for inheritance should rarely implement `Serializable`, and interfaces should rarely extend it.

Inner classes should not implement `Serializable`.

### Consider Using a Custom Serialized Form

Trivial details.

### Write readObject Methods Defensively

Trivial details.

### For Instance Control, Prefer enum Types to readResolve

Trivial details.

### Consider Serialization Proxies Instead of Serialized Instances

Trivial details.