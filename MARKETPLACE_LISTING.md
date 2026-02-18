# JetBrains Marketplace Listing — Local Secrets Tripwire

---

## 📝 Plugin Information

| Field       | Value                                  |
|-------------|----------------------------------------|
| **Name**    | Local Secrets Tripwire                 |
| **Plugin ID** | `com.jakubjirak.secretstripwire`    |
| **Version** | 2026.2.8                               |
| **Category** | Code Quality / Security               |
| **Tags**    | Security, Secrets Detection, API Keys, Entropy Analysis, Pre-commit Hook |

---

## 🎯 Short Description

> Scans files for accidentally committed secrets — API keys, passwords, tokens, and private keys — using pattern matching and Shannon entropy analysis, with real-time editor warnings. (198 chars)

---

## 📖 Full Description

### 🔐 Stop Secrets from Leaking into Your Codebase

**The Problem:** A single committed API key or password can cost your company millions. Developers accidentally push secrets to repositories every day, and traditional `.gitignore` files aren't enough.

**The Solution:** Local Secrets Tripwire catches secrets *before* they leave your machine. It runs real-time pattern matching and Shannon entropy analysis directly inside your IDE, flagging API keys, tokens, passwords, and private keys the instant you type them.

---

### ✨ Key Features

#### 🆓 Free Tier

- 🔍 **14 Built-in Patterns** — Detects AWS keys, GitHub tokens, Slack tokens, Stripe keys, Google API keys, SendGrid keys, JWTs, private keys, connection strings, and more
- 📊 **Shannon Entropy Analysis** — Identifies high-entropy strings that may be secrets (configurable threshold, default 4.5)
- ✏️ **Editor Integration** — Inline warnings and gutter icons for detected secrets in real time
- 🔎 **Local Inspection** — Runs as a standard IntelliJ code inspection (Security group, WARNING level)
- ✅ **Allowlist Support** — Suppress false positives via `.secrets-allowlist` file in your project root
- 🧩 **Up to 3 Custom Patterns** — Add your own detection regex patterns
- 🙈 **Masked Display** — Secrets are partially masked in findings for safety

#### ⭐ Pro Tier — $2.90/month

- ♾️ **Unlimited Custom Patterns** — No cap on custom detection patterns
- 🛡️ **Pre-commit Guard** — Optional blocking of commits containing secrets
- 📜 **Git History Scan** — Scan past commits for leaked secrets
- 🚫 **Hard Blocking Mode** — Prevent files with secrets from being saved
- 👥 **Team-shared Configuration** — Share settings via `.secrets-tripwire.yaml`

---

### 💰 Pricing Model

| Tier     | Price           | Highlights                                      |
|----------|-----------------|--------------------------------------------------|
| **Free** | $0              | 14 patterns, entropy analysis, editor warnings   |
| **Pro**  | $2.90/month     | Unlimited patterns, pre-commit guard, git scan   |

---

### 🎯 Perfect For

- **Solo developers** who want to prevent accidental secret leaks
- **Security-conscious teams** enforcing secret hygiene in codebases
- **DevOps engineers** building pre-commit security gates
- **Open-source maintainers** protecting contributor credentials

---

### ⚙️ Requirements

| Requirement        | Value                    |
|--------------------|--------------------------|
| **Minimum IDE Build** | 232 (IntelliJ 2023.2+) |
| **Java Version**   | 17+                      |
| **Optional**       | Git4Idea plugin (for Git history scan) |

---

### 🚀 Quick Start

1. **Install** the plugin from JetBrains Marketplace
2. **Open** any project — the plugin automatically scans open files
3. **Configure** at **Settings → Tools → Secrets Tripwire** (adjust entropy threshold, add custom patterns)
4. **Review** inline warnings and gutter icons in the editor
5. **Allowlist** false positives by adding entries to `.secrets-allowlist` in your project root

---

## 📸 Screenshots & Media

1. **Editor Warnings** — Inline gutter icons and highlighted secret detections in a source file
2. **Settings Panel** — Secrets Tripwire configuration page with entropy threshold slider and pattern toggles
3. **Custom Patterns Table** — Adding custom regex patterns with ID, Name, Regex, Secret Type, and Severity fields
4. **Allowlist File** — Example `.secrets-allowlist` file suppressing false positives
5. **Inspection Results** — IntelliJ inspection results panel showing grouped secret findings
6. **Pre-commit Guard (Pro)** — Commit dialog showing blocked commit due to detected secrets
7. **Git History Scan (Pro)** — Tool window displaying secrets found in past commits
8. **Masked Display** — Partially masked secret values in the findings panel for safe viewing

---

## 🏷️ Marketplace Categories

| Type          | Category                  |
|---------------|---------------------------|
| **Primary**   | Security                  |
| **Secondary** | Code Quality Tools        |

---

## 🔍 Keywords (SEO)

`secrets detection`, `API key scanner`, `credential leak prevention`, `pre-commit hook`, `Shannon entropy`, `password detection`, `token scanner`, `security inspection`, `secret scanning`, `git secrets`, `AWS key detection`, `GitHub token scanner`, `sensitive data`, `code security`, `leak prevention`

---

## 📋 Compatibility

| IDE                          | Compatible |
|------------------------------|------------|
| IntelliJ IDEA Community      | ✅          |
| IntelliJ IDEA Ultimate       | ✅          |
| PyCharm                      | ✅          |
| WebStorm                     | ✅          |
| GoLand                       | ✅          |
| PhpStorm                     | ✅          |
| CLion                        | ✅          |
| Rider                        | ✅          |
| RubyMine                     | ✅          |
| Android Studio               | ✅          |

**Minimum Build:** 232 (IntelliJ Platform 2023.2+)

---

## 📞 Vendor Information

| Field        | Value                        |
|--------------|------------------------------|
| **Vendor**   | Jakub Jirak                  |
| **Email**    | jirak@jakubjirak.com         |
| **Website**  | https://jakubjirak.com       |
| **Support**  | support@jakubjirak.com       |

---

## ✅ Pre-Launch Checklist

- [ ] Plugin icon (40×40 SVG and 80×80 PNG) prepared
- [ ] Screenshots (1280×800 minimum) captured for all key features
- [ ] Short description within 255-character limit
- [ ] Full description reviewed and proofread
- [ ] Pricing tiers configured in JetBrains Marketplace portal
- [ ] Product descriptor code `PSECRETSTRIPWRE` verified
- [ ] Plugin signed with JetBrains Marketplace certificate
- [ ] License agreement (EULA) uploaded
- [ ] CHANGELOG.md up to date with latest version notes
- [ ] Compatibility tested on minimum build (232)
- [ ] Free tier vs Pro tier feature gating verified
- [ ] `.secrets-allowlist` documentation included in plugin help
- [ ] Support email monitored and responsive
