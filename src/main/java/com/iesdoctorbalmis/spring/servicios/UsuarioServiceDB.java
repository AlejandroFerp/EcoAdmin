package com.iesdoctorbalmis.spring.servicios;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.iesdoctorbalmis.spring.modelo.Usuario;
import com.iesdoctorbalmis.spring.repository.UsuarioRepository;

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
        if (u.getPassword() != null && !u.getPassword().isBlank()) {
            u.setPassword(encoder.encode(u.getPassword()));
        }
        return repo.save(u);
    }

    @Override
    public void delete(Long id) { repo.deleteById(id); }
}
