# Tavnit Quickstart

Generate your first Kotlin renderer in a few minutes. For the concept and the full picture see the
[README](README.md).

## 1. Add the dependencies

Create a new gradle project or add a new subproject in your existing gradle build.
Both artifacts are required — the implementation is discovered at runtime via `ServiceLoader`.

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("org.codeblessing.tavnit:tavnit-api:1.0.0")
    runtimeOnly("org.codeblessing.tavnit:tavnit:1.0.0")
}
```

## 2. Annotate a real source file

Write tavnit commands inside ordinary source-code comments. The file stays valid in its native
tooling. Example `src/main/resources/greeting.html`:

```html
<!-- @tt{{{
  @template-renderer
     [ templateRendererClassName="GreetingRenderer" templateRendererPackageName="com.example.render" ]
     [ modelName="user" modelClassName="User" modelPackageName="com.example.model" ]
}}}@ -->
<p>Hello, <!-- @tt{{{ @replace-value-by-expression [ searchValue="NAME" replaceByExpression="user.name" ] }}}@ -->NAME<!-- @tt{{{ @end-replace-value-by-expression }}}@ -->!</p>
```

## 3. Run tavnit

Add a `JavaExec` task that invokes the tavnit main class:

```kotlin
tasks.register<JavaExec>("generateTavnitRenderers") {
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("org.codeblessing.tavnit.TavnitKt")
    args(
        "--template-renderer", "$projectDir/src/generated/kotlin",
        // <path>:<glob> — the CLI uses a filename glob (not a regex)
        "--search", "$projectDir/src/main/resources:*.html",
    )
}
```

```
./gradlew generateTavnitRenderers
```

This writes `GreetingRenderer.kt` under `src/generated/kotlin/com/example/render/`.

## 4. Use the generated renderer

Build your model and call the renderer from your own code:

```kotlin
val html: String = GreetingRenderer.renderTemplate(User(name = "Ada"))
```

Whenever the source file changes, re-run the task and the renderer is regenerated — the source
file is the single source of truth.

## Next steps

- An overview over tavnit: [README](README.md)
- The complete list of commands with examples: [COMMAND-REFERENCE.md](documentation/COMMAND-REFERENCE.md)
- Running tavnit from the command line: [MAIN-FUNCTION-USAGE.md](documentation/MAIN-FUNCTION-USAGE.md)
- A full, runnable example project: [tavnit-blackbox-tests](tavnit-blackbox-tests)
