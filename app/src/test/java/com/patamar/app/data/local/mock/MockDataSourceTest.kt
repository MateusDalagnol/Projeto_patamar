package com.patamar.app.data.local.mock

import com.patamar.app.data.model.EventCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class MockDataSourceTest {

    private val events = MockDataSource.events

    @Test fun `tem 62 eventos (12 originais + 50 novos) com ids unicos`() {
        assertEquals(62, events.size)
        assertEquals(events.size, events.map { it.id }.toSet().size)
        assertEquals(events.size, events.map { it.name }.toSet().size)
    }

    @Test fun `todas as categorias tem variedade`() {
        EventCategory.values().forEach { category ->
            val count = events.count { it.category == category }
            assertTrue("$category tem só $count evento(s)", count >= 5)
        }
    }

    @Test fun `todos os eventos ficam em Curitiba`() {
        events.forEach {
            assertTrue("${it.name} lat ${it.lat}", it.lat in -25.55..-25.30)
            assertTrue("${it.name} lng ${it.lng}", it.lng in -49.40..-49.15)
            assertTrue("${it.name} distancia ${it.distanceMeters}", it.distanceMeters in 1..30_000)
        }
    }

    @Test fun `datas ficam nas proximas semanas`() {
        val today = LocalDate.now()
        events.forEach {
            assertTrue("${it.name} em ${it.date}", !it.date.isBefore(today) && !it.date.isAfter(today.plusDays(21)))
        }
    }

    @Test fun `ha eventos gratuitos, em destaque e no fim de semana suficientes`() {
        assertTrue(events.count { it.isFree } >= 25)
        assertTrue(events.count { it.isHighlighted } >= 12)
        assertTrue(events.count { it.date.dayOfWeek.value >= 5 } >= 10)
    }

    @Test fun `todo evento tem texto e foto`() {
        events.forEach {
            assertTrue(it.name.isNotBlank() && it.description.isNotBlank() && it.address.isNotBlank())
            assertTrue(it.imageUrl.startsWith("https://"))
        }
    }
}
