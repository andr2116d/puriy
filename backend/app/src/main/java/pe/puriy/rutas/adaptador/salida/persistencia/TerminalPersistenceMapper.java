package pe.puriy.rutas.adaptador.salida.persistencia;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import pe.puriy.compartido.dominio.TerminalId;
import pe.puriy.rutas.dominio.modelo.DatosTerminal;
import pe.puriy.rutas.dominio.modelo.Terminal;
import puriy.basedatos.rutas.tables.records.TerminalesRecord;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class TerminalPersistenceMapper {

    static TerminalesRecord aRegistro(Terminal terminal) {
        var datos = terminal.datos();
        var registro = new TerminalesRecord();
        registro.setId(terminal.id().valor());
        registro.setNombre(datos.nombre());
        registro.setCiudad(datos.ciudad());
        registro.setDireccion(datos.direccion());
        registro.setVende(datos.vende());
        registro.setEmbarca(datos.embarca());
        registro.setVersion(terminal.version());
        return registro;
    }

    static Terminal aDominio(TerminalesRecord registro) {
        var datos = new DatosTerminal(
                registro.getNombre(),
                registro.getCiudad(),
                registro.getDireccion(),
                registro.getVende(),
                registro.getEmbarca());
        return Terminal.reconstituir()
                .id(new TerminalId(registro.getId()))
                .datos(datos)
                .version(registro.getVersion())
                .build();
    }
}
