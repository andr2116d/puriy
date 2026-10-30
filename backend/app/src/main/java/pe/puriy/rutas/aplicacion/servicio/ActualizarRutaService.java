package pe.puriy.rutas.aplicacion.servicio;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.puriy.compartido.dominio.EdicionConcurrenteException;
import pe.puriy.rutas.aplicacion.puerto.entrada.ActualizarRutaCommand;
import pe.puriy.rutas.aplicacion.puerto.entrada.ActualizarRutaUseCase;
import pe.puriy.rutas.aplicacion.puerto.entrada.DetalleRuta;
import pe.puriy.rutas.aplicacion.puerto.salida.RutaRepositoryPort;
import pe.puriy.rutas.dominio.excepcion.RutaNoEncontradaException;
import pe.puriy.rutas.dominio.servicio.VerificadorDeParadas;

@Service
@RequiredArgsConstructor
class ActualizarRutaService implements ActualizarRutaUseCase {

    private final RutaRepositoryPort rutas;
    private final CargadorDeTerminales cargadorDeTerminales;

    @Override
    @Transactional
    public DetalleRuta ejecutar(ActualizarRutaCommand comando) {
        var ruta = rutas.buscarPorId(comando.id()).orElseThrow(() -> new RutaNoEncontradaException(comando.id()));
        if (ruta.version() != comando.version()) {
            throw new EdicionConcurrenteException();
        }
        var datos = comando.datos();
        var terminales = cargadorDeTerminales.porId(datos.terminales());
        VerificadorDeParadas.exigirTerminalesDeEmbarque(datos, terminales);

        ruta.actualizar(datos);
        return DetalleRuta.desde(rutas.actualizar(ruta), terminales);
    }
}
