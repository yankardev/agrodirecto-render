package com.agrodirecto.auth.config;

import com.agrodirecto.auth.entity.Distrito;
import com.agrodirecto.auth.entity.Rol;
import com.agrodirecto.auth.entity.Usuario;
import com.agrodirecto.auth.repository.DistritoRepository;
import com.agrodirecto.auth.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/** Datos de prueba. Solo se cargan si las tablas están vacías. El orden fija los IDs (admin=1, agricultores=2..4, cliente=5). */
@Component
public class DatosIniciales implements CommandLineRunner {

    private final UsuarioRepository usuarios;
    private final DistritoRepository distritos;
    private final PasswordEncoder encoder;

    public DatosIniciales(UsuarioRepository usuarios, DistritoRepository distritos, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.distritos = distritos;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (distritos.count() == 0) {
            List.of("Barranco", "Ate", "Chorrillos", "Comas", "La Molina", "Lima Cercado", "Miraflores",
                    "San Borja", "San Isidro", "San Juan de Lurigancho", "Santiago de Surco", "Villa El Salvador")
                    .forEach(n -> distritos.save(new Distrito(n)));
        }
        if (usuarios.count() == 0) {
            crear("Administrador", "admin@agrodirecto.com", "Admin123", Rol.ADMINISTRADOR, "999000111", "Av. Arequipa 1200", "Lima Cercado");
            crear("Juan Quispe", "agricultor1@agrodirecto.com", "Agro123", Rol.AGRICULTOR, "987111222", "Fundo Los Andes, Huaral", "Comas");
            crear("María Condori", "agricultor2@agrodirecto.com", "Agro123", Rol.AGRICULTOR, "987333444", "Chacra Santa Rosa, Cañete", "Villa El Salvador");
            crear("Pedro Huamán", "agricultor3@agrodirecto.com", "Agro123", Rol.AGRICULTOR, "987555666", "Valle de Lurín", "Ate");
            crear("Lucía Torres", "cliente1@agrodirecto.com", "Agro123", Rol.CLIENTE, "955123456", "Calle Los Pinos 245", "Miraflores");
        }
    }

    private void crear(String nombre, String email, String clave, Rol rol, String tel, String dir, String distrito) {
        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setEmail(email);
        u.setPasswordHash(encoder.encode(clave));
        u.setRol(rol);
        u.setTelefono(tel);
        u.setDireccion(dir);
        u.setDistrito(distrito);
        u.setAprobado(true);
        usuarios.save(u);
    }
}
