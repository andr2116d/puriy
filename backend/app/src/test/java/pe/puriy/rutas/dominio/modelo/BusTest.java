package pe.puriy.rutas.dominio.modelo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class BusTest {

    private final Bus bus = Bus.registrar(BusesDeEjemplo.dosPisos());

    @Test
    void un_bus_registrado_empieza_en_la_version_inicial() {
        assertThat(bus.version()).isZero();
    }

    @Test
    void actualizar_cambia_el_mapa_y_conserva_la_identidad() {
        var id = bus.id();

        bus.actualizar(BusesDeEjemplo.unPiso());

        assertThat(bus.id()).isEqualTo(id);
        assertThat(bus.datos().placa()).isEqualTo(new Placa("B2C-456"));
        assertThat(bus.datos().mapa().cantidadPisos()).isEqualTo(1);
    }
}
