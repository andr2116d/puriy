package pe.puriy.reservas.aplicacion.servicio;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import pe.puriy.compartido.dominio.Monto;
import pe.puriy.reservas.aplicacion.puerto.salida.TarifaPort;
import pe.puriy.reservas.dominio.modelo.ConsultaDePrecio;

class TarifaPortEnMemoria implements TarifaPort {

    private final Map<Integer, Monto> precioPorPiso = new HashMap<>();
    private final List<LocalDate> fechasConsultadas = new ArrayList<>();

    void definir(int piso, long centimos) {
        precioPorPiso.put(piso, Monto.soles(centimos));
    }

    void quitar(int piso) {
        precioPorPiso.remove(piso);
    }

    List<LocalDate> fechasConsultadas() {
        return List.copyOf(fechasConsultadas);
    }

    @Override
    public Optional<Monto> buscarPrecio(ConsultaDePrecio consulta) {
        fechasConsultadas.add(consulta.fechaDeViaje());
        return Optional.ofNullable(precioPorPiso.get(consulta.piso()));
    }
}
