package org.plugindocker.plugindocker

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

class DockerWindowFactory : ToolWindowFactory {
    override fun shouldBeAvailable(project: Project) = true

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val view = DockerToolWindowView()
        val controller = DockerToolWindowController(project, view)

        val content = ContentFactory.getInstance().createContent(view.content, null, false)
        content.setDisposer(controller)
        toolWindow.contentManager.addContent(content)
        Disposer.register(controller, view)
    }
}
