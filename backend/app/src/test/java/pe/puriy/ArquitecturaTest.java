package pe.puriy;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.onionArchitecture;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

@AnalyzeClasses(packages = "pe.puriy", importOptions = ImportOption.DoNotIncludeTests.class)
class ArquitecturaTest {

    private static final String PAQUETE_MODULOS = "pe.puriy.";
    private static final String PAQUETE_BASEDATOS = "puriy.basedatos.";

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
            .resideInAnyPackage("org.springframework..", "org.jooq..", "tools.jackson..", "puriy.basedatos..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule solo_la_persistencia_usa_las_tablas = noClasses()
            .that()
            .resideOutsideOfPackage("..adaptador.salida.persistencia..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("puriy.basedatos..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule cada_modulo_usa_solo_las_tablas_de_su_esquema = classes()
            .that()
            .resideInAPackage("..adaptador.salida.persistencia..")
            .should(usarSoloTablasDeSuEsquema())
            .allowEmptyShould(true);

    @Test
    void los_modulos_respetan_sus_limites() {
        ApplicationModules.of(PuriyApplication.class).verify();
    }

    private static ArchCondition<JavaClass> usarSoloTablasDeSuEsquema() {
        return new ArchCondition<>("usar solo las tablas del esquema de su módulo") {
            @Override
            public void check(JavaClass clase, ConditionEvents eventos) {
                var modulo = primerSegmento(clase.getPackageName(), PAQUETE_MODULOS);
                clase.getDirectDependenciesFromSelf().stream()
                        .filter(dependencia ->
                                dependencia.getTargetClass().getPackageName().startsWith(PAQUETE_BASEDATOS))
                        .filter(dependencia -> !primerSegmento(
                                        dependencia.getTargetClass().getPackageName(), PAQUETE_BASEDATOS)
                                .equals(modulo))
                        .forEach(dependencia ->
                                eventos.add(SimpleConditionEvent.violated(dependencia, dependencia.getDescription())));
            }
        };
    }

    private static String primerSegmento(String paquete, String prefijo) {
        if (!paquete.startsWith(prefijo)) {
            return "";
        }
        var resto = paquete.substring(prefijo.length());
        var punto = resto.indexOf('.');
        return punto < 0 ? resto : resto.substring(0, punto);
    }
}
