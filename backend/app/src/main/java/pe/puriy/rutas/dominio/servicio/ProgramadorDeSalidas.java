package pe.puriy.rutas.dominio.servicio;

import java.time.Instant;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import pe.puriy.rutas.dominio.excepcion.RecurrenciaInvalidaException;
import pe.puriy.rutas.dominio.modelo.BusId;
import pe.puriy.rutas.dominio.modelo.Recurrencia;
import pe.puriy.rutas.dominio.modelo.Ruta;
import pe.puriy.rutas.dominio.modelo.Salida;
import pe.puriy.rutas.dominio.modelo.TipoServicio;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProgramadorDeSalidas {

    public static List<Salida> programarRecurrentes(
            Ruta ruta, BusId busId, TipoServicio tipoServicio, Recurrencia recurrencia, Instant ahora) {
        var salidas = recurrencia.partidas().stream()
                .map(parteEn -> Salida.programar(ruta, busId, tipoServicio, parteEn, ahora))
                .toList();
        exigirQueElBusLlegueAntesDeLaSiguiente(salidas);
        return salidas;
    }

    private static void exigirQueElBusLlegueAntesDeLaSiguiente(List<Salida> salidas) {
        for (var i = 1; i < salidas.size(); i++) {
            var anterior = salidas.get(i - 1).horario();
            var siguiente = salidas.get(i).horario();
            if (anterior.seSuperponeCon(siguiente)) {
                throw RecurrenciaInvalidaException.salidasSuperpuestas(siguiente.parteEn());
            }
        }
    }
}
