package com.github.onsdigital.elastic.importer.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author sullid (David Sullivan) on 18/12/2017
 * @project dp-elastic-importer
 */
public class FileScanner {

    private String baseDir;
    private List<String> fileNames;

    public FileScanner(String baseDir) {
        this.baseDir = baseDir;
        this.fileNames = new ArrayList<>();
    }

    private void scan(File directory) throws IOException {
        // Scans all directories for json files
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                this.scan(file);
            } else if (file.getName().equals("data.json")) {
                this.fileNames.add(file.getAbsolutePath());
            }
        }
    }

    private void scan() throws IOException {
        File baseDirectory = new File(this.baseDir);
        if (!baseDirectory.isDirectory()) {
            throw new IOException("Base directory provided is not a directory");
        }
        this.scan(baseDirectory);
    }

    private File[] subDirectories(File currentDirectory) {
        return currentDirectory.listFiles(File::isDirectory);
    }

    public String getBaseDir() {
        return baseDir;
    }

    public List<String> getFiles() throws IOException {
        if (this.fileNames.size() == 0) {
            this.scan();
        }
        return fileNames;
    }

    public static void main(String[] args) {
        String zebedeeRoot = System.getenv("zebedee_root");
        String dataDirectory = String.format("%s/zebedee/master/", zebedeeRoot);

        FileScanner scanner = new FileScanner(dataDirectory);
        try {
            List<String> fileNames = scanner.getFiles();
            System.out.println(fileNames.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
