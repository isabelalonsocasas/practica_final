package edu.comillas.icai.gitt.pat.spring.practica_final;

import edu.comillas.icai.gitt.pat.spring.practica_final.RECORDS.Usuario;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;


@Configuration @EnableMethodSecurity
public class ConfiguracionSeguridad {

    //AÑADIMOS EL ALMACEN DE DATOS
    private final AlmacenDatos almacen;

    public ConfiguracionSeguridad(AlmacenDatos almacen) {
        this.almacen = almacen;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.csrf(csrf -> csrf.disable()); //Desactivar protección CSRF (Utilizamos PostMan no HTML)

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/pistaPadel/auth/register").permitAll() //No es necesario para el registro
                .requestMatchers("/pistaPadel/auth/login").permitAll() //Se permite para loggearte
                .anyRequest().authenticated() );//Las demás rutas exigen login
        //LOGGIN
        http.formLogin(form -> form
                .loginProcessingUrl("/pistaPadel/auth/login") //Definimos la URL
                .successHandler((req, res, auth) -> res.setStatus(HttpStatus.OK.value()))
                .failureHandler((req, res, ex) -> res.setStatus(HttpStatus.UNAUTHORIZED.value()))
        );

        // LOGOUT
        http.logout(logout -> logout
                .logoutUrl("/pistaPadel/auth/logout") //Definimos la URL
                .logoutSuccessHandler((req, res, auth) -> res.setStatus(HttpStatus.NO_CONTENT.value()))
        );

        return http.build();
        }
}

