# InvisibleNMS
Avoiding code duplication by copying whole methods at compile time

InvisibleNMS is aimed at Bukkit/Spigot plugins that use NMS code, but can be used in any kind of java project. The use case is allowing to overwrite methods using default implementations from interfaces, which normally does not work. Thus you can create one interface with an implementation and implement that in several classes.

For example, I want to overwrite the method `setName` in both my `BoatEntity` and `ZombieEntity` class using the same content. The method is define in my `Entity` class. Now I could just go ahead and duplicate the code here, but that'd be boring, so instead, we use this tool to avoid duplication.

So instead you can write the following interface:

```java
@Mixin
interface CustomNameSetter {
    default void setName(String name) {
        Super.call(name + "-entity");
    }
}
```

And use that in my class(es):

```java
@CopyDefaults
public class BoatEntity extends Entity implements CustomNameSetter {
    // ...
}
```

Now, this tool will then _at compile time_ copy the method over so that in the resulting class, the code will look like this:

```java
public class BoatEntity extends Entity implements CustomNameSetter {
    public void setName(String name) {
        super.setName(name + "-entity");
    }
}
```

The reason we use `Super.call()` here instead of the real `super` is because `super` does not work in interfaces, since there might not be a super implementation. Calling `Super.call` doesn't do anything and is only there so we can identify where the to place the real super call.

## Usage

This tool behaves similar to any other annotation processing tool. For example in gradle, you can add it to your dependencies like this:
```groovy
dependencies {
    annotationProcessor 'de.eternalwings.bukkit:invisiblenms:1.0-SNAPSHOT'
    // ...
}
```

This will also add the necessary annotations to your classpath. Now as soon as you compile your project, it'll already mixin those methods.
