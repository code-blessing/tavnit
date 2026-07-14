# tavnit blackbox tests

A full, runnable end-to-end example of tavnit, wired up the way a real project would use it. It
doubles as the black-box test suite: real annotated source files are turned into renderer classes,
the renderers are compiled and executed, and their output is compared against expected files.

## Subprojects

| Subproject | Role |
|------------|------|
| [`example-business-project`](example-business-project) | The "user" project. Contains real HTML/Kotlin source files annotated with `@tt{{{ ... }}}@` commands under `src/main`. This is the input to tavnit. |
| [`template-renderer-creator`](template-renderer-creator) | Runs tavnit. Defines the `createTavnitRenderers` task (a `JavaExec` invoking `org.codeblessing.tavnit.TavnitKt`) that reads the annotated files and writes renderer classes into `template-renderer-executor`. |
| [`template-renderer-executor`](template-renderer-executor) | Holds the generated renderer classes (under `src/tavnit-generated/kotlin`) and compiles them, proving the generated Kotlin is valid. |
| [`blackbox-tests`](blackbox-tests) | Executes the generated renderers and asserts their output matches the `*.expectation.*` files. |

## Running it

Regenerate the renderer classes from the annotated sources:

```
./gradlew :tavnit-blackbox-tests:template-renderer-creator:createTavnitRenderers
```

Run the black-box tests (this also regenerates and compiles the renderers as needed):

```
./gradlew :tavnit-blackbox-tests:blackbox-tests:test
```

## Note on the ServiceLoader classpath

The `createTavnitRenderers` task puts the tavnit implementation on its classpath as a **packaged
jar** rather than as a loose project dependency. This is deliberate: the `META-INF/services`
registration that `ServiceLoader` relies on must travel atomically with the classes, and a stale
loose resources directory can otherwise cause an intermittent *"Could not find an implementation of
TavnitProcessorApi"* failure. See the comments in
[`template-renderer-creator/build.gradle.kts`](template-renderer-creator/build.gradle.kts).
