import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    id("org.jetbrains.kotlin.multiplatform") version libs.versions.kotlin.get()
    id("org.jetbrains.kotlin.plugin.compose") version libs.versions.kotlin.get()
    id("org.jetbrains.kotlin.plugin.serialization") version libs.versions.kotlin.get()
    id("org.jetbrains.compose") version libs.versions.composeMultiplatform.get()
    // Hot reload temporarily disabled for stability
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

// Removed forced resolution strategy - let Gradle resolve compatible versions naturally

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
            packageName = "NutritionApp"
            packageVersion = "1.0.0"

            description = "Open source nutrition meal planning application"


            windows {
                dirChooser = true
                perUserInstall = true
                menuGroup = "NutritionApp"
                upgradeUuid = "BF9CDA6A-1391-46C5-8EA5-57A86EFBD073"
                iconFile.set(project.file("../thumbnail.png"))
            }
        }
    }
}

// JFR Profiling Tasks
tasks.register<JavaExec>("runWithJFR") {
    group = "profiling"
    description = "Run the application with JFR recording (60 seconds)"
    classpath = kotlin.jvm("desktop").compilations.getByName("main").runtimeDependencyFiles + kotlin.jvm("desktop").compilations.getByName("main").output.allOutputs
    mainClass.set("org.example.project.MainKt")

    val jfrFile = layout.buildDirectory.file("reports/jfr/nutrition-app-${System.currentTimeMillis()}.jfr")
    jvmArgs(
        "-XX:+FlightRecorder",
        "-XX:StartFlightRecording=duration=60s,filename=${jfrFile.get().asFile.absolutePath}",
        "-XX:FlightRecorderOptions=stackdepth=256"
    )

    doFirst {
        jfrFile.get().asFile.parentFile.mkdirs()
        println("JFR recording will be saved to: ${jfrFile.get().asFile.absolutePath}")
        println("Recording for 60 seconds...")
    }
}

tasks.register<JavaExec>("runWithJFRLong") {
    group = "profiling"
    description = "Run the application with JFR recording (5 minutes)"
    classpath = kotlin.jvm("desktop").compilations.getByName("main").runtimeDependencyFiles + kotlin.jvm("desktop").compilations.getByName("main").output.allOutputs
    mainClass.set("org.example.project.MainKt")

    val jfrFile = layout.buildDirectory.file("reports/jfr/nutrition-app-long-${System.currentTimeMillis()}.jfr")
    jvmArgs(
        "-XX:+FlightRecorder",
        "-XX:StartFlightRecording=duration=300s,filename=${jfrFile.get().asFile.absolutePath}",
        "-XX:FlightRecorderOptions=stackdepth=256"
    )

    doFirst {
        jfrFile.get().asFile.parentFile.mkdirs()
        println("JFR recording will be saved to: ${jfrFile.get().asFile.absolutePath}")
        println("Recording for 5 minutes...")
    }
}

tasks.register<JavaExec>("runWithJFRManual") {
    group = "profiling"
    description = "Run the application with JFR enabled (manual start/stop with jcmd)"
    classpath = kotlin.jvm("desktop").compilations.getByName("main").runtimeDependencyFiles + kotlin.jvm("desktop").compilations.getByName("main").output.allOutputs
    mainClass.set("org.example.project.MainKt")

    jvmArgs(
        "-XX:+FlightRecorder",
        "-XX:FlightRecorderOptions=stackdepth=256"
    )

    doFirst {
        println("JFR enabled. Use jcmd to start/stop recordings:")
        println("  jcmd <pid> JFR.start duration=60s filename=recording.jfr")
        println("  jcmd <pid> JFR.stop")
        println("  jcmd <pid> JFR.dump filename=recording.jfr")
    }
}

tasks.register<JavaExec>("runWithJFRExceptions") {
    group = "profiling"
    description = "Run with JFR focused on exceptions and errors (60 seconds)"
    classpath = kotlin.jvm("desktop").compilations.getByName("main").runtimeDependencyFiles + kotlin.jvm("desktop").compilations.getByName("main").output.allOutputs
    mainClass.set("org.example.project.MainKt")

    val jfrFile = layout.buildDirectory.file("reports/jfr/exceptions-${System.currentTimeMillis()}.jfr")
    jvmArgs(
        "-XX:+FlightRecorder",
        "-XX:StartFlightRecording=duration=300s,filename=${jfrFile.get().asFile.absolutePath}",
        "-XX:FlightRecorderOptions=stackdepth=256",
        // Enhanced exception and class tracking
        "-XX:+LogJavaExceptions",
        "-XX:+TraceClassLoading",
        "-XX:+TraceClassUnloading",
        // Memory-efficient GC configuration
        "-XX:+UseG1GC",
        "-XX:+UseCompressedOops",
        "-XX:MaxMetaspaceSize=512m",
        "-XX:MetaspaceSize=256m",
        "-XX:G1PeriodicGCInterval=30000",
        // Verbose GC for analysis
        "-XX:+PrintGCDetails",
        "-XX:+PrintGCTimeStamps"
    )

    doFirst {
        jfrFile.get().asFile.parentFile.mkdirs()
        println("Exception-focused JFR recording will be saved to: ${jfrFile.get().asFile.absolutePath}")
        println("Enhanced tracking: exceptions, class loading/unloading, GC details")
        println("Recording for 60 seconds...")
    }
}
