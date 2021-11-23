package cl.spring.empleos.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.spring.empleos.model.Categoria;

//public interface CategoriasRepository extends CrudRepository<Categoria, Integer>
public interface CategoriasRepository extends JpaRepository<Categoria, Integer> {
	

}
