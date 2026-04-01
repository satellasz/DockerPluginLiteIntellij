# Testing

Este projeto possui testes unitarios focados na regra da tool window de Docker.

## Onde estao

- Testes: `src/test/kotlin/org/plugindocker/plugindocker/`
- Arquivo atual principal: `DockerToolWindowControllerTest.kt`

## O que esta coberto

- carregamento inicial da lista de containers
- habilitacao dos botoes `Iniciar` e `Parar` conforme o estado do container
- carregamento de detalhes ao selecionar um container
- execucao das acoes de `start` e `stop`
- filtro por nome do container
- selecao correta apos ordenacao da tabela
- anexo do terminal integrado na tool window por meio de inicializador injetavel

## Estrategia

- A logica testada fica no `DockerToolWindowController`
- A `DockerToolWindowView` permanece como camada de UI Swing
- O controller aceita `Executor` e dispatcher de UI injetaveis para permitir testes sincronos
- A integracao do terminal e injetada no controller para permitir teste unitario sem subir o runtime real do Terminal da IDE
- Os testes usam um `FakeDockerService` para evitar dependencia de Docker real

## Como rodar

Use:

```powershell
.\gradlew.bat test
```

## Requisitos de ambiente

- Gradle 9 exige Java 17 ou superior para executar os testes
- Se o ambiente estiver com Java 8, o Gradle falhara antes da compilacao

## Observacoes

- Os testes atuais sao unitarios e nao validam integracao real com Docker
- O teste do terminal valida o encaixe do componente na view, mas nao inicializa uma sessao real do terminal do IntelliJ Platform
- Casos ligados ao IntelliJ Platform runtime podem exigir testes de integracao separados
- Se houver erro ligado a listener do IntelliJ/JUnit, revisar a configuracao de dependencias de teste no `build.gradle.kts`
