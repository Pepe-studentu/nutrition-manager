package org.example.project.service

import androidx.compose.runtime.mutableStateOf

enum class Language(val code: String, val displayName: String) {
    ENGLISH("en", "English"),
    ROMANIAN("ro", "Română")
}

object TranslationService {
    private var currentLanguage = mutableStateOf(Language.ROMANIAN)

    val currentLang get() = currentLanguage.value

    fun setLanguage(language: Language) {
        currentLanguage.value = language
    }

    fun getString(key: String): String {
        return translations[currentLanguage.value]?.get(key) ?: key
    }

    fun getString(key: String, vararg args: Any): String {
        val template = translations[currentLanguage.value]?.get(key) ?: key
        return formatString(template, *args)
    }

    private fun formatString(template: String, vararg args: Any): String {
        var result = template
        args.forEachIndexed { index, arg ->
            result = result.replace("{$index}", arg.toString())
        }
        return result
    }

    private val translations = mapOf(
        Language.ENGLISH to mapOf(
            // Navigation
            "foods" to "Foods",
            "menus" to "Menus",
            "settings" to "Settings",
            "language" to "Language",

            // Common actions
            "add" to "Add",
            "edit" to "Edit",
            "delete" to "Delete",
            "cancel" to "Cancel",
            "save" to "Save",
            "update" to "Update",
            "search" to "Search",
            "none" to "none",
            "results_found" to "{0} results found",
            "add_food_button" to "Add Food",
            "search_menus" to "Search Menus",
            "add_menu_button" to "Add Menu",
            "confirm_deletion" to "Confirm Deletion",
            "remove_meal_from_menu" to "Remove Meal from Menu",
            "confirm_remove_meal" to "Are you sure you want to remove the selected meal from this menu slot?",
            "remove" to "Remove",
            "duplicate" to "Duplicate",
            "calories" to "Calories",

            // Food related
            "food" to "Food",
            "add_food" to "Add food",
            "edit_food" to "Edit food",
            "basic" to "basic",
            "compound" to "compound",
            "title" to "Title",
            "category" to "Category",
            "tags" to "Tags (separated by semicolon)",
            "tags_short" to "Tags",
            "search_foods" to "Search foods",
            "add_ingredients_to_see_categories" to "Add ingredients to see categories",
            "macronutrients_per_100g" to "Macronutrients (per 100g)",
            "protein" to "Protein",
            "carbs" to "Carbs",
            "fat" to "Fat",
            "fats" to "Fats",
            "water_percentage" to "Water %",
            "food_added_successfully" to "Food added successfully",
            "food_updated_successfully" to "Food updated successfully",
            "failed_to_add_food" to "Failed to add food. Check inputs.",
            "failed_to_update_food" to "Failed to update food. Check inputs.",
            "food_deleted" to "Food deleted",
            "failed_to_delete_food" to "Failed to delete food",
            "confirm_delete_food" to "Are you sure you want to delete this food?",
            "cannot_delete_food_in_use" to "Cannot delete food that is being used in meals or other foods",
            "used_in_items" to "Used in {0} item(s)",

            // Meal related
            "meal" to "Meal",
            "meals" to "Meals",
            "add_meal" to "Add Meal",
            "update_meal" to "Update Meal",
            "edit_meal" to "Edit Meal",
            "add_meal_to_slot" to "Add meal to slot",
            "description" to "description",
            "grams" to "g",
            "remove" to "Remove",
            "meal_added_successfully" to "Meal added successfully",
            "meal_updated_successfully" to "Meal updated successfully",
            "failed_to_add_meal" to "Failed to add meal. Check inputs.",
            "failed_to_update_meal" to "Failed to update meal. Check inputs.",

            // Menu related
            "menu" to "Menu",
            "create_menu" to "Create Menu",
            "menu_name" to "Menu Name",
            "days" to "Days",
            "menu_created" to "Menu created",
            "failed_to_create_menu" to "Failed to create menu",
            "menu_deleted" to "Menu deleted",
            "failed_to_delete_menu" to "Failed to delete menu",
            "confirm_delete_menu" to "Are you sure you want to delete this menu?",
            "breakfast" to "Breakfast",
            "snack_1" to "Snack 1",
            "lunch" to "Lunch",
            "snack_2" to "Snack 2",
            "dinner" to "Dinner",
            "day" to "Day",
            "total" to "Total",
            "average" to "Average",

            // Print dialog
            "print_menu" to "Print Menu",
            "include_signature" to "Include signature",
            "signature_placeholder" to "Nutritionist signature...",
            "template_css_customization" to "Template CSS Customization",
            "copy_template" to "Copy Template",
            "update_template" to "Update Template",
            "template_updated_successfully" to "Template updated successfully",
            "no_valid_css_found" to "No valid CSS found in clipboard",
            "no_content_found_in_clipboard" to "No content found in clipboard",
            "generate_pdf" to "Generate PDF",
            "failed_to_generate_pdf" to "Failed to generate PDF",

            // Sort indicators
            "ascending" to "Ascending",
            "descending" to "Descending",

            // FoodInputDialog strings
            "components" to "Components:",
            "total_per_100g" to "Total (per 100g):",

            // AddMealDialog strings
            "edit_meal" to "Edit Meal",
            "add_meal_to_slot" to "Add meal to slot",
            "description" to "description",
            "search_foods" to "Search Foods",

            // PrintDialog additional strings
            "print_menu_title" to "Print: {0}",
            "signature_file" to "Signature file: {0}",
            "copy_paste_instruction" to "Copy -> Edit CSS elsewhere -> Paste",
            "template_copied_message" to "Template copied to clipboard - edit CSS and paste back",
            "failed_to_generate_template" to "Failed to generate template: {0}",
            "failed_to_update_template" to "Failed to update template: {0}",
            "pdf_generated_message" to "PDF generated and opened: {0}",

            // MenuDialogs strings
            "create_multi_day_menu" to "Create Multi-Day Menu",
            "description" to "Description",
            "number_of_days" to "Number of Days",
            "create" to "Create",
            "menu_created_successfully" to "Menu created successfully",
            "failed_to_create_menu_check_inputs" to "Failed to create menu. Check inputs.",

            // Menu components strings
            "days" to "days",
            "print" to "Print",
            "daily_total" to "Daily Total",
            "add_meal" to "Add meal",
            "add_a_meal" to "Add a meal",
            "no_data" to "No data",
            "day_number" to "Day {0}",

            // MenuPrintService strings for PDF
            "pdf_day_header" to "Day",
            "pdf_daily_total" to "Daily Total"
        ),

        Language.ROMANIAN to mapOf(
            // Navigation
            "foods" to "Alimente",
            "menus" to "Meniuri",
            "settings" to "Setări",
            "language" to "Limba",

            // Common actions
            "add" to "Adaugă",
            "edit" to "Editează",
            "delete" to "Șterge",
            "cancel" to "Anulează",
            "save" to "Salvează",
            "update" to "Actualizează",
            "search" to "Caută",
            "none" to "niciunul",
            "results_found" to "{0} rezultate găsite",
            "add_food_button" to "Adaugă Aliment",
            "search_menus" to "Caută Meniuri",
            "add_menu_button" to "Adaugă Meniu",
            "confirm_deletion" to "Confirmă Ștergerea",
            "remove_meal_from_menu" to "Elimină Masa din Meniu",
            "confirm_remove_meal" to "Ești sigur că vrei să elimini masa selectată?",
            "remove" to "Elimină",
            "duplicate" to "Duplică",
            "calories" to "Calorii",

            // Food related
            "food" to "Aliment",
            "add_food" to "Adaugă aliment",
            "edit_food" to "Editează aliment",
            "basic" to "simplu",
            "compound" to "compus",
            "title" to "Titlu",
            "category" to "Categorie",
            "tags" to "Etichete (separate prin punct și virgulă)",
            "tags_short" to "Etichete",
            "search_foods" to "Caută alimente",
            "add_ingredients_to_see_categories" to "Adaugă ingrediente pentru a vedea categoriile",
            "macronutrients_per_100g" to "Macronutrienți (per 100g)",
            "protein" to "Proteine",
            "carbs" to "Glucide",
            "fat" to "Lipide",
            "fats" to "Lipide",
            "water_percentage" to "Apă %",
            "food_added_successfully" to "Aliment adăugat cu succes",
            "food_updated_successfully" to "Aliment actualizat cu succes",
            "failed_to_add_food" to "Eroare la adăugarea alimentului. Verifică datele introduse.",
            "failed_to_update_food" to "Eroare la actualizarea alimentului. Verifică datele introduse.",
            "food_deleted" to "Aliment șters",
            "failed_to_delete_food" to "Eroare la ștergerea alimentului",
            "confirm_delete_food" to "Ești sigur că vrei să ștergi acest aliment?",
            "cannot_delete_food_in_use" to "Nu se poate șterge alimentul care este folosit în mese sau alte alimente",
            "used_in_items" to "Folosit în {0} element(e)",

            // Meal related
            "meal" to "Masă",
            "meals" to "Mese",
            "add_meal" to "Adaugă Masă",
            "update_meal" to "Actualizează Masă",
            "edit_meal" to "Editează Masă",
            "add_meal_to_slot" to "Adaugă masă în slot",
            "description" to "descriere",
            "grams" to "g",
            "remove" to "Elimină",
            "meal_added_successfully" to "Masă adăugată cu succes",
            "meal_updated_successfully" to "Masă actualizată cu succes",
            "failed_to_add_meal" to "Eroare la adăugarea mesei. Verifică datele introduse.",
            "failed_to_update_meal" to "Eroare la actualizarea mesei. Verifică datele introduse.",

            // Menu related
            "menu" to "Meniu",
            "create_menu" to "Creează Meniu",
            "menu_name" to "Numele Meniului",
            "days" to "zile",
            "menu_created" to "Meniu creat",
            "failed_to_create_menu" to "Eroare la crearea meniului",
            "menu_deleted" to "Meniu șters",
            "failed_to_delete_menu" to "Eroare la ștergerea meniului",
            "confirm_delete_menu" to "Ești sigur că vrei să ștergi acest meniu?",
            "breakfast" to "Mic dejun",
            "snack_1" to "Gustare 1",
            "lunch" to "Prânz",
            "snack_2" to "Gustare 2",
            "dinner" to "Cină",
            "day" to "Ziua",
            "total" to "Total",
            "average" to "Medie",

            // Print dialog
            "print_menu" to "Printează Meniu",
            "include_signature" to "Include semnătura",
            "signature_placeholder" to "Semnătura nutriționistului...",
            "template_css_customization" to "Personalizare Template CSS",
            "copy_template" to "Copiază Template",
            "update_template" to "Actualizează Template",
            "template_updated_successfully" to "Template actualizat cu succes",
            "no_valid_css_found" to "Nu s-a găsit CSS valid în clipboard",
            "no_content_found_in_clipboard" to "Nu s-a găsit conținut în clipboard",
            "generate_pdf" to "Generează PDF",
            "failed_to_generate_pdf" to "Eroare la generarea PDF-ului",

            // Sort indicators
            "ascending" to "Crescător",
            "descending" to "Descrescător",

            // FoodInputDialog strings
            "components" to "Componente:",
            "total_per_100g" to "Total (per 100g):",

            // AddMealDialog strings
            "edit_meal" to "Editează Masă",
            "add_meal_to_slot" to "Adaugă masă în slot",
            "description" to "descriere",
            "search_foods" to "Caută Alimente",

            // PrintDialog additional strings
            "print_menu_title" to "Printează: {0}",
            "signature_file" to "Fișier semnătură: {0}",
            "copy_paste_instruction" to "Copiază -> Editează CSS în altă parte -> Lipește",
            "template_copied_message" to "Template copiat în clipboard - editează CSS și lipește înapoi",
            "failed_to_generate_template" to "Eroare la generarea template-ului: {0}",
            "failed_to_update_template" to "Eroare la actualizarea template-ului: {0}",
            "pdf_generated_message" to "PDF generat și deschis: {0}",

            // MenuDialogs strings
            "create_multi_day_menu" to "Creează Meniu",
            "description" to "Descriere",
            "number_of_days" to "Numărul de Zile",
            "create" to "Creează",
            "menu_created_successfully" to "Meniu creat cu succes",
            "failed_to_create_menu_check_inputs" to "Eroare la crearea meniului. Verifică datele introduse.",

            // Menu components strings
            "days" to "zile",
            "print" to "Printează",
            "daily_total" to "Total Zilnic",
            "add_meal" to "Adaugă masă",
            "add_a_meal" to "Adaugă o masă",
            "no_data" to "Fără date",
            "day_number" to "Ziua {0}",

            // MenuPrintService strings for PDF
            "pdf_day_header" to "Ziua",
            "pdf_daily_total" to "Total zilnic"
        )
    )
}

// Composable helper function for easy access
@androidx.compose.runtime.Composable
fun tr(key: String): String {
    // Re-compose when language changes
    TranslationService.currentLang
    return TranslationService.getString(key)
}

// Composable helper function with arguments
@androidx.compose.runtime.Composable
fun tr(key: String, vararg args: Any): String {
    // Re-compose when language changes
    TranslationService.currentLang
    return TranslationService.getString(key, *args)
}