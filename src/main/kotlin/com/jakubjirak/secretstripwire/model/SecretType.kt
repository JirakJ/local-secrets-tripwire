package com.jakubjirak.secretstripwire.model

enum class SecretType(val displayName: String) {
    API_KEY("API Key"),
    PASSWORD("Password"),
    TOKEN("Token"),
    PRIVATE_KEY("Private Key"),
    CONNECTION_STRING("Connection String"),
    AWS_KEY("AWS Key"),
    GITHUB_TOKEN("GitHub Token"),
    SLACK_TOKEN("Slack Token"),
    STRIPE_KEY("Stripe Key"),
    JWT("JWT Token"),
    GENERIC_SECRET("Generic Secret"),
    HIGH_ENTROPY("High-Entropy String"),
}
