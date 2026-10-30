package pe.puriy;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.onionArchitecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

@AnalyzeClasses(packages = "pe.puriy", importOptions = ImportOption.DoNotIncludeTests.class)
class ArquitecturaTest {

    @ArchTest
    static final ArchRule capas_hexagonales = onionArchitecture()
            .domainModels("..dominio.modelo..", "..dominio.excepcion..")
            .domainServices("..dominio.servicio..")
            .applicationServices("..aplicacion..")
            .adapter("web", "..adaptador.entrada.web..")
            .adapter("eventos de entrada", "..adaptador.entrada.evento..")
            .adapter("programados", "..adaptador.entrada.programado..")
            .adapter("salida", "..adaptador.salida..")
            .withOptionalLayers(true);

    @ArchTest
    static final ArchRule el_dominio_no_depende_de_frameworks = noClasses()
            .that()
            .resideInAPackage("..dominio..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("org.springframework..", "org.jooq..", "tools.jackson..")
            .allowEmptyShould(true);

    @Test
    void los_modulos_respetan_sus_limites() {
        ApplicationModules.of(PuriyApplication.class).verify();
    }
}
