package pe.puriy.reservas.dominio.modelo;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import pe.puriy.compartido.dominio.RutaId;
import pe.puriy.compartido.dominio.SalidaId;
import pe.puriy.compartido.dominio.TerminalId;
import pe.puriy.compartido.dominio.TipoServicio;
import pe.puriy.compartido.dominio.ZonaHoraria;
import pe.puriy.reservas.dominio.excepcion.AsientoInexistenteException;
import pe.puriy.reservas.dominio.excepcion.TramoInvalidoException;
import pe.puriy.reservas.dominio.excepcion.VentaCerradaException;

public record Salida(
        SalidaId id,
        RutaId rutaId,
        TipoServicio tipoServicio,
        List<Parada> paradas,
        Map<Integer, Integer> pisoPorAsiento) {

    public Salida {
        paradas = List.copyOf(paradas);
        pisoPorAsiento = Map.copyOf(pisoPorAsiento);
    }

    public Tramo tramoEntre(TerminalId origen, TerminalId destino) {
        var ordenOrigen = ordenDe(origen);
        var ordenDestino = ordenDe(destino);
        if (ordenOrigen < 0 || ordenDestino <= ordenOrigen) {
            throw new TramoInvalidoException(origen, destino);
        }
        return new Tramo(ordenOrigen, ordenDestino);
    }

    public ConsultaDePrecio consultaDePrecio(Tramo tramo, int piso) {
        return new ConsultaDePrecio(
                rutaId,
                paradas.get(tramo.ordenOrigen()).terminalId(),
                paradas.get(tramo.ordenDestino()).terminalId(),
                tipoServicio,
                piso,
                fechaDeViaje(tramo));
    }

    public LocalDate fechaDeViaje(Tramo tramo) {
        return horaDePasoPorOrigen(tramo).atZone(ZonaHoraria.PERU).toLocalDate();
    }

    public int pisoDe(int numeroAsiento) {
        var piso = pisoPorAsiento.get(numeroAsiento);
        if (piso == null) {
            throw new AsientoInexistenteException(numeroAsiento);
        }
        return piso;
    }

    public void exigirVentaAbierta(Tramo tramo, ReglasDeCanal reglas, Instant ahora) {
        var cierre = reglas.cierreDeVenta(horaDePasoPorOrigen(tramo));
        if (!ahora.isBefore(cierre)) {
            throw new VentaCerradaException(cierre);
        }
    }

    public Parada paradaDeOrigen(Tramo tramo) {
        return paradas.get(tramo.ordenOrigen());
    }

    public Parada paradaDeDestino(Tramo tramo) {
        return paradas.get(tramo.ordenDestino());
    }

    private Instant horaDePasoPorOrigen(Tramo tramo) {
        return paradaDeOrigen(tramo).horaDePaso();
    }

    private int ordenDe(TerminalId terminal) {
        return paradas.stream()
                .filter(parada -> parada.terminalId().equals(terminal))
                .mapToInt(Parada::orden)
                .findFirst()
                .orElse(-1);
    }
}
