package pe.puriy.reservas.dominio.modelo;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import pe.puriy.compartido.dominio.RutaId;
import pe.puriy.compartido.dominio.SalidaId;
import pe.puriy.compartido.dominio.TipoServicio;
import pe.puriy.compartido.dominio.ZonaHoraria;

public record SalidaCandidata(
        SalidaId id,
        RutaId rutaId,
        TipoServicio tipoServicio,
        ParadaDelTramo origen,
        ParadaDelTramo destino,
        Map<Integer, Integer> asientosPorPiso) {

    public SalidaCandidata {
        asientosPorPiso = Map.copyOf(asientosPorPiso);
    }

    public TramoDeSalida tramo() {
        return new TramoDeSalida(id, new Tramo(origen.orden(), destino.orden()));
    }

    public boolean tieneVentaAbierta(ReglasDeCanal reglas, Instant ahora) {
        return ahora.isBefore(reglas.cierreDeVenta(origen.horaDePaso()));
    }

    public List<Integer> pisos() {
        return asientosPorPiso.keySet().stream().sorted().toList();
    }

    // Tras un cambio de bus puede haber más ocupados que asientos en un piso
    public int asientosLibresEn(int piso, AsientosOcupados ocupados) {
        return Math.max(0, asientosPorPiso.getOrDefault(piso, 0) - ocupados.enPiso(piso));
    }

    public ConsultaDePrecio consultaDePrecio(int piso) {
        var fechaDeViaje = origen.horaDePaso().atZone(ZonaHoraria.PERU).toLocalDate();
        return new ConsultaDePrecio(
                rutaId, origen.terminalId(), destino.terminalId(), tipoServicio, piso, fechaDeViaje);
    }
}
