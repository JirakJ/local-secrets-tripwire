# Local Secrets Tripwire — Business Requirements Specification (BRS)

**Verze:** 2026.2.1
**Autor:** Jakub Jirák
**Datum:** 12. února 2026
**Model:** Freemium JetBrains Plugin

---

## 1. Executive Summary

Local Secrets Tripwire chrání vývojáře před commitnutím tajných klíčů do repozitáře. Na rozdíl od server-side řešení (GitGuardian, GitHub Secret Scanning) funguje lokálně a v reálném čase — varuje ještě před tím, než secret opustí stroj.

---

## 2. Obchodní cíle

### 2.1 KPI
| Metrika | Cíl (6 měsíců) | Cíl (12 měsíců) |
|---------|:---:|:---:|
| Celkové instalace | 8 000 | 35 000 |
| MAU | 4 000 | 15 000 |
| Paid konverze | 3 % | 5 % |
| Marketplace rating | ≥ 4.0 | ≥ 4.3 |

---

## 3. Cílový trh

- **Všichni vývojáři** (60 %) — universální problém
- **Security-conscious týmy** (25 %) — compliance, audit
- **Open source maintaineři** (15 %) — ochrana repozitáře

---

## 4. Konkurenční prostředí

| Řešení | Omezení |
|--------|---------|
| GitGuardian (VC-backed) | Server-side, ne realtime v IDE, drahý |
| GitHub Secret Scanning | Jen GitHub, post-push |
| Gitleaks / TruffleHog | CLI, ne IDE-native |
| pre-commit hooks | Manuální setup, ne inline feedback |

**Diferenciace:** Lokální, realtime, IDE-native, zero-config.

**Riziko:** VC-backed konkurence (GitGuardian) může přidat IDE plugin. Diferenciátor: lightweight, privacy-first, okamžitá zpětná vazba.

---

## 5. Revenue model

| Tier | Cena |
|------|------|
| **Free** | $0 — 20 patterns, realtime warning, pre-commit dialog |
| **Pro** | $2.90/měsíc nebo $29/rok — custom patterns, git scan, blocking |
| **Team** | $5.90/user/měsíc — Pro + company patterns, compliance reports |

---

## 6. Go-to-Market

1. Blog: „I Almost Leaked My AWS Keys. This Plugin Stopped Me."
2. Security community: OWASP, security-focused subreddits
3. DevSecOps konference

---

## 7. Milníky

| Milestone | Popis |
|-----------|-------|
| **M1: MVP** | 20 patterns, realtime highlighting, pre-commit warning |
| **M2: Smart** | Entropy detection, reduced false positives |
| **M3: History** | Git historie scan, report |
| **M4: Team** | Company patterns, compliance reports |
| **M5: Integration** | SARIF export, CI pipeline support |

---

## 8. Success Criteria

1. 15 000+ instalací do 9 měsíců
2. Marketplace rating ≥ 4.0
3. ≥ 3 % paid konverze
4. Demonstrativní „prevented leak" stories
