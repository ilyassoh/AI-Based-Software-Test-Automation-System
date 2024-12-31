package brk.oussama.prj.controller;

import brk.oussama.prj.services.TestGeneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {
    @Autowired
    TestGeneService testGeneService;

    public TestController() {
    }

    @PostMapping("/Generate/{projectName}")
    public ResponseEntity<String> generateTestU(@PathVariable String projectName) {
        try {
            testGeneService.generateTestGene(projectName);
            return ResponseEntity.ok("test created");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error while generating the test" + e.getMessage());
        }
    }
}
