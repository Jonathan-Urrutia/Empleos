package cl.spring.empleos.controller;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import cl.spring.empleos.model.Perfil;
import cl.spring.empleos.model.Usuario;
import cl.spring.empleos.model.Vacante;
import cl.spring.empleos.service.ICategoriasService;
import cl.spring.empleos.service.IUsuariosService;
import cl.spring.empleos.service.IVacantesService;

@Controller
public class HomeController {
	
	@Autowired
	private ICategoriasService serviceCategorias;
	
	@Autowired
	private IVacantesService serviceVacantes;
	
	@Autowired
	private IUsuariosService serviceUsuarios;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@GetMapping("/")
	public String mostrarHome(Model model) {
			
		return "home";
	}
	
	//La interfaz Authentication nos permitirá rescatar datos del inicio de sesión.
	@GetMapping("/index")
	public String mostrarIndex(Authentication auth, HttpSession session) {
		
		String username = auth.getName();
		System.out.println("Nombre del usuario: " + username);
		
		for (GrantedAuthority rol : auth.getAuthorities()) {
			
			System.out.println("ROL: " + rol.getAuthority());
			
		}
		
		if(session.getAttribute("usuario") == null) {
			
			Usuario usuario  = serviceUsuarios.buscarPorUsername(username);
			usuario.setPassword(null); //Por seguridad setiamos la password como null.
			System.out.println("Usuario: " + usuario);
			session.setAttribute("usuario", usuario); //Agregamos el usuario a la sesión.
		
		}
		

		
		
		return "redirect:/";
		
	}
	
	@GetMapping("/signup")
	public String registrarse(Usuario usuario, Model model) {
		
		return "formRegistro";
	}
	
	@PostMapping("/signup")
	public String guardarRegistro(Usuario usuario, BindingResult result,  RedirectAttributes attributes) {

		if(result.hasErrors()) {
			
			for(ObjectError error : result.getAllErrors()) {
				
				System.out.println("Ocurrio un error: " + error.getDefaultMessage());
				
			}
			
			return "redirect:/formRegistro";
			
		}
		
		String pwdPlano = usuario.getPassword();
		String pwdEncriptado = passwordEncoder.encode(pwdPlano);
		usuario.setPassword(pwdEncriptado);
		
		usuario.setEstatus(1); 
		usuario.setFechaRegistro(new Date()); 
		Perfil perfil = new Perfil();
		perfil.setId(3);
		
		usuario.agregar(perfil);
		serviceUsuarios.guardar(usuario);
		attributes.addFlashAttribute("msg", "Usuario registrado");
		return "redirect:/usuarios/index";
	}
	
	@GetMapping("/tabla")
	public String mostrarTabla(Model model) {
		
		List<Vacante> lista = serviceVacantes.buscarTodas();
		model.addAttribute("vacantes", lista);
		
		return "tabla";
		
	}
	
	@GetMapping("/detalle")
	public String mostrarDetalle(Model model) {
		
		Vacante vacante = new Vacante();
		
		vacante.setNombre("Ingeniero de comunicaciones");
		vacante.setDescripcion("Se necesita ingeniero para dar soporte a intranet");
		vacante.setFecha(new Date());
		vacante.setSalario(9700.0);
		
		model.addAttribute("vacante", vacante);
		
		return "detalle";
		
	}
	
	@GetMapping("/listado")
	public String mostrarListado(Model model) {
		
		List<String> lista = new LinkedList<String>();
		lista.add("Ingeniero de Sistemas");
		lista.add("Auxiliar de Contabilidad");
		lista.add("Vendedor");
		lista.add("Arquitecto");
		
		model.addAttribute("empleos", lista);
		
		return "listado";
		
		
	}
	
	//@ModelAttribute sirve para asignar un objeto del modelo al parametro Vacante durante el databinding 
	//Example es una clase que permite realizar una consulta sql a traves de los atributos de la clase otorgada (en este caso Vacante), ignorando aquellos datos que sean nulos. 
	@GetMapping("/search")
	public String buscar(@ModelAttribute("search") Vacante vacante, Model model) {
		
		System.out.println("Buscan do por: " + vacante);
		ExampleMatcher matcher = ExampleMatcher.
				//where descripcion like '%?%'
				matching().withMatcher("descripcion", ExampleMatcher.GenericPropertyMatchers.contains()); 
		Example<Vacante> example = Example.of(vacante, matcher);
		List<Vacante> lista = serviceVacantes.buscarByExample(example);
		model.addAttribute("vacantes", lista);
		return "home";
		
	}
	
	@GetMapping("/login")
	public String mostrarLogin() {
		return "formLogin";
		
	}
	
	@GetMapping("/logout")
	public String logout(HttpServletRequest request) {
		
		SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
		logoutHandler.logout(request, null, null);
		return "redirect:/login";
		
	}
	
	@GetMapping("/bcrypt/{texto}")
	@ResponseBody //Esta anotación permitirá que el valor devuelto se muestre en el navegador. 
	public String encriptar(@PathVariable("texto") String texto) {
		return texto +  " Encriptado en Bcryp: " + passwordEncoder.encode(texto);
		
	}
	
	/**
	 * InitBinder para Strings si los detecta vacios en el Data Binding los settea a NULL.
	 * @param binder
	 */
	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
	}
	
	@ModelAttribute
	public void setGenericos(Model model) {
		Vacante vacanteSearch = new Vacante();
		vacanteSearch.reset();
		model.addAttribute("vacantes", serviceVacantes.buscarDestacadas());
		model.addAttribute("categorias", serviceCategorias.buscarTodas());
		model.addAttribute("search", vacanteSearch);

	}
			
}
	
	


