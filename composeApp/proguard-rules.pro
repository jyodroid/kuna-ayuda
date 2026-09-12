# R8 / ProGuard keep rules for the release build.
#
# This app uses reflection-heavy libraries — kotlinx.serialization (all the @Serializable DTOs in
# core:data), Ktor client, and Koin DI — so R8 full-mode would otherwise strip generated serializers
# and crash at runtime (serialization/DI failures that only surface on-device). These rules keep what
# those libraries need while still letting R8 obfuscate + shrink the rest.
#
# Ktor, kotlinx-coroutines and Koin ship their own consumer ProGuard rules inside their artifacts
# (auto-applied by R8); the blocks below are the app-specific additions + belt-and-suspenders.

# Keep annotations R8 needs to reason about serialization + generics.
-keepattributes *Annotation*, InnerClasses, Signature, EnclosingMethod, RuntimeVisibleAnnotations

# ---- kotlinx.serialization -----------------------------------------------------------------------
# Canonical keep set from the kotlinx.serialization docs: keep every generated $$serializer and the
# Companion.serializer()/INSTANCE hooks the runtime looks up reflectively.
-keepclassmembers @kotlinx.serialization.Serializable class ** {
    *** Companion;
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}
-keep,includedescriptorclasses class **$$serializer { *; }
-dontnote kotlinx.serialization.**

# Belt-and-suspenders: keep this app's own @Serializable model/DTO classes and their members intact,
# so field names (the JSON keys) and serializers are never renamed or removed.
-keep @kotlinx.serialization.Serializable class com.jyodroid.kunasismoayuda.** { *; }

# Enums are serialized by name and read via values()/valueOf() reflectively — keep those members.
-keepclassmembers enum com.jyodroid.kunasismoayuda.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ---- Ktor client ---------------------------------------------------------------------------------
# Ktor ships consumer rules; keep the surface anyway (engines/attributes resolved reflectively) and
# silence warnings for its optional transitive integrations we don't use.
-keep class io.ktor.** { *; }
-keepclassmembers class io.ktor.** { *; }
-dontwarn io.ktor.**

# ---- Koin --------------------------------------------------------------------------------------
-keep class org.koin.** { *; }
-dontwarn org.koin.**

# ---- Misc transitive warnings (logging backends pulled in by Ktor logging) -----------------------
-dontwarn org.slf4j.**
-dontwarn kotlinx.coroutines.**
