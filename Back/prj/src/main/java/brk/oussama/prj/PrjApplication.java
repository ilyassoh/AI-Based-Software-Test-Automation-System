package brk.oussama.prj;

import brk.oussama.prj.module.User;
import brk.oussama.prj.repo.UserRepo;
import brk.oussama.prj.services.FileSystemService;
import org.slf4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
@EnableDiscoveryClient
public class PrjApplication {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(PrjApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(PrjApplication.class, args);
    }

    @Bean
    CommandLineRunner createDirectories() {
        return args -> {
            String[] directories = {
                    "uploads",
                    "Git",
                    "uploads/user",
            };

            for (String dir : directories) {
                Path path = Paths.get(dir);
                if (!Files.exists(path)) {
                    try {
                        Files.createDirectories(path);
                        log.info("Created directory: {}", dir);
                    } catch (IOException e) {
                        log.error("Error creating directory: {}", dir, e);
                    }
                } else {
                    log.info("Directory already exists: {}", dir);
                }
            }
        };
    }

    @Bean
    CommandLineRunner initializeAdminUser(UserRepo userRepo, FileSystemService fileSystemService) {
        return args -> {
            // Vérifier si un utilisateur avec l'ID 1 existe déjà
            userRepo.findById(1L).ifPresentOrElse(user -> {
                log.info("Admin user already exists.");
            }, () -> {
                User adminUser = userRepo.save(User.builder()
                        .email("admin@admin.com")
                        .lastName("Admin")
                        .firstName("Admin")
                        .username("admin")
                        .password("admin") // Remplacez par un mot de passe sécurisé dans un environnement réel !
                        .build());

                // Ajouter le répertoire pour cet utilisateur
                try {
                    fileSystemService.addfolderuser(adminUser.getUsername());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                log.info("Admin user created with username: {}", adminUser.getUsername());
            });
        };
    }
}
