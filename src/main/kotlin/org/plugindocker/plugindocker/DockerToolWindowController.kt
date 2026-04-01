package org.plugindocker.plugindocker

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.util.concurrency.AppExecutorUtil
import org.dockerservice.ContainerInfo
import org.dockerservice.DockerService
import java.util.concurrent.Executor
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class DockerToolWindowController(
    private val view: DockerToolWindowView,
    private val dockerService: DockerService = DockerService(),
    private val backgroundExecutor: Executor = AppExecutorUtil.getAppExecutorService(),
    private val uiDispatcher: ((() -> Unit) -> Unit) = { action ->
        ApplicationManager.getApplication().invokeLater(action)
    },
    private val terminalInitializer: ((DockerToolWindowView, Disposable) -> Unit)? = null,
) : Disposable {
    companion object {
        private const val ACTION_START = "start"
        private const val ACTION_STOP = "stop"
    }

    private var containers: List<ContainerInfo> = emptyList()
    private var displayedContainers: List<ContainerInfo> = emptyList()

    init {
        attachTerminal()
        bindActions()
        refreshContainers()
    }

    constructor(
        project: Project,
        view: DockerToolWindowView,
        dockerService: DockerService = DockerService(),
        backgroundExecutor: Executor = AppExecutorUtil.getAppExecutorService(),
        uiDispatcher: ((() -> Unit) -> Unit) = { action ->
            ApplicationManager.getApplication().invokeLater(action)
        },
    ) : this(
        view = view,
        dockerService = dockerService,
        backgroundExecutor = backgroundExecutor,
        uiDispatcher = uiDispatcher,
        terminalInitializer = { targetView, parentDisposable ->
            val workingDirectory = project.basePath ?: System.getProperty("user.home")
            val terminalWidget = DockerTerminalRunner(project).createEmbeddedTerminal(parentDisposable, workingDirectory)
            targetView.attachTerminal(terminalWidget.component)
        },
    )

    private fun bindActions() {
        view.refreshButton.addActionListener { refreshContainers() }
        view.startButton.addActionListener {
            executeContainerAction(ACTION_START) { containerId -> dockerService.startContainer(containerId) }
        }
        view.stopButton.addActionListener {
            executeContainerAction(ACTION_STOP) { containerId -> dockerService.stopContainer(containerId) }
        }
        view.searchField.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent?) = applyFilter()
            override fun removeUpdate(e: DocumentEvent?) = applyFilter()
            override fun changedUpdate(e: DocumentEvent?) = applyFilter()
        })
        view.containerTable.selectionModel.addListSelectionListener {
            if (!it.valueIsAdjusting) {
                updateActionButtons()
                loadSelectedContainerDetails()
            }
        }
    }

    private fun attachTerminal() {
        terminalInitializer?.invoke(view, this)
    }

    private fun refreshContainers() {
        view.setLoadingState(true)
        view.statusLabel.text = "Carregando containers..."
        view.detailsArea.text = "Carregando detalhes..."

        backgroundExecutor.execute {
            runCatching { dockerService.listContainers() }
                .onSuccess { loadedContainers ->
                    runOnEdt {
                        containers = loadedContainers
                        applyFilter()
                        view.statusLabel.text = "${loadedContainers.size} container(s) encontrado(s)."
                        view.detailsArea.text = "Selecione um container para inspecionar."
                        view.setLoadingState(false)
                        updateActionButtons()
                    }
                }
                .onFailure { error ->
                    runOnEdt {
                        containers = emptyList()
                        applyFilter()
                        view.statusLabel.text = "Falha ao carregar containers."
                        view.detailsArea.text = error.message ?: error.javaClass.simpleName
                        view.setLoadingState(false)
                        updateActionButtons()
                    }
                }
        }
    }

    private fun loadSelectedContainerDetails() {
        val container = selectedContainer() ?: run {
            view.detailsArea.text = "Selecione um container para inspecionar."
            return
        }

        view.detailsArea.text = "Carregando detalhes de ${container.names}..."

        backgroundExecutor.execute {
            runCatching { dockerService.inspectContainer(container.id) }
                .onSuccess { details ->
                    val text = buildString {
                        appendLine("ID: ${details.id}")
                        appendLine("Imagem: ${details.image}")
                        appendLine("Em execução: ${details.running}")
                        appendLine("Status: ${details.status}")
                        append("Modo de rede: ${details.networkMode}")
                    }
                    runOnEdt {
                        view.detailsArea.text = text
                    }
                }
                .onFailure { error ->
                    runOnEdt {
                        view.detailsArea.text = error.message ?: error.javaClass.simpleName
                    }
                }
        }
    }

    private fun executeContainerAction(actionName: String, operation: (String) -> Unit) {
        val container = selectedContainer() ?: run {
            view.statusLabel.text = "Selecione um container primeiro."
            return
        }

        view.setLoadingState(true)
        view.statusLabel.text = "${actionLabel(actionName)} ${container.names}..."

        backgroundExecutor.execute {
            runCatching { operation(container.id) }
                .onSuccess {
                    runOnEdt {
                        view.statusLabel.text = successMessage(actionName, container.names)
                        refreshContainers()
                    }
                }
                .onFailure { error ->
                    runOnEdt {
                        view.statusLabel.text = failureMessage(actionName)
                        view.detailsArea.text = error.message ?: error.javaClass.simpleName
                        view.setLoadingState(false)
                        updateActionButtons()
                    }
                }
        }
    }

    private fun updateActionButtons() {
        val selectedContainer = selectedContainer()
        val hasSelection = selectedContainer != null
        view.startButton.isEnabled = hasSelection && !selectedContainer.state.equals("running", ignoreCase = true)
        view.stopButton.isEnabled = hasSelection && selectedContainer.state.equals("running", ignoreCase = true)
    }

    private fun selectedContainer(): ContainerInfo? = displayedContainers.getOrNull(view.selectedRow())

    private fun applyFilter() {
        val search = view.searchField.text.trim()
        displayedContainers = if (search.isBlank()) {
            containers
        } else {
            containers.filter { it.names.contains(search, ignoreCase = true) }
        }
        view.renderContainers(displayedContainers)
        view.setNameFilter(search)
    }

    private fun actionLabel(actionName: String): String = when (actionName) {
        ACTION_START -> "Iniciando"
        ACTION_STOP -> "Parando"
        else -> actionName
    }

    private fun successMessage(actionName: String, containerName: String): String = when (actionName) {
        ACTION_START -> "Container $containerName iniciado com sucesso."
        ACTION_STOP -> "Container $containerName parado com sucesso."
        else -> "Ação executada com sucesso em $containerName."
    }

    private fun failureMessage(actionName: String): String = when (actionName) {
        ACTION_START -> "Falha ao iniciar container."
        ACTION_STOP -> "Falha ao parar container."
        else -> "Falha ao executar ação no container."
    }

    private fun runOnEdt(action: () -> Unit) {
        uiDispatcher(action)
    }

    override fun dispose() = Unit
}
