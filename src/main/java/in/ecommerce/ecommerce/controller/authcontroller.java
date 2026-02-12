package in.ecommerce.ecommerce.controller;

import in.ecommerce.ecommerce.DTO.LoginRequestDto;
import in.ecommerce.ecommerce.DTO.RegisterRequestDto;
import in.ecommerce.ecommerce.entity.User;
import in.ecommerce.ecommerce.security.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class authcontroller {
    @Autowired
    UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequestDto user){
        return  ResponseEntity.status(HttpStatus.CREATED).body(userService.register(user));
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDto user){
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(userService.verify(user));
    }
}
