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
  <li><b>14 built-in detection patterns</b> — AWS keys, GitHub tokens, Slack tokens, Stripe keys, Google API keys, SendGrid keys, JWTs, private keys, connection strings, and more</li>
  <li><b>Real-time editor scanning</b> — highlights secrets as you type with gutter icons (🔐) and inline warnings</li>
  <li><b>Shannon entropy analysis</b> — identifies high-entropy strings that may be secrets (configurable threshold)</li>
  <li><b>Privacy-first architecture</b> — all scanning runs locally; secrets never leave your machine</li>
  <li><b>Allowlist support</b> — suppress false positives with <code>.secrets-allowlist</code></li>
  <li><b>Masked secret display</b> — detected secrets are partially masked for safety</li>
  <li><b>Pre-commit guard</b> — block commits containing secrets</li>
  <li><b>Git history scan</b> — find historically committed secrets</li>
</ul>

<h3>Detects These Secret Types</h3>
<ul>
  <li>AWS Access Keys &amp; Secret Keys</li>
  <li>GitHub Tokens (<code>ghp_</code>, <code>ghs_</code>, <code>gho_</code>)</li>
  <li>Slack Bot &amp; User Tokens (<code>xoxb-</code>, <code>xoxp-</code>)</li>
  <li>Stripe Keys (<code>sk_live_</code>, <code>sk_test_</code>)</li>
  <li>Google API Keys (<code>AIza...</code>)</li>
  <li>SendGrid Keys (<code>SG...</code>)</li>
  <li>Private Keys (RSA, EC, OpenSSH)</li>
  <li>Connection Strings (JDBC, MongoDB, Redis)</li>
  <li>JWT Tokens</li>
  <li>Generic API keys, passwords, and secrets</li>
</ul>

<h2>Free &amp; Open Source</h2>
<p>Every feature is free for everyone — no license needed. Source code: <a href="https://github.com/JirakJ/local-secrets-tripwire">github.com/JirakJ/local-secrets-tripwire</a> (Apache-2.0). Issues and pull requests are welcome.</p>
<ul>
  <li>All 14 built-in detection patterns</li>
  <li>Real-time editor highlighting with gutter warnings</li>
  <li>Shannon entropy analysis (configurable threshold)</li>
  <li><code>.secrets-allowlist</code> for false positive suppression</li>
  <li>Unlimited custom detection patterns</li>
  <li>Masked secret display</li>
  <li>Settings page for enabling/disabling individual patterns</li>
  <li><b>Git history scan</b> — find secrets that were previously committed to the repository</li>
  <li><b>Hard blocking mode</b> — prevent file saves when secrets are detected</li>
  <li><b>Pre-commit guard</b> — block commits containing secrets automatically</li>
  <li><b>Team-shared configuration</b> — commit <code>.secrets-tripwire.yaml</code> for consistent team detection</li>
  <li><b>Report export</b> — export findings in SARIF and CSV formats</li>
</ul>

<h2>Getting Started</h2>
<ol>
  <li><b>Install</b> — search "Local Secrets Tripwire" in <code>Settings → Plugins</code> and click Install</li>
  <li><b>Open any file</b> — secrets are detected and highlighted in real time as you type</li>
  <li><b>Review findings</b> — click gutter icons (🔐) to see secret details and take action</li>
</ol>

<h2>Compatibility</h2>
<ul>
  <li>IntelliJ IDEA 2024.3+ and all JetBrains IDEs</li>
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

## Screenshots Needed
1. **Real-time detection** — editor view showing highlighted secrets with gutter icons (🔐) and inline warning annotations
2. **Secret details tooltip** — hover popup displaying the detected secret type, masked value, and suggested actions
3. **Settings panel** — configuration page showing all 14 detection patterns with enable/disable toggles
4. **Pre-commit guard** — dialog blocking a commit that contains detected secrets
5. **Git history scan** — results view showing historically committed secrets across the repository
