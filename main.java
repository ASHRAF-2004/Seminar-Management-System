public class main {
    public static void main(String[] args) {
        if (!compileSources()) {
            System.err.println("Failed to compile source files. Please check the errors above.");
            System.exit(1);
        }

        try {
            Class<?> mainClass = Class.forName("ui.Main");
            java.lang.reflect.Method entryPoint = mainClass.getMethod("main", String[].class);
            entryPoint.invoke(null, (Object) args);
        } catch (Exception exception) {
            System.err.println("Unable to launch ui.Main: " + exception.getMessage());
            exception.printStackTrace();
            System.exit(1);
        }
    }

    private static boolean compileSources() {
        javax.tools.JavaCompiler compiler = javax.tools.ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            System.err.println("No system Java compiler found. Are you running a JRE instead of a JDK?");
            return false;
        }

        java.util.List<String> sourceFiles = new java.util.ArrayList<>();
        java.util.List<String> roots = java.util.Arrays.asList("ui", "model", "repository", "service", "util");
        for (String root : roots) {
            java.nio.file.Path rootPath = java.nio.file.Paths.get(root);
            if (!java.nio.file.Files.exists(rootPath)) {
                continue;
            }
            try (java.util.stream.Stream<java.nio.file.Path> paths = java.nio.file.Files.walk(rootPath)) {
                paths.filter(path -> path.toString().endsWith(".java"))
                        .forEach(path -> sourceFiles.add(path.toString()));
            } catch (java.io.IOException exception) {
                System.err.println("Failed to read source directory: " + root + " (" + exception.getMessage() + ")");
                return false;
            }
        }

        if (sourceFiles.isEmpty()) {
            System.err.println("No source files found to compile.");
            return false;
        }

        java.util.List<String> compilerArgs = new java.util.ArrayList<>();
        compilerArgs.add("-classpath");
        compilerArgs.add(".");
        compilerArgs.addAll(sourceFiles);

        int result = compiler.run(null, null, null, compilerArgs.toArray(new String[0]));
        return result == 0;
    }
}
