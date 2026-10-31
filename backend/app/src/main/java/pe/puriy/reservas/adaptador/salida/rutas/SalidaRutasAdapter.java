package pe.puriy.reservas.adaptador.salida.rutas;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.puriy.compartido.dominio.RecursoNoEncontradoException;
import pe.puriy.compartido.dominio.SalidaId;
import pe.puriy.compartido.dominio.ZonaHoraria;
import pe.puriy.reservas.aplicacion.puerto.salida.SalidaPort;
import pe.puriy.reservas.dominio.modelo.Parada;
import pe.puriy.reservas.dominio.modelo.ParadaDelTramo;
import pe.puriy.reservas.dominio.modelo.Salida;
import pe.puriy.reservas.dominio.modelo.SalidaCandidata;
import pe.puriy.rutas.aplicacion.puerto.entrada.BuscarSalidasDesdeCiudadQuery;
import pe.puriy.rutas.aplicacion.puerto.entrada.BuscarSalidasDesdeCiudadUseCase;
import pe.puriy.rutas.aplicacion.puerto.entrada.BuscarSalidasEntreCiudadesQuery;
import pe.puriy.rutas.aplicacion.puerto.entrada.BuscarSalidasEntreCiudadesUseCase;
import pe.puriy.rutas.aplicacion.puerto.entrada.BuscarSalidasHaciaCiudadQuery;
import pe.puriy.rutas.aplicacion.puerto.entrada.BuscarSalidasHaciaCiudadUseCase;
import pe.puriy.rutas.aplicacion.puerto.entrada.ObtenerSalidaParaReservaQuery;
import pe.puriy.rutas.aplicacion.puerto.entrada.ObtenerSalidaParaReservaUseCase;
import pe.puriy.rutas.aplicacion.puerto.entrada.SalidaEntreCiudades;
import pe.puriy.rutas.aplicacion.puerto.entrada.SalidaEntreCiudades.AsientosPorPiso;
import pe.puriy.rutas.aplicacion.puerto.entrada.SalidaParaReserva;
import pe.puriy.rutas.aplicacion.puerto.entrada.SalidaParaReserva.AsientoDeLaSalida;

@Component
@RequiredArgsConstructor
class SalidaRutasAdapter implements SalidaPort {

    private final ObtenerSalidaParaReservaUseCase obtenerSalida;
    private final BuscarSalidasEntreCiudadesUseCase buscarSalidas;
    private final BuscarSalidasDesdeCiudadUseCase buscarDesdeCiudad;
    private final BuscarSalidasHaciaCiudadUseCase buscarHaciaCiudad;

    @Override
    public Optional<Salida> buscar(SalidaId id) {
        try {
            return Optional.of(aSalida(obtenerSalida.ejecutar(new ObtenerSalidaParaReservaQuery(id))));
        } catch (RecursoNoEncontradoException excepcion) {
            return Optional.empty();
        }
    }

    @Override
    public List<SalidaCandidata> buscarEntreCiudades(
            String ciudadOrigen, String ciudadDestino, LocalDate fechaDeViaje) {
        var consulta = new BuscarSalidasEntreCiudadesQuery(ciudadOrigen, ciudadDestino, fechaDeViaje);
        return aCandidatas(buscarSalidas.ejecutar(consulta));
    }

    @Override
    public List<SalidaCandidata> buscarDesdeCiudad(String ciudad, LocalDate fechaDeViaje) {
        var consulta = new BuscarSalidasDesdeCiudadQuery(
                ciudad,
                fechaDeViaje.atStartOfDay(ZonaHoraria.PERU).toInstant(),
                fechaDeViaje.plusDays(1).atStartOfDay(ZonaHoraria.PERU).toInstant());
        return aCandidatas(buscarDesdeCiudad.ejecutar(consulta));
    }

    @Override
    public List<SalidaCandidata> buscarHaciaCiudad(String ciudad, Instant desde, Instant hasta) {
        return aCandidatas(buscarHaciaCiudad.ejecutar(new BuscarSalidasHaciaCiudadQuery(ciudad, desde, hasta)));
    }

    private static List<SalidaCandidata> aCandidatas(List<SalidaEntreCiudades> salidas) {
        return salidas.stream().map(SalidaRutasAdapter::aCandidata).toList();
    }

    private static Salida aSalida(SalidaParaReserva salida) {
        var paradas = salida.paradas().stream()
                .map(parada -> new Parada(parada.orden(), parada.terminalId(), parada.horaDePaso()))
                .toList();
        var pisoPorAsiento = salida.asientos().stream()
                .collect(Collectors.toMap(AsientoDeLaSalida::numero, AsientoDeLaSalida::piso));
        return new Salida(salida.id(), salida.rutaId(), salida.tipoServicio(), paradas, pisoPorAsiento);
    }

    private static SalidaCandidata aCandidata(SalidaEntreCiudades salida) {
        var asientosPorPiso = salida.asientosPorPiso().stream()
                .collect(Collectors.toMap(AsientosPorPiso::piso, AsientosPorPiso::cantidad));
        return new SalidaCandidata(
                salida.id(),
                salida.rutaId(),
                salida.tipoServicio(),
                aParada(salida.origen()),
                aParada(salida.destino()),
                asientosPorPiso);
    }

    private static ParadaDelTramo aParada(SalidaEntreCiudades.ParadaDelTramo parada) {
        return new ParadaDelTramo(
                parada.orden(), parada.terminalId(), parada.terminal(), parada.ciudad(), parada.horaDePaso());
    }
}
