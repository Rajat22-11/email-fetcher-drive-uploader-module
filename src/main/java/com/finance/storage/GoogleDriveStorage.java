package com.finance.storage;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.Collections;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "google", name = "enabled", havingValue = "true", matchIfMissing = true)
public class GoogleDriveStorage implements DriveStorage {
    private final Drive drive;


    @Override
    public void saveEml(String folderId, String filename, byte[] content) throws IOException {
        File metadata = new File()
                .setName(filename)
                .setMimeType("message/rfc822")
                .setParents(Collections.singletonList(folderId));

        var mediaContent = new ByteArrayContent("message/rfc822", content);
        File uploaded = drive.files().create(metadata, mediaContent)
                .setFields("id, name")
                .execute();

        log.info("Uploaded {} to Drive folder {} as file id {}", filename, folderId, uploaded.getId());
    }
}


