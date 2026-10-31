package pe.puriy.reservas.aplicacion.servicio;

import java.time.Clock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.puriy.compartido.dominio.Monto;
import pe.puriy.reservas.aplicacion.puerto.entrada.BuscarSalidasDisponiblesQuery;
import pe.puriy.reservas.aplicacion.puerto.entrada.BuscarSalidasDisponiblesUseCase;
import pe.puriy.reservas.aplicacion.puerto.entrada.SalidaDisponible;
import pe.puriy.reservas.aplicacion.puerto.salida.ReservaLecturaPort;
import pe.puriy.reservas.aplicacion.puerto.salida.SalidaPort;
import pe.puriy.reservas.aplicacion.puerto.salida.TarifaPort;
import pe.puriy.reservas.dominio.modelo.AsientosOcupados;
import pe.puriy.reservas.dominio.modelo.ConsultaDePrecio;
import pe.puriy.reservas.dominio.modelo.PisoDisponible;
import pe.puriy.reservas.dominio.modelo.PoliticaReserva;
import pe.puriy.reservas.dominio.modelo.SalidaCandidata;

@Service
@RequiredArgsConstructor
class BuscarSalidasDisponiblesService implements BuscarSalidasDisponiblesUseCase {

    private final SalidaPort salidas;
    private final ReservaLecturaPort reservas;
    private final TarifaPort tarifas;
    private final PoliticaReserva politica;
    private final Clock reloj;

    // Sin @Transactional: el error de un tramo sin tarifa dejaría la transacción compartida marcada
    // para rollback y la búsqueda entera fallaría, cuando solo esa salida debe quedar fuera
    @Override
    public List<SalidaDisponible> ejecutar(BuscarSalidasDisponiblesQuery consulta) {
        var ahora = reloj.instant();
        var reglas = politica.para(consulta.canal());
        var candidatas = salidas
                .buscarEntreCiudades(consulta.ciudadOrigen(), consulta.ciudadDestino(), consulta.fechaDeViaje())
                .stream()
                .filter(salida -> salida.tieneVentaAbierta(reglas, ahora))
                .toList();
        var ocupados = reservas.contarOcupados(
                candidatas.stream().map(SalidaCandidata::tramo).toList(), ahora);
        var precios = new HashMap<ConsultaDePrecio, Optional<Monto>>();
        return candidatas.stream()
                .flatMap(salida -> {
                    var deLaSalida = ocupados.getOrDefault(salida.tramo(), AsientosOcupados.NINGUNO);
                    var pisos = pisosDisponibles(salida, deLaSalida, precios);
                    return SalidaDisponible.conLugarPara(consulta.cantidadPasajeros(), salida, pisos).stream();
                })
                .toList();
    }

    private List<PisoDisponible> pisosDisponibles(
            SalidaCandidata salida, AsientosOcupados ocupados, Map<ConsultaDePrecio, Optional<Monto>> precios) {
        var pisos = new ArrayList<PisoDisponible>();
        for (var piso : salida.pisos()) {
            var libres = salida.asientosLibresEn(piso, ocupados);
            if (libres > 0) {
                precios.computeIfAbsent(salida.consultaDePrecio(piso), tarifas::buscarPrecio)
                        .ifPresent(precio -> pisos.add(new PisoDisponible(piso, libres, precio)));
            }
        }
        return pisos;
    }
}
