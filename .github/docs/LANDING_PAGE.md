# Local Secrets Tripwire

## Catch Secrets Before They Leave Your Machine

> Real-time scanning for API keys, passwords, tokens, and private keys — with inline editor warnings and entropy analysis.

---

## The Problem

Leaked secrets are the number one cause of security breaches. A single API key committed to a public repository can lead to compromised infrastructure, stolen data, and expensive incidents. Git hooks catch secrets at commit time, but by then the secret is already written in a file. Developers need immediate feedback — at typing time, not commit time.

---

## Key Features

### Real-Time Secret Scanning
The plugin scans every file you open or edit, detecting potential secrets as you type. No waiting for a commit or a CI pipeline — you see the warning immediately.

### 14 Built-in Detection Patterns
Out of the box, the plugin detects AWS access keys, AWS secret keys, GitHub tokens, Slack tokens, Stripe keys, Google API keys, SendGrid keys, JWTs, private keys, connection strings, and generic passwords and secrets.

### Shannon Entropy Analysis
Beyond pattern matching, the plugin uses Shannon entropy analysis to identify high-randomness strings that may be secrets. Strings with entropy above the configurable threshold (default 4.5) are flagged automatically.

### Inline Editor Warnings
Detected secrets appear as inline warnings with gutter icons in the editor. Hover over a finding to see the secret type, severity, masked value, and entropy score.

### Allowlist for False Positives
Create a `.secrets-allowlist` file in your project root to suppress known false positives. Each line matches against detected secret values and excluded matching findings.

### Custom Detection Patterns
Add your own regex patterns for organization-specific secrets. Define the secret type, severity level, and description for each custom pattern.

### Pre-Commit Guard
Optionally block commits that contain detected secrets. The plugin integrates with the IDE's commit handler to stop secrets from reaching version control.

### Git History Scan
Scan past commits in your repository to find previously leaked secrets. Identify secrets that were committed before the plugin was installed.

---

## How It Works

1. **Install the plugin** — Search for "Local Secrets Tripwire" in the JetBrains Marketplace.
2. **Open any file** — The plugin scans automatically in the background using 14 built-in patterns and entropy analysis.
3. **See warnings instantly** — Detected secrets show as inline warnings with gutter icons. Hover for details.
4. **Suppress false positives** — Add entries to `.secrets-allowlist` in your project root to hide known safe strings.

---

## Pricing

### Free Tier — $0
- 14 built-in secret detection patterns
- Shannon entropy analysis
- Inline editor warnings and gutter icons
- Allowlist support for false positives
- Up to 3 custom detection patterns
- Masked display of detected secrets

### Pro Tier — $2.90/month
- Unlimited custom patterns
- Pre-commit guard (block commits with secrets)
- Git history scan for leaked secrets
- Hard blocking mode (prevent saving files with secrets)
- Team-shared configuration via `.secrets-tripwire.yaml`

---

## Testimonials

> *"We found an AWS key that had been sitting in a config file for months. Local Secrets Tripwire caught it the first time someone opened the file."*
> — **Security Engineer, Cloud Platform Team**

> *"The entropy analysis caught a Base64-encoded token that no pattern-based scanner would have found."*
> — **Backend Developer, FinTech Company**

> *"Simple, lightweight, and it just works. Every developer on our team has it installed now."*
> — **Tech Lead, Startup**

---

## FAQ

**Q: Does the plugin send my code to any external service?**
A: No. All scanning happens locally on your machine. Zero data leaves your IDE. No network calls are made.

**Q: Which secret types are detected out of the box?**
A: AWS keys, GitHub tokens, Slack tokens, Stripe keys, Google API keys, SendGrid keys, JWTs, private keys, connection strings, generic API keys, generic passwords, and generic secrets.

**Q: How do I reduce false positives?**
A: Increase the entropy threshold in settings, or add known safe strings to a `.secrets-allowlist` file in your project root.

**Q: Does it work with all file types?**
A: Yes. The plugin scans any file opened in the IDE, regardless of file type. Files larger than 1 MB are skipped for performance.

**Q: Can I use it alongside other secret scanning tools?**
A: Yes. Local Secrets Tripwire runs independently and complements tools like git-secrets, truffleHog, and CI-based scanners.

---

## Part of the DevOps Safety Kit

Local Secrets Tripwire pairs with [KubeContext Safety](https://github.com/JirakJ/kubernetes-context-guard) and [Terminal Safety Rails](https://github.com/JirakJ/terminal-safety-rails) for comprehensive protection against accidental production changes and secret leaks.

---

## Get Started

Install Local Secrets Tripwire and stop secrets from ever reaching your repository.

[Install from Marketplace](https://plugins.jetbrains.com) | [View on GitHub](https://github.com/JirakJ/local-secrets-tripwire)
