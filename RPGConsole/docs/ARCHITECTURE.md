# RPGConsole — Guia de Pacotes e Classes

Este documento apresenta a estrutura atual do projeto e a responsabilidade principal de cada pacote, classe e enum. Ele não substitui a documentação detalhada do código; serve como um mapa rápido para retomar o desenvolvimento.

## Visão geral

O projeto está organizado por domínio:

| Pacote | Responsabilidade |
|---|---|
| `rpg` | Inicialização e composição das partes do jogo. |
| `rpg.characters` | Personagens, atributos e ranks de inimigos. |
| `rpg.battle` | Execução e resultado das batalhas. |
| `rpg.skills` | Definição de habilidades e tipos de dano. |
| `rpg.items` | Itens, inventário, catálogo e drops. |
| `rpg.enemies` | Definições externas de inimigos e seu carregamento. |
| `rpg.world` | Loop principal do mundo, exploração e recompensas. |
| `rpg.world.scenarios` | Hierarquia e tipos de cenários. |
| `rpg.world.encounters` | Regras, carregamento e sorteio de encontros. |
| `rpg.world.exploration` | Configuração e estado da exploração linear. |
| `rpg.ui` | Telas e menus reutilizáveis do console. |

## `rpg`

| Tipo | Nome | Finalidade | Observação importante |
|---|---|---|---|
| Classe | `Main` | Ponto de entrada do programa. Carrega dados, cria o jogador, o cenário inicial e o mundo. | É a raiz de composição: conecta os objetos, mas não deve concentrar regras do jogo. |

## `rpg.characters`

| Tipo | Nome | Finalidade | Observação importante |
|---|---|---|---|
| Classe-base | `GameCharacter` | Reúne o estado e os comportamentos compartilhados por jogadores e inimigos. | Guarda HP, MP e SP atuais, atributos de combate, ataque, defesa, dano e recuperação. |
| Classe | `CharacterStats` | Agrupa os atributos iniciais de um personagem. | É imutável e contém HP, MP, SP, ATK, DEF, MAG, RES, VEL e LCK. Evita construtores gigantes em toda a hierarquia. |
| Classe | `Player` | Representa o personagem controlado pelo jogador. | Acrescenta progressão, EXP, nível, ouro, habilidades e inventário ao comportamento básico. |
| Classe | `Enemy` | Representa uma instância de inimigo usada em batalha. | Possui rank, descrição e tabela de drops. Cada encontro deve criar uma instância nova. |
| Enum | `EnemyRank` | Classifica a dificuldade e o valor de recompensa dos inimigos. | Inclui `COMMON`, `VETERAN`, `LEADER`, `BOSS`, `EPIC` e `LEGENDARY`, cada um com nome exibido e multiplicador. |

## `rpg.battle`

| Tipo | Nome | Finalidade | Observação importante |
|---|---|---|---|
| Classe | `Battle` | Controla o loop de batalha, turnos, ações, habilidades, itens, análise e fuga. | Atualmente trabalha com um jogador e um inimigo. Será refatorada na fase de party e múltiplos inimigos. |
| Enum | `BattleResult` | Representa como uma batalha terminou. | Valores atuais: vitória, derrota ou fuga. Permite ao `World` reagir sem interpretar textos da batalha. |

## `rpg.skills`

| Tipo | Nome | Finalidade | Observação importante |
|---|---|---|---|
| Classe | `Skill` | Armazena os dados de uma habilidade. | Reúne nome, nível de aquisição, tipo de dano, poder, cura, custos de MP/SP e descrição. |
| Enum | `DamageType` | Representa a natureza do dano de uma habilidade. | Mantém o identificador técnico em inglês e um texto apropriado para exibição em português. |

## `rpg.items`

| Tipo | Nome | Finalidade | Observação importante |
|---|---|---|---|
| Classe-base | `Item` | Define os dados e o contrato comum dos itens. | O ID estável é usado na identidade lógica do item e será importante para arquivos externos e saves. |
| Classe | `HealingItem` | Representa um item consumível que recupera HP. | Utiliza `GameCharacter.recoverHp()` para aplicar a cura sem ultrapassar o HP máximo. |
| Classe | `ItemCatalog` | Centraliza os itens conhecidos pelo jogo. | Atualmente utiliza constantes, como `MERUMA`; futuramente poderá ser preenchido a partir de `items.txt`. |
| Classe | `Inventory` | Guarda itens e suas quantidades. | Usa uma associação `Item -> Integer` e é utilizado dentro e fora de batalha. |
| Classe | `DropEntry` | Define uma possibilidade de drop. | Guarda item, chance e quantidade mínima/máxima, além de realizar o sorteio da quantidade. |

## `rpg.enemies`

| Tipo | Nome | Finalidade | Observação importante |
|---|---|---|---|
| Classe | `EnemyDefinition` | Representa o molde imutável usado para criar inimigos. | Guarda ID, nome, `CharacterStats`, rank, descrição e drops. `createEnemy()` produz uma instância com recursos cheios. |
| Classe | `EnemyCatalog` | Armazena as definições de inimigos por ID. | Permite localizar, por exemplo, a definição associada a `lobermo`. |
| Classe | `EnemyLoader` | Converte as linhas de `data/enemies.txt` em definições. | Valida quantidade de campos, números e valores do enum antes de registrar cada inimigo. |

## `rpg.world`

| Tipo | Nome | Finalidade | Observação importante |
|---|---|---|---|
| Classe | `World` | Controla o loop do mundo, menus de cenário, exploração, batalhas, resultados e recompensas. | Mantém o cenário e a sessão de exploração atuais. O drop de Merumã do Lobermo ainda está temporariamente definido aqui. |

O `World` coordena outros componentes, mas as regras específicas ficam em objetos menores:

- `Scenario` informa onde o jogador está.
- `ExplorationSession` informa em qual casa ele está.
- `EncounterGenerator` escolhe o inimigo.
- `Battle` executa a luta.
- `ItemMenu` apresenta e utiliza itens.

## `rpg.world.scenarios`

| Tipo | Nome | Finalidade | Observação importante |
|---|---|---|---|
| Classe abstrata | `Scenario` | Estrutura comum de todos os cenários. | Possui ID estável, nome, descrição e contrato para ações disponíveis. É a base exigida para a herança dos cenários. |
| Classe abstrata | `CombatScenario` | Base para cenários que permitem exploração e combate. | Acrescenta recompensas-base e `ExplorationSettings`. |
| Classe | `Field` | Representa um campo aberto explorável. | Define ações como explorar, inventário, status e viagem. Verdes Ermos é atualmente uma instância desta classe. |
| Enum | `ScenarioAction` | Lista todas as ações que algum cenário pode oferecer. | Cada tipo de cenário escolhe quais valores disponibiliza ao jogador. |

## `rpg.world.encounters`

| Tipo | Nome | Finalidade | Observação importante |
|---|---|---|---|
| Enum | `EncounterAreaRestriction` | Define em qual terreno uma entrada pode aparecer. | Valores: `NORMAL_ONLY`, `SPECIAL_ONLY` e `ANY`. |
| Classe | `EncounterEntry` | Representa a relação entre cenário e inimigo. | Guarda faixa de posições, peso, máximo de cópias e restrição de terreno. É semelhante a um registro de tabela de interseção. |
| Classe | `EncounterCatalog` | Agrupa as entradas de encontro por ID de cenário. | Filtra candidatos usando cenário, posição e indicação de área especial. |
| Classe | `EncounterLoader` | Carrega e valida `data/encounters.txt`. | Verifica também se cada `enemyId` existe no `EnemyCatalog`. |
| Classe | `EncounterGenerator` | Seleciona uma definição de inimigo por sorteio ponderado. | Retorna `Optional.empty()` quando não existe candidato elegível. Atualmente escolhe apenas um inimigo. |

### Dados preparados para uso futuro

`EncounterEntry.maxCopies` e `ExplorationSettings.minEnemies/maxEnemies` já existem, mas ainda não controlam a batalha atual. Eles serão utilizados quando o gerador passar a montar grupos de até três inimigos.

## `rpg.world.exploration`

| Tipo | Nome | Finalidade | Observação importante |
|---|---|---|---|
| Classe | `ExplorationSettings` | Guarda as regras imutáveis de exploração de uma área. | Contém número de casas, chances de encontro e área especial, limites de inimigos e mensagens dos terrenos. |
| Classe | `ExplorationSession` | Guarda o estado mutável de uma exploração em andamento. | Controla a posição atual, conclusão da área e sorteio de terreno especial nas casas intermediárias. |

## `rpg.ui`

| Tipo | Nome | Finalidade | Observação importante |
|---|---|---|---|
| Classe | `TitleScreen` | Apresenta a tela inicial e aguarda Enter. | Exibe o título provisório e os integrantes do grupo. |
| Classe | `GameOverScreen` | Apresenta a tela de derrota. | Agradece ao jogador por testar a versão atual. |
| Classe | `ConsoleUI` | Reúne utilidades gerais da interface de console. | Atualmente oferece a limpeza simulada da tela. |
| Classe | `ItemMenu` | Apresenta o inventário e permite usar itens. | É reutilizado dentro e fora das batalhas. |

## Arquivos externos

| Arquivo | Finalidade | Estado atual |
|---|---|---|
| `data/enemies.txt` | Define inimigos, atributos, rank e descrição. | Implementado e carregado na inicialização. |
| `data/encounters.txt` | Relaciona cenários e inimigos, com posições, pesos, cópias e terreno. | Implementado e carregado na inicialização. |
| `data/enemy_drops.txt` | Definirá drops por inimigo. | Planejado. |
| `data/items.txt` | Definirá itens externos. | Planejado. |

## Fluxos principais

### Inicialização

`Main -> EnemyLoader -> EnemyCatalog -> EncounterLoader -> EncounterCatalog -> World`

### Exploração

`World -> ExplorationSession -> EncounterGenerator -> EnemyDefinition -> Enemy -> Battle`

### Recompensas

`BattleResult -> World -> EXP/ouro -> DropEntry -> Inventory`

## Pontos temporários conhecidos

- O sistema de batalha ainda é um contra um.
- O gerador ainda seleciona somente um inimigo.
- `maxCopies`, `minEnemies` e `maxEnemies` ainda não são consumidos pelo combate.
- O drop de Merumã do Lobermo ainda está em `World`.
- O catálogo de itens ainda usa constantes.
- Somente Verdes Ermos está conectado ao loop atual do mundo.
- A opção de viagem ainda não muda de cenário.

## Regra de manutenção

Atualize este documento quando:

- uma classe ou enum for criado, removido ou renomeado;
- uma responsabilidade importante mudar de pacote;
- um comportamento temporário for substituído pela implementação definitiva;
- um novo arquivo externo for introduzido.
