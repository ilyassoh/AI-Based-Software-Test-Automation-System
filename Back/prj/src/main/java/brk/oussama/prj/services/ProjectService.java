package brk.oussama.prj.services;

import brk.oussama.prj.module.Projet;
import brk.oussama.prj.module.User;
import brk.oussama.prj.repo.ProjetRepo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProjectService {
    private final ProjetRepo projetRepo;
    private final FileSystemService fileSystemService;
    private final ObjectMapper objectMapper;
    private final TestGeneService testGeneService;

    public ProjectService(ProjetRepo projetRepo, FileSystemService fileSystemService, ObjectMapper objectMapper, TestGeneService testGeneService) {
        this.projetRepo = projetRepo;
        this.fileSystemService = fileSystemService;
        this.objectMapper = objectMapper;
        this.testGeneService = testGeneService;
    }

    public Projet saveProjetWithDetail(String projetName, String path, User user) throws JsonProcessingException {
        List<String> files = fileSystemService.getFileMap(path);
        String entity = objectMapper.writeValueAsString(testGeneService.getEntity(files));
        Projet projet = Projet.builder()
                .javafile(objectMapper.writeValueAsString(files))
                .nom(projetName)
                .path(path)
                .user(user)
                .entity(entity)
                .build();
        return projetRepo.save(projet);
    }
}
