package pe.puriy.rutas.aplicacion.servicio;

import java.util.List;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.puriy.rutas.aplicacion.puerto.entrada.ObtenerSalidaParaReservaQuery;
import pe.puriy.rutas.aplicacion.puerto.entrada.ObtenerSalidaParaReservaUseCase;
import pe.puriy.rutas.aplicacion.puerto.entrada.SalidaParaReserva;
import pe.puriy.rutas.aplicacion.puerto.entrada.SalidaParaReserva.AsientoDeLaSalida;
import pe.puriy.rutas.aplicacion.puerto.entrada.SalidaParaReserva.ParadaDeLaSalida;
import pe.puriy.rutas.aplicacion.puerto.salida.BusRepositoryPort;
import pe.puriy.rutas.aplicacion.puerto.salida.RutaRepositoryPort;
import pe.puriy.rutas.aplicacion.puerto.salida.SalidaRepositoryPort;
import pe.puriy.rutas.dominio.excepcion.SalidaNoEncontradaException;
import pe.puriy.rutas.dominio.modelo.Bus;
import pe.puriy.rutas.dominio.modelo.Ruta;
import pe.puriy.rutas.dominio.modelo.Salida;

@Service
@RequiredArgsConstructor
class ObtenerSalidaParaReservaService implements ObtenerSalidaParaReservaUseCase {

    private final SalidaRepositoryPort salidas;
    private final RutaRepositoryPort rutas;
    private final BusRepositoryPort buses;

    @Override
    @Transactional(readOnly = true)
    public SalidaParaReserva ejecutar(ObtenerSalidaParaReservaQuery consulta) {
        var salida =
                salidas.buscarPorId(consulta.id()).orElseThrow(() -> new SalidaNoEncontradaException(consulta.id()));
        var ruta = rutas.buscarPorId(salida.rutaId())
                .orElseThrow(() -> new IllegalStateException("falta la ruta de la salida " + salida.id()));
        var bus = buses.buscarPorId(salida.busId())
                .orElseThrow(() -> new IllegalStateException("falta el bus de la salida " + salida.id()));
        return new SalidaParaReserva(
                salida.id(), salida.rutaId(), salida.tipoServicio(), paradas(salida, ruta), asientos(bus));
    }

    private static List<ParadaDeLaSalida> paradas(Salida salida, Ruta ruta) {
        var partida = salida.horario().parteEn();
        return IntStream.range(0, ruta.paradas().size())
                .mapToObj(orden -> new ParadaDeLaSalida(
                        orden, ruta.paradas().get(orden).terminalId(), ruta.horaDePaso(partida, orden)))
                .toList();
    }

    private static List<AsientoDeLaSalida> asientos(Bus bus) {
        return bus.datos().mapa().asientos().stream()
                .map(asiento -> new AsientoDeLaSalida(asiento.numero(), asiento.piso()))
                .toList();
    }
}
