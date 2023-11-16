package projectnwt2023.backend.appUser.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import projectnwt2023.backend.appUser.AppUser;
import projectnwt2023.backend.appUser.Role;
import projectnwt2023.backend.appUser.dto.AppUserDTO;
import projectnwt2023.backend.appUser.dto.LoginDTO;
import projectnwt2023.backend.appUser.dto.TokenResponseDTO;
import projectnwt2023.backend.appUser.service.interfaces.IAppUserService;
import projectnwt2023.backend.auth.JwtTokenUtil;
import projectnwt2023.backend.exceptions.EntityAlreadyExistsException;
import projectnwt2023.backend.exceptions.EntityNotFoundException;
import projectnwt2023.backend.helper.Constants;
import projectnwt2023.backend.mail.MailService;

import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@CrossOrigin(origins = {"http://localhost:4200", "http://localhost"})
@RestController
@RequestMapping("/api/user")
@Validated
public class AppUserController {

    @Autowired
    private IAppUserService appUserService;
    @Autowired
    private MailService mailService;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @PostMapping(produces = "application/json", consumes = "application/json")
    public ResponseEntity<AppUserDTO> registerUser(@Valid @RequestBody AppUserDTO appUserDTO) throws IOException {

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

        mailService.sendTextEmailMilos("mikicamiki.bat@gmail.com", "Activate account", "Follow link to activate account: " + "http://localhost:8080/api/user/activate/" + token);

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

    @PostMapping(value = "/superadmin/changePassword")
    @PreAuthorize(value = "hasRole('SUPER_ADMIN')")
    public ResponseEntity<AppUserDTO> registerAdmin(@RequestBody String password) {

        List<AppUser> superAdmins = appUserService.findAllByRole(Role.SUPER_ADMIN);

        if (superAdmins.size() == 0)
            throw new EntityNotFoundException(AppUser.class);

        AppUser superAdmin = superAdmins.get(0);

        String newPassword = new BCryptPasswordEncoder().encode(password);

        superAdmin.setPassword(newPassword);
        superAdmin.setActive(true);

        AppUser saved = appUserService.saveAppUser(superAdmin);

        File superAdminFile = new File(Constants.superAdminPath);
        if (superAdminFile.delete()) {
            System.out.println("Deleted the file: " + superAdminFile.getName());
        } else {
            System.out.println("Failed to delete the file.");
        }

        return new ResponseEntity<>(new AppUserDTO(saved), HttpStatus.OK);
    }

    @PostMapping(value = "/admin", produces = "application/json", consumes = "application/json")
    @PreAuthorize(value = "hasRole('SUPER_ADMIN')")
    public ResponseEntity<AppUserDTO> registerAdmin(@Valid @RequestBody AppUserDTO appUserDTO) {

        if (appUserService.findByEmail(appUserDTO.getEmail()).isPresent())
            throw new EntityAlreadyExistsException(AppUser.class);

        Random r = new Random();
        Long token = r.nextLong();

        String password = new BCryptPasswordEncoder().encode(appUserDTO.getPassword());

        AppUser appUser = new AppUser(appUserDTO);
        appUser.setRole(Role.ADMIN);
        appUser.setActive(true);
        appUser.setToken(token);
        appUser.setPassword(password);

        AppUser saved = appUserService.saveAppUser(appUser);

        return new ResponseEntity<>(new AppUserDTO(saved), HttpStatus.OK);
    }

    @PostMapping(value = "/login", consumes = "application/json", produces = "application/json")
    public ResponseEntity<TokenResponseDTO> login(@Valid @RequestBody LoginDTO loginDTO){

        UsernamePasswordAuthenticationToken authReq = new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword());

        Authentication auth = authenticationManager.authenticate(authReq);

        SecurityContext sc = SecurityContextHolder.getContext();
        sc.setAuthentication(auth);

        String role = sc.getAuthentication().getAuthorities().toString();
        Optional<AppUser> appUserOptional = appUserService.findByEmail(loginDTO.getEmail());

        String token = jwtTokenUtil.generateToken(
                loginDTO.getEmail(),
                Role.valueOf(role.substring(role.indexOf("_") + 1, role.length() - 1)),
                appUserOptional.get().getId());

        return new ResponseEntity<>(
                new TokenResponseDTO(token),
                HttpStatus.OK
        );

    }

    @GetMapping(value = "/{id}", produces = "application/json")
    public ResponseEntity<AppUserDTO> getUserByEmail(@PathVariable Long id) {

        Optional<AppUser> appUserOptional = appUserService.findById(id);

        if (!appUserOptional.isPresent())
            throw new EntityNotFoundException(AppUser.class);

        return new ResponseEntity<>(new AppUserDTO(appUserOptional.get()), HttpStatus.OK);
    }

}
