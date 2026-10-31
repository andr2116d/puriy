package pe.puriy.reservas.adaptador.salida.tarifas;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.puriy.compartido.dominio.Monto;
import pe.puriy.compartido.dominio.ReglaDeNegocioException;
import pe.puriy.reservas.aplicacion.puerto.salida.TarifaPort;
import pe.puriy.reservas.dominio.modelo.ConsultaDePrecio;
import pe.puriy.tarifas.aplicacion.puerto.entrada.ObtenerPrecioQuery;
import pe.puriy.tarifas.aplicacion.puerto.entrada.ObtenerPrecioUseCase;

@Component
@RequiredArgsConstructor
class TarifaTarifasAdapter implements TarifaPort {

    private final ObtenerPrecioUseCase obtenerPrecio;

    @Override
    public Optional<Monto> buscarPrecio(ConsultaDePrecio consulta) {
        var query = ObtenerPrecioQuery.para(
                consulta.rutaId(),
                consulta.origen(),
                consulta.destino(),
                consulta.tipoServicio(),
                consulta.piso(),
                consulta.fechaDeViaje());
        try {
            return Optional.of(obtenerPrecio.ejecutar(query).precio());
        } catch (ReglaDeNegocioException excepcion) {
            return Optional.empty();
        }
    }
}
