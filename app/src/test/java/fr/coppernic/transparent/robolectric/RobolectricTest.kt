package fr.coppernic.invengo.killtag.robolectric

import fr.coppernic.sdk.cpcutils.BuildConfig
import org.awaitility.Awaitility.await
import org.junit.Assert.assertTrue
import org.junit.BeforeClass
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog
import java.util.concurrent.atomic.AtomicBoolean


/**
 * Base class extended by every Robolectric test in this project.
 * <p>
 * Robolectric tests are done in a single thread !
 */
@RunWith(RobolectricTestRunner::class)
abstract class RobolectricTest {

    companion object {
        @BeforeClass
        @JvmStatic
        fun beforeClass() {
            //Configure robolectric
            ShadowLog.stream = System.out
        }
    }

    private val unblock = AtomicBoolean(false)

    fun sleep(ms: Long) {
        try {
            Thread.sleep(ms)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

    }

    fun unblock() {
        unblock.set(true)
    }

    fun block() {
        await().untilTrue(unblock)
    }

    fun doNotGoHere() {
        assertTrue(false)
    }

}