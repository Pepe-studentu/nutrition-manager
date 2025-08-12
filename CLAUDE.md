# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Kotlin Multiplatform Desktop application built with Compose Multiplatform. It's a nutrition/meal planning application that allows users to manage ingredients, meals, and daily menus with nutritional calculations.
The user is a nutrition professional who will use the app to speed up his workflow.

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
- **Data Persistence**: JSON file-based storage for ingredients, meals, and menus

### Key Components

#### Model (`src/desktopMain/kotlin/org/example/project/model/`)
- `Model.kt` - Central singleton managing all application state and data operations
- `Ingredient.kt`, `Meal.kt`, `DailyMenu.kt` - Data classes representing core entities
- `SizedIngredient.kt` - Bridge entity linking ingredients to meals with quantities
- `Screen.kt` - Navigation enumeration with icons

#### View Layer (`src/desktopMain/kotlin/org/example/project/view/`)
- `App.kt` - Main application layout with navigation bar and content area
- `MyNavBar.kt` - Left sidebar navigation component
- Screen-specific components: `IngredientsScreen.kt`, `MealScreen.kt`, `MenuScreen.kt`
- Dialog components: `AddMealDialog.kt`, `AddMenuDialog.kt`, `InsertDialog.kt`
- `IngredientTable.kt` - Reusable table component for ingredient display

### Data Flow
1. Application loads data from JSON files on startup (ingredients.json, meals.json, menus.json)
2. Model provides reactive state through Compose's mutableStateOf/mutableStateListOf
3. UI components observe Model state directly and trigger CRUD operations
4. All changes are immediately persisted to JSON files

### Navigation
Uses a simple enum-based navigation system with a sidebar. Screens include:
- Ingredients: Manage nutritional ingredients database
- Meals: Compose meals from ingredients with portions
- Menus: Create daily menus from meals
- MultiMenus: Placeholder for future functionality

## Development Notes

### State Management
The application uses a centralized Model singleton pattern rather than ViewModels or dependency injection. All state is managed reactively through Compose state primitives.

### Data Validation
- Nutritional values must be non-negative and sum to ≤100%
- Referential integrity enforced (can't delete ingredients used in meals, meals used in menus)
- Input validation with proper error handling

### File Structure
- Main entry point: `Main.kt`
- All source code in `src/desktopMain/kotlin/org/example/project/`
- Resources in `src/desktopMain/composeResources/`
- JSON data files stored at project root

## Planning
 - the 8 categories are: diary, meat&fish, eggs, veggies, cereals&legumes,
sugars, fats, drinks
 - yes, recursive macros for compound foods
 - yes, for frequency counting, only within menus
 - the dialog will be the add meal dialog, which is already implemented (albeit
in a very not polished form, and it needs help with the ingredient -> food 
replacement. There is a selection part to the left and a quantity input 
to the right, where the macros for the whole food are visible)
 - no, the content of the JSON storage is there because I needed testing, so
no migration is needed

### UI restructuring
key thing: keep it minimal. That is, no need for a polished look, not at all.
In this stage of the development, I want a functional app. A good looking one
has a lot of code, in my opinion, and risks being changed a lot. For example,
the ui code I have now focuses more on the looks than needed. This is why, please
keep the ui code compact and clean. Later, it will be polished after we have
the app working as intended. 

For now, let's focus on these parts.
