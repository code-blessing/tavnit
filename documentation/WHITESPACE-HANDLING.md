# Whitespace Handling - How template comments disappear from the output

Template comments are instructions, not content — so the generated renderer must never print
them. tavnit guarantees this in two steps.

## Step 1 — the comment itself is always removed

The full comment carrier, including the host language's comment delimiters and the magic
brackets, is always dropped:

```
<!-- @tt{{{ @foreach [...] }}}@ -->          ◄── the entire comment is removed from the output
```

What the comment *does* (open a scope, replace a value, …) is applied to the surrounding plain
text; what the comment *looks like* never appears.

> Note: this only applies to comments that actually contain `@tt{{{ ... }}}@`. A plain comment
> with no tavnit instructions is just plain text and is reproduced verbatim in the
> output.

## Step 2 — the whitespace around the comment is cleaned up

If only the comment text vanished but its surrounding indentation and line break stayed, the
output would be littered with blank, indented lines and stray spaces. So tavnit also
trims the whitespace **directly around** each comment, looking only at the text on the
comment's own line — what comes before it (up to the start of the line) and after it (up to the
end of the line).

> Legend for the visualizations:
> 
> - `·` = a single space/tab
> 
> - `⏎` = a line break,
> - `<!-- @tt{{{ ... }}}@ -->` = the template comment.
> -  Parts marked with `╳` are removed, the rest is kept. The template comment itself always disappears. 


### What is looked at

The comment itself (```<!-- @tt{{{ ... }}}@ -->```) is always removed in the output.
The decision considers **only** the text *directly before* the comment (up to
the start of its line) and *directly after* the comment (up to the end of its
line):

```
... preceding text⏎·····<!-- @tt{{{ ... }}}@ -->····⏎ following text ...
                   └─┬─┘└──────comment────────┘└─┬─┘
              before the comment           after the comment
```

## Default whitespace removing behavior


There are four cases, depending on whether the text before and after the comment
(on the same line) is only whitespace (spaces and tabs) or also contains real text:

### Case 1 — text before, only whitespace after (trailing comment)

Everything before the comment is kept, except the blanks between the last
non-blank and the comment, which are removed; the blanks after it are removed as
well, but the line break is kept.

```
END</li>····<!-- @tt{{{ ... }}}@ -->····⏎     -->   END</li>⏎
        ╳╳╳╳^^^^^^^^^^^^^^^^^^^^^^^╳╳╳╳
```

### Case 2 — only whitespace before, text after (leading comment)

Nothing is removed  (apart from the comment itself) — the indentation and the following text are preserved.

```
····<!-- @tt{{{ ... }}}@ -->START</li>     -->   ····START</li>
    ^^^^^^^^^^^^^^^^^^^^^^^
```

### Case 3 — only whitespace before *and* after (comment alone on its line)

The whole comment line is removed: the blanks before it are removed (the
preceding line break is kept), and the blanks together with the following line
break are removed as well.

```
prevText⏎····<!-- @tt{{{ ... }}}@ -->····⏎nextText     -->   prevText⏎nextText
         ╳╳╳╳^^^^^^^^^^^^^^^^^^^^^^^╳╳╳╳╳
```

### Case 4 — text before *and* after

Nothing is removed (apart from the comment itself).

```
A····<!-- @tt{{{ ... }}}@ -->····B     -->   A····B
     ^^^^^^^^^^^^^^^^^^^^^^^
```

## Why this behavior?

- **So you don't constantly end up with doubled blank lines or wrong
  indentation.** A comment standing alone on its line (case 3) would otherwise
  leave an empty, indented line behind in the output.
- **Because comments in the source file usually have to be indented** — whether
  for readability or because of the linter/editor. That indentation belongs to
  the source, not the output.
- **When there is only whitespace before/after the comment, probably none of it
  needs to be kept** (it only served to position the comment). If there is real
  text next to it instead (cases 1, 2, 4), the indentation is likely
  intentional and is preserved.
- **Only a single comment is ever considered on its own**, never in combination
  with neighboring comments — this keeps the behavior local and predictable.

## Overriding the default behavior

When the default behavior isn't enough, it can be overridden per side.

|                                          | Before the comment                                                                                            | After the comment                                                                                            |
|------------------------------------------|---------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------|
| Remove only the **blanks**               | [remove-blanks-before-comment](COMMAND-REFERENCE.md#remove-blanks-before-comment)                             | [remove-blanks-after-comment](COMMAND-REFERENCE.md#remove-blanks-after-comment)                              |
| Remove the **blanks and the line break** | [remove-blanks-and-linebreak-before-comment](COMMAND-REFERENCE.md#remove-blanks-and-linebreak-before-comment) | [remove-blanks-and-linebreak-after-comment](COMMAND-REFERENCE.md#remove-blanks-and-linebreak-after-comment)  |


```
preceding text⏎····<!-- @tt{{{ ... }}}@ -->····⏎following text
              ├───┤                       ├───┤
              │                           └ remove-blanks-after-comment ────────────────► ····  removed
              │                             remove-blanks-and-linebreak-after-comment ──► ····⏎ removed
              │
              └ remove-blanks-before-comment ───────────────────────────────────────────► ····  removed
                remove-blanks-and-linebreak-before-comment ─────────────────────────────► ⏎···· removed
```

Rules:

- A `remove-*` command overrides the default decision **for its own side
  only** and leaves the other side untouched.
- `no-default-whitespace-remove` switches the default behavior off for **both**
  sides; then only the comment itself disappears. Explicit `remove-*` commands
  still take effect (and override the now-disabled default for their side).
- A command that asks for exactly what the default would already do is a no-op.
  Conflicting commands on the same side are rejected by the validator.

The individual commands including their shorter aliases are documented in
[COMMAND-REFERENCE.md](./COMMAND-REFERENCE.md).
