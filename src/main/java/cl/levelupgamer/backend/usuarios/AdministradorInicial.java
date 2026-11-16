package cl.levelupgamer.backend.usuarios;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
@Component @RequiredArgsConstructor
@ConditionalOnProperty(name="app.bootstrap.enabled",havingValue="true")
public class AdministradorInicial implements ApplicationRunner {
 private final UsuarioAdministracionService servicio;
 private final UsuarioRepository repositorio;
 @Value("${ADMIN_CORREO}") private String correo;
 @Value("${ADMIN_PASSWORD}") private String clave;
 @Value("${ADMIN_RUN}") private String run;
 public void run(ApplicationArguments args) {
  if(repositorio.existsByCorreo(correo.trim().toLowerCase(java.util.Locale.ROOT))) return;
  servicio.crear(new UsuarioAdminRequest(run,"Administrador","Inicial",correo,clave,LocalDate.of(1990,1,1),"Metropolitana","Santiago","Dirección de prueba",RolUsuario.ADMINISTRADOR));
 }
}
