package com.iesdoctorbalmis.spring.servicios;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.iesdoctorbalmis.spring.modelo.Usuario;
import com.iesdoctorbalmis.spring.repository.UsuarioRepository;

@Service
public class UsuarioServiceDB implements UsuarioService {

    @Autowired
    private UsuarioRepository repo;

    @Autowired
    private PasswordEncoder encoder;

    public List<Usuario> findAll() { return repo.findAll(); }

    public Usuario findById(Long id) { return repo.findById(id).orElse(null); }

    public Usuario save(Usuario u) {
        if (u.getPassword() != null && !u.getPassword().startsWith("$2"))
            u.setPassword(encoder.encode(u.getPassword()));
        return repo.save(u);
    }

    public void delete(Long id) { repo.deleteById(id); }
}
