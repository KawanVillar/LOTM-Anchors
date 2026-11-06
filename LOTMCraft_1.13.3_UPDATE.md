# Atualização para LOTMCraft 1.13.3

## Dependência

- Versão centralizada em `lotm_version`, usada pelo Gradle e pelos metadados NeoForge.
- Arquivo recebido copiado para `libs/lotmcraft-1.13.3.jar`, sem alterar o original.
- SHA-256 atual: `D067E05369387D707F3BDFB57BC7F5CF142B96146B876297221A8191BC8F9736`.
- O metadado interno declara LOTMCraft 1.13.3, Minecraft 1.21.1 e NeoForge 21.1.217 ou superior.
- O arquivo atual tem 114.923.434 bytes e 2.383 classes Java. Substitui o arquivo anterior, que não continha classes do mod.
- Confirmadas as assinaturas de `BeyonderData`, `TeamComponent`, `TeamUtils`, `HonorificNamesEventHandler.storePendingPrayer` e `QuestManager.completeQuest` no JAR atual.
- A tarefa `validateLotmDependency` continua impedindo a compilação contra um arquivo sem `BeyonderData.class`.

## Verificacao antes do commit - 2026-11-05

- `gradlew.bat build` executado com JDK 21 e LOTMCraft 1.13.3 completo: BUILD SUCCESSFUL.
- 317 verificacoes de regressao passaram, incluindo NBT/historico, painel, renderizacao, TOML e sanidade.
- Pacote final inspecionado: sem classes do mod principal, testes, fixtures ou demonstracao local;
  metadados expandidos e configuracao de Mixins presente.
- Cache e downloads do ambiente Gradle foram resolvidos para esta verificacao.
- Testes de fontes unicas e persistencia no jogo informados pelo autor.
- Fluxo de oracao/convite/aceite entre jogadores e comportamento do Team multiplayer continuam pendentes.

## Ponto de partida do Red Priest

A implementação existente em `UniqueAnchorManager.countSubordinatesBySequence` consulta entidades vivas carregadas em todas as dimensões. Para um dono no pathway `red_priest`, conta entidades com `SUBORDINATE_COMPONENT`, `isSubordinate()` verdadeiro e `getControllerUUID()` igual ao UUID do dono. Os valores são calculados por Sequence (1 a 9, além de não-Beyonder na posição 10), usando `AnchorConfig`.

Esse era o ponto de partida antes da integração do Team. A rotina foi ampliada para membros online e vivos com vínculo confirmado nos componentes nativos do líder e do membro. NPCs carregados e vivos continuam usando o componente de subordinação, com exclusão do líder e deduplicação por UUID. A elegibilidade de jogadores segue `TeamUtils.isEligibleLeader`; NPCs vinculados não exigem Sequence <= 3, permitindo regressão para 4 sem perder sua contribuição. O dono ainda precisa permanecer Red Priest. Os totais de estabilidade e o avanço agora incluem essa fonte.

A API nativa foi reinspecionada no JAR completo 1.13.3. A liderança continua exigindo Red Priest Sequence 3 ou inferior. Não há contribuição offline/descarregada nem propagação por hierarquia: somente membros diretos e NPCs controlados pelo líder. As contribuições são recalculadas, sem crédito permanente no saldo normal. NPCs sem attachment de subordinação são ignorados sem criar dados novos. Preservadas as alterações locais já existentes nos subordinados, nas configurações e na rede.
