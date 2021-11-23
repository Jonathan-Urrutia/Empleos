package cl.spring.empleos.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.spring.empleos.model.Perfil;



public interface PerfilesRepository extends JpaRepository<Perfil, Integer> {

}
