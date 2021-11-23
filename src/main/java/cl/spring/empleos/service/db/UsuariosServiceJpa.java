package cl.spring.empleos.service.db;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cl.spring.empleos.model.Usuario;
import cl.spring.empleos.repository.UsuariosRepository;
import cl.spring.empleos.service.IUsuariosService;

@Service
public class UsuariosServiceJpa implements IUsuariosService {
	
	@Autowired
	private UsuariosRepository usuariosRepo;

	@Override
	public void guardar(Usuario usuario) {
		
		usuariosRepo.save(usuario);

	}

	@Override
	public void eliminar(Integer idUsuario) {
		
		usuariosRepo.deleteById(idUsuario);

	}

	@Override
	public List<Usuario> buscarTodos() {
		
		return usuariosRepo.findAll();
		
	}

	@Override
	public Usuario buscarPorUsername(String username) {
		
		return usuariosRepo.findByUsername(username);
	}

}
