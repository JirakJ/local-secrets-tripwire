# Local Secrets Tripwire — Screenshot Plan

## Screenshots

### 1. Inline Secret Warning — AWS Key
**What to capture:** An editor showing a config file with an AWS access key detected. The line should have an inline warning annotation and a gutter icon. Show the warning highlight on the secret value.

### 2. Hover Tooltip with Secret Details
**What to capture:** The tooltip that appears when hovering over a detected secret. Show the secret type (e.g., "AWS_KEY"), severity (ERROR), masked value (e.g., "AKIA****XXXX"), and entropy score.

### 3. Multiple Secrets Detected
**What to capture:** A file with several different secret types detected — an AWS key, a GitHub token, a private key header, and a generic password. Show gutter icons on each line.

### 4. Settings Page — Secrets Tripwire
**What to capture:** The Settings → Tools → Secrets Tripwire page showing the enable toggle, entropy threshold slider, and the custom patterns table with example entries.

### 5. Allowlist File
**What to capture:** A `.secrets-allowlist` file open in the editor with comments and example entries, showing how false positives are suppressed.

### 6. Entropy Analysis Detection
**What to capture:** A high-entropy string (e.g., a Base64-encoded token) flagged with INFO severity. Show that this string was caught by entropy analysis rather than pattern matching, visible in the tooltip.

### 7. Pre-Commit Guard Dialog
**What to capture:** The commit dialog showing a warning that secrets were detected in staged files. Show the list of detected secrets with file names and the option to cancel the commit.

### 8. Inspection Results Panel
**What to capture:** The Inspection Results panel showing all detected secrets grouped under the Security inspection group, with file locations and severity icons.

---

## Video Demo Outline (40 seconds)

| Time | Scene | Description |
|------|-------|-------------|
| 0:00–0:05 | Intro | Show the plugin name. Open a project in the IDE. |
| 0:05–0:15 | Detection in action | Open a config file containing an AWS key and a GitHub token. Show inline warnings appearing. |
| 0:15–0:22 | Hover details | Hover over a detected secret to show the tooltip with type, severity, and masked value |
| 0:22–0:28 | Entropy detection | Show a high-entropy string being flagged automatically |
| 0:28–0:35 | Allowlist | Open the .secrets-allowlist file, add an entry, show the false positive disappear |
| 0:35–0:40 | Outro | Show pricing (Free + $2.90/mo Pro) and install CTA |
