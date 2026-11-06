package cl.levelupgamer.backend.boletas;

import cl.levelupgamer.backend.boletas.dto.BoletaResponse;
import cl.levelupgamer.backend.boletas.dto.DetalleBoletaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoletaService {

    private final BoletaRepository boletaRepository;
    private final DetalleBoletaRepository detalleBoletaRepository;

    @Transactional(readOnly = true)
    public List<BoletaResponse> listarPorUsuario(Long usuarioId) {

        List<Boleta> boletas = boletaRepository.findByUsuarioId(usuarioId);

        return boletas.stream()
                .map(this::convertirAResponse)
                .toList();
    }

    private BoletaResponse convertirAResponse(Boleta boleta) {

        List<DetalleBoleta> detalles =
                detalleBoletaRepository.findByBoletaId(boleta.getId());

        List<DetalleBoletaResponse> items = detalles.stream()
                .map(detalle -> new DetalleBoletaResponse(
                        detalle.getProductoId(),
                        detalle.getCantidad(),
                        detalle.getPrecioUnitario(),
                        detalle.getSubtotal()
                ))
                .toList();

        return new BoletaResponse(
                boleta.getId(),
                boleta.getFechaEmision(),
                boleta.getSubtotal(),
                boleta.getDescuento(),
                boleta.getTotal(),
                items
        );
    }
}