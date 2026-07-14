# Supported file formats

tavnit recognizes the template commands (```@tt{{{ ... }}}@```) inside the comments of the
processed source files. Which comment syntax is understood depends on the file extension.

The following table lists every file extension that is configured out of the box together with the
comment formats that are supported for it. A file format can support several comment formats; in that
case any of them can be used to host the template commands.

Supported comment styles are defined in a classpath resource file 
[tavnit-config.properties](tavnit/src/main/resources/tavnit-config.properties)
and can be extended by providing an individual
classpath resource `/tavnit-config-overwrite.properties`.

### Configuring your own comment styles and file extensions

The overwrite file uses the same property format as the built-in configuration. Declare one or
more named comment styles and then map file extensions to them:

```properties
# A line comment style: give it a unique id (here "lua") and its start delimiter.
# commentType is either LINE_COMMENT or BLOCK_COMMENT.
commentStyle.lua.startOfComment=--
commentStyle.lua.commentType=LINE_COMMENT

# A block comment style additionally needs an end delimiter.
commentStyle.luaBlock.startOfComment=--[[
commentStyle.luaBlock.endOfComment=]]
commentStyle.luaBlock.commentType=BLOCK_COMMENT

# Map a file extension (without the leading dot) to one or more comment style ids (comma-separated).
extension.lua=lua,luaBlock
```

An entry in the overwrite file replaces the entry with the same key in the built-in
configuration, so you can also redefine the comment styles of an already-supported extension.


| File Extension | Supported Comment Format |
| --- | --- |
| `.4th` | `:` |
| `.R` | `#` |
| `.abap` | `*` and `"` |
| `.ada` | `--` |
| `.adb` | `--` |
| `.ads` | `--` |
| `.agda` | `--` and `{- -}` |
| `.ahk` | `;` and `/* */` |
| `.ahkl` | `;` and `/* */` |
| `.als` | `--` and `//` and `/* */` |
| `.apl` | `⍝` |
| `.as` | `//` and `/* */` |
| `.asm` | `;` and `#` and `|` and `/* */` |
| `.asm68k` | `|` |
| `.astro` | `<!-- -->` and `//` and `/* */` |
| `.au3` | `;` |
| `.awk` | `#` |
| `.bal` | `//` |
| `.bas` | `'` |
| `.bash` | `#` |
| `.bat` | `REM` and `::` |
| `.bib` | `%` |
| `.blade.php` | `{{-- --}}` |
| `.brs` | `'` |
| `.bst` | `%` |
| `.bzl` | `#` |
| `.c` | `//` and `/* */` |
| `.carbon` | `//` |
| `.cbl` | `*` and `*>` |
| `.cc` | `//` and `/* */` |
| `.cfg` | `#` |
| `.chpl` | `//` and `/* */` |
| `.cjs` | `//` and `/* */` |
| `.cl` | `//` and `/* */` |
| `.clj` | `;` |
| `.cljs` | `;` |
| `.cls` | `%` |
| `.cmake` | `#` |
| `.cmd` | `REM` and `::` |
| `.cob` | `*` and `*>` |
| `.coffee` | `#` and `### ###` |
| `.comp` | `//` and `/* */` |
| `.conf` | `#` |
| `.coq` | `(* *)` |
| `.cpp` | `//` and `/* */` |
| `.cpy` | `*` and `*>` |
| `.cql` | `//` |
| `.cr` | `#` |
| `.cs` | `//` and `/* */` |
| `.csh` | `#` |
| `.css` | `/* */` |
| `.cts` | `//` and `/* */` |
| `.cu` | `//` and `/* */` |
| `.cue` | `//` |
| `.cuh` | `//` and `/* */` |
| `.cxx` | `//` and `/* */` |
| `.cypher` | `//` |
| `.d` | `//` and `/* */` and `/+ +/` |
| `.dart` | `//` and `/* */` |
| `.def` | `(* *)` |
| `.dhall` | `--` and `{- -}` |
| `.di` | `//` and `/* */` and `/+ +/` |
| `.dockerfile` | `#` |
| `.dpr` | `//` and `{ }` and `(* *)` |
| `.dtx` | `%` |
| `.dyalog` | `⍝` |
| `.e` | `--` |
| `.edn` | `;` |
| `.el` | `;` |
| `.elc` | `;` |
| `.elm` | `--` and `{- -}` |
| `.envrc` | `#` |
| `.eps` | `%` |
| `.erb` | `<%# %>` |
| `.erl` | `%` |
| `.ex` | `#` |
| `.exs` | `#` |
| `.f` | `C` |
| `.f03` | `!` |
| `.f08` | `!` |
| `.f18` | `!` |
| `.f77` | `*` |
| `.f90` | `!` |
| `.f95` | `!` |
| `.fish` | `#` |
| `.fnl` | `;` |
| `.for` | `*` |
| `.forth` | `:` |
| `.frag` | `//` and `/* */` |
| `.fs` | `(* *)` and `//` and `/* */` |
| `.fsi` | `(* *)` and `//` and `/* */` |
| `.fsx` | `(* *)` and `//` and `/* */` |
| `.fth` | `:` |
| `.ftl` | `<#-- -->` |
| `.ftlh` | `<#-- -->` |
| `.ftlx` | `<#-- -->` |
| `.fx` | `//` and `/* */` |
| `.fxh` | `//` and `/* */` |
| `.g` | `//` and `/* */` |
| `.g4` | `//` and `/* */` |
| `.gd` | `#` |
| `.gdb` | `#` |
| `.gemspec` | `#` and `=begin =end` |
| `.geom` | `//` and `/* */` |
| `.glsl` | `//` and `/* */` |
| `.go` | `//` and `/* */` |
| `.gql` | `#` |
| `.gradle` | `//` and `/* */` |
| `.graphql` | `#` |
| `.groovy` | `//` and `/* */` |
| `.gst` | `" "` |
| `.h` | `//` and `/* */` |
| `.hack` | `//` and `#` and `/* */` |
| `.haml` | `-#` |
| `.handlebars` | `{{!-- --}}` and `{{! }}` |
| `.haskell` | `--` and `{- -}` |
| `.hbs` | `{{!-- --}}` and `{{! }}` |
| `.hcl` | `#` and `//` and `/* */` |
| `.hh` | `//` and `#` and `/* */` |
| `.hlsl` | `//` and `/* */` |
| `.hpp` | `//` and `/* */` |
| `.hrl` | `%` |
| `.hs` | `--` and `{- -}` |
| `.htm` | `<!-- -->` |
| `.html` | `<!-- -->` |
| `.hx` | `//` and `/* */` |
| `.idr` | `--` and `{- -}` |
| `.ini` | `;` and `#` |
| `.ino` | `//` and `/* */` |
| `.ins` | `%` |
| `.j2` | `{# #}` |
| `.jade` | `//` |
| `.janet` | `#` |
| `.java` | `//` and `/* */` and `///` |
| `.jdn` | `#` |
| `.jinja` | `{# #}` |
| `.jinja2` | `{# #}` |
| `.jl` | `#` and `#= =#` |
| `.js` | `//` and `/* */` |
| `.json5` | `//` and `/* */` |
| `.jsonnet` | `//` and `/* */` and `#` |
| `.jsp` | `<%-- -%>` |
| `.jspx` | `<%-- -%>` |
| `.jsx` | `//` and `/* */` |
| `.julia` | `#` and `#= =#` |
| `.kk` | `//` and `/* */` |
| `.kki` | `//` and `/* */` |
| `.ksh` | `#` |
| `.kt` | `//` and `/* */` |
| `.kts` | `//` and `/* */` |
| `.lagda` | `--` and `{- -}` |
| `.lean` | `--` and `/- -/` |
| `.less` | `//` and `/* */` |
| `.lhs` | `--` and `{- -}` |
| `.libsonnet` | `//` and `/* */` and `#` |
| `.lidr` | `--` and `{- -}` |
| `.lisp` | `;` and `#| |#` |
| `.litcoffee` | `#` and `### ###` |
| `.lpr` | `//` and `{ }` and `(* *)` |
| `.lsp` | `;` and `#| |#` |
| `.lua` | `--` and `--[[ ]]` |
| `.m` | `%` and `%{ %}` |
| `.mak` | `##` |
| `.make` | `#` |
| `.mako` | `##` |
| `.mdx` | `<!-- -->` and `//` and `/* */` |
| `.metal` | `//` and `/* */` |
| `.mjs` | `//` and `/* */` |
| `.mk` | `#` |
| `.ml` | `(* *)` |
| `.mli` | `(* *)` |
| `.mlx` | `%` and `%{ %}` |
| `.mm` | `//` and `/* */` |
| `.mod` | `(* *)` |
| `.mts` | `//` and `/* */` |
| `.mustache` | `{{!-- --}}` and `{{! }}` |
| `.mxml` | `<!-- -->` |
| `.nasm` | `;` |
| `.nim` | `#` and `##` and `#[ ]#` |
| `.nimble` | `#` and `##` and `#[ ]#` |
| `.nims` | `#` and `##` and `#[ ]#` |
| `.ninja` | `#` |
| `.nix` | `#` and `/* */` |
| `.njk` | `{# #}` |
| `.nu` | `#` |
| `.ob` | `(* *)` |
| `.ob2` | `(* *)` |
| `.obn` | `(* *)` |
| `.pas` | `//` and `{ }` and `(* *)` |
| `.php` | `#` and `//` and `/* */` |
| `.pl` | `#` |
| `.pl1` | `/* */` |
| `.pli` | `/* */` |
| `.pm` | `#` |
| `.podspec` | `#` and `=begin =end` |
| `.pony` | `//` and `/* */` |
| `.pp` | `//` and `{ }` and `(* *)` |
| `.pro` | `%` and `/* */` |
| `.prolog` | `%` and `/* */` |
| `.properties` | `#` and `!` |
| `.proto` | `//` and `/* */` |
| `.ps` | `%` |
| `.ps1` | `#` and `<# #>` |
| `.psd1` | `#` and `<# #>` |
| `.psm1` | `#` and `<# #>` |
| `.pug` | `//` |
| `.purs` | `--` and `{- -}` |
| `.pxd` | `#` |
| `.pxi` | `#` |
| `.py` | `#` and `""" """` and `''' '''` |
| `.pyi` | `#` and `""" """` and `''' '''` |
| `.pyw` | `#` and `""" """` and `''' '''` |
| `.pyx` | `#` |
| `.r` | `#` |
| `.rake` | `#` and `=begin =end` |
| `.rb` | `#` and `=begin =end` |
| `.re` | `//` and `/* */` |
| `.rei` | `//` and `/* */` |
| `.res` | `//` and `/* */` |
| `.resi` | `//` and `/* */` |
| `.rex` | `/* */` |
| `.rexx` | `/* */` |
| `.rkt` | `;` and `#| |#` |
| `.rq` | `#` |
| `.rs` | `//` and `/* */` and `///` and `//!` |
| `.s` | `;` and `#` and `/* */` |
| `.sass` | `//` and `/* */` |
| `.scala` | `//` and `/* */` |
| `.scheme` | `;` |
| `.scm` | `;` |
| `.scss` | `//` and `/* */` |
| `.sh` | `#` |
| `.slim` | `#` |
| `.smarty` | `{* *}` |
| `.sparql` | `#` |
| `.sql` | `--` and `/* */` |
| `.ss` | `;` |
| `.st` | `" "` |
| `.star` | `#` |
| `.sty` | `%` |
| `.styl` | `//` and `/* */` |
| `.sv` | `//` and `/* */` |
| `.svelte` | `<!-- -->` and `//` and `/* */` |
| `.svg` | `<!-- -->` |
| `.svh` | `//` and `/* */` |
| `.swift` | `//` and `/* */` |
| `.tcl` | `#` |
| `.tcsh` | `#` |
| `.tesc` | `//` and `/* */` |
| `.tese` | `//` and `/* */` |
| `.tex` | `%` |
| `.tf` | `#` and `//` and `/* */` |
| `.tfvars` | `#` and `//` and `/* */` |
| `.thy` | `(* *)` |
| `.tk` | `#` |
| `.tla` | `\*` and `(* *)` |
| `.toml` | `#` |
| `.tpl` | `{* *}` |
| `.ts` | `//` and `/* */` |
| `.tsx` | `//` and `/* */` |
| `.twig` | `{# #}` |
| `.v` | `//` and `/* */` |
| `.vala` | `//` and `/* */` |
| `.vapi` | `//` and `/* */` |
| `.vb` | `'` |
| `.vbe` | `'` |
| `.vbs` | `'` |
| `.vert` | `//` and `/* */` |
| `.vh` | `//` and `/* */` |
| `.vhd` | `--` |
| `.vhdl` | `--` |
| `.vim` | `"` |
| `.vlang` | `//` and `/* */` |
| `.vm` | `##` and `#* *#` |
| `.vsl` | `##` and `#* *#` |
| `.vtl` | `##` and `#* *#` |
| `.vue` | `<!-- -->` |
| `.wgsl` | `//` |
| `.wsf` | `<!-- -->` |
| `.xhtml` | `<!-- -->` |
| `.xml` | `<!-- -->` |
| `.xq` | `(: :)` |
| `.xql` | `(: :)` |
| `.xqm` | `(: :)` |
| `.xquery` | `(: :)` |
| `.xsl` | `<!-- -->` |
| `.xslt` | `<!-- -->` |
| `.yaml` | `#` |
| `.yml` | `#` |
| `.zig` | `//` and `///` and `//!` |
| `.zsh` | `#` |
