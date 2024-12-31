package brk.oussama.prj.controller;

import brk.oussama.prj.services.FileSystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projet")
public class SystemfileController {
    @Autowired
    private FileSystemService fileservice;


    @GetMapping("/names")
    public List<String> listAllProjects(){
        return fileservice.getAllProjectNames();
    }

    @GetMapping("/{prj}")
    public List<Map<String, Object>> listFiles(@PathVariable String prj,@RequestParam(defaultValue = "") String localPath) {
        return fileservice.listFiles(prj,localPath);
    }
    @GetMapping("/{prj}/file/read")
        public ResponseEntity<String> readFile(@PathVariable String prj, @RequestParam(defaultValue = "") String localPath) {
        try {
            String content = fileservice.readFileContent(prj, localPath);
            return ResponseEntity.ok(content);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body("An error occurred while reading the file: " + e.getMessage());
        }
    }
    @PostMapping("/{prj}/download")
    public ResponseEntity<InputStreamResource> Zipproject(@PathVariable String prj) throws IOException {
        File zipFile = new File(fileservice.PathZipfile(prj));
        InputStreamResource resource = new InputStreamResource(new FileInputStream(zipFile));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + zipFile.getName() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(zipFile.length())
                .body(resource);
    }

}