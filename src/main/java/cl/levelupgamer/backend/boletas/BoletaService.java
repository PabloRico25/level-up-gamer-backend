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

    @Transactional(readOnly = true)
    public List<BoletaResponse> listarTodas() {

        return boletaRepository.findAll()
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public BoletaResponse buscarPorId(Long id) {

        Boleta boleta = boletaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró la boleta"
                ));

        return convertirAResponse(boleta);
    }

    @Transactional(readOnly = true)
    public List<BoletaResponse> listarPorUsuario(Long usuarioId) {

        return boletaRepository.findByUsuarioId(usuarioId)
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public BoletaResponse buscarPropia(Long id, Long usuarioId) {
        Boleta boleta = boletaRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"No se encontró la boleta"));
        if (!boleta.getUsuario().getId().equals(usuarioId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"No puedes consultar una boleta ajena");
        return convertirAResponse(boleta);
    }

    private BoletaResponse convertirAResponse(Boleta boleta) {

        List<DetalleBoletaResponse> items =
                detalleBoletaRepository.findByBoletaId(boleta.getId())
                        .stream()
                        .map(detalle -> {
                            var item = new DetalleBoletaResponse(detalle.getProductoId(),detalle.getCantidad(),detalle.getPrecioUnitario(),detalle.getSubtotal());
                            item.setId(detalle.getId()); item.setBoletaId(boleta.getId()); return item;
                        })
                        .toList();

        var respuesta = new BoletaResponse(
                boleta.getId(),
                boleta.getFechaEmision(),
                boleta.getSubtotal(),
                boleta.getDescuento(),
                boleta.getTotal(),
                items
        );
        respuesta.setUsuarioId(boleta.getUsuario().getId());
        return respuesta;
    }
}