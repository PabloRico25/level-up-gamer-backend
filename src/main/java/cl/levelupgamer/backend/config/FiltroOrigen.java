package cl.levelupgamer.backend.config;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.access.AccessDeniedException;
/** Rechaza orígenes no autorizados con el mismo JSON que los demás 403. */
public class FiltroOrigen extends OncePerRequestFilter {
 private final UrlBasedCorsConfigurationSource origenes;
 private final ErroresSeguridad errores;
 public FiltroOrigen(UrlBasedCorsConfigurationSource origenes,ErroresSeguridad errores) {this.origenes=origenes;this.errores=errores;}
 @Override protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain) throws ServletException,IOException {
  String origin=req.getHeader("Origin");var config=origenes.getCorsConfiguration(req);
  if(origin!=null && config!=null && config.checkOrigin(origin)==null) {errores.handle(req,res,new AccessDeniedException("Origen no autorizado"));return;}
  chain.doFilter(req,res);
 }
}
