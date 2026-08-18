package com.jyodroid.kunasismoayuda.ui.board

import com.jyodroid.kunasismoayuda.core.domain.model.ResourceType
import com.jyodroid.kunasismoayuda.resources.Res
import com.jyodroid.kunasismoayuda.resources.rtype_food
import com.jyodroid.kunasismoayuda.resources.rtype_hygiene
import com.jyodroid.kunasismoayuda.resources.rtype_medicine
import com.jyodroid.kunasismoayuda.resources.rtype_other
import com.jyodroid.kunasismoayuda.resources.rtype_shelter
import com.jyodroid.kunasismoayuda.resources.rtype_water
import org.jetbrains.compose.resources.StringResource

fun resourceTypeLabelRes(type: ResourceType): StringResource = when (type) {
    ResourceType.WATER -> Res.string.rtype_water
    ResourceType.FOOD -> Res.string.rtype_food
    ResourceType.MEDICINE -> Res.string.rtype_medicine
    ResourceType.SHELTER -> Res.string.rtype_shelter
    ResourceType.HYGIENE -> Res.string.rtype_hygiene
    ResourceType.OTHER -> Res.string.rtype_other
}

fun resourceTypeEmoji(type: ResourceType): String = when (type) {
    ResourceType.WATER -> "💧"
    ResourceType.FOOD -> "🍞"
    ResourceType.MEDICINE -> "💊"
    ResourceType.SHELTER -> "🏠"
    ResourceType.HYGIENE -> "🧼"
    ResourceType.OTHER -> "📦"
}
