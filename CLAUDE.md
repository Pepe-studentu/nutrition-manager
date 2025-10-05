# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

### Prompt space

/
## Project Overview
This is a Kotlin Multiplatform Desktop application built with Compose Multiplatform. It's a nutrition/meal planning application that allows users to manage foods, meals, and multi-day menus with nutritional calculations.
The user is a nutrition professional who will use the app to speed up their workflow.

### Target User & Workflow
- **Primary User**: Nutrition professional (dietician, nutritionist)
- **Main Goal**: Speed up client meal planning workflow
- **Key Activities**: Create food database, design meals, plan multi-day menus, generate professional PDFs for clients

## Build and Development Commands

```bash
# Build the application
./gradlew build

# Run the desktop application
./gradlew composeApp:run

# Run tests
./gradlew test

# Clean build artifacts
./gradlew clean

# Create distribution packages
./gradlew packageDistributionForCurrentOS
```

## Architecture Overview

### Core Structure
- **Model Layer**: Centralized state management using a singleton `Model` object with reactive Compose state
- **View Layer**: Compose UI components organized by screen functionality
- **Data Persistence**: JSON file-based storage for foods, meals, and multi-day menus

## Detailed Architecture Analysis

### Data Model Layer (`src/desktopMain/kotlin/org/example/project/model/`)

#### Core Entities
- **Model.kt** (519 lines) - The heart of the application
  - Singleton pattern with reactive Compose state (`mutableStateListOf`, `mutableStateOf`)  
  - Manages all CRUD operations with immediate JSON persistence
  - Implements macro calculation caching with recursive resolution for compound foods
  - Handles search/filtering with relevance scoring (exact match > prefix > contains)
  - Synchronous sorting with pre-calculated macros to avoid repeated computation
  - Usage count tracking for referential integrity (prevents deletion of used foods/meals)
  - Current JSON files: `foods.json`, `meals.json`, `multi_day_menus.json` in composeApp/ directory

- **Food.kt** (84 lines) - Supports two food types
  - **Basic Foods**: Direct macro values (proteins, carbs, fats, waterMassPercentage)
  - **Compound Foods**: Composed of other foods with percentage-based composition
  - Effective categories/tags inheritance system for compound foods (inherits from components)
  - Validation ensures macro values ≤100% total and non-negative
  - Caching system for effective categories/tags (cleared when foods modified)
  - Usage count for tracking references from meals/other foods

- **FoodMacros.kt** - Nutritional data container (proteins, carbs, fats, water percentage)
- **SizedFood.kt** - Bridge entity linking foods to meals with gram quantities
- **Meal.kt** - Collection of sized foods with calculated totals and description
- **DailyMenu.kt** - 5 meal slots (breakfast, snack1, lunch, snack2, dinner) with calculated nutritionals
- **MultiDayMenu.kt** - Multi-day plan with averages calculation
- **Screen.kt** - Navigation enum (Foods, Menus) with Material icons

#### Search & Sorting Architecture
Model.kt implements sophisticated search with relevance scoring:
- **Exact name match**: 100 points
- **Name prefix**: 50 points  
- **Name contains**: 20 points
- **Category/tag matches**: 30/25 points respectively
- **Component matches**: 15/5 points
- **Usage count bonus**: Added to all scores
- **Default sort**: By usage count (descending) when no query
- **Macro sort**: Pre-calculates all macros once to avoid repeated recursive calls

### View Layer Architecture (`src/desktopMain/kotlin/org/example/project/view/`)

#### Screen Architecture Pattern
Both major screens follow a consistent ViewState pattern:

**FoodsScreen.kt** (214 lines):
- Uses `FoodsViewState` data class for centralized state management
- Single `updateFoods()` function coordinates search/sort operations
- Search triggers re-filtering through Model.filterFoods()
- Sorting uses SortState enum (NONE, ASCENDING, DESCENDING) with 3-step cycle
- Food table with sortable columns, edit/delete actions
- Dialog state management for add/edit/delete operations
- Snackbar feedback for user actions

**MenusScreen.kt** (374 lines):
- Uses `MenusViewState` data class with complex cell selection state
- Global cell selection system: `Triple<String, Int, Int>` (menuId, dayIndex, mealIndex)
- Floating toolbar appears at screen bottom when cell selected
- Context-aware dialogs (add meal vs edit existing meal)
- Menu creation/deletion with confirmation dialogs
- PDF generation integration via MenuPrintService

#### Component Organization
- **FoodTable.kt**: Reusable sortable table with recursive compound food display
- **MultiDayMenuCard.kt**: Expandable cards containing MenuGrid components
- **MenuGrid.kt**: Complex grid layout for daily meal planning
- **MyNavBar.kt**: Sidebar navigation with screen switching
- **SortableHeaderCell.kt**: Reusable header with sort indicators

#### Dialog System
- **FoodInputDialog.kt**: Handles both adding new foods and editing existing ones
- **AddMealDialog.kt**: Create/edit meals with food selection and quantities
- **MenuDialogs.kt**: Multi-day menu creation dialogs
- Consistent pattern: onDismiss + action callbacks, snackbar integration

### Service Layer (`src/desktopMain/kotlin/org/example/project/service/`)

#### MenuPrintService.kt (262 lines)
- PDF generation using OpenHTMLToPDF library
- HTML template system with fallback inline template
- Landscape A4 format optimized for menu tables
- XHTML compliance preprocessing
- Automatic file naming with timestamp
- Desktop integration (opens PDF with default viewer)
- **Output location**: `~/NutritionApp/MenuPDFs/` (auto-created if doesn't exist)

### Technology Stack & Dependencies
- **Core**: Kotlin Multiplatform + Compose Multiplatform (Material 3)
- **Serialization**: kotlinx.serialization for JSON persistence  
- **PDF**: OpenHTMLToPDF (pdfbox + slf4j) for menu printing
- **Coroutines**: kotlinx-coroutines-swing for async operations
- **Testing**: JUnit + Kotlin test utilities with comprehensive test coverage

### Key Architectural Decisions

#### State Management Philosophy
- **No ViewModels**: Direct Model observation in Composables
- **Immediate persistence**: All changes saved to JSON immediately
- **Reactive updates**: Compose state primitives for UI reactivity
- **Centralized validation**: All business rules in Model layer

#### Performance Optimizations
- **Macro caching**: Prevents repeated recursive calculations
- **Pre-calculated sorting**: Computes all macros once before sorting
- **Usage-based default sorting**: Frequently used foods appear first
- **Effective inheritance caching**: Categories/tags cached for compound foods

#### Data Integrity System
- **Referential integrity**: Cannot delete foods/meals that are in use
- **Usage counting**: Tracks how many times foods are referenced
- **Validation on mutations**: All create/update operations validated
- **Orphan prevention**: Cleanup prevents orphaned meals when removed from menus

### Current Technical Debt & Opportunities
- **PDF output configuration**: Currently saves to `~/NutritionApp/MenuPDFs/` (could add user-configurable path)
- **File organization**: Some large files (Model.kt, MenusScreen.kt) could be split
- **Error handling**: Could be more granular for better user feedback  
- **Testing coverage**: Good model coverage, could expand UI testing
- **Internationalization**: Currently English-only
- **Configuration**: Hard-coded paths and settings could be configurable

### Data Flow
1. Application loads data from JSON files on startup (foods.json, meals.json, multi_day_menus.json)
2. Model provides reactive state through Compose's mutableStateOf/mutableStateListOf
3. UI components observe Model state directly and trigger CRUD operations
4. All changes are immediately persisted to JSON files
5. Macro calculations are cached and computed recursively for compound foods

### Food System
- **Basic Foods**: Have direct macro values (proteins, carbs, fats, water percentage)
- **Compound Foods**: Composed of other foods with percentage-based composition
- **Usage Tracking**: Foods track usage count for referential integrity
- **Validation**: Ensures macro values are non-negative and don't exceed 100% total

### Navigation
Uses a simple enum-based navigation system with a sidebar. Screens include:
- Foods: Manage nutritional foods database (basic and compound foods)
- Menus: Create and manage multi-day menus with daily meal planning

## Development Notes

### State Management
The application uses a centralized Model singleton pattern rather than ViewModels or dependency injection. All state is managed reactively through Compose state primitives.

### Data Validation
- Nutritional values must be non-negative and sum to ≤100% for basic foods
- Compound food components must have positive percentages
- Referential integrity enforced (can't delete foods used in other foods/meals, meals used in menus)
- Input validation with proper error handling

### Macro Calculation
- Uses recursive calculation with caching for performance
- Compound foods calculate macros by aggregating component food macros
- Cache is cleared when foods are modified to ensure accuracy

### File Structure
- Main entry point: `Main.kt`
- All source code in `src/desktopMain/kotlin/org/example/project/`
- Resources in `src/desktopMain/composeResources/`
- JSON data files stored in `composeApp/` directory

## Claude Workflow Instructions

### Change Detection and Review
When the user mentions changes or asks to review them:
1. **Check CLAUDE.md changes**: I can detect changes automatically without git
2. **Use conservative git diff**: `git diff --unified=3 --no-prefix --ignore-cr-at-eol HEAD~1`
3. **Check current status**: `git status` for unstaged changes
4. **Search for CLAUDE comments**: `git grep "// claude:"` at every prompt

### Inline Code Instructions
- User will place contextual instructions as `// claude:` comments in code files
- I must search for these comments at every prompt and follow them
- Instructions are context-specific and placed where relevant in the code

### Branch Strategy
- **Development branch**: Use for frequent small commits and active development
- **Main branch**: Keep clean with meaningful commits only
- User will explicitly indicate when to commit to main branch
- Development branch allows tracking incremental changes via diffs

### Comment Cleanup
Before committing, remove CLAUDE comments with:
```bash
git grep -l "// claude:" | xargs sed -i '/\/\/ claude:/d'
```

### File Management
- CLAUDE.md is gitignored to avoid polluting commits
- Instructions primarily placed in CLAUDE.md
- Inline comments used for context-specific guidance only

### Token Efficiency
- Use targeted diffs instead of reading entire files
- Focus on changed sections only
- Leverage git commands for efficient change detection

### UI Code Organization Principles
When working with Compose UI code, follow these organization principles:

- **Prefer shorter files**: 650+ lines in a single file is too much - break into smaller, focused files
- **Extract composables frequently**: Create separate composables to maintain readability and reusability
- **Use aggregation over composition**: Create dedicated composables like `DeleteStuffDialog` instead of writing dialog code inline
- **Avoid long composables with deep nesting**: Break complex composables into smaller, focused components
- **Prioritize readability**: Even when compiled output is the same, readable code is easier to understand and maintain
- **Keep UI code modular**: Well-separated composables are easier to test, reuse, and modify
- **Separate concerns**: Dialog state and logic should be contained within appropriate scope levels

