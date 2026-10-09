package com.focusguard.diagnostics

import java.util.concurrent.CopyOnWriteArrayList

class DiagnosticLogRepository {
    private val buffer = CopyOnWriteArrayList<DiagnosticEntry>()
    private val maxSize = 2000

    fun append(entry: DiagnosticEntry) {
        buffer.add(entry)
        while (buffer.size > maxSize) {
            buffer.removeAt(0)
        }
    }

    fun snapshot(): List<DiagnosticEntry> = buffer.toList()

    fun clear() {
        buffer.clear()
    }
}
