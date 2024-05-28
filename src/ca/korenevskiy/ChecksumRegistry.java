package ca.korenevskiy;

import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class ChecksumRegistry {

    private final Path sourceDirectoryPath;
    private final Path destinationDirectoryPath;
    private Map<String, String> checksumFileRegistry;

    private final String CHECKSUM_REGISTRY_FILENAME = ".registry.ser";


    public ChecksumRegistry(Path sourceDirectoryPath, Path destinationDirectoryPath) {
        this.sourceDirectoryPath = sourceDirectoryPath;
        this.destinationDirectoryPath = destinationDirectoryPath;
    }

    public void saveChecksumMap() {

        Path registryPath = destinationDirectoryPath.resolve(Path.of(CHECKSUM_REGISTRY_FILENAME));

        try (OutputStream os = Files.newOutputStream(registryPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
             ObjectOutputStream oos = new ObjectOutputStream(os)) {
            oos.writeObject(checksumFileRegistry);
            System.out.println("Saved registry to file.");
        } catch (IOException e) {
            System.out.println("I/O exception. Caused by " + e.getMessage());
        }

    }


    public boolean shouldSourceFileBeCopied(Path sourceFile) {

        if (isSourceFileChanged(sourceFile)) {
            updateRegistry(sourceFile);
            return true;
        }
        return false;
    }


    private boolean isSourceFileChanged(Path sourceFile) {

        String destinationFilepath = getDestinationPath(sourceFile).toString();
        String destinationChecksum = checksumFileRegistry.get(destinationFilepath);

        if (destinationChecksum == null) {
            return true;
        }

        String sourceChecksum = getFileChecksum(sourceFile);
        return !sourceChecksum.equalsIgnoreCase(destinationChecksum);
    }


    private void updateRegistry(Path sourceFile) {

        String checksum = getFileChecksum(sourceFile);
        String destinationFilepath = getDestinationPath(sourceFile).toString();
        checksumFileRegistry.put(destinationFilepath, checksum);
    }


    private String getFileChecksum(Path file) {

        String checksum = "";
        try (InputStream is = Files.newInputStream(file)) {

            byte[] data = is.readAllBytes();
            byte[] hash = MessageDigest.getInstance("MD5").digest(data);
            checksum = new BigInteger(1, hash).toString(16);

        } catch (IOException e) {
            System.out.println("I/O exception. Caused by: " + e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        return checksum;
    }


    private Path getDestinationPath(Path sourcePath) {
        Path relativePath = getRelativePathFromFile(sourcePath);
        return destinationDirectoryPath.resolve(relativePath);
    }


    private Path getRelativePathFromFile(Path file) {
        return sourceDirectoryPath.relativize(file);
    }


    public void initializeChecksumRegistry() {
        if (isRegistryExists()) {
            loadChecksumRegistryFromFile();
            return;
        }
        checksumFileRegistry = new HashMap<>();
    }


    private boolean isRegistryExists() {
        Path checksumFilePath = getChecksumFilepath();
        return Files.exists(checksumFilePath);
    }


    private Path getChecksumFilepath() {
        return destinationDirectoryPath.resolve(Path.of(CHECKSUM_REGISTRY_FILENAME));
    }


    @SuppressWarnings("unchecked")
    private void loadChecksumRegistryFromFile() {
        Path checksumFilePath = getChecksumFilepath();
        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(checksumFilePath))) {
            checksumFileRegistry = (Map<String, String>) ois.readObject();
            System.out.println("Loaded data from registry");
        } catch (IOException e) {
            System.out.println("I/O exception. Caused by " + e.getMessage());
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            System.out.println("Exception while reading data from registry. Caused by " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

}
