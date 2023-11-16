package projectnwt2023.backend.appUser.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import projectnwt2023.backend.appUser.AppUser;
import projectnwt2023.backend.appUser.Role;
import projectnwt2023.backend.appUser.repository.AppUserRepository;
import projectnwt2023.backend.appUser.service.interfaces.IAppUserService;
import projectnwt2023.backend.helper.Constants;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class AppUserService implements IAppUserService {

    @Autowired
    private AppUserRepository appUserRepository;

    @Override
    public Optional<AppUser> findByEmail(String email) {
        return appUserRepository.findByEmail(email);
    }

    @Override
    public Optional<AppUser> findByToken(Long token) {
        return appUserRepository.findByToken(token);
    }

    @Override
    public Optional<AppUser> findById(Long id) {
        return appUserRepository.findById(id);
    }

    @Override
    public AppUser saveAppUser(AppUser appUser) {
        return appUserRepository.save(appUser);
    }

    @Override
    public AppUser activateUser(AppUser appUser) {
        appUser.setActive(true);
        return appUserRepository.save(appUser);
    }

    @Override
    public List<AppUser> findAllByRole(Role role) {
        return appUserRepository.findAllByRole(role);
    }

    private String generateRandomString(Integer length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";

        StringBuilder ret = new StringBuilder();
        Random rnd = new Random();

        while (ret.length() < length) {
            int index = (int) (rnd.nextFloat() * chars.length());
            ret.append(chars.charAt(index));
        }

        return ret.toString();
    }

    @Override
    public AppUser generateSuperAdmin() {
        Random r = new Random();
        Long token = r.nextLong();

        String plainPassword = generateRandomString(20);

        String password = new BCryptPasswordEncoder().encode(plainPassword);

        AppUser superAdmin = new AppUser();
        superAdmin.setName("SUPERADMIN");
        superAdmin.setLastName("SUPERADMIN");
        superAdmin.setEmail("superadmin@gmail.com");
        superAdmin.setRole(Role.SUPER_ADMIN);
        superAdmin.setPassword(password);
        superAdmin.setToken(token);
        superAdmin.setActive(false);

        try (PrintWriter out = new PrintWriter(Constants.superAdminPath)) {
            out.println(plainPassword);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        return appUserRepository.save(superAdmin);
    }
}
