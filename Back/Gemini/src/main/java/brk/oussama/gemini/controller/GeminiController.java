package brk.oussama.gemini.controller;

import brk.oussama.gemini.dto.Data;
import brk.oussama.gemini.service.GeminiService;
import brk.oussama.gemini.service.TestType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gemini")
public class GeminiController {

    private final GeminiService geminiService;

    @Autowired
    public GeminiController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @PostMapping("/generateTest")
    public ResponseEntity<String> generateTest(@RequestBody Data data) {
        System.out.println("--");
        try {
            String response = geminiService.generateTest(data.file(), data.testType(), data.additionalPrompt());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/generatePOMTest")
    public ResponseEntity<String> generatePOMTest(@RequestBody Data pomXml) {
        try {
            String response = geminiService.genereratePOMtest(pomXml.file());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}
