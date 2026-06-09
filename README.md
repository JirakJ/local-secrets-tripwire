# Local Secrets Tripwire

[![Build](https://github.com/JirakJ/local-secrets-tripwire/actions/workflows/build.yml/badge.svg)](https://github.com/JirakJ/local-secrets-tripwire/actions/workflows/build.yml)
[![Version](https://img.shields.io/badge/version-2026.4.0-blue.svg)](https://github.com/JirakJ/local-secrets-tripwire/releases)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)

> Scans files for accidentally committed secrets — API keys, passwords, tokens, and private keys — using pattern matching and Shannon entropy analysis, with real-time editor warnings.

## ✨ Features

- **42 built-in patterns** — AWS, GitHub (classic, fine-grained, OAuth, refresh), OpenAI, Anthropic, GitLab, npm, PyPI, Hugging Face, Slack, Stripe, Twilio, Telegram, DigitalOcean, Shopify, Databricks, Atlassian, Doppler, Vault, Azure Storage, Mailchimp, Firebase, Google, SendGrid, JWTs, private keys (incl. PGP), connection strings, credentials in URLs, Bearer headers, and more
- **Shannon entropy analysis** — identifies high-entropy strings that may be secrets (opt-in, configurable threshold)
- **Editor integration** — severity-aware inline warnings on the exact secret range, in real time
- **Quick fixes** — add to allowlist or suppress with an inline `tripwire:ignore` comment
- **Inline suppression** — `// tripwire:ignore` or `# tripwire:ignore` silences a line everywhere (editor + pre-commit)
- **Local inspection** — runs as a standard IntelliJ code inspection (Security group)
- **Allowlist support** — suppress false positives via `.secrets-allowlist` file in your project root
- **Custom patterns** — add your own detection regex patterns
- **Masked display** — secrets are partially masked in findings; the raw value is never shown
- **Pre-commit guard** — scans staged files and shows masked findings with a Commit Anyway / Cancel choice
- **Git history scan** — scan past commits for leaked secrets

## 📸 Screenshots

![Editor detection](docs/screenshots/editor-secret-detection.png)

![Settings](docs/screenshots/settings-panel.png)

*Regenerated from the real plugin UI: `./gradlew test --tests "*ScreenshotGeneratorTest*" -PscreenshotsDir=$(pwd)/docs/screenshots`*

## 📦 Installation

### From JetBrains Marketplace
1. Open **Settings → Plugins → Marketplace**
2. Search for **"Local Secrets Tripwire"**
3. Click **Install**

### From ZIP
1. Download the latest release from [Releases](https://github.com/JirakJ/local-secrets-tripwire/releases)
2. Open **Settings → Plugins → ⚙️ → Install Plugin from Disk**
3. Select the downloaded ZIP file

## 🚀 Quick Start

1. Install the plugin and restart your IDE
2. Open any file — the plugin automatically scans for secrets in the background
3. Detected secrets appear as inline warnings with gutter icons
4. Hover over a finding to see details (secret type, severity, masked value, entropy)
5. To suppress false positives, add entries to a `.secrets-allowlist` file in your project root

## ⚙️ Configuration

**Settings → Tools → Secrets Tripwire**

| Setting | Default | Description |
|---------|---------|-------------|
| Enable plugin | `true` | Enable or disable secret scanning |
| Entropy threshold | `4.5` | Shannon entropy threshold for high-entropy string detection (higher = fewer false positives) |
| Custom patterns | *(empty)* | Table of custom secret detection patterns (id, name, regex, secret type, severity, description, enabled) |

### Custom Pattern Fields

| Field | Description |
|-------|-------------|
| ID | Unique identifier for the pattern |
| Name | Human-readable pattern name |
| Regex | Regular expression to match secrets |
| Secret Type | `API_KEY`, `PASSWORD`, `TOKEN`, `PRIVATE_KEY`, `AWS_KEY`, `GITHUB_TOKEN`, `SLACK_TOKEN`, `STRIPE_KEY`, `JWT`, `CONNECTION_STRING`, `GENERIC_SECRET`, or `HIGH_ENTROPY` |
| Severity | `ERROR`, `WARNING`, or `INFO` |
| Description | Shown in the editor warning tooltip |
| Enabled | Whether this pattern is active |

## 🎯 Usage

### Real-Time Scanning
The plugin runs as an IntelliJ `LocalInspectionTool` in the **Security** inspection group. It scans every file you open or edit, combining built-in patterns with any custom patterns you've defined. Files larger than 1 MB are skipped for performance.

### Built-in Detection Patterns

| Pattern | Secret Type | Severity | Example Match |
|---------|------------|----------|---------------|
| AWS Access Key | AWS_KEY | 🔴 ERROR | `AKIA...` (20 chars) |
| AWS Secret Key | AWS_KEY | 🔴 ERROR | `aws_secret_access_key = "..."` |
| GitHub Token | GITHUB_TOKEN | 🔴 ERROR | `ghp_...`, `ghs_...` |
| GitHub OAuth Token | GITHUB_TOKEN | 🔴 ERROR | `gho_...` |
| Slack Bot Token | SLACK_TOKEN | 🔴 ERROR | `xoxb-...` |
| Slack User Token | SLACK_TOKEN | 🔴 ERROR | `xoxp-...` |
| Stripe Secret Key | STRIPE_KEY | 🔴 ERROR | `sk_live_...`, `sk_test_...` |
| Google API Key | API_KEY | 🔴 ERROR | `AIza...` (39 chars) |
| SendGrid API Key | API_KEY | 🔴 ERROR | `SG....` |
| Private Key | PRIVATE_KEY | 🔴 ERROR | `-----BEGIN RSA PRIVATE KEY-----` |
| Connection String | CONNECTION_STRING | 🔴 ERROR | `jdbc:...://user:pass@host` |
| Generic API Key | API_KEY | 🟡 WARNING | `api_key = "..."` |
| JWT Token | JWT | 🟡 WARNING | `eyJ...` |
| Generic Password | PASSWORD | 🟡 WARNING | `password = "..."` |
| Generic Secret | GENERIC_SECRET | 🟡 WARNING | `secret = "..."` |

### Entropy Analysis
In addition to pattern matching, the plugin performs Shannon entropy analysis on quoted strings of 16+ characters. Strings exceeding the configured entropy threshold (default: 4.5) are flagged as potential secrets with `INFO` severity.

### Allowlist
Create a `.secrets-allowlist` file in your project root to suppress false positives:

```
# Lines starting with # are comments
test-api-key-not-a-real-secret
example-token-for-documentation
```

Each line is matched against detected secret values. Matching findings are excluded from results.

## 📋 Requirements

- IntelliJ IDEA 2024.3+ (or any JetBrains IDE based on IntelliJ Platform)
- Java 17+
- Git4Idea plugin (optional, for Git integration features)

## 🏗️ Building from Source

```bash
git clone https://github.com/JirakJ/local-secrets-tripwire.git
cd local-secrets-tripwire
./gradlew build
```

## 🤝 Part of DevOps Safety Kit

Local Secrets Tripwire pairs with [Kubernetes Context Guard](https://github.com/JirakJ/kubernetes-context-guard) and [Terminal Safety Rails](https://github.com/JirakJ/terminal-safety-rails) for comprehensive protection against accidental production changes and secret leaks.

## 📝 License

MIT License — see [LICENSE](LICENSE) for details.

## 👤 Author

**Jakub Jirák**
- GitHub: [@JirakJ](https://github.com/JirakJ)
