package com.socialwork.academy

import com.socialwork.academy.navigation.Routes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.lang.reflect.Modifier

/** Guards the "fully offline" promise and basic project sanity. */
class OfflineTest {

    private fun read(path: String): String {
        val file = File(path)
        assertTrue("$path not found at ${file.absolutePath}", file.exists())
        return file.readText(Charsets.UTF_8)
    }

    @Test
    fun manifestAsksForNoNetworkPermission() {
        val manifest = read("src/main/AndroidManifest.xml").replace(Regex("<!--.*?-->", RegexOption.DOT_MATCHES_ALL), "")
        assertFalse(manifest.contains("android.permission.INTERNET"))
        assertFalse(manifest.contains("ACCESS_NETWORK_STATE"))
        assertFalse(manifest.contains("usesCleartextTraffic"))
    }

    @Test
    fun noNetworkAdsOrTrackingLibrariesAreUsed() {
        val gradle = read("build.gradle.kts").lowercase()
        listOf("okhttp", "retrofit", "volley", "ktor", "firebase", "admob", "play-services", "coil", "glide", "analytics")
            .forEach { assertFalse("network/tracking library '$it' found in build.gradle.kts", gradle.contains(it)) }
    }

    @Test
    fun sourceCodeHasNoWebAddresses() {
        val offenders = File("src/main/java").walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .filter { it.readText(Charsets.UTF_8).contains("http://") || it.readText(Charsets.UTF_8).contains("https://") }
            .map { it.name }.toList()
        assertTrue("web addresses found in: $offenders", offenders.isEmpty())
    }

    @Test
    fun appSupportsRightToLeftLayouts() {
        assertTrue(read("src/main/AndroidManifest.xml").contains("android:supportsRtl=\"true\""))
    }

    @Test
    fun routeNamesAreUnique() {
        val routes = Routes::class.java.declaredFields
            .filter { Modifier.isStatic(it.modifiers) && it.type == String::class.java }
            .map { it.apply { isAccessible = true }.get(null) as String }
        assertTrue(routes.size >= 20)
        assertEquals(routes.size, routes.toSet().size)
    }
}
