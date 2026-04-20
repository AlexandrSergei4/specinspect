package com.alki.specinspect.data.storage

interface SpecificationPersistentStorage {
    fun read(): String?
    fun write(value: String)
    fun clear()
}

object NoOpSpecificationPersistentStorage : SpecificationPersistentStorage {
    override fun read(): String? = null

    override fun write(value: String) = Unit

    override fun clear() = Unit
}
