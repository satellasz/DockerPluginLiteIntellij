# Testing

Este projeto possui testes unitários focados na regra da tool window de Docker.

## Onde estão

- Testes: `src/test/kotlin/org/plugindocker/plugindocker/`
- Arquivo atual principal: `DockerToolWindowControllerTest.kt`

## O que está coberto

- carregamento inicial da lista de containers
- habilitação dos botões `Iniciar` e `Parar` conforme o estado do container
- carregamento de detalhes ao selecionar um container
- execução das ações de `start` e `stop`
- filtro por nome do container
- seleção correta após ordenação da tabela

## Estratégia

- A lógica testada fica no `DockerToolWindowController`
- A `DockerToolWindowView` permanece como camada de UI Swing
- O controller aceita `Executor` e dispatcher de UI injetáveis para permitir testes síncronos
- Os testes usam um `FakeDockerService` para evitar dependência de Docker real

## Como rodar

Use:

```powershell
.\gradlew.bat test
```

## Requisitos de ambiente

- Gradle 9 exige Java 17 ou superior para executar os testes
- Se o ambiente estiver com Java 8, o Gradle falhará antes da compilação

## Observações

- Os testes atuais são unitários e não validam integração real com Docker
- Casos ligados ao IntelliJ Platform runtime podem exigir testes de integração separados
- Se houver erro ligado a listener do IntelliJ/JUnit, revisar a configuração de dependências de teste no `build.gradle.kts`
