package pe.puriy.tarifas.adaptador.salida.persistencia;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import pe.puriy.compartido.dominio.Monto;
import pe.puriy.compartido.dominio.RutaId;
import pe.puriy.compartido.dominio.TerminalId;
import pe.puriy.compartido.dominio.TipoServicio;
import pe.puriy.tarifas.dominio.modelo.CriterioDeTarifa;
import pe.puriy.tarifas.dominio.modelo.ParDeParadas;
import pe.puriy.tarifas.dominio.modelo.Tarifa;
import pe.puriy.tarifas.dominio.modelo.TarifaId;
import pe.puriy.tarifas.dominio.modelo.TipoVigencia;
import pe.puriy.tarifas.dominio.modelo.Vigencia;
import puriy.basedatos.tarifas.tables.records.TarifasRecord;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class TarifaPersistenceMapper {

    static TarifasRecord aRegistro(Tarifa tarifa) {
        var criterio = tarifa.criterio();
        var registro = new TarifasRecord();
        registro.setId(tarifa.id().valor());
        registro.setRutaId(criterio.par().rutaId().valor());
        registro.setTerminalOrigenId(criterio.par().origen().valor());
        registro.setTerminalDestinoId(criterio.par().destino().valor());
        registro.setTipoServicio(criterio.tipoServicio().name());
        registro.setPiso((short) criterio.piso());
        registro.setTipoVigencia(tarifa.vigencia().tipo().name());
        registro.setVigenteDesde(tarifa.vigencia().desde());
        registro.setVigenteHasta(tarifa.vigencia().hasta());
        registro.setPrecioCentimos(tarifa.precio().centimos());
        registro.setVersion(tarifa.version());
        return registro;
    }

    static Tarifa aDominio(TarifasRecord registro) {
        var par = new ParDeParadas(
                new RutaId(registro.getRutaId()),
                new TerminalId(registro.getTerminalOrigenId()),
                new TerminalId(registro.getTerminalDestinoId()));
        var criterio = new CriterioDeTarifa(par, TipoServicio.valueOf(registro.getTipoServicio()), registro.getPiso());
        var vigencia = new Vigencia(
                TipoVigencia.valueOf(registro.getTipoVigencia()),
                registro.getVigenteDesde(),
                registro.getVigenteHasta());
        return Tarifa.reconstituir()
                .id(new TarifaId(registro.getId()))
                .criterio(criterio)
                .vigencia(vigencia)
                .precio(Monto.soles(registro.getPrecioCentimos()))
                .version(registro.getVersion())
                .build();
    }
}
