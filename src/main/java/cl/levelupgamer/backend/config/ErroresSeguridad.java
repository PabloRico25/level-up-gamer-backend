package cl.levelupgamer.backend.config;
import cl.levelupgamer.backend.comun.ErrorRespuesta;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.access.AccessDeniedException;
import tools.jackson.databind.ObjectMapper;
@Component
public class ErroresSeguridad implements AuthenticationEntryPoint, AccessDeniedHandler {
 private final ObjectMapper mapper;
 public ErroresSeguridad(ObjectMapper mapper) { this.mapper=mapper; }
 public void commence(HttpServletRequest req,HttpServletResponse res,AuthenticationException ex) throws IOException { responder(req,res,401,"Unauthorized","Debes iniciar sesión con un token válido"); }
 public void handle(HttpServletRequest req,HttpServletResponse res,AccessDeniedException ex) throws IOException { responder(req,res,403,"Forbidden","No tienes permisos para esta operación"); }
 private void responder(HttpServletRequest req,HttpServletResponse res,int estado,String error,String mensaje) throws IOException {
  res.setStatus(estado); res.setContentType("application/json"); res.setCharacterEncoding("UTF-8");
  mapper.writeValue(res.getWriter(),new ErrorRespuesta(LocalDateTime.now(),estado,error,mensaje,req.getRequestURI(),null));
 }
}
