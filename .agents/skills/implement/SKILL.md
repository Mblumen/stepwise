---
name: implement
description: "Implement work requested from a spec, GitHub issue, or set of tickets. Use when the user asks to implement, build, or complete ticketed work."
---

Implement the work described by the user in the spec or tickets.

## Branch gate

Complete this gate before inspecting implementation code, writing tests, or changing files:

1. Check the current branch, working-tree status, and available local and remote branches.
2. For each issue, create a new dedicated branch from the intended base and check it out. Use `issue/<number>-<short-slug>` unless the repository specifies another convention.
3. Run `git branch --show-current` and verify that it reports the dedicated issue branch.

The gate is complete only when the dedicated branch is checked out. Keep one issue per branch; do not implement a second issue on the same branch. If existing worktree changes make creating or switching branches unsafe, preserve them and ask the user how they should be separated before implementation begins.

Use /tdd where possible, at pre-agreed seams.

Run typechecking regularly, single test files regularly, and the full test suite once at the end.

Once done, use /code-review to review the work.

Commit your work to the issue's branch.

Do not close the issue when implementation is complete. Close it only after the
issue branch or its pull request has been merged.
