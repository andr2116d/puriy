package pe.puriy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(ContenedoresDePrueba.class)
class PuriyApplicationIT {

    @Test
    void arranca_y_crea_un_esquema_por_modulo(@Autowired DSLContext dsl) {
        var esquemas = dsl.select(field("schema_name", String.class))
                .from(table("information_schema.schemata"))
                .fetch(field("schema_name", String.class));

        assertThat(esquemas)
                .contains(
                        "rutas",
                        "tarifas",
                        "reservas",
                        "compras",
                        "pagos",
                        "boletos",
                        "operacion",
                        "notificaciones",
                        "identidad");
    }
}
