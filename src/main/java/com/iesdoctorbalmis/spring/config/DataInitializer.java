package com.iesdoctorbalmis.spring.config;

import com.iesdoctorbalmis.spring.modelo.Usuario;
import com.iesdoctorbalmis.spring.modelo.enums.Rol;
import com.iesdoctorbalmis.spring.repository.UsuarioRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${ecoadmin.admin.email:admin@ecoadmin.com}")
    private String adminEmail;

    @Value("${ecoadmin.admin.password:#{null}}")
    private String adminPassword;

    public DataInitializer(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (usuarioRepository.count() == 0) {
            if (adminPassword == null || adminPassword.isBlank()) {
                log.warn("No se ha configurado ecoadmin.admin.password. No se creara usuario admin por defecto.");
                return;
            }
            Usuario admin = new Usuario(
                    "Administrador",
                    adminEmail,
                    passwordEncoder.encode(adminPassword),
                    Rol.ADMIN
            );
            usuarioRepository.save(admin);
            log.info("Usuario admin creado con email: {}", adminEmail);
        }
    }
}
