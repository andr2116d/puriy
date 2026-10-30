plugins {
    id("puriy.java")
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.jooq.docker)
}

dependencies {
    implementation(platform(libs.spring.boot.bom))
    implementation(platform(libs.spring.modulith.bom))

    implementation(libs.spring.boot.starter.webmvc)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.spring.boot.starter.jooq)
    implementation(libs.spring.boot.starter.flyway)
    implementation(libs.flyway.postgresql)
    implementation(libs.spring.modulith.starter.core)
    implementation(libs.uuid.creator)
    runtimeOnly(libs.postgresql)

    jooqCodegen(platform(libs.spring.boot.bom))
    jooqCodegen(libs.jooq.codegen)
    jooqCodegen(libs.flyway.core)
    jooqCodegen(libs.flyway.postgresql)
    jooqCodegen(libs.postgresql)

    developmentOnly(platform(libs.spring.boot.bom))
    developmentOnly(libs.spring.boot.docker.compose)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.boot.starter.webmvc.test)
    testImplementation(libs.spring.boot.starter.jooq.test)
    testImplementation(libs.spring.boot.testcontainers)
    testImplementation(libs.spring.modulith.starter.test)
    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.testcontainers.junit)
    testImplementation(libs.archunit)
    testRuntimeOnly(libs.junit.launcher)
}

jooq {
    withContainer {
        image {
            name = "postgres:17"
        }
    }
}

tasks.generateJooqClasses {
    schemas.set(
            listOf(
                    "rutas",
                    "tarifas",
                    "reservas",
                    "compras",
                    "pagos",
                    "boletos",
                    "operacion",
                    "notificaciones",
                    "identidad"))
    basePackageName.set("puriy.basedatos")
    migrationLocations.setFromFilesystem("src/main/resources/db/migration")
    flywayProperties.put("flyway.createSchemas", "false")
    flywayProperties.put("flyway.defaultSchema", "public")
}
