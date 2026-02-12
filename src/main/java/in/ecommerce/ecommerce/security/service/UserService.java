package in.ecommerce.ecommerce.security.service;

import in.ecommerce.ecommerce.DTO.LoginRequestDto;
import in.ecommerce.ecommerce.DTO.RegisterRequestDto;
import in.ecommerce.ecommerce.entity.Role;
import in.ecommerce.ecommerce.entity.User;
import in.ecommerce.ecommerce.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private JWTService jwtService;

    @Autowired
    AuthenticationManager authManager;

    @Autowired
    private UserRepo repo;


    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public User register(RegisterRequestDto request) {
        User user=User.builder()
                .email(request.getEmail())
                .password(encoder.encode(request.getPassword()))
                .isVerified(false)
                .role(Role.valueOf("CUSTOMER"))
                .build();

        repo.save(user);
        return user;
    }

    public String verify(LoginRequestDto user) {
        Authentication authentication = authManager.authenticate(new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword()));
        if (authentication.isAuthenticated()) {
            return jwtService.generateToken(user.getEmail()) ;
        } else {
            return "password or email not matched";
        }
    }
}
