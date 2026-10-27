# PvPRounds — correção de respawn e sincronização visual

## O que foi alterado

- O `Main` solicita o respawn do jogador **um tick depois** do evento de morte. Isso evita chamar o respawn antes que o servidor conclua o processamento da morte e também atende jogadores fora do torneio, incluindo clientes 1.8 conectados por ViaVersion/ViaRewind.
- `Automatic` e `Automatic2` não tentam mais respawnar o jogador dentro do evento de morte. Eles aguardam `PlayerRespawnEvent` antes de executar o comando de saída, restaurar o jogador e atualizar a fila do torneio.
- Antes de teleportar os lutadores para a arena, o plugin limpa o alvo de espectador, restaura Survival, remove voo, zera velocidade/queda/fogo e limpa efeitos. O estado é reaplicado depois do teleporte.

## Como instalar

1. Faça backup do plugin atual e das configurações.
2. Abra este projeto no Eclipse usando o JDK adequado ao seu Paper (Java 21 para Paper 1.21.11).
3. Corrija os caminhos das dependências externas no `.classpath` para apontarem aos arquivos do seu ambiente.
4. Faça `Project > Clean` e compile o projeto; depois substitua o JAR instalado pelo JAR gerado e reinicie o servidor.
5. Teste a morte e o início de uma nova luta com um cliente moderno e outro 1.8 via ViaVersion/ViaRewind.

## Limitação desta entrega

Este ZIP contém o código-fonte atualizado, não um JAR compilado. O projeto depende de APIs/arquivos externos (incluindo a API Paper/Spigot e classes do WaveMC/KP-CORE) que não estavam disponíveis neste ambiente, então foi possível validar a sintaxe Java, mas não fazer uma compilação integral nem um teste dentro do servidor.
