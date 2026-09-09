package com.github.reygnn.nyx_launcher.data.icon

import com.github.reygnn.nyx_launcher.home.model.IconRef
import java.security.MessageDigest

/**
 * Pure, deterministic keying (ICL-INV-5). A [CacheKey] and its disk file name
 * are a stable function of `(component, sizePx, variant)` — so disk hits survive
 * a process restart (cold start shows icons without re-compositing).
 *
 * The file name is `"<pkgHash>-<contentHash>.webp"`. The `pkgHash` PREFIX lets
 * `evict(pkg)` delete a package's files by glob (`"<pkgHash>-*.webp"`) without a
 * reverse-hash or a disk index (ICON_LOADER_SPEC §8-D7 / §6).
 */
object IconCacheKey {

    fun of(ref: IconRef, sizePx: Int, variant: IconVariant): CacheKey {
        val key = when (ref) {
            is IconRef.System -> ref.key
            is IconRef.Pack -> ref.key
        }
        val packId = (ref as? IconRef.Pack)?.packId.orEmpty()
        val content = listOf(
            key.packageName, key.className, key.userSerial.toString(),
            sizePx.toString(), variant.name, packId,
        ).joinToString("|")
        return CacheKey("${shortHash(key.packageName)}-${shortHash(content)}")
    }

    fun fileName(key: CacheKey): String = "${key.raw}.webp"

    /** Prefix shared by every file of [pkg]; glob `"$prefix-*.webp"` on evict. */
    fun packagePrefix(pkg: String): String = shortHash(pkg)

    private fun shortHash(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return buildString {
            for (i in 0 until 8) append("%02x".format(digest[i])) // 16 hex chars
        }
    }
}
