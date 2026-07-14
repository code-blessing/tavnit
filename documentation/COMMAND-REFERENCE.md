# Keyword/Command reference

The following keywords/commands are supported:
* [template-renderer](#template-renderer)
* [end-template-renderer](#end-template-renderer)
* [replace-value-by-expression](#replace-value-by-expression) (re)
* [end-replace-value-by-expression](#end-replace-value-by-expression) (end-re)
* [replace-value-by-value](#replace-value-by-value) (rv)
* [end-replace-value-by-value](#end-replace-value-by-value) (end-rv)
* [if](#if)
* [else-if](#else-if)
* [else](#else)
* [end-if](#end-if)
* [foreach](#foreach)
* [end-foreach](#end-foreach)
* [ignore-text](#ignore-text)
* [end-ignore-text](#end-ignore-text)
* [print-text](#print-text)
* [remark](#remark)
* [modify-provided-filepath-by-replacements](#modify-provided-filepath-by-replacements)
* [render-template](#render-template)
* [add-import-to-renderer](#add-import-to-renderer)
* [move-comment-backward](#move-comment-backward) (mvb)
* [move-comment-forward](#move-comment-forward) (mvf)
* [remove-blanks-before-comment](#remove-blanks-before-comment) (rbb)
* [remove-blanks-after-comment](#remove-blanks-after-comment) (rba)
* [remove-blanks-and-linebreak-before-comment](#remove-blanks-and-linebreak-before-comment) (rlb)
* [remove-blanks-and-linebreak-after-comment](#remove-blanks-and-linebreak-after-comment) (rla)
* [no-default-whitespace-remove](#no-default-whitespace-remove) (ndr)

Commands always start with a `@`.



## template-renderer

Syntax: 
``````
@template-renderer
    [
        templateRendererClassName="..."
        templateRendererPackageName="..."
        templateRendererInterfaceName="..."
        templateRendererInterfacePackageName="..."
    ]
    [
        modelClassName="..."
        modelName="..."
        modelPackageName="..."
        isList="yes|no"
    ]
    [ ... ]
    ....
@end-template-renderer
``````

Aliases: _none_

Defines the template renderer kotlin class in which the content of the given file is put into. Optionally declares model instances (kotlin function parameters) passed to the renderer. 

The first attribute group specifies the renderer class; subsequent repeating groups each define one model parameter.

Additional template-renderer commands can be nested inside the top-level one; each nested template-renderer produces an independent renderer class and is closed with end-template-renderer. A nested template renderer is completely independent (and its content therefore removed from) the parent template renderer. Also all other commands defined in the parent template (models, if..else, replacements, etc.) will not affect the child template renderer, as each template renderer resides in its own class.

Example:

```html
<!-- @tt{{{
  @template-renderer
     [ templateRendererClassName="GreetingRenderer" templateRendererPackageName="com.example.render" ]
     [ modelName="user" modelClassName="User" modelPackageName="com.example.model" ]
}}}@ -->
<p>Hello</p>
<!-- @tt{{{ @end-template-renderer }}}@ -->
```

Varia:
* This command must be closed using the [end-template-renderer](#end-template-renderer) command.
* This command supports to be auto-closed. The corresponding [end-template-renderer](#end-template-renderer) command can be skipped.
* This command has a primary group of attributes optionally followed by zero or more groups of attributes.
* This command/keyword is NOT forced to reside as nested element in a certain parent element.
* This command/keyword can be combined with any other command in the same comment.

Primary Attributes:
* *templateRendererClassName*: The name of the template class that will generate this template.
  * Required attribute: _Yes_
  * Required not empty: _Yes_
  * Allowed values: _\<unrestricted\>_
  * Mutually exclusive with: none
* *templateRendererPackageName*: The name of the package where the class defined with ```templateRendererClassName``` resides in.
  * Required attribute: _No_
  * Required not empty: _Yes_
  * Allowed values: _\<unrestricted\>_
  * Mutually exclusive with: none
* *templateRendererInterfaceName*: The name of an optional interface class name that is added to the class defined with the ```templateRendererClassName```.
  * Required attribute: _No_
  * Required not empty: _Yes_
  * Allowed values: _\<unrestricted\>_
  * Mutually exclusive with: none
* *templateRendererInterfacePackageName*: The name of the package where the interface defined with ```templateRendererInterfaceName``` resides in.
  * Required attribute: _No_
  * Required not empty: _Yes_
  * Allowed values: _\<unrestricted\>_
  * Mutually exclusive with: none

Repeatable Group Attributes:
* *modelName*: The name of the model variable. The variable can later be used to access fields and functions on the model e.g. in conditions or as replacement values.
  * Required attribute: _Yes_
  * Required not empty: _Yes_
  * Allowed values: _\<unrestricted\>_
  * Mutually exclusive with: none
* *modelClassName*: The name of the model class. This class provides all the fields in the template.
  * Required attribute: _Yes_
  * Required not empty: _Yes_
  * Allowed values: _\<unrestricted\>_
  * Mutually exclusive with: none
* *modelPackageName*: The name of the package where the model class defined with ```modelClassName``` resides in.
  * Required attribute: _No_
  * Required not empty: _Yes_
  * Allowed values: _\<unrestricted\>_
  * Mutually exclusive with: none
* *isList*: When set to ```yes```, the model parameter is declared as a list of the model class defined with ```modelClassName```, i.e. ```List<ModelClass>``` instead of ```ModelClass```. Defaults to ```no```.
  * Required attribute: _No_
  * Required not empty: _Yes_
  * Allowed values: ```yes```,```no```
  * Mutually exclusive with: none

## end-template-renderer

Syntax: 
``````
@end-template-renderer
``````

Aliases: _none_

Varia:
* This command is closing the [template-renderer](#template-renderer) command.
* This command triggers to close all nested commands that support auto-closing.
* This command/keyword does not support groups and has no attributes.
* This command/keyword is NOT forced to reside as nested element in a certain parent element.
* This command/keyword can be combined with any other command in the same comment.

## replace-value-by-expression

Syntax: 
``````
@replace-value-by-expression
    [
        searchValue="..."
        replaceByExpression="..."
    ]
    [ ... ]
    ....
@end-replace-value-by-expression
``````

Aliases: ```@re``` (can be used in place of ```@replace-value-by-expression```)

Replaces a value by a kotlin expression in a multiline string. The expression is often accessing properties or functions on a model instance declared with the template-renderer command.

Example:

```html
<h1><!-- @tt{{{ @replace-value-by-expression [ searchValue="TITLE" replaceByExpression="page.title" ] }}}@ -->TITLE<!-- @tt{{{ @end-replace-value-by-expression }}}@ --></h1>
```

Varia:
* This command must be closed using the [end-replace-value-by-expression](#end-replace-value-by-expression) command.
* This command supports to be auto-closed. The corresponding [end-replace-value-by-expression](#end-replace-value-by-expression) command can be skipped.
* This command can have many groups of attributes
* This command/keyword is NOT forced to reside as nested element in a certain parent element.
* This command/keyword can be combined with any other command in the same comment.

Repeatable Group Attributes:
* *searchValue*: The token that has to be searched in the enclosed block of content. The search is case-sensitive.
  * Required attribute: _Yes_
  * Required not empty: _Yes_
  * Allowed values: _\<unrestricted\>_
  * Mutually exclusive with: none
* *replaceByExpression*: The expression accessing the model class with which the token defined with the attribute ```searchValue``` is replaced.
  * Required attribute: _Yes_
  * Required not empty: _Yes_
  * Allowed values: _\<unrestricted\>_
  * Mutually exclusive with: none

## end-replace-value-by-expression

Syntax: 
``````
@end-replace-value-by-expression
``````

Aliases: ```@end-re``` (can be used in place of ```@end-replace-value-by-expression```)

Varia:
* This command is closing the [replace-value-by-expression](#replace-value-by-expression) command.
* This command triggers to close all nested commands that support auto-closing.
* This command/keyword does not support groups and has no attributes.
* This command/keyword is NOT forced to reside as nested element in a certain parent element.
* This command/keyword can be combined with any other command in the same comment.

## replace-value-by-value

Syntax: 
``````
@replace-value-by-value
    [
        searchValue="..."
        replaceByValue="..."
    ]
    [ ... ]
    ....
@end-replace-value-by-value
``````

Aliases: ```@rv``` (can be used in place of ```@replace-value-by-value```)

Replaces a value by another (fixed) value.

Example:

```html
<a><!-- @tt{{{ @replace-value-by-value [ searchValue="URL" replaceByValue="/home" ] }}}@ -->URL<!-- @tt{{{ @end-replace-value-by-value }}}@ --></a>
```

Varia:
* This command must be closed using the [end-replace-value-by-value](#end-replace-value-by-value) command.
* This command supports to be auto-closed. The corresponding [end-replace-value-by-value](#end-replace-value-by-value) command can be skipped.
* This command can have many groups of attributes
* This command/keyword is NOT forced to reside as nested element in a certain parent element.
* This command/keyword can be combined with any other command in the same comment.

Repeatable Group Attributes:
* *searchValue*: The token that has to be searched in the enclosed block of content. The search is case-sensitive.
  * Required attribute: _Yes_
  * Required not empty: _Yes_
  * Allowed values: _\<unrestricted\>_
  * Mutually exclusive with: none
* *replaceByValue*: The plain value the attribute ```searchValue``` is replaced.
  * Required attribute: _Yes_
  * Required not empty: _Yes_
  * Allowed values: _\<unrestricted\>_
  * Mutually exclusive with: none

## end-replace-value-by-value

Syntax: 
``````
@end-replace-value-by-value
``````

Aliases: ```@end-rv``` (can be used in place of ```@end-replace-value-by-value```)

Varia:
* This command is closing the [replace-value-by-value](#replace-value-by-value) command.
* This command triggers to close all nested commands that support auto-closing.
* This command/keyword does not support groups and has no attributes.
* This command/keyword is NOT forced to reside as nested element in a certain parent element.
* This command/keyword can be combined with any other command in the same comment.

## if

Syntax: 
``````
@if
    [
        conditionExpression="..."
    ]
    ....
@end-if
``````

Aliases: _none_

Render the enclosed content only if the condition expression evaluates to true.

Example:

```html
<!-- @tt{{{ @if [ conditionExpression="user.isAdmin" ] }}}@ -->
<p>Welcome, administrator.</p>
<!-- @tt{{{ @end-if }}}@ -->
```

Varia:
* This command must be closed using the [end-if](#end-if) command.
* This command neither triggers an auto-closing of nested commands nor will it be auto-closed.
* This command/keyword must have exactly one group of attributes.
* This command/keyword is NOT forced to reside as nested element in a certain parent element.
* This command/keyword can be combined with any other command in the same comment.

Attributes:
* *conditionExpression*: The condition returning a boolean value that is used for the if statement or else-if statement.
  * Required attribute: _Yes_
  * Required not empty: _Yes_
  * Allowed values: _\<unrestricted\>_
  * Mutually exclusive with: none

## else-if

Syntax: 
``````
@else-if
    [
        conditionExpression="..."
    ]
``````

Aliases: _none_

Render the enclosed content only if the condition expression evaluates to true and all previous conditions of the if/else-if conditions evaluates to false.

Example:

```html
<!-- @tt{{{ @if [ conditionExpression="cart.isEmpty()" ] }}}@ -->
<p>Your cart is empty.</p>
<!-- @tt{{{ @else-if [ conditionExpression="cart.hasSingleItem()" ] }}}@ -->
<p>One item in your cart.</p>
<!-- @tt{{{ @else }}}@ -->
<p>Several items in your cart.</p>
<!-- @tt{{{ @end-if }}}@ -->
```

Varia:
* This command stands for itself and does not need to be closed by another command.
* This command triggers to close all nested commands that support auto-closing.
* This command/keyword must have exactly one group of attributes.
* This command/keyword must reside as directly nested element in the parent element [if](#if).
* This command/keyword can be combined with any other command in the same comment.

Attributes:
* *conditionExpression*: The condition returning a boolean value that is used for the if statement or else-if statement.
  * Required attribute: _Yes_
  * Required not empty: _Yes_
  * Allowed values: _\<unrestricted\>_
  * Mutually exclusive with: none

## else

Syntax: 
``````
@else
``````

Aliases: _none_

Render the enclosed content only if all previous if/else-if conditions evaluates to false

Example:

```html
<!-- @tt{{{ @if [ conditionExpression="user.isLoggedIn" ] }}}@ -->
<p>Welcome back.</p>
<!-- @tt{{{ @else }}}@ -->
<p>Please log in.</p>
<!-- @tt{{{ @end-if }}}@ -->
```

Varia:
* This command stands for itself and does not need to be closed by another command.
* This command triggers to close all nested commands that support auto-closing.
* This command/keyword does not support groups and has no attributes.
* This command/keyword must reside as directly nested element in the parent element [if](#if).
* This command/keyword can be combined with any other command in the same comment.

## end-if

Syntax: 
``````
@end-if
``````

Aliases: _none_

Varia:
* This command is closing the [if](#if) command.
* This command triggers to close all nested commands that support auto-closing.
* This command/keyword does not support groups and has no attributes.
* This command/keyword is NOT forced to reside as nested element in a certain parent element.
* This command/keyword can be combined with any other command in the same comment.

## foreach

Syntax: 
``````
@foreach
    [
        iteratorExpression="..."
        loopVariable="..."
    ]
    ....
@end-foreach
``````

Aliases: _none_

Iterates/Loops over a collection of items (=iterable). In each loop, the current item is hold in a loop variable.

Example:

```html
<ul><!-- @tt{{{ @foreach [ iteratorExpression="page.items" loopVariable="item" ] }}}@ -->
  <li>an item</li><!-- @tt{{{ @end-foreach }}}@ -->
</ul>
```

Varia:
* This command must be closed using the [end-foreach](#end-foreach) command.
* This command neither triggers an auto-closing of nested commands nor will it be auto-closed.
* This command/keyword must have exactly one group of attributes.
* This command/keyword is NOT forced to reside as nested element in a certain parent element.
* This command/keyword can be combined with any other command in the same comment.

Attributes:
* *iteratorExpression*: The expression returning the collection/iterable that is looped over.
  * Required attribute: _Yes_
  * Required not empty: _Yes_
  * Allowed values: _\<unrestricted\>_
  * Mutually exclusive with: none
* *loopVariable*: The name of the loop variable, similar to the model variable from ```modelName```. The variable holds the current instance of the loop iterable defined with ```iteratorExpression```.
  * Required attribute: _Yes_
  * Required not empty: _Yes_
  * Allowed values: _\<unrestricted\>_
  * Mutually exclusive with: none

## end-foreach

Syntax: 
``````
@end-foreach
``````

Aliases: _none_

Varia:
* This command is closing the [foreach](#foreach) command.
* This command triggers to close all nested commands that support auto-closing.
* This command/keyword does not support groups and has no attributes.
* This command/keyword is NOT forced to reside as nested element in a certain parent element.
* This command/keyword can be combined with any other command in the same comment.

## ignore-text

Syntax: 
``````
@ignore-text
    ....
@end-ignore-text
``````

Aliases: _none_

Ignores the text and does not output it in the template renderer.

Example:

```html
<ul><!-- @tt{{{ @foreach [ iteratorExpression="page.items" loopVariable="item" ] }}}@ -->
  <li>an item</li><!-- @tt{{{ @end-foreach @ignore-text }}}@ -->
  <li>only a sample row to keep the raw file looking complete</li>
  <li>another sample row</li><!-- @tt{{{ @end-ignore-text }}}@ -->
</ul>
```

Varia:
* This command must be closed using the [end-ignore-text](#end-ignore-text) command.
* This command supports to be auto-closed. The corresponding [end-ignore-text](#end-ignore-text) command can be skipped.
* This command/keyword does not support groups and has no attributes.
* This command/keyword is NOT forced to reside as nested element in a certain parent element.
* This command/keyword can be combined with any other command in the same comment.

## end-ignore-text

Syntax: 
``````
@end-ignore-text
``````

Aliases: _none_

Varia:
* This command is closing the [ignore-text](#ignore-text) command.
* This command triggers to close all nested commands that support auto-closing.
* This command/keyword does not support groups and has no attributes.
* This command/keyword is NOT forced to reside as nested element in a certain parent element.
* This command/keyword can be combined with any other command in the same comment.

## print-text

Syntax: 
``````
@print-text
    [
        text="..."
    ]
``````

Aliases: _none_

Print additional text as output of the template renderer.

Example:

```html
<p><!-- @tt{{{ @print-text [ text="Generated by tavnit" ] }}}@ --></p>
```

Varia:
* This command stands for itself and does not need to be closed by another command.
* This command neither triggers an auto-closing of nested commands nor will it be auto-closed.
* This command/keyword must have exactly one group of attributes.
* This command/keyword is NOT forced to reside as nested element in a certain parent element.
* This command/keyword can be combined with any other command in the same comment.

Attributes:
* *text*: The text content of the command (e.g. the text printed by ```print-text``` or the remark documented by ```remark```).
  * Required attribute: _Yes_
  * Required not empty: _No_
  * Allowed values: _\<unrestricted\>_
  * Mutually exclusive with: none

## remark

Syntax: 
``````
@remark
    [
        text="..."
    ]
``````

Aliases: _none_

Adds a remark to the template that is purely meant for internal documentation. The text is completely ignored: it neither appears in the generated template renderer nor in its output.

Use this command to leave notes or explanations inside a template (e.g. why a certain construct is built the way it is) without affecting the result in any way.

Example:

```html
<!-- @tt{{{ @remark [ text="This block is repeated once per article." ] }}}@ -->
```

Varia:
* This command stands for itself and does not need to be closed by another command.
* This command neither triggers an auto-closing of nested commands nor will it be auto-closed.
* This command/keyword must have exactly one group of attributes.
* This command/keyword is NOT forced to reside as nested element in a certain parent element.
* This command/keyword can be combined with any other command in the same comment.

Attributes:
* *text*: The text content of the command (e.g. the text printed by ```print-text``` or the remark documented by ```remark```).
  * Required attribute: _Yes_
  * Required not empty: _No_
  * Allowed values: _\<unrestricted\>_
  * Mutually exclusive with: none

## modify-provided-filepath-by-replacements

Syntax: 
``````
@modify-provided-filepath-by-replacements
``````

Aliases: _none_

Each template renderer provides the path of the source file as string. By using this command, the path can be modified with all replacements provided by ```replace-value-by-expression``` and ```replace-value-by-value``` the ```modify-provided-filepath-by-replacements``` command is currently nested in.

The intention of this command is that the filename and path can also take part of the replacements and this has not to be handled separately and outside of the template renderer; the replacements for the filename follow often the same patterns as for the file content. If you change in your template every ```foo``` to ```bar```, it is likely that you also want to change the path of the file e.g. from ```src/foo/foo.txt``` to ```src/bar/bar.txt``` to generate dynamic file paths.

You can use this command multiple times per template renderer. The replacements are done one after another in the order of the command usage.

If you create multiple template renderers from one file (multiple template-renderer), you can (and have to) call ```modify-provided-filepath-by-replacements``` for each template renderer individually.

Example:

```html
<!-- @tt{{{
  @template-renderer [ templateRendererClassName="PageRenderer" ]
  @replace-value-by-value [ searchValue="index" replaceByValue="home" ]
  @modify-provided-filepath-by-replacements
}}}@ -->
```

With a source file `src/index.html` the generated `filePath(...)` returns `src/home.html`.

Varia:
* This command stands for itself and does not need to be closed by another command.
* This command neither triggers an auto-closing of nested commands nor will it be auto-closed.
* This command/keyword does not support groups and has no attributes.
* This command/keyword is NOT forced to reside as nested element in a certain parent element.
* This command/keyword can be combined with any other command in the same comment.

## render-template

Syntax: 
``````
@render-template
    [
        templateRendererClassName="..."
        templateRendererPackageName="..."
    ]
    [
        modelName="..."
        modelExpression="..."
    ]
    [ ... ]
``````

Aliases: _none_

Calls another template renderer and embeds its output. The first attribute group specifies the renderer class; subsequent groups map model parameters to expressions.

This command's syntax has a lot of similarity to template-renderer, as it calls a template renderer defined by the template-renderer block.

Example:

```html
<tbody><!-- @tt{{{
  @foreach [ iteratorExpression="page.rows" loopVariable="row" ]
  @render-template
     [ templateRendererClassName="RowRenderer" templateRendererPackageName="com.example.render" ]
     [ modelName="row" modelExpression="row" ]
  @end-foreach
}}}@ --></tbody>
```

Varia:
* This command stands for itself and does not need to be closed by another command.
* This command neither triggers an auto-closing of nested commands nor will it be auto-closed.
* This command has a primary group of attributes optionally followed by zero or more groups of attributes.
* This command/keyword is NOT forced to reside as nested element in a certain parent element.
* This command/keyword can be combined with any other command in the same comment.

Primary Attributes:
* *templateRendererClassName*: The name of the template class that will generate this template.
  * Required attribute: _Yes_
  * Required not empty: _Yes_
  * Allowed values: _\<unrestricted\>_
  * Mutually exclusive with: none
* *templateRendererPackageName*: The name of the package where the class defined with ```templateRendererClassName``` resides in.
  * Required attribute: _No_
  * Required not empty: _Yes_
  * Allowed values: _\<unrestricted\>_
  * Mutually exclusive with: none

Repeatable Group Attributes:
* *modelName*: The name of the model variable. The variable can later be used to access fields and functions on the model e.g. in conditions or as replacement values.
  * Required attribute: _Yes_
  * Required not empty: _Yes_
  * Allowed values: _\<unrestricted\>_
  * Mutually exclusive with: none
* *modelExpression*: The expression that provides the value for the model parameter specified by ```modelName``` when calling the template renderer.
  * Required attribute: _Yes_
  * Required not empty: _Yes_
  * Allowed values: _\<unrestricted\>_
  * Mutually exclusive with: none

## add-import-to-renderer

Syntax: 
``````
@add-import-to-renderer
    [
        importClassName="..."
        importPackageName="..."
    ]
    [ ... ]
``````

Aliases: _none_

Adds one or more imports to the generated template renderer class. Each repeating attribute group adds one import.

Once a symbol is imported, it can be referenced by its short name in any expression (e.g. in ```if``` conditions, ```foreach``` iterables or ```replace-value-by-expression``` expressions). This lets you keep those expressions short and readable instead of repeating the fully qualified name (e.g. write ```DayOfWeek.WEDNESDAY``` instead of ```java.time.DayOfWeek.WEDNESDAY```).

Imports are added to the renderer class header in addition to the imports tavnit derives automatically (model classes, the renderer interface and rendered sub-templates). Duplicate imports are removed.

Example:

```html
<!-- @tt{{{ @add-import-to-renderer [ importClassName="DayOfWeek.WEDNESDAY" importPackageName="java.time" ] }}}@ -->
```

Varia:
* This command stands for itself and does not need to be closed by another command.
* This command neither triggers an auto-closing of nested commands nor will it be auto-closed.
* This command can have many groups of attributes
* This command/keyword is NOT forced to reside as nested element in a certain parent element.
* This command/keyword can be combined with any other command in the same comment.

Repeatable Group Attributes:
* *importClassName*: The name of the symbol to import (e.g. a class, object or enum entry like ```DayOfWeek.WEDNESDAY```). Combined with ```importPackageName``` (when given) to form the fully qualified import.
  * Required attribute: _Yes_
  * Required not empty: _Yes_
  * Allowed values: _\<unrestricted\>_
  * Mutually exclusive with: none
* *importPackageName*: The name of the package the symbol defined with ```importClassName``` resides in (e.g. ```java.time```). When omitted, the value of ```importClassName``` is imported as-is.
  * Required attribute: _No_
  * Required not empty: _No_
  * Allowed values: _\<unrestricted\>_
  * Mutually exclusive with: none

## move-comment-backward

Syntax: 
``````
@move-comment-backward
    [
        beforeFirstOccurrenceOf="..."
        afterFirstOccurrenceOf="..."
        beforeLastOccurrenceOf="..."
        afterLastOccurrenceOf="..."
    ]
``````

Aliases: ```@mvb``` (can be used in place of ```@move-comment-backward```)

Moves the whole comment in which this command is written backward (i.e. before the preceding text). Optionally positions it relative to the first or last occurrence of a given text in the surrounding content. The comment will be moved at most to the previous comment or to the beginning of the file.

This is useful as some file formats do not allow to put a comment as first line of the file.

Example:  XML starts with a preamble like ```<?xml version="1.0" encoding="iso-8859-1"?>``` and this text should be part of the template renderer's output. But it is not possible to write a XML comment before this preamble. To still span the template from the beginning of the file, you can move the comment to the beginning of the file using this command (```@move-comment-backward```)

Example:

```xml
<?xml version="1.0"?><!-- @tt{{{
  @move-comment-backward
  @template-renderer [ templateRendererClassName="FeedRenderer" ]
}}}@ -->
<feed>...</feed>
```

Varia:
* This command stands for itself and does not need to be closed by another command.
* This command neither triggers an auto-closing of nested commands nor will it be auto-closed.
* This command can have zero or one group of attributes.
* This command/keyword is NOT forced to reside as nested element in a certain parent element.
* This command/keyword must not be used in the same comment as [move-comment-forward](#move-comment-forward) (mutually exclusive).

Attributes:
* *beforeFirstOccurrenceOf*: Positions the comment before the first occurrence of the given text in the surrounding content.
  * Required attribute: _No_
  * Required not empty: _Yes_
  * Allowed values: _\<unrestricted\>_
  * Mutually exclusive with: ```afterFirstOccurrenceOf```, ```beforeLastOccurrenceOf```, ```afterLastOccurrenceOf```
* *afterFirstOccurrenceOf*: Positions the comment after the first occurrence of the given text in the surrounding content.
  * Required attribute: _No_
  * Required not empty: _Yes_
  * Allowed values: _\<unrestricted\>_
  * Mutually exclusive with: ```beforeFirstOccurrenceOf```, ```beforeLastOccurrenceOf```, ```afterLastOccurrenceOf```
* *beforeLastOccurrenceOf*: Positions the comment before the last occurrence of the given text in the surrounding content.
  * Required attribute: _No_
  * Required not empty: _Yes_
  * Allowed values: _\<unrestricted\>_
  * Mutually exclusive with: ```beforeFirstOccurrenceOf```, ```afterFirstOccurrenceOf```, ```afterLastOccurrenceOf```
* *afterLastOccurrenceOf*: Positions the comment after the last occurrence of the given text in the surrounding content.
  * Required attribute: _No_
  * Required not empty: _Yes_
  * Allowed values: _\<unrestricted\>_
  * Mutually exclusive with: ```beforeFirstOccurrenceOf```, ```afterFirstOccurrenceOf```, ```beforeLastOccurrenceOf```

## move-comment-forward

Syntax: 
``````
@move-comment-forward
    [
        beforeFirstOccurrenceOf="..."
        afterFirstOccurrenceOf="..."
        beforeLastOccurrenceOf="..."
        afterLastOccurrenceOf="..."
    ]
``````

Aliases: ```@mvf``` (can be used in place of ```@move-comment-forward```)

Moves the whole comment in which this command is written forward (i.e. after the following text). Optionally positions it relative to the first or last occurrence of a given text in the surrounding content. The comment will be moved at most to the next comment or to the end of the file.

Example:

```html
<!-- @tt{{{ @move-comment-forward [ afterFirstOccurrenceOf="</head>" ] }}}@ -->
```

Varia:
* This command stands for itself and does not need to be closed by another command.
* This command neither triggers an auto-closing of nested commands nor will it be auto-closed.
* This command can have zero or one group of attributes.
* This command/keyword is NOT forced to reside as nested element in a certain parent element.
* This command/keyword must not be used in the same comment as [move-comment-backward](#move-comment-backward) (mutually exclusive).

Attributes:
* *beforeFirstOccurrenceOf*: Positions the comment before the first occurrence of the given text in the surrounding content.
  * Required attribute: _No_
  * Required not empty: _Yes_
  * Allowed values: _\<unrestricted\>_
  * Mutually exclusive with: ```afterFirstOccurrenceOf```, ```beforeLastOccurrenceOf```, ```afterLastOccurrenceOf```
* *afterFirstOccurrenceOf*: Positions the comment after the first occurrence of the given text in the surrounding content.
  * Required attribute: _No_
  * Required not empty: _Yes_
  * Allowed values: _\<unrestricted\>_
  * Mutually exclusive with: ```beforeFirstOccurrenceOf```, ```beforeLastOccurrenceOf```, ```afterLastOccurrenceOf```
* *beforeLastOccurrenceOf*: Positions the comment before the last occurrence of the given text in the surrounding content.
  * Required attribute: _No_
  * Required not empty: _Yes_
  * Allowed values: _\<unrestricted\>_
  * Mutually exclusive with: ```beforeFirstOccurrenceOf```, ```afterFirstOccurrenceOf```, ```afterLastOccurrenceOf```
* *afterLastOccurrenceOf*: Positions the comment after the last occurrence of the given text in the surrounding content.
  * Required attribute: _No_
  * Required not empty: _Yes_
  * Allowed values: _\<unrestricted\>_
  * Mutually exclusive with: ```beforeFirstOccurrenceOf```, ```afterFirstOccurrenceOf```, ```beforeLastOccurrenceOf```

## remove-blanks-before-comment

Syntax: 
``````
@remove-blanks-before-comment
``````

Aliases: ```@rbb``` (can be used in place of ```@remove-blanks-before-comment```)

Removes the consecutive blanks (spaces and tabs) directly preceding the comment from the neighboring text part. Stops before the line-ending; the line-ending itself is kept.

This is useful if you don't want to have dangling spaces/indents in your template output if the tavnit comments itself have to follow some indentation rules (e.g. by your linter).

The exact whitespace rules and override commands are described here: [WHITESPACE-HANDLING.md](WHITESPACE-HANDLING.md)

Example:

```html
<li>item</li>    <!-- @tt{{{ @remove-blanks-before-comment }}}@ -->
```

The blanks between `</li>` and the comment are removed; the line-ending is kept.

Varia:
* This command stands for itself and does not need to be closed by another command.
* This command neither triggers an auto-closing of nested commands nor will it be auto-closed.
* This command/keyword does not support groups and has no attributes.
* This command/keyword is NOT forced to reside as nested element in a certain parent element.
* This command/keyword must not be used in the same comment as [remove-blanks-and-linebreak-before-comment](#remove-blanks-and-linebreak-before-comment) (mutually exclusive).

## remove-blanks-after-comment

Syntax: 
``````
@remove-blanks-after-comment
``````

Aliases: ```@rba``` (can be used in place of ```@remove-blanks-after-comment```)

Removes the consecutive blanks (spaces and tabs) directly following the comment from the neighboring text part. Stops before the line-ending; the line-ending itself is kept.

This is useful if you don't want to have dangling spaces/indents in your template output if the tavnit comments itself have to follow some indentation rules (e.g. by your linter).

The exact whitespace rules and override commands are described here: [WHITESPACE-HANDLING.md](WHITESPACE-HANDLING.md)

Example:

```html
<!-- @tt{{{ @remove-blanks-after-comment }}}@ -->    <li>item</li>
```

The blanks between the comment and `<li>` are removed; the line-ending is kept.

Varia:
* This command stands for itself and does not need to be closed by another command.
* This command neither triggers an auto-closing of nested commands nor will it be auto-closed.
* This command/keyword does not support groups and has no attributes.
* This command/keyword is NOT forced to reside as nested element in a certain parent element.
* This command/keyword must not be used in the same comment as [remove-blanks-and-linebreak-after-comment](#remove-blanks-and-linebreak-after-comment) (mutually exclusive).

## remove-blanks-and-linebreak-before-comment

Syntax: 
``````
@remove-blanks-and-linebreak-before-comment
``````

Aliases: ```@rlb``` (can be used in place of ```@remove-blanks-and-linebreak-before-comment```)

Removes the consecutive blanks (spaces and tabs) directly preceding the comment from the neighboring text part, including the immediately adjacent line-ending.

This is useful if you don't want to have empty lines in your template output due to the tavnits comments.

The exact whitespace rules and override commands are described here: [WHITESPACE-HANDLING.md](WHITESPACE-HANDLING.md)

Example:

```html
<p>text</p>
<!-- @tt{{{ @remove-blanks-and-linebreak-before-comment }}}@ -->
```

The blanks and the line-ending directly before the comment are removed, so no empty
line is left in the output.

Varia:
* This command stands for itself and does not need to be closed by another command.
* This command neither triggers an auto-closing of nested commands nor will it be auto-closed.
* This command/keyword does not support groups and has no attributes.
* This command/keyword is NOT forced to reside as nested element in a certain parent element.
* This command/keyword must not be used in the same comment as [remove-blanks-before-comment](#remove-blanks-before-comment) (mutually exclusive).

## remove-blanks-and-linebreak-after-comment

Syntax: 
``````
@remove-blanks-and-linebreak-after-comment
``````

Aliases: ```@rla``` (can be used in place of ```@remove-blanks-and-linebreak-after-comment```)

Removes the consecutive blanks (spaces and tabs) directly following the comment from the neighboring text part, including the immediately adjacent line-ending.

This is useful if you don't want to have empty lines in your template output due to the tavnits comments.

The exact whitespace rules and override commands are described here: [WHITESPACE-HANDLING.md](WHITESPACE-HANDLING.md)

Example:

```html
<!-- @tt{{{ @remove-blanks-and-linebreak-after-comment }}}@ -->
<p>text</p>
```

The blanks and the line-ending directly after the comment are removed, so no empty
line is left in the output.

Varia:
* This command stands for itself and does not need to be closed by another command.
* This command neither triggers an auto-closing of nested commands nor will it be auto-closed.
* This command/keyword does not support groups and has no attributes.
* This command/keyword is NOT forced to reside as nested element in a certain parent element.
* This command/keyword must not be used in the same comment as [remove-blanks-after-comment](#remove-blanks-after-comment) (mutually exclusive).

## no-default-whitespace-remove

Syntax: 
``````
@no-default-whitespace-remove
``````

Aliases: ```@ndr``` (can be used in place of ```@no-default-whitespace-remove```)

Disables the default whitespace handling for the comment it is written in. Only the comment itself is removed; no blanks and no line breaks before or after the comment are touched.

Normally a comment that stands alone on its line has its surrounding whitespace collapsed automatically (see the note below). Use this command when you intentionally want to keep that whitespace (e.g. a blank line or the indentation) around an otherwise stand-alone comment.

Explicit remove-blanks commands still take effect: they override the (now disabled) default decision for their own side. So you can combine this command with e.g. ```@remove-blanks-before-comment``` to keep everything except the blanks directly before the comment.

The exact whitespace rules and override commands are described here: [WHITESPACE-HANDLING.md](WHITESPACE-HANDLING.md)

Example:

```html
    <!-- @tt{{{ @no-default-whitespace-remove }}}@ -->
```

Only the comment itself is removed; the indentation and the surrounding line breaks that
the default handling would otherwise collapse are kept.

Varia:
* This command stands for itself and does not need to be closed by another command.
* This command neither triggers an auto-closing of nested commands nor will it be auto-closed.
* This command/keyword does not support groups and has no attributes.
* This command/keyword is NOT forced to reside as nested element in a certain parent element.
* This command/keyword can be combined with any other command in the same comment.
