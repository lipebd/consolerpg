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
| `Calculator` | Centraliza uma fórmula ou cálculo de domínio sem controlar o fluxo que o utiliza. |
| `Coordinator` | Conecta vários objetos e serviços para executar um processo maior. |
| `Execution` | Guarda o estado mutável e limitado de uma única execução de uma ação composta. |
| `Context` | Agrupa as entradas compartilhadas por uma execução, como usuário e alvos. |
| `Menu` | Exibe opções, lê a escolha do jogador e devolve o resultado para quem chamou. |
| `Screen` | Exibe informações; normalmente não decide regras do domínio. |
| `Result` | Transporta o resultado já calculado de uma operação para outra camada. |
| `Outcome` | Enumera as conclusões possíveis de uma tentativa sem exigir inferência da interface. |
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

SkillDefinition = conteúdo imutável e reutilizável da habilidade
SkillExecution  = estado temporário de uma única utilização
```

### Coordenação principal

```text
Main
    -> carrega e conecta dependências
    -> cria World

World
    -> coordena cenário, exploração, viagem e início das batalhas
    -> disponibiliza lojas urbanas como locais da cidade;
    -> seleciona uniformemente mercadores elegíveis durante a exploração;
    -> resolve ambos para o mesmo estado persistente em memória e para o mesmo `ShopMenu`.

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
| `GameCurrency` | Utilitário de apresentação | Centraliza o nome visível da moeda e a formatação de valores numéricos. Não armazena ouro, define preços ou executa transações; essas responsabilidades pertencem aos respectivos estados e sistemas. |

---

## Pacote `rpg.characters`

| Classe | Categoria | Finalidade e relações principais |
|---|---|---|
| `CharacterStats` | Dados imutáveis validados | Agrupa HP, MP, SP, ataque, defesa, magia, resistência, velocidade e sorte. Exige HP máximo e todos os atributos regulares positivos; MP e SP máximos podem ser zero. É usado na criação de `PlayableCharacter`, `Enemy` e `EnemyDefinition`. |
| `GameCharacter` | Classe-base de execução | Guarda HP, MP e SP atuais e o estado temporário de combate compartilhado por aliados e inimigos. Fornece operações de dano, defesa, recuperação, ressurreição, gasto de recursos, efeitos temporários e consulta de atributos efetivos. Também expõe uma cópia imutável dos atributos naturais e permite que subclasses substituam esse conjunto de forma protegida. O cálculo de ataques pertence a `DamageCalculator`. |
| `PlayableCharacter` | Personagem jogável | Estende `GameCharacter` com nível, EXP, definição de classe e acesso dinâmico às habilidades liberadas no nível atual. Ao ganhar níveis, usa `CharacterGrowthCalculator` para substituir os atributos naturais uma única vez pelo resultado do nível final. Seus objetos pertencem a `Party` e participam das batalhas como aliados. |
| `Enemy` | Inimigo de execução | Estende `GameCharacter` e representa uma ocorrência mutável de um inimigo dentro de uma batalha. Recebe seus dados de `EnemyDefinition`, possui rank, descrição e tabela de drops, e é colocado em `EnemyBattleSlot`. Vários objetos `Enemy` podem nascer da mesma definição sem compartilhar HP. |
| `EnemyRank` | Enum de domínio | Define os ranks válidos dos inimigos, como comum, veterano, líder, boss, épico e lendário. É usado por loaders, definições, recompensas e validações de dungeon. |
| `Party` | Estado compartilhado do grupo | Armazena de um a quatro `PlayableCharacter`, além do inventário e ouro compartilhados. É consumida por `World`, `Battle`, menus e serviços de recompensa. Recursos e progressão individuais continuam pertencendo a cada personagem. |

---

## Pacote `rpg.classes`

| Classe | Categoria | Finalidade e relações principais |
|---|---|---|
| `PlayableClassDefinition` | Definição imutável | Armazena ID, nome, atributos-base, atributos primário e secundário, categoria de peso e descrição de uma classe jogável. É referenciada por `PlayableCharacter` e pelas definições de habilidade. |
| `CharacterGrowthCalculator` | Calculador de domínio | Calcula todos os atributos naturais de uma classe em um nível-alvo a partir dos atributos-base e das regras globais de crescimento. Centraliza os intervalos primário e secundário, os bônus de recursos por especialização e a proteção contra overflow. |
| `PlayableClassLoader` | Carregamento de dados | Lê as sete classes de `playable_classes.txt`, interpreta descrições com `\n`, cria `CharacterStats`, valida os dados e acrescenta linha e ID ao erro quando um cadastro é inválido. |
| `PlayableClassCatalog` | Catálogo | Registra e localiza `PlayableClassDefinition` por ID estável e permite que `SkillLoader` valide a classe associada a cada habilidade. |
| `PlayableClassWeight` | Enum de domínio | Define as categorias de peso das classes, usadas agora na validação e futuramente na compatibilidade com armaduras. |

---

## Pacote `rpg.enemies`

| Classe | Categoria | Finalidade e relações principais |
|---|---|---|
| `EnemyLoader` | Carregamento de dados | Lê `enemies.txt`, ignora comentários e linhas vazias, converte atributos e rank, interpreta quebras literais `\n` e cria `EnemyDefinition`. Registra cada definição em `EnemyCatalog` e acrescenta linha e ID ao erro quando os dados são inválidos. |
| `EnemyDefinition` | Definição imutável | Armazena o ID, nome, atributos, rank e descrição reutilizáveis de um tipo de inimigo. É consultada por encontros, bosses e épicos. O `BattleCoordinator` usa a definição para criar um novo `Enemy` mutável. |
| `EnemyCatalog` | Catálogo | Registra `EnemyDefinition` por ID estável, rejeita IDs duplicados e permite recuperar a definição de um inimigo. É usado por loaders que validam referências, por `EncounterGenerator`, por dungeons e por `BattleCoordinator`. |
| `EnemyDropLoader` | Carregamento de dados | Lê `enemy_drops.txt`, valida os IDs de inimigos usando `EnemyCatalog`, resolve os itens completos no `ItemCatalog` recebido, cria `DropEntry` e registra as entradas em `EnemyDropCatalog`. |
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
| `ItemCategory` | Enum de domínio | Define as categorias gerais de item reconhecidas pelo jogo. Permite organizar o inventário e indicar quais comportamentos especializados podem ser associados a cada definição. |
| `ItemDefinition` | Definição imutável | Define os dados imutáveis compartilhados de um item: ID, categoria, permissão futura de troca entre jogadores, preços comerciais opcionais, nome e descrição. Valida preços positivos e impede `KEY_ITEM` de definir `shopPurchasePrice`. |
| `ItemDefinitionCatalog` | Catálogo de definições | Registra as definições básicas carregadas por `ItemLoader`, rejeita IDs duplicados e permite que loaders especializados e `ItemFactory` resolvam itens por ID durante a inicialização. |
| `ItemLoader` | Carregamento de dados | Lê `items.txt`, interpreta os campos comuns, converte quebras literais `\n`, valida os dados e registra cada `ItemDefinition` em `ItemDefinitionCatalog`. |
| `RecoveryItemTargetType` | Enum de alvo | Define se um item de recuperação afeta um único aliado (`SINGLE_ALLY`) ou todos os aliados elegíveis (`ALL_ALLIES`). |
| `RecoveryItemDefinition` | Definição especializada imutável | Armazena os componentes de recuperação associados a um item: HP, MP, SP, HP de ressurreição, purificação e tipo de alvo. Exige pelo menos um componente efetivo. |
| `RecoveryItemDefinitionCatalog` | Catálogo especializado | Registra as definições carregadas de `recovery_items.txt`, rejeita especializações duplicadas e permite que `ItemFactory` encontre o comportamento associado a cada definição básica. |
| `RecoveryItemLoader` | Carregamento especializado | Lê `recovery_items.txt`, valida valores, alvos e referências em `ItemDefinitionCatalog`, exige a categoria `CONSUMABLE` e registra `RecoveryItemDefinition`. |
| `ItemFactory` | Fábrica de execução | Combina cada `ItemDefinition` com sua eventual definição especializada. Cria `RecoveryItem` quando existe uma especialização de recuperação e `BasicItem` quando o item ainda não possui comportamento próprio. |
| `Item` | Classe-base de domínio | Classe-base dos itens em runtime. Copia da definição a identidade, a categoria, `playerTradable`, `baseShopSalePrice`, `shopPurchasePrice`, nome e descrição. |
| `BasicItem` | Item básico | Implementação concreta mínima de `Item` para categorias que ainda não possuem comportamento especializado. Pode ser armazenado, exibido e recebido como recompensa, mas não executa uma ação de uso. |
| `RecoveryItem` | Item de recuperação | Especializa `Item` com efeitos combináveis de HP, MP, SP, ressurreição e purificação. Verifica alvos elegíveis, aplica ressurreição antes da recuperação comum de HP e devolve um `RecoveryItemUseResult`. |
| `RecoveryItemUseResult` | Resultado imutável | Informa separadamente os efeitos realmente produzidos por um uso, evitando que menus precisem deduzir o resultado comparando estados anteriores e posteriores do personagem. |
| `ItemCatalog` | Catálogo de execução | Registra os objetos `Item` completos criados por `ItemFactory`, rejeita IDs duplicados e permite resolvê-los por ID. É usado por drops, exploração e demais fluxos que precisam do item de execução, não das definições cruas. |
| `Inventory` | Estado mutável | Guarda os itens pertencentes à `Party` e suas quantidades compartilhadas. Permite adicionar, remover, consultar e listar itens; não impõe peso nem limite de capacidade. |
| `DropEntry` | Entrada de drop | Define uma possibilidade individual de recompensa: item, chance e quantidades mínima e máxima. É carregada por `EnemyDropLoader`, armazenada em `EnemyDropCatalog` e sorteada por `BattleRewardService`. |
---

## Pacote `rpg.skills`

| Classe | Categoria | Finalidade e relações principais |
|---|---|---|
| `SkillDefinition` | Definição imutável | Combina identidade, classe, nível de aquisição, custos, nome, descrição e lista ordenada de componentes de uma habilidade. Pode ser referenciada diretamente pelo personagem porque o estado mutável de cada uso fica em `SkillExecution`. |
| `SkillEffectDefinition` | Definição imutável de componente | Representa um componente de dano, recuperação, estágio, purificação, dissipação, modificador ou estado especial. Valida quais campos são obrigatórios, opcionais ou proibidos para cada tipo. |
| `SkillLoader` | Carregamento de dados | Lê `skills.txt` e `skill_effects.txt`, valida IDs de classe, cabeçalhos, componentes, sequência, fases e referências cruzadas e registra habilidades completas. |
| `SkillCatalog` | Catálogo | Registra `SkillDefinition` por ID estável e oferece as definições carregadas aos personagens e sistemas consumidores. |
| `SkillEffectType` | Enum de domínio | Define os tipos fechados de componente reconhecidos pelo executor. |
| `SkillTargetType` | Enum de alvo | Define alvos como usuário, aliado, inimigo, grupos completos ou alvos aleatórios. Orienta a seleção feita uma única vez para a execução. |
| `SkillExecutionPhase` | Enum de fase | Ordena componentes em `PRE_ACTION`, `PRIMARY` e `POST_ACTION`. |
| `SkillConditionType` | Enum de condição | Define condições opcionais avaliadas antes da aplicabilidade e dos testes probabilísticos. |
| `DamageType` | Enum de domínio | Distingue dano físico e mágico para que `DamageCalculator` use o atributo ofensivo, a defesa e o fator adequados. |

---

## Pacote `rpg.effects`

| Classe | Categoria | Finalidade e relações principais |
|---|---|---|
| `CombatEffectState` | Estado mutável agregado | Guarda os estados positivos e negativos de cada atributo e o estado de Distorção Ancestral de um personagem. Centraliza aplicação, redução, purificação, dissipação e limpeza. |
| `AttributeStageState` | Estado mutável | Mantém separadamente estágios positivos e negativos e suas durações para um atributo. |
| `AttributeStageRules` | Regras imutáveis | Limita estágios, aplica os multiplicadores congelados e faz o arredondamento half-up do atributo efetivo. |
| `SpecialBattleStateType` | Enum de domínio | Define estados especiais que não pertencem automaticamente às regras de purificação e dissipação. |

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
| `DamageCalculator` | Calculador de domínio | Centraliza a fórmula de dano físico e mágico usada por habilidades e ataques básicos. Mantém `KF` e `KM` como constantes internas de balanceamento. |
| `RecoveryCalculator` | Calculador de domínio | Calcula recuperação de HP e ressurreição a partir do poder e do ATK ou MAG efetivo indicado pelo componente. MP e SP permanecem fixos. |
| `EffectResistanceCalculator` | Calculador de domínio | Calcula e sorteia a resistência baseada na LCK efetiva do aplicador e na RES efetiva do alvo. Mantém `KR` e o teto de chance em um único lugar. |
| `SkillExecutor` | Executor de domínio | Orquestra condições, aplicabilidade, um trigger por componente, resistência individual e aplicação. Reutiliza calculadores e operações de `GameCharacter` sem repetir suas fórmulas. |
| `SkillExecution` | Estado de uma utilização | Preserva os resultados, modificadores acumulados e componentes `POST_ACTION` de uma única utilização de habilidade. |
| `SkillExecutionContext` | Contexto imutável | Agrupa o usuário e os alvos selecionados uma única vez para todos os componentes da habilidade. |
| `SkillEffectResult` | Resultado imutável | Associa componente, alvo, desfecho e valor produzido para que `Battle` apenas apresente o resultado. |
| `SkillEffectOutcome` | Enum de resultado | Distingue `APPLIED`, `RESISTED` e `NOT_TRIGGERED`. |

### Fluxo resumido da batalha

```text
World
    -> BattleCoordinator
    -> EnemyDefinition vira Enemy
    -> EnemyBattleGroup
    -> Battle
    -> SkillExecutor, quando a ação é uma habilidade
    -> BattleResult
    -> World
```

---

## Pacote `rpg.battle.rewards`

| Classe | Categoria | Finalidade e relações principais |
|---|---|---|
| `RewardCalculator` | Calculador | Soma percentuais globais e individuais de forma aditiva, aplica-os à recompensa-base, arredonda uma única vez em half-up e rejeita overflow. Não concede nem armazena recompensas. |
| `PartyRewardModifierState` | Estado mutável | Guarda no máximo um modificador temporário de EXP e um de GOLD para a `Party`, com suas durações em vitórias recompensadas. Os dois tipos podem coexistir, mas efeitos do mesmo tipo não acumulam. |
| `BattleRewardModifiers` | Snapshot imutável | Registra os modificadores usados em uma vitória, incluindo percentuais globais temporários, modificadores individuais e durações restantes após a recompensa. Permite apresentar o resultado sem consultar estado mutável já avançado. |
| `BattleRewardService` | Serviço de regra | Agrega recompensas-base, cria o snapshot de modificadores, usa `RewardCalculator`, concede EXP individual e GOLD compartilhado, reduz a duração dos efeitos após a vitória, calcula progressão e drops e produz `BattleRewards`. |
| `BattleRewards` | Resultado agregado | Agrupa a EXP global apresentada, o GOLD, o snapshot de modificadores, os drops consolidados e os resultados individuais de progressão. É produzido por `BattleRewardService` e exibido por `BattleRewardScreen`. |
| `CharacterProgressionResult` | Resultado individual | Registra EXP concedida, níveis anterior e atual, cópias dos atributos naturais antes e depois do crescimento, habilidades recém-liberadas e eventual restauração de recursos. Faz parte de `BattleRewards`. |

---

## Pacote `rpg.ui`

As classes deste pacote cuidam da apresentação e da entrada do jogador. Elas podem validar escolhas, mas não devem decidir regras centrais de batalha, exploração ou progressão.

| Classe | Categoria | Finalidade e relações principais |
|---|---|---|
| `ConsoleUI` | Utilitário de interface | Centraliza operações gerais do console, como limpeza da tela ou pausa para continuação. É reutilizada por telas e fluxos que precisam do mesmo comportamento visual. |
| `TitleScreen` | Tela | Exibe a apresentação inicial do jogo e controla a entrada necessária para começar. É chamada durante a inicialização antes de `World`. |
| `GameOverScreen` | Tela | Exibe a derrota definitiva temporária e informações finais da `Party`. É acionada por `World` quando `BattleResult` indica derrota. |
| `CombatScenarioMenu` | Menu de cenário de combate | Recebe as ações de `CombatScenarioAction` permitidas no campo ou dungeon atual, mantém numeração contínua, separa grupos visualmente e devolve a escolha. Não é usado por cidades. |
| `CityMenu` | Menu de cidade | Recebe o nome da cidade e as ações de `CityAction` atualmente disponíveis. Omite ações sem destino funcional, preserva numeração contínua e separa navegação dos recursos do grupo. |
| `CityLocationMenu` | Menu de navegação urbana | Exibe apenas os nomes dos locais funcionais da cidade e devolve a posição selecionada. Não conhece nem executa as regras internas dos serviços. |
| `InnMenu` | Menu de serviço urbano | Exibe os dados de `InnDefinition`, controla confirmação e pagamento da estadia e restaura a `Party`. Mantém o jogador no INN até um retorno explícito e apresenta o resultado das ações dentro da tela atual. |
| `ScenarioBranchMenu` | Menu | Recebe as bifurcações disponíveis na posição atual, exibe seus nomes e descrições e devolve a `ScenarioBranchDefinition` escolhida. Não altera o cenário nem o estado da exploração. |
| `RouteMenu` | Menu | Recebe as rotas adjacentes atualmente disponíveis, exibe nomes e descrições das saídas e devolve a `TravelRouteDefinition` escolhida. Não resolve o destino nem aplica a posição de chegada; essas consequências pertencem a `World`. |
| `CityTransportMenu` | Menu de transporte pago | Recebe os destinos urbanos descobertos e configurados, exibe preços e saldo por meio de `GameCurrency`, valida seleção e saldo, solicita confirmação, realiza o pagamento e devolve a `CityTransportDefinition` escolhida. Não altera o cenário. |
| `InventoryMenu` | Menu geral de inventário | Exibe a moeda compartilhada por meio de `GameCurrency` e todas as categorias do inventário em ordem determinística. Categorias vazias permanecem selecionáveis e exibem sua mensagem somente depois da seleção. Categorias preenchidas apresentam uma lista paginada e ordenada de itens, detalhes e ações contextuais, como o uso de `RecoveryItem`. |
| `BattleItemMenu` | Menu de itens de batalha | Filtra diretamente os consumíveis utilizáveis em batalha, apresenta uma lista paginada e abre seus detalhes. Devolve sucesso apenas quando o uso produz algum efeito, permitindo que `Battle` decida se a ação do personagem foi consumida. |
| `RecoveryItemUseMenu` | Fluxo de uso | Centraliza a seleção de alvos, a aplicação de efeitos individuais ou coletivos, a remoção do item somente após um resultado efetivo e a apresentação dos valores recuperados. É compartilhado por `InventoryMenu` e `BattleItemMenu`. |
| `ItemMenuFormatter` | Formatador de interface | Centraliza a largura das colunas, o truncamento seguro de nomes longos e o resumo dos componentes de recuperação usados pelas telas de inventário. |
| `PartyStatusMenu` | Menu de consulta | Exibe uma visão resumida dos membros da `Party`, a moeda compartilhada e os modificadores temporários de recompensa ativos. Permite selecionar um integrante para abrir `CharacterStatusMenu`. |
| `CharacterStatusMenu` | Tela de consulta | Mostra classe, recursos, atributos, nível e EXP de um `PlayableCharacter`. Apresenta suas habilidades disponíveis em uma lista compacta e abre a ficha técnica e narrativa da habilidade selecionada por meio de `SkillDetailFormatter`. |
| `AllyTargetMenu` | Menu de alvo | Exibe aliados vivos ou derrotados conforme o contexto e devolve o `PlayableCharacter` escolhido. Um aliado vivo pode ser selecionado mesmo com HP cheio porque componentes compostos ainda podem purificá-lo ou fortalecê-lo. |
| `EnemyTargetMenu` | Menu de alvo | Exibe apenas inimigos vivos de `EnemyBattleGroup` e devolve o `EnemyBattleSlot` escolhido. É usado por ataques, habilidades e análise. |
| `CombatEffectFormatter` | Formatador de interface | Produz a apresentação compartilhada dos estágios ativos e da Distorção Ancestral sem mover regras de duração ou atributo para a interface. |
| `SkillDetailFormatter` | Formatador de interface | Produz a ficha compartilhada de uma habilidade com nível, tipo principal, poder, alvo, custo e descrição narrativa. Deriva os campos técnicos do primeiro componente efetivo de `PRIMARY` e é reutilizado pelo status e pela confirmação em batalha. |
| `RewardModifierFormatter` | Formatador de interface | Produz a notação compacta compartilhada de `M.EXP` e `M.GOLD`, incluindo multiplicador e vitórias restantes, sem calcular ou alterar recompensas. |
| `EpicEncounterMenu` | Menu de confirmação | Recebe uma lista de uma a três `EnemyDefinition`, apresenta nomes e descrições do encontro épico e pergunta se o jogador deseja enfrentá-lo. Devolve apenas a decisão; `World` inicia a batalha. |
| `BattleTurnOrderScreen` | Tela | Mostra o participante atual e uma prévia das próximas ações usando `BattleTurnOrder`. Não gera nem altera a fila. |
| `BattleRewardScreen` | Tela | Exibe um resumo compacto de `BattleRewards`: modificadores globais usados, EXP global, GOLD, itens consolidados e progressão de nível. Não lista cada EXP individual, nem calcula ou concede recompensas. |
| `ShopMenu` | Menu comercial | Interface compartilhada por lojas urbanas e mercadores de exploração. Exibe ofertas, categorias vendáveis, confirmações, paginação, preços, estoque e feedback das transações. |

---

## Pacote `rpg.world`

| Classe | Categoria | Finalidade e relações principais |
|---|---|---|
| `World` | Coordenador principal do jogo | Mantém o cenário atual, a sessão de exploração e o loop do mundo. Monta ações disponíveis, coordena exploração, eventos, rotas adjacentes, transporte pago, encontros aleatórios, épicos e bosses, chama menus e `BattleCoordinator` e interpreta `BattleResult`. Conhece vários subsistemas porque é o ponto em que suas consequências se encontram, mas delega entrada, apresentação e cálculos específicos. |
| `WorldProgress` | Estado mutável | Registra os IDs dos cenários descobertos na execução atual, valida referências por meio de `ScenarioCatalog` e expõe uma cópia não modificável do progresso. Permanece separado de `TravelProgress`, pois descoberta e desbloqueio de rotas são conceitos independentes. |

---

## Pacote `rpg.world.encounters`

| Classe | Categoria | Finalidade e relações principais |
|---|---|---|
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
| `ExplorationEventType` | Enum de domínio | Define os resultados possíveis de uma exploração, como batalha, recompensa, mercador, encontro épico ou nada. É produzido por `ExplorationEventGenerator` e interpretado por `World`. |
| `ExplorationEventGenerator` | Gerador aleatório | Recebe `ExplorationSettings` e o estado de disponibilidade de eventos únicos, realiza o sorteio ponderado e devolve um `ExplorationEventType`. Não executa a consequência do evento; essa responsabilidade pertence a `World`. |
| `ExplorationAreaRestriction` | Enum de domínio compartilhado | Define se uma entrada é elegível somente em área normal, somente em área especial ou em ambas. É reutilizada por encontros e recompensas de exploração. |

### Diferença essencial

```text
ExplorationSettings = regras fixas do cenário
ExplorationSession  = progresso atual do jogador nessas regras
```

---

## Pacote `rpg.world.rewards`

| Classe | Categoria | Finalidade e relações principais |
|---|---|---|
| `ExplorationRewardType` | Enum de domínio | Define os tipos de recompensa de exploração atualmente suportados: item e ouro. |
| `ExplorationRewardEntry` | Definição de pool | Representa uma linha de `exploration_rewards.txt`, contendo tipo, ID opcional, intervalo de quantidade, peso relativo e restrição de área. |
| `ExplorationRewardLoader` | Carregamento de dados | Lê `exploration_rewards.txt`, valida cenários, itens, campos, duplicatas e pools obrigatórios e registra as entradas no catálogo. |
| `ExplorationRewardCatalog` | Catálogo | Agrupa entradas por cenário e fornece aquelas permitidas para a área normal ou especial atual. |
| `ExplorationRewardGenerator` | Gerador aleatório | Seleciona exatamente uma entrada elegível por peso relativo e sorteia sua quantidade dentro do intervalo configurado. |
| `ExplorationReward` | Resultado de execução | Representa a recompensa concreta já selecionada, pronta para ser aplicada por `World` ao inventário ou ao ouro do grupo. |

### Diferença essencial

```text
ExplorationRewardEntry     = uma possibilidade configurada no pool
ExplorationRewardCatalog   = onde as possibilidades estão registradas
ExplorationRewardGenerator = quem seleciona uma entrada e sua quantidade
ExplorationReward          = o resultado concreto do sorteio
```

## Pacote `rpg.world.scenarios`

### Cenários de execução

| Classe | Categoria | Finalidade e relações principais |
|---|---|---|
| `Scenario` | Classe-base de execução | Define os dados comuns dos cenários usados durante o jogo: ID, nome e descrição. É herdada por `City` e `CombatScenario` e registrada em `ScenarioCatalog`. |
| `CombatScenarioAction` | Enum de interface e fluxo | Define ações exclusivas de campos e dungeons, como explorar, seguir uma bifurcação, enfrentar o boss, viajar, abrir o inventário e ver status. |
| `CityAction` | Enum de interface e fluxo | Define as ações urbanas atualmente suportadas: visitar um local, sair por uma rota adjacente, abrir o inventário e ver status. |
| `CombatScenario` | Classe-base de execução | Estende `Scenario` com recompensas-base e `ExplorationSettings`. É herdada por `Field` e `Dungeon` e fornece os dados necessários para encontros e recompensas. |
| `Field` | Cenário de execução | Representa um campo explorável com eventos aleatórios e conclusão após resolver sua posição final. É criado por `ScenarioFactory` a partir das definições comuns e de combate. |
| `Dungeon` | Cenário de execução | Representa uma masmorra explorável. Além dos dados de `CombatScenario`, possui `DungeonSettings` com grupos obrigatórios de bosses e épicos opcionais. Sua posição final é resolvida por uma batalha de boss. |
| `DungeonSettings` | Configuração de execução | Guarda as listas de um a três IDs de bosses e de um a três IDs épicos usadas pelo objeto `Dungeon`. Recebe os dados de `DungeonDefinition` durante a criação do cenário. |
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
| `ScenarioCatalog` | Catálogo de execução | Registra os objetos `Scenario` já criados e permite resolvê-los por ID durante o jogo. É usado por `World`, `RouteMenu`, `CityTransportMenu`, `WorldProgress` e validações de destinos. Não guarda definições cruas dos arquivos. |

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

## Pacote `rpg.world.branches`

| Classe | Categoria | Finalidade e relações principais |
|---|---|---|
| `ScenarioBranchLoader` | Carregamento de dados | Lê `scenario_branches.txt`, interpreta ID, cenário de origem, posição, destino, nome e descrição, converte quebras literais `\n` e valida as referências. Exige uma origem explorável, rejeita posições fora da exploração e impede bifurcações na posição final de masmorras. |
| `ScenarioBranchDefinition` | Definição imutável | Representa uma saída disponível numa posição específica: ID estável, cenário de origem, posição, cenário de destino, nome apresentado ao jogador e texto descritivo. O destino pode ser cidade, campo ou masmorra. |
| `ScenarioBranchCatalog` | Catálogo | Registra bifurcações por ID e por combinação de cenário de origem e posição. Permite múltiplos destinos na mesma posição e rejeita IDs ou destinos duplicados no mesmo ponto. |

### Fluxo da bifurcação

```text
scenario_branches.txt
    -> ScenarioBranchLoader
    -> ScenarioBranchDefinition
    -> ScenarioBranchCatalog
    -> World
    -> ScenarioBranchMenu
    -> World altera o cenário
```
`ScenarioBranchMenu` apenas devolve a definição escolhida. `World` resolve o cenário de destino, encerra a sessão anterior e cria uma nova sessão quando necessário.

---

## Pacote `rpg.world.inns`

Este pacote representa os dados fixos dos INNs urbanos. Ele não controla entrada do jogador nem altera diretamente a `Party`; essas responsabilidades pertencem a `World` e `InnMenu`.

| Classe | Categoria | Finalidade e relações principais |
|---|---|---|
| `InnLoader` | Carregamento de dados | Lê `inns.txt`, interpreta quebras literais `\n`, valida os cinco campos, resolve o cenário associado e exige o tipo `CITY`. Produz definições registradas em `InnCatalog`. |
| `InnDefinition` | Definição imutável | Armazena ID estável, ID do cenário urbano, nome, descrição e preço fixo da estadia para todo o grupo. Rejeita textos vazios e preços menores ou iguais a zero. |
| `InnCatalog` | Catálogo de definições | Registra INNs por ID estável e por ID da cidade. Rejeita IDs duplicados e permite no máximo um INN por cenário urbano. |

### Fluxo do INN

```text
inns.txt
    -> InnLoader
    -> InnDefinition
    -> InnCatalog
    -> World
    -> CityLocationMenu
    -> InnMenu
    -> Party
```

`InnCatalog` informa a `World` se a cidade atual possui um INN funcional. `CityLocationMenu` cuida apenas da navegação pelo nome do local. Depois da seleção, `InnMenu` apresenta seus detalhes e executa confirmação, pagamento, ressurreição e restauração.

---

## Pacote rpg.world.shops

O pacote `rpg.world.shops` separa a configuração externa das lojas, o estado mutável gerado para a execução atual e as regras atômicas de compra e venda.

| Classe | Tipo | Responsabilidade |
|---|---|---|
| `ShopAccessType` | Enum | Diferencia lojas fixas de cidade (`CITY`) e mercadores encontrados durante exploração (`EXPLORATION`). |
| `ShopDefinition` | Definição imutável | Armazena ID, cenário, tipo de acesso, quantidade de slots, nome e descrição de uma loja. |
| `ShopCatalog` | Catálogo | Registra lojas por ID e permite consultar todas as lojas associadas a um cenário. |
| `ShopLoader` | Loader | Carrega `shops.txt`, valida referências e tipos de cenário e exige mercadores elegíveis quando `merchantChance` for positivo. |
| `ShopStockEntry` | Definição imutável | Representa uma possível oferta com item, percentual de preço, estoque inicial, peso e preço final calculado. |
| `ShopStockCatalog` | Catálogo | Registra possíveis ofertas por ID e por loja, rejeitando duplicatas comerciais. |
| `ShopStockLoader` | Loader | Carrega `shop_stock.txt`, valida referências, preços, estoque, pesos, ofertas obrigatórias e a regra global contra arbitragem. |
| `ShopOfferState` | Estado mutável | Mantém uma oferta selecionada e sua quantidade restante durante a execução. |
| `ShopState` | Estado mutável agregado | Associa uma `ShopDefinition` à lista fixa de ofertas selecionadas para aquela loja. |
| `ShopStateCatalog` | Catálogo de runtime | Preserva o mesmo `ShopState` de cada loja durante toda a execução do programa. |
| `ShopStateGenerator` | Serviço de geração | Seleciona ofertas por peso relativo, sem reposição, e cria o estado inicial de todas as lojas. |
| `ShopPurchaseResult` | Enum de resultado | Identifica sucesso ou motivo de rejeição de uma tentativa de compra. |
| `ShopSaleResult` | Enum de resultado | Identifica sucesso ou motivo de rejeição de uma tentativa de venda. |
| `ShopTransactionService` | Serviço de domínio | Valida e aplica compras e vendas de forma atômica, protegendo estoque, inventário, GOLD e cálculos contra estados inválidos e overflow. |

### Fluxo das lojas

```text
shops.txt
    -> ShopLoader
    -> ShopCatalog

shop_stock.txt
    -> ShopStockLoader
    -> ShopStockCatalog

ShopCatalog + ShopStockCatalog
    -> ShopStateGenerator
    -> ShopStateCatalog
    -> World
    -> ShopMenu
    -> ShopTransactionService
    -> Party, Inventory e ShopOfferState
```

As ofertas ativas são geradas apenas uma vez por execução. Uma oferta esgotada não é substituída por outra entrada de `shop_stock.txt`, e o mesmo estado é reutilizado em acessos posteriores à loja ou ao mercador. Reabastecimento e persistência ficam adiados até a implementação de save e load.

## Pacote `rpg.world.travel`

| Classe | Categoria | Finalidade e relações principais |
|---|---|---|
| `TravelRouteLoader` | Carregamento de dados | Lê `travel_routes.txt`, interpreta ID, origem, destino, estado inicial, nome, descrição e posição de chegada, converte quebras literais `\n` e valida as referências. Exige posição vazia para cidades e uma posição válida para campos e masmorras. |
| `TravelRouteDefinition` | Definição imutável | Representa uma rota direcionada carregada do arquivo: ID, origem, destino, desbloqueio inicial, textos e posição opcional de chegada na exploração. Não guarda o desbloqueio mutável da campanha. |
| `TravelCatalog` | Catálogo | Registra as definições de rota, rejeita IDs duplicados e fornece as rotas que partem de um cenário. É consultado por `TravelProgress` e pelo fluxo de viagem. Cada direção precisa de uma definição própria. |
| `TravelProgress` | Estado mutável | Mantém quais rotas estão desbloqueadas na execução atual. Inicializa o estado usando `initiallyUnlocked`, filtra as rotas disponíveis e permite que `World` consulte ou libere saídas conforme o progresso. |

### Diferença essencial

```text
TravelRouteDefinition = dados fixos da rota adjacente
TravelCatalog         = todas as rotas adjacentes cadastradas
TravelProgress        = quais rotas adjacentes o jogador desbloqueou
RouteMenu             = qual definição de rota disponível o jogador escolhe
World                 = resolve o destino e aplica sua posição de chegada
```

---

## Pacote `rpg.world.transport`

| Classe | Categoria | Finalidade e relações principais |
|---|---|---|
| `CityTransportLoader` | Carregamento de dados | Lê `city_transport.txt`, converte quebras literais `\n`, valida preços e exige que cada destino corresponda a um cenário `CITY`. Rejeita destinos urbanos duplicados e produz `CityTransportCatalog`. |
| `CityTransportDefinition` | Definição imutável | Armazena o ID da cidade de destino, o preço fixo e a descrição exibida após a viagem. Não guarda descoberta, saldo ou qualquer estado mutável. |
| `CityTransportCatalog` | Catálogo | Registra transportes pagos por cidade de destino, rejeita duplicatas e fornece as definições usadas por `World` para filtrar cidades descobertas. |

### Diferença para rotas adjacentes

```text
TravelRouteDefinition   = deslocamento adjacente direcionado
TravelProgress          = desbloqueio mutável das rotas adjacentes
CityTransportDefinition = preço e narrativa do transporte pago para uma cidade
WorldProgress           = cenários que o jogador já descobriu
CityTransportMenu       = seleção, confirmação e pagamento
World                   = mudança efetiva para a cidade escolhida
```

## Mapa rápido de dependências

| Se você está procurando... | Comece por... | Caminho principal |
|---|---|---|
| Inicialização e montagem | `Main` | loaders → catálogos → serviços → `World` |
| Fluxo geral do jogo | `World` | cenário → exploração/rotas/transporte → batalha |
| Dados de um inimigo | `EnemyDefinition` | `EnemyLoader` → `EnemyCatalog` → `EnemyDefinition` |
| Dados de uma classe jogável | `PlayableClassDefinition` | `playable_classes.txt` → `PlayableClassLoader` → `PlayableClassCatalog` |
| Crescimento natural de um personagem | `CharacterGrowthCalculator` | definição de classe + nível → `CharacterStats` → `PlayableCharacter` |
| Inimigo perdendo HP | `Enemy` | `EnemyDefinition` → `BattleCoordinator` → `Enemy` |
| Sorteio de inimigos | `EncounterGenerator` | `EncounterCatalog` → filtros/pesos → definições |
| Execução da luta | `Battle` | ordem de turnos → ações → `BattleResult` |
| Definição de uma habilidade | `SkillDefinition` | `skills.txt` + `skill_effects.txt` → `SkillLoader` → `SkillCatalog` |
| Uma utilização de habilidade | `SkillExecution` | `Battle` → contexto e alvos → `SkillExecutor` → resultados → `Battle` |
| Fórmulas de combate | classes `Calculator` | executor ou batalha → calculador especializado → operação em `GameCharacter` |
| Efeitos temporários | `CombatEffectState` | componente aplicado → estados por atributo → atributo efetivo/formatter |
| Preparação da luta | `BattleCoordinator` | definições → grupo → `Battle` → recompensas |
| Recompensas de batalha | `BattleRewardService` | inimigos derrotados + estado de modificadores → `RewardCalculator` → `BattleRewards` |
| Modificadores temporários de recompensa | `PartyRewardModifierState` | ativação → vitórias recompensadas restantes → snapshot imutável |
| Formatação de modificadores | `RewardModifierFormatter` | percentuais + durações → resumo `M.EXP`/`M.GOLD` |
| Recompensas de exploração | `ExplorationRewardGenerator` | `ExplorationRewardCatalog` → filtros/pesos → `ExplorationReward` → `Party` |
| Posição no mapa | `ExplorationSession` | posição atual → resolução → avanço/conclusão |
| Chances da exploração | `ExplorationSettings` | carregadas de `combat_scenarios.txt` |
| Criação dos mapas | `ScenarioFactory` | catálogos de definição → cenários de execução |
| Cenário atual | `World` | objeto recuperado de `ScenarioCatalog` |
| Rotas existentes | `TravelCatalog` | definições carregadas de `travel_routes.txt` |
| Rotas liberadas | `TravelProgress` | estado mutável sobre `TravelCatalog` |
| Cenários descobertos | `WorldProgress` | entradas por rota, branch ou transporte → IDs descobertos |
| Transporte pago | `CityTransportCatalog` | destinos configurados + `WorldProgress` → `CityTransportMenu` → `World` |
| Apresentação monetária | `GameCurrency` | nome da moeda + formatação de valores |
| Escolha apresentada ao jogador | classes `Menu` | recebem opções válidas e devolvem uma escolha |
| Informação apenas exibida | classes `Screen` | recebem resultados prontos e os apresentam |
| Definições de lojas | `ShopDefinition` | `shops.txt` → `ShopLoader` → `ShopCatalog` |
| Possíveis ofertas | `ShopStockEntry` | `shop_stock.txt` → `ShopStockLoader` → `ShopStockCatalog` |
| Estado comercial atual | `ShopStateCatalog` | `ShopStateCatalog` → `ShopState` → `ShopOfferState` |
| Interface comercial | `ShopMenu` | `World` → `ShopMenu` → `ShopTransactionService` |
| Transações comerciais | `ShopTransactionService` | validação → `Party`, `Inventory` e `ShopOfferState` |

## Regra prática para novas classes

Ao criar ou alterar uma classe, faça estas perguntas:

1. Ela está carregando dados, armazenando dados, executando regras, coordenando fluxos ou apenas exibindo informações?
2. O estado dela é uma definição imutável ou muda durante a execução?
3. Ela realmente precisa conhecer todas as classes que importa?
4. Um menu está tomando uma decisão que deveria pertencer a `World`, `Battle` ou a um serviço?
5. Um coordenador está calculando uma regra que poderia ser isolada em um serviço?
6. Um catálogo está guardando objetos de definição ou objetos mutáveis de execução?

Se a resposta sobre a responsabilidade não couber em uma frase clara, a classe pode estar acumulando funções demais.
