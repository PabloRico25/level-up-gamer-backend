package cl.levelupgamer.backend.boletas.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter

public class DetalleBoletaResponse {

    @lombok.Setter private Long id;
    @lombok.Setter private Long boletaId;
    public DetalleBoletaResponse(String productoId,Integer cantidad,Long precioUnitario,Long subtotal) {
        this.productoId=productoId;this.cantidad=cantidad;this.precioUnitario=precioUnitario;this.subtotal=subtotal;
    }
    private String productoId;
    private Integer cantidad;
    private Long precioUnitario;
    private Long subtotal;
}