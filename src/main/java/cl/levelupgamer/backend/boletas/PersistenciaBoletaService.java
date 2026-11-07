
package cl.levelupgamer.backend.boletas;

import cl.levelupgamer.backend.boletas.dto.BoletaResponse;
import cl.levelupgamer.backend.boletas.dto.DetalleBoletaResponse;
import cl.levelupgamer.backend.usuarios.Usuario;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PersistenciaBoletaService {

    private final BoletaRepository boletaRepository;
    private final DetalleBoletaRepository detalleBoletaRepository;

    // Debe ejecutarse dentro de una transacción de compra.
    @Transactional(propagation = Propagation.MANDATORY)
    public BoletaResponse guardar(
            Usuario usuario,
            CotizadorCompra.ResultadoCompra resultado) {

        // Crear la boleta con sus montos calculados
        Boleta boleta = new Boleta();

        boleta.setUsuario(usuario);
        boleta.setSubtotal(resultado.subtotal());
        boleta.setDescuento(resultado.descuento());
        boleta.setTotal(resultado.total());

        // Guardar la boleta
        Boleta guardada = boletaRepository.save(boleta);

        // Preparar los productos comprados
        List<DetalleBoleta> detalles = new ArrayList<>();

        for (DetalleBoletaResponse item : resultado.items()) {

            DetalleBoleta detalle = new DetalleBoleta();

            detalle.setBoleta(guardada);
            detalle.setProductoId(item.getProductoId());
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(item.getPrecioUnitario());
            detalle.setSubtotal(item.getSubtotal());

            detalles.add(detalle);
        }

        // Guardar todos los detalles
        detalleBoletaRepository.saveAll(detalles);

        // Devolver la información registrada
        return new BoletaResponse(
                guardada.getId(),
                guardada.getFechaEmision(),
                guardada.getSubtotal(),
                guardada.getDescuento(),
                guardada.getTotal(),
                resultado.items()
        );
    }
}
