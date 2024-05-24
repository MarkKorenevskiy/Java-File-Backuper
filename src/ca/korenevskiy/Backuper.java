package ca.korenevskiy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Backuper {

    private final Path sourcePath;
    private final Path destinationPath;

    private Backuper(String sourceFilename, String destinationFilename) {
        this.sourcePath = Path.of(sourceFilename);
        this.destinationPath = Path.of(destinationFilename);
    }

    public static Backuper getInstance(String sourceFilename, String destinationFilename) {
        return new Backuper(sourceFilename, destinationFilename);
    }

    public void backupDirectory() {

        BackupChecksumRegistry checksumRegistry = new BackupChecksumRegistry(sourcePath, destinationPath);
        checksumRegistry.initializeChecksumRegistry();

        BackupFileVisitor fileVisitor = BackupFileVisitor.getInstance(sourcePath, destinationPath, checksumRegistry);

        try {
            Files.walkFileTree(sourcePath, fileVisitor);
        } catch (IOException e) {
            System.out.println("Error during file traversal: " + e);
        }

        checksumRegistry.saveChecksumMap();
    }

}
