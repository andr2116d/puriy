plugins {
    id("puriy.java")
}

val catalogo = the<VersionCatalogsExtension>().named("libs")

fun libreria(alias: String) = catalogo.findLibrary(alias).get()

dependencies {
    implementation(libreria("aws-lambda-core"))

    testImplementation(platform(libreria("spring-boot-bom")))
    testImplementation(libreria("junit-jupiter"))
    testImplementation(libreria("assertj-core"))
    testImplementation(libreria("mockito-core"))
    testRuntimeOnly(libreria("junit-launcher"))
}

tasks.named<Test>("integrationTest") {
    enabled = false
}

val salidaPrincipal = the<SourceSetContainer>()["main"].output

val empaquetar = tasks.register<Zip>("empaquetar") {
    description = "Arma el ZIP de la Lambda: las clases en la raíz y las dependencias en lib/."
    group = LifecycleBasePlugin.BUILD_GROUP
    archiveFileName.set("${project.name}.zip")
    destinationDirectory.set(layout.buildDirectory.dir("distributions"))
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
    from(salidaPrincipal)
    into("lib") {
        from(configurations.named("runtimeClasspath"))
    }
}

tasks.named("assemble") {
    dependsOn(empaquetar)
}
