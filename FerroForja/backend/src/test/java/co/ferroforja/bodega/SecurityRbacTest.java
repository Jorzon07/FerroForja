package co.ferroforja.bodega;

import com.fasterxml.jackson.databind.ObjectMapper;
import co.ferroforja.bodega.almacen.dto.request.ReajusteAlmacenRequest;
import co.ferroforja.bodega.operarios.dto.request.GenerarOperarioRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Set;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de seguridad, autorización basada en roles (RBAC) y protección CSRF.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SecurityRbacTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Petición anónima a /api/v1/inventory/stocks debe ser rechazada con 401 Unauthorized")
    void peticionAnonimaRetorna401() throws Exception {
        mockMvc.perform(get("/api/v1/almacen/existencias"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Usuario Operativo intentando ejecutar Ajuste Manual de Inventario debe recibir 403 Forbidden (RN-08)")
    @WithMockUser(username = "vendedor_mostrador", roles = {"USUARIO_OPERATIVO"})
    void operarioOperativoNoPuedeEjecutarReajustes() throws Exception {
        ReajusteAlmacenRequest req = new ReajusteAlmacenRequest();
        req.setFichaId(1L);
        req.setTipoReajuste("INCREMENTO");
        req.setVolumen(new BigDecimal("5.000"));
        req.setCausa("Intento no autorizado de ajuste de inventario");

        mockMvc.perform(post("/api/v1/almacen/reajustes")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Usuario Operativo intentando consultar o crear usuarios debe recibir 403 Forbidden")
    @WithMockUser(username = "vendedor_mostrador", roles = {"USUARIO_OPERATIVO"})
    void operarioOperativoNoPuedeGestionarOperarios() throws Exception {
        mockMvc.perform(get("/api/v1/operarios"))
                .andExpect(status().isForbidden());

        GenerarOperarioRequest req = new GenerarOperarioRequest();
        req.setUsername("nuevo_hacker");
        req.setPassword("123456");
        req.setNombreVisible("Usuario No Permitido");
        req.setPerfiles(Set.of("ROLE_ADMINISTRADOR"));

        mockMvc.perform(post("/api/v1/operarios")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Usuario Operativo intentando consultar la bitácora de auditoría debe recibir 403 Forbidden (RN-11)")
    @WithMockUser(username = "vendedor_mostrador", roles = {"USUARIO_OPERATIVO"})
    void operarioOperativoNoPuedeIndagarBitacora() throws Exception {
        mockMvc.perform(get("/api/v1/bitacora/registros"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Petición mutativa POST sin token CSRF debe ser rechazada con 403 Forbidden")
    @WithMockUser(username = "admin_forja", roles = {"ADMINISTRADOR"})
    void peticionPostSinCsrfRetorna403() throws Exception {
        ReajusteAlmacenRequest req = new ReajusteAlmacenRequest();
        req.setFichaId(1L);
        req.setTipoReajuste("INCREMENTO");
        req.setVolumen(new BigDecimal("1.000"));
        req.setCausa("Ajuste sin token CSRF");

        mockMvc.perform(post("/api/v1/almacen/reajustes")
                // Sin .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Usuario Administrador autenticado con CSRF puede consultar usuarios exitosamente (200 OK)")
    @WithMockUser(username = "admin_forja", roles = {"ADMINISTRADOR"})
    void administradorPuedeIndagarOperarios() throws Exception {
        mockMvc.perform(get("/api/v1/operarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }
}
