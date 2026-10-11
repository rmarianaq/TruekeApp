package co.edu.uniquindio.truekeapp.core.component

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Chair
import androidx.compose.material.icons.rounded.Checkroom
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.SportsSoccer
import androidx.compose.material.icons.rounded.Toys
import androidx.compose.ui.graphics.vector.ImageVector
import co.edu.uniquindio.truekeapp.R
import co.edu.uniquindio.truekeapp.domain.model.Category
import co.edu.uniquindio.truekeapp.domain.model.ItemCondition
import co.edu.uniquindio.truekeapp.domain.model.UserLevel

// Cómo se MUESTRA cada categoría y estado: texto traducible + ícono

@get:StringRes
val Category.labelRes: Int
    get() = when (this) {
        Category.TECHNOLOGY -> R.string.category_technology
        Category.BOOKS -> R.string.category_books
        Category.CLOTHING -> R.string.category_clothing
        Category.HOME -> R.string.category_home
        Category.SPORTS -> R.string.category_sports
        Category.TOYS -> R.string.category_toys
        Category.OTHER -> R.string.category_other
    }

val Category.icon: ImageVector
    get() = when (this) {
        Category.TECHNOLOGY -> Icons.Rounded.Devices
        Category.BOOKS -> Icons.AutoMirrored.Rounded.MenuBook
        Category.CLOTHING -> Icons.Rounded.Checkroom
        Category.HOME -> Icons.Rounded.Chair
        Category.SPORTS -> Icons.Rounded.SportsSoccer
        Category.TOYS -> Icons.Rounded.Toys
        Category.OTHER -> Icons.Rounded.Category
    }

@get:StringRes
val ItemCondition.labelRes: Int
    get() = when (this) {
        ItemCondition.NEW -> R.string.condition_new
        ItemCondition.LIKE_NEW -> R.string.condition_like_new
        ItemCondition.GOOD -> R.string.condition_good
        ItemCondition.USED -> R.string.condition_used
    }

@get:StringRes
val UserLevel.labelRes: Int
    get() = when (this) {
        UserLevel.NOVATO -> R.string.level_novato
        UserLevel.INTERCAMBIADOR -> R.string.level_intercambiador
        UserLevel.NEGOCIANTE -> R.string.level_negociante
        UserLevel.MAESTRO_DEL_TRUEQUE -> R.string.level_maestro
    }