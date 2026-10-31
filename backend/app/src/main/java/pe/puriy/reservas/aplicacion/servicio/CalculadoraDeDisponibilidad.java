package pe.puriy.reservas.aplicacion.servicio;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.puriy.compartido.dominio.Monto;
import pe.puriy.reservas.aplicacion.puerto.entrada.SalidaDisponible;
import pe.puriy.reservas.aplicacion.puerto.salida.ReservaLecturaPort;
import pe.puriy.reservas.aplicacion.puerto.salida.TarifaPort;
import pe.puriy.reservas.dominio.modelo.AsientosOcupados;
import pe.puriy.reservas.dominio.modelo.ConsultaDePrecio;
import pe.puriy.reservas.dominio.modelo.PisoDisponible;
import pe.puriy.reservas.dominio.modelo.ReglasDeCanal;
import pe.puriy.reservas.dominio.modelo.SalidaCandidata;

@Service
@RequiredArgsConstructor
class CalculadoraDeDisponibilidad {

    private final ReservaLecturaPort reservas;
    private final TarifaPort tarifas;

    // Sin transacción: el error de un tramo sin tarifa dejaría la transacción compartida marcada
    // para rollback y la búsqueda entera fallaría, cuando solo esa salida debe quedar fuera
    List<SalidaDisponible> ofrecer(
            List<SalidaCandidata> candidatas, int cantidadPasajeros, ReglasDeCanal reglas, Instant ahora) {
        var abiertas = candidatas.stream()
                .filter(salida -> salida.tieneVentaAbierta(reglas, ahora))
                .toList();
        var ocupados = reservas.contarOcupados(
                abiertas.stream().map(SalidaCandidata::tramo).toList(), ahora);
        var precios = new HashMap<ConsultaDePrecio, Optional<Monto>>();
        return abiertas.stream()
                .flatMap(salida -> {
                    var deLaSalida = ocupados.getOrDefault(salida.tramo(), AsientosOcupados.NINGUNO);
                    var pisos = pisosDisponibles(salida, deLaSalida, precios);
                    return SalidaDisponible.conLugarPara(cantidadPasajeros, salida, pisos).stream();
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
