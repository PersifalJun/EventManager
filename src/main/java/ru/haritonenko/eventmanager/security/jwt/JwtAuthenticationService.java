package ru.haritonenko.eventmanager.security.jwt;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import ru.haritonenko.eventmanager.user.api.dto.authorization.UserCredentials;

@Service
public class JwtAuthenticationService {

    private final AuthenticationManager authenticationManager;

    private final JwtTokenManager jwtTokenManager;

    public JwtAuthenticationService(AuthenticationManager authenticationManager, JwtTokenManager jwtTokenManager) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenManager = jwtTokenManager;
    }

    public String authenticate(UserCredentials userFromSignInRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        userFromSignInRequest.login(),
                        userFromSignInRequest.password()
                )
        );
        return jwtTokenManager.generateToken(userFromSignInRequest.login());
    }
}
