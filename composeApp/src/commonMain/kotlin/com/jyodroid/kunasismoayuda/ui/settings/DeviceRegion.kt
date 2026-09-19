package com.jyodroid.kunasismoayuda.ui.settings

/**
 * The device's ISO 3166-1 alpha-2 region code (e.g. "CO"), or null if unknown. Used only to **suggest**
 * a country on first run — never to auto-commit the choice. Offline, no permission, no network.
 */
expect fun deviceRegionCode(): String?
