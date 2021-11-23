package cl.spring.empleos.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import cl.spring.empleos.model.Solicitud;
import cl.spring.empleos.service.ISolicitudesService;
import cl.spring.empleos.service.IUsuariosService;
import cl.spring.empleos.service.IVacantesService;
import cl.spring.empleos.util.Utileria;

@Controller
@RequestMapping("/solicitudes")
public class SolicitudesController {
	
	@Autowired
	IVacantesService serviceVacantes;
	
	@Autowired
	ISolicitudesService serviceSolicitudes;
	
	@Autowired
	IUsuariosService serviceUsuarios;
	
	/**
	 * EJERCICIO: Declarar esta propiedad en el archivo application.properties. El valor sera el directorio
	 * en donde se guardarán los archivos de los Curriculums Vitaes de los usuarios.
	 */
	@Value("${empleosapp.ruta.cv}")
	private String ruta;
		
    /**
	 * Metodo que muestra la lista de solicitudes sin paginacion
	 * Seguridad: Solo disponible para un usuarios con perfil ADMINISTRADOR/SUPERVISOR.
	 * @return
	 */
    @GetMapping("/index") 
	public String mostrarIndex(Model model) {
   
    	List<Solicitud> lista = serviceSolicitudes.buscarTodas();
    	model.addAttribute("solicitudes", lista);
    	// EJERCICIO
		return "solicitudes/listSolicitudes";
		
	}
    
    /**
	 * Metodo que muestra la lista de solicitudes con paginacion
	 * Seguridad: Solo disponible para usuarios con perfil ADMINISTRADOR/SUPERVISOR.
	 * @return
	 */
	@GetMapping("/indexPaginate")
	public String mostrarIndexPaginado(Model model, Pageable page) {
		
		Page<Solicitud> lista = serviceSolicitudes.buscarTodas(page);
		model.addAttribute("solicitudes", lista);
		
		
		return "solicitudes/listSolicitudes";
		
	}
    
	/**
	 * Método para renderizar el formulario para aplicar para una Vacante
	 * Seguridad: Solo disponible para un usuario con perfil USUARIO.
	 * @return
	 */
	@GetMapping("/create/{idVacante}")
	public String crear(@PathVariable("idVacante") int idVacante, Model model, Solicitud solicitud) {

		model.addAttribute("vacante", serviceVacantes.buscarPorId(idVacante));
		// EJERCICIO
		return "solicitudes/formSolicitud";
		
	}
	
	/**
	 * Método que guarda la solicitud enviada por el usuario en la base de datos
	 * Seguridad: Solo disponible para un usuario con perfil USUARIO.
	 * @return
	 */
	@PostMapping("/save")
	public String guardar(Solicitud solicitud, BindingResult result, RedirectAttributes attributes, @RequestParam("archivoCV") MultipartFile multiPart) {
		
		if(result.hasErrors()) {
			
			for(ObjectError error : result.getAllErrors()) {
				
				System.out.println("Ocurrio un error: " + error.getDefaultMessage());
				
			}
			
			return "solicitudes/formSolicitud";
			
		}
		
		if (!multiPart.isEmpty()) {
			 
			String nombreArchivo = Utileria.guardarArchivo(multiPart, ruta);
			
			if (nombreArchivo != null){ 
				
			solicitud.setArchivo(nombreArchivo); 
			
			}
		}
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		solicitud.setUsuario(serviceUsuarios.buscarPorUsername(auth.getName()));
		solicitud.setFecha(new Date());
		serviceSolicitudes.guardar(solicitud);
		attributes.addFlashAttribute("msg", "Solicitud Guardada");
		System.out.println("Solicitud: " + solicitud);
		
		// EJERCICIO
		return "redirect:/solicitudes/indexPaginate";	
		
	}
	
	/**
	 * Método para eliminar una solicitud
	 * Seguridad: Solo disponible para usuarios con perfil ADMINISTRADOR/SUPERVISOR. 
	 * @return
	 */
	@GetMapping("/delete/{id}")
	public String eliminar(@PathVariable("id") int idSolicitud) {
		
		serviceSolicitudes.eliminar(idSolicitud);
		
		// EJERCICIO
		return "redirect:/solicitudes/indexPaginate";
		
	}
	
	@GetMapping("/edit/{id}")
	public String editar(@PathVariable("id") int idSolicitud, Model model) {
		
		Solicitud solicitud = serviceSolicitudes.buscarPorId(idSolicitud);
		model.addAttribute("solicitud", solicitud);
		model.addAttribute("vacante", solicitud.getVacante());
		
		// EJERCICIO
		return "solicitudes/formSolicitud";
		
	}
			
	/**
	 * Personalizamos el Data Binding para todas las propiedades de tipo Date
	 * @param webDataBinder
	 */
	@InitBinder
	public void initBinder(WebDataBinder webDataBinder) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
		webDataBinder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
	}
	
}
