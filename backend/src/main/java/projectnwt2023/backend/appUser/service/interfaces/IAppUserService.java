package projectnwt2023.backend.appUser.service.interfaces;

import projectnwt2023.backend.appUser.AppUser;

import java.util.Optional;

public interface IAppUserService {

    Optional<AppUser> findByEmail(String email);
    Optional<AppUser> findById(Long id);
    Optional<AppUser> findByToken(Long token);
    AppUser saveAppUser(AppUser appUser);
    AppUser activateUser(AppUser appUser);

}
