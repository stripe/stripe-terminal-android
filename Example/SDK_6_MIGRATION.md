# Terminal SDK 6.0.0 migration diary

Date: 2026-09-24. Application: this repository's Kotlin and Java Android examples,
starting from SDK 5.8.0 at commit `ceb286a14020f919d78ef1eee244b1396a7e964e`.
Scope: upgrade the existing flows using the locally downloaded 6.0.0 release
candidate; record build results, documentation gaps, and remaining device tests.

Sources reviewed:

- [SDK v6 migration guide](https://docs.google.com/document/d/1lG0QMKHYUEoX0uqxW18Eg1bXJARjGHJ7M5e3kNkBHdI/edit)
- [Terminal SDK v6 Dogfooding](https://docs.google.com/document/d/1ujSwCna9qtJv05VlyzMR02WyEhoViPnv81cEmbnBvJ8/edit)

## Changes applied

| Area | Migration in these apps |
| --- | --- |
| Dependencies | Both apps use `stripeterminal-core` and `stripeterminal-taptopay` 6.0.0; Kotlin also uses `stripeterminal-ktx` 6.0.0. Maven local is searched first, limited to `com.stripe:stripeterminal.*` modules. |
| Build toolchain | Kotlin 1.9.25 → 2.3.21; AGP 8.8.2 → 8.13.2. Existing Gradle 8.14.4, compile/target SDK 35, and minimum SDK 26 retained. |
| Build memory | Gradle heap increased from 1536 MB to 4096 MB after the combined release/lint build stopped due to garbage-collector thrashing. Validation limits workers to two. The pre-existing backend URL edit is preserved. |
| Initialization | Java now supplies `LocaleConfig.HardcodedLocale.Builder("en-US").build()`, matching the Kotlin app's existing locale choice. |
| Setup intents | Both apps pass `AllowRedisplay.ALWAYS` to `CollectSetupIntentConfiguration.Builder`; removed the separate argument to `processSetupIntent`, including the Kotlin coroutine wrapper. The existing consent choice is preserved. |
| Refunds | Kotlin uses `ProcessRefundParameters.ByPaymentIntentId` with the existing `processRefund` coroutine API. Neither app used the removed collect/confirm refund flow. Refund IDs are not dereferenced or used as storage keys. |
| Charges | Kotlin ledger details now read `latestCharge` instead of `getCharges().firstOrNull()`, retaining null-safe display behavior when the charge is not expanded. |
| Capture | Both payment builders explicitly select `CaptureMethod.Manual`. Kotlin exposes capture in its ledger and captures forwarded offline payments; Java captures through the backend after processing. Extended/incremental authorization options also need manual capture. |
| Tap to Pay | Both manifests explicitly declare `MODIFY_AUDIO_SETTINGS`. Existing Tap to Pay connections already use `TapUseCase.Pay`, including Java Easy Connect. |
| Reflection | Added `@Keep` to each app's Gson `ConnectionToken` response model so shrinking cannot remove fields or constructors accessed through reflection. No blanket app keep rules were added. |

## Documentation feedback and additional findings

1. **Build-tool compatibility needs an AGP/R8 step.** The guide mentions Kotlin
   2.3.21 and compile SDK 35, but omits the shrinker compatibility requirement.
   [AGP 8.13.2 release notes](https://developer.android.com/build/releases/past-releases/agp-8-13-0-release-notes)
   explicitly add Kotlin 2.3 support through R8 8.13.19. Upgrading the example's
   Kotlin plugin alone leaves its old AGP/R8 pairing behind. Document a compatible
   AGP/Gradle/JDK combination, including for Java consumers of Kotlin SDK artifacts.
   This migration used AGP 8.13.2, Gradle 8.14.4, and JDK 21.0.10.
2. **Minified validation needs sufficient build memory.** Both debug apps built,
   but the first combined release/lint run with the checked-in 1.5 GB heap stopped
   with `Gradle build daemon has been stopped: since the JVM garbage collector is
   thrashing`. The retry uses a 4 GB heap and two workers. This is an observed
   build-workflow issue; it is not evidence of a runtime SDK memory regression.
3. **Clarify refund spelling and coroutine examples.** Android section 4 says
   `interact_present`; the payment-method name is `interac_present`. The guide
   shows callback APIs; the public Kotlin example uses the KTX wrappers. Add a
   KTX example showing `processRefund(parameters = ..., collectConfig = ...)`
   and `processSetupIntent(intent = ..., collectConfig = ...)` with redisplay
   configured in the builder. These calls compile against the local candidate.
4. **Explain the practical reflection audit.** The narrower SDK rules are present
   in the artifacts: the core/external rules remove broad keeps for packages such
   as `com.squareup.**`, `kotlin.**`, and `okio.**` compared with cached 5.8.0.
   App-owned Gson models still need their own retention rules. The connection-token
   models were protected explicitly here. A successful R8 build alone does not
   validate token deserialization or other reflection at runtime.
5. **Record an immutable RC identifier.** The local POMs identify `6.0.0` but do not
   identify a release-candidate source commit. Checksums below identify the tested
   files. The dogfooding setup should provide a candidate revision/checksum so
   reports from different downloads of the same version can be compared.
6. **Backend setup remains necessary for transaction testing.** The existing
   `EXAMPLE_BACKEND_URL` points to `http://0.0.0.0:4567`. That is not the host
   machine's address from an emulator. Use `http://10.0.2.2:4567` for a host backend
   from an Android emulator, or an appropriate reachable address for hardware.
   Release builds do not include the debug manifest's cleartext-traffic allowance;
   use HTTPS for release transaction tests. The URL has not been changed.
7. **R8 emits a warning from dependency rules.** Both release builds report
   `Rule precondition matches static final fields javac has inlined` at
   `META-INF/proguard/gson.pro:67`, matching six
   `com.visa.vac.tc.emvconverter.Constants` string constants. R8 recommends adding
   `!static` to the rule. Builds and the startup checks below pass, but the SDK team
   should review the Gson rule/card-kernel interaction. No SDK dependency rules
   were suppressed or modified in this migration.

## Coverage and remaining work

| Migration-guide area | Status / follow-up |
| --- | --- |
| Versions, initialization, setup/refund API names, latest charge, capture method | Updated and compiled in both applicable apps. Payment outcomes still require the runtime checks below. |
| `ReaderPaymentInteractionListener` | Neither example implemented the removed QR/selection callbacks or exposes a non-card payment flow. No QR/selection UI was added. Before enabling payment methods that require it, implement both interaction types and pass the listener to every Bluetooth/USB connection. Verify success, cancellation, and UI-display failure callbacks. Without it, such transactions fail as documented. |
| Nullable refund ID | No unsafe `Refund.id` accesses found. The repository stores refund objects and associates them by payment intent, not refund ID. Exercise a response with a null ID before adding new ID-based UI/storage. |
| Invoice, setup-attempt names, split card-present types, `ReaderSupportResult` | No removed-type/property references found. Existing card-detail property accesses compile with the new owning model types. |
| Reader cancellation and low battery | Error screens already display SDK error codes distinctly, without matching localized strings. No `PRINTER_LOW_BATTERY` branch exists. Exercise `CANCELED`, `CANCELED_BY_READER`, and `READER_BATTERY_LOW` on appropriate readers. |
| Tap to Pay UX / saved state | No custom `TapToPayUxConfiguration` or Parcelable storage exists. Default UI now follows the SDK's system-theme default; verify light/dark mode on a supported Tap to Pay device. |
| Tap to Pay update advice and session expiration | `onUpdateRequirementsAvailable` is not implemented. Add an advisory UI if desired; test `SESSION_EXPIRED` recovery by reconnecting. Kotlin shows connection errors, but Java's normal discovery failure callback currently navigates back without displaying the error. This existing Java behavior should be improved for useful dogfooding feedback. |
| Reader-update simulation | No `SimulateReaderUpdate` or removed simulator `update` property is used. Add `testReaderUpdate` only to test-mode Bluetooth/USB connections to exercise low-battery, available/required update, and cancellation scenarios. Never enable it for live-mode connections. |
| Stored offline payments | Not validated by compilation or a fresh-install startup test. Install this build over a 5.8.0 installation with representative queued offline payments, preserving package ID/signing key and app data. Verify upload, capture, ledger reconciliation, and restart/reconnect behavior. Do not uninstall or clear data for this test. |
| Payment/setup/refund runtime behavior | Still requires a reachable test-mode backend and relevant simulated/physical readers. Check manual authorization then capture, CAD/Interac refund, saved-card setup, extended/incremental authorization, and offline forwarding. |
| Physical-reader and Tap to Pay behavior | Startup on an emulator cannot validate Bluetooth/USB connections, reader UI cancellation, hardware battery/update errors, Tap to Pay attestation, or payment audio. |

## Validation

Commands run from `Example` (`gradlew` lacks an executable bit in this checkout):

```sh
bash gradlew :kotlinapp:assembleDebug :javaapp:assembleDebug \
  :kotlinapp:assembleRelease :javaapp:assembleRelease \
  :kotlinapp:lintDebug :javaapp:lintDebug \
  --max-workers=2 --continue --console=plain
git diff --check
```

| Check | Result |
| --- | --- |
| Kotlin and Java debug APKs | PASS |
| Kotlin and Java release APKs, with R8 minification and resource shrinking enabled | PASS; combined validation finished `BUILD SUCCESSFUL` with the 4 GB heap and two workers. |
| Kotlin debug lint | PASS, zero errors; 34 warnings and one hint. |
| Java debug lint | PASS, zero errors; 90 warnings, mostly missing Java nullness annotations. |
| Merged manifests, debug and release, both apps | PASS: `MODIFY_AUDIO_SETTINGS` present. |
| Resolved Terminal artifacts | PASS: all resolved Terminal modules are 6.0.0 AARs from Maven local, including transitive external/internal-common modules. |
| Emulator startup, both debug apps | PASS: installed with permissions granted; cold launches returned `Status: ok`. |
| Emulator startup, both minified release apps | PASS: temporary copies signed with the local debug key installed over the debug apps and cold-launched successfully; no `AndroidRuntime` crash entries observed. Project release signing configuration was not changed. |
| Minified Gson token deserialization, both release APKs | PASS: a temporary `app_process` harness loaded each APK with `PathClassLoader`, deserialized a synthetic `{"secret":"synthetic_smoke_test_value"}` response using the APK's Gson and `ConnectionToken` classes, and asserted `getSecret()` returned the value. This does not exercise the backend or connection-token API. |
| Whitespace check | `git diff --check` passes. |

Startup/reflection checks used a newly created, isolated Pixel 8a Android 16
(API 36, arm64) emulator. The emulator was stopped after validation. No physical
reader was modified and no payments, setups, or refunds were initiated.

Lint reports are generated at `kotlinapp/build/reports/lint-results-debug.html`
and `javaapp/build/reports/lint-results-debug.html`. Remaining warnings include
dependency-update suggestions, Java nullness, existing Bluetooth API deprecations,
Kotlin annotation-target migration advice, and Java 8 source/target warnings under
JDK 21. They do not block these builds. Full temporary build output is at
`/tmp/terminal-v6-validation.log` for this working session.

## Candidate artifact identity

Artifacts were already present under `~/.m2/repository/com/stripe/<module>/6.0.0/`.
The following SHA-256 hashes identify the AARs consumed by these examples:

| Module | SHA-256 |
| --- | --- |
| `stripeterminal-core` | `b022633d349f10ad1f223fc276beaff73b6ce81a84a0c11e437d483b75c6ce88` |
| `stripeterminal-taptopay` | `8e28c113f026062d013eb322ac26d70f9a1dc95b187f713803fa67cd063b9739` |
| `stripeterminal-ktx` | `4f77540f31d1fe0ab48c898a09b2b35ace28cdc7f039b614e58d04004c2c82f3` |
| `stripeterminal-external` (transitive) | `2f059d6e20aad8c958ca6da45456f52bc012693fbdeb4bbb98228c1494ec4075` |
| `stripeterminal-internal-common` (transitive) | `e3ba873ab4c8ccb100c2ea9e1451da810eca93492d4bd7018a9590a5133b37db` |

The aggregate `stripeterminal` and `stripeterminal-appsondevices` artifacts are
installed locally but are not dependencies of these two apps. This diary does not
claim coverage of the Apps on Devices integration or the iOS portions of the docs.

--------
### Human generated writings:
```

