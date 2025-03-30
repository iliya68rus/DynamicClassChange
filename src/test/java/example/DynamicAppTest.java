package example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@Disabled
public class DynamicAppTest {

//    @Container
//    public GenericContainer<?> appContainer = new GenericContainer<>("openjdk:17-jdk")
////            .withCopyFileToContainer(MountableFile.forHostPath("build/libs/DynamicClassChange-1.0.0-SNAPSHOT.jar"), "/opt/app.jar")
//            .withCopyFileToContainer(
//                    MountableFile.forHostPath(
//                            Path.of("build", "libs", "DynamicClassChange-1.0.0-SNAPSHOT.jar").toAbsolutePath()),
//                    "/opt/app.jar"
//            )
//            .withCommand("java", "-jar", "/opt/app.jar")
//            .waitingFor(Wait.forLogMessage(".*Application started.*", 1))
//            .withStartupTimeout(Duration.ofMinutes(1));

    @Container
    public GenericContainer<?> appContainer = new GenericContainer<>(
            new ImageFromDockerfile()
//                    .withDockerfile(Paths.get("src/test/docker/Dockerfile"))
                    .withDockerfile(Paths.get("Dockerfile"))
    )
            .waitingFor(Wait.forLogMessage(".*Application started.*", 1));

    @Test
    void testDynamicClassChange() throws IOException {
        // 1. Создаем исходный файл DynamicClass.java
        String initialContent = """
                package example;
                
                public class DynamicClass implements DynamicInterface {
                    @Override
                    public String getValue() {
                        return "INITIAL_VALUE";
                    }
                }
                """;

        // 2. Копируем файл в контейнер
        Path tempFile = Files.createTempFile("DynamicClass", ".java");
        Files.writeString(tempFile, initialContent);

        appContainer.copyFileToContainer(
                MountableFile.forHostPath(tempFile),
                "/DynamicClass.java"
        );

        // 3. Проверяем начальное поведение
        // (здесь нужно добавить логику проверки - например через HTTP запросы или анализ логов)

        // 4. Модифицируем файл
        String modifiedContent = """
                package example;
                
                public class DynamicClass implements DynamicInterface {
                    @Override
                    public String getValue() {
                        return "MODIFIED_VALUE";
                    }
                }
                """;

        Files.writeString(tempFile, modifiedContent, StandardOpenOption.TRUNCATE_EXISTING);

        // 5. Копируем измененный файл
        appContainer.copyFileToContainer(
                MountableFile.forHostPath(tempFile),
                "/DynamicClass.java"
        );

        // 6. Проверяем измененное поведение
        // (аналогично пункту 3)

        Files.deleteIfExists(tempFile);
    }

    @Test
    void testContainerFiles() throws Exception {
        var result = appContainer.execInContainer("ls", "-la", "/opt");
        System.out.println(result.getStdout());
    }

    @BeforeEach
    void checkJarExists() {
        Path jarPath = Path.of("build", "libs", "DynamicClassChange-1.0.0-SNAPSHOT.jar");
        assertTrue(Files.exists(jarPath), "JAR file not found at: " + jarPath.toAbsolutePath());
    }
}
