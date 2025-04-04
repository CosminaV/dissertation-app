package ro.ase.ism.dissertation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import ro.ase.ism.dissertation.model.user.Role;
import ro.ase.ism.dissertation.model.user.User;
import ro.ase.ism.dissertation.repository.UserRepository;

@Slf4j
@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		EnvLoader.loadEnv();
		SpringApplication.run(BackendApplication.class, args);
	}

	@Bean
	CommandLineRunner initAdminUser(UserRepository userRepository) {
		return args -> {
			if (userRepository.findByFirstName("admin").isEmpty()) {
				User admin = new User();
				admin.setFirstName("admin");
				admin.setEmail("admin@admin.com");
				admin.setPassword(new BCryptPasswordEncoder().encode("admin")); // cripteaza parola
				admin.setRole(Role.ADMIN);

				userRepository.save(admin);
				log.info("Admin user created");
			} else {
				log.info("Admin user already exists.");
			}
		};
	}
}
