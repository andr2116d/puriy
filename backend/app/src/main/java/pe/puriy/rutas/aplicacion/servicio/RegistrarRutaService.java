package pe.puriy.rutas.aplicacion.servicio;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.puriy.rutas.aplicacion.puerto.entrada.DetalleRuta;
import pe.puriy.rutas.aplicacion.puerto.entrada.RegistrarRutaCommand;
import pe.puriy.rutas.aplicacion.puerto.entrada.RegistrarRutaUseCase;
import pe.puriy.rutas.aplicacion.puerto.salida.RutaRepositoryPort;
import pe.puriy.rutas.dominio.modelo.Ruta;
import pe.puriy.rutas.dominio.servicio.VerificadorDeParadas;

@Service
@RequiredArgsConstructor
class RegistrarRutaService implements RegistrarRutaUseCase {

    private final RutaRepositoryPort rutas;
    private final CargadorDeTerminales cargadorDeTerminales;

    @Override
    @Transactional
    public DetalleRuta ejecutar(RegistrarRutaCommand comando) {
        var datos = comando.datos();
        var terminales = cargadorDeTerminales.porId(datos.terminales());
        VerificadorDeParadas.exigirTerminalesDeEmbarque(datos, terminales);

        var ruta = Ruta.trazar(datos);
        rutas.agregar(ruta);
        return DetalleRuta.desde(ruta, terminales);
    }
}
