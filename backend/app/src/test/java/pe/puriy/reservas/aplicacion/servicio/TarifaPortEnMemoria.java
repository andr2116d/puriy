package pe.puriy.reservas.aplicacion.servicio;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import pe.puriy.compartido.dominio.Monto;
import pe.puriy.reservas.aplicacion.puerto.salida.TarifaPort;
import pe.puriy.reservas.dominio.modelo.Salida;
import pe.puriy.reservas.dominio.modelo.Tramo;

class TarifaPortEnMemoria implements TarifaPort {

    private final Map<Integer, Monto> precioPorPiso = new HashMap<>();
    private final List<LocalDate> fechasConsultadas = new ArrayList<>();

    void definir(int piso, long centimos) {
        precioPorPiso.put(piso, Monto.soles(centimos));
    }

    List<LocalDate> fechasConsultadas() {
        return List.copyOf(fechasConsultadas);
    }

    @Override
    public Optional<Monto> buscarPrecio(Salida salida, Tramo tramo, int piso) {
        fechasConsultadas.add(salida.fechaDeViaje(tramo));
        return Optional.ofNullable(precioPorPiso.get(piso));
    }
}
