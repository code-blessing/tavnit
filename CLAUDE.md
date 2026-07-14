# CLAUDE.md

## Project Overview

**tavnit** is a reverse template engine for Kotlin. It parses real source files (HTML, Kotlin, TypeScript, SCSS) annotated with special comment-based commands (`@tt{{{ ... }}}@`) and generates Kotlin renderer classes that produce output using multiline string templates.

## Guidelines

### Code Quality

- Prefer the most direct, obvious solution — no clever indirection or unnecessary abstraction
- High-level methods read as a sequence of well-named steps; detail lives in focused helpers

### Temporary Files

- Create all temporary files in [.tmp](./.tmp)

## Terminology

- Template commands are documented in `COMMAND-REFERENCE.md` (auto-generated).

## Key Conventions

- The API and implementation are decoupled via `ServiceLoader` (see `META-INF/services/` in the tavnit module).
- No external runtime dependencies beyond Kotlin stdlib — pure Kotlin implementation.

