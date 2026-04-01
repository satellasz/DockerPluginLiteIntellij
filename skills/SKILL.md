---
name: plugin-docker-context
description: Contexto de trabalho para o projeto PluginDocker. Use quando precisar implementar, revisar ou estender este plugin IntelliJ em Kotlin que lista containers Docker, exibe detalhes, executa start/stop e incorpora um terminal integrado na tool window usando o plugin Terminal da plataforma.
---

# PluginDocker Contexto

Use este contexto para trabalhar no plugin IntelliJ deste repositorio.

## Entender a base

- Tratar este repositorio como um plugin IntelliJ em Kotlin com Gradle Kotlin DSL.
- Considerar o arquivo principal de build em `build.gradle.kts`.
- Considerar o manifesto do plugin em `src/main/resources/META-INF/plugin.xml`.
- Considerar a implementacao atual da tool window em:
  - `src/main/kotlin/org/plugindocker/plugindocker/DockerWindow.kt`
  - `src/main/kotlin/org/plugindocker/plugindocker/DockerToolWindowView.kt`
  - `src/main/kotlin/org/plugindocker/plugindocker/DockerToolWindowController.kt`
  - `src/main/kotlin/org/plugindocker/plugindocker/DockerTerminalRunner.kt`

## Respeitar a arquitetura atual

- Manter separacao entre view e logica.
- Colocar componentes Swing, tabela, abas e estado visual na view.
- Colocar integracao com `DockerService`, carregamento assincrono e regras de acao no controller.
- Manter a criacao do terminal encapsulada em `DockerTerminalRunner`.
- Deixar a factory apenas montar a view, instanciar o controller e registrar o conteudo da tool window.

## Dependencias relevantes

- Usar `org.dockerservice:docker-service:1.0-SNAPSHOT` como fachada para acesso ao Docker.
- Declarar `com.intellij.modules.platform`, `com.intellij.java` e `org.jetbrains.plugins.terminal` no `plugin.xml`.
- Declarar o plugin bundled `org.jetbrains.plugins.terminal` no `build.gradle.kts` para compilar a integracao do terminal.
- Assumir que a biblioteca `docker-service` ja publica dependencias transitivas de `docker-java`.
- Em ambiente IntelliJ, lembrar que JNA pode conflitar com a JNA da IDE. Se reaparecer erro de native library, revisar exclusoes da dependencia no Gradle.
- Assumir que acesso ao Docker no Windows depende do pipe configurado no `DockerService`. Se houver erro com `dockerDesktopLinuxEngine`, revisar a implementacao do projeto que publica essa lib.

## Comportamento atual da UI

- Listar containers na tool window.
- Exibir detalhes do container selecionado.
- Permitir acoes de iniciar e parar container.
- Exibir uma aba `Terminal` integrada na parte inferior da tool window.
- Criar o terminal via `LocalTerminalDirectRunner.startShellTerminalWidget(...)`.
- Executar chamadas de Docker fora da EDT e atualizar a UI via `ApplicationManager.getApplication().invokeLater`.
- Manter textos da interface em portugues.

## Ao implementar novas mudancas

- Preservar o fluxo de atualizacao da lista apos acoes de container.
- Evitar chamadas bloqueantes na thread da UI.
- Reutilizar o `DockerService` existente em vez de criar integracao Docker paralela no plugin.
- Evitar API `@ApiStatus.Internal` do plugin Terminal quando houver alternativa publica.
- Aceitar o tema padrao da IDE para o terminal integrado; nao forcar tema com APIs deprecated ou instaveis.
- Preferir mudancas pequenas e incrementais na tool window antes de introduzir novas camadas.

## Testes

- Considerar `src/test/kotlin/org/plugindocker/plugindocker/DockerToolWindowControllerTest.kt` como suite principal.
- O controller aceita inicializador de terminal injetavel para permitir teste unitario do encaixe do terminal sem subir o runtime real da IDE.
- Se alterar a integracao do terminal, atualizar tambem `skills/TESTING.md`.

## Limitacoes do ambiente

- O projeto usa Gradle 9 e precisa de Java 17+ para validar com `gradlew`.
- Se o ambiente local estiver com Java 8, o build falhara antes da compilacao.
- Se nao for possivel validar com Gradle, fazer checagem estatica cuidadosa nos arquivos alterados.
