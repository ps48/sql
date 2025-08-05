# OpenSearch SQL Project Hierarchy & Architecture

## 📁 **Top-Level Project Structure**

```
opensearch-sql/
├── 📋 Project Configuration
│   ├── build.gradle                    # Main build configuration
│   ├── settings.gradle                 # Gradle settings
│   ├── gradle.properties              # Build properties
│   └── gradlew, gradlew.bat           # Gradle wrapper scripts
│
├── 📚 Documentation & Governance
│   ├── README.md                      # Project overview
│   ├── CONTRIBUTING.md                # Contribution guidelines
│   ├── DEVELOPER_GUIDE.rst           # Developer documentation
│   ├── MAINTAINERS.md, ADMINS.md     # Project governance
│   ├── CODE_OF_CONDUCT.md            # Community guidelines
│   ├── SECURITY.md                   # Security policies
│   └── LICENSE.txt, NOTICE, THIRD-PARTY # Legal documents
│
├── 🏗️ Core Modules
│   ├── core/                         # Core SQL engine (V2)
│   ├── legacy/                       # Legacy SQL engine (V1)
│   ├── opensearch/                   # OpenSearch integration layer
│   ├── plugin/                       # Main plugin entry point
│   ├── ppl/                         # PPL (Piped Processing Language)
│   ├── sql/                         # SQL-specific components
│   └── protocol/                    # Communication protocols
│
├── 🔧 Advanced Features
│   ├── async-query/                 # Asynchronous query execution
│   ├── async-query-core/            # Async query core components
│   ├── datasources/                 # Multi-datasource support
│   ├── prometheus/                  # Prometheus integration
│   └── spark/                       # Spark integration
│
├── 🧪 Testing & Quality
│   ├── integ-test/                  # Integration tests
│   ├── benchmarks/                  # Performance benchmarks
│   ├── doctest/                     # Documentation tests
│   └── build-tools/                 # Build and testing tools
│
├── 📖 Documentation
│   ├── docs/                        # User and developer docs
│   └── release-notes/               # Version release notes
│
└── 🛠️ Build & Development
    ├── buildSrc/                    # Custom Gradle plugins
    ├── gradle/                      # Gradle wrapper files
    ├── scripts/                     # Build and deployment scripts
    └── common/                      # Shared utilities
```

## 🏛️ **Core Architecture Overview**

### **Query Engine Evolution**
```
V1 (Legacy)     V2 (Current)      V3 (Calcite - Beta)
    ↓               ↓                    ↓
SQL Parser  →  Enhanced Parser  →  Calcite Parser
DSL Trans.  →  Semantic Analyzer →  Calcite Optimizer
Executor    →  Physical Planner  →  Calcite Executor
            →  OpenSearch API    →  OpenSearch API
```

### **Query Processing Pipeline**
```
SQL/PPL Query
     ↓
   Parser (ANTLR4)
     ↓
Abstract Syntax Tree (AST)
     ↓
Semantic Analyzer
     ↓
Logical Plan
     ↓
┌─────────────────┬─────────────────┐
│   V2 Engine     │   V3 Engine     │
│                 │   (Calcite)     │
├─────────────────┼─────────────────┤
│ Physical Plan   │ RelNode Plan    │
│      ↓          │      ↓          │
│ Execution Eng.  │ Calcite Exec.   │
└─────────────────┴─────────────────┘
     ↓                    ↓
OpenSearch API    OpenSearch API
     ↓                    ↓
   Results              Results
```

## 📦 **Module Deep Dive**

### **Core Module (`core/`)**
```
core/
├── src/main/java/org/opensearch/sql/
│   ├── analysis/                    # Query analysis & validation
│   │   ├── Analyzer.java           # Main analyzer
│   │   ├── ExpressionAnalyzer.java # Expression analysis
│   │   └── TypeEnvironment.java    # Type checking
│   │
│   ├── ast/                        # Abstract Syntax Tree
│   │   ├── expression/             # Expression nodes
│   │   ├── statement/              # Statement nodes
│   │   └── tree/                   # Tree structures
│   │
│   ├── calcite/                    # Calcite integration (V3)
│   │   ├── CalcitePlanContext.java
│   │   ├── CalciteRelNodeVisitor.java
│   │   └── OpenSearchSchema.java
│   │
│   ├── data/                       # Data models & types
│   │   ├── model/                  # Data value models
│   │   ├── type/                   # Type system
│   │   └── utils/                  # Data utilities
│   │
│   ├── executor/                   # Query execution
│   │   ├── ExecutionEngine.java    # Main execution engine
│   │   ├── QueryManager.java       # Query lifecycle
│   │   └── execution/              # Execution strategies
│   │
│   ├── expression/                 # Expression system
│   │   ├── Expression.java         # Base expression
│   │   ├── FunctionExpression.java # Function expressions
│   │   ├── aggregation/            # Aggregation functions
│   │   ├── conditional/            # Conditional expressions
│   │   ├── datetime/               # Date/time functions
│   │   ├── function/               # 🎯 FUNCTION DEFINITIONS
│   │   ├── operator/               # Operators
│   │   ├── text/                   # Text functions
│   │   └── window/                 # Window functions
│   │
│   ├── planner/                    # Query planning
│   │   ├── Planner.java           # Main planner
│   │   ├── logical/               # Logical planning
│   │   ├── physical/              # Physical planning
│   │   └── optimizer/             # Query optimization
│   │
│   └── storage/                    # Storage abstraction
       ├── StorageEngine.java      # Storage interface
       └── Table.java              # Table abstraction
│
└── src/test/java/                  # Test mirror structure
```

### **Function System Architecture (`core/src/main/java/org/opensearch/sql/expression/function/`)**
```
function/
├── 📋 Core Function Framework
│   ├── BuiltinFunctionRepository.java    # Central function registry
│   ├── FunctionDSL.java                 # Function definition DSL
│   ├── OpenSearchFunctions.java         # OpenSearch-specific functions
│   ├── DefaultFunctionResolver.java     # Function resolution
│   ├── FunctionBuilder.java             # Function construction
│   ├── FunctionSignature.java           # Function signatures
│   └── PPLBuiltinOperators.java         # PPL operators
│
├── 🔧 Function Implementation Types
│   ├── FunctionImplementation.java      # Base implementation
│   ├── ScalarUDF.java                  # Scalar functions
│   ├── TableFunctionImplementation.java # Table functions
│   └── SerializableFunction.java        # Serializable functions
│
├── 📁 Function Categories
│   ├── CollectionUDF/                   # Array/collection functions
│   │   ├── ArrayFunctionImpl.java      # Array operations
│   │   ├── ExistsFunctionImpl.java     # Existence checks
│   │   ├── FilterFunctionImpl.java     # Array filtering
│   │   ├── TransformFunctionImpl.java  # Array transformations
│   │   └── ReduceFunctionImpl.java     # Array reductions
│   │
│   ├── jsonUDF/                        # JSON functions
│   │   ├── JsonExtractFunctionImpl.java
│   │   ├── JsonAppendFunctionImpl.java
│   │   └── JsonUtils.java
│   │
│   └── udf/                            # User-defined functions
│       ├── datetime/                   # Date/time functions
│       ├── math/                       # Mathematical functions
│       ├── condition/                  # Conditional functions
│       └── ip/                         # IP address functions
│
└── 🎯 MISSING: multivalue/              # ⚠️ Multivalue functions (TO BE IMPLEMENTED)
```

### **Plugin Module (`plugin/`)**
```
plugin/src/main/java/org/opensearch/sql/plugin/
├── SQLPlugin.java                      # Main plugin class
├── rest/                              # REST API endpoints
│   ├── RestPPLQueryAction.java        # PPL query endpoint
│   ├── RestSQLQueryAction.java        # SQL query endpoint
│   └── RestQuerySettingsAction.java   # Settings endpoint
└── transport/                         # Transport layer
    └── TransportPPLQueryAction.java   # PPL transport
```

### **Integration Test Structure (`integ-test/`)**
```
integ-test/src/test/java/org/opensearch/sql/
├── calcite/                           # Calcite engine tests
│   └── remote/
│       └── CalciteArrayFunctionIT.java # 🎯 Array function tests
├── ppl/                              # PPL integration tests
├── sql/                              # SQL integration tests
└── legacy/                           # Legacy engine tests
```

## 🔄 **Engine Comparison Matrix**

| Feature | V1 (Legacy) | V2 (Current) | V3 (Calcite) |
|---------|-------------|--------------|--------------|
| **SQL Support** | Basic | Enhanced | Full ANSI SQL |
| **PPL Support** | ❌ | ✅ | ✅ Enhanced |
| **Complex Queries** | Limited | Better | Advanced |
| **JOIN Operations** | Basic | Limited | Full Support |
| **Subqueries** | ❌ | Limited | Full Support |
| **Query Optimization** | Basic | Rule-based | RBO + CBO |
| **Function Library** | Limited | Extended | Calcite + Custom |
| **Multivalue Functions** | ❌ | ❌ | 🎯 **OPPORTUNITY** |
| **Status** | Deprecated | Production | Beta (3.0.0) |

## 🎯 **Multivalue Functions Integration Points**

### **Current State Analysis**
- **V2 Engine**: No multivalue function support found
- **V3 Engine**: Perfect opportunity for integration
- **Calcite Library**: Rich array function support available
- **Test Infrastructure**: `CalciteArrayFunctionIT.java` suggests active development

### **Integration Strategy**
1. **Leverage Calcite Functions**: Use existing `ARRAY_*` functions
2. **Custom OpenSearch Functions**: Implement OpenSearch-specific multivalue operations
3. **Unified API**: Provide consistent interface across SQL and PPL
4. **Backward Compatibility**: Ensure V2 compatibility where needed

### **Implementation Locations**
```
📍 Primary Implementation:
   core/src/main/java/org/opensearch/sql/expression/function/multivalue/

📍 Calcite Integration:
   core/src/main/java/org/opensearch/sql/calcite/

📍 Test Coverage:
   integ-test/src/test/java/org/opensearch/sql/calcite/remote/
   core/src/test/java/org/opensearch/sql/expression/function/multivalue/

📍 Documentation:
   docs/user/ppl/functions/
   docs/user/sql/functions/
```

## 🚀 **Development Workflow**

### **Build Commands**
```bash
# Build entire project
./gradlew build

# Run tests
./gradlew test

# Run integration tests
./gradlew integTest

# Run specific test class
./gradlew test --tests "MvAppendFunctionTest"

# Enable Calcite engine for testing
# Set: plugins.calcite.enabled=true
```

### **Key Configuration Files**
- `opensearch.yml`: OpenSearch configuration
- `gradle.properties`: Build properties
- `lombok.config`: Code generation settings

---

*Last Updated: January 21, 2025*
*Context: OpenSearch SQL Plugin Architecture Analysis*
