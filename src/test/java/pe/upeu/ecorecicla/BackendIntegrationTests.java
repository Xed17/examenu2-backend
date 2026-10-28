package pe.upeu.ecorecicla;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import pe.upeu.ecorecicla.web.dto.*;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BackendIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeAll
    void resetearEstadoInicial() {
        jdbcTemplate.execute("UPDATE ruta SET estado = 'ABIERTA', cerrada_en = NULL, cerrada_por = NULL WHERE id = 1");
        jdbcTemplate.execute("UPDATE ruta_punto SET motivo_omision = NULL, omitido_por = NULL, omitido_en = NULL WHERE ruta_id = 1");
        jdbcTemplate.execute("DELETE FROM pesaje WHERE id > 15");
        jdbcTemplate.execute("DELETE FROM operacion_cliente");
    }

    private String loginYObtenerToken(String usuario, String contrasena) throws Exception {
        LoginRequest req = new LoginRequest(usuario, contrasena);
        MvcResult result = mockMvc.perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )
            .andExpect(status().isOk())
            .andReturn();

        LoginResponse resp = objectMapper.readValue(result.getResponse().getContentAsString(), LoginResponse.class);
        return resp.token();
    }

    @Test
    @Order(1)
    void test01_loginExitosoDevuelveTokenYClaimsCorrectos() throws Exception {
        String token = loginYObtenerToken("operario1", "Eco2026*");
        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    @Order(2)
    void test02_loginConContrasenaIncorrectaDevuelve401() throws Exception {
        LoginRequest req = new LoginRequest("operario1", "PasswordErronea");
        mockMvc.perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.codigo").value("CREDENCIALES_INVALIDAS"));
    }

    @Test
    @Order(3)
    void test03_peticionSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios/me"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @Order(4)
    void test04_peticionConTokenAlteradoDevuelve401() throws Exception {
        mockMvc.perform(
            get("/api/v1/usuarios/me")
                .header("Authorization", "Bearer token_completamente_invalido_y_alterado")
        )
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @Order(5)
    void test05_obtenerUsuarioActualAutenticado() throws Exception {
        String token = loginYObtenerToken("supervisor1", "Eco2026*");
        mockMvc.perform(
            get("/api/v1/usuarios/me")
                .header("Authorization", "Bearer " + token)
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("supervisor1"))
            .andExpect(jsonPath("$.roles[0]").value("SUPERVISOR"));
    }

    @Test
    @Order(6)
    void test06_operarioPuedeLeerRutaDeHoy() throws Exception {
        String token = loginYObtenerToken("operario1", "Eco2026*");
        mockMvc.perform(
            get("/api/v1/rutas/mia")
                .header("Authorization", "Bearer " + token)
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.estado").value("ABIERTA"))
            .andExpect(jsonPath("$.puntos").isArray());
    }

    @Test
    @Order(7)
    void test07_auditorPuedeLeerRutaPeroNoCrearNiCerrar_SC_B() throws Exception {
        String tokenAuditor = loginYObtenerToken("auditor1", "Eco2026*");

        // Lectura permitida
        mockMvc.perform(
            get("/api/v1/rutas/mia")
                .header("Authorization", "Bearer " + tokenAuditor)
        )
            .andExpect(status().isOk());

        // Escritura de pesaje rechazada con 403
        CrearPesajeRequest req = new CrearPesajeRequest(
            "CARTON",
            new BigDecimal("20.0"),
            new BigDecimal("2.0"),
            null,
            "Intento de registrar como auditor",
            null
        );

        mockMvc.perform(
            post("/api/v1/rutas/1/puntos/1/pesajes")
                .header("Authorization", "Bearer " + tokenAuditor)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @Order(8)
    void test08_RN03_rechazoCuandoBrutoEsMenorOIgualATara() throws Exception {
        String token = loginYObtenerToken("operario1", "Eco2026*");

        CrearPesajeRequest req = new CrearPesajeRequest(
            "CARTON",
            new BigDecimal("5.0"),
            new BigDecimal("10.0"), // Tara mayor a bruto
            null,
            "Test tara mayor a bruto",
            null
        );

        mockMvc.perform(
            post("/api/v1/rutas/1/puntos/1/pesajes")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.validationErrors.pesoBruto").exists());
    }

    @Test
    @Order(9)
    void test09_RN03_creacionDePesajeCalculaPesoNetoCorrectamente() throws Exception {
        String token = loginYObtenerToken("operario1", "Eco2026*");

        CrearPesajeRequest req = new CrearPesajeRequest(
            "VIDRIO",
            new BigDecimal("35.50"),
            new BigDecimal("5.50"),
            new BigDecimal("10.0"),
            "Calculo neto verificado",
            null
        );

        mockMvc.perform(
            post("/api/v1/rutas/1/puntos/1/pesajes")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.pesoNeto").value(30.00))
            .andExpect(jsonPath("$.material").value("VIDRIO"));
    }

    @Test
    @Order(10)
    void test10_idempotenciaEnCreacionDePesajeConOperacionId() throws Exception {
        String token = loginYObtenerToken("operario1", "Eco2026*");
        String uuid = "test-uuid-idempotente-java-1234";

        CrearPesajeRequest req = new CrearPesajeRequest(
            "PLASTICO",
            new BigDecimal("40.0"),
            new BigDecimal("4.0"),
            null,
            "Pesaje idempotente",
            uuid
        );

        MvcResult firstCall = mockMvc.perform(
            post("/api/v1/rutas/1/puntos/2/pesajes")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )
            .andExpect(status().isCreated())
            .andReturn();

        PesajeResponse firstPesaje = objectMapper.readValue(firstCall.getResponse().getContentAsString(), PesajeResponse.class);

        MvcResult secondCall = mockMvc.perform(
            post("/api/v1/rutas/1/puntos/2/pesajes")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )
            .andExpect(status().isCreated())
            .andReturn();

        PesajeResponse secondPesaje = objectMapper.readValue(secondCall.getResponse().getContentAsString(), PesajeResponse.class);

        assertEquals(firstPesaje.id(), secondPesaje.id());
    }

    @Test
    @Order(11)
    void test11_SC_C_persistenciaYConsultaDeHumedadEstimada() throws Exception {
        String token = loginYObtenerToken("operario1", "Eco2026*");

        CrearPesajeRequest req = new CrearPesajeRequest(
            "CARTON",
            new BigDecimal("80.0"),
            new BigDecimal("10.0"),
            new BigDecimal("18.5"),
            "Carton con humedad estimada 18.5%",
            null
        );

        MvcResult res = mockMvc.perform(
            post("/api/v1/rutas/1/puntos/3/pesajes")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.humedadEstimada").value(18.5))
            .andReturn();

        PesajeResponse pesajeCreado = objectMapper.readValue(res.getResponse().getContentAsString(), PesajeResponse.class);
        assertNotNull(pesajeCreado.humedadEstimada());
        assertEquals(0, new BigDecimal("18.5").compareTo(pesajeCreado.humedadEstimada()));
    }

    @Test
    @Order(12)
    void test12_RN04_cierreDeRutaExitosoTrasAtenderUOmitirTodosLosPuntos() throws Exception {
        String tokenOperario = loginYObtenerToken("operario1", "Eco2026*");
        String tokenSupervisor = loginYObtenerToken("supervisor1", "Eco2026*");

        // Omitir puntos restantes para cumplir RN-04
        OmisionRequest omisionReq = new OmisionRequest("Local cerrado por remodelacion");
        mockMvc.perform(
            put("/api/v1/rutas/1/puntos/5/omision")
                .header("Authorization", "Bearer " + tokenOperario)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(omisionReq))
        )
            .andExpect(status().isOk());

        mockMvc.perform(
            put("/api/v1/rutas/1/puntos/6/omision")
                .header("Authorization", "Bearer " + tokenOperario)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(omisionReq))
        )
            .andExpect(status().isOk());

        // Cierre con supervisor
        mockMvc.perform(
            post("/api/v1/rutas/1/cierre")
                .header("Authorization", "Bearer " + tokenSupervisor)
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.estado").value("CERRADA"))
            .andExpect(jsonPath("$.cerradaPor").value("Supervisor Uno"));
    }

    @Test
    @Order(13)
    void test13_RN01_rechazoDeCreacionEnRutaCerrada() throws Exception {
        String token = loginYObtenerToken("operario1", "Eco2026*");

        CrearPesajeRequest req = new CrearPesajeRequest(
            "CARTON",
            new BigDecimal("20.0"),
            new BigDecimal("2.0"),
            null,
            "Intento en ruta cerrada",
            null
        );

        mockMvc.perform(
            post("/api/v1/rutas/1/puntos/1/pesajes")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.codigo").value("RUTA_CERRADA"));
    }

    @Test
    @Order(14)
    void test14_RN02_operarioSoloPuedeCorregirSusPropiosPesajesDelDia() throws Exception {
        jdbcTemplate.execute("UPDATE ruta SET estado = 'ABIERTA', cerrada_en = NULL, cerrada_por = NULL WHERE id = 1");

        String tokenOperario1 = loginYObtenerToken("operario1", "Eco2026*");

        // Intentar corregir pesaje #2 (pertenece a operario2)
        CorregirPesajeRequest req = new CorregirPesajeRequest(
            "CARTON",
            new BigDecimal("50.0"),
            new BigDecimal("5.0"),
            null,
            "Intento de editar pesaje ajeno"
        );

        mockMvc.perform(
            put("/api/v1/pesajes/2")
                .header("Authorization", "Bearer " + tokenOperario1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.codigo").value("PESAJE_AJENO"));
    }

    @Test
    @Order(15)
    void test15_anulacionLogicaDePesajeConMotivoObligatorio() throws Exception {
        String tokenSupervisor = loginYObtenerToken("supervisor1", "Eco2026*");

        AnularPesajeRequest req = new AnularPesajeRequest("Material con humedad excesiva no apto para reciclaje");

        mockMvc.perform(
            post("/api/v1/pesajes/1/anulacion")
                .header("Authorization", "Bearer " + tokenSupervisor)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.estado").value("ANULADO"))
            .andExpect(jsonPath("$.motivoAnulacion").value("Material con humedad excesiva no apto para reciclaje"));
    }
}

