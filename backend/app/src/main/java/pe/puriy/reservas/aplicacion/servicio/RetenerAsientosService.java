package pe.puriy.reservas.aplicacion.servicio;

import java.time.Clock;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.puriy.compartido.dominio.ClaveDeIdempotenciaReutilizadaException;
import pe.puriy.reservas.aplicacion.puerto.entrada.DetalleReserva;
import pe.puriy.reservas.aplicacion.puerto.entrada.RetenerAsientosCommand;
import pe.puriy.reservas.aplicacion.puerto.entrada.RetenerAsientosUseCase;
import pe.puriy.reservas.aplicacion.puerto.salida.ReservaRepositoryPort;
import pe.puriy.reservas.aplicacion.puerto.salida.SalidaPort;
import pe.puriy.reservas.aplicacion.puerto.salida.TarifaPort;
import pe.puriy.reservas.dominio.excepcion.SalidaNoEncontradaException;
import pe.puriy.reservas.dominio.modelo.AsientosSolicitados;
import pe.puriy.reservas.dominio.modelo.PoliticaReserva;
import pe.puriy.reservas.dominio.modelo.Reserva;
import pe.puriy.reservas.dominio.modelo.Salida;
import pe.puriy.reservas.dominio.modelo.SolicitudDeReserva;
import pe.puriy.reservas.dominio.modelo.Tramo;

@Service
@RequiredArgsConstructor
class RetenerAsientosService implements RetenerAsientosUseCase {

    private final ReservaRepositoryPort reservas;
    private final SalidaPort salidas;
    private final TarifaPort tarifas;
    private final PoliticaReserva politica;
    private final Clock reloj;

    @Override
    @Transactional
    public DetalleReserva ejecutar(RetenerAsientosCommand comando) {
        var ahora = reloj.instant();
        var salida = salidas.buscar(comando.salidaId())
                .orElseThrow(() -> new SalidaNoEncontradaException(comando.salidaId()));
        var tramo = salida.tramoEntre(comando.origen(), comando.destino());

        // Un reintento recibe lo ya retenido aunque desde entonces haya cerrado la venta o cambiado la tarifa
        var reserva = reservas.buscarPorClave(comando.claveIdempotencia())
                .orElseGet(() -> retenerNueva(comando, salida, tramo, ahora));
        var solicitados = new AsientosSolicitados(salida.id(), tramo, comando.asientos());
        if (!reserva.correspondeA(comando.canal(), comando.titular(), solicitados)) {
            throw new ClaveDeIdempotenciaReutilizadaException();
        }
        return DetalleReserva.desde(reserva, ahora);
    }

    private Reserva retenerNueva(RetenerAsientosCommand comando, Salida salida, Tramo tramo, Instant ahora) {
        var reglas = politica.para(comando.canal());
        salida.exigirVentaAbierta(tramo, reglas, ahora);

        var solicitud = new SolicitudDeReserva(
                comando.claveIdempotencia(), salida.id(), tramo, comando.canal(), comando.titular());
        var reserva = Reserva.retener(
                solicitud, AsientosConPrecio.de(tarifas, salida, tramo, comando.asientos()), reglas, ahora);
        return reservas.agregarUnaVez(reserva);
    }
}
