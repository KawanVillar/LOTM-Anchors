# Red Priest Team — LOTMCraft 1.13.3

Inspeção do bytecode do JAR local de 114.923.434 bytes, SHA-256
`D067E05369387D707F3BDFB57BC7F5CF142B96146B876297221A8191BC8F9736`.

## Funcionamento nativo

- `TeamUtils.addMember` e os comandos/convites operam com `ServerPlayer`: o cadastro de membros do Team é de jogadores.
- `TeamUtils.isEligibleLeader` exige pathway `red_priest` e Sequence <= 3.
- Limite de membros, sem contar o líder: 4 na Sequence 3, 5 na Sequence 2, 6 nas Sequences 1 e 0.
- Compartilhamento de habilidades: 1, 2 ou 3 slots por membro conforme a Sequence do líder.
- `TeamComponent` armazena lista de UUIDs, UUID do líder e opções de foco/alívio de dano. `TeamUtils.addMember` grava o UUID do próprio líder no componente dele e copia o componente para o membro.
- `CohortUtil` valida convites pendentes com expiração e exige líder online ao aceitar. A aceitação chama diretamente `TeamUtils.addMember`; este verifica capacidade e membro duplicado, mas não repete toda a elegibilidade e o vínculo anterior do convidado. Convite não deve ser tratado como prova suficiente de vínculo atual pelo addon.
- NPCs usam `SubordinateComponent`, com flag de subordinação e UUID do controlador. `SubordinateUtils.turnEntityIntoSubordinate` rejeita jogadores. `CohortUtil.getAllSubordinates` consulta NPCs carregados, inclusive em outras dimensões. Portanto o Cohort dispõe de NPCs, embora eles não sejam membros do Team de jogadores.

## Proteções das âncoras

- O dono precisa estar vivo, não espectador, associado a servidor e permanecer Red Priest. Para jogadores do Team, exige-se também liderança elegível (Sequence <= 3). NPCs com vínculo nativo preservado continuam contribuindo após regressão para Sequence 4.
- Lista do líder e vínculo do membro devem concordar. Não basta possuir uma cópia antiga da lista do Team.
- Sem contagem do próprio líder, jogadores mortos, espectadores ou jogadores fora da lista online.
- Cadastro com UUID inválido/nulo/abreviado, duplicata, inclusão do líder ou tamanho acima do limite nativo não concede âncoras de jogadores. NPCs válidos continuam independentes desse cadastro.
- Cada UUID contribui uma vez. Jogadores são excluídos da busca de NPCs.
- Dados nativos não são reparados ou sobrescritos pelo addon. A proteção controla somente as âncoras, não corrige os comandos ou o compartilhamento de habilidades do mod principal.
- Sem saldo permanente nem cache de contribuição offline. Expulsão/saída/dissolução passam a valer na próxima consulta ao estado. Tela aberta é uma fotografia: reabrir para atualizar.

## Verificação sem multiplayer

`TeamAnchorRules` separa a validação de dados do acesso ao Minecraft.
`TeamAnchorRulesTest` cobre 21 cenários: vínculo válido, próprio líder,
estranho, troca/ausência de líder, morte, espectador, remoção, lista inconsistente,
duplicata, UUID inválido/abreviado/nulo, lista nula, capacidade excedida e limite exato.

Todos os fontes principais e de teste compilaram com Java 21 contra a 1.13.3,
com processamento de anotações desativado. O teste executou sem Minecraft e
passou nos 21 cenários. `check`/`build` passam a incluir `testTeamAnchorRules`.
O novo encadeamento Gradle não foi executado nesta revisão.

Isso não valida pacotes, Mixins em execução, sincronização, comportamento de
convites no jogo ou carregamento de dimensões. Esses continuam exigindo teste de integração.
