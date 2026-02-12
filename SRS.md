# Local Secrets Tripwire — Software Requirements Specification (SRS)

**Verze:** 2026.2.1
**Autor:** Jakub Jirák
**Datum:** 12. února 2026
**Model:** Freemium JetBrains Plugin

---

## 1. Úvod

### 1.1 Účel
Local Secrets Tripwire detekuje tajné klíče, hesla a tokeny v kódu ještě před commitem. Na rozdíl od server-side řešení (GitHub Secret Scanning, GitGuardian) funguje lokálně a okamžitě — varuje vývojáře v reálném čase při psaní.

### 1.2 Rozsah
Plugin skenuje soubory v editoru a staged changes pomocí regex pattern matching a entropy analýzy. Varuje před potenciálními secrets a blokuje commit přes pre-commit hook.

---

## 2. Celkový popis

### 2.1 Perspektiva produktu
Existují VC-backed řešení (GitGuardian, TruffleHog, Gitleaks), ale žádné nemá IDE-native inline detekci v reálném čase. Plugin je lightweight alternativa s okamžitou zpětnou vazbou.

### 2.2 Uživatelské skupiny
- **Všichni vývojáři** — prevence náhodného leaku
- **Security týmy** — enforcement secret hygiene
- **Open source maintaineři** — ochrana před contributors leaky

---

## 3. Funkční požadavky

### FR-1: Secret Detection
- **FR-1.1:** Pattern-based detekce:
  - AWS keys (AKIA...)
  - GitHub tokens (ghp_, gho_, ghs_)
  - Google API keys
  - Slack tokens (xoxb-, xoxp-)
  - Private keys (BEGIN RSA/EC/OPENSSH PRIVATE KEY)
  - JWT tokens
  - Connection strings (jdbc:, mongodb://, redis://)
  - Generic passwords (password=, secret=, api_key=)
- **FR-1.2:** Entropy-based detekce pro vysoké-entropy stringy
- **FR-1.3:** Kombinace: regex + entropy threshold per pattern

### FR-2: Realtime Editor Warning
- **FR-2.1:** Inspection highlighting — podtržení secret hodnoty
- **FR-2.2:** Gutter ikona: 🔐 s severity (ERROR/WARNING)
- **FR-2.3:** Tooltip: typ secretu, doporučení (env var, vault, .env)
- **FR-2.4:** Quick-fix: „Move to .env file", „Replace with env var reference"

### FR-3: Pre-commit Guard
- **FR-3.1:** Commit dialog interceptor — varování před commitem se secrets
- **FR-3.2:** Blokovatelný commit (konfigurovatelné: warn vs. block)
- **FR-3.3:** „Allow this secret" — whitelist s komentářem proč
- **FR-3.4:** Git hook generátor (optional pre-commit hook skript)

### FR-4: Scan Historie (Paid)
- **FR-4.1:** Skenování git historie na historicky commitnuté secrets
- **FR-4.2:** Report: „Secret found in commit abc123, file X, line Y"
- **FR-4.3:** Doporučení: rotate key, git filter-branch

### FR-5: Custom Patterns (Paid)
- **FR-5.1:** Definovatelné custom patterns (regex)
- **FR-5.2:** Company-specific patterns (interní API klíče, interní URL)
- **FR-5.3:** Import/export pattern sady

### FR-6: Konfigurace
- **FR-6.1:** Settings page: enabled patterns, entropy threshold, block vs. warn
- **FR-6.2:** `.secrets-tripwire.yaml` v repo root
- **FR-6.3:** Per-file/line suppress: `// tripwire:ignore` nebo `.secretsignore`

---

## 4. Nefunkční požadavky

### NFR-1: Výkon
- Realtime scan < 50ms per keystroke (debounced)
- Full file scan < 200ms
- Neblokuje editor

### NFR-2: Privacy
- Veškeré skenování lokální — žádná data neopouštějí stroj
- Whitelisted secrets ukládány hashovaně (ne plaintext)

### NFR-3: False Positives
- Target: < 15 % false positive rate
- Entropy threshold konfigurovatelný
- Easy suppress workflow

---

## 5. Datový model

### 5.1 SecretPattern
```
SecretPattern {
  id: String
  name: String (e.g., "AWS Access Key")
  regex: String
  entropyThreshold: Double? (min Shannon entropy)
  severity: Enum (ERROR, WARNING)
  recommendation: String
  source: Enum (BUILTIN, CUSTOM, TEAM)
  enabled: Boolean
}
```

### 5.2 SecretFinding
```
SecretFinding {
  file: VirtualFile
  offset: Int
  length: Int
  pattern: SecretPattern
  value: String (masked: "AKIA****XXXX")
  entropy: Double
  status: Enum (DETECTED, WHITELISTED, FIXED)
}
```

---

## 6. Freemium model

### Free tier
- 20 built-in patterns (top cloud providers + generic)
- Realtime editor highlighting
- Pre-commit dialog warning
- Max 3 custom patterns

### Paid tier
- Neomezené custom patterns
- Git historie scan
- Commit blocking (hard block)
- Company pattern import/export
- Report export (SARIF, CSV)
- `.secretsignore` management
