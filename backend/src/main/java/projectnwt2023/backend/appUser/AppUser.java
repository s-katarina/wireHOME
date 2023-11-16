package projectnwt2023.backend.appUser;

import lombok.*;
import projectnwt2023.backend.appUser.dto.AppUserDTO;

import javax.persistence.*;

import static javax.persistence.InheritanceType.JOINED;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
@TableGenerator(name="appUser_id_generator", table="primary_keys", pkColumnName="key_pk", pkColumnValue="appUser", valueColumnName="value_pk")
@Inheritance(strategy=JOINED)
@Table(name = "app_user")
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;
    private String name;
    private String lastName;
    private String email;
    private String password;
    private Role role;
    private Long token;
    private Boolean active;

    public AppUser(AppUserDTO appUserDTO) {
        this.setName(appUserDTO.getName());
        this.setLastName(appUserDTO.getLastName());
        this.setEmail(appUserDTO.getEmail());
    }
}
