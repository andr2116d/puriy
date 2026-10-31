package pe.puriy.tarifas.dominio.modelo;

import java.time.LocalDate;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import pe.puriy.compartido.dominio.Monto;
import pe.puriy.compartido.dominio.RutaId;
import pe.puriy.compartido.dominio.TerminalId;
import pe.puriy.compartido.dominio.TipoServicio;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TarifasDeEjemplo {

    public static final RutaId LIMA_TUMBES = RutaId.nuevo();
    public static final TerminalId LIMA = TerminalId.nuevo();
    public static final TerminalId CHIMBOTE = TerminalId.nuevo();
    public static final TerminalId TRUJILLO = TerminalId.nuevo();
    public static final ParadasDeRuta PARADAS = new ParadasDeRuta(LIMA_TUMBES, List.of(LIMA, CHIMBOTE, TRUJILLO));

    public static final ParDeParadas LIMA_TRUJILLO = new ParDeParadas(LIMA_TUMBES, LIMA, TRUJILLO);
    public static final CriterioDeTarifa CAMA_PRIMER_PISO = new CriterioDeTarifa(LIMA_TRUJILLO, TipoServicio.CAMA, 1);

    public static final Vigencia TODO_EL_ANIO = Vigencia.regularDesde(LocalDate.of(2026, 1, 1));
    public static final Vigencia FIN_DE_ANIO =
            new Vigencia(TipoVigencia.TEMPORADA, LocalDate.of(2026, 12, 15), LocalDate.of(2027, 1, 5));
    public static final Vigencia NAVIDAD =
            new Vigencia(TipoVigencia.FERIADO, LocalDate.of(2026, 12, 24), LocalDate.of(2026, 12, 25));

    public static Tarifa regular(long centimos) {
        return Tarifa.definir(CAMA_PRIMER_PISO, TODO_EL_ANIO, Monto.soles(centimos));
    }

    public static Tarifa conVigencia(Vigencia vigencia, long centimos) {
        return Tarifa.definir(CAMA_PRIMER_PISO, vigencia, Monto.soles(centimos));
    }
}
