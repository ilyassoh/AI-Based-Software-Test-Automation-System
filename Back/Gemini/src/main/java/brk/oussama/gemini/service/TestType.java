package brk.oussama.gemini.service;

public enum TestType {
    SIMPLE("unit test JUnit5 with Code Coverage 100%"),
    INJECT("unit test JUnit5 and Mockito with Code Coverage 100%"),
    WEB("unit test JUnit5 and MockitoMVC with Code Coverage 100%");

    private final String description;

    TestType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

