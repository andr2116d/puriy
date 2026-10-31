package pe.puriy.reservas.dominio.modelo;

import java.time.LocalDate;
import pe.puriy.compartido.dominio.RutaId;
import pe.puriy.compartido.dominio.TerminalId;
import pe.puriy.compartido.dominio.TipoServicio;

public record ConsultaDePrecio(
        RutaId rutaId,
        TerminalId origen,
        TerminalId destino,
        TipoServicio tipoServicio,
        int piso,
        LocalDate fechaDeViaje) {}
