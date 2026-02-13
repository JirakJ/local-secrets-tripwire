# Local Secrets Tripwire — Git Enhancement Specifikace

**Verze:** 2026.2.1
**Status:** Plánováno
**Závislosti:** Git4Idea API, `git log`, `git diff`, VCS checkin handler

---

## Přehled

Rozšíření pluginu o pokročilou Git integraci pro blokování commitů obsahujících secrets,
skenování celé Git historie pro leaked secrets a sledování stáří secrets přes commity.

---

## FR-GE-001: Pre-Commit Secret Scan

### Popis

Blokuje `git commit` obsahující detekované secrets. Integruje se s IntelliJ VCS checkin
handlerem pro interceptování commitů před jejich vytvořením.

### Funkční požadavky

- [ ] **FR-GE-001.1** — `CheckinHandler` implementace: scan staged souborů před commitem
- [ ] **FR-GE-001.2** — Hard block mód: commit je zablokován dokud nejsou secrets odstraněny
- [ ] **FR-GE-001.3** — Warning mód: zobrazí warning dialog s možností „Commit Anyway"
- [ ] **FR-GE-001.4** — Diff-only scan: skenuje pouze přidané/změněné řádky (ne celý soubor)
- [ ] **FR-GE-001.5** — Inline suppress: `// tripwire:allow` ignoruje konkrétní řádek
- [ ] **FR-GE-001.6** — Pre-push hook: dodatečný scan před `git push` (double safety)
- [ ] **FR-GE-001.7** — Statistiky: počet zablokovaných commitů, top patterns

### Acceptance Criteria

1. Commit obsahující AWS klíč je zablokován (hard block mód)
2. Warning mód zobrazí dialog s masked secret hodnotou a doporučením
3. Diff-only scan nereportuje secrets v nezměněných řádcích
4. `// tripwire:allow` suppress funguje pro konkrétní řádek
5. Pre-push scan zachytí secrets přidané přes command-line `git commit` (mimo IDE)
6. Performance: scan staged souborů <1s pro 50 souborů
7. Statistiky jsou zobrazeny v tool window dashboard

### Datový model — rozšíření

```kotlin
data class PreCommitScanResult(
    val findings: List<StagedSecretFinding>,
    val scanMode: ScanMode,           // HARD_BLOCK, WARNING, OFF
    val scanDuration: Duration,
    val filesScanned: Int
)

data class StagedSecretFinding(
    val file: String,
    val lineNumber: Int,
    val pattern: SecretPattern,
    val maskedValue: String,
    val inDiffOnly: Boolean,          // true = nově přidaný řádek
    val suppressed: Boolean           // true = má tripwire:allow
)

enum class ScanMode {
    HARD_BLOCK,   // commit zablokován
    WARNING,      // warning dialog s možností pokračovat
    OFF           // vypnuto
}

data class CommitBlockStats(
    val totalBlockedCommits: Int,
    val totalFindingsBlocked: Int,
    val topPatterns: Map<String, Int>,  // pattern name → count
    val periodStart: Instant
)
```

### Technická implementace

- `SecretCheckinHandlerFactory.kt` — registrace v `plugin.xml`
- `StagedFilesScanner.kt` — Git4Idea API pro staged files + diff extraction
- `DiffOnlyFilter.kt` — parsování `git diff --cached` pro identifikaci nových řádků
- `PrePushHandler.kt` — Git4Idea `GitPushHandler` listener
- `CommitBlockStatsService.kt` — persisted statistiky

---

## FR-GE-002: Git History Audit

### Popis

Skenování celé Git historie pro leaked secrets. Prochází všechny commity pomocí
`git log -p` a identifikuje secrets, které byly kdykoliv commitovány (i pokud jsou
již odstraněny z aktuálního stavu).

### Funkční požadavky

- [ ] **FR-GE-002.1** — Full history scan: `git log -p --all` pro všechny větve
- [ ] **FR-GE-002.2** — Inkrementální scan: pouze nové commity od posledního auditu
- [ ] **FR-GE-002.3** — Report: seznam nalezených secrets s commit hash, soubor, řádek, autor
- [ ] **FR-GE-002.4** — Severity klasifikace: active (stále v kódu) vs. removed (smazáno ale v historii)
- [ ] **FR-GE-002.5** — Remediation suggestions: `git filter-branch` nebo BFG Repo-Cleaner
- [ ] **FR-GE-002.6** — Export reportu: JSON, CSV, SARIF formát
- [ ] **FR-GE-002.7** — Progress indikátor: zpracováno X/Y commitů

### Acceptance Criteria

1. Full scan prochází všechny commity ve všech větvích
2. Secret odstraněný v commitu #100 ale přidaný v #50 je nalezen a označen jako „removed"
3. Inkrementální scan zpracovává pouze commity od posledního auditu
4. Report obsahuje: commit hash, datum, autor, soubor, řádek, masked value, severity
5. SARIF export je validní a importovatelný do GitHub Security tab
6. Progress bar ukazuje reálný pokrok (X/Y commitů)
7. Scan lze zrušit bez ztráty částečných výsledků

### Datový model — rozšíření

```kotlin
data class HistoryAuditResult(
    val findings: List<HistorySecretFinding>,
    val totalCommitsScanned: Int,
    val scanDuration: Duration,
    val lastScannedCommit: String,
    val scanDate: Instant
)

data class HistorySecretFinding(
    val commitHash: String,
    val commitDate: Instant,
    val author: String,
    val file: String,
    val lineNumber: Int,
    val pattern: SecretPattern,
    val maskedValue: String,
    val currentStatus: HistoryFindingStatus,  // ACTIVE, REMOVED, ROTATED
    val introducedIn: String,       // commit kde secret poprvé přidán
    val removedIn: String?          // commit kde secret odstraněn (null = stále aktivní)
)

enum class HistoryFindingStatus {
    ACTIVE,    // secret je stále v aktuálním kódu
    REMOVED,   // secret byl odstraněn, ale je v historii
    ROTATED    // secret byl nahrazen jinou hodnotou
}

data class AuditReport(
    val projectName: String,
    val generatedAt: Instant,
    val result: HistoryAuditResult,
    val format: ReportFormat        // JSON, CSV, SARIF
)
```

### Technická implementace

- `GitHistoryAuditor.kt` — iterace přes `git log -p` s pattern matching
- `IncrementalAuditTracker.kt` — cache posledního skenovaného commitu
- `HistoryFindingClassifier.kt` — detekce zda secret je stále active/removed/rotated
- `AuditReportExporter.kt` — JSON/CSV/SARIF generátor
- `HistoryAuditToolWindowTab.kt` — nový tab v tool window s výsledky a progress barem

---

## FR-GE-003: Secret Rotation Tracking

### Popis

Sledování stáří secrets přes commity. Upozorní na secrets, které nebyly rotovány
po delší dobu, a vizualizuje rotační historii.

### Funkční požadavky

- [ ] **FR-GE-003.1** — Detekce stáří secret hodnoty: `git log -S` pro sledování změn
- [ ] **FR-GE-003.2** — Konfigurovatelné rotační politiky: max stáří per secret typ
  - [ ] API klíče: 90 dní
  - [ ] Databázová hesla: 180 dní
  - [ ] SSL certifikáty: 365 dní
- [ ] **FR-GE-003.3** — Warning notifikace: „Secret {name} nebyl rotován {X} dní"
- [ ] **FR-GE-003.4** — Rotační timeline: vizualizace kdy byl secret naposledy změněn
- [ ] **FR-GE-003.5** — Compliance report: seznam secrets s jejich stářím a rotačním statusem
- [ ] **FR-GE-003.6** — Integration s `.env` soubory: tracking stáří env var hodnot

### Acceptance Criteria

1. Secret přidaný před 100 dny s 90denní politikou zobrazí warning
2. Rotační politiky jsou konfigurovatelné per pattern typ
3. Změna hodnoty secret resetu stáří counter (detekce přes `git log -S`)
4. Timeline zobrazuje historii rotací s daty a autory
5. Compliance report obsahuje: secret typ, stáří, politika, status (OK/OVERDUE)
6. `.env` soubory jsou zahrnuty do tracking

### Datový model — rozšíření

```kotlin
data class SecretRotationInfo(
    val secretIdentifier: String,     // pattern match identifier
    val secretType: String,           // pattern name (e.g., "AWS Access Key")
    val file: String,
    val lineNumber: Int,
    val firstSeen: Instant,           // kdy secret poprvé přidán
    val lastRotated: Instant,         // kdy naposledy změněn
    val ageInDays: Int,
    val rotationPolicy: RotationPolicy?,
    val status: RotationStatus
)

data class RotationPolicy(
    val patternId: String,
    val maxAgeDays: Int,
    val warningBeforeDays: Int = 14   // warning 14 dní před expirací
)

enum class RotationStatus {
    OK,          // v rámci politiky
    WARNING,     // blíží se expiraci
    OVERDUE,     // překročena rotační politika
    NO_POLICY    // žádná politika definována
}

data class RotationTimeline(
    val secretIdentifier: String,
    val rotations: List<RotationEvent>,
    val currentAge: Int
)

data class RotationEvent(
    val commitHash: String,
    val date: Instant,
    val author: String,
    val changeType: String            // "initial", "rotated", "removed"
)
```

### Technická implementace

- `SecretAgeTracker.kt` — `git log -S` wrapper pro sledování secret value changes
- `RotationPolicyEngine.kt` — evaluace stáří vs. politika, generování warnings
- `RotationTimelineCollector.kt` — sběr rotačních eventů z Git historie
- `RotationComplianceReport.kt` — generování compliance reportu
- `RotationWarningNotifier.kt` — balloon notifikace pro overdue secrets

---

## Společné technické požadavky

### Performance

- Pre-commit scan: <1s pro 50 staged souborů
- History audit: background task s progress indikátorem, cancellable
- Rotation tracking: lazy evaluation, cached výsledky s TTL 1 hodina
- Všechny Git operace na background thread

### Konfigurace — rozšíření `SecretsTripwireSettings`

```kotlin
// Nová pole v existujícím Settings
val preCommitScanEnabled: Boolean = true
val preCommitScanMode: ScanMode = ScanMode.WARNING
val diffOnlyScan: Boolean = true
val prePushScanEnabled: Boolean = false
val historyAuditEnabled: Boolean = true
val lastAuditCommit: String? = null
val rotationTrackingEnabled: Boolean = false
val rotationPolicies: List<RotationPolicy> = listOf(
    RotationPolicy("aws_access_key", maxAgeDays = 90),
    RotationPolicy("generic_password", maxAgeDays = 180),
    RotationPolicy("ssl_certificate", maxAgeDays = 365)
)
val auditReportFormat: ReportFormat = ReportFormat.JSON
```

### Freemium rozdělení

| Feature | Free | Paid |
|---------|------|------|
| Pre-commit scan (warning mód) | ✅ | ✅ |
| Hard block mód | ❌ | ✅ |
| Diff-only scan | ✅ | ✅ |
| Git history audit | ❌ | ✅ |
| SARIF export | ❌ | ✅ |
| Secret rotation tracking | ❌ | ✅ |
| Compliance report | ❌ | ✅ |
| Pre-push hook | ❌ | ✅ |
