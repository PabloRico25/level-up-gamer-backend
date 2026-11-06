package cl.levelupgamer.backend.boletas.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class BoletaResponse {

    private Long id;
    private LocalDateTime fechaEmision;
    private Integer subtotal;
    private Integer descuento;
    private Integer total;

    private List<DetalleBoletaResponse> items;

}