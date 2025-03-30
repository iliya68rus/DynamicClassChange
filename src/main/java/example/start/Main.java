package example.start;

import example.DynamicInterface;
import example.DynamicLoader;

import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        DynamicLoader loader;
        if (args.length > 0) {
            String rootDir = args[0];
            System.out.println("Give argument rootDir=" + rootDir);
            loader = new DynamicLoader(rootDir);
        } else {
            loader = new DynamicLoader();
        }
        String sourceFile = "DynamicClass.java";

        for (int i = 0; i < 100; i++) {
            DynamicInterface obj = loader.loadDynamicClass(sourceFile);
            System.out.println(obj.getValue());
            TimeUnit.SECONDS.sleep(1);
        }
    }
}
