package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.alert.AlertEngine
import com.example.data.model.AlertSeverity
import com.example.data.model.VitalType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read app name from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("HealthPulse", appName)
  }

  @Test
  fun `alert engine detects normal heart rate`() {
    val eval = AlertEngine.evaluate(VitalType.HEART_RATE, 72.0)
    assertEquals(AlertSeverity.NORMAL, eval.severity)
    assertEquals(false, eval.isAbnormal)
  }

  @Test
  fun `alert engine detects severe hypertension`() {
    val eval = AlertEngine.evaluate(VitalType.BLOOD_PRESSURE, 185.0, 125.0)
    assertEquals(AlertSeverity.CRITICAL, eval.severity)
    assertEquals(true, eval.isAbnormal)
  }

  @Test
  fun `alert engine detects hypoxia`() {
    val eval = AlertEngine.evaluate(VitalType.BLOOD_OXYGEN, 88.0)
    assertEquals(AlertSeverity.CRITICAL, eval.severity)
    assertEquals(true, eval.isAbnormal)
  }
}
