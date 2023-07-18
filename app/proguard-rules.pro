# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile


#RULES :)
# Add any custom ProGuard rules specific to your app here

# Keep classes and members of the "com.pixoai.android" package
-keep class com.pixoai.android.** { *; }

# Keep classes and members of the Firebase library
-keep class com.google.firebase.** { *; }

# Keep classes and members of the Glide library
-keep class com.bumptech.glide.** { *; }

# Keep classes and members of the Picasso library
-keep class com.squareup.picasso.** { *; }

# Keep any custom classes or packages you want to preserve
# -keep class com.example.mypackage.** { *; }

# Specify any additional library-specific ProGuard rules

# Example: Keep all classes and members of the OkHttp library
-keep class okhttp3.** { *; }

# Example: Keep all classes and members of the Retrofit library
-keep class retrofit2.** { *; }

# Example: Keep all classes and members of the Gson library
-keep class com.google.gson.** { *; }

# Add any other specific rules for libraries or custom classes

# ...

# Specify optimization and shrinking options
-dontoptimize
-dontshrink

# Specify any additional ProGuard configuration options

# ...
