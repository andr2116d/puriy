package pe.puriy.reservas.adaptador.entrada.web;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import pe.puriy.compartido.dominio.ZonaHoraria;
import pe.puriy.reservas.adaptador.entrada.web.SalidaDisponibleResponse.ParadaResponse;
import pe.puriy.reservas.adaptador.entrada.web.SalidaDisponibleResponse.PisoResponse;
import pe.puriy.reservas.aplicacion.puerto.entrada.SalidaDisponible;
import pe.puriy.reservas.dominio.modelo.ParadaDelTramo;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class SalidaDisponibleWebMapper {

    static SalidaDisponibleResponse aResponse(SalidaDisponible salida) {
        var pisos = salida.pisos().stream()
                .map(piso -> new PisoResponse(
                        piso.piso(), piso.asientosLibres(), piso.precio().centimos()))
                .toList();
        return new SalidaDisponibleResponse(
                salida.id().valor(),
                salida.tipoServicio().name(),
                aParada(salida.origen()),
                aParada(salida.destino()),
                salida.asientosLibres(),
                salida.precioDesde().centimos(),
                pisos);
    }

    private static ParadaResponse aParada(ParadaDelTramo parada) {
        return new ParadaResponse(
                parada.terminalId().valor(),
                parada.terminal(),
                parada.ciudad(),
                parada.horaDePaso().atZone(ZonaHoraria.PERU).toOffsetDateTime());
    }
}
