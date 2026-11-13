# Railway CSV WhatsApp Bulk Sender

Java 17 + Spring Boot app that presents an HTML CSV-upload form and forwards the parsed rows to your existing bulk-text API.

**Default target API**

`POST https://kskmessagingservice-production.up.railway.app/api/whatsapp/send/bulk/text`

## CSV format

```csv
to,body,previewUrl
918008977520,"Hello from CSV",true
918523875047,"Visit https://example.com",true
```

Headers are case-insensitive. `to`, `body`, and `previewUrl` must be present. The app always sends `previewUrl` as boolean `true` even if the CSV cell differs. Phone numbers should include the country code, e.g. `918001234567`, and contain digits only (an optional leading `+` is stripped).

## Run locally

Requires JDK 17 and Maven 3.8+.

```bash
mvn clean package
mvn spring-boot:run
```

Open http://localhost:8080, select the CSV and click **Send bulk messages**.

## How it works

- HTML form posts the file as multipart field `file` to `POST /api/csv/send-bulk`.
- The backend validates the headers and each row.
- It builds `{ "messages": [ { "to": "...", "body": "...", "previewUrl": true } ] }`.
- It makes a server-to-server POST to the configured bulk API and shows the upstream status and response.

## Configuration

- `PORT` defaults to `8080`.
- `WHATSAPP_BULK_API_URL` overrides the default destination.
- `MAX_CSV_ROWS` defaults to `5000`.
- Upload size limit is 10 MB.

## Deploy on Railway

Push the project to GitHub, then in Railway select **New Project → Deploy from GitHub repo**. Railway should detect the Maven/Spring Boot app. Generate a public domain under the service's Settings / Networking.

## Production security note

This starter app has no authentication. Do not expose it publicly without adding login/authorization and rate limits: anyone who can reach it could trigger bulk sends. Store any credentials as Railway variables, never in public frontend code. Send only to opted-in recipients and comply with Meta WhatsApp policies. This app does not automatically retry failed sends.
