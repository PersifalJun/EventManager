package ru.haritonenko.eventmanager.user.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.haritonenko.eventmanager.user.service.domain.User;
import ru.haritonenko.eventmanager.user.service.UserService;

import java.io.IOException;
import java.util.List;

import static java.util.Objects.isNull;

@Slf4j
@Component
public class JwtTokenFilter extends OncePerRequestFilter {

    private final JwtTokenManager jwtTokenManager;
    private final UserService userService;

    public JwtTokenFilter(JwtTokenManager jwtTokenManager,
                          @Lazy UserService userService
    ) {
        this.jwtTokenManager = jwtTokenManager;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if(isNull(authorizationHeader)
                || !authorizationHeader.startsWith("Bearer ")){
            filterChain.doFilter(request,response);
            return;
        }
        var jwtToken = authorizationHeader.substring(7);
        String loginFromToken;
        try{
            loginFromToken = jwtTokenManager.getLoginFromToken(jwtToken);
        }catch(Exception ex){
            log.error("Error while reading jwt",ex);
            filterChain.doFilter(request,response);
            return;
        }
        User user = userService.findByLogin(loginFromToken);
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                user,
                null,
                List.of(new SimpleGrantedAuthority(user.role().toString()))
        );
        SecurityContextHolder.getContext()
                .setAuthentication(token);
        filterChain.doFilter(request,response);
    }
}
