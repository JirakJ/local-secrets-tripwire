# Local Secrets Tripwire — Implementační plán

**Verze:** 2026.2.1
**Status:** Připraveno k implementaci

---

## Fáze 0: Plugin Ikona & Branding

- [ ] Navrhnout plugin ikonu (SVG + PNG)
  - [ ] Koncept: 🔐 Zámek s tripwire drátem
  - [ ] Hlavní ikona: 40×40 SVG pro Marketplace listing
  - [ ] Toolbar ikona: 16×16 + 13×13 SVG (light + dark varianta)
  - [ ] Tool window ikona: 13×13 SVG
  - [ ] `src/main/resources/META-INF/pluginIcon.svg` (40×40, light theme)
  - [ ] `src/main/resources/META-INF/pluginIcon_dark.svg` (40×40, dark theme)
  - [ ] Action ikony pro toolbar/menu (16×16 SVG, light + dark)
- [ ] Barevná paleta konzistentní s Security brandem
- [ ] Marketplace banner obrázek (1280×800)
- [ ] Screenshoty pro Marketplace listing (min 3, 1280×800 PNG)

## Fáze 1: Projekt scaffolding

- [ ] Inicializovat IntelliJ Platform Plugin projekt (Gradle Kotlin DSL)
  - [ ] `build.gradle.kts` — IntelliJ Platform 2024.3+, JVM 17
  - [ ] `settings.gradle.kts`, `gradle.properties` (verze `2026.2.1`)
  - [ ] Gradle wrapper (8.x)
- [ ] `src/main/resources/META-INF/plugin.xml`
  - [ ] Plugin ID: `com.jakubjirak.secretstripwire`
  - [ ] Závislosti: `com.intellij.modules.platform`, `Git4Idea`
- [ ] Balíčková struktura: `com.jakubjirak.secretstripwire.*`
  - [ ] `.model` — datové třídy
  - [ ] `.detection` — pattern matching + entropy
  - [ ] `.patterns` — pattern registry
  - [ ] `.inspection` — IntelliJ inspections
  - [ ] `.commit` — pre-commit guard
  - [ ] `.quickfix` — quick-fix actions
  - [ ] `.history` — git historie scan
  - [ ] `.ui` — notifikace, dialogy
  - [ ] `.settings` — konfigurace
- [ ] `src/main/resources/messages/SecretsTripwireBundle.properties`
- [ ] `.gitignore` (build/, .idea/, .gradle/, .kotlin/, out/)
- [ ] `gradle/libs.versions.toml` — version catalog
- [ ] `LICENSE` (Apache-2.0)
- [ ] `.editorconfig` (Kotlin style conventions)
- [ ] Detekt konfigurace (`detekt.yml`)
- [ ] GitHub Actions CI workflow (`.github/workflows/build.yml`)
- [ ] Ověřit `./gradlew build`

## Fáze 2: Datový model

- [ ] `SecretPattern.kt`
  - [ ] `id: String`, `name: String` (e.g., "AWS Access Key")
  - [ ] `regex: String`, `entropyThreshold: Double?`
  - [ ] `severity: PatternSeverity` (ERROR, WARNING)
  - [ ] `recommendation: String`
  - [ ] `source: PatternSource` (BUILTIN, CUSTOM, TEAM)
  - [ ] `enabled: Boolean`
- [ ] `SecretFinding.kt`
  - [ ] `file: VirtualFile`, `offset: Int`, `length: Int`
  - [ ] `pattern: SecretPattern`
  - [ ] `maskedValue: String` (AKIA****XXXX)
  - [ ] `entropy: Double`
  - [ ] `status: FindingStatus` (DETECTED, WHITELISTED, FIXED)
- [ ] `PatternSeverity.kt`, `PatternSource.kt`, `FindingStatus.kt` — enums
- [ ] Unit testy

## Fáze 3: Pattern Registry

- [ ] `PatternRegistry.kt`
  - [ ] `fun getActivePatterns(): List<SecretPattern>`
  - [ ] Merge: BUILTIN + USER + TEAM, respektuje enabled/disabled
  - [ ] Singleton service
- [ ] `BuiltinPatterns.kt` — 20 předdefinovaných patterns:
  - [ ] AWS Access Key: `AKIA[0-9A-Z]{16}`
  - [ ] AWS Secret Key: high entropy after `aws_secret_access_key`
  - [ ] GitHub Token: `gh[ps]_[A-Za-z0-9_]{36,}`
  - [ ] GitHub OAuth: `gho_[A-Za-z0-9_]{36,}`
  - [ ] Google API Key: `AIza[0-9A-Za-z\\-_]{35}`
  - [ ] Slack Bot Token: `xoxb-[0-9]{10,}`
  - [ ] Slack User Token: `xoxp-[0-9]{10,}`
  - [ ] RSA Private Key: `-----BEGIN RSA PRIVATE KEY-----`
  - [ ] EC Private Key: `-----BEGIN EC PRIVATE KEY-----`
  - [ ] OpenSSH Private Key: `-----BEGIN OPENSSH PRIVATE KEY-----`
  - [ ] JWT: `eyJ[A-Za-z0-9-_]+\.eyJ[A-Za-z0-9-_]+\.[A-Za-z0-9-_]+`
  - [ ] JDBC connection string: `jdbc:[a-z]+://.*:.*@`
  - [ ] MongoDB URI: `mongodb(\+srv)?://[^:]+:[^@]+@`
  - [ ] Redis URI: `redis://:[^@]+@`
  - [ ] Generic password: `(password|passwd|pwd)\s*[=:]\s*['\"][^'\"]{8,}`
  - [ ] Generic API key: `(api[_-]?key|apikey)\s*[=:]\s*['\"][^'\"]{16,}`
  - [ ] Generic secret: `(secret|token)\s*[=:]\s*['\"][^'\"]{16,}`
  - [ ] Stripe Key: `sk_(live|test)_[A-Za-z0-9]{24,}`
  - [ ] Twilio: `SK[a-f0-9]{32}`
  - [ ] SendGrid: `SG\.[A-Za-z0-9_-]{22}\.[A-Za-z0-9_-]{43}`
- [ ] Unit testy pro každý pattern (true positives + true negatives)

## Fáze 4: Entropy Calculator

- [ ] `EntropyCalculator.kt`
  - [ ] `fun shannonEntropy(input: String): Double`
  - [ ] Shannon entropy: `-Σ p(x) * log2(p(x))`
  - [ ] Threshold: > 4.5 = pravděpodobně secret (konfigurovatelné)
  - [ ] Podpora: base64 detekce, hex detekce (higher threshold pro tyto)
- [ ] Unit testy (known entropy values, edge cases)

## Fáze 5: Secret Scanner

- [ ] `SecretScanner.kt`
  - [ ] `fun scan(text: String, file: VirtualFile, patterns: List<SecretPattern>): List<SecretFinding>`
  - [ ] Fáze 1: regex matching pro každý pattern
  - [ ] Fáze 2: entropy check pro matches s entropy threshold
  - [ ] Fáze 3: whitelist/ignore filter
  - [ ] Masking: nahradit střed hodnoty hvězdičkami
  - [ ] Performance: compiled regex cache, abort na velkých souborech (>1MB skip)
- [ ] `SecretIgnoreService.kt`
  - [ ] `// tripwire:ignore` inline suppress
  - [ ] `.secretsignore` soubor (gitignore-like syntax pro soubory)
  - [ ] Whitelisted hodnoty: SHA-256 hash (ne plaintext)
- [ ] Unit testy (matching, masking, ignore, performance)

## Fáze 6: Realtime Editor Integration

- [ ] `SecretAnnotator.kt` implements `Annotator`
  - [ ] Registrace v `plugin.xml`
  - [ ] Highlight: podtržení secret hodnoty (ERROR = červená vlnovka, WARNING = žlutá)
  - [ ] Tooltip: typ secretu, doporučení
  - [ ] Debounce: scan spuštěn 300ms po posledním keystroke
- [ ] `SecretInspection.kt` extends `LocalInspectionTool`
  - [ ] Registrace v `plugin.xml`: `<localInspection>`
  - [ ] Funguje s Analyze → Inspect Code (batch mode)
  - [ ] Quick-fixes (viz Fáze 7)
- [ ] `SecretGutterIconProvider.kt` implements `LineMarkerProvider`
  - [ ] Ikona 🔐 u řádku se secret
  - [ ] Click → show findings detail popup
- [ ] Inspection description HTML

## Fáze 7: Quick Fixes

- [ ] `MoveToEnvFileQuickFix.kt`
  - [ ] Vytvoří/append do `.env` souboru: `KEY_NAME=<value>`
  - [ ] Nahradí v kódu: `process.env.KEY_NAME` / `System.getenv("KEY_NAME")` (per jazyk)
- [ ] `ReplaceWithEnvVarQuickFix.kt`
  - [ ] Dialog: „Replace with environment variable reference"
  - [ ] Input: variable name
  - [ ] Output: nahrazení hardcoded hodnoty env var referencí
- [ ] `WhitelistSecretQuickFix.kt`
  - [ ] Dialog: „Why is this safe?" (povinný komentář)
  - [ ] Přidání SHA-256 hash do whitelist
  - [ ] Přidání `// tripwire:ignore` komentáře
- [ ] Unit testy

## Fáze 8: Pre-Commit Guard

- [ ] `SecretCheckinHandler.kt` implements `CheckinHandler`
  - [ ] Registrace v `plugin.xml`: `<checkinHandlerFactory>`
  - [ ] Před commitem: scan všech staged souborů
  - [ ] Pokud findings: zobrazit warning dialog
    - [ ] List nalezených secrets (masked)
    - [ ] Tlačítka: „Fix & Re-check" / „Commit Anyway" / „Cancel"
  - [ ] Konfigurovatelné: warn vs. hard block (paid)
- [ ] `PreCommitScanService.kt`
  - [ ] `fun scanStagedFiles(project: Project): List<SecretFinding>`
  - [ ] Využití Git4Idea API pro staged files
- [ ] Integration testy

## Fáze 9: Konfigurace

- [ ] `SecretsTripwireSettings.kt` implements `PersistentStateComponent`
  - [ ] Enabled/disabled patterns
  - [ ] Custom patterns (max 3 free, unlimited paid)
  - [ ] Entropy threshold
  - [ ] Commit behavior: warn / block / off
  - [ ] Ignored file patterns
- [ ] `SecretsTripwireConfigurable.kt` — Settings → Tools → Secrets Tripwire
  - [ ] Pattern tabulka (name, regex, severity, enabled toggle)
  - [ ] Add/Edit/Remove custom pattern
  - [ ] Entropy threshold slider
  - [ ] Commit guard mode dropdown
  - [ ] Ignored files patterns
- [ ] `.secrets-tripwire.yaml` team config
- [ ] Unit testy

## Fáze 10: Freemium & Licensing Infrastructure

- [ ] Integrace JetBrains Marketplace Licensing API
  - [ ] `LicenseChecker.kt` — ověření licence přes `com.intellij.ide.plugins.PluginManager` a Marketplace API
  - [ ] `LicenseStatus.kt` — enum: FREE, TRIAL, PAID, EXPIRED
  - [ ] `FreemiumGate.kt` — centrální guard pro paid features
    - [ ] `fun isPaidFeatureAvailable(featureId: String): Boolean`
    - [ ] Graceful degradation — paid UI elementy skryté/disabled ve free tieru
  - [ ] Trial period: 30 dní plný přístup
- [ ] `plugin.xml` — `<product-descriptor>` element pro Marketplace licensing
  - [ ] `code` atribut (unique product code)
  - [ ] `release-date`, `release-version`
- [ ] Nag screen / upgrade prompt
  - [ ] Non-intrusive balloon notification při pokusu o paid feature
  - [ ] „Upgrade to Pro" action → Marketplace stránka
  - [ ] Max 1× denně (ne spam)
- [ ] Feature flags
  - [ ] `FeatureRegistry.kt` — definice free vs. paid features
  - [ ] Každý paid feature má `@PaidFeature("feature.id")` anotaci nebo guard check
- [ ] Unit testy pro licensing logic (mock Marketplace API)

## Fáze 11: Production-Grade Quality

- [ ] Error Handling & Resilience
  - [ ] Globální exception handler (`ErrorReportSubmitter` implementace)
  - [ ] Graceful degradation — plugin nesmí crashnout IDE
  - [ ] Try-catch na všech EP entry pointech
  - [ ] Timeout na všech I/O operacích (network, git, file system)
  - [ ] Rate limiting na API volání
- [ ] Logging
  - [ ] `Logger` instance v každé třídě (IntelliJ `Logger.getInstance()`)
  - [ ] Structured logging: DEBUG pro vývoj, INFO pro operace, WARN/ERROR pro problémy
  - [ ] Žádné citlivé údaje v logech
- [ ] Telemetrie (opt-in, anonymní)
  - [ ] `PluginUsageTracker.kt` — FUS (Feature Usage Statistics) integrace
  - [ ] Metriky: aktivace featury, error rate, latence
  - [ ] Registrace FUS group v `plugin.xml`
- [ ] Performance
  - [ ] Profiling na velkých projektech (10k+ souborů)
  - [ ] Background thread pro všechny I/O operace (nesmí blokovat EDT)
  - [ ] Read/write action správně (ReadAction, WriteAction, smart mode check)
  - [ ] Disposal pattern: `Disposable` pro všechny listeners a services
  - [ ] Memory leak check (WeakReference kde potřeba)
- [ ] Testing — production úroveň
  - [ ] Unit testy: ≥80 % coverage core logiky
  - [ ] Integrační testy: `BasePlatformTestCase` pro PSI, inspections, EP
  - [ ] UI testy: `HeavyPlatformTestCase` pro tool windows, dialogy
  - [ ] Performance testy: benchmark na velkých souborech
- [ ] CI/CD Pipeline
  - [ ] GitHub Actions workflow:
    - [ ] `./gradlew check` — kompilace + testy
    - [ ] `./gradlew verifyPlugin` — plugin strukturální validace
    - [ ] `./gradlew verifyPluginProjectConfiguration` — metadata validace
    - [ ] `./gradlew runPluginVerifier` — binární kompatibilita s target IDE verzemi
    - [ ] Qodana scan (JetBrains code quality)
  - [ ] Release workflow:
    - [ ] Tag-based release (push tag → build → publish)
    - [ ] `./gradlew publishPlugin` — automatický upload na Marketplace
    - [ ] Changelog generace z git commitů
  - [ ] Dependabot / Renovate pro dependency updates
- [ ] Kódová kvalita
  - [ ] Kotlin coding conventions (official JetBrains style)
  - [ ] Detekt nebo ktlint pro statickou analýzu
  - [ ] Žádné deprecated API (IntelliJ Platform compatibility)
  - [ ] `@ApiStatus.Experimental` / `@ApiStatus.Internal` anotace
- [ ] Dokumentace
  - [ ] KDoc pro všechny public API
  - [ ] README.md: instalace, konfigurace, screenshoty, FAQ
  - [ ] CHANGELOG.md (Keep a Changelog format)
  - [ ] CONTRIBUTING.md (pokud open source)

## Fáze 12: Polish & Release

- [ ] README.md
- [ ] `./gradlew verifyPlugin`, `./gradlew buildPlugin`
- [ ] Manuální testování: Java, Kotlin, Python, JS, YAML, .env soubory
- [ ] False positive rate assessment (scan OSS projekty)
- [ ] Cross-IDE test
- [ ] Marketplace listing
- [ ] Git tag `v2026.2.1`

---

## Budoucí fáze (Paid)

- [ ] Neomezené custom patterns
- [ ] Git historie scan (prohledání starých commitů)
- [ ] Hard commit block (ne jen warning)
- [ ] Company pattern import/export (JSON)
- [ ] SARIF export pro CI integrace
- [ ] `.secretsignore` management UI
- [ ] Compliance report (HTML/PDF)
