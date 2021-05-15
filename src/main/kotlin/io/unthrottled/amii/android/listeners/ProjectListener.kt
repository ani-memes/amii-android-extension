package io.unthrottled.amii.android.listeners

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import io.unthrottled.amii.android.PluginMaster
import io.unthrottled.amii.android.tools.Logging

internal class ProjectListener :
  ProjectManagerListener, Logging {

  override fun projectOpened(project: Project) {
    PluginMaster.instance.projectOpened(project)
  }

  override fun projectClosed(project: Project) {
    PluginMaster.instance.projectClosed(project)
  }
}
