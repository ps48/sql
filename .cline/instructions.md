# Vibe Coding Instructions & Documentation 🚀

Welcome to our collaborative coding workspace! This document serves as our central hub for project management, task tracking, and contextual instructions.

## 🎯 Current Project Context

**Project:** OpenSearch SQL Plugin - Multivalue Functions
**Repository:** `/Users/sgguruda/work/opensource/repos/sql`
**Focus Area:** Implementing and enhancing multivalue functions for SQL queries

### Active Files & Components
- `multivalue-functions-spec.md` - Specification document
- `mvappend-implementation-plan.md` - Implementation roadmap
- `core/src/test/java/org/opensearch/sql/expression/function/multivalue/MvAppendFunctionTest.java`
- `integ-test/src/test/java/org/opensearch/sql/calcite/remote/CalciteArrayFunctionIT.java`

## 📋 Task Tracking

### 🔄 Current Sprint
- [ ] **Task 1:** Review and enhance multivalue function specifications
- [ ] **Task 2:** Implement MvAppend function improvements
- [ ] **Task 3:** Add comprehensive test coverage
- [ ] **Task 4:** Integration testing with Calcite

### ✅ Completed Tasks
- [x] Created vibe-coding documentation structure
- [x] Established project context and workflow

### 🎯 Upcoming Goals
- [ ] Performance optimization for multivalue operations
- [ ] Documentation updates
- [ ] Code review and refactoring

## 🛠️ Development Workflow

### Plan & Act Mode Usage
1. **Plan Mode:** Use for brainstorming, architecture decisions, and complex problem analysis
2. **Act Mode:** Use for implementation, file modifications, and testing

### Auto-Approve Settings (Recommended)
- ✅ Auto-approve reading project files
- ✅ Max requests: 15-20
- ⚠️ Manual approval for file edits (for review control)

## 📝 Contextual Instructions

### Code Quality Standards
- **Complete Code Only:** Never truncate or omit code sections
- **Confidence Checks:** Rate confidence (1-10) for major decisions
- **Thorough Analysis:** Analyze all relevant files before making changes
- **Documentation:** Keep inline comments and documentation updated

### Project-Specific Guidelines
- Follow OpenSearch coding conventions
- Maintain backward compatibility
- Include comprehensive test coverage
- Update integration tests when adding new functions

### Communication Style
- Be conversational and specific in requests
- Use `@filename` to reference specific files
- Break complex tasks into manageable steps
- Provide clear context for each task

## 🎨 Vibe Coding Principles

### The Flow State
1. **Trust the Process** - Plan thoroughly, then execute confidently
2. **Natural Communication** - Conversational, not robotic interactions
3. **Iterative Improvement** - Continuous feedback and refinement
4. **Controlled Autonomy** - Balance between AI assistance and human oversight
5. **Momentum Maintenance** - Keep the coding flow smooth and productive

### Best Practices
- Start each session with clear objectives
- Reference previous work when continuing tasks
- Use checkpoints for complex implementations
- Maintain project context across sessions

## 📊 Project Metrics & Goals

### Success Criteria
- [ ] All multivalue functions properly implemented
- [ ] 100% test coverage for new functions
- [ ] Performance benchmarks met
- [ ] Documentation complete and accurate
- [ ] Integration tests passing

### Quality Gates
- Code review approval required
- All tests must pass
- Performance regression checks
- Documentation updates verified

## 🔧 Technical Context

### Key Technologies
- **Java** - Primary development language
- **Gradle** - Build system
- **JUnit** - Testing framework
- **OpenSearch** - Search and analytics engine
- **Apache Calcite** - SQL parser and optimizer

### Development Environment
- **IDE:** VSCode with Cline integration
- **Java Version:** [To be determined from project config]
- **Build Tool:** Gradle
- **Testing:** JUnit + Integration tests

## 📚 Resources & References

### Documentation Links
- [OpenSearch SQL Plugin Docs](link-to-docs)
- [Apache Calcite Documentation](link-to-calcite-docs)
- [Project Contributing Guidelines](CONTRIBUTING.md)

### Useful Commands
```bash
# Build the project
./gradlew build

# Run tests
./gradlew test

# Run specific test class
./gradlew test --tests "MvAppendFunctionTest"
```

## 🎯 Session Agenda Template

### Session Start Checklist
- [ ] Review previous session outcomes
- [ ] Identify current objectives
- [ ] Check for any blocking issues
- [ ] Set session goals and priorities

### Session End Summary
- [ ] Document completed tasks
- [ ] Note any issues or blockers
- [ ] Plan next session objectives
- [ ] Update task tracking status

---

## 📊 **Research & Analysis Summary**

### **Documentation Sources Analyzed**
1. **OpenSearch SQL DeepWiki** - Architecture and engine evolution
2. **Apache Calcite Documentation** - Function library and capabilities  
3. **GitHub Issue #3229** - Calcite integration RFC and roadmap
4. **Project Structure Analysis** - Complete codebase exploration

### **Key Findings**

#### **🏗️ Project Architecture**
- **Multi-Engine Design**: V1 (Legacy) → V2 (Current) → V3 (Calcite-Beta)
- **Modular Structure**: 15+ modules with clear separation of concerns
- **Function System**: Centralized registry with category-based organization
- **Missing Component**: No multivalue function support in current engines

#### **🎯 Calcite Integration Status (Issue #3229)**
- **Progress**: 36/42 sub-issues completed (86% done)
- **Status**: V3 engine in beta, available with `plugins.calcite.enabled=true`
- **Opportunity**: Perfect timing for multivalue function integration
- **Benefits**: RBO/CBO optimization, full ANSI SQL, mature function library

#### **📚 Apache Calcite Function Library**
- **Rich Array Support**: 30+ array functions (`ARRAY_APPEND`, `ARRAY_CONCAT`, etc.)
- **Multivalue Operations**: Extensive collection manipulation capabilities
- **SQL Standard**: Full compliance with SQL array specifications
- **Extensibility**: Custom function framework for OpenSearch-specific needs

#### **🔍 Current Multivalue Function State**
- **V2 Engine**: No multivalue function support found
- **Test Files**: Suggest work in progress (`MvAppendFunctionTest.java`)
- **Integration Tests**: `CalciteArrayFunctionIT.java` indicates active development
- **Implementation Gap**: Missing `multivalue/` directory in function structure

### **Strategic Recommendations**

#### **🎯 Implementation Strategy**
1. **Target V3 Engine**: Align with ongoing Calcite integration
2. **Leverage Calcite Functions**: Reuse existing array operations where possible
3. **Custom Extensions**: Implement OpenSearch-specific multivalue functions
4. **Unified API**: Consistent interface across SQL and PPL languages

#### **📍 Implementation Locations**
```
Primary: core/src/main/java/org/opensearch/sql/expression/function/multivalue/
Calcite: core/src/main/java/org/opensearch/sql/calcite/
Tests:   integ-test/src/test/java/org/opensearch/sql/calcite/remote/
         core/src/test/java/org/opensearch/sql/expression/function/multivalue/
```

## 📝 Session Notes

### [Date: 2025-01-21] - Initial Setup & Research
**Objectives:** 
- Establish vibe-coding workflow and documentation structure
- Analyze project structure and multivalue function opportunities
- Research Calcite integration and function capabilities

**Completed:** 
- ✅ Created instructions.md framework
- ✅ Established project context and workflow principles
- ✅ Analyzed complete project hierarchy (15+ modules)
- ✅ Researched OpenSearch SQL DeepWiki documentation
- ✅ Analyzed Apache Calcite function library (30+ array functions)
- ✅ Reviewed GitHub Issue #3229 (Calcite integration RFC)
- ✅ Created comprehensive project-hierarchy.md documentation
- ✅ Identified multivalue function implementation opportunities

**Key Discoveries:**
- V3 Calcite engine is 86% complete (36/42 sub-issues done)
- Apache Calcite has extensive array function support ready to leverage
- No existing multivalue function support in current V2 engine
- Perfect timing to integrate multivalue functions with V3 engine development

**Next Steps:**
- Create multivalue-calcite-integration-plan.md
- Analyze existing spec files in detail
- Develop implementation roadmap aligned with V3 engine
- Plan function mapping between Calcite arrays and OpenSearch multivalue needs

---

*Last Updated: January 21, 2025*
*Maintainer: Collaborative Cline & Human Team*

> 💡 **Tip:** Keep this document updated as we progress through the project. It's our shared memory and roadmap!
> 
> 🎯 **Current Focus:** Multivalue functions integration with Calcite V3 engine - excellent timing for this initiative!
