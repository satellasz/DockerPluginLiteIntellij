package org.plugindocker.plugindocker

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.terminal.ui.TerminalWidget
import org.jetbrains.plugins.terminal.LocalTerminalDirectRunner
import org.jetbrains.plugins.terminal.shellStartupOptions

class DockerTerminalRunner(project: Project) : LocalTerminalDirectRunner(project) {
    fun createEmbeddedTerminal(disposable: Disposable, workingDirectory: String): TerminalWidget {
        val startupOptions = shellStartupOptions(workingDirectory)
        return startShellTerminalWidget(disposable, startupOptions, true)
    }
}
