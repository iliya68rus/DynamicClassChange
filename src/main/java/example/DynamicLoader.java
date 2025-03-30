package example;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

public class DynamicLoader {
    public static final String OUTPUT_DIR = "target/classes";
    private final String rootDir;

    @SuppressWarnings("unchecked")
    public <T> T loadDynamicClass(String sourceFilePath) {
        try {
            String dynamicClassName = extractClassNameFromSource(sourceFilePath);
            compileSourceFile(sourceFilePath);
            byte[] classBytes = loadClassBytes(dynamicClassName);
            return (T) createHiddenClassInstance(classBytes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load dynamic class", e);
        }
    }

    private String extractClassNameFromSource(String sourceFilePath) throws Exception {
        Path sourcePath = Paths.get(rootDir + sourceFilePath);
        String sourceContent = Files.readString(sourcePath);

        String packageName = "";
        Pattern packagePattern = Pattern.compile("package\\s+([a-zA-Z][a-zA-Z0-9_.]*)\\s*;");
        var packageMatcher = packagePattern.matcher(sourceContent);
        if (packageMatcher.find()) {
            packageName = packageMatcher.group(1);
        }

        Pattern classPattern = Pattern.compile("class\\s+([a-zA-Z][a-zA-Z0-9_]*)");
        var classMatcher = classPattern.matcher(sourceContent);
        if (!classMatcher.find()) {
            throw new IllegalStateException("Class declaration not found in source file");
        }
        String className = classMatcher.group(1);

        return packageName.isEmpty() ? className : packageName + "." + className;
    }

    private void compileSourceFile(String sourceFilePath) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IllegalStateException("System Java Compiler not available. Are you running with JDK instead of JRE?");
        }

        int compilationResult = compiler.run(null, null, null,
                rootDir + sourceFilePath,
                "-d", rootDir + OUTPUT_DIR);

        if (compilationResult != 0) {
            throw new RuntimeException("Compilation failed with result code: " + compilationResult);
        }
    }

    private byte[] loadClassBytes(String dynamicClassName) throws Exception {
        Path classFilePath = Paths.get(rootDir + OUTPUT_DIR, dynamicClassName.replace('.', '/') + ".class");
        return Files.readAllBytes(classFilePath);
    }

    private Object createHiddenClassInstance(byte[] classBytes) throws Exception {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        Class<?> hiddenClass = lookup.defineHiddenClass(classBytes, true, MethodHandles.Lookup.ClassOption.NESTMATE)
                .lookupClass();
        return hiddenClass.getConstructor().newInstance();
    }

    public DynamicLoader(String rootDir) {
        this.rootDir = rootDir;
    }

    public DynamicLoader() {
        rootDir = "";
    }
}
