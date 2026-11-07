package cl.levelupgamer.backend.boletas;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

@Component
public class DescuentoDuoc {

    public long calcular(long subtotal, String correo) {

        if (subtotal < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El subtotal no puede ser negativo"
            );
        }

        if (correo == null) {
            return 0L;
        }

        String correoNormalizado =
                correo.trim().toLowerCase(Locale.ROOT);

        boolean esDuoc =
                correoNormalizado.endsWith("@duoc.cl")
                        || correoNormalizado.endsWith("@profesor.duoc.cl");

        if (!esDuoc) {
            return 0L;
        }

        return BigDecimal.valueOf(subtotal)
                .multiply(new BigDecimal("0.20"))
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();
    }
}