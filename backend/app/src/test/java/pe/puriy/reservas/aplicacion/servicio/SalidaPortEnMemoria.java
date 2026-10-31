package pe.puriy.reservas.aplicacion.servicio;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import pe.puriy.compartido.dominio.SalidaId;
import pe.puriy.compartido.dominio.ZonaHoraria;
import pe.puriy.reservas.aplicacion.puerto.salida.SalidaPort;
import pe.puriy.reservas.dominio.modelo.Salida;
import pe.puriy.reservas.dominio.modelo.SalidaCandidata;

class SalidaPortEnMemoria implements SalidaPort {

    private final Map<SalidaId, Salida> salidas = new HashMap<>();
    private final List<SalidaCandidata> candidatas = new ArrayList<>();

    void agregar(Salida salida) {
        salidas.put(salida.id(), salida);
    }

    void agregar(SalidaCandidata candidata) {
        candidatas.add(candidata);
    }

    @Override
    public Optional<Salida> buscar(SalidaId id) {
        return Optional.ofNullable(salidas.get(id));
    }

    @Override
    public List<SalidaCandidata> buscarEntreCiudades(
            String ciudadOrigen, String ciudadDestino, LocalDate fechaDeViaje) {
        return candidatas.stream()
                .filter(candidata -> candidata.origen().ciudad().equals(ciudadOrigen))
                .filter(candidata -> candidata.destino().ciudad().equals(ciudadDestino))
                .filter(candidata -> fechaDe(candidata).equals(fechaDeViaje))
                .toList();
    }

    private static LocalDate fechaDe(SalidaCandidata candidata) {
        return candidata.origen().horaDePaso().atZone(ZonaHoraria.PERU).toLocalDate();
    }
}
