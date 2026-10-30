# revscope

**A web dashboard for Ghidra-powered reverse engineering.** Upload a binary, let
[Ghidra](https://github.com/NationalSecurityAgency/ghidra) analyse it, and browse its
functions and decompiled C in a modern browser UI, instead of the Swing desktop app.

[![CI](https://github.com/niklasmarderx/revscope/actions/workflows/ci.yml/badge.svg)](https://github.com/niklasmarderx/revscope/actions/workflows/ci.yml)
[![License: MIT OR Apache-2.0](https://img.shields.io/badge/license-MIT%20OR%20Apache--2.0-blue.svg)](#license)

## Architecture

```
          ┌──────────────────┐        REST/JSON        ┌───────────────────────────┐
          │  Angular 17 SPA  │  ───────────────────▶   │   Spring Boot 4 backend   │
          │  upload · browse │   /api/analyses …       │   (Java 21)               │
          └──────────────────┘                         │                           │
                                                        │   GhidraService           │
                                                        │   ├─ SampleGhidraService  │  ← default, no install
                                                        │   └─ HeadlessGhidraService│  ← drives analyzeHeadless
                                                        └───────────┬───────────────┘
                                                                    │ ProcessBuilder + post-script
                                                                    ▼
                                                            Ghidra (headless)
```

The backend hides Ghidra behind a `GhidraService` strategy. Out of the box it uses a
built-in **sample backend** that returns realistic analysis, so the whole app runs
without installing Ghidra. Point `ghidra.home` at a real Ghidra install and it switches
to the **headless backend**, which runs `analyzeHeadless` with a bundled post-script and
parses the exported JSON.

## Tech stack

| Layer    | Technology                          |
|----------|-------------------------------------|
| Frontend | Angular 17, TypeScript, RxJS        |
| Backend  | Java 21, Spring Boot 4, Spring MVC  |
| Build    | Maven (backend), npm/Angular CLI    |
| CI       | GitHub Actions (builds both)        |

## Run it locally

**Backend** (port 8080):

```sh
cd backend
./mvnw spring-boot:run
```

**Frontend** (port 4200, proxies `/api` to the backend):

```sh
cd frontend
npm install
npm start
```

Open http://localhost:4200, upload any file, and explore the analysis. No Ghidra
installation is needed for the demo — the sample backend generates the data.

## Wire in a real Ghidra

Install Ghidra (needs JDK 21), then start the backend with:

```sh
cd backend
./mvnw spring-boot:run -Dspring-boot.run.arguments=--ghidra.home=/path/to/ghidra
```

The backend writes each upload to a temp file and runs
`support/analyzeHeadless … -postScript ExportAnalysis.java`
(see [`backend/ghidra-scripts/ExportAnalysis.java`](backend/ghidra-scripts/ExportAnalysis.java)),
then parses the resulting JSON.

## API

| Method | Path                                         | Purpose                        |
|--------|----------------------------------------------|--------------------------------|
| POST   | `/api/analyses`                              | Upload a binary (multipart)    |
| GET    | `/api/analyses`                              | List analyses                  |
| GET    | `/api/analyses/{id}`                         | Analysis detail + functions    |
| GET    | `/api/analyses/{id}/functions/{address}`     | Decompiled code for a function |

## Roadmap

- Interactive call-graph view (D3 / cytoscape).
- String and cross-reference tables.
- Persist analyses in a database instead of in memory.
- AI-assisted function renaming and summaries over the decompiled output.
- Authentication and multi-user projects.

## License

Licensed under either of [Apache License, Version 2.0](LICENSE-APACHE) or
[MIT license](LICENSE-MIT) at your option.
