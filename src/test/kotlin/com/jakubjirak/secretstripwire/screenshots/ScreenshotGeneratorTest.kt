package com.jakubjirak.secretstripwire.screenshots

import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.jakubjirak.secretstripwire.inspection.SecretInspection
import com.jakubjirak.secretstripwire.settings.SecretsTripwireConfigurable
import java.awt.Component
import java.awt.Container
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import javax.swing.JComponent

/**
 * Renders real plugin UI to PNG files for the README and the JetBrains
 * Marketplace listing. Disabled by default; opt in with:
 *
 *   ./gradlew test --tests "*ScreenshotGeneratorTest*" -PscreenshotsDir=$(pwd)/docs/screenshots
 */
class ScreenshotGeneratorTest : BasePlatformTestCase() {

    private val outputDir: File?
        get() = System.getProperty("secretstripwire.screenshots.dir")
            ?.takeIf { it.isNotBlank() }
            ?.let { File(it) }

    fun testRenderScreenshots() {
        val dir = outputDir ?: return // opt-in only
        dir.mkdirs()

        renderEditorWithDetections(dir)
        renderSettingsPanel(dir)

        val written = dir.listFiles { f -> f.extension == "png" }?.toList().orEmpty()
        assertTrue("No screenshots were produced", written.isNotEmpty())
        for (file in written) {
            assertTrue("Screenshot ${file.name} looks empty", file.length() > 5_000)
        }
    }

    private fun renderEditorWithDetections(dir: File) {
        myFixture.enableInspections(SecretInspection())
        myFixture.configureByText(
            "application.properties",
            """
            # Demo configuration — every value below is a synthetic example
            server.port=8080
            aws.access-key=AKIAIOSFODNN7EXAMPLE
            github.token=ghp_ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmn
            stripe.key=sk_live_ABCDEFGHIJKLMNOPQRSTUVWXyz
            db.url=jdbc:mysql://admin:hunter2pass@db.internal:3306/prod
            docs.sample=AKIAIOSFODNN7EXAMPLE # tripwire:ignore
            """.trimIndent(),
        )
        myFixture.doHighlighting()

        val editor = myFixture.editor as EditorEx
        editor.settings.isLineNumbersShown = true
        render(editor.component, 980, 360, File(dir, "editor-secret-detection.png"))
    }

    private fun renderSettingsPanel(dir: File) {
        val configurable = SecretsTripwireConfigurable()
        val panel = configurable.createComponent() ?: error("Settings panel not created")
        configurable.reset()
        render(panel as JComponent, 980, 560, File(dir, "settings-panel.png"))
        configurable.disposeUIResources()
    }

    private fun render(component: JComponent, width: Int, height: Int, target: File) {
        component.setBounds(0, 0, width, height)
        layoutRecursively(component)

        // 2x for crisp marketplace images
        val image = BufferedImage(width * 2, height * 2, BufferedImage.TYPE_INT_RGB)
        val g = image.createGraphics()
        g.scale(2.0, 2.0)
        g.color = component.background ?: java.awt.Color.WHITE
        g.fillRect(0, 0, width, height)
        component.print(g)
        g.dispose()

        ImageIO.write(image, "png", target)
    }

    private fun layoutRecursively(component: Component) {
        component.doLayout()
        if (component is Container) {
            for (child in component.components) {
                layoutRecursively(child)
            }
        }
    }
}
