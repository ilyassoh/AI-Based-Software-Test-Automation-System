package brk.oussama.prj.services;

import brk.oussama.prj.module.Projet;
import brk.oussama.prj.repo.ProjetRepo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;


@Service
public class TestGeneService {
    private final ProjetRepo projetRepo;
    private final ObjectMapper objectMapper;
    private final int NumberThread = 1;
    private final GeminiService geminiService;
    private final FileSystemService fileSystemService;

    public TestGeneService(ProjetRepo projetRepo, ObjectMapper objectMapper, GeminiService geminiService, FileSystemService fileSystemService) {
        this.projetRepo = projetRepo;
        this.objectMapper = objectMapper;
        this.geminiService = geminiService;
        this.fileSystemService = fileSystemService;
    }

    public void generateTestGene(String namprj) throws JsonProcessingException {
        Projet prj = projetRepo.findByNom(namprj); // Get the project from the repository
        String[] filePaths = objectMapper.readValue(prj.getJavafile(), new TypeReference<String[]>() {
        });
        String path = prj.getPath();
        List<String> classNames = objectMapper.readerFor(new TypeReference<List<String>>() {
        }).readValue(prj.getEntity());
        File[] files = FileSystemService.convertToFiles(filePaths);
        if (files != null) {
            ConcurrentLinkedQueue<File> fileQueue = new ConcurrentLinkedQueue<>();
            for (File file : files) {
                fileQueue.add(file);
            }
            ExecutorService executor = Executors.newFixedThreadPool(NumberThread);
            for (File file : files) {
                executor.submit(new FileProcessor(fileQueue, classNames, filePaths, path, geminiService, fileSystemService));
            }
            executor.shutdown();
        }
    }

    static class FileProcessor implements Runnable {
        private final ConcurrentLinkedQueue<File> fileQueue;
        private final List<String> classNames;
        private final String[] fileclas;
        private final String filePath;
        private final GeminiService geminiService;
        private final FileSystemService fileSystemService;


        FileProcessor(ConcurrentLinkedQueue<File> fileQueue, List<String> classNames, String[] entityNames, String filePath, GeminiService geminiService, final FileSystemService fileSystemService) {
            this.fileQueue = fileQueue;
            this.classNames = classNames;
            this.fileclas = entityNames;
            this.filePath = filePath;
            this.geminiService = geminiService;
            this.fileSystemService = fileSystemService;
        }

        private static String getFileNameFromPath(String filePath) {
            String[] pathParts = filePath.split("\\\\");
            String fullFileName = pathParts[pathParts.length - 1]; // Get the last part (file name)
            return fullFileName.substring(0, fullFileName.lastIndexOf('.')); // Remove the extension (.java)
        }

        @SneakyThrows
        @Override
        public void run() {
            while (!fileQueue.isEmpty()) {
                File file = fileQueue.poll();
                if (file != null) {
                    if (isClassFile(file)) {
                        try {


                        Map<String, List<String>> send = analyzeMethodSignatures(file);
                        Set<String> classAS = filterFilePaths(send, Arrays.asList(fileclas)).stream().collect(Collectors.toSet());
                        String fileContent = readFileContent(file);
                        Set<String> matchingFilePaths = classNames.stream()
                                .filter(filePath -> {
                                    String className = getFileNameFromPath(filePath);
                                    return fileContent.contains(" " + className + " ");
                                })
                                .collect(Collectors.toSet());
                        for (String className : matchingFilePaths) {
                            if (!className.isBlank()) {
                                classAS.add(className);
                            }
                        }
                        String code = "";
                        String str = "";
                        if (matchingFilePaths.size() > 0) {
                            String[] pathArray = matchingFilePaths.toArray(new String[0]);
                            File[] files = FileSystemService.convertToFiles(pathArray);
                            str = geminiService.getClassAssociatedContent(files);
                        }
                        if (isController(file)) {

                            code = geminiService.generateTest(file, TestType.WEB, str);


                        } else if (isInject(file)) {
                            code = geminiService.generateTest(file, TestType.WEB, str);
                        } else {
                            code = geminiService.generateTest(file, TestType.WEB, str);
                        }
                        fileSystemService.createJavaClassWithPackage(filePath, cleanJavaSnippet(code));
                        }catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                }
            }
        }
    }

    private static String readFileContent(File file) throws IOException {
        return new String(Files.readAllBytes(file.toPath()));
    }

    public static boolean isClassFile(File file) {
        try {
            String fileContent = readFileContent(file);
            return fileContent.contains("class ");
        } catch (IOException e) {
            throw new RuntimeException("Error reading file: " + file.getName(), e);
        }
    }

    public static boolean isInject(File file) {
        try {
            String fileContent = readFileContent(file);
            if (fileContent.contains("@Autowired")) {
                return true;
            }

            String constructorPattern = "public " + file.getName().split("\\.")[0] + "\\s*\\((.*)\\)";  // match class name and constructor
            if (fileContent.matches(".*" + constructorPattern + ".*")) {
                String constructorParameters = fileContent.split(constructorPattern)[1];
                return constructorParameters.contains("final");
            }
            if (fileContent.contains("@RequiredArgsConstructor") && fileContent.contains("final")) {
                return true;
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading file: " + file.getName(), e);
        }
        return false;
    }

    public static boolean isController(File file) {
        try {
            String fileContent = readFileContent(file);
            return fileContent.contains("@RestController"); // Check for @RestController annotation
        } catch (IOException e) {
            throw new RuntimeException("Error reading file: " + file.getName(), e);
        }
    }

    public static boolean isRepoFile(File file) {
        try {
            String fileContent = readFileContent(file);
            return (fileContent.contains("interface ") && fileContent.contains("@Repository") && fileContent.contains("JpaRepository")) || (fileContent.contains("interface ") && fileContent.contains("JpaRepository"));
        } catch (IOException e) {
            throw new RuntimeException("Error reading file: " + file.getName(), e);
        }
    }

    public static String extractEntityName(File file) throws IOException {
        try {
            String fileContent = readFileContent(file);
            String regex = "(?i)extends\\s+JpaRepository\\s*<\\s*([^,\\s>]+)";
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
            java.util.regex.Matcher matcher = pattern.matcher(fileContent);

            if (matcher.find()) {
                return matcher.group(1).trim(); // Extract and return the entity name
            } else {
                throw new IllegalArgumentException(
                        "No valid JpaRepository entity found in file: " + file.getName()
                );
            }
        } catch (Exception e) {
            throw new RuntimeException(
                    "Error extracting entity name from file: " + file.getName(), e
            );
        }
    }


    // Class to handle file entity processing in parallel

    static class RepoProcessor implements Callable<List<String>> {
        private final ConcurrentLinkedQueue<File> fileQueue;

        RepoProcessor(ConcurrentLinkedQueue<File> fileQueue) {
            this.fileQueue = fileQueue;
        }

        @Override
        public List<String> call() {
            List<String> entityNames = new ArrayList<>();
            while (!fileQueue.isEmpty()) {
                File file = fileQueue.poll();
                if (file != null && isRepoFile(file)) {
                    try {
                        entityNames.add(extractEntityName(file)); // Assumes this method exists
                    } catch (IOException e) {
                        System.out.println("Error extracting entity name: " + e.getMessage());
                    }
                }
            }
            return entityNames;
        }
    }

    public List<String> getEntity(List<String> pathClass) {
        List<String> entityPath = new ArrayList<>();
        String[] pathArray = pathClass.toArray(new String[0]);
        File[] files = FileSystemService.convertToFiles(pathArray); // Assumes this method exists

        if (files != null) {
            ConcurrentLinkedQueue<File> fileQueue = new ConcurrentLinkedQueue<>();
            for (File file : files) {
                fileQueue.add(file);
            }

            ExecutorService executor = Executors.newFixedThreadPool(NumberThread);
            List<Future<List<String>>> futures = new ArrayList<>();

            for (int i = 0; i < NumberThread; i++) {
                futures.add(executor.submit(new RepoProcessor(fileQueue)));
            }

            executor.shutdown();
            for (Future<List<String>> future : futures) {
                try {
                    List<String> result = future.get();
                    if (!result.isEmpty()) {
                        for (String entityName : result) {
                            pathClass.stream().forEach(e -> {
                                if (e.contains(File.separator + entityName + ".java")) {
                                    entityPath.add(e);
                                }
                            });

                        }
                    }
                } catch (InterruptedException | ExecutionException e) {
                    System.out.println("Error retrieving future result: " + e.getMessage());
                }
            }
        }
        return entityPath;
    }

    //analyse
    public static Map<String, List<String>> analyzeMethodSignatures(File file) throws IOException {
        String classContent = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
        JavaParser parser = new JavaParser();
        CompilationUnit cu = parser.parse(classContent).getResult().orElseThrow();
        List<Map<String, List<String>>> methods = new ArrayList<>();
        MethodVisitor methodVisitor = new MethodVisitor(methods);
        methodVisitor.visit(cu, null);
        Map<String, List<String>> result = process(methods);
        return result;
    }

    public static List<String> filterFilePaths(Map<String, List<String>> inputMap, List<String> filePaths) {
        List<String> matchedPaths = new ArrayList<>();
        for (String filePath : filePaths) {
            String fileName = filePath.substring(filePath.lastIndexOf("\\") + 1).replace(".java", "");
            for (List<String> validNames : inputMap.values()) {
                if (validNames.contains(fileName)) {
                    matchedPaths.add(filePath);
                    break; // No need to check further categories
                }
            }
        }
        return matchedPaths;
    }

    //get all type and param duplicate
    public static Map<String, List<String>> process(List<Map<String, List<String>>> input) {
        Map<String, List<String>> result = new HashMap<>();
        Set<String> paramSet = new HashSet<>();
        Set<String> typeSet = new HashSet<>();
        String[] keys = {"param", "type"};
        for (Map<String, List<String>> map : input) {
            List<String> params = map.get(keys[0]);
            List<String> types = map.get(keys[1]);
            if (params != null) paramSet.addAll(params);
            if (types != null) typeSet.addAll(types);
        }
        result.put(keys[0], new ArrayList<>(paramSet));
        result.put(keys[1], new ArrayList<>(typeSet));
        return result;
    }

    static class MethodVisitor extends VoidVisitorAdapter<Void> {

        private final List<Map<String, List<String>>> methodList;

        public MethodVisitor(List<Map<String, List<String>>> methodList) {
            this.methodList = methodList;
        }

        @Override
        public void visit(MethodDeclaration md, Void arg) {
            super.visit(md, arg);
            Map<String, List<String>> result = new HashMap<>();
            String methodName = md.getNameAsString();
            String returnType = extractReturnType(md.getType());
            List<String> paramTypes = new ArrayList<>();
            md.getParameters().forEach(param -> paramTypes.add(param.getType().asString()));
            result.put("param", paramTypes);
            result.put("type", Collections.singletonList(returnType));
            methodList.add(result);
        }

        // Extract the actual type from wrapped types
        private String extractReturnType(Type returnType) {
            String returnTypeStr = returnType.asString();
            if (returnTypeStr.startsWith("ResponseEntity")) {
                return extractGenericType(returnTypeStr, "ResponseEntity");
            } else if (returnTypeStr.startsWith("List")) {
                return extractGenericType(returnTypeStr, "List");
            } else if (returnTypeStr.startsWith("Page")) {
                return extractGenericType(returnTypeStr, "Page");
            } else if (returnTypeStr.startsWith("Optional")) {
                return extractGenericType(returnTypeStr, "Optional");
            } else {
                return returnTypeStr; // No wrapper, just return the type
            }
        }

        private String extractGenericType(String returnTypeStr, String wrapper) {
            int startIdx = returnTypeStr.indexOf('<') + 1;
            int endIdx = returnTypeStr.indexOf('>');
            if (startIdx != -1 && endIdx != -1) {
                return returnTypeStr.substring(startIdx, endIdx);
            }
            return returnTypeStr; // In case the generic type is not found
        }

    }

    //this for clean code java test
    public static String cleanJavaSnippet(String input) {
        return input
                .replaceFirst("```java\\n", "") // Remove starting ```java
                .replaceFirst("```$", "")       // Remove ending ```
                .trim();                        // Trim unnecessary spaces or newlines
    }
}