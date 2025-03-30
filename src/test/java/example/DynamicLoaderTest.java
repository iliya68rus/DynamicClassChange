package example;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import util.TestUtil;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static example.DynamicLoader.OUTPUT_DIR;

public class DynamicLoaderTest {

    @Test
    void testDynamicClassLoading() throws Exception {
        String sourceFile = "src/main/java/example/DynamicClass.java";

        changeClassTest(sourceFile, "MY_VALUE", "NEW_VALUE");
    }

    @Test
    void testDynamicClassLoadingFromRoot() throws Exception {
        String sourceFile = "DynamicClass.java";

        changeClassTest(sourceFile, "MY_ROOT_VALUE", "NEW_ROOT_VALUE");
    }

    @Test
    @Disabled
    void testDynamicInLoop() throws InterruptedException {
        DynamicLoader loader = new DynamicLoader();
        String sourceFile = "DynamicClassLoop.java";

        for (int i = 0; i < 100; i++) {
            DynamicInterface obj = loader.loadDynamicClass(sourceFile);
            System.out.println(obj.getValue());
            TimeUnit.SECONDS.sleep(1);
        }
    }

    private void changeClassTest(String sourceFile, String baseValue, String newValue) throws Exception {
        DynamicLoader loader = new DynamicLoader();

        // Сохраняем оригинальное содержимое для восстановления после теста
        Path sourceFilePath = Path.of(sourceFile);
        String originalContent = Files.readString(sourceFilePath);
        try {
            // Первая загрузка
            DynamicInterface obj = loader.loadDynamicClass(sourceFile);
            Assertions.assertEquals(baseValue, obj.getValue());

            // Модификация и повторная загрузка
            TestUtil.replaceTextInFile(sourceFile, baseValue, newValue);
            obj = loader.loadDynamicClass(sourceFile);
            Assertions.assertEquals(newValue, obj.getValue());
        } finally {
            // Восстанавливаем оригинальное содержимое
            TestUtil.replaceTextInFile(sourceFile, newValue, baseValue);

            // Дополнительная проверка восстановления
            String finalContent = Files.readString(sourceFilePath);
            Assertions.assertEquals(originalContent, finalContent, "File content should be restored after test");
        }
    }

    @AfterAll
    static void deleteClasses() {
        deleteFolder(new File(OUTPUT_DIR.substring(0, OUTPUT_DIR.indexOf("/"))));
    }

    static void deleteFolder(File folder) {
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteFolder(file);
                }
            }
        }
        boolean ignore = folder.delete();
    }
}
