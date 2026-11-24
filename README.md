# KSK Business Messaging Portal

Java 17 / Spring Boot 3.5 portal for business-scoped WhatsApp campaigns, template definitions and WhatsApp configuration stored in MongoDB.

## Run locally

Requirements: JDK 17 and Maven 3.8+.

Set the MongoDB connection as an environment variable (do not commit credentials):

```bash
# Windows PowerShell
$env:SPRING_DATA_MONGODB_URI="mongodb+srv://<user>:<password>@<cluster>/<database>?retryWrites=true&w=majority"
$env:KSK_MESSAGING_BASE_URL="https://<your-ksk-messaging-service>"
mvn clean package
mvn spring-boot:run
```

Open `http://localhost:8082`, register or sign in, then use the dashboard.

## 1. CSV image URLs for template headers

CSV supports either text or template rows. Template rows can include an optional `imageUrl` column:

```csv
to,templateName,languageCode,parameters,imageUrl
919876543210,order_update,en_US,Asha|ORD-123,https://cdn.example.com/order-banner.jpg
919876543211,order_update,en_US,Ravi|ORD-124,
```

- `to` must include country code; optional leading `+` is removed.
- `parameters` are body parameters separated by `|`.
- `imageUrl` is a public HTTPS image URL for a Meta template with an IMAGE header. It is stored with each message job and sent as the `image` property to `POST /api/whatsapp/send/template`.
- For text rows, use `to,body`.
- Maximum rows and upload size are controlled by `MAX_CSV_ROWS` and Spring multipart settings.

The same optional image URL is available in the Quick message template form.

## 2. Template builder

Dashboard → Business templates supports:

- Template name, language, category (UTILITY, MARKETING, AUTHENTICATION) and parameter format (positional or named)
- Header: none, text, image, video, document or location
- Body text with `{{1}}`, `{{2}}` placeholders and example values
- Optional footer
- Optional buttons stored as a JSON array. Common Meta button types include `QUICK_REPLY`, `URL`, `PHONE_NUMBER`, `COPY_CODE` and `OTP`.

Definitions are stored per business in `portal_business_templates`, including the normalized `components` structure, buttons, examples and timestamps. This portal saves the local template definition; it does not create, submit or approve a template in Meta Business Manager. Ensure the saved name, language and components match the approved Meta template before sending.

## 3. WhatsApp configuration editor

Dashboard → WhatsApp configuration edits the logged-in business's document in the `whatsapp_configurations` collection. The editor loads the document's existing fields, presents common Meta Cloud API settings (Graph URL/version, phone number ID, WABA ID, App ID/Secret, access token, webhook verify token and callback URL), and displays additional existing fields in a JSON-aware editor. Additional keys are preserved and saved as flat document fields alongside `businessId` and `updatedAt`.

The current portal archive did not include a formal schema for `whatsapp_configurations`; the editor therefore preserves additional keys dynamically instead of assuming they do not exist. The business document is selected by `businessId`.

## 4. Batch processing

The scheduler runs every five minutes by default and processes up to 5,000 pending jobs per run. Configure with `BATCH_CRON`, `BATCH_PAGE_SIZE`, `BATCH_MAX_ATTEMPTS`, `BATCH_RETRY_DELAY_SECONDS` and `BATCH_ENABLED`.

## Environment variables

- `SPRING_DATA_MONGODB_URI` (required): MongoDB connection string.
- `KSK_MESSAGING_BASE_URL` (default `http://localhost:8080`): base URL of KSKMessagingService.
- `PORT` (default `8082`).
- `SECURE_COOKIE` (default `false`; set `true` behind HTTPS).
- `MAX_CSV_ROWS` (default `50000`).
- `BATCH_ENABLED`, `BATCH_CRON`, `BATCH_PAGE_SIZE`, `BATCH_MAX_ATTEMPTS`, `BATCH_RETRY_DELAY_SECONDS`.

## Important notes

- WhatsApp image URLs must be publicly reachable over HTTPS by Meta; localhost and private-network URLs will not work.
- Protect MongoDB credentials, App Secret and access tokens. The portal requires authentication for the configuration page and its APIs. Restrict database access and use HTTPS in production.
- The portal's template builder stores definitions locally; Meta approval still happens through Meta Business Manager / WhatsApp Manager or a separately integrated Meta template API.
- Send only to opted-in recipients and follow Meta's WhatsApp Business policies.


## Admin dashboard and business warranty controls

- Admin username is fixed as `KskMSAdmin`; configure its password with `ADMIN_PASSWORD` in the process environment. No admin password is stored in MongoDB. Set a strong secret before starting the service; startup fails if `ADMIN_PASSWORD` is blank.
- Admin dashboard: `/admin`. It lists businesses, toggles active status, updates warranty date, manages templates across businesses, and lists/adds/edits WhatsApp configurations. Deactivation also disables the associated business login and prevents queued batch jobs from being dispatched.
- `business_info` now includes `isActive` (new records default to true), `warrantyUpdatedAt`, and `updatedAt`. Legacy documents without `isActive` are treated as active by the Java primitive default.

## Business image upload

The user dashboard posts the selected image to the KSK Messaging Service endpoint `POST {KSK_MESSAGING_BASE_URL}/api/images/upload` as multipart form data using the `file` field. The URL returned by that API is saved to the latest matching document in the existing `portal_business_templates` collection (`imageUrl`, `updatedAt`). The image-upload page uses the same persistent sidebar as the main business dashboard. No direct S3/R2 SDK, credentials, or bucket configuration is used by this portal.

## CSV format

The sample file is `phoneNumber,message`. CSV uploads always queue `TEMPLATE` jobs and resolve the template name (`name`) and image URL (`imageUrl`, falling back to `headerMediaUrl`) from the latest template for that business in `portal_business_templates`. Upload is rejected if the business is inactive or has no template in that collection. The `message` value is stored as the job message and split on `|` into template body parameters.

## Admin dashboard

The admin sidebar links to Business info, Templates, WhatsApp configuration, and Logout. Admin template listing, creation, and editing all use the existing `portal_business_templates` collection. Business status and warranty date updates use the admin API with CSRF protection configured for the portal's AJAX header.
