# LOTM Anchors

[![Build](https://github.com/KawanVillar/LOTM-Anchors/actions/workflows/build.yml/badge.svg?branch=master)](https://github.com/KawanVillar/LOTM-Anchors/actions/workflows/build.yml)
[![Licença](https://img.shields.io/badge/licen%C3%A7a-All%20Rights%20Reserved-red)](LICENSE)

> ⚠️ **Status:** projeto em **alpha** (MVP), com regras de balanceamento ainda provisórias.
>
> 🔒 **Licença:** este repositório é **proprietário (All Rights Reserved)**. Consulte `LICENSE` antes de copiar, modificar ou redistribuir.

Addon NeoForge para Minecraft 1.21.1 integrado ao LOTMCraft.

O LOTM Anchors adiciona um sistema de **Âncoras** persistentes por jogador, usado para representar estabilidade durante o avanço de Sequences. O addon também integra recompensas por quests, orações através de Nomes Honoríficos e um sistema opcional de perda de Âncoras após longos períodos offline.

## Requisitos

- Java Development Kit (JDK) 21;
- Git;
- Minecraft 1.21.1;
- NeoForge 21.1.217 ou superior;
- LOTMCraft 1.13.0;
- IntelliJ IDEA ou VS Code com suporte a Gradle, opcionalmente;
- acesso à internet no primeiro build para baixar dependências do Gradle, NeoForge e Minecraft.

O JAR usado para compilação está incluído em:

```text
libs/lotmcraft-1.13.0.jar
```

Essa é uma dependência local do projeto. Qualquer redistribuição deve respeitar a licença e a origem desse artefato.

## Instalação para jogadores

1. Compile o projeto (ou obtenha um build confiável compatível).
2. Copie o JAR gerado em `build/libs/` para a pasta `mods`.
3. Garanta que o ambiente usa versões compatíveis de Minecraft, NeoForge e LOTMCraft.

## Instalação para desenvolvimento

Clone o repositório e entre na pasta do projeto:

```bash
git clone https://github.com/KawanVillar/LOTM-Anchors.git
cd LOTM-Anchors
```

Abra a pasta no IntelliJ IDEA ou no VS Code e importe o projeto como um projeto Gradle.

### Windows

```bat
gradlew.bat clean build
```

### Linux/macOS

Depois de renomear o wrapper Unix para `gradlew`, caso necessário, conceda permissão de execução:

```bash
chmod +x gradlew
./gradlew clean build
```

O JAR compilado será gerado em:

```text
build/libs/
```

## Execução do ambiente de desenvolvimento

O projeto possui configurações Gradle para cliente, servidor dedicado e GameTest Server.

### Cliente

```bash
./gradlew runClient
```

No Windows:

```bat
gradlew.bat runClient
```

### Servidor dedicado

```bash
./gradlew runServer
```

No Windows:

```bat
gradlew.bat runServer
```

### GameTest Server

```bash
./gradlew runGameTestServer
```

No Windows:

```bat
gradlew.bat runGameTestServer
```

## Funcionalidades implementadas

- Dados persistentes de Âncoras por jogador usando Attachments do NeoForge;
- comandos administrativos para conceder, remover e definir Âncoras;
- consulta de pathway, Sequence, quantidade de Âncoras e risco adicional;
- requisitos configuráveis para avanço às Sequences 3, 2, 1 e 0;
- integração com `BeyonderData.getPathway` e `BeyonderData.getSequence` do LOTMCraft;
- integração com `StartAdvanceSequencePathwayEvent`;
- aumento do risco de falha quando o jogador possui menos Âncoras que o requisito configurado;
- integração com orações por Nome Honorífico;
- cooldown por adorador e alvo para impedir farming por spam;
- recompensas por quests do LOTMCraft através do sistema de integração do addon;
- perda opcional de Âncoras por tempo offline, desativada por padrão.

## Comandos

| Comando | Permissão | Descrição |
|---|---:|---|
| `/anchors` | Jogador | Mostra suas Âncoras, Sequence, requisito seguinte e risco adicional. |
| `/anchors check <player>` | Jogador | Consulta os dados de outro jogador. |
| `/anchors give <player> <amount>` | OP nível 2 | Concede Âncoras ao jogador. |
| `/anchors take <player> <amount>` | OP nível 2 | Remove Âncoras do jogador. |
| `/anchors set <player> <amount>` | OP nível 2 | Define diretamente a quantidade de Âncoras. |

## Configuração

Depois que o servidor iniciar, o arquivo de configuração será criado em:

```text
config/lotm_anchors-server.toml
```

As opções principais incluem:

- `required_sequence_3`: Âncoras necessárias para avançar à Sequence 3;
- `required_sequence_2`: Âncoras necessárias para avançar à Sequence 2;
- `required_sequence_1`: Âncoras necessárias para avançar à Sequence 1;
- `required_sequence_0`: Âncoras necessárias para avançar à Sequence 0;
- `failure_multiplier`: multiplicador aplicado ao déficit de Âncoras;
- `max_failure_chance`: limite máximo do risco adicional;
- `prayer_reward`: recompensa por oração válida;
- `prayer_cooldown_minutes`: cooldown por adorador e alvo;
- `announce_prayer_reward`: informa ao adorador quando a oração é reconhecida;
- `offline_decay.enabled`: ativa ou desativa a perda por ausência prolongada;
- `offline_decay.grace_hours`: período de tolerância antes da perda;
- `offline_decay.percent_per_day`: percentual de perda configurado;
- `offline_decay.interval_hours`: intervalo de aplicação da perda.

Exemplo documentado de configuração:

```text
docs/example-server-config.toml
```

A perda por tempo offline fica desativada por padrão enquanto as regras do servidor ainda estão em desenvolvimento.

A fórmula atual do risco adicional é:

```text
risco = (1 - âncoras atuais / âncoras necessárias) * failure_multiplier
```

O resultado é limitado por `max_failure_chance`. A lógica original de falha, regressão, morte e consequências do LOTMCraft continua sendo executada pelo próprio LOTMCraft.

## Compatibilidade

O desenvolvimento e os testes são direcionados para:

| Componente | Versão |
|---|---|
| Minecraft | 1.21.1 |
| NeoForge | 21.1.217 ou superior |
| Java | 21 |
| LOTMCraft | 1.13.0 |
| Gradle Wrapper | Gradle 8.8 |

## Compatibilidade e limitações de Mixins/APIs internas

O projeto compila contra o arquivo local `libs/lotmcraft-1.13.0.jar`.

O JAR analisado do LOTMCraft declara internamente a versão `1.0.0` no seu `neoforge.mods.toml`. Por esse motivo, a dependência declarada pelo addon utiliza a faixa `[1.0.0,)`, embora o desenvolvimento esteja direcionado ao LOTMCraft 1.13.0.

Alterações nas APIs internas do LOTMCraft podem exigir atualizações no addon, especialmente nas seguintes integrações:

- `de.jakob.lotm.util.BeyonderData`;
- `de.jakob.lotm.events.custom.StartAdvanceSequencePathwayEvent`;
- `de.jakob.lotm.events.HonorificNamesEventHandler#answerState`;
- sistema de quests do LOTMCraft.

## Estrutura do projeto

```text
src/main/java/                  Código Java do addon
src/main/resources/             Metadados NeoForge e configuração de Mixins
libs/                           Dependência local do LOTMCraft
build.gradle                    Configuração do Gradle e NeoForge
gradle.properties               Versões e propriedades do projeto
gradle/wrapper/                 Gradle Wrapper
gradlew                         Wrapper Unix/Linux/macOS
gradlew.bat                     Wrapper Windows
settings.gradle                 Configuração do projeto Gradle
```

Os diretórios `build/`, `.gradle/`, `run/` e arquivos gerados pelo ambiente de desenvolvimento não devem ser versionados.

## Contribuição

Consulte [CONTRIBUTING.md](CONTRIBUTING.md) para fluxo de branches, testes obrigatórios, atualização de changelog e cuidados com Mixins/APIs internas do LOTMCraft.

## Limitações atuais

- O projeto ainda está em fase MVP/alpha;
- o balanceamento das Âncoras é provisório;
- a interface `/anchors` ainda está em evolução e pode mudar entre versões alpha;
- o histórico detalhado de transações ainda não foi implementado;
- o sistema de `AnchorSource` ainda não foi separado para quests, NPCs, jogadores e outras fontes;
- quests próprias do addon ainda não foram implementadas;
- a integração nativa com CustomNPCs ainda não foi implementada;
- as regras definitivas de acumulação por oração ainda estão em desenvolvimento;
- mudanças na API do LOTMCraft podem quebrar a compilação ou as integrações em runtime.

## Próximas etapas

- separar um sistema de `AnchorSource`;
- criar quests próprias do addon;
- finalizar as regras de Âncoras por Sequence;
- revisar o balanceamento de orações e recompensas;
- expandir a GUI para consulta detalhada de Âncoras;
- implementar histórico de transações;
- avaliar integração nativa com CustomNPCs.

## Licença

Este projeto é disponibilizado sob todos os direitos reservados. Consulte o arquivo `LICENSE` para as condições de uso, cópia, modificação e redistribuição.
