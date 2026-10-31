package pe.puriy.tarifas.aplicacion.servicio;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.puriy.tarifas.aplicacion.puerto.entrada.DefinirTarifaCommand;
import pe.puriy.tarifas.aplicacion.puerto.entrada.DefinirTarifaUseCase;
import pe.puriy.tarifas.aplicacion.puerto.entrada.DetalleTarifa;
import pe.puriy.tarifas.aplicacion.puerto.salida.RutaPort;
import pe.puriy.tarifas.aplicacion.puerto.salida.TarifaRepositoryPort;
import pe.puriy.tarifas.dominio.excepcion.RutaNoEncontradaException;
import pe.puriy.tarifas.dominio.modelo.Tarifa;

@Service
@RequiredArgsConstructor
class DefinirTarifaService implements DefinirTarifaUseCase {

    private final TarifaRepositoryPort tarifas;
    private final RutaPort rutas;

    @Override
    @Transactional
    public DetalleTarifa ejecutar(DefinirTarifaCommand comando) {
        var par = comando.criterio().par();
        var paradas = rutas.buscarParadas(par.rutaId()).orElseThrow(() -> new RutaNoEncontradaException(par.rutaId()));
        paradas.exigirTramo(par);

        var tarifa = Tarifa.definir(comando.criterio(), comando.vigencia(), comando.precio());
        tarifas.agregar(tarifa);
        return DetalleTarifa.desde(tarifa);
    }
}
