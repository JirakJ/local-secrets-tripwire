# Copilot Instructions — Local Secrets Tripwire

## Projekt
JetBrains IDE plugin — detekuje tajné klíče, hesla a tokeny v kódu v reálném čase a brání jejich commitnutí.

## Technický stack
- **Jazyk:** Kotlin (JVM 17+)
- **Build:** Gradle (Kotlin DSL) s `org.jetbrains.intellij.platform` plugin 2.x
- **Platform:** IntelliJ Platform SDK 2024.3+
- **Testování:** JUnit 5 + `BasePlatformTestCase`

## Build a testy
```bash
./gradlew build
./gradlew test
./gradlew test --tests "*.SecretPatternMatcherTest.testAwsKeyDetection"
./gradlew runIde
./gradlew verifyPlugin
```

## Architektura
- **Detection:** `SecretScanner` — orchestruje regex matching + entropy analýzu
- **Patterns:** `PatternRegistry` — built-in + custom patterns, YAML konfigurace
- **Entropy:** `EntropyCalculator` — Shannon entropy pro identifikaci high-entropy stringů
- **Editor:** `SecretInspection` (LocalInspectionTool), `SecretAnnotator` (Annotator) — realtime highlighting
- **Commit guard:** `SecretCheckinHandler` (CheckinHandler) — interceptuje commit dialog
- **Quick-fixes:** `MoveToEnvFileQuickFix`, `ReplaceWithEnvVarQuickFix`
- **History scan:** `GitHistoryScanner` — prochází git log pro historické secrets (Paid)

## Konvence
- Balíčky: `com.jirak.secretstripwire.*`
- Secret hodnoty vždy maskované v UI a logech (AKIA****XXXX)
- Whitelisted secrets ukládány jako SHA-256 hash, nikdy plaintext
- Detekce debounced (300ms) pro minimální editor overhead
- Suppress: `// tripwire:ignore` nebo `.secretsignore` soubor
- Texty v `messages/SecretsTripwireBundle.properties`
- Verzování: `2026.2.X`

## Freemium hranice
- Free: 20 built-in patterns, realtime highlighting, pre-commit warning, 3 custom patterns
- Paid: neomezené custom, git scan, commit blocking, company patterns, SARIF export
