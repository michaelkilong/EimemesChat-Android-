# EimemesChat AI — Android

Native Android client for EimemesChat AI, built with Jetpack Compose.

## Setup

1. Copy your `google-services.json` into `app/`
2. Open in Android Studio
3. Update `app/src/main/res/values/strings.xml` with your `default_web_client_id`
4. Build & run

## CI/CD

- Push to `main` → debug APK uploaded as GitHub Actions artifact
- Tag `v*` → release APK attached to GitHub Release

### GitHub Secrets required for release builds:
| Secret | Description |
|--------|-------------|
| `KEYSTORE_FILE` | Base64-encoded `.jks` keystore |
| `KEY_ALIAS` | Key alias |
| `KEY_PASSWORD` | Key password |
| `STORE_PASSWORD` | Keystore password |

## Architecture
- **UI:** Jetpack Compose + Material 3
- **Auth:** Firebase Auth (Google Sign-In)
- **Database:** Firestore (same project as web app)
- **AI Backend:** Vercel SSE endpoint (shared with web)
- **DI:** Hilt
