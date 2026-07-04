# Development

The other docs in this repo ([Getting Started](../getting-started.md), [Core Concepts](../core-concepts.md),
etc.) are written for someone *consuming* the library. This folder is written for someone *working on* it —
whether that's you in six months, or a contributor — and doubles as a walkthrough of the design patterns
behind the v1.1.0 package restructure, for anyone who wants to see them applied in a small, real codebase
rather than a textbook example.

| Page | Covers |
|---|---|
| [Project Setup](project-setup.md) | Module layout, Maven profiles, versioning, the package tour, build constraints |
| [Design Patterns](design-patterns.md) | The patterns introduced during the restructure — what each one is, why it was chosen here, and where to find it in the source |

> [!NOTE]
> These patterns aren't unique to this library — they're common, general-purpose OOP tools. The goal of
> [Design Patterns](design-patterns.md) is to explain each one in general terms first, then point at the
> exact class that uses it, so the concept transfers to other codebases you work on.
