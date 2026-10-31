package pe.puriy.reservas.aplicacion.servicio;

import java.time.Clock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.puriy.compartido.dominio.Monto;
import pe.puriy.reservas.aplicacion.puerto.entrada.DetalleReserva;
import pe.puriy.reservas.aplicacion.puerto.entrada.RetenerAsientosCommand;
import pe.puriy.reservas.aplicacion.puerto.entrada.RetenerAsientosUseCase;
import pe.puriy.reservas.aplicacion.puerto.salida.ReservaRepositoryPort;
import pe.puriy.reservas.aplicacion.puerto.salida.SalidaPort;
import pe.puriy.reservas.aplicacion.puerto.salida.TarifaPort;
import pe.puriy.reservas.dominio.excepcion.SalidaNoEncontradaException;
import pe.puriy.reservas.dominio.excepcion.TramoSinTarifaException;
import pe.puriy.reservas.dominio.modelo.AsientoReservado;
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
        var reglas = politica.para(comando.canal());
        salida.exigirVentaAbierta(tramo, reglas, ahora);

        var solicitud = new SolicitudDeReserva(
                comando.claveIdempotencia(), salida.id(), tramo, comando.canal(), comando.titular());
        var reserva = Reserva.retener(solicitud, conPrecio(salida, tramo, comando.asientos()), reglas, ahora);
        return DetalleReserva.desde(reservas.agregarUnaVez(reserva), ahora);
    }

    private List<AsientoReservado> conPrecio(Salida salida, Tramo tramo, List<Integer> numeros) {
        var precioPorPiso = new HashMap<Integer, Monto>();
        var asientos = new ArrayList<AsientoReservado>();
        for (var numero : numeros) {
            var piso = salida.pisoDe(numero);
            var precio = precioPorPiso.get(piso);
            if (precio == null) {
                precio = tarifas.buscarPrecio(salida, tramo, piso).orElseThrow(() -> new TramoSinTarifaException(piso));
                precioPorPiso.put(piso, precio);
            }
            asientos.add(new AsientoReservado(numero, piso, precio));
        }
        return asientos;
    }
}
