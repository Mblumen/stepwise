# Domain Docs

How the engineering skills should consume this repo's domain documentation when exploring the codebase.

## Before exploring, read these

- `CONTEXT.md` at the repository root.
- Relevant ADRs under `docs/adr/`.

If these files do not exist, proceed silently. Domain-modeling workflows create
them lazily when terminology or architectural decisions are resolved.

## File structure

This is a single-context repository:

```
/
|-- CONTEXT.md
|-- docs/
|   `-- adr/
`-- app/
```

## Use the glossary's vocabulary

When output names a domain concept, use the term defined in `CONTEXT.md`. Avoid
synonyms that the glossary explicitly rejects.

If a required concept is missing, reconsider whether the terminology matches
the project or note the gap for the domain-modeling workflow.

## Flag ADR conflicts

Explicitly identify output that contradicts an existing ADR instead of silently
overriding the recorded decision.
