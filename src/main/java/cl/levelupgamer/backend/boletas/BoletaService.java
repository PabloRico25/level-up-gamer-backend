package cl.levelupgamer.backend.boletas;

import cl.levelupgamer.backend.boletas.dto.BoletaResponse;
import cl.levelupgamer.backend.boletas.dto.DetalleBoletaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoletaService {

    private final BoletaRepository boletaRepository;
    private final DetalleBoletaRepository detalleBoletaRepository;

    // Consultar todas las boletas
    @Transactional(readOnly = true)
    public List<BoletaResponse> listarTodas() {

        return boletaRepository.findAll()
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    // Consultar una boleta por su ID
    @Transactional(readOnly = true)
    public BoletaResponse buscarPorId(Long id) {

        Boleta boleta = boletaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró la boleta"
                ));

        return convertirAResponse(boleta);
    }

    // Consultar boletas de un usuario
    @Transactional(readOnly = true)
    public List<BoletaResponse> listarPorUsuario(Long usuarioId) {

        return boletaRepository.findByUsuarioId(usuarioId)
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    // Convertir los datos de MySQL a una respuesta para React
    private BoletaResponse convertirAResponse(Boleta boleta) {

        List<DetalleBoletaResponse> items =
                detalleBoletaRepository.findByBoletaId(boleta.getId())
                        .stream()
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