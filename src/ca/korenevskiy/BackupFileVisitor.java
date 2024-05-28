package ca.korenevskiy;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class BackupFileVisitor extends SimpleFileVisitor<Path> {

    private final Path sourceDirectoryFilepath;
    private final Path destinationDirectoryFilepath;
    private ChecksumRegistry checksumRegistry;


    public static BackupFileVisitor getInstance(Path sourceDirectoryFilepath, Path destinationDirectoryFilepath,
                                                ChecksumRegistry checksumRegistry) {
        return new BackupFileVisitor(sourceDirectoryFilepath, destinationDirectoryFilepath, checksumRegistry);
    }


    public static BackupFileVisitor getInstance(String sourceDirectoryFilename, String destinationDirectoryFilepath,
                                                ChecksumRegistry checksumRegistry) {
        return getInstance(Path.of(sourceDirectoryFilename), Path.of(destinationDirectoryFilepath), checksumRegistry);
    }


    private BackupFileVisitor(Path sourceDirectoryFilepath, Path destinationDirectoryFilepath,
                              ChecksumRegistry checksumRegistry) {
        this.sourceDirectoryFilepath = sourceDirectoryFilepath;
        this.destinationDirectoryFilepath = destinationDirectoryFilepath;
        this.checksumRegistry = checksumRegistry;
    }


    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        createDestinationDirectoryIfNotExists(dir);

        return FileVisitResult.CONTINUE;
    }


    private void createDestinationDirectoryIfNotExists(Path sourceDirectory) throws IOException {
        Path destinationDirectory = getDestinationPath(sourceDirectory);

        if (!Files.exists(destinationDirectory)) {
            Files.createDirectory(destinationDirectory);
        }
    }


    private Path getDestinationPath(Path sourcePath) {
        Path relativePath = getRelativePathFromFile(sourcePath);
        return destinationDirectoryFilepath.resolve(relativePath);
    }


    private Path getRelativePathFromFile(Path file) {
        return sourceDirectoryFilepath.relativize(file);
    }


    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

        if (checksumRegistry.shouldSourceFileBeCopied(file)) {
            System.out.println("Copying file: " + file.toString());
            copyFile(file);
        }
        return FileVisitResult.CONTINUE;
    }


    private void copyFile(Path sourceFile) {

        Path destinationFile = getDestinationPath(sourceFile);
        try {
            Files.copy(sourceFile, destinationFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.out.println("Exception while copying file. Caused by " + e.getMessage());
            throw new RuntimeException(e);
        }

    }
}
