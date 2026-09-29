# Setup no Windows

## Requisitos
- JDK 21 (NeoForge 1.21.1 usa Java 21).
- IntelliJ IDEA ou VS Code com suporte Gradle.
- LOTMCraft 1.13.0 no diretório `mods` para teste.

## Abrir no IntelliJ
1. File > Open e selecione esta pasta.
2. Escolha importar como projeto Gradle.
3. Aguarde o Gradle baixar NeoForge/Minecraft.
4. Rode `build` para gerar o JAR.
5. Para teste, copie o JAR de `build/libs/` para a pasta `mods` junto do LOTMCraft 1.13.0.

## Observação
O ambiente usado para gerar este ZIP não possui Gradle instalado nem acesso de rede ao Maven, portanto o `build` não foi executado aqui. O código foi montado contra as assinaturas verificadas diretamente no JAR do LOTMCraft fornecido.
