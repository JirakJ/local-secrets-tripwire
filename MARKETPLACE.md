# Local Secrets Tripwire — JetBrains Marketplace Listing

## Plugin Name
Local Secrets Tripwire

## Tagline
Detect secrets in your code before they reach the repository — scan locally, ship safely.

## Short Description
Prevent secret leaks with real-time detection of API keys, tokens, passwords, and private keys in your code. Local scanning with entropy analysis — secrets never leave your machine.

## Full Description

```html
<p>
  <b>Prevent secret leaks at the source.</b> Local Secrets Tripwire detects API keys, tokens, passwords,
  and private keys in real time as you type — before secrets ever reach your repository. Unlike server-side
  scanners, all analysis happens locally on your machine, ensuring your sensitive data stays private.
</p>

<h2>Key Features</h2>
<ul>
  <li><b>42 built-in detection patterns</b> — AWS, GitHub, OpenAI, Anthropic, GitLab, npm, PyPI, Hugging Face, Slack, Stripe, Twilio, Google, SendGrid, JWTs, private keys (RSA/EC/DSA/OpenSSH/PGP), connection strings, credentials in URLs, Bearer headers and more</li>
  <li><b>Real-time inspection</b> — severity-aware highlighting of the exact secret range (Security inspection group)</li>
  <li><b>Shannon entropy analysis</b> — opt-in detection of high-randomness strings with a configurable threshold</li>
  <li><b>Custom patterns</b> — your own regexes with name, severity and enable toggle</li>
  <li><b>Allowlist &amp; inline suppression</b> — <code>.secrets-allowlist</code> and <code>tripwire:ignore</code> comments, both available as quick fixes</li>
  <li><b>Masked display</b> — detected values are masked; the raw secret is never shown</li>
  <li><b>Pre-commit guard</b> — scans the files being committed and offers Commit Anyway / Cancel</li>
  <li><b>Privacy-first</b> — all scanning runs locally; nothing leaves your machine</li>
</ul>

<h2>Free &amp; Open Source</h2>
<p>Every feature is free for everyone — no license needed. Source code: <a href="https://github.com/JirakJ/local-secrets-tripwire">github.com/JirakJ/local-secrets-tripwire</a> (Apache-2.0). Issues and pull requests are welcome.</p>

<h2>Getting Started</h2>
<ol>
  <li><b>Install</b> — search "Local Secrets Tripwire" in <code>Settings → Plugins</code> and click Install</li>
  <li><b>Open any file</b> — secrets are detected and highlighted in real time as you type</li>
  <li><b>Review findings</b> — hover a highlighted secret and press Alt+Enter for the allowlist / suppress quick fixes</li>
</ol>

<h2>Compatibility</h2>
<ul>
  <li>IntelliJ IDEA 2023.2+ and other IntelliJ-based IDEs</li>
  <li>macOS, Linux, and Windows</li>
  <li>Works with any programming language and file type</li>
</ul>

<h2>Part of the Developer Productivity Suite</h2>
<p>
  Local Secrets Tripwire is part of a comprehensive developer safety toolkit. Combine it with
  <b>Kubernetes Context Guard</b> (always know your kubectl context),
  <b>Terminal Safety Rails</b> (intercept dangerous terminal commands),
  <b>Architecture Breadcrumbs</b> (embed architectural decisions in code), and
  <b>ADR Companion</b> (manage Architecture Decision Records)
  for complete development workflow protection.
</p>
```

## Tags
security, secrets, api-keys, leak-prevention, code-scanning

## Category
Security

## Pricing

Free & open source (Apache-2.0) — every feature is available to everyone, no license needed.

## Screenshots
1. **Real-time detection** — editor with a highlighted secret and its masked description
2. **Quick fixes** — Alt+Enter menu with "Add to allowlist" and "Suppress with tripwire:ignore"
3. **Settings** — entropy threshold and the custom pattern table
4. **Pre-commit guard** — dialog listing masked findings with Commit Anyway / Cancel
