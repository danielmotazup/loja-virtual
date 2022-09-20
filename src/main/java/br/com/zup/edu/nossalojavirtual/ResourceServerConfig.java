package br.com.zup.edu.nossalojavirtual;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
public class ResourceServerConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http
                .cors()
                .and()
                .csrf().disable()
                .formLogin().disable()
                .logout().disable()
                .rememberMe().disable()
                .httpBasic().disable()
                .requestCache().disable()
                .headers().frameOptions().deny()
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()

                .antMatchers(HttpMethod.POST, "/api/users").hasAuthority("SCOPE_users:write")
                .antMatchers(HttpMethod.GET, "/api/users").hasAuthority("SCOPE_users:read")

                .antMatchers(HttpMethod.POST, "/api/categories").hasAuthority("SCOPE_categories:write")
                .antMatchers(HttpMethod.GET, "/api/categories").hasAuthority("SCOPE_categories:read")

                .antMatchers(HttpMethod.POST, "/api/products").hasAuthority("SCOPE_products:write")
                .antMatchers(HttpMethod.GET, "/api/products").hasAuthority("SCOPE_products:read")
                .antMatchers(HttpMethod.POST, "/api/products").hasAuthority("SCOPE_products:write")
                .antMatchers(HttpMethod.GET, "/api/products").hasAuthority("SCOPE_products:read")

                .antMatchers(HttpMethod.POST, "/api/opinions").hasAuthority("SCOPE_products:write")
                .antMatchers(HttpMethod.GET, "/api/opinions").hasAuthority("SCOPE_products:read")

                .antMatchers(HttpMethod.POST, "/api/products/*/questions").hasAuthority("SCOPE_products:write")
                .antMatchers(HttpMethod.GET, "/api/products/*/questions").hasAuthority("SCOPE_products:read")


                .anyRequest().authenticated()
                .and()
                .oauth2ResourceServer().jwt();
    }
}
