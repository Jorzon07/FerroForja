package co.ferroforja.bodega;

import com.fasterxml.jackson.databind.ObjectMapper;
import co.ferroforja.bodega.socios.entity.Socio;
import co.ferroforja.bodega.socios.repository.SocioRepository;
import co.ferroforja.bodega.expendio.dto.request.GenerarExpendioRequest;
import co.ferroforja.bodega.expendio.dto.request.ExpendioDetalleRequest;
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
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ExpendioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SocioRepository socioRepository;

    @Test
    @DisplayName("GET /api/v1/sales sin autenticación debe retornar 401 Unauthorized")
    void shouldReturn401WhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/expendios"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/v1/sales con usuario autenticado debe retornar 200 OK")
    @WithMockUser(username = "vendedor_mostrador", roles = {"USUARIO_OPERATIVO"})
    void shouldReturn200WhenAuthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/expendios"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/v1/sales con cliente y líneas válidas debe registrar venta 201 Created (RN-05, RN-06)")
    @WithMockUser(username = "admin_forja", roles = {"ADMINISTRADOR"})
    void shouldCreateExpendioSuccessfully() throws Exception {
        Socio comprador = socioRepository.findByNroIdentificacion("1020304050")
                .orElseGet(() -> {
                    Socio t = new Socio();
                    t.setTipoIdentificacion("CC");
                    t.setNroIdentificacion("1020304050");
                    t.setNombreLegal("Juan Perez Constructor");
                    t.setTipoPersona("NATURAL");
                    t.setEsComprador(true);
                    t.setEsSuministrador(false);
                    return socioRepository.save(t);
                });

        GenerarExpendioRequest req = new GenerarExpendioRequest();
        req.setCompradorId(comprador.getId());
        req.setImporteAbonadoEfectivo(new BigDecimal("100000.00"));
        req.setDetalles(List.of(
                new ExpendioDetalleRequest(1L, new BigDecimal("1.000"))
        ));

        mockMvc.perform(post("/api/v1/expendios")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.correlativo").exists())
                .andExpect(jsonPath("$.situacion").value("COMPLETADA"))
                .andExpect(jsonPath("$.detalles").isArray());
    }

    @Test
    @DisplayName("POST /api/v1/sales con pago insuficiente debe retornar 409 Conflict (ProblemDetail)")
    @WithMockUser(username = "admin_forja", roles = {"ADMINISTRADOR"})
    void shouldFailWhenPaymentIsInsufficient() throws Exception {
        Socio comprador = socioRepository.findByNroIdentificacion("1020304050").orElseThrow();

        GenerarExpendioRequest req = new GenerarExpendioRequest();
        req.setCompradorId(comprador.getId());
        req.setImporteAbonadoEfectivo(new BigDecimal("100.00")); // Pago ridículo frente al precio
        req.setDetalles(List.of(
                new ExpendioDetalleRequest(1L, new BigDecimal("1.000"))
        ));

        mockMvc.perform(post("/api/v1/expendios")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflicto de Regla de Negocio"));
    }
}
