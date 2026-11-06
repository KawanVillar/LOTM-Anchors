# Changelog

Alteracoes relevantes do LOTM Anchors. As entradas anteriores documentam o historico;
a secao Unreleased descreve o estado que sera publicado no proximo commit.

## [Unreleased] - 2026-11-05

### Dependencia e repositorio

- Atualizado para LOTMCraft 1.13.3 completo; versao centralizada no Gradle e metadados.
- Validacao de `BeyonderData.class` antes da compilacao e processamento de recursos.
- JAR do mod principal deixa de ser versionado; instrucoes em `libs/README.md`.
- Regras de Git para excluir caches, IDEs, logs, mundos e artefatos de build.

### Fe e fontes de ancoras

- Oracao nativa integrada a convites com consentimento, expiracao e protecao contra reutilizacao.
- Um patrono por fiel; `/faith leave` e `/faith remove` encerram vinculos.
- Contribuicao por Sequence, persistencia da ultima Sequence/nome para fieis offline.
- Red Priest Team com validacao reciproca, limites nativos e deduplicacao por UUID.
- NPCs subordinados carregados preservam contribuicao apos regressao para Sequence 4,
  enquanto o controlador continuar Red Priest.
- Fontes exclusivas de Error, Fool e Visionary coletadas em conjunto com o painel
  e o calculo de estabilidade, sem creditos permanentes no saldo normal.

### Interface e historico

- Painel Sources por pathway com ordenacao da Sequence mais forte para a mais fraca,
  paginacao, tooltips e confirmacao de remocao de fieis.
- Corrigido desfoque sobre o conteudo e layout para diferentes escalas de GUI.
- Textos do addon em ingles; dados ficticios removidos da distribuicao e mantidos somente nos testes.
- Tela History com 32 registros persistentes por jogador, sem novos avisos no chat.
- Mudancas nos totais das fontes unicas precisam persistir por 30 segundos para serem registradas.
- Back to sources retorna localmente, preservando aba/pagina, sem depender de resposta do servidor pausado.
- Protocolo de rede atual: **7**. Cliente e servidor precisam da mesma versao do addon.

### Sanidade e configuracao

- Integracao opcional com a sanidade nativa, desativada por padrao, para deficit nas Sequences 3 a 0.
- Teto proporcional ao deficit e ao maximo nativo, queda gradual e tolerancias de entrada,
  respawn, mudanca de Sequence e deficit continuo. Sem cura automatica ou drenagem abaixo do teto pelo addon.
- Avisos por estagio e previa dos requisitos/deficit/teto da proxima Sequence no resumo.
- TOML organizado em secoes, com unidades e exemplos em ingles, preservando chaves ativas.
- Removidas quatro opcoes obsoletas de oracao sem efeito.
- Perda offline agora respeita percentual diario e intervalo configurados; continua opcional.
- Exemplo TOML gerado pela definicao real do NeoForge.

### Verificacao

- Testes de Team, consentimento, NBT, historico, codecs, permissoes, layout, TOML,
  perda offline e sanidade incluidos em `check` e `build`.
- Testes solo de fontes unicas e persistencia informados pelo autor.
- Fluxo completo de fe entre jogadores ainda pendente de validacao multiplayer.

## [0.1.4] - 2026-10-07

### Adicionado

- Adicionado o cálculo de Âncoras únicas geradas por Avatares do Error pertencentes ao jogador.
- Adicionado o agrupamento de Avatares do Error por Sequence, abrangendo as Sequences 1, 2, 3 e 4.
- Adicionadas configurações para definir a quantidade de Âncoras únicas concedidas por Avatar do Error de cada Sequence.
- Adicionado o comando `/anchors debug avatars`, disponível para operadores, para consultar a quantidade de Avatares do Error e o total de Âncoras únicas calculadas.
- Adicionada uma interface gráfica de informações para consultar as Âncoras de um jogador.
- A interface exibe o pathway, Sequence, nome da Sequence, quantidade atual de Âncoras, requisitos para o próximo avanço, Âncoras faltantes, risco adicional, Âncoras obtidas ao longo da vida e informações de Âncoras únicas.
- Adicionado o comando `/anchors info` para abrir a interface do próprio jogador.
- Adicionada a opção `/anchors info <player>` para consultar a interface de outro jogador, disponível para operadores.
- O comando `/anchors` agora abre diretamente a interface gráfica em vez de exibir as informações apenas no chat.
- Adicionada comunicação cliente-servidor por payload de rede para transmitir os dados da interface.
- Adicionado o registro do canal de rede `lotm_anchors:open_anchor_info` com protocolo `1`.
- Atualizada a interface gráfica para exibir Âncoras únicas por grupo e por Sequence.
- Adicionado o valor total de estabilidade, combinando Âncoras normais e Âncoras únicas.
- Adicionada formatação de números com separadores de milhares para facilitar a leitura na interface.
- Ajustado o layout da interface para utilizar duas colunas e acomodar as informações adicionais.

### Commits

- [`9fad96f`](https://github.com/KawanVillar/LOTM-Anchors/commit/9fad96f323feadcea630e42d4ba9844e195ebb8e) — `feat: display unique anchors in anchor info screen`
- [`f43b204`](https://github.com/KawanVillar/LOTM-Anchors/commit/f43b20436e7dc14b5cabb4416d3bd6ffbfb6e752) — `feat: implement Error avatar unique anchors and debug command`
- [`61a7405`](https://github.com/KawanVillar/LOTM-Anchors/commit/61a74052834ba058bce5d245905b36827f774c54) — `feat: adicionado sistema de âncoras na interface e conexão via rede.`

## [0.1.3] - 2026-10-06

### Alterado

- Ajustado o tratamento de perda de Âncoras no `PlayerLifecycleHandler` para limitar a perda aplicada após ausência prolongada do jogador.
- A perda aplicada nessa rotina passou a considerar 20% das Âncoras atuais.
- Atualizadas para inglês as mensagens exibidas ao jogador sobre risco de falha, perda de Âncoras e recompensas por oração.
- Adicionado o `HonorificNamesEventHandlerMixin` para observar a conclusão de orações por Nome Honorífico.
- Registrado o novo mixin na configuração `lotm_anchors.mixins.json`.

### Commits

- [`6ac3748`](https://github.com/KawanVillar/LOTM-Anchors/commit/6ac374a8fe1ed34396b6698bc8afb46bbae6a692) — `feat: limitar perda de ancoras na morte no PlayerLifecycleHandler`
- [`9d4cb44`](https://github.com/KawanVillar/LOTM-Anchors/commit/9d4cb4473e131754018eaede680eb1b8ad733ce3) — `feat: limitar perda de ancoras na morte no PlayerLifecycleHandler`
- [`bfb2331`](https://github.com/KawanVillar/LOTM-Anchors/commit/bfb233194b726a9982b2909c5694028672c5f417) — `feat: limitar perda de ancoras na morte no PlayerLifecycleHandler`

## [0.1.3] - 2026-10-05

### Documentação e build

- Atualizado o README com instruções de instalação, comandos de build, execução dos ambientes de cliente, servidor e GameTest, compatibilidade, configuração e limitações atuais.
- Ajustada a estrutura de uso do Gradle Wrapper para o projeto.
- Atualizada a versão do mod de `0.1.0-alpha` para `0.1.3`.
- Configurado o nome dos arquivos JAR para incluir a versão e um timestamp de geração.
- Adicionada a licença proprietária do projeto.

### Commits

- [`6868567`](https://github.com/KawanVillar/LOTM-Anchors/commit/6868567c3bcbf5832d3d03e867b6149f357e9d61) — `docs: atualizar README e corrigir Gradle Wrapper`
- [`b0cd100`](https://github.com/KawanVillar/LOTM-Anchors/commit/b0cd1004999731317d7f914332c23513a95dbe6d) — `docs: atualizar README com setup, comandos e compatibilidade`

## Correções estruturais e de integração - 2026-10-05

### Corrigido

- Corrigida a estrutura do projeto, removendo a cópia antiga localizada no diretório `lotm_anchors/` e consolidando os arquivos na raiz do repositório.
- Removidos arquivos de auditoria, documentação e configuração duplicados da estrutura anterior.

### Commits

- [`12b5476`](https://github.com/KawanVillar/LOTM-Anchors/commit/12b5476254b8e2e3090185fd765e38212b4b8d14) — `Fix: Correção da estrutura do projeto`

### Integração de quests

- Corrigida a concessão de Âncoras após o descarte/conclusão de quests.
- Substituído o acompanhamento baseado em `PlayerTickEvent` por uma integração via Mixin com o gerenciador de quests do LOTMCraft.
- Adicionado o suporte de Mixin ao Gradle e ao `neoforge.mods.toml`.
- Centralizada a recompensa de quests no método `rewardCompletedQuestFromMixin`.
- Mantido o controle de recompensas repetidas e o tratamento especial da quest `defend_village`.

### Commits

- [`e61af76`](https://github.com/KawanVillar/LOTM-Anchors/commit/e61af76b3908af9bd8f815269ee0792fe0c92803) — `Fix: Correção do bug ao discartar a quest ganhar ancoras.`

## [0.1.0-alpha] - 2026-09-29

### Adicionado

- Criada a estrutura inicial do addon NeoForge para Minecraft 1.21.1 e LOTMCraft 1.13.0.
- Implementados dados persistentes de Âncoras por jogador usando Attachments do NeoForge.
- Adicionado o gerenciamento de Âncoras, incluindo concessão, remoção, definição direta e persistência em NBT.
- Adicionados os comandos `/anchors`, `/anchors check`, `/anchors give`, `/anchors take` e `/anchors set`.
- Implementada a configuração de requisitos de Âncoras para avanço às Sequences 3, 2, 1 e 0.
- Integrada a estabilidade de avanço por meio de `StartAdvanceSequencePathwayEvent`, aumentando o risco de falha quando o jogador possui menos Âncoras que o requisito configurado.
- Integrada a leitura de pathway e Sequence por meio de `BeyonderData` do LOTMCraft.
- Adicionadas recompensas por orações válidas usando Nome Honorífico, com cooldown por adorador e alvo para evitar farming por spam.
- Adicionado suporte opcional à perda de Âncoras por tempo offline, desativado por padrão.
- Adicionada a estrutura Gradle com NeoForge, Java 21, Gradle Wrapper e dependência local do LOTMCraft.
- Adicionada documentação inicial sobre APIs do LOTMCraft, configuração, build e setup no Windows.
- Adicionada a licença proprietária do projeto na configuração inicial.

### Commits

- [`1507ea7`](https://github.com/KawanVillar/LOTM-Anchors/commit/1507ea78a70de7679d299f704774a974235373f1) — `init: configuração inicial e estrutura base do projeto`

[0.1.4]: https://github.com/KawanVillar/LOTM-Anchors/compare/0.1.3...master
[0.1.3]: https://github.com/KawanVillar/LOTM-Anchors/compare/0.1.0-alpha...master
[0.1.0-alpha]: https://github.com/KawanVillar/LOTM-Anchors/commit/1507ea78a70de7679d299f704774a974235373f1
