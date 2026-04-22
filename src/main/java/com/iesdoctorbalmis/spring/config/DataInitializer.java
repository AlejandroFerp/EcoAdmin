package com.iesdoctorbalmis.spring.config;

import java.io.InputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iesdoctorbalmis.spring.modelo.Centro;
import com.iesdoctorbalmis.spring.modelo.Direccion;
import com.iesdoctorbalmis.spring.modelo.ListaLer;
import com.iesdoctorbalmis.spring.modelo.Residuo;
import com.iesdoctorbalmis.spring.modelo.Traslado;
import com.iesdoctorbalmis.spring.modelo.Usuario;
import com.iesdoctorbalmis.spring.modelo.enums.EstadoTraslado;
import com.iesdoctorbalmis.spring.modelo.enums.Rol;
import com.iesdoctorbalmis.spring.repository.CentroRepository;
import com.iesdoctorbalmis.spring.repository.DireccionRepository;
import com.iesdoctorbalmis.spring.repository.ListaLerRepository;
import com.iesdoctorbalmis.spring.repository.ResiduoRepository;
import com.iesdoctorbalmis.spring.repository.TrasladoRepository;
import com.iesdoctorbalmis.spring.repository.UsuarioRepository;
import com.iesdoctorbalmis.spring.servicios.TrasladoService;

@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UsuarioRepository usuarioRepo;
    private final DireccionRepository direccionRepo;
    private final CentroRepository centroRepo;
    private final ResiduoRepository residuoRepo;
    private final TrasladoRepository trasladoRepo;
    private final ListaLerRepository listaLerRepo;
    private final TrasladoService trasladoService;
    private final PasswordEncoder passwordEncoder;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Value("${ecoadmin.admin.email:admin@ecoadmin.com}")
    private String adminEmail;

    @Value("${ecoadmin.admin.password:#{null}}")
    private String adminPassword;

    @Value("${ecoadmin.seed.enabled:true}")
    private boolean seedEnabled;

    public DataInitializer(UsuarioRepository usuarioRepo, DireccionRepository direccionRepo,
            CentroRepository centroRepo, ResiduoRepository residuoRepo,
            TrasladoRepository trasladoRepo, ListaLerRepository listaLerRepo,
            TrasladoService trasladoService, PasswordEncoder passwordEncoder,
            org.springframework.jdbc.core.JdbcTemplate jdbcTemplate) {
        this.usuarioRepo = usuarioRepo;
        this.direccionRepo = direccionRepo;
        this.centroRepo = centroRepo;
        this.residuoRepo = residuoRepo;
        this.trasladoRepo = trasladoRepo;
        this.listaLerRepo = listaLerRepo;
        this.trasladoService = trasladoService;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!seedEnabled) return;
        cargarListaLer();
        seedDatos();
    }

    private void cargarListaLer() {
        if (listaLerRepo.count() > 0) return;
        try {
            InputStream is = new ClassPathResource("data/lista_ler.json").getInputStream();
            List<LerEntry> entries = new ObjectMapper().readValue(is, new TypeReference<>() {});
            List<ListaLer> entidades = entries.stream()
                .filter(e -> e.codigo() != null && !e.codigo().isBlank())
                .map(e -> new ListaLer(e.codigo().trim(), e.texto().trim()))
                .toList();
            listaLerRepo.saveAll(entidades);
            log.info("Cargados {} codigos LER", entidades.size());
        } catch (Exception e) {
            log.warn("No se pudo cargar lista_ler.json: {}", e.getMessage());
        }
    }

    private void seedDatos() {
        if (usuarioRepo.count() > 0) return;

        if (adminPassword == null || adminPassword.isBlank()) {
            log.warn("No se ha configurado ecoadmin.admin.password. No se creara usuario admin por defecto.");
            return;
        }

        // --- Usuarios ---
        Usuario admin = usuarioRepo.save(new Usuario("Administrador", adminEmail,
                passwordEncoder.encode(adminPassword), Rol.ADMIN));
        Usuario prod1 = usuarioRepo.save(new Usuario("EcoBat Levante S.L.",
                "ecobat@empresa.com", passwordEncoder.encode("ecobat123"), Rol.PRODUCTOR));
        Usuario prod2 = usuarioRepo.save(new Usuario("IndustriAli S.A.",
                "industria@empresa.com", passwordEncoder.encode("industria123"), Rol.PRODUCTOR));
        Usuario gestor = usuarioRepo.save(new Usuario("GestResid Valencia S.L.",
                "gestresid@empresa.com", passwordEncoder.encode("gestresid123"), Rol.GESTOR));
        Usuario trans = usuarioRepo.save(new Usuario("TransEco Mediterraneo S.L.",
                "transeco@empresa.com", passwordEncoder.encode("transeco123"), Rol.TRANSPORTISTA));
        log.info("Creados 5 usuarios de ejemplo");

        // --- Direcciones ---
        Direccion d1 = crearDireccion("Sede EcoBat", "Pol. Industrial Las Atalayas, Nave 12",
                "Alicante", "03114", "Alicante", 38.3452, -0.4815);
        Direccion d2 = crearDireccion("Planta IndustriAli", "Avda. de Elche 45",
                "Alicante", "03008", "Alicante", 38.3565, -0.4900);
        Direccion d3 = crearDireccion("Centro GestResid", "Ctra. Valencia km 3",
                "Paterna", "46980", "Valencia", 39.5028, -0.4410);
        Direccion d4 = crearDireccion("Almacen intermedio", "Pol. Ind. Torrellano, Parcela 8",
                "Elche", "03320", "Alicante", 38.2650, -0.5520);
        Direccion d5 = crearDireccion("Planta tratamiento Murcia", "Pol. Ind. Oeste, C/ Rio Segura 10",
                "Murcia", "30169", "Murcia", 37.9838, -1.1280);
        Direccion d6 = crearDireccion("Oficina TransEco", "C/ San Vicente 88, 3o",
                "Alicante", "03004", "Alicante", 38.3460, -0.4830);
        Direccion d7 = crearDireccion("Planta reciclaje norte", "Avda. Mediterraneo s/n",
                "Castellon", "12006", "Castellon", 39.9864, -0.0513);
        Direccion d8 = crearDireccion("Gestor Elche", "C/ de la Industria 22",
                "Elche", "03203", "Alicante", 38.2669, -0.6983);

        // --- Centros ---
        Centro c1 = crearCentro(prod1, "EcoBat Levante - Productor", "PRODUCTOR", d1,
                "NIMA/2023/001", "966112233", "contacto@ecobat.es", "Juan Garcia");
        Centro c2 = crearCentro(prod2, "IndustriAli - Productor", "PRODUCTOR", d2,
                "NIMA/2023/002", "965334455", "contacto@industriali.es", "Maria Lopez");
        Centro c3 = crearCentro(gestor, "GestResid Valencia - Gestor", "GESTOR", d3,
                "NIMA/2023/003", "961778899", "recepcion@gestresid.es", "Pedro Martinez");
        Centro c4 = crearCentro(prod1, "EcoBat Almacen Temporal", "PRODUCTOR", d4,
                "NIMA/2023/004", "966112234", "almacen@ecobat.es", "Ana Ruiz");
        Centro c5 = crearCentro(gestor, "Planta Tratamiento Murcia", "GESTOR", d5,
                "NIMA/2023/005", "968223344", "planta@gestresid.es", "Carlos Hernandez");
        Centro c6 = crearCentro(gestor, "Reciclaje Castellon", "GESTOR", d7,
                "NIMA/2023/006", "964556677", "castellon@gestresid.es", "Laura Sanchez");
        log.info("Creados 6 centros de ejemplo");

        // --- Residuos ---
        Residuo r1 = crearResiduo(500, "kg", "PENDIENTE", c1, "16 06 01*",
                "Baterias de plomo (acumuladores usados)");
        Residuo r2 = crearResiduo(200, "kg", "PENDIENTE", c1, "16 06 02*",
                "Baterias de niquel-cadmio");
        Residuo r3 = crearResiduo(150, "kg", "PENDIENTE", c2, "16 06 05",
                "Otras baterias y acumuladores (litio-ion)");
        Residuo r4 = crearResiduo(80, "litros", "PENDIENTE", c2, "13 02 05*",
                "Aceites minerales no clorados de motor");
        Residuo r5 = crearResiduo(300, "kg", "PENDIENTE", c1, "20 01 33*",
                "Baterias especificadas en 16 06 01, 16 06 02");
        Residuo r6 = crearResiduo(1200, "kg", "PENDIENTE", c4, "16 06 01*",
                "Baterias de plomo (lote grande)");
        Residuo r7 = crearResiduo(50, "kg", "PENDIENTE", c2, "16 06 03*",
                "Pilas que contienen mercurio");

        // --- Traslados ---
        Traslado t1 = trasladoRepo.save(new Traslado(c1, c3, r1, trans));
        Traslado t2 = trasladoRepo.save(new Traslado(c1, c5, r2, trans));
        Traslado t3 = trasladoRepo.save(new Traslado(c2, c3, r3, trans));
        Traslado t4 = trasladoRepo.save(new Traslado(c2, c5, r4, trans));
        Traslado t5 = trasladoRepo.save(new Traslado(c1, c6, r5, trans));
        Traslado t6 = trasladoRepo.save(new Traslado(c4, c3, r6, trans));
        Traslado t7 = trasladoRepo.save(new Traslado(c2, c6, r7, null));

        // Fechas programadas (para vista calendario)
        java.time.LocalDateTime hoy = java.time.LocalDateTime.now()
                .withHour(8).withMinute(0).withSecond(0).withNano(0);
        programar(t1, hoy.plusDays(2),  hoy.plusDays(2).withHour(14));
        programar(t2, hoy.minusDays(1), hoy.minusDays(1).withHour(18));
        programar(t3, hoy.minusDays(5), hoy.minusDays(5).withHour(17));
        programar(t4, hoy.minusDays(8), hoy.minusDays(7).withHour(12));
        programar(t5, hoy.minusDays(3), hoy.minusDays(2).withHour(16));
        programar(t6, hoy.plusDays(4),  hoy.plusDays(5).withHour(13));
        programar(t7, hoy.plusDays(7),  hoy.plusDays(7).withHour(15));

        // Avanzar algunos traslados por la maquina de estados
        trasladoService.cambiarEstado(t2.getId(), EstadoTraslado.EN_TRANSITO, "Recogida realizada", trans);
        trasladoService.cambiarEstado(t3.getId(), EstadoTraslado.EN_TRANSITO, "Sale de Alicante", trans);
        trasladoService.cambiarEstado(t3.getId(), EstadoTraslado.ENTREGADO, "Recibido en GestResid", trans);
        trasladoService.cambiarEstado(t4.getId(), EstadoTraslado.EN_TRANSITO, "Inicio transporte aceites", trans);
        trasladoService.cambiarEstado(t4.getId(), EstadoTraslado.ENTREGADO, "Entregado en Murcia", trans);
        trasladoService.cambiarEstado(t4.getId(), EstadoTraslado.COMPLETADO, "Tratamiento finalizado", trans);
        trasladoService.cambiarEstado(t5.getId(), EstadoTraslado.EN_TRANSITO, "Rumbo a Castellon", trans);
        trasladoService.cambiarEstado(t5.getId(), EstadoTraslado.ENTREGADO, "Recibido", trans);
        trasladoService.cambiarEstado(t5.getId(), EstadoTraslado.COMPLETADO, "Procesado y certificado", trans);

        // Esparcir las fechas de creacion en el pasado para que los filtros de graficas (Hoy, 7 dias, 30 dias) funcionen
        log.info("Ajustando fechas de creacion para simular historico (filtros de graficas)...");
        try {
            int[] diasAtras = { 0, 1, 3, 5, 10, 15, 25 };
            List<Traslado> todos = trasladoRepo.findAll();
            for (int i = 0; i < todos.size(); i++) {
                int restar = diasAtras[i % diasAtras.length];
                jdbcTemplate.update("UPDATE traslados SET fecha_creacion = DATEADD(DAY, ?, CURRENT_TIMESTAMP) WHERE id = ?", -restar, todos.get(i).getId());
            }
        } catch (Exception e) {
            log.warn("No se pudieron ajustar las fechas historicas: {}", e.getMessage());
        }

        log.info("Creados 7 residuos, 7 traslados con historial y fechas programadas");
    }

    private void programar(Traslado t, java.time.LocalDateTime inicio, java.time.LocalDateTime fin) {
        t.setFechaProgramadaInicio(inicio);
        t.setFechaProgramadaFin(fin);
        trasladoRepo.save(t);
    }

    private Direccion crearDireccion(String nombre, String calle, String ciudad,
            String cp, String provincia, double lat, double lon) {
        Direccion d = new Direccion(calle, ciudad, cp, provincia, "Espana");
        d.setNombre(nombre);
        d.setLatitud(lat);
        d.setLongitud(lon);
        return direccionRepo.save(d);
    }

    private Centro crearCentro(Usuario usuario, String nombre, String tipo, Direccion dir,
            String nima, String telefono, String email, String contacto) {
        Centro c = new Centro(usuario, nombre, tipo, dir);
        c.setNima(nima);
        c.setTelefono(telefono);
        c.setEmail(email);
        c.setNombreContacto(contacto);
        return centroRepo.save(c);
    }

    private Residuo crearResiduo(double cantidad, String unidad, String estado, Centro centro,
            String codigoLER, String descripcion) {
        Residuo r = new Residuo(cantidad, unidad, estado, centro);
        r.setCodigoLER(codigoLER);
        r.setDescripcion(descripcion);
        return residuoRepo.save(r);
    }

    private record LerEntry(String codigo, String texto) {}
}
