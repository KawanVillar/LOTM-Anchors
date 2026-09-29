# LOTM Anchors — MVP 0.1.0-alpha

Addon NeoForge 1.21.1 para LOTMCraft 1.13.0.

## O que já está implementado

- Dados persistentes de Âncoras por jogador usando Attachment do NeoForge.
- Recompensas por missão através do comando `/anchors give <player> <amount>` — ideal para CustomNPCs.
- `/anchors`, `/anchors check <player>`, `/anchors give`, `/anchors take`, `/anchors set`.
- Integração direta com `BeyonderData.getPathway/getSequence` do LOTMCraft.
- Sequence 4 não exige Âncoras.
- Sequence 3, 2, 1 e 0 usam requisitos configuráveis.
- Integração com `StartAdvanceSequencePathwayEvent` do LOTMCraft: o addon aumenta a chance de falha quando o jogador não tem Âncoras suficientes. A falha real continua sendo executada pelo LOTMCraft, incluindo suas consequências já existentes.
- Integração com o sistema de Nome Honorífico: uma oração válida detectada pelo handler do LOTMCraft pode conceder Âncoras ao alvo, com cooldown por adorador -> alvo para impedir spam.
- Framework opcional de perda gradual por tempo offline, desligado por padrão.

## Regras provisórias

Os valores de quantidade e chance são provisórios e ficam em `config/lotm_anchors-server.toml`.

A fórmula atual de risco é:

`risco = (1 - âncoras atuais / âncoras necessárias) * failure_multiplier`

com limite em `max_failure_chance`.

Isso é deliberadamente simples para podermos revisar o balanceamento durante o desenvolvimento.

## Build

1. Use JDK 21.
2. Abra o projeto no IntelliJ IDEA ou VS Code com suporte Gradle.
3. O JAR exato do LOTMCraft 1.13.0 está incluído em `libs/lotmcraft-1.13.0.jar` apenas para compilação.
4. Execute `gradlew build`.

NeoForge 1.21.1 oficialmente requer Java 21. Consulte a documentação oficial do NeoForge para o fluxo de MDK/build.

## Próximas etapas

- Separar um sistema de `AnchorSource` para quests, NPCs, players e outras fontes.
- Criar quests próprias do addon.
- Definir regras definitivas de Âncoras por Sequence.
- Definir exatamente como orações devem acumular Âncoras.
- GUI `/anchors`.
- Histórico detalhado de transações.
- Possível integração nativa com CustomNPCs, se necessário.


## Integração confirmada com o JAR analisado

O código foi estruturado usando as APIs encontradas diretamente no `lotmcraft-1.13.0(3).jar`:

- `de.jakob.lotm.util.BeyonderData#getPathway/getSequence`
- `de.jakob.lotm.events.custom.StartAdvanceSequencePathwayEvent`
- `de.jakob.lotm.events.HonorificNamesEventHandler#answerState`
- `de.jakob.lotm.util.playerMap.PlayerMap` / `HonorificName` como sistema já existente de reconhecimento dos nomes honrosos

A descoberta mais importante foi `StartAdvanceSequencePathwayEvent`: o LOTMCraft publica esse evento antes de verificar `Math.random() < failureChance`. O addon usa esse ponto para aumentar o risco por falta de Âncoras, sem duplicar a lógica de morte/regressão/característica do LOTMCraft.

### Observação sobre a versão

O arquivo fornecido é `lotmcraft-1.13.0(3).jar`, porém o `META-INF/neoforge.mods.toml` interno analisado declara `version="1.0.0"`. Por isso o `neoforge.mods.toml` do addon não exige literalmente `1.13.0`; ele exige LOTMCraft `>=1.0.0` e o projeto inclui o JAR 1.13.0 fornecido em `libs/` para manter o desenvolvimento alinhado ao arquivo analisado.
