package pe.puriy.tarifas.aplicacion.servicio;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import pe.puriy.compartido.dominio.EdicionConcurrenteException;
import pe.puriy.compartido.dominio.RutaId;
import pe.puriy.tarifas.aplicacion.puerto.salida.TarifaRepositoryPort;
import pe.puriy.tarifas.dominio.excepcion.TarifaSuperpuestaException;
import pe.puriy.tarifas.dominio.modelo.CriterioDeTarifa;
import pe.puriy.tarifas.dominio.modelo.Tarifa;
import pe.puriy.tarifas.dominio.modelo.TarifaId;

class TarifaRepositoryEnMemoria implements TarifaRepositoryPort {

    private final Map<TarifaId, Tarifa> tarifas = new LinkedHashMap<>();

    @Override
    public void agregar(Tarifa tarifa) {
        exigirSinCruces(tarifa);
        tarifas.put(tarifa.id(), copia(tarifa, tarifa.version()));
    }

    @Override
    public Tarifa actualizar(Tarifa tarifa) {
        exigirVersion(tarifa);
        exigirSinCruces(tarifa);
        var actualizada = copia(tarifa, tarifa.version() + 1);
        tarifas.put(tarifa.id(), actualizada);
        return copia(actualizada, actualizada.version());
    }

    @Override
    public void eliminar(Tarifa tarifa) {
        exigirVersion(tarifa);
        tarifas.remove(tarifa.id());
    }

    @Override
    public Optional<Tarifa> buscarPorId(TarifaId id) {
        return Optional.ofNullable(tarifas.get(id)).map(tarifa -> copia(tarifa, tarifa.version()));
    }

    @Override
    public List<Tarifa> listarPorRuta(RutaId rutaId) {
        return tarifas.values().stream()
                .filter(tarifa -> tarifa.criterio().par().rutaId().equals(rutaId))
                .toList();
    }

    @Override
    public List<Tarifa> listarVigentesEn(CriterioDeTarifa criterio, LocalDate fechaDeViaje) {
        return tarifas.values().stream()
                .filter(tarifa -> tarifa.criterio().equals(criterio) && tarifa.estaVigenteEn(fechaDeViaje))
                .toList();
    }

    private void exigirVersion(Tarifa tarifa) {
        var guardada = tarifas.get(tarifa.id());
        if (guardada == null || guardada.version() != tarifa.version()) {
            throw new EdicionConcurrenteException();
        }
    }

    private void exigirSinCruces(Tarifa candidata) {
        if (tarifas.values().stream().anyMatch(candidata::seCruzaCon)) {
            throw new TarifaSuperpuestaException(candidata.vigencia().tipo());
        }
    }

    private static Tarifa copia(Tarifa tarifa, long version) {
        return Tarifa.reconstituir()
                .id(tarifa.id())
                .criterio(tarifa.criterio())
                .vigencia(tarifa.vigencia())
                .precio(tarifa.precio())
                .version(version)
                .build();
    }
}
