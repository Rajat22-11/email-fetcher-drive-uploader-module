# Authentication Notes for the Processor Module

This document captures the authentication model that the processor module should follow.

The goal is to keep the processor module compatible with the current application so it can reuse the same Google account authorization, the same credential file format, and the same token storage layout.

---

## Existing authentication model

The current app uses a Google OAuth 2.0 **Installed App / Desktop Client** flow.

### Shared credential inputs

The processor module should keep using the same files and property keys:

- `src/main/resources/client_secret.json`
- `tokens/StoredCredential`
- `google.client-secret-file`
- `google.tokens-directory-path`
- `google.application-name`

### Shared behavior

- First-time local startup opens a browser for consent
- Google issues a refresh token after authorization
- The refresh token is saved locally for future runs
- Subsequent executions run headlessly using the saved token

---

## Current runtime resolution behavior

The existing app resolves credentials in two places:

### Local / development

- Loads `client_secret.json` from the classpath
- Persists tokens in the local `tokens/` directory

### Render / cloud deployment

- Reads the OAuth secret from `/etc/secrets/client_secret.json`
- Stores tokens in `/app/tokens`

The processor module should continue using the same pattern so no new credential management model is required.

---

## Scope and access considerations

The current application uses the following scopes:

- Gmail read access
- Google Drive file access

If the processor module only reads `.eml` files that were created by the current app, the existing authorization approach should remain aligned with the current design.

If you later need to read files outside the app-created file set, revisit the Drive scope before deploying the processor module.

---

## What not to change

To stay aligned with the current app, do **not** introduce:

- a different secret file format
- a new credential key naming scheme
- a separate token store convention
- a service-account-only auth path

The processor module should remain an extension of the same user-consented OAuth setup.

---

## Deployment reminder

Keep these values consistent across both modules:

```yaml
google:
  enabled: true
  client-secret-file: classpath:client_secret.json
  tokens-directory-path: tokens
  application-name: Email Fetcher Drive Uploader
```

If the processor module becomes a separate deployable service later, keep the same credential layout and only change module-specific processing settings.

