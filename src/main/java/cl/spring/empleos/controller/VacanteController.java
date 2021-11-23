package cl.spring.empleos.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import cl.spring.empleos.model.Vacante;
import cl.spring.empleos.service.ICategoriasService;
import cl.spring.empleos.service.IVacantesService;
import cl.spring.empleos.util.Utileria;

@Controller
@RequestMapping("/vacantes")
public class VacanteController {
	
	@Value("${empleosapp.ruta.imagenes}") //Se inyecta el valor asociado a la propiedad declarada en application.properties
	private String ruta;
	
	@Autowired
	private IVacantesService serviceVacantes;
	
	@Autowired
	//@Qualifier("categoriasServiceJpa") //Otorga prioridad a la implementaci贸n se帽alada. Se escribe colocando la primera letra en min煤scula.
	private ICategoriasService serviceCategorias;
	
	@GetMapping("/index")
	public String mostrarIndex(Model model) {
		
		List<Vacante> lista = serviceVacantes.buscarTodas();
		model.addAttribute("vacantes", lista);
		
		return "vacantes/listVacantes";
		
	}
	
	@GetMapping(value = "/indexPaginate")
	public String mostrarIndexPaginado(Model model, Pageable page) {
		
		Page<Vacante> lista = serviceVacantes.buscarTodas(page);
		model.addAttribute("vacantes", lista);
		
		return "vacantes/listVacantes";
		
	}

	
	@GetMapping("/create")
	public String crear(Vacante vacante, Model model) {
		
		return "vacantes/formVacante";
		
	}

	//El proceso de databinding de Spring mapea automaticamente los atributos de la clase Vacante con los datos recogidos del formulario.
	//Para manejar los errores se utiliza la clase BindingResult, la cual debe agregarse como parametro inmediatamente despu茅s del par谩metro que referencia el modelo.
	@PostMapping("/save")
	public String guardar(Vacante vacante, BindingResult result,  RedirectAttributes attributes,  @RequestParam("archivoImagen") MultipartFile multiPart) {
		
		if(result.hasErrors()) {
			
			for(ObjectError error : result.getAllErrors()) {
				
				System.out.println("Ocurrio un error: " + error.getDefaultMessage());
				
			}
			
			return "vacantes/formVacante";
			
		}
		
		if (!multiPart.isEmpty()) {
			
			//String ruta = "c:/empleos/img-vacantes/"; 
			String nombreImagen = Utileria.guardarArchivo(multiPart, ruta);
			
			if (nombreImagen != null){ // La imagen si se subio
				
			// Procesamos la variable nombreImagen
			vacante.setImagen(nombreImagen); 
			
			}
		}

		
		//Un atributo flash se almacena para ser utilizada en m醩 de una petici髇 (POST/REDIRECT/GET). Una vez accedido el atributo, este se elimina autom醫icamente.
		serviceVacantes.guardar(vacante);
		attributes.addFlashAttribute("msg", "Registro Guardado");
		System.out.println("Vacante: " + vacante);
		
		//Con el literal "redirect" puedes redirigir a la vista se馻lada.
		return "redirect:/vacantes/index"; 
		
	}
	
	/* 
	@PostMapping("/save")
	public String guardar(@RequestParam("nombre") String nombre, @RequestParam("descripcion") String descripcion,
			@RequestParam("estatus") String estatus, @RequestParam("fecha") String fecha, @RequestParam("destacado") int destacado,
			@RequestParam("salario") double salario, @RequestParam("detalles") String detalles) {
		
		System.out.println("Nombre Vacante: " + nombre);
		System.out.println("Descripci贸n " + descripcion);
		System.out.println("Estatus: " + estatus);
		System.out.println("Fecha Publicaci贸n: " + fecha);
		System.out.println("Destacado: " + destacado);
		System.out.println("Salario Ofrecido: " + salario);
		System.out.println("detalles: " + detalles);
	
		return "vacantes/listVacantes";
		
	}*/
	
	@GetMapping("/delete/{id}")
	public String eliminar(@PathVariable("id") int idVacante, Model model, RedirectAttributes attributes) {
		
		System.out.println("Borrando vacante con id: " + idVacante);
		serviceVacantes.eliminar(idVacante);
		attributes.addFlashAttribute("msg", "La vacante fue eliminada");
		
		return "redirect:/vacantes/index";
		
	}
	
	@GetMapping("/edit/{id}")
	public String editar(@PathVariable("id") int idVacante, Model model) {
		
		Vacante vacante = serviceVacantes.buscarPorId(idVacante);
		model.addAttribute("vacante", vacante);
		
		
		return "vacantes/formVacante";
		
	}
	
	@GetMapping("/view/{id}")
	public String verDetalle(@PathVariable("id") int idVacante, Model model) {
		
		Vacante vacante = serviceVacantes.buscarPorId(idVacante);
		System.out.println("Vacante: " + vacante);
		model.addAttribute("vacante", vacante);
		
		//Buscar los detalles de la vacante en Id BD...  
		
		return "detalle";
		
	}
	
	@ModelAttribute
	public void setGenericos(Model model) {
		
		model.addAttribute("categorias", serviceCategorias.buscarTodas());
		
	}
	
	/*@InitBinder permite generar un m茅todo que personaliza la conversi贸n de datos en el mapeo del DataBinder.
	 * El m茅todo debe estar en la clase controladora donde se realiza el mapeo, y debe tener como argumento un objeto WebDataBinder.
	 */
	@InitBinder
	public void initBinder(WebDataBinder webDataBinder) {
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
		webDataBinder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, false));
		
	}

}
