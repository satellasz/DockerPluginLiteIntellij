package org.plugindocker.plugindocker

import org.dockerservice.ContainerDetails
import org.dockerservice.ContainerInfo
import org.dockerservice.DockerService
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DockerToolWindowControllerTest {
    private val immediateExecutor = java.util.concurrent.Executor { runnable -> runnable.run() }
    private val immediateUiDispatcher: ((() -> Unit) -> Unit) = { action -> action() }

    @Test
    fun `carrega containers ao inicializar`() {
        val service = FakeDockerService(
            containers = mutableListOf(
                runningContainer(),
                stoppedContainer(),
            )
        )
        val view = DockerToolWindowView()

        DockerToolWindowController(view, service, immediateExecutor, immediateUiDispatcher)

        assertEquals("2 container(s) encontrado(s).", view.statusLabel.text)
        assertEquals(2, view.containerTable.rowCount)
        assertEquals("Selecione um container para inspecionar.", view.detailsArea.text)
        assertTrue(view.refreshButton.isEnabled)
        assertFalse(view.startButton.isEnabled)
        assertFalse(view.stopButton.isEnabled)
    }

    @Test
    fun `habilita parar e carrega detalhes para container em execucao`() {
        val service = FakeDockerService(containers = mutableListOf(runningContainer()))
        val view = DockerToolWindowView()

        DockerToolWindowController(view, service, immediateExecutor, immediateUiDispatcher)
        view.containerTable.setRowSelectionInterval(0, 0)

        assertFalse(view.startButton.isEnabled)
        assertTrue(view.stopButton.isEnabled)
        assertTrue(view.detailsArea.text.contains("Imagem: nginx:latest"))
        assertTrue(view.detailsArea.text.contains("Em execução: true"))
    }

    @Test
    fun `habilita iniciar para container parado`() {
        val service = FakeDockerService(containers = mutableListOf(stoppedContainer()))
        val view = DockerToolWindowView()

        DockerToolWindowController(view, service, immediateExecutor, immediateUiDispatcher)
        view.containerTable.setRowSelectionInterval(0, 0)

        assertTrue(view.startButton.isEnabled)
        assertFalse(view.stopButton.isEnabled)
    }

    @Test
    fun `filtra containers por nome`() {
        val service = FakeDockerService(
            containers = mutableListOf(
                runningContainer(name = "/api"),
                stoppedContainer(name = "/worker"),
                runningContainer(id = "zzz999888777", name = "/web"),
            )
        )
        val view = DockerToolWindowView()

        DockerToolWindowController(view, service, immediateExecutor, immediateUiDispatcher)
        view.searchField.text = "wor"

        assertEquals(1, view.containerTable.rowCount)
        assertEquals("/worker", view.containerTable.getValueAt(0, 1))
    }

    @Test
    fun `mantem acao correta apos ordenar por nome`() {
        val apiContainer = stoppedContainer(id = "api123456789", name = "/api")
        val webContainer = runningContainer(id = "web123456789", name = "/web")
        val service = FakeDockerService(containers = mutableListOf(webContainer, apiContainer))
        val view = DockerToolWindowView()

        DockerToolWindowController(view, service, immediateExecutor, immediateUiDispatcher)
        view.containerTable.rowSorter.toggleSortOrder(1)
        view.containerTable.setRowSelectionInterval(0, 0)
        view.startButton.doClick()

        assertEquals(listOf(apiContainer.id), service.startedIds)
    }

    @Test
    fun `aciona start e recarrega lista`() {
        val initialContainer = stoppedContainer()
        val startedContainer = runningContainer(id = initialContainer.id, name = initialContainer.names)
        val service = FakeDockerService(containers = mutableListOf(initialContainer)).apply {
            onStart = { containerId ->
                containers[0] = startedContainer
                inspected[containerId] = detailsFrom(startedContainer, true)
            }
        }
        val view = DockerToolWindowView()

        DockerToolWindowController(view, service, immediateExecutor, immediateUiDispatcher)
        view.containerTable.setRowSelectionInterval(0, 0)
        view.startButton.doClick()

        assertEquals(listOf(initialContainer.id), service.startedIds)
        assertEquals("1 container(s) encontrado(s).", view.statusLabel.text)
        assertEquals("running", view.containerTable.getValueAt(0, 3))
    }

    @Test
    fun `aciona stop e recarrega lista`() {
        val initialContainer = runningContainer()
        val stoppedContainer = stoppedContainer(id = initialContainer.id, name = initialContainer.names)
        val service = FakeDockerService(containers = mutableListOf(initialContainer)).apply {
            onStop = { containerId ->
                containers[0] = stoppedContainer
                inspected[containerId] = detailsFrom(stoppedContainer, false)
            }
        }
        val view = DockerToolWindowView()

        DockerToolWindowController(view, service, immediateExecutor, immediateUiDispatcher)
        view.containerTable.setRowSelectionInterval(0, 0)
        view.stopButton.doClick()

        assertEquals(listOf(initialContainer.id), service.stoppedIds)
        assertEquals("1 container(s) encontrado(s).", view.statusLabel.text)
        assertEquals("exited", view.containerTable.getValueAt(0, 3))
    }

    private class FakeDockerService(
        var containers: MutableList<ContainerInfo>,
    ) : DockerService() {
        val inspected = containers.associate { it.id to detailsFrom(it, it.state.equals("running", ignoreCase = true)) }.toMutableMap()
        val startedIds = mutableListOf<String>()
        val stoppedIds = mutableListOf<String>()
        var onStart: (String) -> Unit = {}
        var onStop: (String) -> Unit = {}

        override fun listContainers(): MutableList<ContainerInfo> = containers.toMutableList()

        override fun inspectContainer(id: String): ContainerDetails = inspected.getValue(id)

        override fun startContainer(id: String) {
            startedIds += id
            onStart(id)
        }

        override fun stopContainer(id: String) {
            stoppedIds += id
            onStop(id)
        }
    }

    companion object {
        private fun runningContainer(
            id: String = "abc123456789",
            name: String = "/web",
        ) = ContainerInfo(id, name, "nginx:latest", "running", "Up 2 minutes")

        private fun stoppedContainer(
            id: String = "def987654321",
            name: String = "/worker",
        ) = ContainerInfo(id, name, "busybox:1.36", "exited", "Exited (0) 1 minute ago")

        private fun detailsFrom(container: ContainerInfo, running: Boolean) = ContainerDetails(
            container.id,
            container.image,
            running,
            container.status,
            "bridge",
        )
    }
}
