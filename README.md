# FL Evaluator

A naive evaluator for the functional programming language FL as
described in *FL Language Manual, Parts 1 and 2* (Backus et al.,
1989) [https://theory.stanford.edu/~aiken/publications/trs/RJ7100.pdf].

"Naive" means that it is too slow for a practical interpreter, and,
more importantly, there is no tail recursion optimization. Therefore
the JVM stack size must be chosen large enough for the expression to
be evaluated.

# Usage

`fleval` [ *OPTION* ]... [ *FILE* ]

Evaluates the FL expression in *FILE*. When *FILE* is `--` reads
expression from standard input.

When *FILE* is omitted, the interactive mode is started.

## Options
  * `-p` : In non-interactive mode, the result of the evaluation is
    printed on standard output.
  * `-L` *PATH* : *PATH* is added to the search paths for library
    environments. A library file `lib.fl` is first looked up in the
    current working directory, then in the directories of the search
    paths in order specified using the `-L` options.
  * `-c` : Only the syntax of *FILE* is checked, it is not evaluated.
  * `-to-fl` : Standard input is converted to the FL XML data format
    and printed on standard output.
  * `-from-fl` : FL XML data format is read from standard input and
    converted to text on standard output. This only works if the data
    consists of a single string element.
  * `-V` or `--version` : Prints the current version of the evaluator.
  * `-h-` or `--help` : Displays the help.

# Interactive mode

In the interactive mode, expressions are entered into a REPL at the
prompt `:`. Expressions are evaluated on pressed `ENTER`, except when
the brackets `(`, `[`, `<`, `[|` are not balanced. At the next prompt,
each unbalanced bracket is displayed before the prompt. A line
containing the single command `:)` close all unbalanced brackets.  The
command `:u` *FILE* adds the library in *FILE* to the standard
environment. The functions exported by *FILE* can then be used, as if
they were primitive functions.

# Emacs mode

The emacs supports highlighting of FL expressions and input of special
characters. Automatic indenting is currently not implemented.

See also [emacs/README.txt].

# I/O

The `in` primitive reads a character from standard input, if the
device name is `kbd` (buffered). If the device is a file name, then
the file with this name is opened once for reading. Subsequent calls
to `in` with the same name read the next character from the already
opened file.

The `out` primitive writes a string to standard output, if the device
name is `scr`. If the device is a file name, then the file with this
name is opened once for writing. Subsequent calls to `out` with the
same name append to the already opened file.

The `get` and `put` primitives read and write FL values in an XML data
format. Only values made of atoms and sequences are supported.

See also the RelaxNG schema for the FL XML data format in
[doc/fleval.rnc].

# Parsing oddities

* The character `/` starts an identifier, thus `a/b` must be written
  `a / b`, since `/b` is parses as an identifier,
* The characters `+` and `-` start a number, this `a-2` must be
  written `a - 2`, and `a + 2` must be written `a + 2`.
* Similar rules apply to `<-` and `->`, `<=|` and `|=>`, `[|` and
  `|]`. For example, the sequence containing the function `-` must be
  written `< - >` instead of `<->`.

# Implementation specifics

* Identifiers may start with a letter from `a` to `z`, `A` to `Z`, the
  characters `/`, `$`, `%`, `#`, `_`, `?`, `↑`, `↓`, `^`, `¬`, as well
  as Greek letters from `Α` to `ω` (Unicode range 0x0391 to 0x03C9).

  Their names may continue with the above and the digits `0` to `9`.

* For the character and string escape sequences, see [doc/escapes.txt].

* The name `alpha` is an alias for the primitive function `α`, the
  name `Delta` is an alias for the primitive function `Δ`.

# Limitations and deviations

* No tail recursion optimization is performed (yet).

* In the rule `exdef` *pat* ... , *pat* must be a simple expression,
  in order to avoid an ambiguous grammar. Complex expressions can
  still be used by putting them within parentheses.

* Used defined pattern combiners are not supported.

* Error handling while evaluating environments is patchy.

* Nested comments are not supported.

* The implementation of the most general form of definitions is
  questionable.
