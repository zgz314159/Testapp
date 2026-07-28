# CI Debug Signing

`testapp-ci-debug.keystore.b64` is a public **debug-only** signing key.

Its purpose is to keep locally built and GitHub Actions `debug` APK signatures stable, so later test APKs can update the installed debug app instead of triggering `INSTALL_FAILED_UPDATE_INCOMPATIBLE`.

Rules:

- Debug application ID: `com.zgz314159.testapp.debug`
- Release application ID: `com.zgz314159.testapp`
- Never use this key for release builds or production distribution.
- Release signing continues to use `signing.properties` or CI environment variables.
- The Gradle script decodes this Base64 file into `build/ci-signing/`, which is generated output and must not be committed.
