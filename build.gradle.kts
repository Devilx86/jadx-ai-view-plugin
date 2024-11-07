import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`

    id("com.github.johnrengelman.shadow") version "8.1.1"

	// auto update dependencies with 'useLatestVersions' task
	id("se.patrikerdes.use-latest-versions") version "0.2.18"
	id("com.github.ben-manes.versions") version "0.50.0"
}

dependencies {
	// use compile only scope to exclude jadx-core and its dependencies from result jar
   /* compileOnly("io.github.skylot:jadx-core:1.5.0") {
        isChanging = true
    }
	testImplementation("io.github.skylot:jadx-smali-input:1.5.0") {
		isChanging = true
	}*/

	implementation("io.github.skylot:jadx-core:1.5.1-SNAPSHOT") {
		isChanging = true
	}
	implementation("io.github.skylot:jadx-dex-input:1.5.1-SNAPSHOT") {
		isChanging = true
	}
	implementation("io.github.skylot:jadx-java-input:1.5.1-SNAPSHOT") {
		isChanging = true
	}
	implementation("io.github.skylot:jadx-smali-input:1.5.1-SNAPSHOT") {
		isChanging = true
	}
	implementation("io.github.skylot:jadx-kotlin-metadata:1.5.1-SNAPSHOT") {
		isChanging = true
	}

	implementation("com.fifesoft:rsyntaxtextarea:3.5.1")
	//implementation("io.github.lambdua:service:0.22.3")
	implementation("dev.langchain4j:langchain4j-core:0.35.0")
	implementation("dev.langchain4j:langchain4j-open-ai:0.33.0")
	implementation("dev.langchain4j:langchain4j-google-ai-gemini:0.34.0")


	//implementation("dev.langchain4j:langchain4j-open-ai:0.35.0")


	testImplementation("ch.qos.logback:logback-classic:1.4.14")
    testImplementation("org.assertj:assertj-core:3.24.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.1")
}

repositories {
    mavenCentral()
    maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/")
    google()
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

version = System.getenv("VERSION") ?: "dev"

tasks {
    withType(Test::class) {
        useJUnitPlatform()
    }
    val shadowJar = withType(ShadowJar::class) {
        archiveClassifier.set("") // remove '-all' suffix
    }

    // copy result jar into "build/dist" directory
    register<Copy>("dist") {
        dependsOn(shadowJar)
        dependsOn(withType(Jar::class))

        from(shadowJar)
        into(layout.buildDirectory.dir("dist"))
    }
}

tasks.withType<Jar> {
	// To avoid the duplicate handling strategy error
	duplicatesStrategy = DuplicatesStrategy.EXCLUDE

	// To add all of the dependencies
	from(sourceSets.main.get().output)

	dependsOn(configurations.runtimeClasspath)
	from({
		configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
	})
}
