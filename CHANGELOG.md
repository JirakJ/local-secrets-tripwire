# Changelog

## [2026.4.0] - 2026-06-09 🚀 Feature Release

### Added
- **Detection catalog grew from 15 to 42 patterns** — new coverage for OpenAI,
  Anthropic, GitLab, npm, PyPI, Hugging Face, Slack app tokens & webhooks,
  Stripe webhook secrets, Twilio, Telegram bots, DigitalOcean, Shopify,
  Databricks, Atlassian, Doppler, HashiCorp Vault, Azure Storage, Mailchimp,
  Firebase FCM, AWS temporary keys, GitHub fine-grained & refresh tokens,
  credentials embedded in URLs, hardcoded Bearer headers, and PGP private keys.
- **Quick fixes** — "Add to .secrets-allowlist" and "Suppress with
  tripwire:ignore comment" directly from the inspection popup.
- **Inline suppression** — a `tripwire:ignore` comment on a line silences
  findings on that line (editor and pre-commit guard alike).
- **Severity-aware highlighting** — ERROR patterns render as errors, WARNING
  as warnings, INFO as weak warnings; the exact secret range is underlined,
  not just the first token of the line.
- **Entropy detection is now actually wired into the inspection** — opt-in
  toggle in settings; previously the threshold setting had no effect.
- **Pre-commit guard dialog** — instead of silently cancelling the commit,
  the guard now lists the findings (masked) and offers Commit Anyway / Cancel.
- **Freemium gating implemented** — the "max 10 custom patterns" free limit
  advertised in settings is now enforced via the marketplace license.

### Fixed
- **Pre-commit guard ignored the allowlist and custom patterns** — allowlisted
  values no longer block commits, and organization-specific patterns are
  scanned before commit too.
- **Pre-commit guard respects the plugin enable toggle** and the new
  dedicated pre-commit setting.
- **Settings table silently dropped pattern type and description on save.**
- **Overlapping matches deduplicated** — when multiple patterns hit the same
  offset, only the most severe finding is reported.
- **Git history audit recorded the scan time instead of the commit date** —
  the `Date:` line is now parsed.
- **Allowlist file is cached** (invalidated by modification stamp) instead of
  being re-read from disk on every editor pass.

### Technical
- Added IntelliJ Platform test framework + JUnit Vintage; screenshots are
  rendered from the real plugin UI by an opt-in `ScreenshotGeneratorTest`.
- Test suite grew from 35 to 90 tests, including one positive and negative
  detection test per pattern.

## [2026.3.6] - 2026-04-18

### Changed
- Enabled freemium licensing via `<product-descriptor optional="true">`. Plugin remains fully functional without a license; premium features may be introduced in future minor releases.


## [2026.2.5] - 2026-02-13

### Added
- Added ProGuard obfuscation for code protection


## [2026.2.4] - 2026-02-13

### Changed
- Version bump for JetBrains Marketplace publication


## [2026.2.3] - 2026-02-13

### Changed
- Expanded compatibility range: sinceBuild 232.0+ (IntelliJ 2023.2+)
- Removed upper version bound for future IDE compatibility


All notable changes to this project will be documented in this file.

## [2026.2.2] - 2026-02-13

### Added
- Initial release
- 14 built-in secret detection patterns:
  - AWS Access Key (`AKIA...`)
  - AWS Secret Key (`aws_secret_access_key = ...`)
  - GitHub Personal Access / Service Token (`ghp_`, `ghs_`)
  - GitHub OAuth Token (`gho_`)
  - Slack Bot Token (`xoxb-`)
  - Slack User Token (`xoxp-`)
  - Stripe Secret Key (`sk_live_`, `sk_test_`)
  - Google API Key (`AIza...`)
  - SendGrid API Key (`SG.`)
  - Private Keys (RSA, EC, DSA, OpenSSH)
  - JWT Tokens (`eyJ...`)
  - Connection Strings (JDBC, MongoDB, Redis with embedded credentials)
  - Generic API Key assignments
  - Generic Password and Secret assignments
- Shannon entropy analysis for high-entropy string detection (configurable threshold, default 4.5)
- Real-time editor integration via IntelliJ `LocalInspectionTool` (Security group, WARNING level)
- Inline warnings and gutter icons for detected secrets
- Masked secret display in findings for safety
- Allowlist support via `.secrets-allowlist` project file to suppress false positives
- Custom detection patterns (id, name, regex, secret type, severity, description, enabled toggle)
- 12 secret types: API_KEY, PASSWORD, TOKEN, PRIVATE_KEY, AWS_KEY, GITHUB_TOKEN, SLACK_TOKEN, STRIPE_KEY, JWT, CONNECTION_STRING, GENERIC_SECRET, HIGH_ENTROPY
- 3 severity levels: ERROR, WARNING, INFO
- Configurable settings page (Settings → Tools → Secrets Tripwire)
- Entropy threshold configuration
- 1 MB max file size limit for performance
- Persistent settings stored in `secretsTripwireSettings.xml`
- Balloon notifications for detected secrets
- Optional Git4Idea integration
- Part of DevOps Safety Kit bundle
