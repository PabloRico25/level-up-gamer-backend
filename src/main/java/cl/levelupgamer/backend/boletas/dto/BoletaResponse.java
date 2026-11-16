package cl.levelupgamer.backend.boletas.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter

public class BoletaResponse {

    private Long id;
    @lombok.Setter private Long usuarioId;
    public BoletaResponse(Long id, LocalDateTime fechaEmision, Long subtotal, Long descuento, Long total, List<DetalleBoletaResponse> items) {
        this.id=id; this.fechaEmision=fechaEmision; this.subtotal=subtotal; this.descuento=descuento; this.total=total; this.items=items;
    }
    private LocalDateTime fechaEmision;
    private Long subtotal;
    private Long descuento;
    private Long total;

    private List<DetalleBoletaResponse> items;
    public LocalDateTime getFecha() { return fechaEmision; }

}