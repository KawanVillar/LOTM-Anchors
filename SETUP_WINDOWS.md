# Setup no Windows

1. Instale o JDK 21 e confirme com `java -version`.
2. Clone o repositorio.
3. Baixe o LOTMCraft 1.13.3 completo e coloque em `libs/lotmcraft-1.13.3.jar`.
   Veja [libs/README.md](libs/README.md). A dependencia nao vem no clone.
4. No PowerShell, na pasta do projeto, execute:

```powershell
.\gradlew.bat build
```

O Gradle Wrapper baixa o Gradle; nao precisa de uma instalacao separada.
O primeiro build tambem prepara NeoForge/Minecraft e pode demorar.
Os testes de regressao fazem parte do build; para executar somente as
verificacoes use `.\gradlew.bat check`.

## IntelliJ IDEA

Abra a pasta como projeto Gradle e selecione JDK 21 como Gradle JVM.
Aguarde a sincronizacao antes de executar `build`.
Cores de arquivos no IDE podem indicar arquivos novos ou modificados no Git;
nao indicam, por si so, erros de compilacao.

## Testar no jogo

Copie o JAR de `build/libs/` para `mods`, junto do LOTMCraft 1.13.3 e das
suas dependencias. Remova versoes antigas do addon da instancia de teste.
Cliente e servidor precisam da mesma versao do addon.
Configuracoes ficam em `<world>/serverconfig/lotm_anchors-server.toml`.

## Problemas comuns

- `BeyonderData.class missing`: o arquivo usado e incompleto ou um patch somente
  de recursos. Substitua pela distribuicao completa do mod.
- Java incorreto: confira `JAVA_HOME` e a JVM configurada no IDE.
- Download demorado: acompanhe a tarefa e a conexao; builds seguintes usam o cache.
- Para selecionar uma versao de dependencia no PowerShell, coloque o argumento
  inteiro entre aspas: `.\gradlew.bat "-Plotm_version=1.13.3" build`.
  O arquivo correspondente deve existir em `libs/`; outra versao nao garante compatibilidade.
