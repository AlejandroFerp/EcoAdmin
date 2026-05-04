package com.alejandrofernandez.ecoadmin.servicios;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.alejandrofernandez.ecoadmin.modelo.Usuario;
import com.alejandrofernandez.ecoadmin.repository.UsuarioRepository;

@Service
public class UsuarioServiceDB implements UsuarioService {

    private final UsuarioRepository repo;
    private final PasswordEncoder encoder;

    public UsuarioServiceDB(UsuarioRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    @Override
    public List<Usuario> findAll() { return repo.findAll(); }

    @Override
    public Usuario findById(Long id) { return repo.findById(id).orElse(null); }

    @Override
    public Usuario save(Usuario u) {
        CodigoInmutableSupport.conservarSiAusente(u.getId(), u.getCodigo(), repo::findById, Usuario::getCodigo, u::setCodigo);
        // Solo cifrar si viene una password en claro (sin prefijo BCrypt).
        // Evita re-cifrar el hash existente al editar un usuario.
        String pwd = u.getPassword();
        if (pwd != null && !pwd.isBlank() && !isBcrypt(pwd)) {
            u.setPassword(encoder.encode(pwd));
        }
        return repo.save(u);
    }

    private static boolean isBcrypt(String s) {
        return s.length() >= 4
                && s.charAt(0) == '$'
                && s.charAt(1) == '2'
                && (s.charAt(2) == 'a' || s.charAt(2) == 'b' || s.charAt(2) == 'y')
                && s.charAt(3) == '$';
    }

    @Override
    public void delete(Long id) { repo.deleteById(id); }
}