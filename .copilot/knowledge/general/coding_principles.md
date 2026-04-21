# General Coding Principles

## Readability First
- Optimize code for the reader. Code is read far more often than it is written.
- Use descriptive, precise names for variables, functions, classes, and modules. Avoid cryptic abbreviations.
- Comments must explain WHY a decision was made, not literally describe what the code does.

## Design Principles
- **KISS**: prefer the simplest solution that solves the problem well.
- **DRY**: when logic repeats, extract it into a reusable function or abstraction.
- **SRP**: each function, class, or module must have a single reason to change.
- **Dependency injection**: prefer injecting dependencies over creating them inside the logic.
- **Fail fast**: validate configurations, inputs, and states early, and raise clear errors when something is wrong.

## Avoid Excessive Conditionals
- Avoid chains of `if/isinstance` or repeated inline ternaries inside functions. Use dispatch tables, polymorphism, `dict.get()` with defaults, or guard clauses instead.
- When multiple branches differ only in data (not logic), the data belongs in a table or subclass — not in a conditional.

## Comment Style
- Never use a module-level docstring (`"""..."""`) as a script header. Use `#`-prefixed comment blocks instead.
- Script headers must include a short usage example in `#` comments at the top of the file.
- Document every non-trivial function with a Doxygen/JavaDoc-style block: `@brief`, `@param`, `@return`, and `@raises` where applicable.
- Inline `#` comments remain focused on WHY, not what.

```python
# Correct script header
# enrich_oracle.py — proposes expected_output_keywords for each oracle sample.
#
# Usage:
#   python scripts/enrich_oracle.py --run-dir <path> [--oracle <path>] [--output <path>]

def some_function(x: int) -> str:
    ##
    # @brief Converts an integer to its hex representation.
    # @param x  The integer to convert. Must be non-negative.
    # @return   Hex string prefixed with '0x'.
    # @raises ValueError  If x is negative.
    ##
    if x < 0:
        raise ValueError(f"Expected non-negative integer, got {x}")
    return hex(x)
```

## Practical Rules
- Before introducing an abstraction, verify it reduces real complexity — not just moves code around.
- If a name needs a comment to be understood, the name is weak.
- If a function mixes validation, data access, transformation, and output, it is probably doing too much.
- Do not use complex design patterns if a simple function or clear data structure is enough.
- If context is insufficient to apply these principles without breaking existing repo conventions, preserve the repo convention and apply these guidelines within that frame.
