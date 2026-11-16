
package cl.levelupgamer.backend.usuarios;

public class ValidadorRun {

    public static boolean esValido(String run) {

        if (run == null) {
            return false;
        }

        // Limpiar el RUN
        String limpio = run.replace(".", "")
                .replace("-", "")
                .trim()
                .toUpperCase();

        // Validar formato
        if (!limpio.matches("[0-9]{6,8}[0-9K]")) {
            return false;
        }

        String cuerpo = limpio.substring(0, limpio.length() - 1);

        char digitoIngresado = limpio.charAt(limpio.length() - 1);

        int suma = 0;
        int multiplicador = 2;

        // Calcular digito verificador
        for (int i = cuerpo.length() - 1; i >= 0; i--) {

            int numero = Character.getNumericValue(cuerpo.charAt(i));

            suma += numero * multiplicador;

            multiplicador++;

            if (multiplicador > 7) {
                multiplicador = 2;
            }
        }

        int resultado = 11 - (suma % 11);

        char digitoCorrecto;

        if (resultado == 11) {
            digitoCorrecto = '0';
        } else if (resultado == 10) {
            digitoCorrecto = 'K';
        } else {
            digitoCorrecto = Character.forDigit(resultado, 10);
        }

        return digitoIngresado == digitoCorrecto;
    }
}
