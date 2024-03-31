# Bento - scope-oriented, pragmatic dependency injection

## Introduction

"Dependency" is a mouthful. But it just means "a thing that my code needs to work but can come from outside."

Contrary to popular opinion, in the realm of Java, even basic values like an int, String, or a boolean can be considered dependencies.

When in doubt about whether X is a dependency, a practical approach is to consider if you would like your code to be adaptable by modifying X.

Let's start with a reasonably well written guessing game:

```java
public class Game {

    public static void main(String[] args) {
        int tries = 10;
        int rangeMin = 0;
        int rangeMax = 100;

        int randomNum = rangeMin + (int)(Math.random() * ((rangeMax - rangeMin) + 1));

        System.out.println("Guess a number between " + rangeMin + " and " + rangeMax + ": ");

        Scanner input = new Scanner(System.in);
        while (tries > 0) {
            System.out.println("Tries left: " + tries);
            int guess = input.nextInt();

            if (guess == randomNum) {
                System.out.println("Congratulations! You guessed the number!");
                break;
            } else if (guess < randomNum) {
                System.out.println("Try a higher number.");
            } else {
                System.out.println("Try a lower number.");
            }

            tries--;
        }
    }
}
```

It could well be the optimal way to write such a game.

But humor me and imagine we need to make it reusable in more contexts. Maybe we need the game to work across different platforms, like a browser, a multi-player backend, and Android. Perhaps we want a game where the number of tries decreases, and the range increases with each level.

In the Object-oriented paradigm, we achieve this by separating the dependencies from our logic and stacking them into external classes.

The most straightforward way to do it - is **without** using injection. The value dependencies (ints, Strings) can be moved to constants; the configurable pieces of code that provide behavior - become entire classes:

```java

public class Config {
    public static final int MAX_TRIES = 10;
    public static final int RANGE_MIN = 0;
    public static final int RANGE_MAX = 100;
}

public class InputOutput {
    public void println(String message) {
        System.out.println(message);
    }

    public int nextInt() {
        Scanner input = new Scanner(System.in);
        return input.nextInt();
    }
}

public class Game {
    public static void main(String[] args) {
        int randomNum = Config.RANGE_MIN + (int)(Math.random() * ((Config.RANGE_MAX - Config.RANGE_MIN) + 1));
        int tries = Config.MAX_TRIES;

        InputOutput inputOutput = new InputOutput();

        inputOutput.println("Guess a number between " + Config.RANGE_MIN + " and " + Config.RANGE_MAX + ": ");

        while (tries > 0) {
            System.out.println("Tries left: " + tries);
            int guess = inputOutput.nextInt();

            if (guess == randomNum) {
                inputOutput.println("Congratulations! You guessed the number!");
                break;
            } else if (guess < randomNum) {
                inputOutput.println("Try a higher number.");
            } else {
                inputOutput.println("Try a lower number.");
            }
            tries--;
        }
    }
}
```

The problem is that while dependencies are externalized, they are still hard-coded. There is no way to run multiple instances of the logic simultaneously in a multi-player server with a different range or number of tries because the values always come from the same static fields. Similarly, there's no way to change the implementation of `InputOutput`; even if we extend it and build a `SpecialInputOutput`, the `new` keyword is not polymorphic and will always create an instance of `InputOutput`.

Here's where the "injection" part comes in. Instead of reaching outside from the class to select the values and implementations, we remove all the constructor calls and static field access, replacing them with values passed into our constructor:

```java
public class Game {
    private final int rangeMin;
    private final int rangeMax;
    private final int maxTries;
    private final InputOutput inputOutput;

    /** Constructor with the dependencies injected */
    public Game(int rangeMin, int rangeMax, int maxTries, InputOutput inputOutput) {
        this.rangeMin = rangeMin;
        this.rangeMax = rangeMax;
        this.maxTries = maxTries;
        this.inputOutput = inputOutput;
    }

    public void play() {
        int randomNum =  rangeMin + (int)(Math.random() * ((rangeMax - rangeMin) + 1));
        int tries = maxTries;

        InputOutput inputOutput = new InputOutput();

        inputOutput.println("Guess a number between " + rangeMin + " and " + rangeMax + ": ");

        while (tries > 0) {
            System.out.println("Tries left: " + tries);
            int guess = inputOutput.nextInt();

            if (guess == randomNum) {
                inputOutput.println("Congratulations! You guessed the number!");
                break;
            } else if (guess < randomNum) {
                inputOutput.println("Try a higher number.");
            } else {
                inputOutput.println("Try a lower number.");
            }

            tries--;
        }
    }
}

```

(As an aside: there are other modes of acquiring dependencies, but they share the common core: our code remains passive, it is the caller of our code who is tasked with delivering us everything we need).

The design of the class above is close to optimal with respect to the Object-oriented Paradigm:
- the class has a single responsibility,
- the programmer can make anything customizable.

Something to notice and meditate upon - is that each object can require dependencies, but at the same time - it can itself be a dependency of a higher level.

In fact, all OO programs can be expressed as a single object requiring a set of dependencies - similarly to how any procedural program can be considered a single procedure calling a set of other procedures.

The remaining problems are just technicalities:
- The class is less immediate to use - the user needs to know and provide all the dependencies;
- If the dependencies of the class change, all the instantiations must follow the change.

An elegant solution is to use a specialized Dependency Injection framework. Such frameworks take care of building objects, storing them, and passing them around as dependencies. A program written with a DI container is often bootstrapped by creating a single, main object - the entry point - and with all other objects built and provided as its dependencies.

## Enter Bento

Bento is a mature Dependency Injection library optimized for directness and a distinct lack of magic. It is focused on making the DI a viable solution for a wider range of scenarios than ever before. Bento APIs encourage eliminating literally all uses of the `new` keyword and replacing them with dependency injection, although probably few programmers will go that far.

### Quick Start

The only public-facing class of the bento library - is `Bento`. It represents a map which
stores all the dependencies of a single context.

A context represents a configuration for anything that could conceivably require configuring; for example:

- the entire application (root configuration),
- a single server request,
- a single level in a platform game,
- a single enemy in the level.

It is perfectly possible to use only one `Bento` instance, but the intention is to use a tree of `Bento`s inheriting from their parents and overriding or adding new values. For example, the root Bento in a web shop can be configured with default value for the currency and the payment provider, but each user session can have separate Bento, overriding those values.

Once we have a Bento instance, we can assign values to keys with the `register` method.

```jshelllanguage
Bento bento = Bento.createRoot();

// configure the bento
bento.register("width", 800);
bento.register("height", 600);
```

We can also create child bento, and override or add new keys:

```jshelllanguage
Bento bento = Bento.createRoot();

// ... 
    
Bento child = bento.createChild();
child.register("width", 640);
child.register("test", true);
```

Any value registered in a Bento can be retrieved by one of Bento's typed `getXXX` methods, which attempt to convert the stored value to the type denoted by `XXX`. For example, `bento.getInt("width")` will return an `int` of `800`, but `bento.getFloat("width")` returns a float for the same config key.

One of the possible conversions is creating enum instances. Assuming we have an enum with the names of colors, we can register a color as a string and retrieve it as the enum:

```jshelllanguage 
enum Color {
    RED, GREEN, BLUE;
}

Bento bento = Bento.createRoot();
bento.register("color", "RED");
Color color = bento.getEnum(Color.class, "color");
```

This string-friendliness makes it easy to connect Bento contexts to configuration files. Bento comes with its own implementation of the ini file parser, allowing us to create a Bento object containing all the configuration values in one go. We could create a file:

```properties
# this is an enum:
color = BLUE

# screen size:
width=800
height = 600
```

And use it directly:

```jshelllanguage

Bento bento = PropertiesLoader.loadNew(new FileReader("config.properties"));

Color color = bento.getEnum(Color.class, "color");
int width = bento.getInt("width");
int height = bento.getInt("height");
```

Of course, the point of DI is never to use the getXXX methods directly. Let's imagine we have a class which has all the above values injected:

```jshelllanguage
class Example {
    private final Color color;
    private final int width;
    private final int height;

	public Example(Color color, int width, int height) {
		// ...
	}
}
```

Adding a `@WithFactory`annotation above the constructor will trigger the Bento Annotation Processor to generate a singleton with the following factory method:

```java
public enum ExampleFactory implements BentoFactory<Example> {
    IT;

    @Override
    Example createInContext(Bento bento) {
        return new Example(
            bento.getEnum(Color.class, "color"),
            bento.getInt("width"),
            bento.getInt("height")
        );
    }
}
```

This happens at build-time; no reflection is used, so this solution is both performant and GWT/TeaVM/GraalVM friendly.

We can now create instances of the `Example` class by doing the following:

```jshelllanguage
Bento bento = PropertiesLoader.load("config.properties");
Example e = ExampleFactory.IT.createInContext(bento);
```

The approach above already has plenty of advantages - to add new configurable values to the `Example` class we only need to add new  arguments to its constructor; the processor will regenerate the factory, and the new keys in the property file will instantly work.

But there's more. `Bento` implements special behavior for retrieving values for keys implementing the BentoFactory<T> - both generated and hand-writte. If the key already has a value associated - the value is simply returned. Otherwise - `Bento` will use the provided factory to generate the value and register it for future use.

In the simplest sense, this means we can save some typing:

```jshelllanguage
Bento bento = PropertiesLoader.load("config.properties");
Example e = bento.get(ExampleFactory.IT);
```

However, the crux of this approach is that it allows for cascading dependencies.

Below is another example class; this one uses both simple values and complex objects as dependencies:

```java
class AnotherExample {
    @WithFactory
    public AnotherExample(int width, Example example) {
        // ...
    }
}
```

The generated factory will use the ExampleFactory to retrieve an Example object:

```java

public enum AnotherExampleFactory implements BentoFactory<AnotherExample> {
    IT;

    @Override
    AnotherExample createInContext(Bento bento) {
        return new AnotherExample(
            bento.getInt("width"),
            bento.get(ExampleFactory.IT)
        );
    }
}

```

As we can imagine, The `bento.get` method will be called recursively to construct the whole tree of dependencies required by the top-level class. However, each object type will be constructed only once - since each resolved dependency is immediately stored within the context.

In a typical application using Bento - all the objects are instantiated in one go as dependencies of a single top-level object:

```jshelllanguage

// this is almost any Bento application
Bento bento = PropertiesLoader.load("config.properties");
Application app = bento.get(ApplicationFactory.IT);
app.run();
```

### Multiple instances of a single type

When writing a bullet-hell game, we could want to create each bullet using DI - so that each bullet can depend on configuration values for speed, color, texture, behavior etc. However, by default, Bento stores any object acquired with `get` and does not create it again.

The straightforward solution, not idiomatic, is to use the factory directly:

```jshelllanguage
Bullet bullet = BulletFactory.IT.createInContext(bento);
```

The object creating bullets needs an instance of Bento - fortunately, that is simple enough; bento can be declared as a constructor argument and used as a dependency, like any other class:

```java
public class Alien {
    private final Bento bento;

    @WithFactory
    public Alien(float x, float y, Bento bento) {
        // ...
    }

	private void fire() {
		Bullet bullet = BulletFactory.IT.createInContext(bento);
		bullet.setX(x);
		bullet.setY(y);
		// ...
	}
}

```

The approach above works fine in simple cases, but it has two problems:
the code uses setters, even though initial x and y are clearly the dependencies of the Bullet;
the code bypasses the Bento's dependency cascading - so it will break as soon as the `Bullet` object depends an instance of another class which needs to be instantiated on a per-bullet basis. Only one instance of such class will be created and it will be subsequently shared by all bullets.

The more general solution is to create a child `Bento` responsible for the `Bullet` context. Any dependency of a `Bento` that has a parent defined is first looked up in the parent; only when it's missing - it is created and stored in the child context.

I will assume that the Bullet class looks somewhat like this:
```java
    public class Bullet {
        @WithFactory
        public Bullet(float initialX, float initialY) {
            // ..
        }
    }
```
The long and explicit solution using a child bento is:

```java
public class Alien {
    private final Bento bento;
    
    @WithFactory
    public Alien(float x, float y, Bento bento) {
        // ...
    }

	private fire() {
		Bento bulletBento = bento.createChild();
		bulletBento.register("initialX", x);
		bulletBento.register("initialY", y);
		Bullet bullet = bulletBento.get(BulletFactory.IT);
		// ...
	}
}

```

The most idiomatic version which we will see in a second - does exactly what the above version does, but is shorter and easier to maintain.
We can create an interface for the functionality of creating a child Bento, configuring it with arbitrary values, and creating a new object - and have the annotation processor generate the repetitive code behind it:

```java
@BentoWrapper
interface BulletSpawner {
    Bullet spawn(float initialX, float initialY);
}
```

Bento Annotation Processor will create both an implementation, and a factory of that implementation, so that the final version of the alien class will be simply:

```java

public class Alien {
    private final BulletSpawner bulletSpawner;
    @WithFactory
    public Alien(float x, float y, BulletSpawner bulletSpawner) {
        // ...
        this.bulletSpawner = bulletSpawner;
    }

	private fire() {
		Bullet bullet = bulletSpawner(x, y);
		// ...
	}
}


```
The code might look quite magical, but the additionally generated code is as straightforward as possible:

```java

public class BulletSpawnerImpl {
private final Bento bento;

	@WithFactory
	public BulletSpawnerImpl(Bento bento) {
		this.bento = bento;
	}

	@Override
	Bullet spawn(float initialX, float initialY) {
		Bento scope = bento.createChild();
		scope.register("initialX", initialX);
		scope.register("initialY", initialY);
		return scope.get(BulletFactory.IT);
	}
}

```

And a factory making it the default implementation of the interface:

```java
public enum BulletSpawner implements BentoFactory<BulletSpawner> {
    IT;
    public BulletSpawner createInContext(Bento bento) {
        return bento.get(BulletSpawnerImplFactory.IT);
    }
}

```

Note that in the above code the `BulletSpawnerImplFactory` does not need to be generated explicitly, it is generated as the consequence of the `@WithFactory` annotation in the first generated class.

### Prefixed keys
[this section is a stub]

### Categories
[this section is a stub]

### Switching implementations
[this section is a stub]

### Generic classes
[this section is a stub]

### Resetting objects at runtime
[this section is a stub]

### Switching implementations
[this section is a stub]
