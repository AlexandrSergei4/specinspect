package com.alki.specinspect.data.storage

interface ReviewPersistentStorage {
    fun read(): String?
    fun write(value: String)
    fun clear()
}

object NoOpReviewPersistentStorage : ReviewPersistentStorage {
    override fun read(): String? = null

    override fun write(value: String) = Unit

    override fun clear() = Unit
}
