# Nesting, scope, and closing commands

Most [commands](COMMAND-REFERENCE.md) do not act on a single point in the file — they act on a **region** of the chain.
That region is the command's **scope** (its area of effect): everything from the command's
opening up to its matching close.

### Scope = "the plain text I influence"

Consider `@foreach`. It does not just mark a spot; it says *"repeat everything between here and
my close foreach element of the collection"*. The plain text inside that region is its scope.
The same is true for `@if` (render the scope only when the condition holds),
`@replace-value-by-expression` (apply the replacement to all plain text in the scope),
`@ignore-text` (drop the scope from the output), and `@template-renderer` (the scope is the
body of the generated renderer class).

```
<!-- @tt{{{ @foreach [...] }}}@ -->
         ┌──────────────────────────────────┐
         │  ...plain text...                │   ◄── scope of @foreach
         │  ...more plain text...           │       (repeated once per element)
         └──────────────────────────────────┘
<!-- @tt{{{ @end-foreach }}}@ -->
```

### Closing a command

A scope-opening command is closed in one of two ways:

1. **Explicitly**, with its `@end-…` command (e.g. `@end-foreach`, `@end-if`,
   `@end-template-renderer`).
2. **By auto-close**, for commands that support it. `@template-renderer`,
   `@replace-value-by-expression`, `@replace-value-by-value`, and `@ignore-text` may have their
   `@end-…` omitted; tavnit closes them automatically. Auto-closing is triggered when
   an enclosing scope closes, or at the end of the file. (`@if` and `@foreach` do **not**
   support auto-close — they must be closed explicitly.)

Some commands are **self-contained** and open no scope at all: 
`@print-text`, `@remark`, `@render-template`, `@add-import-to-renderer`, the `move-comment-*`
commands, and the whitespace `remove-*` commands. They act at their own position and need no
closing command.

### Nesting and how scopes stack

Commands nest like brackets: a scope opened later must be closed before a scope opened earlier.
tavnit tracks open scopes on a stack. When a closing command appears, it must match
the **most recently opened** scope, otherwise parsing fails with a clear error.

```
<!-- @tt{{{ @if [conditionExpression="model.showList"] }}}@ -->     ┐
  <ul><!-- @tt{{{ @foreach [...] }}}@ -->                           │ ┐
    <li><!-- @tt{{{ @replace-value-by-expression [...] }}}@ -->X    │ │ ┐
        </li><!-- @tt{{{ @end-replace-value-by-expression }}}@ -->  │ │ ┘  scope: replace-value-by-expression
  </ul><!-- @tt{{{ @end-foreach }}}@ -->                            │ ┘    scope: foreach
<!-- @tt{{{ @end-if }}}@ -->                                        ┘      scope: if
```

Reading the area of effect:

- The **`@if` scope** spans the whole block — nothing inside it is rendered unless
  `model.showList` is true.
- The **`@foreach` scope** lives *inside* the `@if` scope — it repeats the `<li>` region for
  every element, but only when the surrounding `@if` already decided to render.
- The **`@replace-value-by-expression` scope** is the innermost — its value replacement applies
  only to the `<li>` text it encloses.

Each inner scope is fully contained in its parent. A command's effect reaches exactly the plain
text inside its own scope and the scopes nested within it — never the plain text of a sibling
or an ancestor outside its range.

### Auto-close in action

Because closing commands also **trigger auto-close of any auto-closeable scopes still open
inside them**, you can often omit the inner `@end-…`. For example:

```
<!-- @tt{{{ 
            @foreach [...] 
            @replace-value-by-expression [...] 
}}}@ -->
  ...content...
<!-- @tt{{{ @end-foreach }}}@ -->     ◄── closes @foreach AND auto-closes the still-open
                                          @replace-value-by-expression inside it
```

Here `@end-foreach` closes the `@foreach` scope and, on the way, auto-closes the
`@replace-value-by-expression` that was opened inside it (because that command supports
auto-closing). Likewise, any auto-closeable scope still open at the **end of the file** is
closed automatically. A scope that does **not** support auto-closing (like `@if` or `@foreach`)
and is left open will instead raise an exception.

### Directly-nested commands

A few commands must sit **directly inside** a specific parent. `@else` and `@else-if` must be
directly nested in an `@if`. tavnit validates this and rejects a misplaced `@else`.

### Nested template renderers are independent

`@template-renderer` is special: you may nest one `@template-renderer` inside another, but a
nested renderer is **fully independent**. Its content is removed from the parent renderer and
placed in its own class, and none of the parent's models, conditions, loops, or replacements
apply to it. Each renderer class stands on its own.
