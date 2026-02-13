# Changelog

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
