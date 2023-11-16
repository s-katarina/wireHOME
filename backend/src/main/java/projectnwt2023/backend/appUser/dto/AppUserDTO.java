package projectnwt2023.backend.appUser.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import projectnwt2023.backend.appUser.AppUser;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AppUserDTO {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long id;
    private String name;
    private String lastName;
    private String email;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String password;

    public AppUserDTO(AppUser appUser) {
        this.setId(appUser.getId());
        this.setName(appUser.getName());
        this.setLastName(appUser.getLastName());
        this.setPassword(null);
        this.setEmail(appUser.getEmail());
    }

}
