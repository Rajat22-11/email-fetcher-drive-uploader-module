# Email Fetcher & Google Drive Uploader Module

A lightweight, automated Spring Boot batch utility designed to fetch financial updates from Gmail based on specific labels and senders, and securely store them as raw `.eml` files inside designated Google Drive folders.

---

# Overview

This module is designed to automate the collection and archival of financial alert emails (such as Screener.in updates) from a personal Gmail account and upload them into organized Google Drive folders for downstream processing.

The application uses Google's OAuth 2.0 Desktop Application flow and follows a Refresh Token-based authentication strategy, enabling fully automated, headless execution in both local and cloud environments.

---

# OAuth2 Authentication Architecture

When using a personal Gmail account, Service Accounts cannot access the inbox directly. Instead, the application operates as an OAuth2 Desktop Client and acts on behalf of the authenticated user.

```text
+-----------------------------------------------------------------------------------+
|                                 YOUR LOCAL COMPUTER                               |
|                                                                                   |
|  [Spring Boot App] ----(1. Reads)----> [client_secret.json]                      |
|         |                                  (Client ID & Secret Identity)          |
|         |                                                                         |
|         +----------(2. Checks for Token)----> [tokens/StoredCredential]           |
|         |                                         |                               |
|         |--[ IF FOUND: Silent Background Login ]--+                               |
|         |                                                                         |
|         +--[ IF NOT FOUND: One-Time Manual Login Setup ]                          |
|                  |                                                                |
|                  v                                                                |
|         (Opens Web Browser) ---> [User Logs In & Clicks 'Allow']                  |
|                                                  |                                |
|   [Spring Boot App] <---(Receives Auth Code)-----+                                |
|         |                                                                         |
|         v                                                                         |
|   (Exchanges Code for Permanent Refresh Token)                                    |
|         |                                                                         |
|         v                                                                         |
|   (Saves Token to [tokens/StoredCredential] for future headless runs)             |
+---------|-------------------------------------------------------------------------+
          |
          | (3. Sends Refresh Token + Client Secret)
          v
+------------------------+      (4. Issues Short-Lived)      +----------------------+
|  Google Auth Platform  | --------------------------------> |  Spring Boot App     |
|  (OAuth 2.0 Server)    | <-------------------------------- |  Memory Session      |
|                        |       Temporary Access Token      +----------------------+
+------------------------+                                              |
                                                                        |
                                                                        | (5. Authorized API Calls)
                                                                        v
                                                             +----------------------+
                                                             |  Google API Gateway  |
                                                             +----------------------+
                                                                /                \
                                                               /                  \
                                                              v                    v
                                                    [Gmail Inbox]         [Google Drive Folder]
                                                    Fetch Emails          Store Raw .eml Files
```

---

# How OAuth2 Works

## 1. Application Identity (`client_secret.json`)

The `client_secret.json` file does **not** provide access to your Gmail account.

Instead, it defines your application's identity to Google's OAuth infrastructure.

### Key Parameters

#### client_id

A public identifier for your application.

Think of it as the username of your Spring Boot application.

#### client_secret

A private cryptographic key shared only between Google and your application.

Used to verify that requests are coming from your registered application.

#### auth_uri

Google authorization endpoint.

Users are redirected here during the initial login flow.

#### token_uri

Google token endpoint.

Used to exchange authorization codes and refresh tokens for active access tokens.

---

## 2. Refresh Token Strategy

Access Tokens expire approximately every 60 minutes.

Without a refresh token, users would need to manually log in repeatedly.

To enable background automation, the application requests:

```text
access_type=offline
```

during the initial OAuth flow.

Google then issues:

* Access Token (short-lived)
* Refresh Token (long-lived)

The refresh token is stored locally inside:

```text
tokens/StoredCredential
```

During every future application startup:

1. Application reads the stored refresh token.
2. Sends it securely to Google's token endpoint.
3. Receives a fresh access token.
4. Executes Gmail and Drive API operations.

Result:

* No browser prompts
* No manual intervention
* Fully automated background execution

---

# Features

* Gmail email retrieval using Gmail API
* Sender-based email filtering
* Label-based Gmail filtering
* Upload raw `.eml` messages to Google Drive
* OAuth2 Desktop Application authentication
* Refresh Token persistence
* Headless cloud execution
* Spring Boot batch-friendly architecture
* Secure secret isolation from source control

---

# Technical Stack

| Component      | Technology            |
| -------------- | --------------------- |
| Framework      | Spring Boot 4.x       |
| Language       | Java 21               |
| Build Tool     | Maven                 |
| Email API      | Gmail API v1          |
| Storage API    | Google Drive API v3   |
| Authentication | OAuth2 Desktop Client |
| Deployment     | Docker / Render       |

---

# Project Structure

```text
project-root/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   └── resources/
│   │       ├── application.yml
│   │       └── client_secret.json
│
├── tokens/
│   └── StoredCredential
│
├── pom.xml
├── Dockerfile
└── README.md
```

---

# Configuration

## application.yml

Configure runtime behavior using:

```yaml
google:
  enabled: true
  client-secret-file: classpath:client_secret.json
  tokens-directory-path: tokens
  application-name: Email Fetcher Drive Uploader

mail:
  label: "MyFinance/Screener"
  mappings:
    "[no-reply@screener.in]": "YOUR_TARGET_GOOGLE_DRIVE_FOLDER_ID"

screener:
  sender: no-reply@screener.in
  subject-contains: Screener.in Updates
  timezone: Asia/Kolkata
  days-back: 1
```

---

# Required Credential Files

For security reasons, credential files must never be committed to Git.

Both files should be excluded using `.gitignore`.

---

## A. OAuth Client Configuration

Download OAuth credentials from Google Cloud Console and save them as:

```text
src/main/resources/client_secret.json
```

Example:

```json
{
  "installed": {
    "client_id": "YOUR_CLIENT_ID.apps.googleusercontent.com",
    "project_id": "YOUR_PROJECT_ID",
    "auth_uri": "https://accounts.google.com/o/oauth2/auth",
    "token_uri": "https://oauth2.googleapis.com/token",
    "client_secret": "YOUR_CLIENT_SECRET"
  }
}
```

---

## B. Stored Refresh Token

After the first successful login, Google issues a refresh token which is automatically saved to:

```text
tokens/StoredCredential
```

This file enables future headless execution.

---

# First-Time Local Setup

## Step 1

Place `client_secret.json` inside:

```text
src/main/resources/
```

## Step 2

Start the application.

```bash
mvn spring-boot:run
```

## Step 3

A browser window opens automatically.

Log in using the target Gmail account and approve permissions.

## Step 4

The application exchanges the authorization code for:

* Access Token
* Refresh Token

## Step 5

Refresh token is persisted to:

```text
tokens/StoredCredential
```

Future runs require no user interaction.

---

# Headless Deployment (Render / Cloud)

For cloud deployments:

1. Keep both credential files out of Git.
2. Build and deploy the application normally.
3. Mount credential files as secret files within the hosting platform.

Required mounted files:

### File 1

```text
src/main/resources/client_secret.json
```

Contents:

```text
Raw contents of your OAuth client configuration
```

### File 2

```text
tokens/StoredCredential
```

Contents:

```text
Refresh token generated during local setup
```

Once mounted:

* Application boots successfully
* Refresh token is exchanged automatically
* Gmail is scanned
* Emails are uploaded to Drive
* No browser interaction is required

---

# GitHub Actions Cron Setup

This module now exposes two HTTP endpoints for external automation:

```text
GET  /health
POST /api/cron/fetch-screener
```

The repository includes a daily GitHub Actions workflow at:

```text
.github/workflows/daily-cron.yml
```

To use it, add this repository secret in GitHub:

```text
RENDER_BASE_URL=https://your-render-service.onrender.com
```

The workflow pings `/health` first to wake the service, then calls `/api/cron/fetch-screener` to run the Gmail-to-Drive sync.

---

# Security Recommendations

Never commit:

```text
client_secret.json
tokens/StoredCredential
```

Recommended `.gitignore` entries:

```gitignore
src/main/resources/client_secret.json
tokens/
```

Additional recommendations:

* Rotate OAuth credentials if leaked.
* Restrict OAuth scopes to minimum required permissions.
* Store secrets using cloud secret management solutions.
* Avoid embedding credentials inside Docker images.

---

# Execution Lifecycle

```text
Application Starts
        |
        v
Load Configuration
        |
        v
Read StoredCredential
        |
        v
Request Fresh Access Token
        |
        v
Connect Gmail API
        |
        v
Filter Emails by Label + Sender
        |
        v
Download Raw Email Content
        |
        v
Upload .eml File to Google Drive
        |
        v
Complete Execution
```

---

# Future Enhancements

* Multiple sender mappings
* Dynamic folder routing
* Email attachment extraction
* PDF report generation
* PostgreSQL audit logging
* Scheduled execution via Spring Scheduler
* Kubernetes CronJobs support
* Automated Screener.in stock analysis pipeline
* AI-powered financial report generation

---

# License

This project is intended for personal automation and educational purposes. Ensure compliance with Google's API Terms of Service and Gmail API usage policies before deploying to production.
