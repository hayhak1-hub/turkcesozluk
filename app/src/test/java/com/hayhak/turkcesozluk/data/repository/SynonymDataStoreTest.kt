package com.hayhak.turkcesozluk.data.repository

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SynonymDataStoreTest {

    @Before
    fun setUp() = SynonymDataStore.clear()

    @After
    fun tearDown() {
        SynonymDataStore.clear()
        SynonymDataStore.updateCache(emptySet())
    }

    @Test
    fun `putAll exposes the loaded data`() {
        SynonymDataStore.putAll(mapOf("güzel" to setOf("hoş", "şirin")))
        assertEquals(setOf("hoş", "şirin"), SynonymDataStore.synonymMap["güzel"])
    }

    @Test
    fun `addSynonym merges into an existing entry instead of replacing it`() {
        SynonymDataStore.putAll(mapOf("güzel" to setOf("hoş")))
        SynonymDataStore.addSynonym("güzel", "şirin")
        assertEquals(setOf("hoş", "şirin"), SynonymDataStore.synonymMap["güzel"])
    }

    @Test
    fun `addSynonym creates a missing entry`() {
        SynonymDataStore.addSynonym("yeni", "taze")
        assertEquals(setOf("taze"), SynonymDataStore.synonymMap["yeni"])
    }

    @Test
    fun `cachedMeanings is rebuilt after the data changes`() {
        SynonymDataStore.putAll(mapOf("güzel" to setOf("hoş")))
        assertEquals(listOf("hoş"), SynonymDataStore.cachedMeanings)

        SynonymDataStore.addSynonym("güzel", "şirin")
        assertEquals(setOf("hoş", "şirin"), SynonymDataStore.cachedMeanings.toSet())
    }

    @Test
    fun `updateCache uses the presorted lists when their size matches`() {
        SynonymDataStore.putAll(mapOf("b" to setOf("x"), "a" to setOf("y")))
        // Kasıtlı olarak sıralı olmayan bir liste veriyoruz: boyut uyuştuğu için
        // olduğu gibi kullanılmalı (yeniden sıralama maliyetinden kaçınmak için).
        SynonymDataStore.updateCache(setOf("a"), preSortedKeys = listOf("b", "a"), preSortedPrimary = listOf("a"))
        assertEquals(listOf("b", "a"), SynonymDataStore.cachedKeys)
        assertEquals(listOf("a"), SynonymDataStore.primaryKeys)
    }

    @Test
    fun `updateCache re-sorts when the presorted list is stale`() {
        SynonymDataStore.putAll(mapOf("b" to setOf("x"), "a" to setOf("y")))
        // Boyutu uyuşmayan (bayat) liste yok sayılmalı.
        SynonymDataStore.updateCache(setOf("a", "b"), preSortedKeys = listOf("b"), preSortedPrimary = listOf("a"))
        assertEquals(listOf("a", "b"), SynonymDataStore.cachedKeys)
        assertEquals(listOf("a", "b"), SynonymDataStore.primaryKeys)
    }

    @Test
    fun `clear empties the map`() {
        SynonymDataStore.putAll(mapOf("güzel" to setOf("hoş")))
        SynonymDataStore.clear()
        assertTrue(SynonymDataStore.synonymMap.isEmpty())
        assertTrue(SynonymDataStore.cachedMeanings.isEmpty())
    }
}
