This is a Kotlin Multiplatform project targeting Desktop.

* `/composeApp` is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - `commonMain` is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    `iosMain` would be the right folder for such calls.


I write here the instructions for Claude because I don't like writing long text in the terminal. 
So here is question 1: where to write such instructions?

Currently, the project doesn't have a good code structure (mostly the UI is 
not well organized). The assumptions made in claude.md are correct about the 
way the app is used, with 1 important thing missing: this app is intended to 
be used by someone with vision impairment. This translates to: visual accessibility (
very big fonts, lack of subtle animations, details or transitions, etc.)

The app needs some significant rewriting. Currently, the workflow of the user
is assumed to be: add / search for ingredients in the ingredients screen.
The ingredients are added from scratch (macros by hand). Then search / add meals
in the meals screen. Finally, add the menus. Each is made from the lower level
components only: menus from meals which are made from ingredients.

Upon discussion with the client, I noticed a flaw:
ingredients should be foods, actually (chicken soup is considered a food with
the new approach, while the current = old one says it's a meal ), and foods 
should be addable both from scratch (current approach) but also from other foods.

My solution that you will implement eventually: foods are like a tree (very 
ambiguous term): the leaves are ingredients from scratch (like carrots) 
and the other nodes are made from these basic foods. For example, salad might 
be made from carrots, cabbage, tomatoes, olive oil. Salad is still a food.
Another rule I forgot to mention: foods are considered to have 100g each.
That is to say, macros are stored as mass percentages. For our salad example above,
instead of mass percentages of macros, we have mass percentages of other foods
(20% carrots, 5% olive oil, etc). These percentages can add to less than 100%
(almost always for basic foods, from scratch, but also for compound foods, 
because I want to leave the flexibility).

Another food might be potato salad, made of potatoes (40%) and salad (55%), for 
example. You see now the idea. 

Another change: instead of UUID as the id for ingredients, the new food class
will have the name as id. So names must be unique. We will do this validation
only when the user adds a new food, so it is safe to assume that all names
are unique (for foods). We will add a new field: category, and I have 8 
different categories that are fixed and I know. (this suggests an enum implementation,
but I am thinking strings would work too, we will see).

The meal class remains the same, but it is now only a utility class,
for making menus easier. A meal is there to associate the quantity for the foods
and to list the macros. Also, sized Ingredient class will become sized food.

Regarding the id for the menus and for the meals, I haven't decided yet. if 
you don't have a better solution, then we leave the UUID there.

Another change (in the model for now, I will specify ui changes later): we must
support sorting eventually (of foods). By name and macros is already trivial,
but I would also like to sort by frequency (how often a food is used in menus, for
example). I don't have enough experience to find an efficient implementation, 
I was thinking at a count variable for each food, that would be incremented and 
decremented as needed.

These were the foods. Now let's move to the menus.

I have daily menus in the current implementation. However, we will need 
menus for N days in the new one. The current implementation of the menu, the
way we store it in memory seems ok to me. The thing that needs modification:
instead of only 1 day, we keep N days (N will vary, the user will have
7, 14, 30 days, maybe). Each meal (lunch, dinner, snack1, etc) will have a
meal assigned. I don't know which is the best solution for storing the N days.
Maybe a new class that stores N daily menus, but I am really not sure this is worth it.

In general, this app has to provide macros information to the user. As you saw,
I keep lazy getters for the macros and this (somehow getting the macros
efficiently) is a key feature.  

Another thing: the user must be able to create, delete, edit, print and duplicate
foods and menus. (ignore printing for now, we will implement it later). 
The duplication part means: editing but changing the name for foods. like a 
"add new food", but initialize with the data from an existing entry. I suspect
the code for adding, editing and duplicating is very similar, so we might share it
(as I tried to do for adding and editing in the current version).

This was the Model part. Let's move to the View (UI) part.

Since the foods were changed, the ui will change too.
I am visualizing something like this: the foods will be displayed like a tree
```
Potato salad ______ (space) macros
  40% Salad
    x% carrots
    y% tomatoes
    z% olive oil
  60% potatoes
```
This is very similar to the current implementation, but instead of only
showing the name of the food we will also list its children. The macros will
be listed only for the root food, not for the children too. 

Next, the meals screen: gone. The new, way more flexible version will use meals
only inside menus, and we need them to see how the macros are spread during the day
(caloric lunch, light dinner, for example). So really the menus class is just for 
ease of calculation. 

A meal inside a menu will look something like: x grams potato salad, y grams bread, etc.
Meals are not "normalized" to 100g each. This is important. 

This means that the only other screen will be the menus screen (the current daily 
menus will be scraped). How will the screen display the menus?
Each menu entry will be collapsed by default, and we will show on the collapsed
part info that makes the menu easy to be recognized. Example:
7 days - male swimmer - x kcal - this much from protein, fats, carbs.

The macro information is the one you see now implemented in the Meals and also
in the daily menus display. We will keep that visualization, and the colors too, 
just moved to the menus. Because we have multiple days and usually these macros
are counted per day, we will use the average of the days (tell me if this implies a 
lot of computational overhead).

Each menu entry will be a big table, basically. The y axis: the days. The x
axis: the 5 meals. The user will click on each "cell" to add or modify the given box.

The menu entry composable will need to be designed. 

Regarding the introduction of the new foods/ menus. The current "FAB brings dialog"
method we keep. The dialogs need help though.

1. the food from scratch: good, needs only the category added.
2. the compound food: similar to the current AddMealDialog
3. maybe a toggle to let the user choose between the 2? or an extended FAB.
we will choose the less verbose option
4. the add menu dialog: prompt the user for N, the description or the target
client (male swimmer in the example above). And create the big table that 
will be filled by clicking on each cell.

Instructions for you:
1. read the text above and understand it
2. ask me questions about the points that are not clear
3. propose candidate implementations in natural language at first, no code 
modifications yet

4. tell me where to put prompts like these in the future 

this long prompt was processed, probably it can be deleted.




Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…