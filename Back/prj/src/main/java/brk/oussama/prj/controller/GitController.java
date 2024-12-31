package brk.oussama.prj.controller;

import brk.oussama.prj.dto.Codedto;
import brk.oussama.prj.services.GitServices;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/git")
public class GitController {
    private final GitServices gitServices;

    public GitController(GitServices gitServices) {
        this.gitServices = gitServices;
    }

    @PostMapping
    public ResponseEntity<Boolean> getProject(@RequestBody Codedto.GitRequest github) {
        int result = gitServices.cloneRepo(github.getUrl());
        return switch (result) {
            case 1 -> ResponseEntity.ok(true);
            case 0 -> ResponseEntity.status(409).body(false);
            default -> ResponseEntity.status(500).body(false);
        };
    }
}
