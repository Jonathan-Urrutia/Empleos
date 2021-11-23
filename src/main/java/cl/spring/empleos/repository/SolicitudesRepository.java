package cl.spring.empleos.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.spring.empleos.model.Solicitud;

public interface SolicitudesRepository extends JpaRepository<Solicitud, Integer> {

}
