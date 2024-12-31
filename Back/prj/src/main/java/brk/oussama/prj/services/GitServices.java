package brk.oussama.prj.services;

import brk.oussama.prj.module.User;
import brk.oussama.prj.repo.UserRepo;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class GitServices {
    private static final String UPLOAD_DIRECTORY = "Git";
    private final UserRepo userRepo;
    private final ProjectService projectService;

    public GitServices(UserRepo userRepo, ProjectService projectService) {
        this.userRepo = userRepo;
        this.projectService = projectService;
    }

    public int cloneRepo(String repoUrl) {
        try {
            User user = userRepo.findById(1L).orElse(null);
            String prj = this.extractProjectName(repoUrl);
            String path = UPLOAD_DIRECTORY + "/" + user.getUsername() + "/" + prj;
            Path folderPath = Paths.get(path).toAbsolutePath();
            File localDirectory = folderPath.toFile();
            if (localDirectory.exists()) {
                System.out.println("Directory already exists: " + localDirectory.getPath());
                return 0; // Directory exists
            }
            File parentDir = localDirectory.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                boolean created = parentDir.mkdirs();
                if (!created) {
                    System.out.println("Failed to create parent directory: " + parentDir.getPath());
                    return -2; // Error creating parent directory
                }
            }
            Git.cloneRepository()
                    .setURI(repoUrl)
                    .setDirectory(localDirectory)
                    .call();
            projectService.saveProjetWithDetail(prj, path, user);
            System.out.println("Repository cloned successfully to: " + localDirectory.getPath());
            return 1;
        } catch (GitAPIException e) {
            System.err.println("Error during cloning: " + e.getMessage());
            e.printStackTrace();
            return -1;
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return -3;
        }
    }

    public String extractProjectName(String gitUrl) {
        if (gitUrl == null || gitUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("L'URL du dépôt Git ne peut pas être vide.");
        }
        if (gitUrl.endsWith(".git")) {
            gitUrl = gitUrl.substring(0, gitUrl.length() - 4);
        }
        int lastSlashIndex = gitUrl.lastIndexOf('/');
        if (lastSlashIndex == -1 || lastSlashIndex == gitUrl.length() - 1) {
            throw new IllegalArgumentException("L'URL du dépôt Git ne contient pas de nom de projet valide.");
        }
        return gitUrl.substring(lastSlashIndex + 1);
    }
}
