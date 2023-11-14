package projectnwt2023.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import projectnwt2023.backend.helper.Constants;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
		Constants.sendgridKey = System.getenv("SENDGRID_API_KEY");
		System.out.println(Constants.sendgridKey);
	}

}
