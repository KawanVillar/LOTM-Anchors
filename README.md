# LOTM Anchors

Addon para **Minecraft 1.21.1 / NeoForge**, integrado ao **LOTMCraft 1.13.3**.
Interface, mensagens e comentarios da configuracao em ingles.

Versao atual: **1.0.0**. Balanceamento configuravel.

## Funcionalidades

- Ancoras persistentes e estabilidade para o avanco de Sequences, usando o sistema nativo de falha.
- Recompensas por quests, com protecao contra repeticao conforme configuracao.
- Fe integrada a oracao por nomes honorificos: o destinatario convida e o orador aceita ou recusa.
- Contribuicao dos fieis ponderada pela propria Sequence; ultima Sequence conhecida preservada offline.
- Fontes unicas de origem: Red Priest Team/NPCs, Error avatars, Fool marionettes e Visionary virtual personas.
- Painel `/anchors sources` com categorias nativas e fontes vinculadas, ordenacao por Sequence mais forte, paginacao e detalhes ao passar o mouse.
- Remocao de fieis com confirmacao; as entidades do mod principal sao somente de consulta.
- Historico persistente com os ultimos 32 eventos, em tela separada e sem avisos extras no chat.
- Pressao opcional sobre a sanidade nativa quando faltam ancoras, com tolerancias e avisos por estagio.
- Previa de requisitos, deficit, risco adicional e teto de sanidade da proxima Sequence.
- Perda offline opcional, proporcional ao percentual diario e ao tempo configurados.

As contribuicoes dinamicas sao recalculadas; nao sao creditadas permanentemente
no saldo de ancoras normais. Fontes descarregadas podem deixar de contribuir.

## Requisitos e build

- JDK 21.
- LOTMCraft **1.13.3 completo**, baixado separadamente.
- Internet no primeiro build para baixar Gradle, NeoForge e Minecraft.

```powershell
git clone https://github.com/KawanVillar/LOTM-Anchors.git
cd LOTM-Anchors
```

Coloque o mod em `libs/lotmcraft-1.13.3.jar`. Veja [libs/README.md](libs/README.md).
Nao basta um patch contendo somente recursos: `validateLotmDependency` verifica
se o arquivo contem `BeyonderData.class`.

```powershell
.\gradlew.bat build
```

No Linux/macOS, use `./gradlew build` com Java 21. O primeiro build pode demorar
para preparar o ambiente Minecraft. Os JARs ficam em `build/libs/`.
O arquivo de distribuicao desta versao e `lotm_anchors-1.0.0.jar`.

Instale o addon e o LOTMCraft na pasta `mods` do cliente e do servidor.
Cliente e servidor precisam da mesma versao do addon: **protocolo 8**.
Nao inclua `libs/`, caches, instancias de teste ou JARs gerados no commit.
O Gradle Wrapper, incluindo seu JAR, faz parte do repositorio.

## Comandos

| Comando | Funcao |
| --- | --- |
| `/anchors` | Resumo de estabilidade |
| `/anchors sources` | Fontes, vinculos e acesso a History |
| `/anchors info <player>` | Consultar outro jogador; operador nivel 2 |
| `/anchors give <player> <amount>` | Adicionar ancoras normais; operador nivel 2 |
| `/anchors take <player> <amount>` | Remover ancoras normais; operador nivel 2 |
| `/anchors set <player> <amount>` | Definir ancoras normais; operador nivel 2 |
| `/faith` | Consultar vinculos de fe |
| `/faith leave` | Encerrar o proprio vinculo como fiel |
| `/faith remove <UUID>` | Remover um dos seus fieis |

Convites e respostas usam os botoes enviados no chat apos a oracao nativa.
O vinculo so concede contribuicao apos consentimento; cada fiel tem um patrono.
A consulta de outro jogador e restrita a operadores. Remover fieis pelo painel
exige ser o proprio dono do vinculo, mesmo para operadores.

## Configuracao

O arquivo SERVER fica em `<world>/serverconfig/lotm_anchors-server.toml`.
Use `defaultconfigs/` para definir valores iniciais de mundos novos.
Nao e necessario apagar configuracoes existentes para atualizar.

[examples/lotm_anchors-server.toml](examples/lotm_anchors-server.toml) e um exemplo
gerado pela propria definicao NeoForge. As regras sao globais por mundo/servidor;
valores de fontes especiais podem ser ajustados por categoria e Sequence.

| Secao | Conteudo |
| --- | --- |
| `[anchors]` | Requisitos de estabilidade, risco e convites |
| `[follower_values]` | Contribuicao por Sequence dos fieis |
| `[unique_anchors]` e subsecoes | Avatares, marionetes, personas e subordinados |
| `[quests]` | Recompensas e regras de quests |
| `[offline_decay]` | Perda offline opcional |
| `[anchor_sanity]` e `[anchor_sanity.max_penalty]` | Sanidade, tolerancias e penalidades |

### Sanidade

Desativada por padrao. Para ativar, altere no TOML do mundo e reinicie:

```toml
[anchor_sanity]
enabled = true
```

Nas Sequences 3 a 0, o deficit proporcional reduz gradualmente o teto de sanidade.
Com zero ancoras, os tetos padrao sao 85%, 70%, 50% e 35% do maximo nativo,
respectivamente. Metade das ancoras exigidas aplica metade da penalidade.

O requisito e o da **Sequence atual**. O addon nao cura nem reduz sanidade que ja
esteja abaixo do teto; o mod principal continua podendo aplicar seus proprios efeitos.
A reducao usa o setter sincronizado nativo como limite estrutural, sem reaplicar
os multiplicadores de dano de sanidade. Recuperar ancoras libera a restricao,
mas a recuperacao de sanidade continua a cargo do mod principal.

Padroes: checagem a cada 5 segundos, tolerancia inicial de 120 segundos apos
entrada/respawn/mudanca de Sequence e mais 30 segundos de deficit continuo.
Tempos usam a simulacao do servidor; nao ha pressao offline. Criativo e espectador
ficam isentos. `announce_pressure` controla os avisos por estagio.
A previa de avanco usa as fontes e o maximo nativo atuais; ambos podem mudar apos avancar.

### Historico

O botao **History** abre os ultimos 32 eventos, mais recentes primeiro.
Registra aceite/encerramento de vinculos, mudancas de contribuicao por Sequence
dos fieis e alteracoes observadas nos totais das categorias de fontes unicas.

Fontes unicas sao observadas a cada 5 segundos; o novo total precisa persistir
por 30 segundos. O primeiro levantamento apos entrada/respawn/reinicio e silencioso.
O historico nao atribui causas como morte ou exclusao a uma entidade que deixou
de contribuir. Nao recupera eventos anteriores a instalacao desta versao.
Os horarios usam o fuso do cliente. **Back to sources** retorna localmente,
preservando a aba e a pagina; **Refresh** busca dados atuais do servidor.

## Regras das fontes unicas e habilidades emprestadas

- **Red Priest:** Team de jogadores online e vivos, com vinculos nativos reciprocos
  e limite de membros respeitado. NPCs usam a subordinacao nativa e precisam estar
  carregados e vivos. Regressao para Sequence 4 preserva a contribuicao de NPCs
  com vinculo valido; o dono precisa continuar Red Priest.
- **Error:** avatares carregados com dono original confirmado.
- **Fool:** marionetes carregadas com controlador nativo confirmado.
- **Visionary:** personas mentais em hospedeiros online e avatares registrados.
  Avatares registrados indisponiveis aparecem sem contribuir; vinculos mentais
  usam a Sequence da Persona, nao a do hospedeiro.

Fontes com dono/controlador nativo valido podem contribuir mesmo quando o jogador
usa outro caminho. Isso inclui marionetes criadas com Puppeteering registrado,
replicado, roubado, compartilhado ou por artefato, e fontes efetivamente criadas
durante a imersao em um papel do Visionary. O addon nao desbloqueia habilidades:
Avatar Creation e Virtual Persona continuam com suas restricoes nativas.

As abas extras aparecem apenas quando existem fontes registradas/vinculadas;
as categorias nativas continuam disponiveis mesmo vazias. Mais de tres abas usam
duas linhas. Team continua restrito a lideranca nativa elegivel de Red Priest.

Cada corpo contribui uma vez: controle de marionete tem prioridade sobre avatar
de Persona, avatar Error e NPC subordinado. Uma entidade controlada como marionete
nao continua contribuindo para seu antigo dono como avatar ou NPC. Vinculos mentais
distintos no mesmo hospedeiro e vinculos de fe sao contabilizados separadamente.

O encerramento da habilidade emprestada nao remove pontos por si so: o que importa
e se o mod principal preserva ou desfaz o vinculo. Nao ha credito permanente no
saldo normal. Dono morto/espectador/nao-Beyonder, fonte morta, perda de controle
ou entidade descarregada nao concedem contribuicao presumida. Nomes e valores
enviados pelo cliente nunca sao aceitos como prova de vinculo ou recompensa.

## Verificacao

```powershell
.\gradlew.bat check
```

`build` tambem executa `check`. Os testes cobrem validacao de Team, consentimento,
persistencia NBT, historico, codecs e permissoes do painel, ordem de renderizacao,
limites de layout, TOML, perda offline e formulas/tolerancias de sanidade.
Nao inicializam uma sessao multiplayer e nao substituem testes dentro do jogo.
Dados sinteticos existem somente em `src/test`, fora do JAR distribuido.

Fontes unicas, recuperacao e persistencia foram testadas manualmente pelo autor
no jogo. O fluxo completo de oracao/convite/aceite entre dois jogadores e a
integracao multiplayer permanecem pendentes de validacao.

## Documentacao

- [Changelog e notas de publicacao](CHANGELOG.md)
- [Desenvolvimento, integracao nativa e limites conhecidos](docs/DEVELOPMENT.md)
- [Dependencia LOTMCraft para compilacao](libs/README.md)

## Licenca

All Rights Reserved. Consulte [LICENSE](LICENSE). LOTMCraft e suas dependencias
pertencem aos respectivos autores e nao sao redistribuidos neste repositorio.
