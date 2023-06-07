package example.cashcard.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private RestAuthenticationEntryPoint authenticationEntryPoint;

    public SecurityConfig(RestAuthenticationEntryPoint authenticationEntryPoint) {
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf((csrf) -> csrf.disable())
                .httpBasic((httpBasic) -> httpBasic
                        .authenticationEntryPoint(authenticationEntryPoint))
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/cashcards/**").hasRole("CARD-OWNER")
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService testOnlyUsers(PasswordEncoder passwordEncoder) {
        User.UserBuilder users = User.builder();
        UserDetails sarah = users
                .username("sarah1")
                .password(passwordEncoder.encode(""))
                .roles("CARD-OWNER")
                .build();
        users = User.builder(); // variable 'users' needs to be reset. otherwise hank will also have "CARD-OWNER" role
        UserDetails hank = users
                .username("hank")
                .password(passwordEncoder.encode(""))
                .roles("NON-OWNER")
                .build();
        users = User.builder();
        UserDetails mike = users
                .username("mike")
                .password(passwordEncoder.encode(""))
                .roles("CARD-OWNER")
                .build();
        return new InMemoryUserDetailsManager(sarah, hank, mike);
    }
}
