plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.saevitus.discord_text"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.saevitus.discord_text"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    packaging  {
        // Exclude the duplicate INDEX.LIST files
        resources.excludes.add("META-INF/INDEX.LIST")
        resources.excludes.add("META-INF/LICENSE.md")
        resources.excludes.add("META-INF/LICENSE-notice.md")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation(files("C:/Users/saevitus/Downloads/JDA-5.0.0-beta.24/build/libs/JDA-5.0.0-beta.24_DEV.jar"))
    implementation("org.slf4j:slf4j-api:2.0.7")
    implementation("com.fasterxml.jackson.core:jackson-core:2.17.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.0")
    implementation("ch.qos.logback:logback-classic:1.2.11")
    implementation("club.minnced:opus-java:1.1.1")
    implementation("com.google.code.findbugs:jsr305:3.0.2")
    implementation("com.neovisionaries:nv-websocket-client:2.14")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    // compileOnly remains the same as it's a transitive dependency
    compileOnly("net.java.dev.jna:jna:4.4.0")
    implementation("net.sf.trove4j:core:3.1.0")
    implementation("org.apache.commons:commons-collections4:4.4")
    implementation("org.apache.commons:commons-lang3:3.14.0")
    implementation("org.assertj:assertj-core:3.25.3")
    implementation("org.jetbrains:annotations:24.1.0")
    implementation("org.junit.jupiter:junit-jupiter:5.10.2")
    implementation("org.mockito:mockito-core:5.11.0")
    implementation("org.reflections:reflections:0.10.2")
    //implementation("org.slf4j:slf4j-api:latest.release")
    implementation("club.minnced:jda-ktx:0.11.0-beta.20") {
        exclude(group = "net.dv8tion", module ="JDA") // Exclude the default JDA
    }
}