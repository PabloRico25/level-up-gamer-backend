package cl.levelupgamer.backend.usuarios;
import cl.levelupgamer.backend.comun.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.*;
@Service @RequiredArgsConstructor
@Transactional
public class UsuarioAdministracionService {
 private final UsuarioRepository usuarios;
 private final PasswordEncoder encoder;
 @Transactional(readOnly=true) public List<UsuarioResponse> listar() { return usuarios.findAll().stream().filter(Usuario::isActivo).map(UsuarioResponse::desde).toList(); }
 @Transactional(readOnly=true) public UsuarioResponse buscar(Long id) { return UsuarioResponse.desde(obtener(id)); }
 public UsuarioResponse crear(UsuarioAdminRequest d) {
  if(d.contrasena()==null || d.contrasena().isBlank()) throw new SolicitudInvalidaException("La contraseña es obligatoria");
  Usuario u=new Usuario(); aplicar(u,d); return UsuarioResponse.desde(usuarios.save(u));
 }
 public UsuarioResponse editar(Long id,UsuarioAdminRequest d,String actor) {
  Usuario u=obtener(id); protegerRolPropio(u,d.tipo(),actor); aplicar(u,d); return UsuarioResponse.desde(u);
 }
 public UsuarioResponse cambiarRol(Long id,RolUsuario tipo,String actor) {
  Usuario u=obtener(id); protegerRolPropio(u,tipo,actor); u.setTipo(tipo); return UsuarioResponse.desde(u);
 }
 public void desactivar(Long id,String actor) {
  Usuario u=obtener(id);
  if(u.getCorreo().equalsIgnoreCase(actor)) throw new SolicitudInvalidaException("No puedes desactivar tu propia cuenta");
  u.setActivo(false);
 }
 private void protegerRolPropio(Usuario u,RolUsuario tipo,String actor) {
  if(u.getCorreo().equalsIgnoreCase(actor) && tipo!=RolUsuario.ADMINISTRADOR) throw new SolicitudInvalidaException("No puedes quitarte tu propio rol de administrador");
 }
 private Usuario obtener(Long id) { return usuarios.findById(id).filter(Usuario::isActivo).orElseThrow(()->new RecursoNoEncontradoException("Usuario no encontrado")); }
 private void aplicar(Usuario u,UsuarioAdminRequest d) {
  if(d.contrasena()!=null && (d.contrasena().length()<8 || d.contrasena().getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72)) throw new SolicitudInvalidaException("Contraseña de al menos 8 caracteres y máximo 72 bytes UTF-8");
  String run=d.run().replace(".","").replace("-","").trim().toUpperCase(Locale.ROOT);
  String correo=d.correo().trim().toLowerCase(Locale.ROOT);
  if(!ValidadorRun.esValido(run)) throw new SolicitudInvalidaException("RUN inválido");
  if(d.fechaNacimiento().plusYears(18).isAfter(LocalDate.now())) throw new SolicitudInvalidaException("Debes tener al menos 18 años");
  if(usuarios.existsByRunAndIdNot(run,u.getId()==null?-1L:u.getId())) throw new ConflictoException("RUN ya registrado");
  if(usuarios.existsByCorreoAndIdNot(correo,u.getId()==null?-1L:u.getId())) throw new ConflictoException("Correo ya registrado");
  u.setRun(run);u.setCorreo(correo);u.setNombre(d.nombre().trim());u.setApellidos(d.apellidos().trim());
  u.setFechaNacimiento(d.fechaNacimiento());u.setRegion(d.region());u.setComuna(d.comuna());u.setDireccion(d.direccion().trim());u.setTipo(d.tipo());
  if(d.contrasena()!=null) u.setContrasenaHash(encoder.encode(d.contrasena()));
 }
 public record UsuarioResponse(Long id,String run,String nombre,String apellidos,String correo,LocalDate fechaNacimiento,String region,String comuna,String direccion,RolUsuario tipo,boolean activo) {
  public static UsuarioResponse desde(Usuario u) { return new UsuarioResponse(u.getId(),u.getRun(),u.getNombre(),u.getApellidos(),u.getCorreo(),u.getFechaNacimiento(),u.getRegion(),u.getComuna(),u.getDireccion(),u.getTipo(),u.isActivo()); }
 }
}
