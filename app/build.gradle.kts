plugins {
 alias(libs.plugins.android.application)
 alias(libs.plugins.compose)
 alias(libs.plugins.serialization)
 alias(libs.plugins.ksp)
}
android {
 namespace = "org.rural.learn"
 compileSdk { version = release(37) { minorApiLevel = 1 } }
 defaultConfig {
  applicationId = "org.rural.learn"
  minSdk = 24
  targetSdk = 37
  versionCode = 1
  versionName = "0.1.0"
  testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
 }
 buildFeatures { compose = true; buildConfig = true }
 buildTypes {
  release { isMinifyEnabled = true; isShrinkResources = true; proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro") }
 }
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
 testOptions { unitTests.isReturnDefaultValues = true }
 packaging { resources.excludes += "/META-INF/{AL2.0,LGPL2.1}" }
}
kotlin { compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) } }
ksp { arg("room.schemaLocation", "$projectDir/schemas") }
dependencies {
 implementation(platform(libs.compose.bom))
 implementation(libs.compose.ui); implementation(libs.compose.foundation); implementation(libs.compose.material3); implementation(libs.compose.preview)
 implementation(libs.navigation); implementation(libs.viewmodel)
 implementation(libs.room.runtime); implementation(libs.room.ktx); ksp(libs.room.compiler)
 implementation(libs.datastore); implementation(libs.work); implementation(libs.serialization); implementation(libs.coroutines); implementation(libs.appcompat)
 testImplementation(libs.junit); testImplementation(libs.coroutines.test); testImplementation(libs.turbine)
 androidTestImplementation(platform(libs.compose.bom)); androidTestImplementation(libs.compose.test); androidTestImplementation(libs.android.test); androidTestImplementation(libs.android.runner); androidTestImplementation(libs.espresso)
 debugImplementation(libs.compose.test.manifest)
}
