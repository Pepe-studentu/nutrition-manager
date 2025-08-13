# Nutrition Meal Planning Application

A Kotlin Multiplatform Desktop application built with Compose Multiplatform for nutrition professionals to efficiently manage foods, create meals, and plan multi-day menus with detailed nutritional calculations.

## Features

- **Food Management**: Create and manage both basic foods (with direct macro values) and compound foods (composed of other foods)
- **Nutritional Categories**: Organize foods into 8 predefined categories (Dairy, Meat & Fish, Eggs, Vegetables, Cereals & Legumes, Sugars, Fats, Drinks)
- **Meal Creation**: Compose meals from foods with specific quantities
- **Multi-Day Menu Planning**: Create comprehensive menu plans spanning multiple days
- **Nutritional Calculations**: Automatic calculation of proteins, carbs, fats, calories, and water content
- **Usage Tracking**: Track food usage frequency for better organization
- **Data Persistence**: All data automatically saved to JSON files

## Architecture

### Food System
- **Basic Foods**: Foods with directly specified macro values (proteins, carbs, fats, water percentage)
- **Compound Foods**: Foods composed of other foods with percentage-based composition
- **Recursive Calculation**: Nutritional values computed recursively with caching for performance
- **Validation**: Ensures data integrity and proper nutritional value ranges

### Menu Structure
- **Daily Menus**: 5 meal slots per day (breakfast, snack1, lunch, snack2, dinner)
- **Multi-Day Menus**: Collections of daily menus with average nutritional calculations
- **Flexible Planning**: Support for varying menu durations (7, 14, 30+ days)

## Getting Started

### Prerequisites
- JDK 11 or higher
- Gradle (included via wrapper)

### Running the Application

```bash
# Build the application
./gradlew build

# Run the desktop application
./gradlew composeApp:run

# Create distribution packages
./gradlew packageDistributionForCurrentOS
```

### Development Commands

```bash
# Run tests
./gradlew test

# Clean build artifacts
./gradlew clean
```

## Project Structure

```
src/desktopMain/kotlin/org/example/project/
├── model/          # Data models and business logic
│   ├── Model.kt    # Central state management
│   ├── Food.kt     # Food entity
│   ├── Meal.kt     # Meal composition
│   └── ...
├── view/           # UI components
│   ├── App.kt      # Main application
│   ├── components/ # Reusable UI components
│   ├── screens/    # Screen-specific UI
│   └── dialogs/    # Dialog components
└── Main.kt         # Application entry point
```

## Data Storage

The application stores data in JSON format:
- `foods.json` - Food database
- `meals.json` - Meal definitions  
- `multi_day_menus.json` - Menu plans

## Technology Stack

- **Kotlin Multiplatform** - Cross-platform development
- **Compose Multiplatform** - Modern declarative UI framework
- **Kotlinx Serialization** - JSON data persistence
- **Material 3** - Design system and theming

## Motivation
I (the developer) build this app to better understand kotlin, app architecture, and to build a foundation 
for elementary concepts in coding a project of non-trivial size. This is not meant to be a production grade
app for general use.

## Target Users
This application is designed for a specific person and is tailored to their needs:
- keep a local database of foods that will be used to build menus for clients
- keep a local database of menus which can be printed and given to the clients
- speed up a process which otherwise relies on books or third party software
- have the app be very visually accessible, as the user has a visual deficiency preventing them from reading and viewing
well regular text and colors

## Development

For detailed development guidelines, architecture information, and project conventions, see [CLAUDE.md](CLAUDE.md).

## License

Not decided yet. Most likely, apache or another very permissive one (todo: decide at the end)