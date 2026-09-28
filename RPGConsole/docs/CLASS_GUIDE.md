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
character_names.txt
    -> CharacterNameLoader
    -> CharacterNameCatalog
    -> CharacterNameSelector
    -> CharacterCreationMenu
    -> PlayableCharacter
    -> Party
```

### Definição e objeto de execução

```text
EnemyDefinition = modelo reutilizável do inimigo
Enemy           = uma ocorrência mutável dentro de uma batalha

ScenarioDefinition = dados comuns carregados de scenarios.txt
Scenario           = cenário de execução usado por World

SkillDefinition = conteúdo imutável e reutilizável da habilidade
SkillUseState  = estado temporário de uma única utilização

CombatSkillDefinition = contrato mínimo compartilhado de execução
SkillDefinition       = habilidade pertencente ao sistema jogável
EnemySkillDefinition  = habilidade reutilizável de inimigos
EnemySkillsetEntry     = vínculo entre espécie, habilidade e nível mínimo

SkillScrollDefinition = vínculo imutável entre item, habilidade e classe
SkillScroll           = item especializado criado para o inventário
```

### Coordenação principal

```text
Main
    -> carrega e conecta dependências
    -> escolhe criação normal ou `--dev-party`
    -> cria a Party somente após confirmação ou configuração
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
| `Main` | Ponto de entrada / composição | Inicia a aplicação, cria o `ConsoleInput` compartilhado, carrega os arquivos externos, monta catálogos e serviços e escolhe entre `--dev-party` e o fluxo normal de seleção de campanha. No fluxo normal, ativa uma campanha restaurada ou cria o protagonista e a nova `Party`; por fim, conecta as dependências e inicia `World`. Deve concentrar montagem, não regras de jogo. |
| `GameCurrency` | Utilitário de apresentação | Centraliza o nome visível da moeda e a formatação de valores numéricos. Não armazena ouro, define preços ou executa transações; essas responsabilidades pertencem aos respectivos estados e sistemas. |

---

## Pacote `rpg.characters`

| Classe | Categoria | Finalidade e relações principais |
|---|---|---|
| `CharacterStats` | Dados imutáveis validados | Agrupa HP, MP, SP, ataque, defesa, magia, resistência, velocidade e sorte. Exige HP máximo e todos os atributos regulares positivos; MP e SP máximos podem ser zero. É usado na criação de `PlayableCharacter`, `Enemy` e `EnemyDefinition`. |
| `GameCharacter` | Classe-base de execução | Guarda os atributos naturais, HP, MP e SP atuais e o estado temporário de combate compartilhado por aliados e inimigos. Os getters públicos combinam valores naturais, bônus estruturais fornecidos pela subclasse e efeitos temporários. Centraliza restauração e gasto validado de MP/SP para ambos os lados da batalha. Também expõe uma cópia imutável apenas dos atributos naturais. O cálculo de ataques pertence a `DamageCalculator`. |
| `Enemy` | Inimigo de execução | Estende `GameCharacter` e representa uma ocorrência mutável de um inimigo dentro de uma batalha. Retém o ID canônico de `EnemyDefinition`, nível positivo oculto, rank, perfil de alvo, descrição, tabela de drops e lista imutável das `EnemySkillDefinition` selecionadas para aquela instância. Vários objetos `Enemy` podem nascer da mesma definição e nível sem compartilhar HP, recursos, efeitos, memória de alvo ou loadout. |
| `EnemyRank` | Enum de domínio | Define os ranks válidos dos inimigos e centraliza seus multiplicadores de recompensa, HP e poder ofensivo, além do teto de skills instaladas em uma instância. `LEGENDARY` permanece reconhecido, mas sem multiplicadores de atributos ou teto de skills configurados. É usado por loaders, definições, crescimento, seleção de loadout, recompensas e validações de dungeon. |
| `CharacterEquipment` | Estado de equipamentos | Guarda as definições da arma, armadura e berloque equipados por um personagem. Expõe slots opcionais, soma bônus estruturais e fornece os modificadores de recompensa do berloque. A mutação direta é restrita ao pacote de personagens. |
| `PlayableCharacter` | Personagem jogável | Estende `GameCharacter` com nome normalizado validado, nível, EXP, definição mutável de classe, equipamentos, definições de habilidade e IDs das habilidades efetivamente aprendidas. Aprende automaticamente habilidades `LEVEL`, permite aprender ou substituir habilidades `SCROLL`, preserva sua ordem e limita pergaminhos a duas vagas. Expõe seis posições opcionais de habilidade: quatro naturais e duas de pergaminho, sem revelar habilidades futuras. Ao ganhar níveis, recalcula os atributos naturais por meio de `CharacterGrowthCalculator`. Sua transição controlada de classe exige equipamentos vazios, preserva nível, EXP e habilidades de pergaminho, substitui a classe e o repertório natural, recalcula os atributos e restaura completamente os recursos. |
| `Party` | Estado compartilhado do grupo | Armazena de um a quatro `PlayableCharacter`, inventário, ouro e modificadores temporários compartilhados. Rejeita integrantes repetidos e nomes equivalentes sem diferenciar maiúsculas e minúsculas; o primeiro integrante é o líder. Também soma os bônus de GOLD dos berloques equipados e centraliza, por meio de `fullyRestoreMembers()`, a ressurreição e a restauração completa de HP, MP e SP de todos os integrantes. `canAddMember()` expõe a mesma validação sem mutação para planos transacionais, enquanto `addMember()` reutiliza essa validação antes da inserção. |
| `CharacterNameRules` | Regras de domínio | Centraliza normalização NFC, limite de 16 pontos de código Unicode, caracteres permitidos, validação e comparação sem diferenciação de maiúsculas e minúsculas para nomes de personagens jogáveis. |
| `CharacterNameCatalog` | Catálogo | Armazena os nomes externos já normalizados e validados, preservando sua ordem e rejeitando duplicatas sem diferenciar maiúsculas e minúsculas. |
| `CharacterNameLoader` | Carregamento de dados | Lê `character_names.txt`, ignora comentários e linhas vazias, aplica `CharacterNameRules`, acrescenta arquivo e linha aos erros e exige nomes suficientes para uma `Party` completa. |
| `CharacterNameSelector` | Seletor aleatório | Seleciona uniformemente um nome de `CharacterNameCatalog` depois de excluir os nomes informados pelo fluxo consumidor. Não conhece menus nem modifica personagens. |

---

## Pacote `rpg.classes`

| Classe | Categoria | Finalidade e relações principais |
|---|---|---|
| `PlayableClassDefinition` | Definição imutável | Armazena ID, nome, um símbolo de apresentação com exatamente um ponto de código Unicode visível, atributos-base, atributos primário e secundário, peso de armadura compatível e descrição de uma classe jogável. É referenciada por `PlayableCharacter`, pelas definições de habilidade e pelas interfaces de criação e status. |
| `CharacterGrowthCalculator` | Calculador de domínio | Calcula todos os atributos naturais de uma classe em um nível-alvo a partir dos atributos-base e das regras globais de crescimento. Centraliza os intervalos primário e secundário, os bônus de recursos por especialização e a proteção contra overflow. |
| `PlayableClassLoader` | Carregamento de dados | Lê as definições de classe de `playable_classes.txt`, interpreta o símbolo e descrições com `\n`, cria `CharacterStats`, valida os dados e acrescenta linha e ID ao erro quando um cadastro é inválido. |
| `PlayableClassCatalog` | Catálogo | Registra e localiza `PlayableClassDefinition` por ID estável e permite que `SkillLoader` valide a classe associada a cada habilidade. |

---

## Pacote `rpg.classes.paths`

| Classe | Categoria | Finalidade e relações principais |
|---|---|---|
| `ClassPathRules` | Constantes de domínio | Centraliza os IDs obrigatórios de `flagelo_iniciante` e `flagelo`, o nível mínimo da transição e o ID da flag permanente de conclusão. |
| `ClassPathRequiredClassValidator` | Validador cruzado | Confirma durante a inicialização que as definições obrigatórias do Flagelo iniciante e final existem e compartilham nome visível, símbolo, atributos-base, especializações e peso de armadura. Permite descrições e repertórios naturais diferentes. |
| `ClassPathDefinition` | Definição imutável | Associa uma classe final ao equipamento introdutório composto por uma arma e uma armadura. Não armazena escolha nem estado de campanha. |
| `ClassPathCatalog` | Catálogo | Registra os sete caminhos finais em ordem externa estável e permite resolução pelo ID canônico da classe. |
| `ClassPathLoader` | Carregamento de dados | Lê `class_paths.txt`, rejeita o Flagelo iniciante, valida cobertura exata das sete classes finais, equipamentos, requisitos de nível e compatibilidade do peso da armadura. |
| `ClassPathAnswerDefinition` | Definição imutável | Representa uma resposta ordenada, seu texto narrativo e a classe final que recebe exatamente um ponto quando ela é escolhida. |
| `ClassPathQuestionDefinition` | Definição agregada imutável | Agrupa uma pergunta ordenada e ao menos duas respostas consecutivas, preservando a ordem externa. |
| `ClassPathQuestionnaireCatalog` | Catálogo | Armazena as perguntas completas do questionário em ordem estável e permite resolver suas respostas canônicas. |
| `ClassPathQuestionnaireLoader` | Carregamento agregado | Lê `class_path_questions.txt` e `class_path_answers.txt`, usa drafts privados, resolve quebras e placeholders narrativos e valida perguntas, respostas, ordens e alvos recomendáveis. |
| `ClassPathQuestionnaireVerifier` | Validador estrutural | Confirma a distribuição canônica de sete perguntas, quatro ternárias, três binárias, dezoito respostas e exposição simétrica das seis classes recomendáveis. Também garante que o Flagelo final não seja recomendado e que nenhum empate máximo possa ultrapassar três classes. |
| `ClassPathRecommendationResult` | Resultado imutável | Reúne as classes com a maior pontuação em ordem estável, representando tanto uma recomendação direta quanto um empate de duas ou três classes sem sorteio. |
| `ClassPathRecommendationService` | Serviço de cálculo puro | Recebe uma resposta canônica para cada pergunta, concede um ponto por resposta e produz todas as classes empatadas na maior pontuação. Não altera personagem nem campanha. |
| `ClassPathTransitionFailureType` | Enum de falha | Distingue cerimônia já concluída, classe atual inelegível, nível insuficiente e capacidade de inventário insuficiente. |
| `ClassPathTransitionFailure` | Resultado de falha | Transporta a causa confirmada de uma transição recusada sem exigir que a interface deduza novamente a regra. |
| `ClassPathTransitionPlan` | Plano imutável | Registra a classe final, o equipamento devolvido, o kit introdutório e as adições consolidadas ao inventário já validadas para uma possível aplicação. |
| `ClassPathTransitionPreparation` | Resultado de preparação | Contém um plano completo ou falhas explícitas, permitindo que a transição seja validada isoladamente ou incorporada à transação composta de recrutamento sem mutar a campanha. |
| `ClassPathTransitionService` | Serviço transacional | Prepara e revalida a transição completa, devolve equipamentos por meio de `EquipmentService`, concede o kit, solicita a troca controlada de classe, restaura os recursos e define a flag permanente sem permitir mutação parcial. |

---

## Pacote `rpg.recruitment`

| Classe | Categoria | Finalidade e relações principais |
|---|---|---|
| `QuestRecruitmentDefinition` | Definição imutável | Associa uma quest e etapa reais a um ID canônico de candidato e ao nível de entrada do futuro aliado. Não guarda nome, classe sorteada nem estado mutável. |
| `QuestRecruitmentCatalog` | Catálogo de relações | Registra no máximo três recrutamentos, preserva a ordem externa e permite busca por quest e etapa ou por candidato. Rejeita chaves e candidatos duplicados. |
| `QuestRecruitmentLoader` | Carregamento de dados | Lê `quest_recruitments.txt`, valida os quatro campos, referências de quest e etapa, IDs canônicos, nível jogável suportado e o limite estrutural de três registros. |
| `RecruitmentCandidateState` | Estado mutável mínimo | Guarda o ID canônico, o nome gerado imutável e o indicador mutável de recrutamento de um candidato já apresentado à narrativa. Não contém estado de batalha. |
| `RecruitmentCandidateStateCatalog` | Catálogo de estado de campanha | Mantém os candidatos já introduzidos, valida que todos pertencem ao catálogo de recrutamentos e impede IDs ou nomes equivalentes repetidos. |
| `RecruitmentCandidateStateRestorer` | Gateway de restauração | Reconstrói candidatos validados por meio das mutações package-private do domínio, pode aplicar o estado recrutado e publica o catálogo completo uma única vez. Permanece em `rpg.recruitment` para não transformar APIs de restauração em mutações públicas gerais. |
| `RecruitmentCandidateIntroductionService` | Serviço de campanha | Introduz um candidato de forma idempotente, reúne nomes da party e de outros candidatos, solicita um nome único ao `CharacterNameSelector` e registra o estado somente após seleção válida. |
| `AllyClassSelector` | Seletor aleatório | Seleciona uniformemente uma classe elegível, excluindo o Flagelo iniciante, o Flagelo final, todas as classes já presentes na party e IDs adicionais fornecidos pelo fluxo consumidor. |
| `RecruitmentAllyFactory` | Fábrica de domínio | Constrói um aliado completo no nível declarado, calcula atributos e habilidades naturais, equipa diretamente arma e armadura introdutórias, mantém o amuleto vazio e restaura todos os recursos. |
| `AllyRecruitmentPlan` | Plano imutável | Registra a party e seus integrantes originais, a definição canônica, o candidato persistente e o aliado completo ainda não inserido. Valida coerência de ID, nome e nível. |
| `AllyRecruitmentService` | Serviço transacional genérico | Prepara, revalida e aplica recrutamentos reutilizáveis. Coordena catálogo, candidato, capacidade da party, seleção de classe, fábrica de aliado, inserção e marcação do candidato sem conhecer a cerimônia do protagonista. |
| `ClassPathRecruitmentPlan` | Plano composto imutável | Reúne a transição confirmada do protagonista e o plano do primeiro aliado sob a mesma interação de quest, exigindo classes finais diferentes. |
| `ClassPathRecruitmentPreparation` | Resultado composto | Contém um plano completo ou falhas esperadas da transição de classe e da conclusão da quest, sem realizar mutação parcial. |
| `ClassPathRecruitmentService` | Serviço transacional composto | Coordena a primeira cerimônia, a validação conjunta de inventário e recompensas, a criação do aliado e o commit ordenado de transição, quest, recompensas, efeitos, inserção na party e estado recrutado. |

---

## Pacote `rpg.development`

| Classe | Categoria | Finalidade e relações principais |
|---|---|---|
| `DevelopmentPartyFactory` | Fábrica de desenvolvimento | Cria a `Party` usada quando `Main` recebe `--dev-party`. Mantém a configuração temporária fora do fluxo normal, exige de um a quatro integrantes com classes distintas e níveis positivos, usa a progressão normal para alcançar cada nível e restaura os recursos ao final. O primeiro integrante configurado torna-se o líder. |

---

## Pacote `rpg.enemies`

| Classe | Categoria | Finalidade e relações principais |
|---|---|---|
| `EnemyLoader` | Carregamento de dados | Lê `enemies.txt`, ignora comentários e linhas vazias, converte atributos-base, especializações opcionais, rank, perfil de alvo, altura e peso, interpreta quebras literais `\n` e cria `EnemyDefinition`. Registra cada definição em `EnemyCatalog` e acrescenta linha e ID ao erro quando os dados são inválidos. |
| `EnemyDefinition` | Definição imutável | Armazena ID, nome, atributos naturais de nível 1, atributos primário, secundário e de fraqueza opcionais, rank, perfil de alvo, descrição, altura e peso de uma espécie. `calculateNaturalStats(level)` delega o cálculo completo ao `EnemyGrowthCalculator`; `createEnemy` combina esses atributos com nível, loadout imutável e drops selecionados para produzir um `Enemy` mutável. Altura e peso permanecem dados descritivos do Bestiário. |
| `EnemyGrowthCalculator` | Calculador de domínio | Valida conflitos entre especializações e calcula atributos naturais completos a partir da definição, do rank e do nível-alvo. Centraliza crescimento de HP, MP, SP e atributos regulares, fraqueza, multiplicadores de HP/ATK/MAG, arredondamento e proteção contra overflow. MP e SP crescem cinco pontos por nível sem bônus de rank ou especialização. |
| `EnemyTargetProfile` | Enum de domínio | Define os quatro comportamentos de alvo individual: `INSTINCTIVE`, `ERRATIC`, `PREDATOR` e `TACTICAL`, junto aos nomes visíveis Instintiva, Errática, Predadora e Tática reutilizados pelas interfaces. Permanece independente de `EnemyRank` e é carregado de `enemies.txt`. |
| `EnemyCatalog` | Catálogo | Registra `EnemyDefinition` por ID estável, rejeita IDs duplicados e permite recuperar a definição de um inimigo. É usado por loaders que validam referências, por `EncounterGenerator`, por dungeons e por `BattleCoordinator`. |
| `EnemyDropLoader` | Carregamento de dados | Lê `enemy_drops.txt`, valida os IDs de inimigos usando `EnemyCatalog`, resolve os itens completos no `ItemCatalog` recebido, cria `DropEntry` e registra as entradas em `EnemyDropCatalog`. |
| `EnemyDropCatalog` | Catálogo de relações | Associa cada ID de inimigo à sua lista imutável de `DropEntry`. É consultado pelo `BattleCoordinator` quando cria os inimigos de execução que participarão da batalha. |

### Diferença essencial

```text
EnemyLoader           -> lê o arquivo
EnemyDefinition       -> guarda o modelo reutilizável
EnemyCatalog          -> localiza o modelo por ID
EnemyGrowthCalculator -> calcula atributos no nível escolhido
BattleCoordinator     -> combina nível, skills e drops
Enemy                 -> luta, gasta recursos e perde HP
```

---

## Pacote `rpg.flags`

| Classe | Categoria | Finalidade e relações principais |
|---|---|---|
| `FlagDefinition` | Definição imutável | Armazena o ID estável e a descrição externa de uma consequência permanente do mundo. Não guarda se a flag está ativa em uma campanha. |
| `FlagCatalog` | Catálogo | Registra definições de flags em ordem estável, rejeita IDs duplicados e resolve referências usadas por requisitos e efeitos de quest. |
| `FlagLoader` | Carregamento de dados | Lê `flags.txt`, interpreta descrições e quebras literais `\n`, produz definições completas e acrescenta arquivo, linha e conteúdo inválido aos erros. |
| `CampaignFlags` | Estado mutável | Guarda somente os IDs definidos durante uma campanha. Valida cada ID pelo catálogo, permite apenas adição, retorna se uma nova flag foi realmente definida e trata repetição como operação idempotente. |

`FlagDefinition` responde o que uma flag significa. `CampaignFlags` responde quais flags já ocorreram em uma campanha. Quest status não é duplicado nesse estado.

---

## Pacote `rpg.requirements`

| Classe | Categoria | Finalidade e relações principais |
|---|---|---|
| `RequirementType` | Enum de domínio | Define os sete tipos iniciais de requisito: nível mínimo do líder, quantidade de item, três estados de quest e presença ou ausência de flag. |
| `RequirementDefinition` | Definição imutável | Representa uma condição individual com tipo, alvo opcional e valor opcional validados conforme o tipo. Não avalia estado de campanha. |
| `RequirementSetDefinition` | Definição agregada | Agrupa requisitos ordenados combinados por AND. Rejeita ordem inválida, duplicações e contradições internas. |
| `RequirementCatalog` | Catálogo | Registra e resolve conjuntos por ID estável, preservando a ordem externa de declaração. |
| `RequirementLoader` | Carregamento de dados | Lê `requirements.txt`, monta conjuntos completos, valida tipos, valores, ordem e referências e não expõe catálogos parcialmente preenchidos. |
| `RequirementEvaluator` | Avaliador puro | Consulta `Party`, inventário, `QuestProgress` e `CampaignFlags` para avaliar um conjunto sem armazenar cache, consumir itens ou alterar qualquer estado. |
| `QuestAvailabilityValidator` | Validador cruzado | Valida os conjuntos referenciados pelas ofertas de quests e rejeita dependência da própria quest e ciclos diretos ou indiretos entre disponibilidades. |

Um resultado de requisito é sempre derivado. Consumidores devem reavaliá-lo antes da ação protegida em vez de armazená-lo como estado mutável.

---

## Pacote `rpg.quests`

| Classe | Categoria | Finalidade e relações principais |
|---|---|---|
| `QuestDefinition` | Definição agregada imutável | Armazena ID, título, descrição, NPC inicial, requisito opcional de disponibilidade e passos sequenciais. Não contém status nem contadores da campanha. |
| `QuestStepDefinition` | Definição agregada imutável | Armazena número, título, descrição e objetivos ordenados de um passo. Exige ao menos um objetivo e valida composição de conversa e entrega. |
| `QuestObjectiveDefinition` | Definição imutável | Armazena ID global, ordem, tipo, alvo opcional, quantidade exigida e texto apresentado. Valida alvo e quantidade conforme o tipo. |
| `QuestObjectiveType` | Enum de domínio | Define `TALK_TO_NPC`, `DEFEAT_ENEMY`, `ANALYZE_ENEMY`, `OBTAIN_ITEM`, `DELIVER_ITEM`, `REACH_SCENARIO`, `COMPLETE_DUNGEON` e `WIN_BATTLE`. |
| `QuestCatalog` | Catálogo | Registra quests completas em ordem estável, rejeita IDs duplicados e permite consulta por ID sem guardar progresso. |
| `QuestLoader` | Carregamento agregado | Lê `quests.txt`, `quest_steps.txt` e `quest_objectives.txt`. Usa drafts privados enquanto relaciona os três arquivos e publica somente quests completas e validadas. |
| `QuestStatus` | Enum de estado | Representa os estados `NOT_STARTED`, `ACTIVE` e `COMPLETED`. Disponibilidade continua derivada por requisitos, não se torna um quarto status. |
| `QuestProgress` | Estado mutável | Guarda quests ativas, passo atual, contadores apenas dos objetivos atuais e IDs concluídos. Impõe ativação única, progresso limitado, avanço linear e conclusão irreversível. |
| `QuestProgressRestorer` | Gateway de restauração | Reconstrói quests ativas e concluídas usando as mutações package-private validadas de `QuestProgress` e publica o estado completo uma única vez. Permanece em `rpg.quests` para preservar o encapsulamento do domínio. |
| `QuestProgressEventType` | Enum de integração | Mapeia operações confirmadas do gameplay aos tipos de objetivo correspondentes. |
| `QuestProgressEvent` | Entrada imutável | Transporta tipo de evento, alvo canônico opcional e quantidade positiva para processamento síncrono. |
| `QuestProgressUpdateType` | Enum de resultado | Distingue progresso parcial, objetivo concluído, passo concluído e quest concluída. |
| `QuestProgressUpdate` | Resultado imutável | Registra uma alteração produzida pelo processamento, incluindo quest, passo, objetivo e quantidades quando aplicáveis. |
| `QuestCompletionFailureType` | Enum de falha | Classifica impedimentos explícitos da transação, como estado incompatível, entrega insuficiente ou capacidade de recebimento. |
| `QuestCompletionFailure` | Resultado de falha | Transporta a causa confirmada de uma conclusão recusada sem exigir que a interface deduza novamente a regra. |
| `QuestProgressResult` | Resultado agregado | Reúne atualizações ordenadas, recompensas, efeitos visíveis ou falha de conclusão produzidos por uma resolução. Permite apresentação agrupada sem impressão pelo domínio. |
| `QuestProgressService` | Serviço de domínio transacional | Processa eventos confirmados, inicia quests, valida interações de NPC e monta o plano completo de conclusão. Coordena entrega, recompensas, efeitos e transição de estado sem permitir mutação parcial, além de validar e repetir a conclusão de uma etapa final já completa quando ela não depende de `TALK_TO_NPC`. |
| `QuestNpcInteractionResolver` | Resolvedor derivado | Determina ofertas e interações da etapa atual disponíveis para um NPC usando catálogo, progresso e requisitos. Preserva a ordem das quests e não armazena marcadores nem altera estado. |

`QuestProgressService` é chamado explicitamente pelos fluxos que confirmam operações. Não existe um barramento global de eventos.

---

## Pacote `rpg.quests.rewards`

| Classe | Categoria | Finalidade e relações principais |
|---|---|---|
| `QuestRewardType` | Enum de domínio | Define recompensas materiais de `GOLD`, `PARTY_EXP` e `ITEM`. |
| `QuestRewardDefinition` | Definição imutável | Armazena quest, ordem, tipo, ID opcional e quantidade positiva. Valida limites máximos de GOLD e item quando aplicáveis. |
| `QuestRewardCatalog` | Catálogo de relações | Agrupa recompensas ordenadas por quest e permite que uma quest não possua recompensa material. |
| `QuestRewardLoader` | Carregamento de dados | Lê `quest_rewards.txt`, valida quests, itens, ordem, duplicações e quantidades e produz o catálogo completo. |

As descrições exibidas ao jogador são derivadas dessas definições; não existe uma segunda lista textual dentro de `QuestDefinition`.

---

## Pacote `rpg.quests.effects`

| Classe | Categoria | Finalidade e relações principais |
|---|---|---|
| `QuestEffectTrigger` | Enum de domínio | Define os momentos `QUEST_START`, `STEP_COMPLETE` e `QUEST_COMPLETE`. |
| `QuestEffectType` | Enum de domínio | Define `SET_FLAG`, `UNLOCK_ROUTE`, `GRANT_ITEM` e `TRANSPORT_PARTY`. |
| `QuestEffectDefinition` | Definição imutável | Armazena quest, passo, trigger, ordem, tipo, alvo, quantidade quando aplicável e texto visível opcional. Valida a relação entre trigger e passo. |
| `QuestEffectCatalog` | Catálogo de relações | Agrupa efeitos ordenados por localização de trigger e permite consulta durante início, transição ou conclusão de quest. |
| `QuestEffectLoader` | Carregamento de dados | Lê `quest_effects.txt` e valida referências de flags, rotas, bifurcações, itens e cenários. Impede `GRANT_ITEM` e `TRANSPORT_PARTY` em `QUEST_START`. |

Efeitos de passo pertencem à transição do passo. Efeitos finais pertencem à mesma transação que entregas, recompensas e conclusão da quest.

---

## Pacote `rpg.narrative`

| Classe | Categoria | Finalidade e relações principais |
|---|---|---|
| `NarrativeLocationDefinition` | Definição imutável | Armazena ID, cidade, nome, descrição e conjunto opcional de requisitos de um espaço narrativo urbano. |
| `NarrativeLocationCatalog` | Catálogo | Registra locais por ID e os agrupa por cidade em ordem estável. |
| `NarrativeLocationLoader` | Carregamento de dados | Lê `narrative_locations.txt`, valida IDs, textos, cidades e requisitos e cria o catálogo completo. |
| `NpcDefinition` | Definição imutável | Armazena identidade canônica, nome, descrição e ID do diálogo padrão. Não contém localização ou estado mutável. |
| `NpcCatalog` | Catálogo | Registra NPCs por ID estável e preserva sua ordem externa. |
| `NpcLoader` | Carregamento de dados | Lê `npcs.txt`, valida identidade e textos e deixa referências de diálogo para a validação cruzada executada quando todos os catálogos existem. |
| `NpcPlacementDefinition` | Definição de relação | Associa um NPC a um local narrativo fixo e a um conjunto opcional de requisitos de visibilidade. |
| `NpcPlacementCatalog` | Catálogo de relações | Registra no máximo um posicionamento por NPC e agrupa posicionamentos por local em ordem estável. |
| `NpcPlacementLoader` | Carregamento de dados | Lê `npc_placements.txt` e valida referências de NPC, local narrativo e requisito. |
| `DialogueSpeakerType` | Enum narrativo | Define `NPC`, `ENEMY`, `LEADER`, `NARRATOR` e `RECRUIT`. |
| `DialogueLineDefinition` | Definição imutável | Representa uma fala com número, tipo de locutor, ID opcional e texto processado. |
| `DialogueDefinition` | Definição agregada imutável | Agrupa falas ordenadas e consecutivas sob um ID de diálogo. |
| `DialogueCatalog` | Catálogo | Registra diálogos completos por ID estável. |
| `DialogueLoader` | Carregamento de dados | Lê `dialogues.txt`, monta sequências lineares e valida ordem, locutores e textos. Referências `RECRUIT` devem apontar para candidatos declarados em `QuestRecruitmentCatalog`. |
| `QuestDialogueContext` | Enum narrativo | Define `OFFER`, `ACCEPT`, `DECLINE`, `PENDING` e `COMPLETE`. |
| `QuestDialogueDefinition` | Definição de relação | Associa quest, passo e contexto a um diálogo canônico. |
| `QuestDialogueCatalog` | Catálogo de relações | Resolve diálogos contextuais por quest, passo e contexto sem duplicar o conteúdo de `DialogueDefinition`. |
| `QuestDialogueLoader` | Carregamento de dados | Lê `quest_dialogues.txt`, valida combinações de passo e contexto e prepara as relações para validação cruzada. |
| `NarrativeTextProcessor` | Processador compartilhado | Decodifica `\n`, valida os placeholders e resolve nomes da party e de candidatos introduzidos, inclusive `{recruitName:candidateId}`. |
| `NarrativeReferenceValidator` | Validador cruzado | Depois que todos os catálogos existem, valida diálogos padrão, locutores, NPCs iniciais, alvos de conversa e cobertura contextual das interações de quest. |

Identidade, posicionamento e fala são conceitos separados. Um NPC pode existir sem posicionamento, e vários diálogos podem reutilizar a mesma identidade canônica.

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
| `SkillScrollDefinition` | Definição especializada imutável | Liga o ID de um item ao ID de uma habilidade `SCROLL` e ao ID de sua classe compatível. Mantém referências externas estáveis sem duplicar a `SkillDefinition`. |
| `SkillScrollDefinitionCatalog` | Catálogo especializado | Registra especializações por ID de item e de habilidade, rejeita vínculos duplicados e permite que loaders e fábricas resolvam a relação. |
| `SkillScrollLoader` | Carregamento especializado | Lê `skill_scrolls.txt`, valida referências nos catálogos de itens, habilidades e classes, exige `SKILL_SCROLL` e `SCROLL`, rejeita divergências de classe e verifica especialização completa. |
| `ItemFactory` | Fábrica de execução | Combina cada `ItemDefinition` com suas eventuais definições especializadas. Cria `RecoveryItem` para recuperação, `SkillScroll` para pergaminhos vinculados a uma habilidade canônica e `BasicItem` quando o item não possui comportamento próprio. Rejeita especializações incompatíveis simultâneas. |
| `Item` | Classe-base de domínio | Classe-base dos itens em runtime. Copia da definição a identidade, a categoria, `playerTradable`, `baseShopSalePrice`, `shopPurchasePrice`, nome e descrição. |
| `BasicItem` | Item básico | Implementação concreta mínima de `Item` para categorias que ainda não possuem comportamento especializado. Pode ser armazenado, exibido e recebido como recompensa, mas não executa uma ação de uso. |
| `RecoveryItem` | Item de recuperação | Especializa `Item` com efeitos combináveis de HP, MP, SP, ressurreição e purificação. Verifica alvos elegíveis, aplica ressurreição antes da recuperação comum de HP e devolve um `RecoveryItemUseResult`. |
| `SkillScroll` | Pergaminho de execução | Especializa `Item` com a definição do vínculo e a `SkillDefinition` canônica que será ensinada. Expõe a classe compatível e o requisito mínimo de nível sem duplicar regras de habilidade. |
| `SkillScrollService` | Serviço de aprendizagem e consumo | Coordena o aprendizado ou a substituição entre `Party`, `PlayableCharacter` e `SkillScroll`. Consome uma unidade somente após `LEARNED` ou `REPLACED`; rejeições preservam o inventário. |
| `RecoveryItemUseResult` | Resultado imutável | Informa separadamente os efeitos realmente produzidos por um uso, evitando que menus precisem deduzir o resultado comparando estados anteriores e posteriores do personagem. |
| `ItemCatalog` | Catálogo de execução | Registra os objetos `Item` completos criados por `ItemFactory`, rejeita IDs duplicados e permite resolvê-los por ID. É usado por drops, exploração e demais fluxos que precisam do item de execução, não das definições cruas. |
| `Inventory` | Estado mutável | Guarda os itens pertencentes à `Party` e suas quantidades compartilhadas. Permite adicionar, remover, consultar e listar itens; não impõe peso nem limite de capacidade. |
| `DropEntry` | Entrada de drop | Define uma possibilidade individual de recompensa: item, chance e quantidades mínima e máxima. É carregada por `EnemyDropLoader`, armazenada em `EnemyDropCatalog` e sorteada por `BattleRewardService`. |
---

## Pacote `rpg.items.equipment`

| Classe | Categoria | Finalidade e relações principais |
|---|---|---|
| `EquipmentDefinition` | Definição-base imutável | Reúne o ID do item, requisito de nível e os nove bônus comuns a todo equipamento. É especializada por arma, armadura e berloque. |
| `WeaponDefinition` | Definição de arma | Acrescenta tipo de dano, tipo de arma, nome do ataque normal e poder-base da arma. O ataque básico e as habilidades ofensivas consultam esse poder. |
| `ArmorDefinition` | Definição de armadura | Acrescenta o peso da armadura. A compatibilidade exige correspondência exata com o peso aceito pela classe. |
| `TrinketDefinition` | Definição de berloque | Acrescenta o tipo do berloque e os modificadores percentuais de EXP individual e GOLD compartilhado. |
| `EquipmentLoader` | Carregamento especializado | Lê `weapons.txt`, `armors.txt` e `trinkets.txt`, valida referências, categorias, valores, enums e duplicidades e registra todas as especializações no mesmo catálogo. |
| `EquipmentCatalog` | Catálogo | Resolve qualquer definição de equipamento pelo ID estável do item e impede que o mesmo ID receba mais de uma especialização. |
| `EquipmentService` | Serviço de transferência | Coordena equipar, trocar e remover itens entre o inventário da `Party` e os slots de `PlayableCharacter`. Valida pertencimento ao grupo e preserva a unidade anterior em trocas. |
| `WeaponType` | Enum de domínio | Define as categorias fechadas de arma aceitas pelos dados externos. |
| `ArmorWeight` | Enum de domínio | Define armaduras `LIGHT`, `MEDIUM` e `HEAVY` e também é usado pela definição da classe jogável. |
| `TrinketType` | Enum de domínio | Define berloques `JEWELRY`, `AMULET` e `TALISMAN`. |

---

## Pacote `rpg.skills`

| Classe | Categoria | Finalidade e relações principais |
|---|---|---|
| `CombatSkillDefinition` | Interface de domínio | Expõe somente ID, custos, nome, componentes ordenados e consulta por fase necessários à execução em batalha. É implementada por `SkillDefinition` e `EnemySkillDefinition`; não contém regras de aprendizagem, classe ou slots. |
| `SkillDefinition` | Definição jogável imutável | Implementa `CombatSkillDefinition` e acrescenta classe jogável, tipo de aquisição, requisito de nível e descrição. Continua sendo o único tipo aceito por progressão, pergaminhos, status e slots de personagens jogáveis. |
| `EnemySkillDefinition` | Definição inimiga imutável | Implementa `CombatSkillDefinition` com ID, custos, nome e componentes ordenados. Não possui classe, aquisição, requisito próprio de nível, descrição ou propriedade por personagem jogável. Rejeita combinações explicitamente proibidas para inimigos, como `REVIVE`. |
| `SkillEffectDefinition` | Definição imutável de componente | Representa um componente de dano, recuperação, estágio, purificação, dissipação, modificador ou estado especial. Valida quais campos são obrigatórios, opcionais ou proibidos para cada tipo, independentemente do lado que executará a skill. |
| `SkillLoader` | Carregamento compartilhado | Lê `skills.txt`, `enemy_skills.txt` e `skill_effects.txt`; valida cabeçalhos, IDs globalmente únicos, classes jogáveis, aquisição, componentes, sequência, fases e referências cruzadas; registra definições completas em `SkillCatalog` e `EnemySkillCatalog`. |
| `SkillCatalog` | Catálogo jogável | Registra `SkillDefinition` por ID estável e oferece exclusivamente as definições jogáveis aos personagens, pergaminhos e sistemas de apresentação. |
| `EnemySkillCatalog` | Catálogo inimigo | Registra `EnemySkillDefinition` por ID estável, rejeita duplicatas e oferece as definições inimigas ao carregamento dos vínculos e à criação de loadouts. |
| `EnemySkillsetEntry` | Relação imutável | Associa um ID de espécie a uma `EnemySkillDefinition` e a um nível mínimo positivo. Não representa uma skill instalada nem contém estado de batalha. |
| `EnemySkillsetLoader` | Carregamento de relações | Lê `enemy_skillsets.txt`, resolve inimigos e skills nos catálogos, rejeita vínculos duplicados e referências inválidas, exige cobertura para toda skill inimiga e valida MP/SP naturais no nível mínimo antes de registrar cada entrada. |
| `EnemySkillsetCatalog` | Catálogo de relações | Agrupa `EnemySkillsetEntry` por ID de inimigo e devolve as skills elegíveis para um nível. É consultado por `BattleCoordinator` antes da criação de cada `Enemy`. |
| `SkillAcquisitionType` | Enum de aquisição | Distingue habilidades aprendidas automaticamente por nível (`LEVEL`) e habilidades ensinadas exclusivamente por pergaminho (`SCROLL`). Aplica-se apenas a `SkillDefinition`. |
| `SkillLearningResult` | Enum de resultado | Representa aprendizagem, substituição e rejeições por duplicidade, classe incompatível, nível insuficiente ou limite de pergaminhos. Permite que domínio, serviço e interface compartilhem o mesmo resultado sem depender de mensagens. |
| `SkillEffectType` | Enum de domínio | Define os tipos fechados de componente reconhecidos pelo executor. |
| `SkillTargetType` | Enum de alvo | Define usuário, aliado, inimigo, grupos completos ou alvos aleatórios. Seu significado é relativo ao lado do usuário da skill. |
| `SkillExecutionPhase` | Enum de fase | Ordena componentes em `PRE_ACTION`, `PRIMARY` e `POST_ACTION`. |
| `SkillConditionType` | Enum de condição | Define condições opcionais avaliadas antes da aplicabilidade e dos testes probabilísticos. |

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
| `BattleCoordinator` | Coordenador | Faz a ponte entre mundo e batalha. Recebe um ou mais `EnemySpawn`, resolve os drops e vínculos de skill, filtra skills pelo nível, seleciona cada loadout sem repetição dentro do teto do rank, cria inimigos independentes e monta `EnemyBattleGroup`. Depois inicia `Battle` e, em caso de vitória, usa `BattleRewardService` e `BattleRewardScreen`. |
| `EnemyTargetSelector` | Seletor de domínio | Resolve alvos individuais vivos conforme o `EnemyTargetProfile` e mantém memória independente por instância durante uma batalha. Sorteia uniformemente para perfis erráticos e instintivos; para predadores e táticos usa pesos positivos, HP percentual perdido, defesa, DEF/RES efetiva quando aplicável e margem de troca. Não participa de alvos próprios, coletivos, aliados ou explicitamente aleatórios. |
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
| `DamageType` | Enum de domínio | Distingue dano físico e mágico para ataques básicos e habilidades, selecionando ATK/DEF ou MAG/RES. Pertence a `rpg.battle` porque não é exclusivo de habilidades. |
| `RecoveryCalculator` | Calculador de domínio | Calcula recuperação de HP e ressurreição a partir do poder e do ATK ou MAG efetivo indicado pelo componente. MP e SP permanecem fixos. |
| `EffectResistanceCalculator` | Calculador de domínio | Calcula e sorteia a resistência baseada na LCK efetiva do aplicador e na RES efetiva do alvo. Mantém `KR` e o teto de chance em um único lugar. |
| `SkillExecutor` | Executor de domínio | Executa qualquer `CombatSkillDefinition`, orquestra condições, aplicabilidade, um trigger por componente, resistência individual e aplicação. Reutiliza calculadores e operações de `GameCharacter` sem repetir suas fórmulas. |
| `SkillUseState` | Estado de uma utilização | Preserva a `CombatSkillDefinition`, o contexto, os resultados, modificadores acumulados e componentes `POST_ACTION` de uma única utilização. Não pertence permanentemente ao personagem nem à definição. |
| `SkillExecutionContext` | Contexto imutável | Agrupa qualquer usuário `GameCharacter`, os alvos resolvidos relativamente ao lado atuante e o poder-base da arma capturado no início da skill. Personagens jogáveis fornecem o DBA equipado; inimigos fornecem zero. Os alvos aleatórios são sorteados por componente. |
| `SkillEffectResult` | Resultado imutável | Associa componente, alvo, desfecho e valor produzido para que `Battle` apenas apresente o resultado. |
| `SkillEffectOutcome` | Enum de resultado | Distingue `APPLIED`, `RESISTED` e `NOT_TRIGGERED`. |

### Fluxo resumido da batalha

```text
World
    -> BattleCoordinator
    -> EnemySpawn + skills elegíveis + drops
    -> Enemy
    -> EnemyBattleGroup
    -> Battle
    -> ataque básico ou SkillExecutor
    -> BattleResult
    -> World
```

---

## Pacote `rpg.battle.rewards`

| Classe | Categoria | Finalidade e relações principais |
|---|---|---|
| `RewardCalculator` | Calculador | Soma percentuais globais e individuais de forma aditiva, aplica-os à recompensa-base, arredonda uma única vez em half-up e rejeita overflow. Não concede nem armazena recompensas. |
| `PartyRewardModifierState` | Estado mutável | Guarda no máximo um modificador temporário de EXP e um de GOLD para a `Party`, com suas durações em vitórias recompensadas. Os dois tipos podem coexistir, mas efeitos do mesmo tipo não acumulam. |
| `BattleRewardModifiers` | Snapshot imutável | Registra os modificadores usados em uma vitória: percentuais globais temporários, GOLD dos equipamentos, EXP individual dos berloques e durações restantes após a recompensa. Permite apresentar o resultado sem consultar estado mutável já avançado. |
| `BattleRewardService` | Serviço de regra | Agrega recompensas-base, captura modificadores temporários e de equipamento, usa `RewardCalculator`, concede EXP individual e GOLD compartilhado, reduz a duração dos efeitos temporários após a vitória, calcula progressão e drops e produz `BattleRewards`. |
| `BattleRewards` | Resultado agregado | Agrupa a EXP global apresentada, o GOLD, o snapshot de modificadores, os drops consolidados e os resultados individuais de progressão. É produzido por `BattleRewardService` e exibido por `BattleRewardScreen`. |
| `CharacterProgressionResult` | Resultado individual | Registra EXP concedida, níveis anterior e atual, cópias dos atributos naturais antes e depois do crescimento, habilidades automaticamente aprendidas e eventual restauração de recursos. Faz parte de `BattleRewards`. |

---

## Pacote `rpg.ui`

As classes deste pacote cuidam da apresentação e da entrada do jogador. Elas podem validar escolhas, mas não devem decidir regras centrais de batalha, exploração ou progressão.

| Classe | Categoria | Finalidade e relações principais |
|---|---|---|
| `ConsoleInput` | Entrada compartilhada | Centraliza leitura UTF-8 por uma thread daemon e fila bloqueante. Permite descartar comandos durante pausas, trata `Ctrl+Z` recuperável no console Windows e substitui leitores independentes nos menus. |
| `ConsoleUI` | Utilitário de interface | Centraliza operações gerais do console, como limpeza, pausa, disposição compartilhada de ações e quebra automática de texto em 90 colunas. Preserva quebras intencionais já existentes e não decide regras de domínio. |
| `TitleScreen` | Tela | Exibe a apresentação inicial do jogo e controla a entrada necessária para começar. É chamada durante a inicialização antes de `World`. |
| `CharacterCreationMenu` | Menu de criação | Permite digitar ou sortear o nome do protagonista, repetir o sorteio, alterar a proposta, confirmar ou cancelar. Exibe nome e a apresentação completa do Flagelo iniciante por meio de `PlayableClassDetailFormatter`, mas devolve apenas o nome confirmado; `Main` cria o personagem e a `Party` posteriormente. |
| `PartyRescueScreen` | Tela | Apresenta o resgate após a derrota completa da `Party`. Recebe o grupo, o `CombatScenario` derrotado, a cidade escolhida e o custo calculado; exibe narrativa, destino, restauração e custo por meio de `GameCurrency`, adapta a mensagem ao tamanho da party e exige ENTER. Não altera ouro, personagens, cenário ou progresso. |
| `CombatScenarioMenu` | Menu de cenário de combate | Recebe as ações de `CombatScenarioAction` permitidas e o conjunto de ações com marcador derivado. Mantém numeração contínua, separa grupos visualmente, pode marcar `Seguir caminho` ou `Viajar` e oferece acesso ao diário sem avançar o tempo. Não é usado por cidades. |
| `CityMenu` | Menu de cidade | Recebe o nome da cidade, as ações disponíveis e o conjunto de ações com marcador derivado. Omite ações sem destino funcional, preserva numeração contínua, pode marcar `Visitar locais`, `Sair da cidade` ou `Viajar` e oferece acesso ao diário sem avançar o tempo. |
| `CityLocationMenu` | Menu de navegação urbana | Exibe INN, lojas, crafters e locais narrativos atualmente acessíveis em uma lista contínua. Acrescenta `[!]` aos locais narrativos acionáveis e devolve a posição selecionada sem executar regras internas. |
| `NarrativeLocationMenu` | Menu narrativo | Exibe cabeçalhos de cidade e local, NPCs atualmente visíveis e seus marcadores derivados. Quando nenhum NPC está visível, apresenta `Não há ninguém aqui.`. |
| `NpcInteractionMenu` | Menu narrativo | Exibe contexto, identidade e descrição do NPC, seguido pelas interações de quest disponíveis em ordem de catálogo e pela opção permanente `Conversar`. |
| `DialoguePresenter` | Apresentador linear | Resolve previamente locutores, placeholders e posições da party; depois apresenta falas externas em sequência, com cabeçalhos opcionais, quebra automática e avanço somente por Enter. Locutores `RECRUIT` são resolvidos pelo estado de candidato já existente, sem criação ou novo sorteio. |
| `QuestOfferMenu` | Menu de decisão | Gera somente as opções de aceitar ou recusar uma oferta após o diálogo externo. Não implementa uma linguagem genérica de escolhas ou ramificações. |
| `QuestMenu` | Menu de consulta | Separa quests ativas e concluídas sem exibir quests não iniciadas. Apresenta descrição resolvida, etapa atual, objetivos e contadores, recompensas materiais derivadas e, quando elegível, a repetição manual da conclusão por meio de `QuestProgressCoordinator`. Não avança o relógio nem altera progresso durante a navegação. |
| `BestiaryMenu` | Menu de consulta | Integra o Bestiário ao `Diário`, pagina todas as definições em ordem de catálogo, oculta registros não descobertos e apresenta nome, rank, personalidade, especialidades e fraqueza opcionais, descrição, altura e peso dos inimigos analisados. Consulta `EnemyCatalog` e `BestiaryStateCatalog` sem avançar o relógio nem descobrir entradas durante a navegação. |
| `QuestProgressCoordinator` | Coordenador de interface | Recebe eventos confirmados ou pedidos de repetição, delega toda regra e mutação a `QuestProgressService` e encaminha o resultado para a apresentação comum ou específica de batalha. Não armazena progresso nem reimplementa regras de quest. |
| `QuestProgressPresenter` | Apresentador de resultados | Traduz `QuestProgressResult`, atualizações e falhas em avisos agrupados, confirmação de início e telas dedicadas de conclusão, efeitos ou bloqueios. Progresso simples produzido em batalha pode ser anexado ao resultado da luta sem uma nova pausa. Não calcula progresso nem aplica recompensas. |
| `QuestRewardSummaryFormatter` | Formatador de interface | Deriva descrições visíveis de recompensas diretamente de `QuestRewardDefinition`, `ItemCatalog` e `GameCurrency`, evitando texto duplicado na quest. |
| `CraftingMenu` | Menu de fabricação | Exibe o crafter selecionado e suas receitas ordenadas, apresenta narrativa, item produzido e quantidades necessárias e possuídas, solicita confirmação e traduz `CraftingResult` em feedback. Resolve dados por catálogo, mas delega toda mutação a `CraftingService`. |
| `InnMenu` | Menu de serviço urbano | Exibe os dados de `InnDefinition`, controla confirmação e pagamento da estadia e solicita a restauração centralizada em `Party.fullyRestoreMembers()`. Mantém o jogador no INN até um retorno explícito e apresenta o resultado das ações dentro da tela atual. |
| `ScenarioBranchMenu` | Menu | Recebe as bifurcações disponíveis na posição atual, exibe seus nomes e descrições e devolve a `ScenarioBranchDefinition` escolhida. Não altera o cenário nem o estado da exploração. |
| `RouteMenu` | Menu | Recebe as rotas adjacentes atualmente disponíveis e os IDs de destino que devem exibir `[!]`. Exibe nomes e descrições e devolve a `TravelRouteDefinition` escolhida. Não consulta quests, resolve o destino nem aplica a posição de chegada; essas responsabilidades pertencem a `World`. |
| `CityTransportMenu` | Menu de transporte pago | Recebe os destinos urbanos descobertos e configurados, além dos IDs de destino que devem exibir `[!]`. Exibe preços e saldo por meio de `GameCurrency`, valida seleção e saldo, solicita confirmação, realiza o pagamento e devolve a `CityTransportDefinition` escolhida. Não consulta quests nem altera o cenário. |
| `InventoryMenu` | Menu geral de inventário | Exibe a moeda compartilhada e todas as categorias em ordem determinística. Listas são paginadas e ações dependem do tipo: recuperação pode ser usada, equipamentos abrem `EquipmentMenu` e pergaminhos exibem a habilidade vinculada e permitem aprendê-la fora de batalha. Após um pergaminho ser consumido, atualiza a categoria sem fechar o inventário. |
| `EquipmentMenu` | Menu compartilhado de equipamentos | Permite equipar a partir de um item do inventário ou administrar os três slots de um personagem. Filtra escolhas incompatíveis, mostra uma comparação do que realmente muda, pede confirmação e delega a transferência a `EquipmentService`. |
| `EquipmentDetailFormatter` | Formatador de interface | Produz fichas de arma, armadura e berloque e comparações alinhadas entre equipamento atual e novo. Usa `Poder` como nome visível do poder-base da arma. |
| `BattleItemMenu` | Menu de itens de batalha | Filtra diretamente os consumíveis utilizáveis em batalha, apresenta uma lista paginada e abre seus detalhes. Devolve sucesso apenas quando o uso produz algum efeito, permitindo que `Battle` decida se a ação do personagem foi consumida. |
| `RecoveryItemUseMenu` | Fluxo de uso | Centraliza a seleção de alvos, a aplicação de efeitos individuais ou coletivos, a remoção do item somente após um resultado efetivo e a apresentação dos valores recuperados. É compartilhado por `InventoryMenu` e `BattleItemMenu`. |
| `SkillScrollUseMenu` | Fluxo de aprendizagem | Localiza o único integrante cuja classe é compatível, apresenta impedimentos, coordena seleção e confirmação de substituição e delega aprendizagem e consumo a `SkillScrollService`. Não oferece a ação quando o grupo não possui aprendiz compatível. |
| `ItemMenuFormatter` | Formatador de interface | Centraliza a largura das colunas, o truncamento seguro de nomes longos e o resumo dos componentes de recuperação usados pelas telas de inventário. |
| `PartyStatusMenu` | Menu de consulta | Exibe cada integrante em duas linhas, preservando o nome completo e mostrando símbolo da classe, nível, recursos, moeda compartilhada, M.EXP temporário e M.GOLD total. Permite selecionar um integrante para abrir `CharacterStatusMenu`. |
| `CharacterStatusMenu` | Tela de consulta | Mostra nome completo, símbolo e nome da classe, recursos, atributos efetivos, nível, EXP e M.EXP total do personagem. Exibe as seis posições de habilidade, incluindo vagas vazias não selecionáveis; a ficha de uma habilidade aprendida usa wrapping compartilhado, páginas fixas de 17 linhas e espaço reservado para feedback. Abre os equipamentos por meio dos menus compartilhados. |
| `AllyTargetMenu` | Menu de alvo | Exibe aliados vivos ou derrotados conforme o contexto e devolve o `PlayableCharacter` escolhido. Um aliado vivo pode ser selecionado mesmo com HP cheio porque componentes compostos ainda podem purificá-lo ou fortalecê-lo. |
| `EnemyTargetMenu` | Menu de alvo | Exibe apenas inimigos vivos de `EnemyBattleGroup` e devolve o `EnemyBattleSlot` escolhido. É usado por ataques, habilidades e análise. |
| `CombatEffectFormatter` | Formatador de interface | Produz a apresentação compartilhada dos estágios ativos e da Distorção Ancestral sem mover regras de duração ou atributo para a interface. |
| `PlayableClassDetailFormatter` | Formatador de interface | Produz o resumo compartilhado de uma classe com símbolo, nome, peso de armadura e especializações, além da apresentação completa com descrição. Exibe o atributo primário com `↑↑` e o secundário com `↑`, consulta `PlayableClassDefinition` e não calcula crescimento. |
| `ClassPathQuestionnaireMenu` | Menu de questionário | Resolve previamente todos os textos narrativos e apresenta as sete perguntas sequencialmente. Repete somente a pergunta atual após entrada inválida e devolve as respostas canônicas sem calcular pontuação. |
| `ClassPathTieMenu` | Menu de desempate | Apresenta uma única revelação temporizada para as duas ou três classes empatadas e permite selecionar uma delas ou rejeitar todas. Entrada inválida repete apenas o prompt, sem reproduzir a animação. |
| `ClassPathRecommendationMenu` | Menu de recomendação | Apresenta uma única revelação temporizada da marca escolhida e depois mantém uma tela compacta para consultar a descrição, abraçar o caminho ou recusar o chamado. Não altera a classe do protagonista. |
| `ClassPathSelectionMenu` | Menu de seleção livre | Lista os sete caminhos finais em ordem de catálogo, permite consultar suas apresentações e devolve a classe escolhida. O Flagelo iniciante nunca aparece como opção. |
| `ClassPathConfirmationMenu` | Menu de confirmação | Exibe a apresentação completa do caminho escolhido e permite apenas confirmar ou escolher novamente. Não oferece cancelamento da cerimônia. |
| `ClassPathChoiceCoordinator` | Coordenador de interface | Encadeia questionário, cálculo, desempate explícito, revelação, aceitação ou seleção livre e confirmação. Devolve uma definição final de classe sem aplicar a transição nem alterar estado de campanha. |
| `ClassPathTransitionFailurePresenter` | Apresentador de falhas | Exibe causas esperadas que impedem a cerimônia, incluindo nível insuficiente, classe inelegível, repetição e capacidade de inventário, sem recalcular regras de domínio. |
| `AllyRecruitmentMenu` | Menu de confirmação obrigatória | Apresenta o nome persistente e a classe sorteada do aliado preparado e oferece somente `Aceitar sua companhia`. Não executa inserção nem altera candidato ou quest. |
| `SkillDetailFormatter` | Formatador de interface | Produz a ficha compartilhada de uma habilidade com requisito de nível, tipo principal, poder, alvo, custo e descrição narrativa. Deriva os campos técnicos do primeiro componente efetivo de `PRIMARY` e é reutilizado pelo status, pela confirmação em batalha e pelos pergaminhos. |
| `RewardModifierFormatter` | Formatador de interface | Produz a notação compacta compartilhada de `M.EXP` e `M.GOLD`, combinando fontes permanentes e temporárias para apresentação sem calcular ou alterar recompensas. |
| `EpicEncounterMenu` | Menu de confirmação | Recebe uma lista de uma a três `EnemyDefinition`, apresenta nomes e descrições do encontro épico e pergunta se o jogador deseja enfrentá-lo. Devolve apenas a decisão; `World` inicia a batalha. |
| `BattleTurnOrderScreen` | Tela | Mostra o participante atual e uma prévia das próximas ações usando `BattleTurnOrder`. Não gera nem altera a fila. |
| `BattleRewardScreen` | Tela | Exibe um resumo compacto de `BattleRewards`: modificadores globais usados, EXP global, GOLD, itens consolidados e progressão de nível. Não lista cada EXP individual, nem calcula ou concede recompensas. |
| `ShopMenu` | Menu comercial | Interface compartilhada por lojas urbanas e mercadores de exploração. Exibe ofertas, categorias vendáveis, confirmações, paginação, preços, estoque e feedback das transações. |

---

## Pacote `rpg.world`

| Classe | Categoria | Finalidade e relações principais |
|---|---|---|
| `World` | Coordenador principal do jogo | Mantém o cenário atual, a sessão de exploração e o loop do mundo. Coordena exploração, tempo, lojas, crafting, rotas, transporte, batalhas, resgate e navegação urbana. Em dungeons, aplica o nível fixo do boss narrativo e sorteia um nível compartilhado para grupos recorrentes e épicos antes de criar seus `EnemySpawn`. Filtra conteúdo narrativo por requisitos, resolve interações de NPC, introduz candidatos encontrados em diálogos, deriva marcadores, emite progresso confirmado e despacha conclusões comuns ou a transação composta da primeira cerimônia e recrutamento. Não implementa internamente as regras de candidato, aliado, classe ou quest. |
| `WorldProgress` | Estado mutável | Registra os IDs dos cenários descobertos na execução atual, valida referências por meio de `ScenarioCatalog` e expõe uma cópia não modificável do progresso. Permanece separado de `TravelProgress`, pois descoberta e desbloqueio de rotas são conceitos independentes. |
| `WorldClock` | Estado temporal mutável | Armazena horas fictícias decorridas, calcula dia e hora derivados e valida avanços contra overflow. `World` o avança somente após batalha concluída e coordena reposição global quando o avanço atravessa meia-noite. |

---

## Pacote `rpg.world.bestiary`

| Classe | Categoria | Finalidade e relações principais |
|---|---|---|
| `BestiaryStateCatalog` | Estado mutável | Mantém em ordem de descoberta os IDs canônicos dos inimigos analisados na campanha. Descobertas repetidas são idempotentes; a interface combina esses IDs com `EnemyCatalog`, e a persistência os transporta por `BestiarySnapshot`. |

---

## Pacote `rpg.world.crafting`

| Classe                     | Categoria             | Finalidade e relações principais                                                                                                                                                                                                   |
| -------------------------- | --------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `CraftingMaterial`         | Requisito imutável    | Representa um item e sua quantidade positiva dentro de uma receita. Apesar do nome histórico preservado da contribuição, aceita qualquer `ItemCategory` e não se limita a itens `MATERIAL`.                                        |
| `CraftingRecipeDefinition` | Definição imutável    | Armazena ID estável, item produzido, nome, descrição narrativa e lista ordenada de requisitos. Rejeita receita sem ingredientes e IDs de ingrediente repetidos. A quantidade de saída é sempre uma unidade.                        |
| `CraftingRecipeLoader`     | Carregamento de dados | Lê `crafting_recipes.txt`, interpreta os pares variáveis de item e quantidade, converte quebras literais `\n`, valida todas as referências por meio de `ItemDefinitionCatalog` e registra as receitas no catálogo.                 |
| `CraftingRecipeCatalog`    | Catálogo              | Registra e localiza receitas por ID estável, rejeita duplicidades e preserva a ordem externa de declaração.                                                                                                                        |
| `CrafterDefinition`        | Definição imutável    | Armazena ID, cidade, nome, descrição e lista ordenada de receitas de um NPC de fabricação. Permite representar diferentes profissões sem criar definições específicas para ferreiro, alquimista ou cozinheiro.                     |
| `CrafterLoader`            | Carregamento de dados | Lê `crafters.txt`, exige referência a um cenário `CITY`, valida cada receita por meio de `CraftingRecipeCatalog` e registra o NPC. Não exige exclusividade por cidade.                                                             |
| `CrafterCatalog`           | Catálogo              | Registra crafters por ID e os agrupa por cenário, preservando a ordem. Permite que `World` encontre todos os NPCs de fabricação associados à cidade atual.                                                                         |
| `CraftingResult`           | Resultado imutável    | Informa sucesso, materiais insuficientes ou limite de quantidade do inventário. Quando necessário, transporta todos os requisitos não atendidos para que a interface não precise deduzir novamente a regra.                        |
| `CraftingService`          | Serviço transacional  | Valida ingredientes e capacidade, consome requisitos e adiciona uma unidade do item produzido no inventário compartilhado da `Party`. Rejeições não alteram o inventário e falhas inesperadas durante a aplicação ativam rollback. |

### Fluxo essencial

```text
itens + receitas + crafters
    -> catálogos validados
    -> World seleciona o NPC da cidade
    -> CraftingMenu seleciona e confirma a receita
    -> CraftingService altera o inventário
    -> CraftingResult descreve o resultado
```

Não existe um objeto mutável `Crafter` porque o NPC atualmente não possui estado próprio durante a execução. `CrafterDefinition` é suficiente até que uma futura mecânica realmente exija estado persistente por NPC.

---

## Pacote `rpg.world.encounters`

| Classe | Categoria | Finalidade e relações principais |
|---|---|---|
| `EncounterEntry` | Definição de disponibilidade | Representa uma linha de `encounters.txt`: cenário, inimigo, intervalos de posição e nível, peso, limite de cópias e restrição de área. Não representa uma batalha já criada. |
| `EncounterLoader` | Carregamento de dados | Lê `encounters.txt`, converte os campos, valida referências por meio de `EnemyCatalog`, verifica o crescimento no nível máximo configurado e registra `EncounterEntry` em `EncounterCatalog`. |
| `EncounterCatalog` | Catálogo | Agrupa e fornece as entradas cadastradas para cada cenário. É consultado por `EncounterGenerator` durante uma batalha aleatória. |
| `EncounterGenerator` | Gerador aleatório | Filtra entradas por cenário, posição e tipo de área e monta uma lista ponderada de `EnemySpawn`. Respeita peso, `maxCopies`, mínimo e máximo de inimigos e o limite de `EnemyBattleGroup`, sorteando o nível de cada cópia independentemente dentro do intervalo inclusivo da entrada. |
| `EnemySpawn` | Solicitação imutável de criação | Associa uma `EnemyDefinition` a um nível positivo já escolhido. Transporta a decisão do encontro até `BattleCoordinator` sem possuir HP, recursos ou estado de combate. |

### Diferença essencial

```text
EncounterEntry     = quem pode aparecer e em quais condições
EncounterCatalog   = onde essas regras estão registradas
EncounterGenerator = quem sorteia o grupo e o nível de cada cópia
EnemySpawn          = definição e nível já escolhidos para uma cópia
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
| `CombatScenario` | Classe-base de execução | Estende `Scenario` com recompensas-base, `ExplorationSettings`, destino preferencial opcional, destino fallback obrigatório e descrição do resgate. É herdada por `Field` e `Dungeon` e fornece a `World` os dados necessários para encontros, recompensas e resgate. |
| `Field` | Cenário de execução | Representa um campo explorável com eventos aleatórios e conclusão após resolver sua posição final. É criado por `ScenarioFactory` a partir das definições comuns e de combate. |
| `Dungeon` | Cenário de execução | Representa uma masmorra explorável e possui `DungeonSettings`. Antes da primeira conclusão termina com o boss narrativo; depois usa o boss recorrente quando disponível ou termina sem boss durante o cooldown. Épicos continuam opcionais e independentes do boss final. |
| `DungeonSettings` | Configuração imutável de execução | Guarda grupos narrativo, recorrente e épico; nível fixo narrativo; intervalos de nível recorrente e épico; cooldowns; posição mínima do épico; e restrição de área. Recebe os dados validados de `DungeonDefinition` durante a criação do cenário. |
| `DungeonProgress` | Estado mutável de campanha | Registra, para cada dungeon, conclusão narrativa e horas absolutas de disponibilidade dos bosses recorrentes e épicos. Permanece separado das definições e da sessão transitória de exploração. |
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
| `CombatScenarioLoader` | Carregamento de dados | Lê os 16 campos de `combat_scenarios.txt`, valida se o cenário existe e não é uma cidade, monta `ExplorationSettings` e interpreta os dados de resgate. Exige destinos urbanos com INN, rejeita destinos preferencial e fallback iguais e relata arquivo, linha e ID quando possível. Produz definições registradas no catálogo correspondente. |
| `CombatScenarioDefinition` | Definição imutável | Armazena os dados externos exclusivos de campos e dungeons: recompensas-base, `ExplorationSettings`, destino preferencial opcional, fallback obrigatório e descrição do resgate. Valida a presença e a diferença dos destinos antes de complementar `ScenarioDefinition`, sem duplicar os dados comuns. |
| `CombatScenarioDefinitionCatalog` | Catálogo de definições | Registra e localiza `CombatScenarioDefinition` por ID de cenário. É usado por `DungeonLoader` para validar dados e por `ScenarioFactory` para criar `Field` e `Dungeon`. |

### Definições específicas de dungeon

| Classe | Categoria | Finalidade e relações principais |
|---|---|---|
| `DungeonLoader` | Carregamento de dados | Lê `dungeons.txt`, valida cenário e configuração de combate, resolve inimigos, exige ranks e grupos corretos, valida níveis, crescimento máximo, cooldowns, posição e área do épico e produz `DungeonDefinition`. |
| `DungeonDefinition` | Definição imutável | Armazena grupos narrativo, recorrente e épico; nível fixo narrativo; intervalos de nível recorrente e épico; cooldowns; posição mínima; e restrição de área. Permite que grupos narrativo e recorrente reutilizem IDs, mas mantém épicos separados dos dois. |
| `DungeonDefinitionCatalog` | Catálogo de definições | Registra e localiza `DungeonDefinition` por ID de cenário. É consultado por `ScenarioFactory` ao criar cada dungeon de execução. |
| `DungeonBossGateDefinition` | Definição imutável | Associa uma masmorra a um conjunto de requisitos, diálogo padrão de bloqueio e cenário de retirada. |
| `DungeonBossGateCatalog` | Catálogo | Consulta a condição de acesso configurada para cada masmorra. |
| `DungeonBossGateLoader` | Carregamento de dados | Lê `dungeon_boss_gates.txt` e valida masmorra, requisitos, diálogo e destino. |
| `CombatScenarioDialogueDefinition` | Definição de relação | Associa cenário, trigger (`BOSS_GATED`, `BOSS_NARRATIVE` ou `BOSS_RECURRING`), contexto opcional de quest e diálogo canônico. |
| `CombatScenarioDialogueCatalog` | Catálogo de relações | Resolve diálogo contextual por cenário, trigger, quest e etapa, com possibilidade de diálogo geral. |
| `CombatScenarioDialogueLoader` | Carregamento de dados | Lê `combat_scenario_dialogues.txt` e valida cenários, gatilhos, contextos de quest e diálogos. |
| `DungeonBossAccessResolver` | Resolvedor de acesso | Avalia o bloqueio do chefe e seleciona a reação contextual ou padrão; também fornece os diálogos configurados antes dos encontros. |

### Criação e registro dos cenários de execução

| Classe | Categoria | Finalidade e relações principais |
|---|---|---|
| `ScenarioFactory` | Fábrica | Recebe `ScenarioDefinition` e consulta os catálogos especializados. Conforme `ScenarioType`, cria `City`, `Field` ou `Dungeon`, combinando dados comuns, dados de combate e dados específicos da dungeon. Ao criar `Dungeon`, propaga grupos, níveis, cooldowns e restrições de `DungeonDefinition` para `DungeonSettings`; ao criar `Field` e `Dungeon`, também propaga os destinos e a descrição de resgate definidos em `CombatScenarioDefinition`. |
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
| `ScenarioBranchLoader` | Carregamento de dados | Lê os sete campos de `scenario_branches.txt`, inclusive `initiallyUnlocked`; valida cenários, posição, estado inicial e textos. Rejeita bifurcações na posição final de masmorras. |
| `ScenarioBranchDefinition` | Definição imutável | Representa uma saída em uma posição de exploração: ID, origem, posição, destino, estado inicial de desbloqueio, nome e descrição. Não guarda o desbloqueio mutável. |
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

Este pacote representa os dados fixos dos INNs urbanos. Ele não controla entrada do jogador nem altera diretamente a `Party`: `World` coordena o acesso e o resgate, `InnMenu` controla a interface e o pagamento da estadia, e `Party` executa a restauração coletiva.

| Classe | Categoria | Finalidade e relações principais |
|---|---|---|
| `InnLoader` | Carregamento de dados | Lê `inns.txt`, interpreta quebras literais `\n`, valida os cinco campos, resolve o cenário associado e exige o tipo `CITY`. Depois do carregamento, rejeita a inicialização quando qualquer cidade registrada não possui INN. |
| `InnCatalog` | Catálogo de definições | Registra INNs por ID estável e por ID da cidade, rejeita IDs duplicados e impede mais de um INN no mesmo cenário urbano. Em conjunto com a validação de cobertura de `InnLoader`, garante exatamente um INN para cada cidade. |
| `InnDefinition` | Definição imutável | Armazena ID estável, ID do cenário urbano, nome, descrição e preço fixo da estadia para todo o grupo. Rejeita textos vazios e preços menores ou iguais a zero. |

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

`InnCatalog` informa a `World` qual INN está associado à cidade atual. `CityLocationMenu` cuida apenas da navegação pelo nome do local. Depois da seleção, `InnMenu` apresenta os detalhes, executa confirmação e pagamento e solicita a restauração coletiva a `Party.fullyRestoreMembers()`.

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

As ofertas ativas são geradas no início de uma nova campanha, persistidas no save e reutilizadas em acessos posteriores à loja ou ao mercador. Uma oferta esgotada não é substituída imediatamente por outra entrada de `shop_stock.txt`. O reabastecimento global ocorre nos limites de meia-noite do relógio ficcional e gera um novo catálogo de estados de loja de forma atômica.

## Pacote `rpg.world.travel`

| Classe | Categoria | Finalidade e relações principais |
|---|---|---|
| `TravelRouteLoader` | Carregamento de dados | Lê `travel_routes.txt`, interpreta ID, origem, destino, estado inicial, nome, descrição e posição de chegada, converte quebras literais `\n` e valida as referências. Exige posição vazia para cidades e uma posição válida para campos e masmorras. |
| `TravelRouteDefinition` | Definição imutável | Representa uma rota direcionada carregada do arquivo: ID, origem, destino, desbloqueio inicial, textos e posição opcional de chegada na exploração. Não guarda o desbloqueio mutável da campanha. |
| `TravelCatalog` | Catálogo | Registra as definições de rota, rejeita IDs duplicados e fornece as rotas que partem de um cenário. É consultado por `TravelProgress` e pelo fluxo de viagem. Cada direção precisa de uma definição própria. |
| `TravelProgress` | Estado mutável | Mantém os IDs desbloqueados de rotas direcionais e bifurcações. Inicializa ambos conforme `initiallyUnlocked` e expõe apenas passagens disponíveis; efeitos de quest podem liberar seus IDs. |

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
| Validação de nome jogável | `CharacterNameRules` | entrada ou arquivo externo → normalização → validação → nome canônico |
| Nomes aleatórios | `CharacterNameSelector` | `character_names.txt` → loader → catálogo → exclusões → sorteio uniforme |
| Criação inicial | `CharacterCreationMenu` | tela inicial → nome manual/aleatório → confirmação → `Main` → protagonista → `Party` |
| Definições de caminhos finais | `ClassPathCatalog` | `class_paths.txt` → `ClassPathLoader` → classe final + arma e armadura introdutórias |
| Questionário de caminhos | `ClassPathQuestionnaireCatalog` | perguntas + respostas externas → seleção sequencial → `ClassPathRecommendationService` |
| Escolha final de caminho | `ClassPathChoiceCoordinator` | questionário → resultado direto ou desempate → recomendação ou seleção livre → classe confirmada |
| Transição do protagonista | `ClassPathTransitionService` | classe confirmada + estado atual → preparação completa → revalidação → aplicação atômica |
| Party de desenvolvimento | `DevelopmentPartyFactory` | `--dev-party` → configuração isolada → progressão → recursos completos → `Party` |
| Fluxo geral do jogo | `World` | cenário → exploração/rotas/transporte → batalha |
| Dados de um inimigo | `EnemyDefinition` | `EnemyLoader` → `EnemyCatalog` → `EnemyDefinition` |
| Dados de uma classe jogável | `PlayableClassDefinition` | `playable_classes.txt` → `PlayableClassLoader` → `PlayableClassCatalog` |
| Apresentação de uma classe | `PlayableClassDetailFormatter` | `PlayableClassDefinition` → símbolo, nome, armadura, especializações e descrição → criação/cerimônia/status |
| Crescimento natural de um personagem | `CharacterGrowthCalculator` | definição de classe + nível → `CharacterStats` → `PlayableCharacter` |
| Inimigo em batalha | `Enemy` | `EnemyDefinition` + nível + loadout + drops → `BattleCoordinator` → `Enemy` |
| Sorteio de inimigos | `EncounterGenerator` | `EncounterCatalog` → filtros/pesos → definições |
| Execução da luta | `Battle` | ordem de turnos → ações → `BattleResult` |
| Definição de uma habilidade jogável | `SkillDefinition` | `skills.txt` + `skill_effects.txt` → `SkillLoader` → `SkillCatalog` |
| Definição de uma habilidade inimiga | `EnemySkillDefinition` | `enemy_skills.txt` + `skill_effects.txt` → `SkillLoader` → `EnemySkillCatalog` |
| Skills disponíveis para uma espécie | `EnemySkillsetCatalog` | inimigo + nível → vínculos elegíveis → teto de `EnemyRank` → loadout da instância |
| Especialização de um pergaminho | `SkillScrollDefinition` | item + habilidade + classe → `SkillScrollLoader` → `SkillScrollDefinitionCatalog` |
| Aprender por pergaminho | `SkillScrollService` | inventário → `SkillScrollUseMenu` → `PlayableCharacter` → consumo condicional |
| Uma utilização de habilidade | `SkillUseState` | `Battle` → contexto e alvos → `SkillExecutor` → resultados → `Battle` |
| Definição de um equipamento | `EquipmentDefinition` | `items.txt` + arquivo especializado → `EquipmentLoader` → `EquipmentCatalog` |
| Equipar, trocar ou remover item | `EquipmentService` | `Inventory` ↔ `PlayableCharacter` → `CharacterEquipment` |
| Equipamento na interface | `EquipmentMenu` | inventário ou status → filtro/seleção → comparação → confirmação |
| Fórmulas de combate | classes `Calculator` | executor ou batalha → calculador especializado → operação em `GameCharacter` |
| Seleção de alvo inimiga | `EnemyTargetSelector` | ação escolhida + perfil + membros vivos → aquisição ou manutenção do alvo individual |
| Efeitos temporários | `CombatEffectState` | componente aplicado → estados por atributo → atributo efetivo/formatter |
| Preparação e resultado da luta | `BattleCoordinator` | definições → grupo → `Battle` → recompensas → eventos de análise/vitória/drop → `QuestProgressCoordinator` |
| Recompensas de batalha | `BattleRewardService` | inimigos derrotados + estado de modificadores → `RewardCalculator` → `BattleRewards` |
| Modificadores de recompensa | `BattleRewardModifiers` | estado temporário + berloques equipados → snapshot imutável → cálculo/apresentação |
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
| Definição completa de quest | `QuestDefinition` | três arquivos externos → `QuestLoader` → passos e objetivos → `QuestCatalog` |
| Estado de uma quest | `QuestProgress` | catálogo imutável → ativação → passo atual → contadores → conclusão |
| Integração de progresso | `QuestProgressService` | operação confirmada → evento ou interação → validação → resultado ordenado |
| Coordenação visual de progresso | `QuestProgressCoordinator` | eventos confirmados ou repetição manual → `QuestProgressService` → apresentação contextual |
| Diário de quests | `QuestMenu` | progresso + definições e recompensas canônicas → consulta ativa/concluída sem mutação temporal |
| Bestiário | `BestiaryMenu` | análise em batalha → `BestiaryStateCatalog` → definições reveladas no `Diário` |
| Eventos de mundo | `World` | mudança de cenário ou vitória de boss concluída → evento canônico → `QuestProgressCoordinator` |
| Recompensas de quest | `QuestRewardCatalog` | `quest_rewards.txt` → validação → plano de conclusão → `Party` |
| Efeitos permanentes | `QuestEffectCatalog` | `quest_effects.txt` → trigger → flags ou rotas |
| Flags permanentes | `CampaignFlags` | `flags.txt` → `FlagCatalog` → IDs definidos na campanha |
| Requisitos reutilizáveis | `RequirementEvaluator` | conjunto externo + estado atual → resultado puro |
| Local narrativo | `NarrativeLocationDefinition` | cidade + requisito → `World` → `CityLocationMenu` |
| NPC narrativo | `NpcDefinition` | identidade + posicionamento visível → `NarrativeLocationMenu` |
| Diálogo linear | `DialoguePresenter` | definição externa → locutores/placeholders resolvidos → falas por Enter |
| Interação de quest por NPC | `QuestNpcInteractionResolver` | NPC + progresso + requisitos → títulos disponíveis → submenu |
| Marcador de quest | `World` | interação acionável ou destino direto pendente → NPC/local, ação principal ou opção imediata de viagem |
| Escolha apresentada ao jogador | classes `Menu` | recebem opções válidas e devolvem uma escolha |
| Informação apenas exibida | classes `Screen` | recebem resultados prontos e os apresentam |
| Definições de lojas | `ShopDefinition` | `shops.txt` → `ShopLoader` → `ShopCatalog` |
| Possíveis ofertas | `ShopStockEntry` | `shop_stock.txt` → `ShopStockLoader` → `ShopStockCatalog` |
| Estado comercial atual | `ShopStateCatalog` | `ShopStateCatalog` → `ShopState` → `ShopOfferState` |
| Interface comercial | `ShopMenu` | `World` → `ShopMenu` → `ShopTransactionService` |
| Transações comerciais | `ShopTransactionService` | validação → `Party`, `Inventory` e `ShopOfferState` |
| Introdução de candidato | `RecruitmentCandidateIntroductionService` | candidato declarado + nomes reservados → nome persistente único → estado mínimo |
| Preparação genérica de aliado | `AllyRecruitmentService` | candidato + party + exclusões → classe elegível → aliado completo → plano |
| Primeiro recrutamento | `ClassPathRecruitmentService` | escolha do protagonista + transição + quest + aliado → revalidação → commit composto |
| Confirmação do aliado | `AllyRecruitmentMenu` | aliado preparado → `Aceitar sua companhia` → retorno ao coordenador |

## Regra prática para novas classes

Ao criar ou alterar uma classe, faça estas perguntas:

1. Ela está carregando dados, armazenando dados, executando regras, coordenando fluxos ou apenas exibindo informações?
2. O estado dela é uma definição imutável ou muda durante a execução?
3. Ela realmente precisa conhecer todas as classes que importa?
4. Um menu está tomando uma decisão que deveria pertencer a `World`, `Battle` ou a um serviço?
5. Um coordenador está calculando uma regra que poderia ser isolada em um serviço?
6. Um catálogo está guardando objetos de definição ou objetos mutáveis de execução?
7. Se ela especializa um item, a identidade comum continua em `ItemDefinition` e o comportamento específico está ligado pelo mesmo ID estável?
8. Uma definição de quest, flag, requisito, diálogo ou NPC está sendo confundida com o estado mutável da campanha?
9. Uma disponibilidade ou marcador derivado está sendo armazenado sem necessidade?
10. Uma classe de interface está tentando aplicar recompensas, efeitos ou progresso diretamente?

Se a resposta sobre a responsabilidade não couber em uma frase clara, a classe pode estar acumulando funções demais.
