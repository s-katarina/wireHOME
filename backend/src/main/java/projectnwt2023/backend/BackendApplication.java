package projectnwt2023.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import projectnwt2023.backend.appUser.AppUser;
import projectnwt2023.backend.appUser.Role;
import projectnwt2023.backend.appUser.service.interfaces.IAppUserService;
import projectnwt2023.backend.devices.service.interfaces.IDeviceService;
import projectnwt2023.backend.helper.Constants;

import java.util.List;

@SpringBootApplication
public class BackendApplication implements ApplicationRunner {

	@Autowired
	private IAppUserService appUserService;

	@Autowired
	private IDeviceService deviceService;

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
		Constants.sendgridKey = System.getenv("SENDGRID_API_KEY");
		System.out.println("Sendgrid api key configured --- " + (Constants.sendgridKey != null));
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		deviceService.preprocessDevices();
		deviceService.preprocessCharger();
		List<AppUser> superAdmins = appUserService.findAllByRole(Role.SUPER_ADMIN);

		if (superAdmins.size() == 0)
			appUserService.generateSuperAdmin();
	}
}
