package projectnwt2023.backend.appUser.service.interfaces;

import com.sun.org.apache.xpath.internal.operations.Bool;
import projectnwt2023.backend.appUser.AppUser;
import projectnwt2023.backend.appUser.Role;

import java.util.List;
import java.util.Optional;

public interface IAppUserService {

    Optional<AppUser> findByEmail(String email);
    Optional<AppUser> findById(Long id);
    Optional<AppUser> findByToken(Long token);
    AppUser saveAppUser(AppUser appUser);
    AppUser activateUser(AppUser appUser);
    List<AppUser> findAllByRole(Role role);
    AppUser generateSuperAdmin();
    String getBase64ProfileImageForUser(AppUser appUser);
    Boolean writeProfileImageForUserFromBase64(String base64, AppUser appUser);

}
