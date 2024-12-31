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
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class FileSystemService {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(FileSystemService.class);
    private final ProjetRepo projetRepo;
    private final ObjectMapper objectMapper;
    private final int NumberThread = 1;
    private final GeminiService geminiService;


    private static final String GIT_UPLOAD = "Git";

    public FileSystemService(ProjetRepo projetRepo, ObjectMapper objectMapper, GeminiService geminiService) {
        this.projetRepo = projetRepo;
        this.objectMapper = objectMapper;
        this.geminiService = geminiService;
    }

    public void addfolderuser(String username) throws IOException {
        Path path = Paths.get(GIT_UPLOAD + "/" + username);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
    }

    public List<String> getAllProjectNames() {
        log.info("Fetching all project names from the database");
        return projetRepo.findAll().stream().map(Projet::getNom).collect(Collectors.toList());
    }

    public List<String> getFileMap(String projet) {
        File directory = new File(projet + "/src/main/java");
        List<String> fileMap = new ArrayList<>();
        if (directory.exists() && directory.isDirectory()) {
            findJavaFileNames(directory, fileMap);
        } else {
            System.out.println("Le répertoire spécifié n'existe pas ou n'est pas un répertoire valide.");
        }
        return fileMap;
    }

    private void findJavaFileNames(File directory, List<String> fileNames) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    findJavaFileNames(file, fileNames);
                } else if (file.isFile() && file.getName().endsWith(".java")) {
                    fileNames.add(file.getAbsolutePath());
                }
            }
        }
    }

    public static File[] convertToFiles(String[] filePaths) {
        if (filePaths == null || filePaths.length == 0) {
            return null;
        }

        File[] files = new File[filePaths.length];
        for (int i = 0; i < filePaths.length; i++) {
            files[i] = new File(filePaths[i]);
        }
        return files;
    }

    public boolean createJavaClassWithPackage(String path_proj, String code) {
        File testFolder = new File(path_proj + "/src/test/java");
        if (!testFolder.exists() || !testFolder.isDirectory()) {
            throw new IllegalArgumentException("The specified folder does not exist or is not valid: " + testFolder.getPath());
        }
        String packageName = extractPackageName(code);

        String className = extractClassName(code);
        System.out.println("Creating class " + className);
        System.out.println("Creating class " + code);

        if (className == null || className.isBlank()) {
            throw new IllegalArgumentException("Unable to detect a valid class name in the provided code.");
        }
        check_ifcreated(path_proj, packageName);
        File packageDir = new File(testFolder.getPath() + "/" + packageName);
        File classFile = new File(packageDir, className + ".java");
        if (!classFile.getParentFile().exists()) {
            boolean dirsCreated = classFile.getParentFile().mkdirs();
            if (!dirsCreated) {
                throw new RuntimeException("Failed to create directories for the class file: " + classFile.getParentFile().getPath());
            }
        }
        try (FileWriter writer = new FileWriter(classFile);
             PrintWriter printWriter = new PrintWriter(writer)) {
            printWriter.print(code); // This writes the string exactly as it is, preserving formatting
        } catch (IOException e) {
            throw new RuntimeException("Failed to write the Java class to the file: " + classFile.getPath(), e);
        }
        return true;
    }

    private void check_ifcreated(String folderName, String path) {
        // Construct the full directory path
        File testFolder = new File(folderName + "/src/test/java" + "/" + path);

        // Check if the folder doesn't exist
        if (!testFolder.exists()) {
            // Create the directories (including any missing parent directories)
            boolean created = testFolder.mkdirs();

            // Optionally, you can log or handle the result
            if (created) {
                System.out.println("Directory created: " + testFolder.getAbsolutePath());
            } else {
                System.out.println("Failed to create directory: " + testFolder.getAbsolutePath());
            }
        }
    }

    // Helper method to extract the class name from the code
    private String extractClassName(String code) {
        String classNamePattern = "class\\s+(\\w+)";
        if(code.contains("public class ")) {
             classNamePattern = "public\\s+class\\s+(\\w+)";

        }
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(classNamePattern);
        java.util.regex.Matcher matcher = pattern.matcher(code);

        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private String extractPackageName(String code) {
        String packageNamePattern = "package\\s+([a-zA-Z0-9_.]+);";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(packageNamePattern);
        java.util.regex.Matcher matcher = pattern.matcher(code);

        if (matcher.find()) {
            // Replace the dots with slashes to get the desired format
            return matcher.group(1).replace('.', '/');
        }
        return "";
    }

    public String readFileContent(String prj, String filePath) {
        Projet projet = projetRepo.findByNom(prj); // Get the project from the repository
        if (projet == null) {
            throw new IllegalArgumentException("The specified project  does not exist: " + prj);
        }
        File file = new File(projet.getPath(), filePath);
        if (!file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("The specified file does not exist or is not a valid file.");
        }
        try {
            // Read file content as a string
            return Files.readString(Paths.get(file.getPath()));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file content", e);
        }
    }

    public List<Map<String, Object>> listFiles(String prj, String folderName) {
        Projet projet = projetRepo.findByNom(prj); // Get the project from the repository
        if (projet == null) {
            throw new IllegalArgumentException("The specified project  does not exist: " + prj);
        }
        // Base path for the Git directory
        File directory = new File(projet.getPath(), folderName);
        System.out.println(directory.getPath());
        System.out.println(folderName);

        List<Map<String, Object>> fileList = new ArrayList<>();

        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    Map<String, Object> fileDetails = new HashMap<>();
                    fileDetails.put("name", file.getName());
                    fileDetails.put("isDirectory", file.isDirectory());
                    fileDetails.put("isFile", file.isFile());
                    fileDetails.put("path", folderName + "/" + file.getName());
                    fileList.add(fileDetails);
                }
            }
        } else {
            throw new IllegalArgumentException("The specified folder does not exist or is not a directory.");
        }
        return fileList;
    }

    public void generateTestGene(String namprj) throws JsonProcessingException {
        log.info("Starting test generation for project: {}", namprj);
        Projet prj = projetRepo.findByNom(namprj); // Get the project from the repository
        if (prj == null) {
            log.error("Project not found: {}", namprj);
            throw new IllegalArgumentException("Project not found: " + namprj);
        }

        String[] filePaths = objectMapper.readValue(prj.getJavafile(), new TypeReference<String[]>() {
        });
        if (filePaths == null || filePaths.length == 0) {
            log.warn("No Java files found for project: {}", namprj);
            return; // Exit early if no files are present.
        }
        String path = prj.getPath();
        List<String> classNames = objectMapper.readerFor(new TypeReference<List<String>>() {
        }).readValue(prj.getEntity());
        File[] files = convertToFiles(filePaths);
        if (files != null) {
            ConcurrentLinkedQueue<File> fileQueue = new ConcurrentLinkedQueue<>();
            for (File file : files) {
                fileQueue.add(file);
            }
            ExecutorService executor = Executors.newFixedThreadPool(NumberThread);
            for (File file : files) {
                executor.submit(new FileProcessor(fileQueue, classNames, filePaths, path, geminiService, this));
            }
            executor.shutdown();
        }
        log.info("Test generation process completed for project: {}", namprj);
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
                                File[] files = convertToFiles(pathArray);
                                str = geminiService.getClassAssociatedContent(files);
                            }
                            if (isController(file)) {
                                code = geminiService.generateTest(file, TestType.WEB, str);
                            } else if (isInject(file)) {
                                code = geminiService.generateTest(file, TestType.INJECT, str);
                            } else {
                                code = geminiService.generateTest(file, TestType.SIMPLE, str);
                            }
                            fileSystemService.createJavaClassWithPackage(filePath, cleanJavaSnippet(code));
                        } catch (Exception e) {
                            log.error("Error processing file: " + file.getAbsolutePath(), e);
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
            log.error("Error checking if file is class file: " + file.getName(), e);
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
            log.error("Error checking isInject in file: " + file.getName(), e);
            throw new RuntimeException("Error reading file: " + file.getName(), e);
        }
        return false;
    }

    public static boolean isController(File file) {
        try {
            String fileContent = readFileContent(file);
            return fileContent.contains("@RestController"); // Check for @RestController annotation
        } catch (IOException e) {
            log.error("Error checking if file is controller: " + file.getName(), e);
            throw new RuntimeException("Error reading file: " + file.getName(), e);
        }
    }

    public static boolean isRepoFile(File file) {
        try {
            String fileContent = readFileContent(file);
            return (fileContent.contains("interface ") && fileContent.contains("@Repository") && fileContent.contains("JpaRepository")) || (fileContent.contains("interface ") && fileContent.contains("JpaRepository"));
        } catch (IOException e) {
            log.error("Error checking if file is a repository file: " + file.getName(), e);
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
                log.error("No valid JpaRepository entity found in file: {}", file.getName());
                throw new IllegalArgumentException(
                        "No valid JpaRepository entity found in file: " + file.getName()
                );
            }
        } catch (Exception e) {
            log.error("Error extracting entity name from file: {}", file.getName(), e);
            throw new RuntimeException(
                    "Error extracting entity name from file: " + file.getName(), e
            );
        }
    }

    // Class to handle file entity processing in parallel

    static class RepoProcessor implements java.util.concurrent.Callable<List<String>> {
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
                        log.error("Error extracting entity name from file: {}", file.getName(), e);
                    }
                }
            }
            return entityNames;
        }
    }

    public List<String> getEntity(List<String> pathClass) {
        List<String> entityPath = new ArrayList<>();
        String[] pathArray = pathClass.toArray(new String[0]);
        File[] files = convertToFiles(pathArray); // Assumes this method exists

        if (files != null) {
            ConcurrentLinkedQueue<File> fileQueue = new ConcurrentLinkedQueue<>();
            for (File file : files) {
                fileQueue.add(file);
            }

            ExecutorService executor = Executors.newFixedThreadPool(NumberThread);
            List<java.util.concurrent.Future<List<String>>> futures = new ArrayList<>();

            for (int i = 0; i < NumberThread; i++) {
                futures.add(executor.submit(new RepoProcessor(fileQueue)));
            }

            executor.shutdown();
            for (java.util.concurrent.Future<List<String>> future : futures) {
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
                } catch (InterruptedException | java.util.concurrent.ExecutionException e) {
                    log.error("Error retrieving future result: ", e);
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
                .replaceFirst("java\\n", "") // Remove starting java
                .replaceFirst("$", "")       // Remove ending
                .trim();                        // Trim unnecessary spaces or newlines
    }

    public String PathZipfile(String name) throws IOException {
        Projet projet = projetRepo.findByNom(name);
        String path = projet.getPath();          // Path of the folder to zip
        String zipfile = projet.getPath() + ".zip"; // Path for the zip file

        // Check if the ZIP file already exists
        File zipFile = new File(zipfile);
        if (zipFile.exists()) {
            if (!zipFile.delete()) {
                throw new IOException("Failed to delete existing zip file: " + zipfile);
            }
        }

        // Create the zip file
        zipFolder(path, zipfile);

        return zipfile; // Return the zip file path
    }

    private void zipFolder(String sourceFolderPath, String zipFilePath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(zipFilePath);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            File sourceFolder = new File(sourceFolderPath);
            addFolderToZip(sourceFolder, sourceFolder.getName(), zos);
        }
    }

    private void addFolderToZip(File folder, String parentFolder, ZipOutputStream zos) throws IOException {
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                // Recursive call for subdirectories
                addFolderToZip(file, parentFolder + "/" + file.getName(), zos);
            } else {
                // Add file to zip
                try (FileInputStream fis = new FileInputStream(file)) {
                    String zipEntryName = parentFolder + "/" + file.getName();
                    zos.putNextEntry(new ZipEntry(zipEntryName));
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = fis.read(buffer)) >= 0) {
                        zos.write(buffer, 0, length);
                    }
                    zos.closeEntry();
                }
            }
        }
    }

}