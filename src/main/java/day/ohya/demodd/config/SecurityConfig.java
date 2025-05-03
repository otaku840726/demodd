package day.ohya.demodd.config;

import day.ohya.demodd.constant.Role;
import day.ohya.demodd.security.JwtClaims;
import day.ohya.demodd.security.PasswordAuthenticationService;
import day.ohya.demodd.security.SkippingBearerTokenResolver;
import day.ohya.demodd.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@EnableMethodSecurity
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtService jwtService;

    private static final String[] PUBLIC_URLS = {
            "/",
            "/index.html",
            "/login",
            "/register",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/actuator/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtDecoder jwtDecoder,
                                                   Converter<Jwt, AbstractAuthenticationToken> customJwtAuthenticationConverter,
                                                   UserOperationLoggingFilter loggingFilter) throws Exception {
        return http
                .cors(Customizer.withDefaults())
                .csrf(csrf ->
                                csrf.disable()
//                        csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
//                        .ignoringRequestMatchers(
//                                "/swagger-ui/**",
//                                "/v3/api-docs/**",
//                                "/swagger-resources/**"
//                        )
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_URLS).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 ->
                        oauth2.bearerTokenResolver(new SkippingBearerTokenResolver(PUBLIC_URLS))
                                .jwt(jwt -> jwt
                                        .decoder(jwtDecoder)
                                        .jwtAuthenticationConverter(customJwtAuthenticationConverter))
                )
                .addFilterAfter(loggingFilter, SecurityContextHolderFilter.class)
                .build();
    }

    @Bean
    public JwtDecoder jwtDecoder() throws Exception {
        var pubKeyRes = new ClassPathResource("keys/public.pem");
        String key = new String(pubKeyRes.getInputStream().readAllBytes(), StandardCharsets.UTF_8)
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");
        byte[] keyBytes = Base64.getDecoder().decode(key);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        RSAPublicKey pub = (RSAPublicKey) kf.generatePublic(spec);
        return NimbusJwtDecoder.withPublicKey(pub).build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            PasswordEncoder passwordEncoder,
            PasswordAuthenticationService userDetailsService) {
        // 帳密授權
        var usernamePasswordProvider = new DaoAuthenticationProvider();
        usernamePasswordProvider.setUserDetailsService(userDetailsService);
        usernamePasswordProvider.setPasswordEncoder(passwordEncoder);

        return new ProviderManager(List.of(usernamePasswordProvider));
    }

    @Bean
    public Converter<Jwt, AbstractAuthenticationToken> customJwtAuthenticationConverter() {
        return new Converter<Jwt, AbstractAuthenticationToken>() {
            @Override
            public AbstractAuthenticationToken convert(@NonNull Jwt jwt) {
                String token = jwt.getTokenValue();

                if (!jwtService.validateToken(jwt)) {
                    throw new BadCredentialsException("JWT 驗證失效或被撤銷");
                }

                // 權限
                JwtClaims jwtClaims = JwtClaims.fromJwt(jwt);
                Set<Role> roles = jwtClaims.getRoles();
                Collection<GrantedAuthority> authorities = roles != null
                        ? roles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .collect(Collectors.toSet())
                        : List.of();

                log.info("authorities={}", authorities);
                return new JwtAuthenticationToken(jwt, authorities);
            }
        };
    }

    @Bean
    public PasswordEncoder encoder() {
        // 安全必要
        return new Argon2PasswordEncoder(16, 32, 4, 65536, 3);
    }

    @Bean
    public UserOperationLoggingFilter userOperationLoggingFilter(DSLContext dsl) {
        // 操作日誌
        return new UserOperationLoggingFilter(dsl);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("＊"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true); // 如果你有使用 Cookie 或認證請開啟
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
