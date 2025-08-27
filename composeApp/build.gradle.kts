import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {

    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    jvm("desktop")
    
    sourceSets {
        val desktopMain by getting
        val desktopTest by getting
        
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.kotlinx.serialization.json)

        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
            implementation(compose.uiTest)
        }
        desktopTest.dependencies {
            implementation("org.junit.jupiter:junit-jupiter:5.9.2")
            implementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
            runtimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation("com.openhtmltopdf:openhtmltopdf-pdfbox:1.0.10")
            implementation("com.openhtmltopdf:openhtmltopdf-slf4j:1.0.10")
        }
    }
}

// Configure tests to run sequentially due to shared Model singleton and file I/O
tasks.withType<Test> {
    // Force sequential execution for integration tests
    maxParallelForks = 1
    
    // Enable JUnit 5 platform
    useJUnitPlatform()
    
    // Verbose test output
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
    }
}

compose.desktop {
    application {
        mainClass = "org.example.project.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "org.example.project"
            packageVersion = "1.0.0"
        }
    }
}
