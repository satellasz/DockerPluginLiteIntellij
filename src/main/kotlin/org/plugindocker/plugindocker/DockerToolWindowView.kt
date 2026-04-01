package org.plugindocker.plugindocker

import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBTabbedPane
import com.intellij.ui.table.JBTable
import com.intellij.openapi.Disposable
import org.dockerservice.ContainerInfo
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import javax.swing.JComponent
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JSplitPane
import javax.swing.JTextField
import javax.swing.JTextArea
import javax.swing.ListSelectionModel
import javax.swing.table.DefaultTableModel
import javax.swing.table.TableRowSorter

class DockerToolWindowView : Disposable {
    private val tableModel = object : DefaultTableModel(arrayOf("ID", "Nome", "Image", "Estado", "Status"), 0) {
        override fun isCellEditable(row: Int, column: Int) = false
    }
    private val rowSorter = TableRowSorter(tableModel)

    val containerTable = JBTable(tableModel).apply {
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
        setShowGrid(false)
        autoCreateRowSorter = false
        rowSorter = this@DockerToolWindowView.rowSorter
    }
    val statusLabel = JBLabel("Carregando containers...")
    val detailsArea = JTextArea().apply {
        isEditable = false
        lineWrap = true
        wrapStyleWord = true
    }
    val searchField = JTextField(20)
    val refreshButton = JButton("Atualizar")
    val startButton = JButton("Iniciar").apply { isEnabled = false }
    val stopButton = JButton("Parar").apply { isEnabled = false }
    private val terminalHost = JBPanel<JBPanel<*>>(BorderLayout())

    val content = JBPanel<JBPanel<*>>(BorderLayout()).apply {
        add(buildToolbar(), BorderLayout.NORTH)
        add(buildCenterPanel(), BorderLayout.CENTER)
        add(statusLabel, BorderLayout.SOUTH)
    }

    fun selectedRow(): Int {
        val selectedViewRow = containerTable.selectedRow
        return if (selectedViewRow >= 0) containerTable.convertRowIndexToModel(selectedViewRow) else -1
    }

    fun renderContainers(items: List<ContainerInfo>) {
        tableModel.rowCount = 0
        items.forEach { container ->
            tableModel.addRow(
                arrayOf(
                    container.id.take(12),
                    container.names,
                    container.image,
                    container.state,
                    container.status,
                )
            )
        }
    }

    fun setNameFilter(filterText: String) {
        val normalized = filterText.trim()
        rowSorter.rowFilter = if (normalized.isBlank()) {
            null
        } else {
            javax.swing.RowFilter.regexFilter("(?i)${Regex.escape(normalized)}", 1)
        }
    }

    fun setLoadingState(isLoading: Boolean) {
        searchField.isEnabled = !isLoading
        refreshButton.isEnabled = !isLoading
        containerTable.isEnabled = !isLoading
        if (isLoading) {
            startButton.isEnabled = false
            stopButton.isEnabled = false
        }
    }

    fun attachTerminal(component: JComponent) {
        terminalHost.removeAll()
        terminalHost.add(component, BorderLayout.CENTER)
        terminalHost.revalidate()
        terminalHost.repaint()
    }

    fun hasAttachedTerminal(): Boolean = terminalHost.componentCount > 0

    private fun buildToolbar(): JPanel = JPanel(FlowLayout(FlowLayout.LEFT)).apply {
        add(JLabel("Buscar:"))
        add(searchField)
        add(refreshButton)
        add(startButton)
        add(stopButton)
    }

    private fun buildCenterPanel(): JSplitPane {
        val detailsScroll = ScrollPaneFactory.createScrollPane(detailsArea)
        detailsScroll.preferredSize = Dimension(0, 140)
        terminalHost.preferredSize = Dimension(0, 220)

        val bottomTabs = JBTabbedPane().apply {
            addTab("Detalhes", detailsScroll)
            addTab("Terminal", terminalHost)
        }

        return JSplitPane(
            JSplitPane.VERTICAL_SPLIT,
            ScrollPaneFactory.createScrollPane(containerTable),
            bottomTabs,
        ).apply {
            resizeWeight = 0.75
            dividerSize = 6
        }
    }

    override fun dispose() = Unit
}
