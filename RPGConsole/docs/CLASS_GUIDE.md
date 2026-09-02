# RPGConsole — Guia de Classes e Responsabilidades

## Finalidade

Este documento funciona como um mapa rápido das classes do RPGConsole.

Ele não descreve todos os métodos nem substitui a documentação completa da arquitetura. Seu objetivo é responder às perguntas mais úteis durante o desenvolvimento:

- Para que esta classe existe?
- Que tipo de responsabilidade ela possui?
- De onde recebe seus dados?
- Para qual classe ou camada entrega seu resultado?
- Qual é a diferença entre classes de nomes parecidos?

O conteúdo está organizado por pacote, seguindo a estrutura atual do código-fonte.

## Vocabulário arquitetural

Alguns nomes aparecem repetidamente porque representam papéis diferentes dentro do mesmo fluxo.

| Termo | Responsabilidade geral |
|---|---|
| `Loader` | Lê um arquivo externo, converte seus campos, valida referências e produz definições ou entradas. |
| `Definition` | Armazena dados reutilizáveis e imutáveis carregados dos arquivos externos. Não representa uma ocorrência mutável durante o jogo. |
| `Catalog` | Registra e localiza objetos por IDs estáveis ou por outra chave de consulta. Também rejeita cadastros duplicados. |
| `Factory` | Combina definições e cria objetos de execução usados pelo jogo. |
| `Settings` | Agrupa configurações imutáveis necessárias durante uma execução ou sessão. |
| `Session` / `Progress` | Guarda estado mutável que muda enquanto o jogador avança. |
| `Entry` | Representa uma entrada individual de uma fila, tabela ou grupo de dados. |
| `Service` | Executa uma regra específica sem assumir o controle do fluxo completo do jogo. |
| `Coordinator` | Conecta vários objetos e serviços para executar um processo maior. |
| `Menu` | Exibe opções, lê a escolha do jogador e devolve o resultado para quem chamou. |
| `Screen` | Exibe informações; normalmente não decide regras do domínio. |
| `Result` | Transporta o resultado já calculado de uma operação para outra camada. |
| `enum` | Define um conjunto fechado de valores válidos e evita o uso de textos soltos para representar estados. |

## Fluxos fundamentais

### Dados externos

```text
arquivo .txt
    -> Loader
    -> Definition ou Entry
    -> Catalog
    -> sistema consumidor
```

Exemplo:

```text
enemies.txt
    -> EnemyLoader
    -> EnemyDefinition
    -> EnemyCatalog
    -> BattleCoordinator
    -> Enemy
```

### Definição e objeto de execução

```text
EnemyDefinition = modelo reutilizável do inimigo
Enemy           = uma ocorrência mutável dentro de uma batalha

ScenarioDefinition = dados comuns carregados de scenarios.txt
Scenario           = cenário de execução usado por World
```

### Coordenação principal

```text
Main
    -> carrega e conecta dependências
    -> cria World

World
    -> coordena cenário, exploração, viagem e início das batalhas

BattleCoordinator
    -> cria inimigos de execução
    -> inicia Battle
    -> calcula e concede recompensas
```

---

## Pacote `rpg`

| Classe | Categoria | Finalidade e relações principais |
|---|---|---|
| `Main` | Ponto de entrada / composição | Inicia a aplicação. Cria o `Scanner`, carrega os arquivos externos, monta os catálogos, cria os serviços, personagens e a `Party`, conecta todas as dependências e inicia `World`. Deve concentrar montagem, não regras de jogo. |

---

## Pacote `rpg.characters`

| Classe | Categoria | Finalidade e relações principais |
|---|---|---|
| `CharacterStats` | Dados imutáveis | Agrupa os valores-base completos de um personagem: HP, MP, SP, ataque, defesa, magia, resistência, velocidade e sorte. É usado na criação de `PlayableCharacter`, `Enemy` e `EnemyDefinition`, evitando construtores com muitos números desconectados. |
| `GameCharacter` | Classe-base de execução | Representa o estado e o comportamento compartilhados por aliados e inimigos. Guarda recursos mutáveis, como HP, MP e SP atuais, e fornece operações comuns, como atacar, defender, receber dano, gastar recursos, recuperar HP e verificar se está vivo. É herdada por `PlayableCharacter` e `Enemy`. |
| `PlayableCharacter` | Personagem jogável | Estende `GameCharacter` com dados controlados pelo jogador: nível, EXP, habilidades aprendidas e progressão individual. Seus objetos pertencem a `Party` e participam das batalhas como aliados. |
| `Enemy` | Inimigo de execução | Estende `GameCharacter` e representa uma ocorrência mutável de um inimigo dentro de uma batalha. Recebe seus dados de `EnemyDefinition`, possui rank, descrição e tabela de drops, e é colocado em `EnemyBattleSlot`. Vários objetos `Enemy` podem nascer da mesma definição sem compartilhar HP. |
| `EnemyRank` | Enum de domínio | Define os ranks válidos dos inimigos, como comum, veterano, líder, boss, épico e lendário. É usado por loaders, definições, recompensas e validações de dungeon. |
| `Party` | Estado compartilhado do grupo | Armazena de um a quatro `PlayableCharacter`, além do inventário e ouro compartilhados. É consumida por `World`, `Battle`, menus e serviços de recompensa. Recursos e progressão individuais continuam pertencendo a cada personagem. |

---

## Pacote `rpg.enemies`

| Classe | Categoria | Finalidade e relações principais |
|---|---|---|
| `EnemyLoader` | Carregamento de dados | Lê `enemies.txt`, ignora comentários e linhas vazias, converte atributos e rank, interpreta quebras literais `\n` e cria `EnemyDefinition`. Registra cada definição em `EnemyCatalog`. |
| `EnemyDefinition` | Definição imutável | Armazena o ID, nome, atributos, rank e descrição reutilizáveis de um tipo de inimigo. É consultada por encontros, bosses e épicos. O `BattleCoordinator` usa a definição para criar um novo `Enemy` mutável. |
| `EnemyCatalog` | Catálogo | Registra `EnemyDefinition` por ID estável, rejeita IDs duplicados e permite recuperar a definição de um inimigo. É usado por loaders que validam referências, por `EncounterGenerator`, por dungeons e por `BattleCoordinator`. |
| `EnemyDropLoader` | Carregamento de dados | Lê `enemy_drops.txt`, valida os IDs de inimigos e itens usando `EnemyCatalog` e `ItemCatalog`, cria `DropEntry` e registra as entradas em `EnemyDropCatalog`. |
| `EnemyDropCatalog` | Catálogo de relações | Associa cada ID de inimigo à sua lista imutável de `DropEntry`. É consultado pelo `BattleCoordinator` quando cria os inimigos de execução que participarão da batalha. |

### Diferença essencial

```text
EnemyLoader     -> lê o arquivo
EnemyDefinition -> guarda o modelo reutilizável
EnemyCatalog    -> localiza o modelo por ID
Enemy           -> luta e perde HP durante uma batalha
```

---

## Pacote `rpg.items`

| Classe | Categoria | Finalidade e relações principais |
|---|---|---|
| `Item` | Classe-base de domínio | Representa as propriedades comuns dos itens, incluindo ID e nome. Serve de base para categorias concretas, como `HealingItem`, e é o tipo armazenado pelo inventário e pelos drops. |
| `HealingItem` | Item de cura | Especializa `Item` com o valor de recuperação de HP. É usado por `ItemMenu` e pela batalha para aplicar cura a um alvo válido. |
| `ItemCatalog` | Catálogo temporário | Registra e localiza itens por ID. Atualmente também concentra itens definidos diretamente em Java, como Merumã. É usado pelo inventário, eventos de exploração e `EnemyDropLoader`. Futuramente receberá definições externas. |
| `Inventory` | Estado mutável | Guarda as quantidades compartilhadas de itens da `Party`. Permite adicionar, remover e consultar itens. Recebe drops de `BattleRewardService` e é acessado por `ItemMenu`. |
| `DropEntry` | Entrada de drop | Define uma possibilidade individual de recompensa: item, chance e quantidades mínima e máxima. É carregada por `EnemyDropLoader`, armazenada em `EnemyDropCatalog` e sorteada por `BattleRewardService`. |

---

## Pacote `rpg.skills`

| Classe | Categoria | Finalidade e relações principais |
|---|---|---|
| `Skill` | Definição de habilidade | Armazena os dados necessários para usar uma habilidade: nível exigido, custos, dano, cura, tipos e alvo. Atualmente é criada em Java e aprendida por `PlayableCharacter`; `Battle` interpreta seus dados ao executar a ação. |
| `DamageType` | Enum de domínio | Identifica a natureza do dano de uma habilidade, permitindo que `Battle` escolha os atributos e cálculos adequados. |
| `RecoveryType` | Enum de domínio | Informa se a habilidade não recupera recursos, cura HP ou revive um aliado. Trabalha em conjunto com `SkillTargetType`. |
| `SkillTargetType` | Enum de domínio | Define quais alvos uma habilidade aceita, como o próprio usuário, um aliado, todos os aliados, um inimigo ou todos os inimigos. Orienta `Battle`, `AllyTargetMenu` e `EnemyTargetMenu`. |

---

## Pacote `rpg.battle`

| Classe | Categoria | Finalidade e relações principais |
|---|---|---|
| `Battle` | Controlador da batalha | Executa o ciclo completo de combate: rodadas, turnos, ações de aliados, ações inimigas, alvos, habilidades, itens, análise, defesa e fuga. Recebe `Party` e `EnemyBattleGroup`, usa menus e serviços auxiliares e devolve `BattleResult`. |
| `BattleCoordinator` | Coordenador | Faz a ponte entre mundo e batalha. Recebe `EnemyDefinition`, cria inimigos independentes com seus drops, monta `EnemyBattleGroup`, inicia `Battle` e, em caso de vitória, usa `BattleRewardService` e `BattleRewardScreen`. Devolve o resultado para `World`. |
| `BattleResult` | Enum de resultado | Representa as três conclusões possíveis da batalha: vitória, derrota ou fuga. É retornado por `Battle` e interpretado por `BattleCoordinator` e `World`. |
| `BattleSide` | Enum de apoio | Identifica se uma entrada da ordem de turnos pertence ao grupo de aliados ou ao grupo inimigo. Evita depender apenas do tipo concreto do personagem ao processar a fila. |
| `BattleTurnEntry` | Entrada de fila | Representa uma ação disponível para um participante dentro da ordem da rodada. Associa o combatente ao seu lado e aos dados necessários para identificar aquela ação na fila. |
| `BattleTurnOrder` | Estado da fila | Guarda a ordem calculada para a rodada e controla qual entrada está ativa e quais vêm depois. Avança pela fila e ignora participantes que foram derrotados após sua criação. |
| `BattleTurnOrderGenerator` | Gerador de iniciativa | Recebe aliados e inimigos vivos, calcula quantidade de ações, iniciativa e desempates e produz `BattleTurnOrder`. Usa velocidade, sorte e aleatoriedade. |
| `BattleTurnOrderScreen` | Interface de batalha | Exibe a ação atual e as próximas entradas da fila. Recebe `BattleTurnOrder`, mas não calcula iniciativa nem altera as regras da rodada. |
| `EnemyBattleGroup` | Grupo inimigo | Armazena de um a três `EnemyBattleSlot`, centraliza o limite máximo do grupo e oferece consultas sobre inimigos vivos ou derrotados. É criado por `BattleCoordinator` e consumido por `Battle`, menus e recompensas. |
| `EnemyBattleSlot` | Posição de batalha | Liga um `Enemy` de execução ao nome usado durante a batalha. Permite numerar inimigos repetidos, como `Lobermo 1` e `Lobermo 2`, sem alterar o nome original da definição. |
| `BattleEscapeService` | Serviço de regra | Calcula a tentativa de fuga em duas etapas usando sorte, velocidade e médias dos inimigos vivos. Não encerra a batalha diretamente; devolve `EscapeAttemptResult` para `Battle`. |
| `EscapeAttemptResult` | Objeto de resultado | Transporta o sucesso ou fracasso da fuga e os valores de chance relevantes para que `Battle` possa aplicar a consequência e apresentar o resultado. |

### Fluxo resumido da batalha

```text
World
    -> BattleCoordinator
    -> EnemyDefinition vira Enemy
    -> EnemyBattleGroup
    -> Battle
    -> BattleResult
    -> World
```

---

## Pacote `rpg.battle.rewards`

| Classe | Categoria | Finalidade e relações principais |
|---|---|---|
| `BattleRewardService` | Serviço de regra | Calcula EXP, ouro, progressão individual e drops após uma vitória. Recebe os inimigos derrotados, o cenário de combate e a `Party`; atualiza os recursos correspondentes e produz `BattleRewards`. |
| `BattleRewards` | Resultado agregado | Agrupa tudo que foi obtido na batalha: EXP, ouro, drops consolidados e resultados de progressão dos personagens. É produzido por `BattleRewardService` e exibido por `BattleRewardScreen`. |
| `CharacterProgressionResult` | Resultado individual | Registra o que aconteceu com um `PlayableCharacter` ao receber EXP: nível anterior, nível atual, EXP concedida e eventual restauração de recursos. Faz parte de `BattleRewards`. |

---

## Pacote `rpg.ui`

As classes deste pacote cuidam da apresentação e da entrada do jogador. Elas podem validar escolhas, mas não devem decidir regras centrais de batalha, exploração ou progressão.

| Classe | Categoria | Finalidade e relações principais |
|---|---|---|
| `ConsoleUI` | Utilitário de interface | Centraliza operações gerais do console, como limpeza da tela ou pausa para continuação. É reutilizada por telas e fluxos que precisam do mesmo comportamento visual. |
| `TitleScreen` | Tela | Exibe a apresentação inicial do jogo e controla a entrada necessária para começar. É chamada durante a inicialização antes de `World`. |
| `GameOverScreen` | Tela | Exibe a derrota definitiva temporária e informações finais da `Party`. É acionada por `World` quando `BattleResult` indica derrota. |
| `ScenarioActionMenu` | Menu | Recebe a lista de `ScenarioAction` atualmente permitida, exibe as ações numeradas e devolve a escolha. `World` monta a lista dinâmica de acordo com cenário, exploração, boss e rotas. |
| `TravelMenu` | Menu | Consulta as rotas desbloqueadas por meio de `TravelProgress`, exibe nomes e descrições das saídas e devolve o cenário de destino resolvido por `ScenarioCatalog`. Não decide quando viajar é permitido. |
| `ItemMenu` | Menu | Exibe o inventário compartilhado, permite escolher e usar itens e oferece retorno explícito mesmo quando o inventário está vazio. Recebe `Party` e usa menus de alvo quando necessário. |
| `PartyStatusMenu` | Menu de consulta | Exibe uma visão resumida dos membros da `Party` e do ouro compartilhado. Permite selecionar um integrante para abrir `CharacterStatusMenu`. |
| `CharacterStatusMenu` | Tela de consulta | Mostra os detalhes de um `PlayableCharacter`: recursos, atributos, nível, EXP e habilidades. Recebe o personagem selecionado por `PartyStatusMenu`. |
| `AllyTargetMenu` | Menu de alvo | Exibe aliados válidos conforme o contexto, como vivos, feridos ou derrotados, e devolve o `PlayableCharacter` escolhido. É usado por itens e habilidades de cura ou ressurreição. |
| `EnemyTargetMenu` | Menu de alvo | Exibe apenas inimigos vivos de `EnemyBattleGroup` e devolve o `EnemyBattleSlot` escolhido. É usado por ataques, habilidades e análise. |
| `EpicEncounterMenu` | Menu de confirmação | Recebe uma lista de uma a três `EnemyDefinition`, apresenta nomes e descrições do encontro épico e pergunta se o jogador deseja enfrentá-lo. Devolve apenas a decisão; `World` inicia a batalha. |
| `BattleTurnOrderScreen` | Tela | Mostra o participante atual e uma prévia das próximas ações usando `BattleTurnOrder`. Não gera nem altera a fila. |
| `BattleRewardScreen` | Tela | Exibe `BattleRewards`, incluindo EXP, ouro, níveis e itens obtidos. Não calcula nem concede as recompensas. |

---

## Pacote `rpg.world`

| Classe | Categoria | Finalidade e relações principais |
|---|---|---|
| `World` | Coordenador principal do jogo | Mantém o cenário atual, a sessão de exploração e o loop do mundo. Monta ações disponíveis, coordena exploração, eventos, viagens, encontros aleatórios, épicos e bosses, chama menus e `BattleCoordinator` e interpreta `BattleResult`. Conhece vários subsistemas porque é o ponto em que suas consequências se encontram, mas delega entrada, apresentação e cálculos específicos. |

---

## Pacote `rpg.world.encounters`

| Classe | Categoria | Finalidade e relações principais |
|---|---|---|
| `EncounterAreaRestriction` | Enum de domínio | Define se uma entrada pode aparecer somente em área normal, somente em área especial ou em qualquer uma. É carregada de `encounters.txt` e aplicada por `EncounterGenerator`. |
| `EncounterEntry` | Definição de disponibilidade | Representa uma linha de `encounters.txt`: cenário, inimigo, intervalo de posições, peso, limite de cópias e restrição de área. Não representa uma batalha já criada. |
| `EncounterLoader` | Carregamento de dados | Lê `encounters.txt`, converte os campos, valida referências por meio de `EnemyCatalog` e registra `EncounterEntry` em `EncounterCatalog`. |
| `EncounterCatalog` | Catálogo | Agrupa e fornece as entradas cadastradas para cada cenário. É consultado por `EncounterGenerator` durante uma batalha aleatória. |
| `EncounterGenerator` | Gerador aleatório | Filtra entradas por cenário, posição e tipo de área e monta uma lista ponderada de `EnemyDefinition`. Respeita peso, `maxCopies`, mínimo e máximo de inimigos e o limite de `EnemyBattleGroup`. Entrega a lista para `World`, que inicia o encontro. |

### Diferença essencial

```text
EncounterEntry     = quem pode aparecer e em quais condições
EncounterCatalog   = onde essas regras estão registradas
EncounterGenerator = quem sorteia o grupo concreto
```

---

## Pacote `rpg.world.exploration`

| Classe | Categoria | Finalidade e relações principais |
|---|---|---|
| `ExplorationSettings` | Configuração imutável | Agrupa comprimento da exploração, chances de eventos, chance de área especial, limites do grupo inimigo e mensagens de área. É carregada por `CombatScenarioLoader`, armazenada em `CombatScenarioDefinition` e usada pelo cenário de execução e por `ExplorationSession`. |
| `ExplorationSession` | Estado mutável | Guarda a posição atual, conclusão da exploração e ocorrências únicas da sessão, como mercador e épico já oferecidos. Começa na posição 1, impede avanço além do final e separa chegar à última posição de concluir o cenário. |
| `ExplorationEventType` | Enum de domínio | Define os resultados possíveis de uma exploração, como batalha, item, mercador, encontro épico ou nada. É produzido por `ExplorationEventGenerator` e interpretado por `World`. |
| `ExplorationEventGenerator` | Gerador aleatório | Recebe `ExplorationSettings` e o estado de disponibilidade de eventos únicos, realiza o sorteio ponderado e devolve um `ExplorationEventType`. Não executa a consequência do evento; essa responsabilidade pertence a `World`. |

### Diferença essencial

```text
ExplorationSettings = regras fixas do cenário
ExplorationSession  = progresso atual do jogador nessas regras
```

---

## Pacote `rpg.world.scenarios`

### Cenários de execução

| Classe | Categoria | Finalidade e relações principais |
|---|---|---|
| `Scenario` | Classe-base de execução | Define os dados comuns dos cenários usados durante o jogo: ID, nome e descrição. É herdada por `City` e `CombatScenario` e registrada em `ScenarioCatalog`. |
| `City` | Cenário de execução | Representa uma cidade sem sessão de exploração. Pode disponibilizar viagem imediatamente e futuramente concentrará serviços urbanos. É criada por `ScenarioFactory`. |
| `CombatScenario` | Classe-base de execução | Estende `Scenario` com recompensas-base e `ExplorationSettings`. É herdada por `Field` e `Dungeon` e fornece os dados necessários para encontros e recompensas. |
| `Field` | Cenário de execução | Representa um campo explorável com eventos aleatórios e conclusão após resolver sua posição final. É criado por `ScenarioFactory` a partir das definições comuns e de combate. |
| `Dungeon` | Cenário de execução | Representa uma masmorra explorável. Além dos dados de `CombatScenario`, possui `DungeonSettings` com grupos obrigatórios de bosses e épicos opcionais. Sua posição final é resolvida por uma batalha de boss. |
| `DungeonSettings` | Configuração de execução | Guarda as listas de um a três IDs de bosses e de um a três IDs épicos usadas pelo objeto `Dungeon`. Recebe os dados de `DungeonDefinition` durante a criação do cenário. |
| `ScenarioAction` | Enum de interface e fluxo | Define as ações que `World` pode oferecer, como explorar, enfrentar o boss, abrir itens, ver status e viajar. `ScenarioActionMenu` exibe a lista; `World` executa a escolha. |
| `ScenarioType` | Enum de dados | Define os tipos válidos dos cenários externos: cidade, campo e dungeon. É lido por `ScenarioLoader` e usado por validações e `ScenarioFactory`. |

### Definições comuns

| Classe | Categoria | Finalidade e relações principais |
|---|---|---|
| `ScenarioLoader` | Carregamento de dados | Lê `scenarios.txt`, interpreta ID, tipo, nome, descrição e quebras literais `\n`, cria `ScenarioDefinition` e registra no catálogo de definições. |
| `ScenarioDefinition` | Definição imutável | Armazena os dados comuns externos de qualquer cenário: ID, `ScenarioType`, nome e descrição. Não contém progresso nem representa uma visita do jogador. |
| `ScenarioDefinitionCatalog` | Catálogo de definições | Registra `ScenarioDefinition` por ID e permite que loaders especializados e `ScenarioFactory` encontrem os dados comuns. Rejeita IDs duplicados. |

### Definições de cenários de combate

| Classe | Categoria | Finalidade e relações principais |
|---|---|---|
| `CombatScenarioLoader` | Carregamento de dados | Lê `combat_scenarios.txt`, valida se o ID existe e não pertence a uma cidade, monta `ExplorationSettings`, cria `CombatScenarioDefinition` e registra no catálogo correspondente. |
| `CombatScenarioDefinition` | Definição imutável | Armazena os dados externos exclusivos de campos e dungeons: recompensas-base e `ExplorationSettings`. Complementa `ScenarioDefinition` em vez de duplicar os dados comuns. |
| `CombatScenarioDefinitionCatalog` | Catálogo de definições | Registra e localiza `CombatScenarioDefinition` por ID de cenário. É usado por `DungeonLoader` para validar dados e por `ScenarioFactory` para criar `Field` e `Dungeon`. |

### Definições específicas de dungeon

| Classe | Categoria | Finalidade e relações principais |
|---|---|---|
| `DungeonLoader` | Carregamento de dados | Lê `dungeons.txt`, valida que o cenário é uma dungeon e possui dados de combate, resolve os IDs em `EnemyCatalog`, exige ranks adequados e aceita grupos de um a três bosses e épicos. Produz `DungeonDefinition`. |
| `DungeonDefinition` | Definição imutável | Armazena o ID da dungeon e as listas externas de IDs dos bosses obrigatórios e épicos opcionais. Garante quantidade válida, ausência de IDs vazios ou repetidos e separação entre os dois grupos. |
| `DungeonDefinitionCatalog` | Catálogo de definições | Registra e localiza `DungeonDefinition` por ID de cenário. É consultado por `ScenarioFactory` ao criar cada dungeon de execução. |

### Criação e registro dos cenários de execução

| Classe | Categoria | Finalidade e relações principais |
|---|---|---|
| `ScenarioFactory` | Fábrica | Recebe `ScenarioDefinition` e consulta os catálogos especializados. Conforme `ScenarioType`, cria `City`, `Field` ou `Dungeon`, combinando dados comuns, dados de combate e dados específicos da dungeon. |
| `ScenarioCatalog` | Catálogo de execução | Registra os objetos `Scenario` já criados e permite resolvê-los por ID durante o jogo. É usado por `World`, `TravelMenu` e validações de destinos. Não guarda definições cruas dos arquivos. |

### Fluxo completo dos cenários

```text
scenarios.txt
    -> ScenarioLoader
    -> ScenarioDefinitionCatalog

combat_scenarios.txt
    -> CombatScenarioLoader
    -> CombatScenarioDefinitionCatalog

dungeons.txt
    -> DungeonLoader
    -> DungeonDefinitionCatalog

todos os catálogos de definição
    -> ScenarioFactory
    -> City / Field / Dungeon
    -> ScenarioCatalog
    -> World
```

---

## Pacote `rpg.world.travel`

| Classe | Categoria | Finalidade e relações principais |
|---|---|---|
| `TravelRouteLoader` | Carregamento de dados | Lê `travel_routes.txt`, interpreta ID, origem, destino, estado inicial, nome e descrição, converte quebras literais `\n`, valida as referências em `ScenarioDefinitionCatalog` e registra `TravelRouteDefinition`. |
| `TravelRouteDefinition` | Definição imutável | Representa uma rota direcionada carregada do arquivo: ID, cenário de origem, cenário de destino, desbloqueio inicial e textos apresentados ao jogador. Não guarda o desbloqueio mutável da campanha. |
| `TravelCatalog` | Catálogo | Registra as definições de rota, rejeita IDs duplicados e fornece as rotas que partem de um cenário. É consultado por `TravelProgress` e pelo fluxo de viagem. Cada direção precisa de uma definição própria. |
| `TravelProgress` | Estado mutável | Mantém quais rotas estão desbloqueadas na execução atual. Inicializa o estado usando `initiallyUnlocked`, filtra as rotas disponíveis e permite que `World` consulte ou libere saídas conforme o progresso. |

### Diferença essencial

```text
TravelRouteDefinition = dados fixos da rota
TravelCatalog         = todas as rotas cadastradas
TravelProgress        = quais rotas o jogador desbloqueou
TravelMenu            = quais rotas disponíveis o jogador escolhe
World                 = quando a ação de viajar pode aparecer
```

---

## Mapa rápido de dependências

| Se você está procurando... | Comece por... | Caminho principal |
|---|---|---|
| Inicialização e montagem | `Main` | loaders → catálogos → serviços → `World` |
| Fluxo geral do jogo | `World` | cenário → exploração/viagem → batalha |
| Dados de um inimigo | `EnemyDefinition` | `EnemyLoader` → `EnemyCatalog` → `EnemyDefinition` |
| Inimigo perdendo HP | `Enemy` | `EnemyDefinition` → `BattleCoordinator` → `Enemy` |
| Sorteio de inimigos | `EncounterGenerator` | `EncounterCatalog` → filtros/pesos → definições |
| Execução da luta | `Battle` | ordem de turnos → ações → `BattleResult` |
| Preparação da luta | `BattleCoordinator` | definições → grupo → `Battle` → recompensas |
| Recompensas | `BattleRewardService` | inimigos derrotados → `BattleRewards` |
| Posição no mapa | `ExplorationSession` | posição atual → resolução → avanço/conclusão |
| Chances da exploração | `ExplorationSettings` | carregadas de `combat_scenarios.txt` |
| Criação dos mapas | `ScenarioFactory` | catálogos de definição → cenários de execução |
| Cenário atual | `World` | objeto recuperado de `ScenarioCatalog` |
| Rotas existentes | `TravelCatalog` | definições carregadas de `travel_routes.txt` |
| Rotas liberadas | `TravelProgress` | estado mutável sobre `TravelCatalog` |
| Escolha apresentada ao jogador | classes `Menu` | recebem opções válidas e devolvem uma escolha |
| Informação apenas exibida | classes `Screen` | recebem resultados prontos e os apresentam |

## Regra prática para novas classes

Ao criar ou alterar uma classe, faça estas perguntas:

1. Ela está carregando dados, armazenando dados, executando regras, coordenando fluxos ou apenas exibindo informações?
2. O estado dela é uma definição imutável ou muda durante a execução?
3. Ela realmente precisa conhecer todas as classes que importa?
4. Um menu está tomando uma decisão que deveria pertencer a `World`, `Battle` ou a um serviço?
5. Um coordenador está calculando uma regra que poderia ser isolada em um serviço?
6. Um catálogo está guardando objetos de definição ou objetos mutáveis de execução?

Se a resposta sobre a responsabilidade não couber em uma frase clara, a classe pode estar acumulando funções demais.
