package pe.puriy.tarifas.adaptador.entrada.web;

import java.util.UUID;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import pe.puriy.compartido.dominio.Monto;
import pe.puriy.compartido.dominio.RutaId;
import pe.puriy.compartido.dominio.TerminalId;
import pe.puriy.tarifas.aplicacion.puerto.entrada.ActualizarTarifaCommand;
import pe.puriy.tarifas.aplicacion.puerto.entrada.DefinirTarifaCommand;
import pe.puriy.tarifas.aplicacion.puerto.entrada.DetalleTarifa;
import pe.puriy.tarifas.dominio.modelo.CriterioDeTarifa;
import pe.puriy.tarifas.dominio.modelo.ParDeParadas;
import pe.puriy.tarifas.dominio.modelo.TarifaId;
import pe.puriy.tarifas.dominio.modelo.Vigencia;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class TarifaWebMapper {

    static DefinirTarifaCommand aCommand(DefinirTarifaRequest solicitud) {
        var par = new ParDeParadas(
                new RutaId(solicitud.rutaId()),
                new TerminalId(solicitud.origenId()),
                new TerminalId(solicitud.destinoId()));
        var criterio = new CriterioDeTarifa(par, solicitud.tipoServicio(), solicitud.piso());
        var vigencia = new Vigencia(solicitud.tipoVigencia(), solicitud.vigenteDesde(), solicitud.vigenteHasta());
        return new DefinirTarifaCommand(criterio, vigencia, Monto.soles(solicitud.precioCentimos()));
    }

    static ActualizarTarifaCommand aCommand(UUID tarifaId, ActualizarTarifaRequest solicitud) {
        var vigencia = new Vigencia(solicitud.tipoVigencia(), solicitud.vigenteDesde(), solicitud.vigenteHasta());
        return new ActualizarTarifaCommand(
                new TarifaId(tarifaId), solicitud.version(), vigencia, Monto.soles(solicitud.precioCentimos()));
    }

    static TarifaResponse aResponse(DetalleTarifa tarifa) {
        var criterio = tarifa.criterio();
        var vigencia = tarifa.vigencia();
        return new TarifaResponse(
                tarifa.id().valor(),
                criterio.par().rutaId().valor(),
                criterio.par().origen().valor(),
                criterio.par().destino().valor(),
                criterio.tipoServicio().name(),
                criterio.piso(),
                vigencia.tipo().name(),
                vigencia.desde(),
                vigencia.hasta(),
                tarifa.precio().centimos(),
                tarifa.version());
    }
}
