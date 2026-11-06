package cl.levelupgamer.backend.boletas.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CrearBoletaRequest {

    @NotEmpty
    private List<@Valid ItemCompraRequest> items;
}