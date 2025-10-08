package org.balch.recipes

import com.diamondedge.logging.FixedLogLevel
import com.diamondedge.logging.KmLogging
import com.diamondedge.logging.PrintLogger
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext

class LoggingExtension : BeforeAllCallback, AfterAllCallback {
    override fun beforeAll(context: ExtensionContext) {
        KmLogging.setLoggers(PrintLogger(FixedLogLevel(true)))
    }

    override fun afterAll(context: ExtensionContext) {
        KmLogging.clear()
    }
}
