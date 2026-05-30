package com.finance.gmail;

import java.util.List;

/**
 * Abstraction over Gmail access so production code can use a real client while tests can mock behaviour.
 */
public interface GmailClient {
    /**
     * Fetches messages for the given Gmail label and sender (from address).
     * Returns a list of EmailMessage containing the raw EML bytes.
     */
    List<EmailMessage> fetchMessagesByLabelAndFrom(String label, String from) throws Exception;
}
