# Style — Commit and Branch Conventions

## Branch naming
```
feature/{issue_number}_{short_description_separated_by_underscores}
```
Example: `feature/109_improve_issue_validator_pr_agent`

## Commit message format
```
embention/AITools#{issue_number} {short imperative summary}

- {line 1: what changed}
- {line 2: why or what it fixes}
- {line 3: optional context or scope}
```
- Maximum 3 bullet lines in the body.
- Title must be a short imperative phrase in English.
- Body lines are optional but preferred for non-trivial changes.
- Never add AI attribution or Co-Authored-By lines.

## Safety Rules
- NEVER delete files or folders without confirming with the user first.
- Check git status before any destructive operation (rm, Remove-Item, git clean).
- Never use --force or --no-verify flags without explicit user approval.
- Never run git commit on behalf of the user. The user decides when and what to commit.
