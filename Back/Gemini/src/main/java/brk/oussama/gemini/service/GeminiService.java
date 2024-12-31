package brk.oussama.gemini.service;


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

@Service
public class GeminiService {
    private final RestClient restClient;

    @Value("${gemini.api.key}")
    private String key;

    public GeminiService(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder
                .baseUrl("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent")
                .build();
    }

    private String callGemini(String prompt) {
        String safePrompt = (prompt == null || prompt.trim().isEmpty())
                ? "Provide a generic helpful response."
                : prompt;
        try {
            Map<String, Object> requestBody = new HashMap<>();
            List<Map<String, Object>> contents = new ArrayList<>();
            Map<String, Object> contentItem = new HashMap<>();
            List<Map<String, Object>> parts = new ArrayList<>();
            Map<String, Object> part = new HashMap<>();
            part.put("text", safePrompt);
            parts.add(part);
            contentItem.put("parts", parts);
            contents.add(contentItem);
            requestBody.put("contents", contents);
            Map<String, Object> response = restClient.post()
                    .uri(uriBuilder -> uriBuilder.queryParam("key", key).build())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {
                    });
            if (response == null) {
                return "No response received from Gemini API";
            }
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            if (candidates == null || candidates.isEmpty()) {
                return "No candidates in Gemini API response";
            }
            Map<String, Object> firstCandidate = candidates.get(0);
            if (firstCandidate == null) {
                return "First candidate is null";
            }
            Map<String, Object> content = (Map<String, Object>) firstCandidate.get("content");
            if (content == null) {
                return "Content is null";
            }
            List<Map<String, Object>> responseParts = (List<Map<String, Object>>) content.get("parts");
            if (responseParts == null || responseParts.isEmpty()) {
                return "No parts in response";
            }
            Object textObj = responseParts.get(0).get("text");
            return textObj != null ? textObj.toString() : "No text found in response";

        } catch (Exception e) {
            e.printStackTrace();
            return "Error calling Gemini API: " + e.getMessage();
        }
    }

    public String generateTest(String file, TestType testType, String add) throws FileNotFoundException {
        String prompt = String.format(
                "I will give you Java code. Could you please generate %s, return only the Java code. Java code:\"\"\" %s \"\"\" %s",
                testType.getDescription(),
                file,
                add
        );
        return callGemini(prompt);

    }

    public String genereratePOMtest(String pom) throws FileNotFoundException {

            String prompt = String.format(
                    "I will give you pom.xml . Could you please check if it this dependency \"\"\"<dependency> <groupId>org.mockito</groupId> <artifactId>mockito-core</artifactId> <version>5.11.0</version> </dependency> <dependency> <groupId>org.springframework.boot</groupId> <artifactId>spring-boot-starter-test</artifactId> <scope>test</scope> </dependency>\"\"\" and plugin \"\"\" <plugin> <groupId>org.jacoco</groupId> <artifactId>jacoco-maven-plugin</artifactId> <version>0.8.11</version> <executions> <execution> <id>prepare-agent</id> <goals> <goal>prepare-agent</goal> </goals> </execution> <execution> <id>report</id> <phase>test</phase> <goals> <goal>report</goal> </goals> </execution> </executions> <configuration> <excludes> <exclude>**/config/**</exclude> <exclude>**/model/**</exclude> <exclude>**/entity/**</exclude> <exclude>**/Application.*</exclude> </excludes> </configuration> </plugin>\"\"\"if exist if not add it, return only the pom.xml. pom.xm code :\"\"\" %s \"\"\" ",
                    pom
            );
            return callGemini(prompt);

    }

}