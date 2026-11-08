# Changelog

Current release notes are provided in English for publication. Older entries
retain their original language.

## [1.0.3] - 2026-11-08

- Restrict unique-source contributions to the controller current dimension, including Team members, NPCs, avatars, marionettes and Personas. Persistent faith bonds remain dimension-independent.
- Replace repeated world scans per player with a shared per-dimension, per-tick owner index. Continue validating native control, life and contribution values on every query.
- Invalidate candidate indexes on entity/player lifecycle events and clear them at shutdown. Manual panels and advancement checks force a fresh membership scan.
- Remove a duplicate follower Sequence refresh pass and add scan-count, scope-isolation, ownership-change and cache-lifecycle regression checks.
- No additional chunk-loading requests or network protocol changes.

- Fixed dedicated-server startup failure caused by resolving client GUI classes during packet registration.
- Deferred client handler resolution for the overview, Sources and History packets until client-side packet handling.
- Added a regression check that blocks client classes while constructing packet handlers.
- Corrected internal mod metadata and the generated JAR version to 1.0.3.
- Network protocol remains 8; no world-data format changes.

## [1.0.2] - 2026-11-06

- Recognize verified native source ownership across pathways, including borrowed abilities and Visionary roles, without unlocking blocked native abilities.
- Keep Red Priest Team leadership requirements; allow valid controlled NPC sources on other pathways.
- Prevent one entity body from contributing through multiple categories, with priority for marionette control.
- Show extra tabs only for existing sources and use two rows for more than three categories.
- Exclude dead sources and invalid owners from contribution; share the same collection between overview, panel and stability calculation.
- Update network protocol to 8; client and server must use the same addon build.
- Add regression checks for cross-pathway snapshots, codecs, body priority and dynamic tab layouts.

## [1.0.0] - 2026-11-05

Updated for **LOTMCraft 1.13.3**, **Minecraft 1.21.1** and **NeoForge 21.1.217+**.

### Faith and anchor sources

- Integrated native honorific-name prayers with follower invitations. The praying player must accept before the bond contributes anchors.
- Follower contributions now scale with their own Sequence. Bonds and last known Sequences persist while followers are offline.
- Added native sources: Red Priest Team members and subordinate NPCs, Error avatars, Fool marionettes and Visionary virtual personas.
- Improved ownership checks, Team validation and duplicate prevention.
- Preserved valid Red Priest NPC contributions after death-related regression to Sequence 4.

### Interface and history

- Added a Sources panel with pathway-specific tabs, Sequence ordering, contribution values and detailed tooltips.
- Added follower removal with confirmation and a separate History screen for the latest 32 events.
- Added next-Sequence requirements, missing anchors and additional advancement risk to the overview.
- Fixed blurred text, improved layouts across GUI scales and corrected Back to sources navigation.
- Removed the fictional-data preview. All addon interface text is in English.

### Optional sanity integration

- Added gradual pressure on native sanity when players in Sequences 3–0 lack anchors.
- Pressure scales with the anchor deficit and Sequence, with configurable grace periods and warnings.
- Added a preview of the next Sequence's sanity ceiling using current sources and the current native maximum.
- Disabled by default. Enable `[anchor_sanity] enabled = true` in the world's server configuration to use it.

### Configuration and fixes

- Reorganized the server TOML with clearer sections and English explanations.
- Corrected optional offline decay to respect the configured rate and elapsed time.
- Added automated regression checks for faith, persistence, history, source validation, networking, layouts, configuration and sanity.

### Installation notes

- Install the same addon version on the client and server, together with LOTMCraft 1.13.3 and its required dependencies.
- The complete LOTMCraft mod is required; it is not bundled with this addon.
- Existing configuration values are preserved. New options are added when the updated mod loads.
- The full prayer/invitation/consent flow between players and borrowed-ability gameplay combinations still require in-game validation.

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
