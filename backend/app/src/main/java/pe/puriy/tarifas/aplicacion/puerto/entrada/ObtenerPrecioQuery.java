package pe.puriy.tarifas.aplicacion.puerto.entrada;

import java.time.LocalDate;
import pe.puriy.compartido.dominio.RutaId;
import pe.puriy.compartido.dominio.TerminalId;
import pe.puriy.compartido.dominio.TipoServicio;
import pe.puriy.tarifas.dominio.modelo.CriterioDeTarifa;
import pe.puriy.tarifas.dominio.modelo.ParDeParadas;

public record ObtenerPrecioQuery(CriterioDeTarifa criterio, LocalDate fechaDeViaje) {

    public static ObtenerPrecioQuery para(
            RutaId rutaId,
            TerminalId origen,
            TerminalId destino,
            TipoServicio tipoServicio,
            int piso,
            LocalDate fechaDeViaje) {
        var par = new ParDeParadas(rutaId, origen, destino);
        return new ObtenerPrecioQuery(new CriterioDeTarifa(par, tipoServicio, piso), fechaDeViaje);
    }
}
