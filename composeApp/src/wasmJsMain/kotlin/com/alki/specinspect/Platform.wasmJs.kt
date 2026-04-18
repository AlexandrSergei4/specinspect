package com.alki.specinspect

/**
 * Реализация Platform для WASM (браузер)
 */
class WasmPlatform : Platform {
    override val name: String = "Web (WASM)"
}

actual fun getPlatform(): Platform = WasmPlatform()
