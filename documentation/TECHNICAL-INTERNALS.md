# Technical Internals

This document explains how **tavnit** processes your source files internally and give some technical details.

> If you just want to use tavnit and are not interested in the technical insights, you can skip this page.

## Dependencies

tavnit is a pure Kotlin library with no external runtime dependencies beyond kotlin-stdlib. 

The public API (_tavnit-api_) is cleanly separated from the implementation (_tavnit_) and the 
two are connected at runtime through Java's standard ServiceLoader mechanism
(see ``META-INF/services/`` in the _tavnit_ module). 

This means your code compiles against (and sees only) a stable, minimal API surface while the implementation 
details remain fully encapsulated.

## How a source file is decomposed internally

To understand how commands behave, it helps to see how tavnit *reads* a file.

Internally, a source file is **tokenized into a flat chain of two kinds of parts**:

- **Plain text** — everything that is *not* a tavnit command block. 
  
  Crucially, this includes ordinary comments. A normal `<!-- a real comment -->` that does **not** contain the
  magic `@tt{{{ ... }}}@` brackets is treated as plain text like any other character in the
  file.

- **Template comments** — the comment blocks that *do* contain the magic brackets
  `@tt{{{ ... }}}@`. Only the part **inside** the brackets is interpreted as commands.

### Visualizing the chain

Take this small HTML source fragment:

```html
<ul><!-- @tt{{{ @foreach [iteratorExpression="model.items" loopVariable="item"] }}}@ -->
  <li>An item</li><!-- @tt{{{ @end-foreach }}}@ -->
</ul>
```

tavnit tokenizes it into an alternating chain of plain-text and template-comment
parts:

```
┌─────────────┐  ┌───────────────────────────┐  ┌──────────────────┐  ┌─────────────────┐  ┌──────────┐
│ PLAIN TEXT  │→ │ TEMPLATE COMMENT          │→ │ PLAIN TEXT       │→ │ TEMPLATE COMMENT│→ │ PLAIN    │
│ "<ul>"      │  │ @foreach [iterator…]      │  │ "\n  <li>An      │  │ @end-foreach    │  │ TEXT     │
│             │  │                           │  │  item</li>"      │  │                 │  │ "\n</ul>"│
└─────────────┘  └───────────────────────────┘  └──────────────────┘  └─────────────────┘  └──────────┘
   (kept as         (an instruction —             (kept as output,        (an instruction —    (kept as
    output)          NOT output)                   may be transformed)      NOT output)          output)
```

The key mental model:

- **Plain-text parts are the material of the output.** They are what the generated renderer
  prints (possibly transformed — a value replaced, a region repeated, a region omitted).
- **Template-comment parts are instructions.** They are never printed. They tell the renderer
  *how* to treat the plain-text parts around them, and they are deleted from the final output.

A file that contains **no** template comments at all is ignored entirely — tavnit
generates nothing for it.

### How the chain is processed

The chain runs through a fixed pipeline (see `ContentParser`):

1. **Tokenize** the raw file into the plain-text / template-comment chain shown above.
2. **Resolve** each template-comment part into its individual commands and attributes.
3. **Preprocess** — validate mutually-exclusive commands, then apply comment-moving
   (`@move-comment-backward` / `@move-comment-forward`) and comment-expansion adjustments
   (`@remove-blanks-before-comment`, `@remove-blanks-after-comment`, 
   `@remove-blanks-and-linebreak-before-comment`, `@remove-blanks-and-linebreak-after-comment`,
   `@no-default-whitespace-remove`, see [WHITESPACE-HANDLING.md](WHITESPACE-HANDLING.md))
4. **Resolve the nesting structure** — match opening and closing commands, insert auto-close
   commands, and validate that everything is properly nested (see [NESTING-AND-SCOPE.md](NESTING-AND-SCOPE.md)).
5. **Custom validation** of the now well-formed command chain.
6. **Split into renderer descriptions** — one per `@template-renderer` — and generate the
   Kotlin renderer class(es).

## Limitations and behavior notes

A few consequences of the tokenization model above are worth knowing:

- **Command markers are matched textually, not lexically.** tavnit locates `@tt{{{ ... }}}@`
  blocks by scanning for the configured comment delimiters; it does **not** parse the source
  language. If the marker sequence appears inside a *string literal* of the source file — for
  example `val s = "/* @tt{{{ @remark [text=\"x\"] }}}@ */"` in a Kotlin template — tavnit
  treats it as a real command and removes it from the output. Keep the `@tt{{{ ... }}}@` marker
  out of string literals in your template files. (A marker that is opened but never closed with
  `}}}@` is detected and reported as an error rather than silently leaking into the output.)

- **Generated output uses `\n` line endings.** Regardless of whether your source file uses
  LF or CRLF (`\r\n`) line endings, the multiline string in the generated renderer is normalized
  to `\n`. The generated Kotlin source is itself LF-based, and the produced output string will
  therefore contain `\n` line breaks. If your consumer needs CRLF output, convert the rendered
  string in your own code.

## Test it yourself

A full, runnable example project can be found in the Gradle subproject [tavnit-blackbox-tests](tavnit-blackbox-tests)
