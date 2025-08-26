# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

### Prompt space


## Project Overview

This is a Kotlin Multiplatform Desktop application built with Compose Multiplatform. It's a nutrition/meal planning application that allows users to manage foods, meals, and multi-day menus with nutritional calculations.
The user is a nutrition professional who will use the app to speed up their workflow.

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

### Key Components

#### Model (`src/desktopMain/kotlin/org/example/project/model/`)
- `Model.kt` - Central singleton managing all application state and data operations
- `Food.kt` - Core food entity supporting both basic foods (with direct macros) and compound foods (composed of other foods)
- `FoodCategory.kt` - Enumeration of food categories (Dairy, Meat & Fish, Eggs, etc.)
- `FoodMacros.kt` - Data class for nutritional macro information (proteins, carbs, fats, water)
- `SizedFood.kt` - Bridge entity linking foods to meals with specific gram quantities
- `Meal.kt` - Meal entity composed of multiple sized foods with calculated nutritional totals
- `DailyMenu.kt` - Daily menu with 5 meal slots (breakfast, snack1, lunch, snack2, dinner)
- `MultiDayMenu.kt` - Multi-day menu plan containing multiple daily menus with average calculations
- `Screen.kt` - Navigation enumeration with icons (Foods, Menus)

#### View Layer (`src/desktopMain/kotlin/org/example/project/view/`)
- `App.kt` - Main application layout with navigation bar and content area
- `components/MyNavBar.kt` - Left sidebar navigation component
- `components/FoodTable.kt` - Reusable table component for food display
- `screens/FoodsScreen.kt` - Food management interface
- `screens/MenusScreen.kt` - Menu management interface
- `dialogs/FoodInputDialog.kt` - Dialog for adding/editing foods
- `dialogs/AddMealDialog.kt` - Dialog for creating meals
- `theme/Theme.kt` - Application theming: always use fonts from MaterialTheme.typography

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

