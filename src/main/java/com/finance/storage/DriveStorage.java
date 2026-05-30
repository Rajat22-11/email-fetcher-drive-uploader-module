package com.finance.storage;

import java.io.IOException;

public interface DriveStorage {
    /**
     * Save an eml file to provided folderPath. Implementations may be local disk or Google Drive.
     * @param folderPath target folder (local path or drive identifier depending on impl)
     * @param filename name of the file including extension
     * @param content raw bytes of the .eml file
     */
    void saveEml(String folderPath, String filename, byte[] content) throws IOException;
}
