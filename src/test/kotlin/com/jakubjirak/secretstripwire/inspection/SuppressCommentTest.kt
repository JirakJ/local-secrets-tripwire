package com.jakubjirak.secretstripwire.inspection

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class SuppressCommentTest {

    @Test
    fun `uses double-slash comments for code files`() {
        assertEquals(" // tripwire:ignore", SuppressLineQuickFix.suppressComment("Main.kt"))
        assertEquals(" // tripwire:ignore", SuppressLineQuickFix.suppressComment("App.java"))
        assertEquals(" // tripwire:ignore", SuppressLineQuickFix.suppressComment("index.ts"))
    }

    @Test
    fun `uses hash comments for config files`() {
        assertEquals(" # tripwire:ignore", SuppressLineQuickFix.suppressComment("application.yaml"))
        assertEquals(" # tripwire:ignore", SuppressLineQuickFix.suppressComment("app.properties"))
        assertEquals(" # tripwire:ignore", SuppressLineQuickFix.suppressComment(".env"))
        assertEquals(" # tripwire:ignore", SuppressLineQuickFix.suppressComment("Dockerfile"))
    }

    @Test
    fun `returns empty for xml where line comments are unsafe`() {
        assertEquals("", SuppressLineQuickFix.suppressComment("pom.xml"))
        assertEquals("", SuppressLineQuickFix.suppressComment("index.html"))
    }
}
