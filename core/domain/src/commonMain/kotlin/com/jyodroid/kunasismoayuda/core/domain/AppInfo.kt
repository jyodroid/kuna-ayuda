package com.jyodroid.kunasismoayuda.core.domain

/**
 * App version, shown in-app (Overview footer) and used as the human-facing release identifier.
 *
 * **Release process — keep these in sync (semantic versioning, MAJOR.MINOR.PATCH):**
 *  1. this [VERSION] constant,
 *  2. `desktopPackageVersion` in `gradle/libs.versions.toml` (the desktop installer version),
 *  3. the git tag `vX.Y.Z` (which triggers the release workflow).
 */
object AppInfo {
    const val VERSION: String = "1.0.0"
}
