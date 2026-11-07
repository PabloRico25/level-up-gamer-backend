package cl.levelupgamer.backend.boletas.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DetalleBoletaResponse {

    private String productoId;
    private Integer cantidad;
    private Long precioUnitario;
    private Long subtotal;
}