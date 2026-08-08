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
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, CustomUserDetailsService userDetailsService,
                           JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint, JwtAccessDeniedHandler jwtAccessDeniedHandler) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
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
                // 401 otan DEN yparxei/den einai egkyro to token, 403 otan YPARXEI
                // syndesi alla o rolos den ftanei gia ti sygkekrimeni energeia
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler))
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

                        // O KATHE syndedemenos (kai o texnikos) mporei na allaxei ton DIKO TOU kodiko.
                        // PROSOXI: prepei na einai PANO apo to "/api/users/**" pou akolouthei,
                        // giati to Spring Security efarmozei ton PROTO kanona pou tairiazei.
                        .requestMatchers("/api/users/me/**").authenticated()

                        // Diaxeirisi xriston -> SUPERVISOR i MANAGER (to POIOUS rolous mporei
                        // na dimiourgisei o kathenas elenxetai pio analytika mesa sto UserService)
                        .requestMatchers("/api/users/**").hasAnyRole("SUPERVISOR", "MANAGER")

                        // Mazikí eisagogí vlavón apo Excel -> SUPERVISOR i MANAGER.
                        // PROSOXI: prepei na einai PANO apo to .anyRequest(), alliws den tha efarmostei.
                        .requestMatchers("/api/faults/import/**").hasAnyRole("SUPERVISOR", "MANAGER")

                        // Diagrafi vlavis -> MONO MANAGER. Einai i pio "epikindini" energeia
                        // stis vlaves: xanetai kai to istoriko sintirisis mazi tis.
                        .requestMatchers(HttpMethod.DELETE, "/api/faults/*").hasRole("MANAGER")

                        // Ola ta alla (GET machines, faults, actions...) -> aploustata na eisai syndedemenos
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
