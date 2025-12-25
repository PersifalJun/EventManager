package ru.haritonenko.eventmanager.user.security.custom;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import ru.haritonenko.eventmanager.user.api.exception.UserNotFoundException;
import ru.haritonenko.eventmanager.user.db.repository.UserRepository;

@Component
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        var user = userRepository.findByLogin(username)
                .orElseThrow(() -> new UserNotFoundException("User not found by login: %s".formatted(username)));
        return User.withUsername(username)
                .password(user.getPassword())
                .authorities(String.valueOf(user.getUserRole()))
                .build();
    }
}
