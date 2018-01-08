package com.github.onsdigital.elastic.importer.handlertasks;

import com.github.onsdigital.fanoutcascade.handlertasks.HandlerTask;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author sullid (David Sullivan) on 08/01/2018
 * @project dp-elastic-parallel-importer
 */
public class FileReaderTask extends HandlerTask {

    private List<String> fileNames;

    public FileReaderTask(List<String> fileNames) {
        super(FileReaderTask.class);
        this.fileNames = fileNames;
    }

    public List<String> getFileNames() {
        return fileNames;
    }

    public List<File> getFiles() {
        List<File> files = new ArrayList<>();
        for (String fileName : this.fileNames) {
            files.add(new File(fileName));
        }

        return files;
    }
}
