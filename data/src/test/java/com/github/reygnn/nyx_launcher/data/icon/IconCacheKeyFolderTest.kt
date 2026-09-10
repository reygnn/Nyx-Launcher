package com.github.reygnn.nyx_launcher.data.icon

import com.github.reygnn.nyx_launcher.home.model.ComponentKey
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/** Pure JVM tests for folder-preview cache keys. */
class IconCacheKeyFolderTest {

    private fun ck(p: String) = ComponentKey(p, "$p.Main")

    @Test fun same_members_and_size_yield_same_key() {
        val a = IconCacheKey.folder(listOf(ck("pa"), ck("pb")), 96)
        val b = IconCacheKey.folder(listOf(ck("pa"), ck("pb")), 96)
        assertThat(a).isEqualTo(b)
    }

    @Test fun different_member_order_yields_different_key() {
        val a = IconCacheKey.folder(listOf(ck("pa"), ck("pb")), 96)
        val b = IconCacheKey.folder(listOf(ck("pb"), ck("pa")), 96)
        assertThat(a).isNotEqualTo(b)
    }

    @Test fun different_size_yields_different_key() {
        val a = IconCacheKey.folder(listOf(ck("pa"), ck("pb")), 96)
        val b = IconCacheKey.folder(listOf(ck("pa"), ck("pb")), 128)
        assertThat(a).isNotEqualTo(b)
    }

    @Test fun different_membership_yields_different_key() {
        val a = IconCacheKey.folder(listOf(ck("pa"), ck("pb")), 96)
        val b = IconCacheKey.folder(listOf(ck("pa"), ck("pc")), 96)
        assertThat(a).isNotEqualTo(b)
    }
}
