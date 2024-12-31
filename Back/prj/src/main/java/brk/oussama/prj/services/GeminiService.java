package brk.oussama.prj.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class GeminiService {
    private final WebClient webClient;

    public GeminiService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("http://localhost:8080/GEMINI/api/gemini")
                .build();
    }


    public String generateTest(File file, TestType testType, String additionalPrompt) throws FileNotFoundException {
        try {
            // Lire le contenu du fichier
            String fileContent = new String(Files.readAllBytes(Paths.get(file.getPath())));

            // Construire le corps de la requête
            GeminiRequest requestBody = new GeminiRequest(fileContent, testType.name(), additionalPrompt);
            System.out.println(requestBody);
            System.out.println("---------------------");

            // Envoyer la requête POST
            return webClient.post()
                    .uri("/generateTest")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(requestBody) // Envoyer l'objet JSON directement
                    .retrieve()
                    .bodyToMono(String.class) // Récupérer la réponse en tant que chaîne
                    .block();
        } catch (IOException e) {
            e.printStackTrace();
            throw new FileNotFoundException("Could not read file: " + file.getPath());
        }
    }

    public String generatePOMTest(String path) throws FileNotFoundException {
        try {
            // Lire le contenu du fichier pom.xml
            String fileContent = new String(Files.readAllBytes(Paths.get(path + "/pom.xml")));

            // Construire le corps de la requête
            GeminiRequest requestBody = new GeminiRequest(fileContent, "SIMPLE", "");

            // Envoyer la requête POST
            return webClient.post()
                    .uri("/generatePOMTest")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(requestBody) // Envoyer l'objet JSON directement
                    .retrieve()
                    .bodyToMono(String.class) // Récupérer la réponse en tant que chaîne
                    .block();
        } catch (IOException e) {
            e.printStackTrace();
            throw new FileNotFoundException("Could not read path: " + path);
        }
    }

    // Classe interne pour représenter le corps des requêtes
    public static class GeminiRequest {
        private String file;
        private String testType;
        private String additionalPrompt;

        public GeminiRequest(String file, String testType, String additionalPrompt) {
            this.file = file;
            this.testType = testType;
            this.additionalPrompt = additionalPrompt;
        }

        public String getFile() {
            return file;
        }

        public void setFile(String file) {
            this.file = file;
        }

        public String getTestType() {
            return testType;
        }

        public void setTestType(String testType) {
            this.testType = testType;
        }

        public String getAdditionalPrompt() {
            return additionalPrompt;
        }

        public void setAdditionalPrompt(String additionalPrompt) {
            this.additionalPrompt = additionalPrompt;
        }
    }

    public String getClassAssociatedContent(File[] files) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (File file : files) {
            if (file.exists() && file.isFile()) {  // Check if the file exists and is a file
                try {
                    sb.append(new String(Files.readAllBytes(Paths.get(file.getPath())))); // Read file as String
                } catch (IOException e) {
                    throw new IOException("Error reading file: " + file.getName(), e); // Re-throw with file name context
                }
            }
        }
        return ", this code associated \"\"\" " + sb.toString() + "\"\"\" ";
    }
}