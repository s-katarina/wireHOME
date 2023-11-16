package projectnwt2023.backend.appUser.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import projectnwt2023.backend.appUser.AppUser;
import projectnwt2023.backend.appUser.Role;
import projectnwt2023.backend.appUser.dto.AppUserDTO;
import projectnwt2023.backend.appUser.service.interfaces.IAppUserService;
import projectnwt2023.backend.exceptions.EntityAlreadyExistsException;
import projectnwt2023.backend.exceptions.EntityNotFoundException;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@CrossOrigin(origins = {"http://localhost:4200", "http://localhost"})
@RestController
@RequestMapping("/api/user")
@Validated
public class AppUserController {

    @Autowired
    private IAppUserService appUserService;

    @PostMapping(produces = "application/json", consumes = "application/json")
    public ResponseEntity<AppUserDTO> registerUser(@Valid @RequestBody AppUserDTO appUserDTO) {

        if (appUserService.findByEmail(appUserDTO.getEmail()).isPresent())
            throw new EntityAlreadyExistsException(AppUser.class);

        Random r = new Random();
        Long token = r.nextLong();

        String password = new BCryptPasswordEncoder().encode(appUserDTO.getPassword());

        AppUser appUser = new AppUser(appUserDTO);
        appUser.setRole(Role.AUTH_USER);
        appUser.setActive(false);
        appUser.setToken(token);
        appUser.setPassword(password);

        AppUser saved = appUserService.saveAppUser(appUser);

        // posalji mejl

        return new ResponseEntity<>(new AppUserDTO(saved), HttpStatus.OK);
    }

    @GetMapping(value = "/activate/{token}", produces = "application/json")
    public ResponseEntity<AppUserDTO> activateUser(@PathVariable Long token) {

        Optional<AppUser> appUserOptional = appUserService.findByToken(token);

        if (!appUserOptional.isPresent())
            throw new EntityNotFoundException(AppUser.class);

        AppUser appUser = appUserOptional.get();
        appUser.setActive(true);

        AppUser saved = appUserService.saveAppUser(appUser);

        return new ResponseEntity<>(new AppUserDTO(saved), HttpStatus.OK);
    }

}
