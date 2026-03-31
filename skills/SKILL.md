---
name: plugin-docker-context
description: Contexto de trabalho para o projeto PluginDocker. Use quando precisar implementar, revisar ou estender este plugin IntelliJ que lista e controla containers Docker via DockerService publicado no GitHub Packages.
---

# PluginDocker Contexto

Use este contexto para trabalhar no plugin IntelliJ deste repositório.

## Entender a base

- Tratar este repositório como um plugin IntelliJ em Kotlin com Gradle Kotlin DSL.
- Considerar o arquivo principal de build em `build.gradle.kts`.
- Considerar o manifesto do plugin em `src/main/resources/META-INF/plugin.xml`.
- Considerar a implementação atual da tool window em:
  - `src/main/kotlin/org/plugindocker/plugindocker/DockerWindow.kt`
  - `src/main/kotlin/org/plugindocker/plugindocker/DockerToolWindowView.kt`
  - `src/main/kotlin/org/plugindocker/plugindocker/DockerToolWindowController.kt`

## Respeitar a arquitetura atual

- Manter separação entre view e lógica.
- Colocar componentes Swing, renderização de tabela e estado visual na view.
- Colocar integração com `DockerService`, carregamento assíncrono e regras de ação no controller.
- Deixar a factory apenas montar a view, instanciar o controller e registrar o conteúdo da tool window.

## Dependências relevantes

- Usar `org.dockerservice:docker-service:1.0-SNAPSHOT` como fachada para acesso ao Docker.
- Assumir que esta biblioteca já publica dependências transitivas de `docker-java`.
- Em ambiente IntelliJ, lembrar que JNA pode conflitar com a JNA da IDE. Se reaparecer erro de native library, revisar exclusões da dependência no Gradle.
- Assumir que acesso ao Docker no Windows depende do pipe configurado no `DockerService`. Se houver erro com `dockerDesktopLinuxEngine`, revisar a implementação do projeto que publica essa lib.

## Comportamento atual da UI

- Listar containers na tool window.
- Exibir detalhes do container selecionado.
- Permitir ações de iniciar e parar container.
- Executar chamadas de Docker fora da EDT e atualizar a UI via `ApplicationManager.getApplication().invokeLater`.
- Manter textos da interface em português.

## Ao implementar novas mudanças

- Preservar o fluxo de atualização da lista após ações de container.
- Evitar chamadas bloqueantes na thread da UI.
- Reutilizar o `DockerService` existente em vez de criar integração Docker paralela no plugin.
- Preferir mudanças pequenas e incrementais na tool window antes de introduzir novas camadas.

## Limitações do ambiente

- O projeto usa Gradle 9 e precisa de Java 17+ para validar com `gradlew`.
- Se o ambiente local estiver com Java 8, o build falhará antes da compilação.
- Se não for possível validar com Gradle, fazer checagem estática cuidadosa nos arquivos alterados.
