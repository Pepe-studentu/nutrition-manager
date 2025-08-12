package org.example.project.model

import kotlinprojecttest.composeapp.generated.resources.Res
import kotlinprojecttest.composeapp.generated.resources.circle
import kotlinprojecttest.composeapp.generated.resources.circle_filled
import kotlinprojecttest.composeapp.generated.resources.dinner_dining_24px
import kotlinprojecttest.composeapp.generated.resources.egg
import kotlinprojecttest.composeapp.generated.resources.hexagon
import kotlinprojecttest.composeapp.generated.resources.square
import kotlinprojecttest.composeapp.generated.resources.triangle
import org.jetbrains.compose.resources.DrawableResource

enum class Screen(val icon: DrawableResource, val filledIcon: DrawableResource) {
    Foods(Res.drawable.circle, Res.drawable.circle),
    Menus(Res.drawable.square, Res.drawable.square)
}