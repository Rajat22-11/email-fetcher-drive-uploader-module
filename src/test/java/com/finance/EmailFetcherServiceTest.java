package com.finance;

import com.finance.config.MailMappingConfig;
import com.finance.gmail.EmailMessage;
import com.finance.gmail.GmailClient;
import com.finance.service.EmailFetcherService;
import com.finance.storage.DriveStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailFetcherServiceTest {

    @Mock
    private GmailClient gmailClient;

    @Mock
    private DriveStorage driveStorage;

    private MailMappingConfig config;

    private EmailFetcherService service;

    @Captor
    ArgumentCaptor<byte[]> bytesCaptor;

    @BeforeEach
    void setUp() {
        config = new MailMappingConfig();
        config.setLabel("SCREENER");
        config.setMappings(Map.of("no-reply@screener.in", "drive-folder-id-123"));

        service = new EmailFetcherService(config, gmailClient, driveStorage);
    }

    @Test
    void processAll_withNoMappings_doesNothing() {
        config.setMappings(Map.of());

        service.processAll();

        verifyNoInteractions(gmailClient, driveStorage);
    }

    @Test
    void testProcessAll_savesFetchedEmlsToDriveStorage() throws Exception {
        byte[] sample = "raw-eml-bytes".getBytes();
        EmailMessage msg = new EmailMessage("abc123", sample);

        when(gmailClient.fetchMessagesByLabelAndFrom("SCREENER", "no-reply@screener.in"))
                .thenReturn(List.of(msg));

        service.processAll();

        verify(driveStorage, times(1)).saveEml(eq("drive-folder-id-123"), eq("abc123.eml"), bytesCaptor.capture());
        assertArrayEquals(sample, bytesCaptor.getValue());
    }

    @Test
    void processAll_withMultipleMessages_uploadsEach() throws Exception {
        config.setMappings(Map.of("no-reply@screener.in", "drive-folder-id-123"));
        when(gmailClient.fetchMessagesByLabelAndFrom("SCREENER", "no-reply@screener.in"))
                .thenReturn(List.of(
                        new EmailMessage("msg-1", "a".getBytes()),
                        new EmailMessage("msg-2", "b".getBytes())
                ));

        service.processAll();

        verify(driveStorage).saveEml("drive-folder-id-123", "msg-1.eml", "a".getBytes());
        verify(driveStorage).saveEml("drive-folder-id-123", "msg-2.eml", "b".getBytes());
        verifyNoMoreInteractions(driveStorage);
    }

    @Test
    void processAll_withMultipleSenders_routesToMatchingFolders() throws Exception {
        config.setMappings(Map.of(
                "no-reply@screener.in", "drive-folder-id-123",
                "support@github.com", "drive-folder-id-456"
        ));
        when(gmailClient.fetchMessagesByLabelAndFrom("SCREENER", "no-reply@screener.in"))
                .thenReturn(List.of(new EmailMessage("msg-1", "a".getBytes())));
        when(gmailClient.fetchMessagesByLabelAndFrom("SCREENER", "support@github.com"))
                .thenReturn(List.of(new EmailMessage("msg-2", "b".getBytes())));

        service.processAll();

        verify(driveStorage).saveEml("drive-folder-id-123", "msg-1.eml", "a".getBytes());
        verify(driveStorage).saveEml("drive-folder-id-456", "msg-2.eml", "b".getBytes());
    }

    @Test
    void processAll_whenGmailThrows_continuesOtherMappings() throws Exception {
        config.setMappings(Map.of(
                "bad@sender.com", "drive-folder-id-1",
                "ok@sender.com", "drive-folder-id-2"
        ));
        when(gmailClient.fetchMessagesByLabelAndFrom("SCREENER", "bad@sender.com"))
                .thenThrow(new RuntimeException("boom"));
        when(gmailClient.fetchMessagesByLabelAndFrom("SCREENER", "ok@sender.com"))
                .thenReturn(List.of(new EmailMessage("msg-2", "b".getBytes())));

        service.processAll();

        verify(driveStorage).saveEml("drive-folder-id-2", "msg-2.eml", "b".getBytes());
    }

    @Test
    void processAll_whenDriveFails_continuesNextMessage() throws Exception {
        config.setMappings(Map.of("no-reply@screener.in", "drive-folder-id-123"));
        when(gmailClient.fetchMessagesByLabelAndFrom("SCREENER", "no-reply@screener.in"))
                .thenReturn(List.of(
                        new EmailMessage("msg-1", "a".getBytes()),
                        new EmailMessage("msg-2", "b".getBytes())
                ));
        doThrow(new IOException("disk"))
                .when(driveStorage)
                .saveEml("drive-folder-id-123", "msg-1.eml", "a".getBytes());

        service.processAll();

        verify(driveStorage).saveEml("drive-folder-id-123", "msg-1.eml", "a".getBytes());
        verify(driveStorage).saveEml("drive-folder-id-123", "msg-2.eml", "b".getBytes());
    }
}
