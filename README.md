# Docker Containers

`Docker Containers` e um plugin para IntelliJ IDEA criado para facilitar o acompanhamento de containers Docker sem sair da IDE.

Em vez de alternar entre terminal, Docker Desktop e ambiente de desenvolvimento, o plugin concentra as acoes mais comuns em uma tool window simples e direta. A proposta e dar visibilidade rapida ao que esta em execucao e permitir intervencoes basicas durante o fluxo de trabalho.

## Visao geral

O plugin foi pensado para quem desenvolve, testa ou valida aplicacoes que dependem de containers locais. Ele ajuda a manter o contexto dentro do IntelliJ, reduzindo interrupcoes e tornando mais facil acompanhar o estado dos servicos que suportam o projeto.

Ao abrir a janela do plugin, a pessoa usuaria consegue visualizar os containers disponiveis, selecionar um item para inspecao e executar acoes imediatas quando necessario.

## Funcionalidades

- Lista os containers disponiveis em uma janela dedicada dentro da IDE.
- Exibe informacoes essenciais para identificacao rapida, como nome, imagem, estado e status.
- Permite buscar containers pelo nome para localizar itens com mais agilidade.
- Mostra detalhes do container selecionado para apoiar consulta e verificacao.
- Permite iniciar um container parado.
- Permite parar um container em execucao.
- Atualiza a lista apos as acoes, mantendo a visao sempre coerente com o estado atual.

## Experiencia dentro da IDE

O foco do plugin e oferecer uma experiencia objetiva: selecionar, consultar e agir. Isso o torna util em cenarios como:

- subir um servico de apoio antes de rodar a aplicacao;
- interromper um container que nao precisa mais ficar ativo;
- conferir rapidamente se o ambiente local esta no estado esperado;
- inspecionar um container sem sair do fluxo de desenvolvimento.

## Para quem este plugin e util

Este plugin faz sentido para pessoas e equipes que usam Docker com frequencia durante o desenvolvimento e querem manter a operacao basica dos containers mais perto do codigo, dentro do proprio IntelliJ.
