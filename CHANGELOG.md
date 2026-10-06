# Changelog

Todas as alterações relevantes deste projeto estão documentadas neste arquivo.

O formato segue uma organização cronológica inversa, com base no histórico de commits do repositório.

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
- [`dbe1bb3`](https://github.com/KawanVillar/LOTM-Anchors/commit/dbe1bb3d6adcfb52e0b7534bfc0d88feefd1b72f) — `docs: adicionar licença proprietária`

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

[0.1.3]: https://github.com/KawanVillar/LOTM-Anchors/compare/0.1.0-alpha...master
[0.1.0-alpha]: https://github.com/KawanVillar/LOTM-Anchors/commit/1507ea78a70de7679d299f704774a974235373f1
