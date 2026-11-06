package cl.levelupgamer.backend.boletas.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ItemCompraRequest {

    @NotBlank
    private String productoId;

    @NotNull
    @Min(1)
    private Integer cantidad;
}