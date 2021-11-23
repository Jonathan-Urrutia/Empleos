package cl.spring.empleos.security;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class DatabaseWebSecurity extends WebSecurityConfigurerAdapter {
	
	@Autowired
	private DataSource dataSource;
	
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.jdbcAuthentication().dataSource(dataSource)
		.usersByUsernameQuery("select username, password, estatus from usuarios where username = ?")
		.authoritiesByUsernameQuery("select u.username, p.perfil from usuarioPerfil up " +
				"inner join usuarios u on u.id = up.idusuario " +
				"inner join perfiles p on p.id = up.idperfil " +
				"where u.username = ?");
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
		
		//Los recursos est�ticos no requieren autenticaci�n
		.antMatchers(
			"/bootstrap/**",
			"/images/**",
			"/tinymce/**",
			"/logos/**").permitAll()
		
		//Las vistas p�blicas no requieren autenticaci�n
		.antMatchers("/",
			"/signup",
			"/search",
			"/bcrypt/**",
			"/vacantes/view/**",
			"/solicitudes/create/**").permitAll()
		
		//Asignar permisos a URLs por ROLES 
		.antMatchers("/vacantes/**").hasAnyAuthority("SUPERVISOR", "ADMINISTRADOR")
		.antMatchers("/categorias/**").hasAnyAuthority("SUPERVISOR", "ADMINISTRADOR")
		.antMatchers("/usuarios/**").hasAnyAuthority("ADMINISTRADOR")
		.antMatchers("/solicitudes/**").hasAnyAuthority("SUPERVISOR", "ADMINISTRADOR")
		
		//Todas las dem�s URLs de la aplicaci�n requieren autenticaci�n
		.anyRequest().authenticated()
		
		//El formulario de login no requiere autenticaci�n
		.and().formLogin().loginPage("/login").permitAll();
		
	}
	
	//SpringBoot asumir� que todas las contrase�as vendr�n encriptadas al momento de detectar un bean PasswordEncoder
	@Bean
	public PasswordEncoder passwordEncoder() {
		
		return new BCryptPasswordEncoder();
		
	}
	
}


