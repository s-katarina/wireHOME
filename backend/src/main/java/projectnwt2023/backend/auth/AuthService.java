package projectnwt2023.backend.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import projectnwt2023.backend.appUser.AppUser;
import projectnwt2023.backend.appUser.Role;
import projectnwt2023.backend.appUser.repository.AppUserRepository;

import java.util.Optional;

@Service
public class AuthService implements UserDetailsService {

    @Autowired
    private AppUserRepository appUserRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<AppUser> ret = appUserRepository.findByEmail(username);
        System.out.println(ret);

        if (ret.isPresent() && ret.get().getActive() ||
            ret.isPresent() && ret.get().getRole().equals(Role.SUPER_ADMIN))
            return org.springframework.security.core.userdetails.User
                    .withUsername(username)
                    .password(ret.get().getPassword())
                    .roles(ret.get().getRole().toString())
                    .build();

        throw new UsernameNotFoundException("User not found with this username: " + username);
    }

}
