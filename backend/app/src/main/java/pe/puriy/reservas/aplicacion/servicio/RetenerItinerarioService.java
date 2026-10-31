package pe.puriy.reservas.aplicacion.servicio;

import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.puriy.compartido.dominio.SalidaId;
import pe.puriy.reservas.aplicacion.puerto.entrada.DetalleItinerario;
import pe.puriy.reservas.aplicacion.puerto.entrada.RetenerItinerarioCommand;
import pe.puriy.reservas.aplicacion.puerto.entrada.RetenerItinerarioCommand.TramoDelItinerario;
import pe.puriy.reservas.aplicacion.puerto.entrada.RetenerItinerarioUseCase;
import pe.puriy.reservas.aplicacion.puerto.salida.ItinerarioRepositoryPort;
import pe.puriy.reservas.aplicacion.puerto.salida.SalidaPort;
import pe.puriy.reservas.aplicacion.puerto.salida.TarifaPort;
import pe.puriy.reservas.dominio.excepcion.ConexionInvalidaException;
import pe.puriy.reservas.dominio.excepcion.SalidaNoEncontradaException;
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
        var reglas = politica.para(comando.canal());
        var primera = obtener(comando.primerTramo().salidaId());
        var segunda = obtener(comando.segundoTramo().salidaId());
        var tramoEnLaPrimera = tramoEn(primera, comando.primerTramo());
        var tramoEnLaSegunda = tramoEn(segunda, comando.segundoTramo());
        exigirConexion(primera, tramoEnLaPrimera, segunda, tramoEnLaSegunda);
        primera.exigirVentaAbierta(tramoEnLaPrimera, reglas, ahora);
        segunda.exigirVentaAbierta(tramoEnLaSegunda, reglas, ahora);

        var solicitud = new SolicitudDeItinerario(
                comando.claveIdempotencia(),
                comando.canal(),
                comando.titular(),
                pedido(primera, tramoEnLaPrimera, comando.primerTramo()),
                pedido(segunda, tramoEnLaSegunda, comando.segundoTramo()));
        var itinerario = Itinerario.retener(solicitud, reglas, ahora);
        return DetalleItinerario.desde(itinerarios.agregarUnaVez(itinerario), ahora);
    }

    private Salida obtener(SalidaId id) {
        return salidas.buscar(id).orElseThrow(() -> new SalidaNoEncontradaException(id));
    }

    private static Tramo tramoEn(Salida salida, TramoDelItinerario tramo) {
        return salida.tramoEntre(tramo.origen(), tramo.destino());
    }

    private void exigirConexion(Salida primera, Tramo tramoEnLaPrimera, Salida segunda, Tramo tramoEnLaSegunda) {
        var llegada = primera.paradaDeDestino(tramoEnLaPrimera);
        var partida = segunda.paradaDeOrigen(tramoEnLaSegunda);
        if (primera.id().equals(segunda.id()) || !conexiones.permite(llegada, partida)) {
            throw new ConexionInvalidaException();
        }
    }

    private TramoPedido pedido(Salida salida, Tramo tramo, TramoDelItinerario pedido) {
        return new TramoPedido(salida.id(), tramo, AsientosConPrecio.de(tarifas, salida, tramo, pedido.asientos()));
    }
}
