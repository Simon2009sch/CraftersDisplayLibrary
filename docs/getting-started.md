# Getting Started

## Adding the dependency

CraftersDisplayLibrary is a plain library, not a plugin — you shade it directly into your own plugin's jar
(the same way you'd bundle any other utility library), rather than depending on it as an external plugin
loaded by the server. It's published through [JitPack](https://jitpack.io).

### Maven

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.Simon2009sch</groupId>
        <artifactId>CraftersDisplayLibrary</artifactId>
        <version>master-SNAPSHOT</version>
    </dependency>
</dependencies>
```

Replace `master-SNAPSHOT` with a tagged release once one exists (JitPack builds any branch, commit, or tag —
see the [JitPack docs](https://jitpack.io/docs/) for the version string format).

You'll also need to actually bundle the library's classes into your plugin's jar, since Paper won't resolve
it at runtime otherwise. With the Shade plugin:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-shade-plugin</artifactId>
            <version>3.5.3</version>
            <executions>
                <execution>
                    <phase>package</phase>
                    <goals><goal>shade</goal></goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

### Gradle (Kotlin DSL)

```kotlin
repositories {
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.Simon2009sch:CraftersDisplayLibrary:master-SNAPSHOT")
}
```

Shade it with the [Shadow plugin](https://gradleup.com/shadow/) so it ends up inside your plugin's jar.

## Required setup

Every consumer **must** call `PluginHolder.setPlugin(...)` once, in `onEnable()`, before touching any other
part of the library. Internally, the library needs a `JavaPlugin` instance to schedule Bukkit tasks (all
animation and highlight/tint effects run on repeating `BukkitTask`s) and to build a `NamespacedKey` used to
tag the entities it spawns.

```java
import me.simoncrafter.CraftersDisplayLibrary.PluginHolder;
import org.bukkit.plugin.java.JavaPlugin;

public final class MyPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        PluginHolder.setPlugin(this);
    }
}
```

> [!WARNING]
> Do this first, before any other CraftersDisplayLibrary code runs. In particular, `Tags.CDL_ENTITY` is
> initialized the moment the `Tags` class is loaded, using whatever plugin `PluginHolder.getPlugin()` returns
> at that moment — if that's still `null`, entity spawning will fail with a `NullPointerException` coming
> from deep inside Bukkit's `NamespacedKey` constructor. Calling `setPlugin` first avoids this entirely.

## Your first display

A minimal example that spawns a solid white cube at a player's location:

```java
import me.simoncrafter.CraftersDisplayLibrary.def.active.Cube.CubeColorDisplay;
import me.simoncrafter.CraftersDisplayLibrary.def.active.Cube.CubeColorInformation;
import org.bukkit.Color;
import org.bukkit.Location;
import org.joml.Quaternionf;
import org.joml.Vector3f;

Location loc = player.getLocation();

CubeColorDisplay cube = CubeColorDisplay.create(
        loc,
        new Vector3f(1, 1, 1),           // scale
        new Vector3f(0, 0, 0),           // translation, relative to loc
        new Quaternionf(),               // rotation
        new CubeColorInformation(Color.WHITE),
        false                            // see-through
);

cube.spawnDisplay();
```

Move it one block up over 20 ticks (1 second):

```java
cube.moveRelative(new Vector3f(0, 1, 0), 20);
```

Remove it when you're done:

```java
cube.remove();
```

That's the whole lifecycle: `create(...)` builds an unspawned, purely in-memory description of the display;
`spawnDisplay()` creates the backing Bukkit entities; the `move*`/`*Rotate*`/`scale*` methods (inherited from
`PositionObject`, see [Core Concepts](core-concepts.md)) mutate and re-render it, optionally animated over a
tick duration; `remove()` despawns everything.

Continue to [Core Concepts](core-concepts.md) to understand the transform tree these methods operate on, or
jump straight to [Displays](displays.md) for a tour of every display type.
