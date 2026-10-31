package pe.puriy.reservas.adaptador.salida.tarifas;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.puriy.compartido.dominio.Monto;
import pe.puriy.compartido.dominio.ReglaDeNegocioException;
import pe.puriy.reservas.aplicacion.puerto.salida.TarifaPort;
import pe.puriy.reservas.dominio.modelo.Salida;
import pe.puriy.reservas.dominio.modelo.Tramo;
import pe.puriy.tarifas.aplicacion.puerto.entrada.ObtenerPrecioQuery;
import pe.puriy.tarifas.aplicacion.puerto.entrada.ObtenerPrecioUseCase;

@Component
@RequiredArgsConstructor
class TarifaTarifasAdapter implements TarifaPort {

    private final ObtenerPrecioUseCase obtenerPrecio;

    @Override
    public Optional<Monto> buscarPrecio(Salida salida, Tramo tramo, int piso) {
        var consulta = ObtenerPrecioQuery.para(
                salida.rutaId(),
                salida.terminalEn(tramo.ordenOrigen()),
                salida.terminalEn(tramo.ordenDestino()),
                salida.tipoServicio(),
                piso,
                salida.fechaDeViaje(tramo));
        try {
            return Optional.of(obtenerPrecio.ejecutar(consulta).precio());
        } catch (ReglaDeNegocioException excepcion) {
            return Optional.empty();
        }
    }
}
