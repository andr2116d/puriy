package pe.puriy.rutas.adaptador.salida.persistencia;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import pe.puriy.compartido.dominio.IdentificadorUnico;
import pe.puriy.compartido.dominio.RutaId;
import pe.puriy.compartido.dominio.SalidaId;
import pe.puriy.compartido.dominio.TipoServicio;
import pe.puriy.rutas.dominio.modelo.BusId;
import pe.puriy.rutas.dominio.modelo.CambioDeBus;
import pe.puriy.rutas.dominio.modelo.Horario;
import pe.puriy.rutas.dominio.modelo.Salida;
import puriy.basedatos.rutas.tables.records.CambiosDeBusRecord;
import puriy.basedatos.rutas.tables.records.SalidasRecord;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class SalidaPersistenceMapper {

    static SalidasRecord aRegistro(Salida salida) {
        var registro = new SalidasRecord();
        registro.setId(salida.id().valor());
        registro.setRutaId(salida.rutaId().valor());
        registro.setBusId(salida.busId().valor());
        registro.setTipoServicio(salida.tipoServicio().name());
        registro.setParteEn(aFecha(salida.horario().parteEn()));
        registro.setLlegaEn(aFecha(salida.horario().llegaEn()));
        registro.setVersion(salida.version());
        return registro;
    }

    static CambiosDeBusRecord aRegistro(CambioDeBus cambio) {
        var registro = new CambiosDeBusRecord();
        registro.setId(IdentificadorUnico.nuevo());
        registro.setSalidaId(cambio.salidaId().valor());
        registro.setBusAnteriorId(cambio.busAnterior().valor());
        registro.setBusNuevoId(cambio.busNuevo().valor());
        registro.setMotivo(cambio.motivo().name());
        registro.setCambiadoEn(aFecha(cambio.cambiadoEn()));
        return registro;
    }

    static Salida aDominio(SalidasRecord registro) {
        return Salida.reconstituir()
                .id(new SalidaId(registro.getId()))
                .rutaId(new RutaId(registro.getRutaId()))
                .busId(new BusId(registro.getBusId()))
                .tipoServicio(TipoServicio.valueOf(registro.getTipoServicio()))
                .horario(new Horario(
                        registro.getParteEn().toInstant(), registro.getLlegaEn().toInstant()))
                .version(registro.getVersion())
                .build();
    }

    static OffsetDateTime aFecha(Instant instante) {
        return instante.atOffset(ZoneOffset.UTC);
    }
}
