# Contribuindo com o LOTM Anchors

Obrigado por considerar contribuir com o projeto.

> Este repositório está em fase **alpha** e usa licença **proprietária (All Rights Reserved)**. Antes de contribuir, leia o arquivo `LICENSE`.

## Requisitos

- Git
- JDK 21
- Minecraft 1.21.1
- NeoForge 21.1.217+
- LOTMCraft 1.13.0

## Clonando e preparando o ambiente

```bash
git clone https://github.com/KawanVillar/LOTM-Anchors.git
cd LOTM-Anchors
```

No Linux/macOS:

```bash
chmod +x gradlew
```

## Build local obrigatório

Linux/macOS:

```bash
./gradlew clean build
```

Windows:

```bat
gradlew.bat clean build
```

## Execução de ambientes de desenvolvimento

Cliente:

```bash
./gradlew runClient
```

Servidor dedicado:

```bash
./gradlew runServer
```

GameTest Server:

```bash
./gradlew runGameTestServer
```

No Windows, use os mesmos comandos com `gradlew.bat`.

## Fluxo de branch e Pull Request

1. Crie uma branch a partir de `master`:
   ```bash
   git checkout -b tipo/descricao-curta
   ```
2. Faça alterações pequenas e focadas.
3. Garanta que o build está passando antes de abrir PR.
4. Abra Pull Request para `master` com descrição clara.
5. Atualize `CHANGELOG.md` na seção **Unreleased** com o que foi alterado.

## Testes e validações obrigatórias

Antes de abrir PR:

- execute `./gradlew build` (ou `gradlew.bat build` no Windows);
- valide mudanças de configuração e compatibilidade manualmente, quando aplicável;
- anexe logs/saídas relevantes no PR em caso de falha de ambiente.

## Cuidados importantes (Mixins e APIs internas)

Este projeto usa Mixins e integra APIs internas do LOTMCraft. Mudanças nessas áreas exigem atenção extra:

- não altere assinaturas e comportamentos sem validar em runtime;
- verifique compatibilidade com `de.jakob.lotm.util.BeyonderData` e eventos do LOTMCraft;
- trate quebras de compatibilidade como mudança de alto risco.

## Dependência local do LOTMCraft

A compilação depende de `libs/lotmcraft-1.13.0.jar`.

- não remova esse arquivo do fluxo de build;
- não substitua por versão diferente sem justificativa técnica e validação completa;
- respeite a licença/origem desse artefato em qualquer redistribuição.

## Configuração do servidor

O arquivo é gerado em `config/lotm_anchors-server.toml`.

Existe um exemplo em `docs/example-server-config.toml` baseado nos valores padrão definidos em `AnchorConfig.java`.

## Arquivos gerados

Não inclua no commit:

- `build/`
- `.gradle/`
- `run/`
- outros artefatos temporários gerados por IDE, Gradle ou execução local.
