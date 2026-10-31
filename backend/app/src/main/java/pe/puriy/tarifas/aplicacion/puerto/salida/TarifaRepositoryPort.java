package pe.puriy.tarifas.aplicacion.puerto.salida;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import pe.puriy.compartido.dominio.RutaId;
import pe.puriy.tarifas.dominio.modelo.CriterioDeTarifa;
import pe.puriy.tarifas.dominio.modelo.Tarifa;
import pe.puriy.tarifas.dominio.modelo.TarifaId;

public interface TarifaRepositoryPort {

    void agregar(Tarifa tarifa);

    Tarifa actualizar(Tarifa tarifa);

    void eliminar(Tarifa tarifa);

    Optional<Tarifa> buscarPorId(TarifaId id);

    List<Tarifa> listarPorRuta(RutaId rutaId);

    List<Tarifa> listarVigentesEn(CriterioDeTarifa criterio, LocalDate fechaDeViaje);
}
