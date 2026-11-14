package pe.puriy.reservas.aplicacion.servicio;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.puriy.compartido.dominio.ClaveDeIdempotenciaReutilizadaException;
import pe.puriy.reservas.aplicacion.puerto.entrada.DetalleItinerario;
import pe.puriy.reservas.aplicacion.puerto.entrada.RetenerItinerarioCommand;
import pe.puriy.reservas.aplicacion.puerto.entrada.RetenerItinerarioCommand.TramoDelItinerario;
import pe.puriy.reservas.aplicacion.puerto.entrada.RetenerItinerarioUseCase;
import pe.puriy.reservas.aplicacion.puerto.salida.ItinerarioRepositoryPort;
import pe.puriy.reservas.aplicacion.puerto.salida.SalidaPort;
import pe.puriy.reservas.aplicacion.puerto.salida.TarifaPort;
import pe.puriy.reservas.dominio.excepcion.ConexionInvalidaException;
import pe.puriy.reservas.dominio.excepcion.SalidaNoEncontradaException;
import pe.puriy.reservas.dominio.modelo.AsientosSolicitados;
import pe.puriy.reservas.dominio.modelo.Itinerario;
import pe.puriy.reservas.dominio.modelo.PoliticaDeConexion;
import pe.puriy.reservas.dominio.modelo.PoliticaReserva;
import pe.puriy.reservas.dominio.modelo.Salida;
import pe.puriy.reservas.dominio.modelo.SolicitudDeItinerario;
import pe.puriy.reservas.dominio.modelo.Tramo;
import pe.puriy.reservas.dominio.modelo.TramoPedido;

@Service
@RequiredArgsConstructor
class RetenerItinerarioService implements RetenerItinerarioUseCase {

    private final ItinerarioRepositoryPort itinerarios;
    private final SalidaPort salidas;
    private final TarifaPort tarifas;
    private final PoliticaReserva politica;
    private final PoliticaDeConexion conexiones;
    private final Clock reloj;

    @Override
    @Transactional
    public DetalleItinerario ejecutar(RetenerItinerarioCommand comando) {
        var ahora = reloj.instant();
        var primero = elegir(comando.primerTramo());
        var segundo = elegir(comando.segundoTramo());

        // Un reintento recibe lo ya retenido aunque desde entonces haya cerrado la venta o cambiado la tarifa
        var itinerario = itinerarios
                .buscarPorClave(comando.claveIdempotencia())
                .orElseGet(() -> retenerNuevo(comando, primero, segundo, ahora));
        if (!itinerario.correspondeA(
                comando.canal(), comando.titular(), primero.solicitados(), segundo.solicitados())) {
            throw new ClaveDeIdempotenciaReutilizadaException();
        }
        return DetalleItinerario.desde(itinerario, ahora);
    }

    private Itinerario retenerNuevo(
            RetenerItinerarioCommand comando, TramoElegido primero, TramoElegido segundo, Instant ahora) {
        var reglas = politica.para(comando.canal());
        exigirConexion(primero, segundo);
        primero.salida().exigirVentaAbierta(primero.tramo(), reglas, ahora);
        segundo.salida().exigirVentaAbierta(segundo.tramo(), reglas, ahora);

        var solicitud = new SolicitudDeItinerario(
                comando.claveIdempotencia(), comando.canal(), comando.titular(), pedido(primero), pedido(segundo));
        return itinerarios.agregarUnaVez(Itinerario.retener(solicitud, reglas, ahora));
    }

    private TramoElegido elegir(TramoDelItinerario pedido) {
        var salida =
                salidas.buscar(pedido.salidaId()).orElseThrow(() -> new SalidaNoEncontradaException(pedido.salidaId()));
        return new TramoElegido(salida, salida.tramoEntre(pedido.origen(), pedido.destino()), pedido.asientos());
    }

    private void exigirConexion(TramoElegido primero, TramoElegido segundo) {
        var llegada = primero.salida().paradaDeDestino(primero.tramo());
        var partida = segundo.salida().paradaDeOrigen(segundo.tramo());
        if (primero.salida().id().equals(segundo.salida().id()) || !conexiones.permite(llegada, partida)) {
            throw new ConexionInvalidaException();
        }
    }

    private TramoPedido pedido(TramoElegido elegido) {
        var salida = elegido.salida();
        var asientos = AsientosConPrecio.de(tarifas, salida, elegido.tramo(), elegido.asientos());
        return new TramoPedido(salida.id(), elegido.tramo(), asientos);
    }

    private record TramoElegido(Salida salida, Tramo tramo, List<Integer> asientos) {

        AsientosSolicitados solicitados() {
            return new AsientosSolicitados(salida.id(), tramo, asientos);
        }
    }
}
