package projectnwt2023.backend.appUser.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import projectnwt2023.backend.appUser.AppUser;
import projectnwt2023.backend.appUser.repository.AppUserRepository;
import projectnwt2023.backend.appUser.service.interfaces.IAppUserService;

import java.util.Optional;

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
}
