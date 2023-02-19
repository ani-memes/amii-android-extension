package io.unthrottled.amii.android.listeners

import com.android.tools.idea.gradle.project.build.BuildContext
import com.android.tools.idea.gradle.project.build.BuildStatus
import com.android.tools.idea.gradle.project.build.GradleBuildListener
import com.android.tools.idea.gradle.project.build.invoker.GradleBuildInvoker
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import io.unthrottled.amii.events.EVENT_TOPIC
import io.unthrottled.amii.events.UserEvent
import io.unthrottled.amii.events.UserEventCategory
import io.unthrottled.amii.events.UserEventListener
import io.unthrottled.amii.events.UserEvents
import io.unthrottled.amii.tools.PluginMessageBundle

internal enum class TaskStatus {
  PASS, FAIL, UNKNOWN
}

class GradleListener(
  private val project: Project
) :
  GradleBuildListener,
  UserEventListener,
  Disposable {

  private var previousTaskStatus = TaskStatus.UNKNOWN

  private val messageBusConnection = project.messageBus.connect()

  init {
    ApplicationManager.getApplication().invokeLater {
      messageBusConnection.subscribe(EVENT_TOPIC, this)
    }
  }

  override fun onDispatch(userEvent: UserEvent) {
    previousTaskStatus = when (userEvent.type) {
      UserEvents.TASK -> {
        when (userEvent.category) {
          UserEventCategory.NEGATIVE -> TaskStatus.FAIL
          UserEventCategory.POSITIVE -> TaskStatus.PASS
          else -> TaskStatus.UNKNOWN
        }
      }

      else -> TaskStatus.UNKNOWN
    }
  }

  override fun dispose() {
    messageBusConnection.dispose()
  }

  override fun buildExecutorCreated(request: GradleBuildInvoker.Request) {
  }

  override fun buildStarted(context: BuildContext) {
  }

  override fun buildFinished(status: BuildStatus, context: BuildContext?) {
    when {
      status == BuildStatus.FAILED -> {
        project.messageBus
          .syncPublisher(EVENT_TOPIC)
          .onDispatch(
            UserEvent(
              UserEvents.TASK,
              UserEventCategory.NEGATIVE,
              PluginMessageBundle.message("user.event.task.failure.name"),
              project
            )
          )
      }

      previousTaskStatus == TaskStatus.FAIL &&
        status == BuildStatus.SUCCESS -> {
        project.messageBus
          .syncPublisher(EVENT_TOPIC)
          .onDispatch(
            UserEvent(
              UserEvents.TASK,
              UserEventCategory.POSITIVE,
              PluginMessageBundle.message("user.event.task.success.name"),
              project
            )
          )
      }
    }
  }
}
