package com.codingfactory.maintrack.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

// Edo orizoume TOUS KANONES asfaleias tis efarmogis: poios mporei na kanei ti,
// xoris na xreiazetai login, kai poios xreiazetai sygkekrimeno rolo.
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, CustomUserDetailsService userDetailsService) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userDetailsService = userDetailsService;
    }

    // To BCrypt "anakateuei" ton kodiko me tetoio tropo pou den mporei na "diavastei" antistrofa.
    // Otan kaneis login, sygrinei ton kodiko pou edoses me to hash - den apothikevei pote to plain text.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    @SuppressWarnings("deprecation")
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // Epitrepoume sto React (pou tha trexei se allo port, p.x. 5173) na kalei to API mas.
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:3000"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // REST API me JWT -> den xreiazomaste CSRF protection (auto einai gia forms me cookies)
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // "STATELESS" = o server DEN thymatai poios eisai anamesa se requests.
                // Kathe request prepei na fernei to token tou - kalytero gia REST API.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Auta ta endpoints ta blepei O KATHENAS, xoris login
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/health").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // Dimiourgia/allagi mihanon -> SUPERVISOR i MANAGER
                        // (PROSOXI: prepei na dosoume HttpMethod.POST, oxi to string "POST" -
                        // alliws to Spring Security to katalavainei san allo URL pattern, oxi san periorismo methodou)
                        .requestMatchers(HttpMethod.POST, "/api/machines/**").hasAnyRole("SUPERVISOR", "MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/machines/**").hasAnyRole("SUPERVISOR", "MANAGER")
                        // Diagrafi mihanis -> MONO MANAGER (pio "epikindini" energeia, pio psilo dikaioma)
                        .requestMatchers(HttpMethod.DELETE, "/api/machines/**").hasRole("MANAGER")

                        // Diaxeirisi xriston -> SUPERVISOR i MANAGER (to POIOUS rolous mporei
                        // na dimiourgisei o kathenas elenxetai pio analytika mesa sto UserService)
                        .requestMatchers("/api/users/**").hasAnyRole("SUPERVISOR", "MANAGER")

                        // Ola ta alla (GET machines, faults, actions...) -> aploustata na eisai syndedemenos
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
