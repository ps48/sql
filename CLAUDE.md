# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Development Commands

### Build and Test
- `./gradlew build` - Complete build including all tests and checks
- `./gradlew assemble` - Generate jar and zip files in build/distributions
- `./gradlew test` - Run unit tests
- `./gradlew :integ-test:integTest` - Run integration tests (takes time)
- `./gradlew :integ-test:yamlRestTest` - Run REST integration tests
- `./gradlew :doctest:doctest` - Run doctests

### Code Quality
- `./gradlew spotlessCheck` - Check code formatting (Google Java Format)
- `./gradlew spotlessApply` - Auto-apply formatting fixes
- `./gradlew jacocoTestReport` - Generate test coverage reports
- `./gradlew pitest` - Run PiTest mutation testing

### Module-specific Commands
- `./gradlew :<module_name>:build` - Build specific module (e.g., `:core:build`)
- `./gradlew :integ-test:integTest -Dtests.class="*QueryIT"` - Run specific integration test

### Local Development
- `./gradlew :plugin:run` - Start OpenSearch with plugin installed for quick testing
- `./gradlew generateGrammarSource` - Regenerate ANTLR parser from grammar files

## Architecture Overview

OpenSearch SQL is a multi-engine query processor supporting both SQL and PPL (Piped Processing Language):

### Core Modules
- **plugin/**: OpenSearch plugin integration layer
- **core/**: Core query engine with execution framework, expression evaluation, and data types
- **sql/**: SQL language parser using ANTLR grammar
- **ppl/**: PPL language parser and AST builder
- **opensearch/**: OpenSearch storage engine adapter
- **prometheus/**: Prometheus storage engine for metrics queries
- **common/**: Shared utilities and data structures
- **protocol/**: Request/response protocol formatting

### Query Processing Flow
1. **Parsing**: ANTLR parsers convert SQL/PPL to AST
2. **Analysis**: Semantic analyzer validates syntax and types
3. **Planning**: Creates logical plans, applies optimizations, generates physical plans
4. **Execution**: Executes physical plans against storage engines
5. **Formatting**: Converts results to requested format (JDBC, CSV, etc.)

### Engine Versions
- **V2 Engine**: Current production engine with ANTLR + Druid dual parsing
- **V3 Engine**: Next-generation using Apache Calcite for optimization and execution
- Fallback mechanism ensures backward compatibility between engine versions

## Key Development Areas

### Grammar Files
- Located in `*/src/main/antlr/` directories
- SQL grammar: `OpenSearchSQLLexer.g4`, `OpenSearchSQLParser.g4`
- PPL grammar: `OpenSearchPPLLexer.g4`, `OpenSearchPPLParser.g4`
- Run `./gradlew generateGrammarSource` after grammar changes

### Expression System
- Core expression framework in `core/src/main/java/org/opensearch/sql/expression/`
- Function implementations in `expression/function/` with UDF support
- Type system in `data/type/` with automatic type conversion

### Storage Engines
- OpenSearch adapter handles index queries and aggregations
- Prometheus adapter for metrics and time-series data
- Storage abstraction allows multiple backends

### Testing Strategy
- Unit tests for individual components
- Integration tests spin up in-memory OpenSearch cluster
- Comparison tests validate against reference implementations
- Doctest ensures documentation examples stay current

## Common Patterns

### Adding New Functions
1. Implement function logic in appropriate `expression/function/` package
2. Register in `BuiltinFunctionRepository`
3. Add tests in corresponding test package
4. Update documentation if user-facing

### Parser Extensions
1. Modify grammar files in `antlr/` directory
2. Update AST builders to handle new syntax
3. Extend semantic analyzer for validation
4. Add integration tests for end-to-end validation

### Storage Engine Extensions
1. Implement `StorageEngine` interface
2. Create table scan and write builders
3. Register in plugin configuration
4. Add comprehensive integration tests

## Important Notes

- Java 21 compatibility required
- Uses Lombok for reducing boilerplate - ensure IDE plugin installed
- Follows Google Java Format code style (enforced by Spotless)
- All changes require DCO sign-off for contribution
- Security analysis welcome - this is defensive security focused codebase