package br.com.alura.AluraFake.user;

import br.com.alura.AluraFake.util.ErrorItemDTO;
import br.com.alura.AluraFake.util.PasswordGeneration;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder; // 1. Importar
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    @PostMapping("/user/new")
    public ResponseEntity newStudent(@RequestBody @Valid NewUserDTO newUser) {
        if(userRepository.existsByEmail(newUser.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorItemDTO("email", "Email já cadastrado no sistema"));
        }

        String rawPassword = newUser.getPassword();
        if (rawPassword == null || rawPassword.isEmpty()) {
            rawPassword = PasswordGeneration.generatePassword();
        }

        String encodedPassword = passwordEncoder.encode(rawPassword);
        User user = new User(newUser.getName(), newUser.getEmail(), newUser.getRole(), encodedPassword);

        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/user/all")
    public List<UserListItemDTO> listAllUsers() {
        return userRepository.findAll().stream().map(UserListItemDTO::new).toList();
    }
}