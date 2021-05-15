package io.unthrottled.amii.android.tools

import com.intellij.util.Alarm

fun registerDelayedRequest(
  alarm: Alarm,
  delay: Int,
  runner: () -> Unit
) {
  if (alarm.isDisposed) return
  alarm.addRequest(runner, delay)
}
