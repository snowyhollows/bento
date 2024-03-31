# Bento - scope-oriented, minimal dependency injection

Dependency Injection allows us, programmers to:
- only write what we need, not boilerplate;
- shape the code like clay, not like stone;
- stay in the flow even when refactoring and trying out new ideas.

You can start using Bento by with (the quickstart guide)[docs/index.md], or read on the rant explaining the philosophy and the frustration behind it.

## Unfortunately, Dependency Injection turned into a cult.

DI is a marvelous idea. It empowers true Object-Oriented Programming, because it cuts the burden of object creation and configuration. It makes refactoring easy, it makes testing easy, it allows code reuse - it makes the world a better place.

Sounds too good to be true? No. It's even better than it sounds.

So, what's the problem?

DI enabled whole new generation of paradigms - at the cost of losing its identity. The original simplicity dissolved. Dependency Injection turned into a stale buzzword, associated with Aspects Oriented Programming, proxying, reflection, unit testing, writing to interfaces and bloated enterprise solutions. DI became synonymous with "Spring" and using DI in a browser game became synonymous with "overthinking".

What started as an enabler - is now seen as a hurdle, because the available tooling, like corporate salesmen, tries to upsell programmers on a whole set of additional programming techniques.

Time to stop this piggybacking. Writing games for jams,
coding challenges, one-off scripts, simulations and proof-of-concepts - all these tasks could hugely benefit from DI, but should
never pay the complexity costs of features like compile-time object-graph validation, runtime scanning or even runtime resolution of services. Those cases need a solid, basic DI with focus on configuration and object life-cycles. This is where Bento comes in.

### So, what is Dependency Injection, really?

It's the art of removing any "dependency" from the code, making it more focused, simpler, and easier to refactor. 

A "dependency" can be:

- any piece of configuration (like a file path, a port number, a database connection string),
- any statically known type (!),
- any object that is created by the code.

Each of the above can be pushed outside, to whoever instantiates the code. And then - pushed further and further, until it reaches some boundary, where at last the configuration can happen. Often this boundary is the entire application (where the dependencies can be read from a configuration file), but it could equally well be any other scope - for example an entire level of a game or a stage of a simulation. Or an object representing a single web request in a web application.

Handling dependencies is practically the same as handling exceptions - but in the opposite direction (top-to-bottom, whereas the exceptions are handled bottom-to-top). Instead of handling exceptions locally, a well-behaved program should pass any exception upstream, perhaps transforming them somehow. The exceptions, like dependencies, can be handled at the boundary, which can be the entire application (where the exception can simply be thrown from `main`) or some scope (for example - a single request in a web application).

### Show, don't tell

Here's a class with three dependencies: a value, an object and a statically known type:

```java

class MissileFiringAlien {
    public static final float HOW_OFTEN_FIRES = 1.0f;
    private float lastFired = 0;
    
    public void step() {
        World world = World.getInstance();
        if (world.getTime() > lastFired + howOften) {
            world.spawn(new Missile());
            lastFired = world.getTime();
        }
    }
}

```

The three dependencies of the code above are:
- `HOW_OFTEN_FIRES` - a value. It's really part of configuration, but the code makes it impossible to change without recompilation.
- `World` - it's a singleton. There's no way to control from the outside of `MissileFiringAlien` which `World` instance it should use, there can always be only one.
- `Missile` - it's a statically known type. There's no way to change the type of missile fired by `MissileFiringAlien`.

Here's the same class, but with dependencies pushed outside:

```java
class MissileFiringAlien {
    // dependencies
    private final float howOftenFires;
    private final World world;
    private final MissilleSupplier missileSupplier;

    // state
    private float lastFired = 0;
    
    public MissileFiringAlien(float howOftenFires, World world, MissilleSupplier missileSupplier) {
        this.howOftenFires = howOftenFires;
        this.worldSupplier = worldSupplier;
        this.missileSupplier = missileSupplier;
    }
    
    public void step() {
        if (world.getTime() > lastFired + howOften) {
            world.spawn(missileSupplier.get());
            lastFired = world.getTime();
        }
    }
}
```

It's slightly longer and pushing the dependencies out does not, in itself solve any problem. The dependencies are the same, and at some stage - they still need to be defined and 
"injected" (fancy word for passing them into the object at construction or initialization time).

The benefit is that the maintenance and reuse are radically easier.

Writing not trivial applications - like scientific simulations, games, web applications and all sorts of creative experiments - is a process of constant refactoring and extending - the opposite to most enterprise applications, where uniformity and stability are the key.

With DI, we can:
- extend the object with any behavior, by just adding extra dependencies.
- make the dependencies as small as possible.
- centralize the configuration into a single place - like a configuration file, or a scope object.
- move the class to other projects, without problems like: "whops, it requires constants from three other classes, and a singleton".

Dependency Injection frees us from:

- Passing some global god-object like `Game` or `Config` everywhere, just in case it's needed.
- Creating huge classes with dozens of public static final fields, and starting each new project by copying them.
- Configuring applications by invisible and untraceable global settings (which causes your classes to break in mysterious ways when copied to another project)

### TLDR

Writing to interfaces, Aspect Oriented Programming, testability and Reflection are nice, but 100% of benefits of Dependency Injection can come from not hard-coding dependencies, and instead passing them to the object at construction time. Code dependencies are:

- simple values, like strings or floats (traditional configuration);
- other objects (also configuration - realizing this is the key to understanding DI);
- statically known types (like `Missile` in the example above).

## Bento - the quickstart

Bento provides is three things:
- `Bento` - a very simple object for storing dependencies in a map. You can `get` or `set` them. Usually interaction with Bento is limited to one or two lines on startup. Bento objects can form a tree hierarchy, where each child can override the parent's values. This is used for creating scopes (i.e. an application scope, a level scope, a single enemy scope).
- A convention for finding factories for classes by name, so that when an object is needed (Like `World`), Bento knows how to create an instance of that object.
- A standard Java Annotation Processor, which kicks in during compilation and creates simple glue code. It is not strictly necessary to get all the benefits of Bento, but it makes good job of reducing boilerplate. The code generated by Bento is human-readable, easy to debug and usually is just a single method call.

### Bento itself

#### Bento and simple values

This is NOT a typical use of Bento, but Bento can be used as a simple map for storing values and translating them between strings and simple values / enums:

```jshelllanguage
Bento bento = Bento.createRoot();
    
bento.register("number", "12");
assertThat(bento.getInt("number")).isEqualTo(12);
assertThat(bento.getFloat("number")).isEqualTo(12);
assertThat(bento.getString("number")).isEqualTo("12");
```

The idea is that the values can be registered as strings (e.g., from a configuration file), and read back as expected types.

#### Bento and passing dependencies

Bento could be used to implement a service locator pattern - each object could, in theory, receive Bento in its constructor, and read its dependencies from inside.

Like this:

```java

class Box {
    private final float width;
    private final float height;
    
    // possible, but not recommended

    public Box(Bento bento) {
        this.width = bento.getFloat("width");
        this.height = bento.getFloat("height");
    }
    
    // ...
}

```

Now we could create a Box like this:

```jshelllanguage
var bento = Bento.createRoot();
bento.register("width", "10");
bento.register("height", "20");

// we create a box
var box = new Box(bento);

// we make the box available to whoever needs it
bento.register("box", box);
```

This is sort of OK, but has two problems:
- the dependencies of Box are invisible from the outside. It's not clear what Box needs to work.
- there is a dependency on Bento itself - and object which, for most part, should be invisible to the code.
- we instantitate `box` eagerly aeven though we don't know whether anyone will need it.

Instead, we should create a class with explicit dependencies:

```java
class Box {
    private final float width;
    private final float height;

    // recommended: pure, explicit dependencies
    public Box(float width, float height) {
        this.width = width;
        this.height = height;
    }
    // ...getters, setters
}
```
The class above doesn't need Bento at all - and it's clear what it needs to work.

We can now create a factory for it (this is in practice not necessary, we'll see how to avoid the boilerplate soon). Bento convention depends on a factory implemented as an "enum singleton" (as described by Joshua Bloch in "Effective Java"); the enum must implement `BentoFactory<T>` interface, meaning it is a stateless factory, able to produce a `T` object when given a `Bento` instance:

```java
public enum BoxFactory implements BentoFactory<Box> {
  IT;

  public Box createInContext(Bento bento) {
    return new Box(bento.getFloat("width"), bento.getFloat("height"));
  }
}
```

As you can see, the object is clear and has explicit dependencies, while its factory depends on `Bento`, but holds no state. It is a singleton - a pure function - a recipe telling us "how to get a `Box` from a `Bento`.

The gimmick and the core idea that makes Bento tick is in the special way that keys of the `BentoFactory` type are handled.

When we try to get an object from Bento using a factory as the key, Bento checks if such key already exists - and if not, it uses the factory to create a single instance and registers it.

This means that:
- we can call `bento.get(BoxFactory.IT)` many times, we will always get the same box over and over
- we don't need to register the factories - they will be lazily registered and used the first time they are needed:

```jshelllanguage
var bento = Bento.createRoot();

bento.register("width", "10");
bento.register("height", "20");

if (Math.random() > 0.5) {
    var box = bento.get(BoxFactory.IT);
    assertThat(box).isSameAs(bento.get(BoxFactory.IT));
    assertThat(box.getWidth()).isEqualTo(10);
    assertThat(box.getHeight()).isEqualTo(20);
}

```
Creation of factories is simple but tedious - which is why Bento provides an annotation processor to generate them for you, for constructors marked with `@WithFactory`. The annotation processor works at compile time and the annotations do not persist in the compiled classes or runtime, so they do not pollute the code or add any runtime overhead.

In practice, we don't need to see the factory classes or register anything. We simply mark every constructor of our object with `@WithFactory`. We don't never need to pull `Box` out of context, it's enough to make a parameter of the `Box` type in our class, and the code for extracting the `Box` will be created for us in the factory:

```java
public class BoxConsumer {
    private final Box box;

    @WithFactory
    public BoxConsumer(Box box, String name) {
        this.box = box;
        this.name = name;
    }
    
    public void act() {
        System.println("Hello, " + name + "! I have a box with width " + box.getWidth() + " and height " + box.getHeight());
   }
}

```

Behind the cenes, a factory is generated:

```java
public enum BoxConsumerFactory implements BentoFactory<BoxConsumer> {
    IT;

    public BoxConsumer createInContext(Bento bento) {
        return new BoxConsumer(bento.get(BoxFactory.IT), bento.getString("name"));
    }
}
```

Note that calls to `bento` are nested - constructing `BoxConsumer` will trigger retrieval of a `Box`. If a box already exits (e.g., because some other object required it) - it will only be retrieved, not recreated. Otherwise, the `BoxFactory` will be used to create a new box. 

In practice, a whole application, having hundreds of objects, can be created from a single factory and a single initial `get` call. Or a single "context" - such as a level, a stage of simulation, a response in a web application.

### Initial config

A shape of a basic Bento app is registering any required dependencies (service dependencies need not be registered, so these will usually be just simple configuration values):

```java
public class Main {
    
    public static void main(String[] args) {
        var bento = Bento.createRoot();
        bento.register("width", "10");
        bento.register("height", "20");
        bento.register("name", "Alice");
        
        bento.get(BoxConsumerFactory.IT).act();
    }
}

```
Core Bento library does not have any other special means of registering values, but bento-config (on which bento-gdx depends) provides a way to load configuration from a file.

We can use it like this:

```java
public class Main {
    
    public static void main(String[] args) {
        var bento = Bento.createRoot();
        var propertiesLoader = bento.get(PropertiesLoaderFactory.IT);
        propertiesLoader.configureFromProperties("config.properties");
        
        bento.get(BoxConsumerFactory.IT).act();
    }
}

```
config.properties can then be created:
```properties
width=10
height=20
name=Alice
```

Let's reflect on this: no matter how big is our object graph, and no matter how far a dependency - we don't need to change the configuration code. It's enough to add a parameter to our constructor, and the parameter is instantly
configurable via the properties file - all without using reflection or magic.

We can also move the parameters and dependencies between classes without the need to refactor anything around them. If we decide that boxes for "Filip" are to be twice as big, we can experiment with different architectures for achieving this:

```java
public class Box {
    private final float width;
    private final float height;

    @WithFactory
    public Box(float width, float height, String name) {
        float factor = "Filip".equals(name) ? 2f : 1.0f;
        this.width = width * factor;
        this.height = height * factor;
    }

    //...
}
```

Now `name` is passed into `Box` in addition to the size, and the BoxConsumer doesn't need to change.

Or maybe we want the bonus factor configurable?

```properties
width=10
height=20
name=Alice
bonusFactor=3
```

```java
public class Box {
    private final float width;
    private final float height;

    @WithFactory
    public Box(float width, float height, String name, float bonusFactor) {
        float factor = "Filip".equals(name) ? bonusFactor : 1.0f;
        this.width = width * factor;
        this.height = height * factor;
    }

    //...
}
```

Or maybe the logic for calculating the bonus becomes complex and we want to move it to a separate class?

```java
public class Box {
    private final float width;
    private final float height;

    @WithFactory
    public Box(float width, float height, BoxSizeBonusCalculator calculator) {
        float calculatedBonus = calculator.calculateBonus();
        this.width = calculatedBonus * width;
        this.height = calculatedBonus * height;
    }

    //...
}
```

```java
public class BoxSizeBonusCalculator {
    private final float bonusFactor;

    @WithFactory
    public BoxSizeBonusCalculator(float bonusFactor, String name, String namesEligibleForBonus) {
        this.bonusFactor = namesWithBonus.contains(name) ? bonusFactor : 1.0f;
    }

    public float calculateBonus() {
        return bonusFactor;
    }
}
```

Now `Box` requires a `BoxSizeBonusCalculator` to be created, and the `BoxConsumer` still doesn't need to change. Also, more behavior is configurable via properties:

```properties
width=10
height=20
name=Alice
bonusFactor=3
namesEligibleForBonus=Filip, Alice
```

The approach to calculating the bonus in the code above might seem sloppy - maybe less behavior should be hard-coded, maybe there should be a map of names, allowing for different people to have different bonus factor... But that's the whole point. Thanks to Bento, the code is easy to refactor, and the dependencies are explicit and easy to understand. We are free to write fast, sloppy code, containing only the pieces of functionality we currently need - because it's so easy to swap parts in and out. The point isn't to make a huge design up-front, but to make a design that can be easily changed, and that keeps us in the flow while we are writing.

## Bento - more features

99% of the time the only Bento functionality used is the generation of the factories and storing the configuration. There are, however, some helper classes that can be useful in some advanced scenarios. I urge everyone to ignore these features and use them only when they are really needed.

### Injecting by-name and by-type

TODO

### Scopes

TODO

### Implementation switches

TODO

### Factories - the wrapper

TODO

### Categories - instance controlled, configurable objects, aka Dynamic Enums

TODO

### Resettables - dynamic reconfiguration of objects

TODO

