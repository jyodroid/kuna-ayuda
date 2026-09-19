package com.jyodroid.kunasismoayuda.ui.settings

import java.util.Locale

actual fun deviceRegionCode(): String? = Locale.getDefault().country.takeIf { it.isNotBlank() }
