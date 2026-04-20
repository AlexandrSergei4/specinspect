package com.alki.specinspect.data.storage

import platform.Foundation.NSUserDefaults

class IosSpecificationPersistentStorage : SpecificationPersistentStorage {

    private val defaults = NSUserDefaults.standardUserDefaults

    override fun read(): String? = defaults.stringForKey(SPECS_KEY)

    override fun write(value: String) {
        defaults.setObject(value, forKey = SPECS_KEY)
    }

    override fun clear() {
        defaults.removeObjectForKey(SPECS_KEY)
    }
}

private const val SPECS_KEY = "persisted_specifications"
