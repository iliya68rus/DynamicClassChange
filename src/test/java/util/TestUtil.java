package util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestUtil {
    private TestUtil() {}

    public static void replaceTextInFile(String filePath, String oldValue, String newValue) throws IOException {
        Path path = Paths.get(filePath);
        String content = Files.readString(path);

        if (!content.contains(oldValue)) {
            throw new IllegalArgumentException("Old value not found in file content");
        }

        String modifiedContent = content.replace(oldValue, newValue);
        Files.writeString(path, modifiedContent);
    }
}
