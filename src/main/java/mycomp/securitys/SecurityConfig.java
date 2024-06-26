package mycomp.securitys;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import mycomp.filters.AuthFilterToken;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

  @Autowired
  private UserDetailsServiceImpl userdtsrvcimpl;

  @Autowired
  private AuthFilterToken authfiltertoken;

  @Bean
  BCryptPasswordEncoder passEncoder() {

	 return new BCryptPasswordEncoder();

  }

  /* ::authenticacion:: */
  @Bean
  DaoAuthenticationProvider authenticationProvider() {

	 var daoAuthProvider = new DaoAuthenticationProvider();

	 daoAuthProvider.setUserDetailsService(userdtsrvcimpl);
	 daoAuthProvider.setPasswordEncoder(passEncoder());

	 return daoAuthProvider;
  }

  @Bean
  AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {

	 return config.getAuthenticationManager();
  }

  /* ::autorizacion:: */
  @Bean
  SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
	 http
	   .cors(cors -> cors.configurationSource(corsConfigurationSource())) // configuración CORS
		.csrf(csrf -> csrf.disable()) // deshabilitar CSRF
		.authorizeHttpRequests(authz -> authz
			 .requestMatchers("/api/user/login","/api/user/**").permitAll() // permitir acceso a rutas especificas 
			 .anyRequest().authenticated()) // cualquier otra solicitud requiere autenticacion
		.sessionManagement(sess -> sess
			 .sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // política de gestión de sesión sin estado
		.authenticationProvider(authenticationProvider()) // proveedor de autenticación personalizado
		.addFilterBefore(authfiltertoken, UsernamePasswordAuthenticationFilter.class); // filtro de autenticación JWT

	 return http.build();
  }
  

  @Bean
  CorsConfigurationSource corsConfigurationSource() {
	 
    var configuration = new CorsConfiguration();
    
    configuration.setAllowedOrigins(List.of("*")); // Permitir todos los orígenes
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("*"));
    
    var source = new UrlBasedCorsConfigurationSource();
    
    source.registerCorsConfiguration("/**", configuration);
    
    return source;
  }

}