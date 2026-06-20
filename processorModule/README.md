# Processor Module

This folder documents the next module in the pipeline: a Drive-to-content processor that picks up yesterday’s `.eml` file from Google Drive, extracts the message content, and passes it into the existing processing logic.

The module is intended to run on a daily schedule and reuse the same Google OAuth configuration, token storage, and secret-file layout as the current email-fetcher module.

The extraction logic is already available in the codebase. This module is focused on the orchestration layer:

- find the target `.eml` file for yesterday
- download or read the raw EML payload from Google Drive
- hand the payload to the existing content extraction flow
- persist or forward the extracted output to downstream processing

---

## Module architecture

```text
GitHub Actions cron / Render wake-up
        |
        v
Processor service startup or cron endpoint
        |
        v
Load shared Google OAuth credentials
        |
        v
Resolve yesterday’s Drive file metadata
        |
        v
Download raw `.eml` bytes from Google Drive
        |
        v
Pass bytes to the existing content extraction logic
        |
        v
Emit structured email content + metadata
        |
        v
Store / analyze / forward the processed output
```

---

## What this module is responsible for

1. **Locate the Drive file**
   - Search the configured Google Drive folder
   - Select the file created for yesterday’s run
   - Prefer a deterministic naming rule or date filter so the same file is always selected

2. **Fetch the raw EML content**
   - Read the file as `message/rfc822`
   - Keep the downloaded bytes unchanged so the extractor can work on the original EML structure

3. **Run the content extraction logic**
   - Reuse the parsing/extraction code that already exists in the repository
   - Keep this module thin and orchestration-only

4. **Forward processed output**
   - Store the extracted body/content, metadata, and any derived fields
   - Keep the processing step idempotent so re-runs do not duplicate work

---

## High-level flow

```text
Google Drive folder
        |
        v
Find yesterday’s .eml file
        |
        v
Download raw EML bytes
        |
        v
Existing content extraction logic
        |
        v
Processed email content
        |
        v
Downstream storage / analysis / reporting
```

---

## Expected configuration

The processor module should reuse the same configuration style as the current application.

### Existing shared keys

```yaml
google:
  enabled: true
  client-secret-file: classpath:client_secret.json
  tokens-directory-path: tokens
  application-name: Email Fetcher Drive Uploader
```

### Suggested processor keys

These keys can be added when the processor module is implemented:

```yaml
processor:
  enabled: true
  timezone: Asia/Kolkata
  lookback-days: 1
  source-folder-id: YOUR_GOOGLE_DRIVE_FOLDER_ID
  file-extension: .eml
  expected-mime-type: message/rfc822
  processed-state-path: processed
```

### Cron trigger config

If the processor module is exposed through the same Render deployment pattern as the current app, the GitHub Actions workflow can reuse the same `RENDER_BASE_URL` secret and call a processor-specific endpoint after the container wakes up.

Recommended daily schedule for **7:30 AM IST**:

```text
0 2 * * *
```

Expected flow for the cron job:

1. Ping `/health` to wake the service.
2. Call the processor endpoint that kicks off the yesterday-file lookup and extraction.
3. Exit cleanly if no file is found so the run stays idempotent.

---

## Current authentication architecture

The processor module should **reuse the same Google OAuth 2.0 architecture** that powers the current app.

### Same credentials, same format

Use the same credential files and keys already used by the current module:

- `src/main/resources/client_secret.json`
- `tokens/StoredCredential`
- `google.client-secret-file`
- `google.tokens-directory-path`
- `google.application-name`

### How it works today

- The application uses a **Google OAuth Desktop / Installed App flow**
- The first local run authorizes the account once
- Google returns a long-lived **refresh token**
- The refresh token is stored in the shared `tokens/` directory
- Later runs execute headlessly without prompting for login

### Runtime locations already supported by the current app

- **Local development**: classpath `client_secret.json` and local `tokens/`
- **Render / cloud deployment**: mounted secret file at `/etc/secrets/client_secret.json`
- **Render token storage**: `/app/tokens`

This means the processor module can follow the same deployment pattern without introducing a new authentication model.

> Note: if the processor must read Drive files that were not created by this same OAuth client, verify the Drive scope is sufficient for your use case before deploying.

---

## Suggested implementation notes

- Keep the picker logic date-aware and timezone-aware
- Filter for yesterday using `Asia/Kolkata` unless a different timezone is configured
- Prefer one source of truth for the folder ID and file naming convention
- Add duplicate prevention so a file is processed once
- Keep extraction pure: input should be raw EML bytes, output should be structured content
- Expose the processor as a cron-friendly endpoint so GitHub Actions can trigger it with the same base URL secret used by the current module

---

## Recommended folder intent

```text
processorModule/
└── README.md
```

As the module is implemented, this folder can also hold any future module-specific notes, examples, or configuration references.

---

## Developer checklist

- [ ] Reuse existing Google OAuth credentials
- [ ] Resolve yesterday’s `.eml` file from Drive
- [ ] Download raw EML bytes without modification
- [ ] Pass bytes into existing extraction logic
- [ ] Persist processed output
- [ ] Prevent duplicate re-processing


