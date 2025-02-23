package sesac.intruders.piggybank.global.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import sesac.intruders.piggybank.global.security.jwt.JwtAccessDeniedHandler;
import sesac.intruders.piggybank.global.security.jwt.JwtAuthenticationEntryPoint;
import sesac.intruders.piggybank.global.security.jwt.JwtAuthenticationFilter;
import sesac.intruders.piggybank.global.security.jwt.JwtTokenProvider;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

        private final JwtTokenProvider jwtTokenProvider;
        private final RedisTemplate redisTemplate;

        @Bean
        public BCryptPasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        // 로그인 하지 않아도 접근 가능한 주소 설정
        private static final String[] AUTH_WHITELIST = {
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/swagger-resources/**",
                        "/webjars/**",
                        "/register/**",
                        "/login/**",
                        "/zipcode/**",
                        "/files/**",
                        "/img/**",
                        "/admin/auth/login",
                        "/admin/auth/logout",
                        "/admin/auth/verify",
                        "/admin/auth/reset-password",
                        "/notices",
                        "/notices/search",
                        "/notices/**",
                        "/qna/**"
        };

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http,
                        JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
                        JwtAccessDeniedHandler jwtAccessDeniedHandler) throws Exception {
                // CSRF, CORS 해제
                http.csrf().disable();
                http.cors().and();

                // 세션 관리 상태 없음으로 구성
                http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

                // FormLogin, BasicHttp 비활성화
                http.formLogin().disable();
                http.httpBasic().disable();

                // JwtAuthFilter 추가
                http.addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider, redisTemplate, AUTH_WHITELIST),
                                UsernamePasswordAuthenticationFilter.class);

                // 예외 처리
                http.exceptionHandling()
                                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                                .accessDeniedHandler(jwtAccessDeniedHandler);

                // 권한 규칙 작성 (authorizeRequests 사용)
                http.authorizeHttpRequests()
                                .antMatchers(AUTH_WHITELIST).permitAll() // AUTH_WHITELIST 경로 인증 없이 허용
                                .antMatchers("/admin/auth/change-password").hasRole("ADMIN") // 비밀번호 변경은 관리자만
                                .antMatchers("/admin/**").hasRole("ADMIN") // 나머지 관리자 기능은 관리자만
                                .anyRequest().hasAnyRole("USER", "ADMIN"); // 나머지는 USER 또는 ADMIN

                return http.build();
        }
}