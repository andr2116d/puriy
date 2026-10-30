package pe.puriy.rutas.aplicacion.servicio;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pe.puriy.rutas.aplicacion.puerto.entrada.DetalleTerminal;
import pe.puriy.rutas.aplicacion.puerto.entrada.ListarTerminalesQuery;
import pe.puriy.rutas.dominio.modelo.Terminal;
import pe.puriy.rutas.dominio.modelo.TerminalesDeEjemplo;

class ListarTerminalesServiceTest {

    private final TerminalRepositoryEnMemoria terminales = new TerminalRepositoryEnMemoria();
    private final ListarTerminalesService servicio = new ListarTerminalesService(terminales);

    @BeforeEach
    void registrarTerminales() {
        terminales.agregar(Terminal.registrar(TerminalesDeEjemplo.trujillo()));
        terminales.agregar(Terminal.registrar(TerminalesDeEjemplo.plazaNorte()));
        terminales.agregar(Terminal.registrar(TerminalesDeEjemplo.javierPrado()));
    }

    @Test
    void sin_filtro_lista_todos_ordenados_por_ciudad_y_nombre() {
        var listado = servicio.ejecutar(ListarTerminalesQuery.todos());

        assertThat(listado)
                .extracting(DetalleTerminal::nombre)
                .containsExactly("Terminal Javier Prado", "Terminal Plaza Norte", "Terminal Trujillo");
    }

    @Test
    void filtra_por_ciudad_sin_importar_mayusculas() {
        var listado = servicio.ejecutar(new ListarTerminalesQuery("lima"));

        assertThat(listado)
                .extracting(DetalleTerminal::ciudad)
                .containsOnly("Lima")
                .hasSize(2);
    }
}
