package cl.spring.empleos.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.spring.empleos.model.Usuario;


public interface UsuariosRepository extends JpaRepository<Usuario, Integer> {
	
	Usuario findByUsername(String username);

}
