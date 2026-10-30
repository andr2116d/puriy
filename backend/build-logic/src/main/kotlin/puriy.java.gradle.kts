import net.ltgt.gradle.errorprone.CheckSeverity
import net.ltgt.gradle.errorprone.errorprone

plugins {
    java
    id("com.diffplug.spotless")
    id("net.ltgt.errorprone")
}

val catalogo = the<VersionCatalogsExtension>().named("libs")

fun libreria(alias: String) = catalogo.findLibrary(alias).get()

fun version(alias: String) = catalogo.findVersion(alias).get().requiredVersion

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(version("java")))
    }
}

dependencies {
    implementation(libreria("jspecify"))
    compileOnly(libreria("lombok"))
    annotationProcessor(libreria("lombok"))
    testCompileOnly(libreria("lombok"))
    testAnnotationProcessor(libreria("lombok"))
    errorprone(libreria("errorprone-core"))
    errorprone(libreria("nullaway"))
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
    options.errorprone {
        disableWarningsInGeneratedCode.set(true)
        excludedPaths.set(".*/build/generated/.*")
        check("NullAway", CheckSeverity.ERROR)
        option("NullAway:AnnotatedPackages", "pe.puriy")
        option("NullAway:JSpecifyMode", "true")
    }
}

tasks.named<JavaCompile>("compileTestJava") {
    options.errorprone.check("NullAway", CheckSeverity.OFF)
}

spotless {
    java {
        palantirJavaFormat(version("palantir-java-format"))
        formatAnnotations()
        targetExclude("build/**")
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

tasks.named<Test>("test") {
    description = "Pruebas rápidas, sin Docker."
    filter {
        excludeTestsMatching("*IT")
    }
}

val integrationTest = tasks.register<Test>("integrationTest") {
    description = "Pruebas de integración (*IT); requieren Docker."
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath
    useJUnitPlatform {
        excludeEngines("archunit")
    }
    filter {
        includeTestsMatching("*IT")
    }
    shouldRunAfter(tasks.named("test"))
}

tasks.named("check") {
    dependsOn(integrationTest)
}
