package com.iesdoctorbalmis.spring.config;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iesdoctorbalmis.spring.modelo.Centro;
import com.iesdoctorbalmis.spring.modelo.Direccion;
import com.iesdoctorbalmis.spring.modelo.Documento;
import com.iesdoctorbalmis.spring.modelo.Empresa;
import com.iesdoctorbalmis.spring.modelo.ListaLer;
import com.iesdoctorbalmis.spring.modelo.Notificacion;
import com.iesdoctorbalmis.spring.modelo.Recogida;
import com.iesdoctorbalmis.spring.modelo.Residuo;
import com.iesdoctorbalmis.spring.modelo.Ruta;
import com.iesdoctorbalmis.spring.modelo.SolicitudRegistro;
import com.iesdoctorbalmis.spring.modelo.Traslado;
import com.iesdoctorbalmis.spring.modelo.Usuario;
import com.iesdoctorbalmis.spring.modelo.enums.EstadoDocumento;
import com.iesdoctorbalmis.spring.modelo.enums.EstadoRecogida;
import com.iesdoctorbalmis.spring.modelo.enums.EstadoRuta;
import com.iesdoctorbalmis.spring.modelo.enums.EstadoSolicitud;
import com.iesdoctorbalmis.spring.modelo.enums.EstadoTraslado;
import com.iesdoctorbalmis.spring.modelo.enums.Rol;
import com.iesdoctorbalmis.spring.modelo.enums.TipoDocumento;
import com.iesdoctorbalmis.spring.repository.CentroRepository;
import com.iesdoctorbalmis.spring.repository.DireccionRepository;
import com.iesdoctorbalmis.spring.repository.DocumentoRepository;
import com.iesdoctorbalmis.spring.repository.EmpresaRepository;
import com.iesdoctorbalmis.spring.repository.ListaLerRepository;
import com.iesdoctorbalmis.spring.repository.NotificacionRepository;
import com.iesdoctorbalmis.spring.repository.RecogidaRepository;
import com.iesdoctorbalmis.spring.repository.ResiduoRepository;
import com.iesdoctorbalmis.spring.repository.RutaRepository;
import com.iesdoctorbalmis.spring.repository.SolicitudRegistroRepository;
import com.iesdoctorbalmis.spring.repository.TrasladoRepository;
import com.iesdoctorbalmis.spring.repository.UsuarioRepository;
import com.iesdoctorbalmis.spring.servicios.TrasladoService;

@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private record LerEntry(String codigo, String texto) {
    }

    @Value("${ecoadmin.admin.email:admin@ecoadmin.com}")
    private String adminEmail;

    @Value("${ecoadmin.admin.password:admin123}")
    private String adminPassword;

    @Value("${ecoadmin.seed.enabled:true}")
    private boolean seedEnabled;

    private final UsuarioRepository usuarioRepository;
    private final DireccionRepository direccionRepository;
    private final CentroRepository centroRepository;
    private final ResiduoRepository residuoRepository;
    private final TrasladoRepository trasladoRepository;
    private final ListaLerRepository listaLerRepository;
        private final NotificacionRepository notificacionRepository;
    private final DocumentoRepository documentoRepository;
    private final EmpresaRepository empresaRepository;
    private final RecogidaRepository recogidaRepository;
    private final RutaRepository rutaRepository;
        private final SolicitudRegistroRepository solicitudRegistroRepository;
    private final TrasladoService trasladoService;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    public DataInitializer(UsuarioRepository usuarioRepository,
            DireccionRepository direccionRepository,
            CentroRepository centroRepository,
            ResiduoRepository residuoRepository,
            TrasladoRepository trasladoRepository,
            ListaLerRepository listaLerRepository,
            NotificacionRepository notificacionRepository,
            DocumentoRepository documentoRepository,
            EmpresaRepository empresaRepository,
            RecogidaRepository recogidaRepository,
            RutaRepository rutaRepository,
            SolicitudRegistroRepository solicitudRegistroRepository,
            TrasladoService trasladoService,
            PasswordEncoder passwordEncoder,
            JdbcTemplate jdbcTemplate) {
        this.usuarioRepository = usuarioRepository;
        this.direccionRepository = direccionRepository;
        this.centroRepository = centroRepository;
        this.residuoRepository = residuoRepository;
        this.trasladoRepository = trasladoRepository;
        this.listaLerRepository = listaLerRepository;
        this.notificacionRepository = notificacionRepository;
        this.documentoRepository = documentoRepository;
        this.empresaRepository = empresaRepository;
        this.recogidaRepository = recogidaRepository;
        this.rutaRepository = rutaRepository;
        this.solicitudRegistroRepository = solicitudRegistroRepository;
        this.trasladoService = trasladoService;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        cargarListaLer();
                if (!seedEnabled) {
                        return;
                }
                if (usuarioRepository.count() == 0) {
            log.info("Iniciando carga de datos semilla...");
            seedDatos();
            log.info("Datos semilla cargados correctamente.");
        }
                asegurarSolicitudesDemo();
                                asegurarNotificacionesDemo();
    }

    private void cargarListaLer() {
        if (listaLerRepository.count() > 0) {
            log.info("Lista LER ya cargada ({} entradas)", listaLerRepository.count());
            return;
        }
        try {
            InputStream is = new ClassPathResource("data/lista_ler.json").getInputStream();
            List<LerEntry> entries = new ObjectMapper().readValue(is, new TypeReference<>() {
            });
            List<ListaLer> entidades = entries.stream()
                    .filter(e -> e.codigo() != null && !e.codigo().isBlank())
                    .map(e -> new ListaLer(e.codigo().trim(), e.texto().trim()))
                    .toList();
            listaLerRepository.saveAll(entidades);
            log.info("Cargados {} codigos LER", entidades.size());
        } catch (Exception e) {
            log.warn("No se pudo cargar lista_ler.json: {}", e.getMessage());
        }
    }

    private void seedDatos() {
        // == 1. USUARIOS (10) ==
        String pw = passwordEncoder.encode(adminPassword);

        Usuario admin = usuarioRepository.save(new Usuario("Admin Principal", adminEmail, pw, Rol.ADMIN));
        Usuario gestor1 = usuarioRepository
                .save(new Usuario("Laura Martínez", "laura.martinez@ecoadmin.com", pw, Rol.GESTOR));
        Usuario gestor2 = usuarioRepository
                .save(new Usuario("Carlos Ruiz", "carlos.ruiz@ecoadmin.com", pw, Rol.GESTOR));
        Usuario prod1 = usuarioRepository.save(new Usuario("Ana García", "ana.garcia@ecoadmin.com", pw, Rol.PRODUCTOR));
        Usuario prod2 = usuarioRepository
                .save(new Usuario("Miguel Torres", "miguel.torres@ecoadmin.com", pw, Rol.PRODUCTOR));
        Usuario prod3 = usuarioRepository
                .save(new Usuario("Elena Sánchez", "elena.sanchez@ecoadmin.com", pw, Rol.PRODUCTOR));
        Usuario prod4 = usuarioRepository
                .save(new Usuario("Javier López", "javier.lopez@ecoadmin.com", pw, Rol.PRODUCTOR));
        Usuario trans1 = usuarioRepository
                .save(new Usuario("Pedro Navarro", "pedro.navarro@ecoadmin.com", pw, Rol.TRANSPORTISTA));
        Usuario trans2 = usuarioRepository
                .save(new Usuario("Rosa Fernández", "rosa.fernandez@ecoadmin.com", pw, Rol.TRANSPORTISTA));
        Usuario trans3 = usuarioRepository
                .save(new Usuario("David Herrero", "david.herrero@ecoadmin.com", pw, Rol.TRANSPORTISTA));

        // == 2. DIRECCIONES (16) ==
        Direccion d1 = new Direccion("Av. de la Constitución, 10", "Alicante", "03001", "Alicante", "España");
        d1.setNombre("Oficina Central Alicante");
        d1.setLatitud(38.3452);
        d1.setLongitud(-0.481);
        d1 = direccionRepository.save(d1);

        Direccion d2 = new Direccion("C/ San Vicente, 45", "Valencia", "46002", "Valencia", "España");
        d2.setNombre("Sede Valencia Centro");
        d2.setLatitud(39.4699);
        d2.setLongitud(-0.3763);
        d2 = direccionRepository.save(d2);

        Direccion d3 = new Direccion("Pol. Ind. Agua Amarga, Parcela 7", "Alicante", "03114", "Alicante", "España");
        d3.setNombre("Planta Tratamiento Alicante");
        d3.setLatitud(38.326);
        d3.setLongitud(-0.495);
        d3 = direccionRepository.save(d3);

        Direccion d4 = new Direccion("Av. del Mediterráneo, 120", "Benidorm", "03501", "Alicante", "España");
        d4.setNombre("Hotel Costa Blanca");
        d4.setLatitud(38.5411);
        d4.setLongitud(-0.1225);
        d4 = direccionRepository.save(d4);

        Direccion d5 = new Direccion("C/ Mayor, 22", "Elche", "03201", "Alicante", "España");
        d5.setNombre("Fábrica Calzado Elche");
        d5.setLatitud(38.2669);
        d5.setLongitud(-0.6983);
        d5 = direccionRepository.save(d5);

        Direccion d6 = new Direccion("Pol. Ind. Torrellano, Nave 15", "Elche", "03320", "Alicante", "España");
        d6.setNombre("Centro Logístico Torrellano");
        d6.setLatitud(38.285);
        d6.setLongitud(-0.575);
        d6 = direccionRepository.save(d6);

        Direccion d7 = new Direccion("Av. de Denia, 78", "Jávea", "03730", "Alicante", "España");
        d7.setNombre("Puerto Deportivo Jávea");
        d7.setLatitud(38.7833);
        d7.setLongitud(0.1667);
        d7 = direccionRepository.save(d7);

        Direccion d8 = new Direccion("C/ Colom, 5", "Alcoy", "03801", "Alicante", "España");
        d8.setNombre("Textil Serpis S.L.");
        d8.setLatitud(38.6985);
        d8.setLongitud(-0.4737);
        d8 = direccionRepository.save(d8);

        Direccion d9 = new Direccion("Pol. Ind. Las Atalayas, Calle B, 3", "Alicante", "03114", "Alicante", "España");
        d9.setNombre("Gestor Residuos Levante");
        d9.setLatitud(38.335);
        d9.setLongitud(-0.51);
        d9 = direccionRepository.save(d9);

        Direccion d10 = new Direccion("Av. Juan Carlos I, 50", "Murcia", "30008", "Murcia", "España");
        d10.setNombre("Planta Reciclaje Murcia");
        d10.setLatitud(37.9922);
        d10.setLongitud(-1.1307);
        d10 = direccionRepository.save(d10);

        Direccion d11 = new Direccion("C/ Corredera, 12", "Orihuela", "03300", "Alicante", "España");
        d11.setNombre("Agroalimentaria Vega Baja");
        d11.setLatitud(38.0847);
        d11.setLongitud(-0.9441);
        d11 = direccionRepository.save(d11);

        Direccion d12 = new Direccion("Pol. Ind. Fuente del Jarro, 22", "Paterna", "46988", "Valencia", "España");
        d12.setNombre("Reciclados Valencia Norte");
        d12.setLatitud(39.505);
        d12.setLongitud(-0.435);
        d12 = direccionRepository.save(d12);

        Direccion d13 = new Direccion("Carretera de Castalla km 3", "Ibi", "03440", "Alicante", "España");
        d13.setNombre("Juguetes Mediterráneo S.A.");
        d13.setLatitud(38.6268);
        d13.setLongitud(-0.5683);
        d13 = direccionRepository.save(d13);

        Direccion d14 = new Direccion("C/ del Mar, 33", "Dénia", "03700", "Alicante", "España");
        d14.setNombre("Conservas Marina Dénia");
        d14.setLatitud(38.8406);
        d14.setLongitud(0.1056);
        d14 = direccionRepository.save(d14);

        Direccion d15 = new Direccion("Av. Oscar Esplá, 30", "Alicante", "03003", "Alicante", "España");
        d15.setNombre("Clínica Salud Alicante");
        d15.setLatitud(38.3436);
        d15.setLongitud(-0.4907);
        d15 = direccionRepository.save(d15);

        Direccion d16 = new Direccion("Pol. Ind. El Príncipe, Nave 8", "San Javier", "30730", "Murcia", "España");
        d16.setNombre("Transportes Mar Menor");
        d16.setLatitud(37.805);
        d16.setLongitud(-0.837);
        d16 = direccionRepository.save(d16);

        // == 3. CENTROS (13) ==
        Centro cp1 = new Centro(prod1, "Hotel Costa Blanca", "PRODUCTOR", d4);
        cp1.setNima("2800000001");
        cp1.setTelefono("965851234");
        cp1.setEmail("hotel@costabl.com");
        cp1.setNombreContacto("Ana García");
        cp1 = centroRepository.save(cp1);

        Centro cp2 = new Centro(prod1, "Fábrica Calzado Elche", "PRODUCTOR", d5);
        cp2.setNima("2800000002");
        cp2.setTelefono("966611234");
        cp2.setEmail("fabrica@calzadoelche.com");
        cp2.setNombreContacto("Ana García");
        cp2 = centroRepository.save(cp2);

        Centro cp3 = new Centro(prod2, "Textil Serpis S.L.", "PRODUCTOR", d8);
        cp3.setNima("2800000003");
        cp3.setTelefono("965541234");
        cp3.setEmail("info@textilserpis.com");
        cp3.setNombreContacto("Miguel Torres");
        cp3 = centroRepository.save(cp3);

        Centro cp4 = new Centro(prod3, "Conservas Marina Dénia", "PRODUCTOR", d14);
        cp4.setNima("2800000004");
        cp4.setTelefono("965781234");
        cp4.setEmail("info@conservasmarina.com");
        cp4.setNombreContacto("Elena Sánchez");
        cp4 = centroRepository.save(cp4);

        Centro cp5 = new Centro(prod3, "Clínica Salud Alicante", "PRODUCTOR", d15);
        cp5.setNima("2800000005");
        cp5.setTelefono("965201234");
        cp5.setEmail("residuos@clinicasalud.com");
        cp5.setNombreContacto("Elena Sánchez");
        cp5 = centroRepository.save(cp5);

        Centro cp6 = new Centro(prod4, "Juguetes Mediterráneo S.A.", "PRODUCTOR", d13);
        cp6.setNima("2800000006");
        cp6.setTelefono("965551234");
        cp6.setEmail("medioambiente@jugmed.com");
        cp6.setNombreContacto("Javier López");
        cp6 = centroRepository.save(cp6);

        Centro cp7 = new Centro(prod2, "Agroalimentaria Vega Baja", "PRODUCTOR", d11);
        cp7.setNima("2800000007");
        cp7.setTelefono("966301234");
        cp7.setEmail("residuos@agrovegabaja.com");
        cp7.setNombreContacto("Miguel Torres");
        cp7 = centroRepository.save(cp7);

        Centro cg1 = new Centro(gestor1, "Planta Tratamiento Alicante", "GESTOR", d3);
        cg1.setNima("2800000010");
        cg1.setTelefono("965301234");
        cg1.setEmail("planta@gestorlevante.com");
        cg1.setNombreContacto("Laura Martínez");
        cg1 = centroRepository.save(cg1);

        Centro cg2 = new Centro(gestor1, "Gestor Residuos Levante", "GESTOR", d9);
        cg2.setNima("2800000011");
        cg2.setTelefono("965311234");
        cg2.setEmail("info@gestorlevante.com");
        cg2.setNombreContacto("Laura Martínez");
        cg2 = centroRepository.save(cg2);

        Centro cg3 = new Centro(gestor2, "Planta Reciclaje Murcia", "GESTOR", d10);
        cg3.setNima("2800000012");
        cg3.setTelefono("968201234");
        cg3.setEmail("planta@reciclajemurcia.com");
        cg3.setNombreContacto("Carlos Ruiz");
        cg3 = centroRepository.save(cg3);

        Centro cg4 = new Centro(gestor2, "Reciclados Valencia Norte", "GESTOR", d12);
        cg4.setNima("2800000013");
        cg4.setTelefono("961501234");
        cg4.setEmail("info@recicladosvn.com");
        cg4.setNombreContacto("Carlos Ruiz");
        cg4 = centroRepository.save(cg4);

        Centro cg5 = new Centro(gestor1, "Centro Logístico Torrellano", "GESTOR", d6);
        cg5.setNima("2800000014");
        cg5.setTelefono("966321234");
        cg5.setEmail("logistica@torrellano.com");
        cg5.setNombreContacto("Laura Martínez");
        cg5 = centroRepository.save(cg5);

        Centro cg6 = new Centro(gestor2, "Puerto Deportivo Jávea", "GESTOR", d7);
        cg6.setNima("2800000015");
        cg6.setTelefono("965791234");
        cg6.setEmail("medio@puertojavea.com");
        cg6.setNombreContacto("Carlos Ruiz");
        cg6 = centroRepository.save(cg6);

        // == 4. EMPRESA ==
        Empresa emp = new Empresa();
        emp.setNombre("EcoAdmin Levante S.L.");
        emp.setCif("B12345678");
        emp.setNima("2800000099");
        emp.setTelefono("965001234");
        emp.setEmail("info@ecoadminlevante.com");
        emp.setWeb("https://ecoadminlevante.com");
        emp.setDireccion("Av. de la Constitución, 10");
        emp.setCiudad("Alicante");
        emp.setCodigoPostal("03001");
        emp.setProvincia("Alicante");
        emp.setPais("España");
        emp.setAutorizacionGestor("AG-CV-2024-001");
        emp.setAutorizacionTransportista("AT-CV-2024-001");
        emp.setAutorizacionProductor("AP-CV-2024-001");
        empresaRepository.save(emp);

        // == 5. RESIDUOS (22) ==
        LocalDateTime now = LocalDateTime.now();

        Residuo r1 = new Residuo(1500.0, "kg", "ALMACENADO", cp1);
        r1.setCodigoLER("200301");
        r1.setDescripcion("Mezclas de residuos municipales");
        r1.setFechaEntradaAlmacen(now.minusDays(200));
        r1.setDiasMaximoAlmacenamiento(180);
        r1 = residuoRepository.save(r1);

        Residuo r2 = new Residuo(800.0, "kg", "ALMACENADO", cp1);
        r2.setCodigoLER("150101");
        r2.setDescripcion("Envases de papel y cartón");
        r2.setFechaEntradaAlmacen(now.minusDays(45));
        r2.setDiasMaximoAlmacenamiento(180);
        r2 = residuoRepository.save(r2);

        Residuo r3 = new Residuo(350.0, "kg", "ALMACENADO", cp2);
        r3.setCodigoLER("040199");
        r3.setDescripcion("Residuos industria del cuero");
        r3.setFechaEntradaAlmacen(now.minusDays(190));
        r3.setDiasMaximoAlmacenamiento(180);
        r3 = residuoRepository.save(r3);

        Residuo r4 = new Residuo(120.0, "litros", "ALMACENADO", cp2);
        r4.setCodigoLER("140603");
        r4.setDescripcion("Disolventes y mezclas orgánicas");
        r4.setFechaEntradaAlmacen(now.minusDays(30));
        r4.setDiasMaximoAlmacenamiento(180);
        r4 = residuoRepository.save(r4);

        Residuo r5 = new Residuo(2000.0, "kg", "ALMACENADO", cp3);
        r5.setCodigoLER("040222");
        r5.setDescripcion("Residuos de fibras textiles");
        r5.setFechaEntradaAlmacen(now.minusDays(60));
        r5.setDiasMaximoAlmacenamiento(180);
        r5 = residuoRepository.save(r5);

        Residuo r6 = new Residuo(450.0, "kg", "ALMACENADO", cp3);
        r6.setCodigoLER("150102");
        r6.setDescripcion("Envases de plástico");
        r6.setFechaEntradaAlmacen(now.minusDays(15));
        r6.setDiasMaximoAlmacenamiento(180);
        r6 = residuoRepository.save(r6);

        Residuo r7 = new Residuo(600.0, "kg", "ALMACENADO", cp4);
        r7.setCodigoLER("020204");
        r7.setDescripcion("Lodos del tratamiento de efluentes");
        r7.setFechaEntradaAlmacen(now.minusDays(185));
        r7.setDiasMaximoAlmacenamiento(180);
        r7 = residuoRepository.save(r7);

        Residuo r8 = new Residuo(250.0, "kg", "ALMACENADO", cp4);
        r8.setCodigoLER("150104");
        r8.setDescripcion("Envases metálicos");
        r8.setFechaEntradaAlmacen(now.minusDays(10));
        r8.setDiasMaximoAlmacenamiento(180);
        r8 = residuoRepository.save(r8);

        Residuo r9 = new Residuo(80.0, "kg", "ALMACENADO", cp5);
        r9.setCodigoLER("180103");
        r9.setDescripcion("Residuos sanitarios infecciosos");
        r9.setFechaEntradaAlmacen(now.minusDays(5));
        r9.setDiasMaximoAlmacenamiento(30);
        r9 = residuoRepository.save(r9);

        Residuo r10 = new Residuo(35.0, "kg", "ALMACENADO", cp5);
        r10.setCodigoLER("180106");
        r10.setDescripcion("Productos químicos de laboratorio");
        r10.setFechaEntradaAlmacen(now.minusDays(25));
        r10.setDiasMaximoAlmacenamiento(90);
        r10 = residuoRepository.save(r10);

        Residuo r11 = new Residuo(1200.0, "kg", "ALMACENADO", cp6);
        r11.setCodigoLER("070213");
        r11.setDescripcion("Residuos de plástico");
        r11.setFechaEntradaAlmacen(now.minusDays(75));
        r11.setDiasMaximoAlmacenamiento(180);
        r11 = residuoRepository.save(r11);

        Residuo r12 = new Residuo(90.0, "litros", "ALMACENADO", cp6);
        r12.setCodigoLER("080111");
        r12.setDescripcion("Residuos de pintura y barniz");
        r12.setFechaEntradaAlmacen(now.minusDays(40));
        r12.setDiasMaximoAlmacenamiento(180);
        r12 = residuoRepository.save(r12);

        Residuo r13 = new Residuo(3000.0, "kg", "ALMACENADO", cp7);
        r13.setCodigoLER("020304");
        r13.setDescripcion("Materias no aptas para consumo");
        r13.setFechaEntradaAlmacen(now.minusDays(20));
        r13.setDiasMaximoAlmacenamiento(90);
        r13 = residuoRepository.save(r13);

        Residuo r14 = new Residuo(500.0, "kg", "ALMACENADO", cp7);
        r14.setCodigoLER("150106");
        r14.setDescripcion("Envases mezclados");
        r14.setFechaEntradaAlmacen(now.minusDays(55));
        r14.setDiasMaximoAlmacenamiento(180);
        r14 = residuoRepository.save(r14);

        Residuo r15 = new Residuo(700.0, "kg", "ALMACENADO", cp1);
        r15.setCodigoLER("200108");
        r15.setDescripcion("Residuos biodegradables de cocina");
        r15.setFechaEntradaAlmacen(now.minusDays(8));
        r15.setDiasMaximoAlmacenamiento(30);
        r15 = residuoRepository.save(r15);

        Residuo r16 = new Residuo(160.0, "kg", "ALMACENADO", cp2);
        r16.setCodigoLER("120105");
        r16.setDescripcion("Virutas y torneados plásticos");
        r16.setFechaEntradaAlmacen(now.minusDays(100));
        r16.setDiasMaximoAlmacenamiento(180);
        r16 = residuoRepository.save(r16);

        Residuo r17 = new Residuo(420.0, "kg", "ALMACENADO", cp3);
        r17.setCodigoLER("040209");
        r17.setDescripcion("Residuos de materiales compuestos textiles");
        r17.setFechaEntradaAlmacen(now.minusDays(130));
        r17.setDiasMaximoAlmacenamiento(180);
        r17 = residuoRepository.save(r17);

        Residuo r18 = new Residuo(55.0, "litros", "ALMACENADO", cp5);
        r18.setCodigoLER("180102");
        r18.setDescripcion("Restos anatómicos y órganos");
        r18.setFechaEntradaAlmacen(now.minusDays(2));
        r18.setDiasMaximoAlmacenamiento(7);
        r18 = residuoRepository.save(r18);

        Residuo r19 = new Residuo(1800.0, "kg", "ALMACENADO", cp6);
        r19.setCodigoLER("170201");
        r19.setDescripcion("Madera de embalajes");
        r19.setFechaEntradaAlmacen(now.minusDays(65));
        r19.setDiasMaximoAlmacenamiento(180);
        r19 = residuoRepository.save(r19);

        Residuo r20 = new Residuo(300.0, "kg", "ALMACENADO", cp4);
        r20.setCodigoLER("020501");
        r20.setDescripcion("Materias no aptas para consumo - lácteos");
        r20.setFechaEntradaAlmacen(now.minusDays(12));
        r20.setDiasMaximoAlmacenamiento(60);
        r20 = residuoRepository.save(r20);

        Residuo r21 = new Residuo(950.0, "kg", "ALMACENADO", cp7);
        r21.setCodigoLER("020106");
        r21.setDescripcion("Heces, orina y estiércol animal");
        r21.setFechaEntradaAlmacen(now.minusDays(35));
        r21.setDiasMaximoAlmacenamiento(90);
        r21 = residuoRepository.save(r21);

        Residuo r22 = new Residuo(200.0, "kg", "ALMACENADO", cp1);
        r22.setCodigoLER("200121");
        r22.setDescripcion("Tubos fluorescentes con mercurio");
        r22.setFechaEntradaAlmacen(now.minusDays(192));
        r22.setDiasMaximoAlmacenamiento(180);
        r22 = residuoRepository.save(r22);

        // == 6. TRASLADOS (16) ==
        LocalDate today = LocalDate.now();

        Traslado t1 = new Traslado(cp1, cg1, r1, trans1);
        t1.setFechaProgramadaInicio(now.minusDays(60));
        t1.setFechaProgramadaFin(now.minusDays(58));
        t1 = trasladoRepository.save(t1);

        Traslado t2 = new Traslado(cp2, cg2, r3, trans1);
        t2.setFechaProgramadaInicio(now.minusDays(50));
        t2.setFechaProgramadaFin(now.minusDays(48));
        t2 = trasladoRepository.save(t2);

        Traslado t3 = new Traslado(cp3, cg1, r5, trans2);
        t3.setFechaProgramadaInicio(now.minusDays(30));
        t3.setFechaProgramadaFin(now.minusDays(28));
        t3 = trasladoRepository.save(t3);

        Traslado t4 = new Traslado(cp4, cg3, r7, trans2);
        t4.setFechaProgramadaInicio(now.minusDays(2));
        t4.setFechaProgramadaFin(now.plusDays(0));
        t4 = trasladoRepository.save(t4);

        Traslado t5 = new Traslado(cp5, cg2, r9, trans3);
        t5.setFechaProgramadaInicio(now.plusDays(3));
        t5.setFechaProgramadaFin(now.plusDays(4));
        t5 = trasladoRepository.save(t5);

        Traslado t6 = new Traslado(cp6, cg4, r11, trans1);
        t6.setFechaProgramadaInicio(now.plusDays(5));
        t6.setFechaProgramadaFin(now.plusDays(7));
        t6 = trasladoRepository.save(t6);

        Traslado t7 = new Traslado(cp7, cg3, r13, trans3);
        t7.setFechaProgramadaInicio(now.minusDays(40));
        t7.setFechaProgramadaFin(now.minusDays(38));
        t7 = trasladoRepository.save(t7);

        Traslado t8 = new Traslado(cp1, cg2, r2, trans2);
        t8.setFechaProgramadaInicio(now.minusDays(20));
        t8.setFechaProgramadaFin(now.minusDays(18));
        t8 = trasladoRepository.save(t8);

        Traslado t9 = new Traslado(cp2, cg1, r4, trans1);
        t9.setFechaProgramadaInicio(now.minusDays(1));
        t9.setFechaProgramadaFin(now.plusDays(1));
        t9 = trasladoRepository.save(t9);

        Traslado t10 = new Traslado(cp3, cg4, r6, trans3);
        t10.setFechaProgramadaInicio(now.plusDays(10));
        t10.setFechaProgramadaFin(now.plusDays(12));
        t10 = trasladoRepository.save(t10);

        Traslado t11 = new Traslado(cp4, cg1, r8, trans1);
        t11.setFechaProgramadaInicio(now.minusDays(70));
        t11.setFechaProgramadaFin(now.minusDays(68));
        t11 = trasladoRepository.save(t11);

        Traslado t12 = new Traslado(cp6, cg2, r12, trans2);
        t12.setFechaProgramadaInicio(now.plusDays(7));
        t12.setFechaProgramadaFin(now.plusDays(9));
        t12 = trasladoRepository.save(t12);

        Traslado t13 = new Traslado(cp7, cg4, r14, trans3);
        t13.setFechaProgramadaInicio(now.minusDays(80));
        t13.setFechaProgramadaFin(now.minusDays(78));
        t13 = trasladoRepository.save(t13);

        Traslado t14 = new Traslado(cp5, cg3, r10, trans1);
        t14.setFechaProgramadaInicio(now.minusDays(15));
        t14.setFechaProgramadaFin(now.minusDays(13));
        t14 = trasladoRepository.save(t14);

        Traslado t15 = new Traslado(cp1, cg4, r15, trans2);
        t15.setFechaProgramadaInicio(now.minusDays(1));
        t15.setFechaProgramadaFin(now.plusDays(1));
        t15 = trasladoRepository.save(t15);

        Traslado t16 = new Traslado(cp6, cg1, r19, trans3);
        t16.setFechaProgramadaInicio(now.plusDays(14));
        t16.setFechaProgramadaFin(now.plusDays(16));
        t16 = trasladoRepository.save(t16);

        // Advance traslados through states
        try {
            trasladoService.cambiarEstado(t1.getId(), EstadoTraslado.EN_TRANSITO, "Recogida iniciada", trans1);
            trasladoService.cambiarEstado(t1.getId(), EstadoTraslado.ENTREGADO, "Entregado en planta", trans1);
            trasladoService.cambiarEstado(t1.getId(), EstadoTraslado.COMPLETADO, "Tratamiento finalizado", gestor1);
            trasladoService.cambiarEstado(t2.getId(), EstadoTraslado.EN_TRANSITO, "En camino", trans1);
            trasladoService.cambiarEstado(t2.getId(), EstadoTraslado.ENTREGADO, "Descarga realizada", trans1);
            trasladoService.cambiarEstado(t2.getId(), EstadoTraslado.COMPLETADO, "Proceso completado", gestor1);
            trasladoService.cambiarEstado(t3.getId(), EstadoTraslado.EN_TRANSITO, "Transporte iniciado", trans2);
            trasladoService.cambiarEstado(t3.getId(), EstadoTraslado.ENTREGADO, "Entrega confirmada", trans2);
            trasladoService.cambiarEstado(t4.getId(), EstadoTraslado.EN_TRANSITO, "Carga completada, en ruta", trans2);
            trasladoService.cambiarEstado(t7.getId(), EstadoTraslado.EN_TRANSITO, "Salida del centro", trans3);
            trasladoService.cambiarEstado(t7.getId(), EstadoTraslado.ENTREGADO, "Recepción en destino", trans3);
            trasladoService.cambiarEstado(t7.getId(), EstadoTraslado.COMPLETADO, "Gestión finalizada", gestor2);
            trasladoService.cambiarEstado(t8.getId(), EstadoTraslado.EN_TRANSITO, "Transporte en curso", trans2);
            trasladoService.cambiarEstado(t8.getId(), EstadoTraslado.ENTREGADO, "Material descargado", trans2);
            trasladoService.cambiarEstado(t9.getId(), EstadoTraslado.EN_TRANSITO, "Recogido y en camino", trans1);
            trasladoService.cambiarEstado(t11.getId(), EstadoTraslado.EN_TRANSITO, "Inicio transporte", trans1);
            trasladoService.cambiarEstado(t11.getId(), EstadoTraslado.ENTREGADO, "Llegada a destino", trans1);
            trasladoService.cambiarEstado(t11.getId(), EstadoTraslado.COMPLETADO, "Residuo tratado", gestor1);
            trasladoService.cambiarEstado(t13.getId(), EstadoTraslado.EN_TRANSITO, "Transporte iniciado", trans3);
            trasladoService.cambiarEstado(t13.getId(), EstadoTraslado.ENTREGADO, "Entregado en planta", trans3);
            trasladoService.cambiarEstado(t13.getId(), EstadoTraslado.COMPLETADO, "Reciclaje completado", gestor2);
            trasladoService.cambiarEstado(t14.getId(), EstadoTraslado.EN_TRANSITO, "En ruta hacia gestor", trans1);
            trasladoService.cambiarEstado(t14.getId(), EstadoTraslado.ENTREGADO, "Descarga en destino", trans1);
            trasladoService.cambiarEstado(t15.getId(), EstadoTraslado.EN_TRANSITO, "Salida con carga", trans2);
        } catch (Exception e) {
            log.warn("Error al avanzar estados de traslados: {}", e.getMessage());
        }

        // == 7. RECOGIDAS (12) ==
        Recogida rec1 = new Recogida();
        rec1.setResiduo(r16);
        rec1.setCentroOrigen(cp2);
        rec1.setCentroDestino(cg1);
        rec1.setTransportista(trans1);
        rec1.setFechaProgramada(today.plusDays(2));
        rec1.setEstado(EstadoRecogida.PROGRAMADA);
        rec1.setObservaciones("Recoger virutas plásticas de fábrica");
        recogidaRepository.save(rec1);

        Recogida rec2 = new Recogida();
        rec2.setResiduo(r17);
        rec2.setCentroOrigen(cp3);
        rec2.setCentroDestino(cg2);
        rec2.setTransportista(trans2);
        rec2.setFechaProgramada(today.plusDays(1));
        rec2.setEstado(EstadoRecogida.PROGRAMADA);
        rec2.setObservaciones("Material textil compuesto - frágil");
        recogidaRepository.save(rec2);

        Recogida rec3 = new Recogida();
        rec3.setResiduo(r18);
        rec3.setCentroOrigen(cp5);
        rec3.setCentroDestino(cg1);
        rec3.setTransportista(trans3);
        rec3.setFechaProgramada(today);
        rec3.setEstado(EstadoRecogida.EN_CURSO);
        rec3.setObservaciones("Residuo sanitario - transporte especial");
        recogidaRepository.save(rec3);

        Recogida rec4 = new Recogida();
        rec4.setResiduo(r20);
        rec4.setCentroOrigen(cp4);
        rec4.setCentroDestino(cg3);
        rec4.setTransportista(trans1);
        rec4.setFechaProgramada(today.minusDays(5));
        rec4.setEstado(EstadoRecogida.COMPLETADA);
        rec4.setObservaciones("Recogida de lácteos caducados completada");
        recogidaRepository.save(rec4);

        Recogida rec5 = new Recogida();
        rec5.setResiduo(r21);
        rec5.setCentroOrigen(cp7);
        rec5.setCentroDestino(cg3);
        rec5.setTransportista(trans2);
        rec5.setFechaProgramada(today.minusDays(10));
        rec5.setEstado(EstadoRecogida.COMPLETADA);
        rec5.setObservaciones("Estiércol animal recogido sin incidencias");
        recogidaRepository.save(rec5);

        Recogida rec6 = new Recogida();
        rec6.setResiduo(r22);
        rec6.setCentroOrigen(cp1);
        rec6.setCentroDestino(cg2);
        rec6.setTransportista(trans3);
        rec6.setFechaProgramada(today.plusDays(4));
        rec6.setEstado(EstadoRecogida.PROGRAMADA);
        rec6.setObservaciones("Tubos fluorescentes - URGENTE por días excedidos");
        recogidaRepository.save(rec6);

        Recogida rec7 = new Recogida();
        rec7.setResiduo(r19);
        rec7.setCentroOrigen(cp6);
        rec7.setCentroDestino(cg4);
        rec7.setTransportista(trans1);
        rec7.setFechaProgramada(today.minusDays(3));
        rec7.setEstado(EstadoRecogida.EN_CURSO);
        rec7.setObservaciones("Madera de embalaje - gran volumen");
        recogidaRepository.save(rec7);

        Recogida rec8 = new Recogida();
        rec8.setResiduo(r6);
        rec8.setCentroOrigen(cp3);
        rec8.setCentroDestino(cg1);
        rec8.setTransportista(trans2);
        rec8.setFechaProgramada(today.plusDays(6));
        rec8.setEstado(EstadoRecogida.PROGRAMADA);
        rec8.setObservaciones("Envases plásticos para reciclaje");
        recogidaRepository.save(rec8);

        Recogida rec9 = new Recogida();
        rec9.setResiduo(r8);
        rec9.setCentroOrigen(cp4);
        rec9.setCentroDestino(cg2);
        rec9.setTransportista(trans3);
        rec9.setFechaProgramada(today.minusDays(15));
        rec9.setEstado(EstadoRecogida.CANCELADA);
        rec9.setObservaciones("Cancelada por cierre temporal del centro");
        recogidaRepository.save(rec9);

        Recogida rec10 = new Recogida();
        rec10.setResiduo(r4);
        rec10.setCentroOrigen(cp2);
        rec10.setCentroDestino(cg1);
        rec10.setTransportista(trans1);
        rec10.setFechaProgramada(today.minusDays(7));
        rec10.setEstado(EstadoRecogida.COMPLETADA);
        rec10.setObservaciones("Disolventes recogidos con ADR");
        recogidaRepository.save(rec10);

        Recogida rec11 = new Recogida();
        rec11.setResiduo(r10);
        rec11.setCentroOrigen(cp5);
        rec11.setCentroDestino(cg3);
        rec11.setTransportista(trans2);
        rec11.setFechaProgramada(today.plusDays(8));
        rec11.setEstado(EstadoRecogida.PROGRAMADA);
        rec11.setObservaciones("Productos químicos laboratorio");
        recogidaRepository.save(rec11);

        Recogida rec12 = new Recogida();
        rec12.setResiduo(r12);
        rec12.setCentroOrigen(cp6);
        rec12.setCentroDestino(cg4);
        rec12.setTransportista(trans3);
        rec12.setFechaProgramada(today.minusDays(1));
        rec12.setEstado(EstadoRecogida.EN_CURSO);
        rec12.setObservaciones("Pintura y barniz - carga peligrosa");
        recogidaRepository.save(rec12);

        // == 8. DOCUMENTOS (10) ==
        Documento doc1 = new Documento(TipoDocumento.NOTIFICACION_PREVIA, t1, "NP-2024-0001");
        doc1.setEstado(EstadoDocumento.CERRADO);
        doc1.setFechaCierre(today.minusDays(55));
        documentoRepository.save(doc1);

        Documento doc2 = new Documento(TipoDocumento.DOCUMENTO_IDENTIFICACION, t1, "DI-2024-0001");
        doc2.setEstado(EstadoDocumento.CERRADO);
        doc2.setFechaCierre(today.minusDays(55));
        documentoRepository.save(doc2);

        Documento doc3 = new Documento(TipoDocumento.HOJA_SEGUIMIENTO, t2, "HS-2024-0001");
        doc3.setEstado(EstadoDocumento.CERRADO);
        doc3.setFechaCierre(today.minusDays(45));
        documentoRepository.save(doc3);

        Documento doc4 = new Documento(TipoDocumento.CONTRATO, t3, "CT-2024-0001");
        doc4.setEstado(EstadoDocumento.EMITIDO);
        doc4.setFechaVencimiento(today.plusDays(30));
        documentoRepository.save(doc4);

        Documento doc5 = new Documento(TipoDocumento.FICHA_ACEPTACION, t4, "FA-2024-0001");
        doc5.setEstado(EstadoDocumento.EMITIDO);
        doc5.setFechaVencimiento(today.plusDays(15));
        documentoRepository.save(doc5);

        Documento doc6 = new Documento(TipoDocumento.NOTIFICACION_PREVIA, t5, "NP-2024-0002");
        doc6.setEstado(EstadoDocumento.BORRADOR);
        documentoRepository.save(doc6);

        Documento doc7 = new Documento(TipoDocumento.ARCHIVO_CRONOLOGICO, t7, "AC-2024-0001");
        doc7.setEstado(EstadoDocumento.CERRADO);
        doc7.setFechaCierre(today.minusDays(35));
        documentoRepository.save(doc7);

        Documento doc8 = new Documento(TipoDocumento.INFORME_FINAL, t11, "IF-2024-0001");
        doc8.setEstado(EstadoDocumento.CERRADO);
        doc8.setFechaCierre(today.minusDays(65));
        documentoRepository.save(doc8);

        Documento doc9 = new Documento(TipoDocumento.DOCUMENTO_IDENTIFICACION, t8, "DI-2024-0002");
        doc9.setEstado(EstadoDocumento.EMITIDO);
        doc9.setFechaVencimiento(today.plusDays(60));
        documentoRepository.save(doc9);

        Documento doc10 = new Documento(TipoDocumento.HOJA_SEGUIMIENTO, t13, "HS-2024-0002");
        doc10.setEstado(EstadoDocumento.VENCIDO);
        doc10.setFechaVencimiento(today.minusDays(10));
        documentoRepository.save(doc10);

        // == 9. RUTAS (6) ==
        Ruta ruta1 = new Ruta();
        ruta1.setNombre("Alicante - Valencia");
        ruta1.setFecha(today.plusDays(0));
        ruta1.setEstado(EstadoRuta.PLANIFICADA);
        ruta1.setOrigen(d1);
        ruta1.setDestino(d12);
        ruta1.setDistanciaKm(170.5);
        ruta1.setFormulaTarifa("w * 0.15 + L * 0.8");
        ruta1.setUnidadTarifa("EUR");
        ruta1.setObservaciones("Ruta habitual norte");
        ruta1 = rutaRepository.save(ruta1);
        jdbcTemplate.update("INSERT INTO ruta_transportistas (ruta_id, transportista_id, activo) VALUES (?, ?, 1)",
                ruta1.getId(), trans1.getId());

        Ruta ruta2 = new Ruta();
        ruta2.setNombre("Elche - Murcia");
        ruta2.setFecha(today.minusDays(5));
        ruta2.setEstado(EstadoRuta.EN_CURSO);
        ruta2.setOrigen(d5);
        ruta2.setDestino(d10);
        ruta2.setDistanciaKm(85.2);
        ruta2.setFormulaTarifa("w * 0.12 + L * 0.6");
        ruta2.setUnidadTarifa("EUR");
        ruta2.setObservaciones("Transporte residuos industriales");
        ruta2 = rutaRepository.save(ruta2);
        jdbcTemplate.update("INSERT INTO ruta_transportistas (ruta_id, transportista_id, activo) VALUES (?, ?, 1)",
                ruta2.getId(), trans2.getId());

        Ruta ruta3 = new Ruta();
        ruta3.setNombre("Benidorm - Alicante");
        ruta3.setFecha(today.plusDays(3));
        ruta3.setEstado(EstadoRuta.PLANIFICADA);
        ruta3.setOrigen(d4);
        ruta3.setDestino(d3);
        ruta3.setDistanciaKm(45.0);
        ruta3.setFormulaTarifa("w * 0.20");
        ruta3.setUnidadTarifa("EUR");
        ruta3.setObservaciones("Residuos hoteleros");
        ruta3 = rutaRepository.save(ruta3);
        jdbcTemplate.update("INSERT INTO ruta_transportistas (ruta_id, transportista_id, activo) VALUES (?, ?, 1)",
                ruta3.getId(), trans1.getId());

        Ruta ruta4 = new Ruta();
        ruta4.setNombre("Alcoy - Valencia");
        ruta4.setFecha(today.minusDays(30));
        ruta4.setEstado(EstadoRuta.COMPLETADA);
        ruta4.setOrigen(d8);
        ruta4.setDestino(d12);
        ruta4.setDistanciaKm(115.0);
        ruta4.setFormulaTarifa("w * 0.18 + L * 0.5");
        ruta4.setUnidadTarifa("EUR");
        ruta4.setObservaciones("Ruta textiles");
        ruta4 = rutaRepository.save(ruta4);
        jdbcTemplate.update("INSERT INTO ruta_transportistas (ruta_id, transportista_id, activo) VALUES (?, ?, 1)",
                ruta4.getId(), trans3.getId());

        Ruta ruta5 = new Ruta();
        ruta5.setNombre("Orihuela - Murcia");
        ruta5.setFecha(today.plusDays(7));
        ruta5.setEstado(EstadoRuta.PLANIFICADA);
        ruta5.setOrigen(d11);
        ruta5.setDestino(d10);
        ruta5.setDistanciaKm(60.0);
        ruta5.setFormulaTarifa("w * 0.10 + L * 0.4");
        ruta5.setUnidadTarifa("EUR");
        ruta5.setObservaciones("Ruta agroalimentaria");
        ruta5 = rutaRepository.save(ruta5);
        jdbcTemplate.update("INSERT INTO ruta_transportistas (ruta_id, transportista_id, activo) VALUES (?, ?, 1)",
                ruta5.getId(), trans2.getId());

        Ruta ruta6 = new Ruta();
        ruta6.setNombre("Denia - Alicante");
        ruta6.setFecha(today.minusDays(15));
        ruta6.setEstado(EstadoRuta.COMPLETADA);
        ruta6.setOrigen(d14);
        ruta6.setDestino(d9);
        ruta6.setDistanciaKm(95.0);
        ruta6.setFormulaTarifa("w * 0.22 + L * 0.7");
        ruta6.setUnidadTarifa("EUR");
        ruta6.setObservaciones("Conservas y envases");
        ruta6 = rutaRepository.save(ruta6);
        jdbcTemplate.update("INSERT INTO ruta_transportistas (ruta_id, transportista_id, activo) VALUES (?, ?, 1)",
                ruta6.getId(), trans3.getId());

        // == 10. JDBC: spread fechaCreacion across last 90 days ==
        log.info("Ajustando fechas de creacion para historico...");
        try {
            List<Traslado> todos = trasladoRepository.findAll();
            int[] diasAtras = { 0, 1, 3, 5, 10, 15, 25, 30, 40, 50, 60, 70, 80, 90, 7, 12 };
            for (int i = 0; i < todos.size(); i++) {
                int restar = diasAtras[i % diasAtras.length];
                jdbcTemplate.update(
                        "UPDATE traslados SET fecha_creacion = datetime('now', '-' || ? || ' days') WHERE id = ?",
                        restar, todos.get(i).getId());
            }
        } catch (Exception e) {
            log.warn("No se pudieron ajustar fechas historicas: {}", e.getMessage());
        }

        log.info(
                "Seed completado: 10 usuarios, 16 direcciones, 13 centros, 1 empresa, 22 residuos, 16 traslados, 12 recogidas, 10 documentos, 6 rutas");
    }

    private void asegurarSolicitudesDemo() {
        long solicitudesExistentes = solicitudRegistroRepository.count();
        if (solicitudesExistentes > 0) {
            log.info("Solicitudes de registro ya cargadas ({})", solicitudesExistentes);
            return;
        }

        Usuario adminResolutor = usuarioRepository.findByRol(Rol.ADMIN).stream().findFirst().orElse(null);
        LocalDateTime now = LocalDateTime.now();

        SolicitudRegistro pendienteProductor = nuevaSolicitudDemo(
                "Lucia Morales",
                "lucia.morales.demo@ecoadmin.com",
                Rol.PRODUCTOR,
                EstadoSolicitud.PENDIENTE,
                now.minusDays(2),
                null,
                null,
                null);
        pendienteProductor.setTelefono("600123123");
        pendienteProductor.setDni("11111111A");
        pendienteProductor.setEmpresa("Conservas Costa Azul");
        pendienteProductor.setNima("03/98765");
        pendienteProductor.setCentroPrincipal("Planta San Vicente");

        SolicitudRegistro pendienteTransportista = nuevaSolicitudDemo(
                "Sergio Vera",
                "sergio.vera.demo@ecoadmin.com",
                Rol.TRANSPORTISTA,
                EstadoSolicitud.PENDIENTE,
                now.minusDays(1),
                null,
                null,
                null);
        pendienteTransportista.setTelefono("600456456");
        pendienteTransportista.setDni("22222222B");
        pendienteTransportista.setEmpresa("Logistica Verde Levante");
        pendienteTransportista.setMatricula("1234MNL");
        pendienteTransportista.setCertificadoAdr("ADR-2026-7781");

        SolicitudRegistro aprobadaGestor = nuevaSolicitudDemo(
                "Marta Iborra",
                "marta.iborra.demo@ecoadmin.com",
                Rol.GESTOR,
                EstadoSolicitud.APROBADA,
                now.minusDays(5),
                now.minusDays(3),
                adminResolutor,
                null);
        aprobadaGestor.setTelefono("600789789");
        aprobadaGestor.setDni("33333333C");
        aprobadaGestor.setEmpresa("Reciclajes del Sureste");
        aprobadaGestor.setAutorizacionGestor("AUT-G-2048");

        SolicitudRegistro rechazadaProductor = nuevaSolicitudDemo(
                "Raul Pastor",
                "raul.pastor.demo@ecoadmin.com",
                Rol.PRODUCTOR,
                EstadoSolicitud.RECHAZADA,
                now.minusDays(7),
                now.minusDays(6),
                adminResolutor,
                "Falta acreditar el NIMA del centro solicitado");
        rechazadaProductor.setTelefono("600987987");
        rechazadaProductor.setDni("44444444D");
        rechazadaProductor.setEmpresa("Talleres Pastor");
        rechazadaProductor.setNima("03/12345");
        rechazadaProductor.setCentroPrincipal("Nave Elche Parque Empresarial");

        solicitudRegistroRepository.saveAll(List.of(
                pendienteProductor,
                pendienteTransportista,
                aprobadaGestor,
                rechazadaProductor));

        log.info("Cargadas 4 solicitudes de registro de prueba");
    }

        private void asegurarNotificacionesDemo() {
                Usuario adminDemo = usuarioRepository.findByEmail(adminEmail)
                                .orElseGet(() -> usuarioRepository.findByRol(Rol.ADMIN).stream().findFirst().orElse(null));
                if (adminDemo == null) {
                        log.info("No hay admin disponible para cargar notificaciones demo");
                        return;
                }

                long pendientes = notificacionRepository.countByDestinatarioAndLeidaFalse(adminDemo);
                if (pendientes > 0) {
                        log.info("Notificaciones demo ya cargadas para {} ({})", adminDemo.getEmail(), pendientes);
                        return;
                }

                LocalDateTime now = LocalDateTime.now();
                notificacionRepository.saveAll(List.of(
                                nuevaNotificacionDemo(
                                                adminDemo,
                                                "Nueva solicitud pendiente",
                                                "Revisa una solicitud de registro nueva para validar el flujo de notificaciones.",
                                                "/users",
                                                now.minusHours(2)),
                                nuevaNotificacionDemo(
                                                adminDemo,
                                                "Traslado pendiente de seguimiento",
                                                "Hay un traslado de prueba esperando revision en la vista de envios.",
                                                "/shipments",
                                                now.minusMinutes(45)),
                                nuevaNotificacionDemo(
                                                adminDemo,
                                                "Resumen diario disponible",
                                                "Abre el dashboard para comprobar que la notificacion desaparece al marcarla como leida.",
                                                "/dashboard",
                                                now.minusMinutes(10))));

                log.info("Cargadas 3 notificaciones demo para {}", adminDemo.getEmail());
        }

    private SolicitudRegistro nuevaSolicitudDemo(String nombre,
            String email,
            Rol rolSolicitado,
            EstadoSolicitud estado,
            LocalDateTime fechaSolicitud,
            LocalDateTime fechaResolucion,
            Usuario resueltoPor,
            String motivoRechazo) {
        SolicitudRegistro solicitud = new SolicitudRegistro();
        solicitud.setNombre(nombre);
        solicitud.setEmail(email);
        solicitud.setRolSolicitado(rolSolicitado);
        solicitud.setEstado(estado);
        solicitud.setFechaSolicitud(fechaSolicitud);
        solicitud.setFechaResolucion(fechaResolucion);
        solicitud.setResueltoPor(resueltoPor);
        solicitud.setMotivoRechazo(motivoRechazo);
        return solicitud;
    }

        private Notificacion nuevaNotificacionDemo(Usuario destinatario,
                        String titulo,
                        String mensaje,
                        String enlace,
                        LocalDateTime fecha) {
                Notificacion notificacion = new Notificacion(destinatario, titulo, mensaje, enlace);
                notificacion.setFecha(fecha);
                notificacion.setLeida(false);
                return notificacion;
        }

    private void programar(Traslado t, LocalDateTime inicio, LocalDateTime fin) {
        t.setFechaProgramadaInicio(inicio);
        t.setFechaProgramadaFin(fin);
        trasladoRepository.save(t);
    }

    private Direccion crearDireccion(String nombre, String calle, String ciudad,
            String cp, String provincia, double lat, double lon) {
        Direccion d = new Direccion(calle, ciudad, cp, provincia, "Espana");
        d.setNombre(nombre);
        d.setLatitud(lat);
        d.setLongitud(lon);
        return direccionRepository.save(d);
    }

}