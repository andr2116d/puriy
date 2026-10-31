package pe.puriy.reservas.adaptador.salida.rutas;

import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.puriy.compartido.dominio.RecursoNoEncontradoException;
import pe.puriy.compartido.dominio.SalidaId;
import pe.puriy.reservas.aplicacion.puerto.salida.SalidaPort;
import pe.puriy.reservas.dominio.modelo.Parada;
import pe.puriy.reservas.dominio.modelo.Salida;
import pe.puriy.rutas.aplicacion.puerto.entrada.ObtenerSalidaParaReservaQuery;
import pe.puriy.rutas.aplicacion.puerto.entrada.ObtenerSalidaParaReservaUseCase;
import pe.puriy.rutas.aplicacion.puerto.entrada.SalidaParaReserva;
import pe.puriy.rutas.aplicacion.puerto.entrada.SalidaParaReserva.AsientoDeLaSalida;

@Component
@RequiredArgsConstructor
class SalidaRutasAdapter implements SalidaPort {

    private final ObtenerSalidaParaReservaUseCase obtenerSalida;

    @Override
    public Optional<Salida> buscar(SalidaId id) {
        try {
            return Optional.of(aSalida(obtenerSalida.ejecutar(new ObtenerSalidaParaReservaQuery(id))));
        } catch (RecursoNoEncontradoException excepcion) {
            return Optional.empty();
        }
    }

    private static Salida aSalida(SalidaParaReserva salida) {
        var paradas = salida.paradas().stream()
                .map(parada -> new Parada(parada.orden(), parada.terminalId(), parada.horaDePaso()))
                .toList();
        var pisoPorAsiento = salida.asientos().stream()
                .collect(Collectors.toMap(AsientoDeLaSalida::numero, AsientoDeLaSalida::piso));
        return new Salida(salida.id(), salida.rutaId(), salida.tipoServicio(), paradas, pisoPorAsiento);
    }
}
