package pe.puriy.reservas.aplicacion.servicio;

import java.time.Clock;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.puriy.reservas.aplicacion.puerto.entrada.BuscarSalidasDisponiblesQuery;
import pe.puriy.reservas.aplicacion.puerto.entrada.BuscarSalidasDisponiblesUseCase;
import pe.puriy.reservas.aplicacion.puerto.entrada.SalidaDisponible;
import pe.puriy.reservas.aplicacion.puerto.salida.SalidaPort;
import pe.puriy.reservas.dominio.modelo.PoliticaReserva;

@Service
@RequiredArgsConstructor
class BuscarSalidasDisponiblesService implements BuscarSalidasDisponiblesUseCase {

    private final SalidaPort salidas;
    private final CalculadoraDeDisponibilidad disponibilidad;
    private final PoliticaReserva politica;
    private final Clock reloj;

    @Override
    public List<SalidaDisponible> ejecutar(BuscarSalidasDisponiblesQuery consulta) {
        var candidatas =
                salidas.buscarEntreCiudades(consulta.ciudadOrigen(), consulta.ciudadDestino(), consulta.fechaDeViaje());
        return disponibilidad.ofrecer(
                candidatas, consulta.cantidadPasajeros(), politica.para(consulta.canal()), reloj.instant());
    }
}
