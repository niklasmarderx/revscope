# revscope

**A web dashboard for Ghidra-powered reverse engineering.** Upload a binary, let
[Ghidra](https://github.com/NationalSecurityAgency/ghidra) analyse it, and browse its
functions, decompiled C, strings, imports and call graph in the browser — with optional
AI-assisted function naming — instead of the Swing desktop app.

[![CI](https://github.com/niklasmarderx/revscope/actions/workflows/ci.yml/badge.svg)](https://github.com/niklasmarderx/revscope/actions/workflows/ci.yml)
[![License: MIT OR Apache-2.0](https://img.shields.io/badge/license-MIT%20OR%20Apache--2.0-blue.svg)](#license)

## Features

- **Upload & analyse** any binary through the web UI or the REST API.
- **Function explorer** with the decompiled C for each function.
- **Strings, imports and an interactive call graph** (cytoscape) per binary.
- **AI explain** — suggest a descriptive name and a one-line summary for a function.
- **Two Ghidra backends** behind one interface: a built-in sample backend (works with
  no install) and a real headless-Ghidra driver.
- **Persistence** — analyses are stored (JPA + H2) and survive restarts.

## Architecture

```
          ┌──────────────────┐        REST/JSON        ┌───────────────────────────┐
          │  Angular 17 SPA  │  ───────────────────▶   │   Spring Boot 4 backend   │
          │ upload · explore │   /api/analyses …       │   (Java 21)               │
          │ graph · AI       │                         │                           │
          └──────────────────┘                         │  GhidraService            │
                                                        │  ├─ SampleGhidraService   │ ← default, no install
                                                        │  └─ HeadlessGhidraService │ ← analyzeHeadless
                                                        │  AiService                │
                                                        │  ├─ StubAiService         │ ← default, heuristic
                                                        │  └─ AnthropicAiService    │ ← Claude (opus-5-5)
                                                        │  AnalysisStore → JPA/H2   │
                                                        └───────────┬───────────────┘
                                                                    │ ProcessBuilder + ExportAnalysis.java
                                                                    ▼
                                                            Ghidra (headless)
```

Each heavy integration hides behind a strategy interface with a zero-config default, so
the whole app runs out of the box and upgrades to the real thing when you configure it:
set `ghidra.home` for real analysis, `anthropic.api-key` for real AI.

## Tech stack

| Layer     | Technology                                      |
|-----------|-------------------------------------------------|
| Frontend  | Angular 17, TypeScript, RxJS, cytoscape         |
| Backend   | Java 21, Spring Boot 4, Spring MVC, Spring Data JPA |
| Storage   | H2 (file-based)                                 |
| AI        | Anthropic Java SDK (`claude-opus-5-5`)          |
| Build/CI  | Maven, npm/Angular CLI, GitHub Actions          |

## Run it locally

**Backend** (port 8080):

```sh
cd backend
./mvnw spring-boot:run
```

**Frontend** (port 4200, proxies `/api` to the backend):

```sh
cd frontend
npm install     # if node_modules is missing
npm start
```

Open http://localhost:4200, upload any file, and explore. With no configuration the
sample backend generates realistic data, so no Ghidra install is needed for the demo.

## Real Ghidra analysis

Point `ghidra.home` at a built Ghidra installation (tested with Ghidra 11.4.3 on JDK 21):

```sh
cd backend
./mvnw spring-boot:run \
  -Dspring-boot.run.arguments="--ghidra.home=/path/to/ghidra --ghidra.script-path=$(pwd)/ghidra-scripts/ExportAnalysis.java"
```

The backend writes each upload to a temp file and runs
`support/analyzeHeadless … -postScript ExportAnalysis.java`
(see [`backend/ghidra-scripts/ExportAnalysis.java`](backend/ghidra-scripts/ExportAnalysis.java)),
then parses the exported JSON. Verified end-to-end against `/bin/ls`: 136 functions with
decompiled C, 306 strings, 91 imports and 201 call-graph edges.

### Docker (Ghidra bundled)

The [`Dockerfile`](Dockerfile) builds a runtime image with Ghidra 11.4.3 bundled, so real
analysis works with one command:

```sh
docker build -t revscope .
docker run -p 8080:8080 revscope
```

## AI-assisted naming

Without a key, a heuristic stub suggests names. To use Claude, set an API key:

```sh
export ANTHROPIC_API_KEY=sk-ant-...
# or pass --anthropic.api-key=... to the backend
```

## API

| Method | Path                                             | Purpose                        |
|--------|--------------------------------------------------|--------------------------------|
| POST   | `/api/analyses`                                  | Upload a binary (multipart)    |
| GET    | `/api/analyses`                                  | List analyses                  |
| GET    | `/api/analyses/{id}`                             | Detail: functions, strings, imports, call graph |
| GET    | `/api/analyses/{id}/functions/{address}`         | Decompiled code for a function |
| POST   | `/api/analyses/{id}/functions/{address}/explain` | AI name + summary for a function |

## Roadmap

- Async analysis with progress for large binaries.
- Authentication and multi-user projects.
- Diffing two analyses (BinDiff-style).

## License

Licensed under either of [Apache License, Version 2.0](LICENSE-APACHE) or
[MIT license](LICENSE-MIT) at your option.
