<h2>Local Secrets Tripwire — Real-Time Secret Detection for Your IDE</h2>

<p>
Leaked secrets are the #1 cause of security breaches. Local Secrets Tripwire scans your files in real time for API keys, tokens, passwords, and private keys — catching them <b>before</b> they are committed to version control. All scanning happens locally. Zero data leaves your machine.
</p>

<h3>Detection Engine</h3>
<ul>
  <li>14 built-in patterns for AWS keys, GitHub tokens, Slack tokens, Stripe keys, Google API keys, SendGrid keys, JWTs, private keys, connection strings, and more</li>
  <li>Shannon entropy analysis for high-randomness strings (configurable threshold)</li>
  <li>Custom regex patterns for organization-specific secrets</li>
  <li>Runs as a standard IntelliJ code inspection in the Security group</li>
</ul>

<h3>Editor Integration</h3>
<ul>
  <li>Inline warnings for lines containing potential secrets</li>
  <li>Gutter icons for quick identification of secret locations</li>
  <li>Hover tooltips showing secret type, severity, masked value, and entropy score</li>
  <li>Masked display of detected values for safety</li>
</ul>

<h3>Configuration</h3>
<ul>
  <li>Allowlist support via <code>.secrets-allowlist</code> file for false positive suppression</li>
  <li>Configurable entropy threshold (default 4.5)</li>
  <li>Custom pattern table with ID, name, regex, secret type, severity, and description</li>
  <li>Team-shared settings via <code>.secrets-tripwire.yaml</code> (Pro)</li>
</ul>

<h3>Advanced Features (Pro)</h3>
<ul>
  <li>Pre-commit guard to block commits containing secrets</li>
  <li>Git history scan for previously leaked secrets</li>
  <li>Hard blocking mode to prevent saving files with secrets</li>
  <li>Unlimited custom detection patterns</li>
</ul>

<h3>Pricing</h3>
<ul>
  <li><b>Free:</b> 14 built-in patterns, entropy analysis, inline warnings, gutter icons, allowlist, up to 3 custom patterns</li>
  <li><b>Pro ($2.90/month):</b> Unlimited custom patterns, pre-commit guard, git history scan, hard blocking mode, team-shared configuration</li>
</ul>

<h3>Requirements</h3>
<ul>
  <li>IntelliJ IDEA 2024.3+ (or any JetBrains IDE based on IntelliJ Platform)</li>
  <li>Java 17+</li>
  <li>Git4Idea plugin (optional, for Git integration features)</li>
</ul>

<h3>Support</h3>
<ul>
  <li>GitHub: <a href="https://github.com/JirakJ/local-secrets-tripwire">github.com/JirakJ/local-secrets-tripwire</a></li>
  <li>Issues: <a href="https://github.com/JirakJ/local-secrets-tripwire/issues">Report a Bug</a></li>
  <li>Author: Jakub Jirák</li>
</ul>
