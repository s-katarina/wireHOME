package projectnwt2023.backend.appUser.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import projectnwt2023.backend.appUser.AppUser;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByEmail(String email);
    Optional<AppUser> findById(Long id);
    Optional<AppUser> findByToken(Long token);

}
