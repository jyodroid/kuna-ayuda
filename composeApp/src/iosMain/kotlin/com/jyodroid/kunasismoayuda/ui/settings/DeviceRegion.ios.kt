package com.jyodroid.kunasismoayuda.ui.settings

import platform.Foundation.NSLocale
import platform.Foundation.countryCode
import platform.Foundation.currentLocale

actual fun deviceRegionCode(): String? = NSLocale.currentLocale.countryCode
