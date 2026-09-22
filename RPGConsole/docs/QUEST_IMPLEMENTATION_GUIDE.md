# RPGConsole — Guia de criação de quests (V6)

Este guia é para quem escreve conteúdo da campanha. As quests são lineares e usam arquivos `.txt` separados por ponto e vírgula. Você pode acrescentar quests usando os tipos já implementados sem editar Java. Para a lista completa de colunas, limites e validações, consulte `DATA_FORMATS.md`.

## Antes de escrever

1. Defina a história, o NPC que oferecerá a quest e como o jogador chegará até ele.
2. Divida a história em etapas consecutivas. Cada etapa precisa de pelo menos um objetivo; a etapa seguinte só recebe progresso depois da anterior.
3. Liste antecipadamente os IDs de NPCs, cenários, inimigos, itens, flags, diálogos e passagens envolvidos. Um nome exibido ao jogador não substitui um ID.
4. Decida quando cada passagem deve ser aberta. Concluir um campo ou uma masmorra não desbloqueia rotas automaticamente.
5. Revise os formatos em `DATA_FORMATS.md` antes de acrescentar colunas ou propor novos tipos.

## Arquivos que trabalham juntos

| Arquivo em `data/` | Para que serve | Colunas, na ordem |
| --- | --- | --- |
| `quests.txt` | Identidade, NPC inicial e disponibilidade | `id;title;description;startingNpcId;availabilityRequirementSetId` |
| `quest_steps.txt` | Etapas em ordem | `questId;step;title;description` |
| `quest_objectives.txt` | Ações exigidas em cada etapa | `id;questId;step;order;objectiveType;targetId;requiredQuantity;text` |
| `quest_dialogues.txt` | Falas de oferta, aceite, recusa, pendência e entrega | `questId;step;context;dialogueId` |
| `dialogues.txt` | Falas ordenadas de cada diálogo | `dialogueId;line;speakerType;speakerId;text` |
| `quest_rewards.txt` | EXP, GOLD e itens da conclusão | `questId;order;rewardType;rewardId;quantity` |
| `quest_effects.txt` | Flags, passagens, itens intermediários e transporte | `questId;step;trigger;order;effectType;targetId;quantity;displayText` |
| `requirements.txt` | Condições reutilizáveis, combinadas com **E** | `setId;order;requirementType;targetId;requiredValue` |
| `flags.txt` | Identidade de consequências permanentes | `id;description` |
| `narrative_locations.txt` | Locais de conversa em cidades | `id;cityId;name;description;requirementSetId` |
| `npcs.txt` | Identidade e conversa padrão de NPCs | `id;name;description;defaultDialogueId` |
| `npc_placements.txt` | Onde e quando o NPC aparece | `npcId;narrativeLocationId;requirementSetId` |
| `quest_recruitments.txt` | Etapa que executa o recrutamento especial | `questId;step;candidateId;allyLevel` |
| `combat_scenario_dialogues.txt` | Diálogos de reação a bloqueio e de chefes | `scenarioId;trigger;questId;step;dialogueId` |
| `dungeon_boss_gates.txt` | Condição de acesso, reação padrão e saída do chefe | `dungeonId;availabilityRequirementSetId;defaultBlockedDialogueId;blockedDestinationScenarioId` |

Use `travel_routes.txt` para passagens direcionais comuns e `scenario_branches.txt` para bifurcações em uma posição de exploração. Uma bifurcação usa `id;sourceScenarioId;position;destinationScenarioId;initiallyUnlocked;displayName;description`. Cada direção de uma rota é uma definição própria.

## Escreva uma quest em ordem

### 1. Cadastre ou reutilize lugares, NPCs e diálogos

Um NPC precisa de um diálogo padrão em `npcs.txt`, mesmo quando também oferece quest. Para aparecer no menu de um local narrativo, precisa de um placement; para apenas falar em outro diálogo, sua definição em `npcs.txt` basta. Um NPC ID tem no máximo um placement. Se a história o leva a outra cidade, use dois IDs com requisitos de visibilidade complementares e apresente ambos como o mesmo personagem.

Os números das linhas de cada diálogo começam em 1 e seguem sem saltos. Tipos de falante: `NPC` (ID canônico de NPC), `ENEMY` (ID canônico de inimigo), `RECRUIT` (ID de candidato), `LEADER` e `NARRATOR` (ID vazio). Uma linha é uma fala; divida uma fala longa para dar ritmo. Escreva `\n` quando quiser uma quebra de linha dentro da mesma fala.

Use `{leaderName}` para o nome do protagonista. `{groupMember2}`, `{groupMember3}` e `{groupMember4}` exigem que a posição correspondente da party já exista. `{recruitName:candidateId}` usa o nome persistente do candidato indicado, sem exigir que ele já seja aliado. Teste o diálogo em um ponto da história em que todos os nomes referidos possam ser resolvidos.

### 2. Defina quando a oferta aparece

Em `quests.txt`, o último campo pode ficar vazio para uma oferta sem requisito. Caso queira exigir outra quest concluída ou um nível mínimo, crie linhas em `requirements.txt` com o mesmo `setId` e informe esse ID na quest. Todos os requisitos do mesmo conjunto devem ser satisfeitos. Tipos: `LEADER_LEVEL_AT_LEAST`, `ITEM_QUANTITY_AT_LEAST`, `QUEST_NOT_STARTED`, `QUEST_ACTIVE`, `QUEST_COMPLETED`, `FLAG_SET` e `FLAG_UNSET`. Não há grupos **OU** nem expressões aninhadas.

Além de a oferta estar disponível, o NPC e seu local precisam estar visíveis por seus próprios requisitos. Aceitar inicia a primeira etapa; recusar não inicia a quest e permite voltar depois.

### 3. Crie etapas e objetivos

Etapas começam em 1 e seguem em sequência. Dentro de cada etapa, `order` dos objetivos também começa em 1 e não salta números. Cada objetivo tem ID único no jogo, quantidade positiva e texto que descreve ao jogador o que fazer.

| Tipo | Alvo | Como progride |
| --- | --- | --- |
| `TALK_TO_NPC` | ID de NPC | Conversa exigida, quantidade 1 |
| `DEFEAT_ENEMY` | ID de inimigo | Inimigos derrotados após vitória |
| `ANALYZE_ENEMY` | ID de inimigo | Análise confirmada, quantidade 1 |
| `OBTAIN_ITEM` | ID de item | Aquisição efetiva; usar o item depois não desfaz o progresso |
| `DELIVER_ITEM` | ID de item | Entrega consome itens durante a interação com o NPC |
| `REACH_SCENARIO` | ID de cenário | Entrada bem-sucedida no cenário, quantidade 1 |
| `COMPLETE_DUNGEON` | ID de masmorra | Conclusão após chefe narrativo ou recorrente, quantidade 1 |
| `WIN_BATTLE` | ID de cenário de combate ou vazio | Vitória no cenário indicado ou em qualquer cenário se vazio |

Uma etapa com `DELIVER_ITEM` precisa de **um** `TALK_TO_NPC` que identifique quem recebe os materiais. Há no máximo um `TALK_TO_NPC` por etapa. Progresso passado não é contado retroativamente: aceite, libere e ordene as etapas de modo que o jogador possa cumprir cada objetivo no momento certo.

### 4. Associe as conversas de quest

Toda quest exige um `OFFER` no passo `0`. `ACCEPT` e `DECLINE` no passo `0` são opcionais. Uma etapa concluída ao conversar com NPC precisa de `COMPLETE` com o número da etapa; uma etapa de entrega pode usar `PENDING` quando faltarem materiais. O diálogo `COMPLETE` aparece depois da validação inicial e antes do consumo dos itens e das recompensas.

`RECRUIT` é um tipo de falante, não uma ordem para recrutar. O recrutamento especial é declarado em `quest_recruitments.txt` para a etapa correspondente e segue a transação da cerimônia. Não acrescente um `party.addMember()` ao conteúdo.

### 5. Distribua recompensas e efeitos

`quest_rewards.txt` concede recompensas materiais na conclusão da quest: `GOLD` e `PARTY_EXP` deixam `rewardId` vazio; `ITEM` usa um item canônico. A ordem começa em 1. A capacidade de recebimento é validada antes da conclusão.

`quest_effects.txt` permite `SET_FLAG`, `UNLOCK_ROUTE`, `GRANT_ITEM` e `TRANSPORT_PARTY`. Gatilhos: `QUEST_START` (step `0`), `STEP_COMPLETE` (step positivo) e `QUEST_COMPLETE` (step `0`). `GRANT_ITEM` usa quantidade positiva e pode entregar um objeto no meio da história; `TRANSPORT_PARTY` pode retirar o grupo de uma masmorra. Ambos não podem usar `QUEST_START`. Os demais efeitos deixam `quantity` vazio.

`UNLOCK_ROUTE` recebe o ID **da rota ou da bifurcação**, não o ID do destino. Desbloqueie explicitamente a volta quando ela também precisar existir. Para impedir que o jogador alcance um chefe antes da história, use um requisito em `dungeon_boss_gates.txt`; o diálogo geral e o destino de retirada vêm desse arquivo. Em `combat_scenario_dialogues.txt`, `BOSS_GATED` pode substituir a reação geral para uma quest e etapa específicas; `BOSS_NARRATIVE` e `BOSS_RECURRING` introduzem a batalha. A apresentação do diálogo por si só não completa objetivos nem altera a quest.

## Exemplo pequeno: pedido em Bree

Este exemplo **ilustra o formato**; escolha IDs livres e ajuste as definições canônicas antes de colocá-lo no jogo. Cada bloco pertence ao arquivo indicado. O NPC já está visível em Bree, inicia a quest e a encerra quando o jogador volta a conversar.

```text
# quests.txt
pedido_da_vila;Um Pedido Simples;Ouça a mensageira de Bree.;mensageira_vila;

# quest_steps.txt
pedido_da_vila;1;A resposta;Retorne à mensageira e diga que pode ajudar.

# quest_objectives.txt
falar_mensageira_pedido;pedido_da_vila;1;1;TALK_TO_NPC;mensageira_vila;1;Fale com a mensageira.

# quest_dialogues.txt
pedido_da_vila;0;OFFER;oferta_pedido_da_vila
pedido_da_vila;1;COMPLETE;conclusao_pedido_da_vila

# dialogues.txt
oferta_pedido_da_vila;1;NPC;mensageira_vila;Você teria um momento para conversar?
conclusao_pedido_da_vila;1;LEADER;;Pode contar comigo.
conclusao_pedido_da_vila;2;NPC;mensageira_vila;Obrigada. Vamos começar.

# quest_rewards.txt
pedido_da_vila;1;PARTY_EXP;;5
```

Para uma quest real, acrescente as definições de NPC, diálogo padrão e placement se ainda não existirem. Com a primeira quest ativa, a opção da interação continua visível; a conclusão exige uma nova interação com a mensageira. Não misture os blocos acima em um só `.txt`.

## Mapa de dependências dos arquivos narrativos

Leia `→` como “o valor desta coluna deve corresponder a um ID definido no arquivo indicado”. O nome mostrado ao jogador não substitui o ID.

- **Cenários, passagens, inimigos e itens** — referências básicas
  - `scenarios.txt`: define `id` de cidade, campo ou masmorra.
  - `travel_routes.txt`: `sourceScenarioId` e `destinationScenarioId` → `scenarios.txt:id`.
  - `scenario_branches.txt`: `sourceScenarioId` e `destinationScenarioId` → `scenarios.txt:id`.
  - `enemies.txt`: define `id` de inimigo.
  - `items.txt`: define `id` de item.

- **Locais e personagens**
  - `narrative_locations.txt`
    - `cityId` → `scenarios.txt:id` de uma cidade.
    - `requirementSetId` → `requirements.txt:setId`, quando preenchido.
  - `npcs.txt`
    - `defaultDialogueId` → `dialogues.txt:dialogueId`.
  - `npc_placements.txt`
    - `npcId` → `npcs.txt:id`.
    - `narrativeLocationId` → `narrative_locations.txt:id`.
    - `requirementSetId` → `requirements.txt:setId`, quando preenchido.

- **Quest e progressão**
  - `quests.txt`
    - `startingNpcId` → `npcs.txt:id`.
    - `availabilityRequirementSetId` → `requirements.txt:setId`, quando preenchido.
  - `quest_steps.txt`
    - `questId` → `quests.txt:id`.
    - `step` identifica uma etapa daquela quest.
  - `quest_objectives.txt`
    - `questId` → `quests.txt:id`.
    - O par `questId + step` → etapa em `quest_steps.txt`.
    - `targetId` → arquivo correspondente ao `objectiveType`; veja a tabela abaixo.
  - `quest_rewards.txt`
    - `questId` → `quests.txt:id`.
    - `rewardId` → `items.txt:id` somente quando `rewardType=ITEM`.
  - `quest_effects.txt`
    - `questId` → `quests.txt:id`.
    - Se `trigger=STEP_COMPLETE`, o par `questId + step` → etapa em `quest_steps.txt`.
    - `targetId` → arquivo correspondente ao `effectType`; veja a tabela abaixo.
  - `quest_recruitments.txt`
    - O par `questId + step` → etapa em `quest_steps.txt`.
    - `candidateId` define a identidade canônica do candidato usada pelos diálogos `RECRUIT`.

- **Requisitos e diálogos**
  - `requirements.txt`
    - `setId` identifica o conjunto que outros arquivos consultam.
    - `targetId` → arquivo correspondente ao `requirementType`; veja a tabela abaixo.
  - `flags.txt`
    - `id` define uma flag que requisitos podem consultar e efeitos podem ativar.
  - `dialogues.txt`
    - `dialogueId` identifica uma sequência de falas.
    - `speakerId` → `npcs.txt:id`, `enemies.txt:id` ou `quest_recruitments.txt:candidateId`, conforme `speakerType`.
  - `quest_dialogues.txt`
    - `questId` → `quests.txt:id`.
    - `step` → `quest_steps.txt:step` da mesma quest quando for positivo; `0` representa oferta, aceite ou recusa.
    - `dialogueId` → `dialogues.txt:dialogueId`.
  - `combat_scenario_dialogues.txt`
    - `scenarioId` → `scenarios.txt:id` de uma masmorra nos gatilhos atuais.
    - `questId + step` → etapa em `quest_steps.txt`, quando o diálogo for específico de uma quest.
    - `dialogueId` → `dialogues.txt:dialogueId`.
  - `dungeon_boss_gates.txt`
    - `dungeonId` → `scenarios.txt:id` de uma masmorra.
    - `availabilityRequirementSetId` → `requirements.txt:setId`.
    - `defaultBlockedDialogueId` → `dialogues.txt:dialogueId`.
    - `blockedDestinationScenarioId` → `scenarios.txt:id`.

### Como resolver campos cujo destino depende do tipo

| Arquivo e coluna | Tipo | Valor correspondente |
| --- | --- | --- |
| `quest_objectives.txt:targetId` | `TALK_TO_NPC` | `npcs.txt:id` |
| ↳ | `DEFEAT_ENEMY`, `ANALYZE_ENEMY` | `enemies.txt:id` |
| ↳ | `OBTAIN_ITEM`, `DELIVER_ITEM` | `items.txt:id` |
| ↳ | `REACH_SCENARIO` | `scenarios.txt:id` |
| ↳ | `COMPLETE_DUNGEON` | `scenarios.txt:id` de uma masmorra |
| ↳ | `WIN_BATTLE` | Vazio para qualquer cenário, ou `scenarios.txt:id` de um cenário de combate |
| `requirements.txt:targetId` | `ITEM_QUANTITY_AT_LEAST` | `items.txt:id` |
| ↳ | `QUEST_NOT_STARTED`, `QUEST_ACTIVE`, `QUEST_COMPLETED` | `quests.txt:id` |
| ↳ | `FLAG_SET`, `FLAG_UNSET` | `flags.txt:id` |
| ↳ | `LEADER_LEVEL_AT_LEAST` | Vazio; o nível fica em `requiredValue` |
| `quest_effects.txt:targetId` | `SET_FLAG` | `flags.txt:id` |
| ↳ | `UNLOCK_ROUTE` | `travel_routes.txt:id` **ou** `scenario_branches.txt:id` |
| ↳ | `GRANT_ITEM` | `items.txt:id` |
| ↳ | `TRANSPORT_PARTY` | `scenarios.txt:id` |
| `dialogues.txt:speakerId` | `NPC` | `npcs.txt:id` |
| ↳ | `ENEMY` | `enemies.txt:id` |
| ↳ | `RECRUIT` | `quest_recruitments.txt:candidateId` |
| ↳ | `LEADER`, `NARRATOR` | Vazio |

**Atenção à direção:** `UNLOCK_ROUTE` usa o ID da passagem, não o ID do cenário de destino. Bree → Entrelinhas e Entrelinhas → Bree são rotas distintas. Já uma bifurcação tem seu próprio ID em `scenario_branches.txt`.

**Atenção às referências circulares:** um NPC pode apontar para seu diálogo padrão, enquanto uma fala desse diálogo aponta de volta para o NPC. Isso é válido; os dois IDs precisam existir e ser consistentes quando o jogo carregar os arquivos.

## Problemas comuns

| Sintoma | Onde olhar |
| --- | --- |
| A quest não aparece | Requisito da oferta, nível, quest anterior e visibilidade do local e do NPC |
| O jogador vê um lugar vazio | Requisitos de placement; um local pode ser visível sem NPC visível |
| A etapa não progride | Etapa atualmente ativa, ID do alvo, cenário da vitória, quantidade e ordem dos objetivos |
| O item já estava na bolsa e `OBTAIN_ITEM` não avançou | Aquisição não é retroativa; use a etapa e o momento de obtenção corretos |
| Falta material na entrega | `DELIVER_ITEM` exige quantidade atual e um `TALK_TO_NPC` recebedor na etapa |
| O diálogo não carrega | Números das linhas, falante e ID canônico, associação necessária e placeholders disponíveis |
| Uma passagem não abriu | Confira ID exato da rota ou bifurcação, direção e gatilho de `UNLOCK_ROUTE` |
| A quest concluída não avançou a próxima | Verifique os requisitos da próxima oferta; concluir uma quest não a aceita automaticamente |
| O save antigo não carrega após mudar conteúdo | Não reutilize IDs para outro significado; consulte `SAVE_STATE.md` para compatibilidade de definições |

## Checklist antes de incluir no projeto

- [ ] O NPC inicial e seu local estão visíveis quando a oferta deve aparecer.
- [ ] Os IDs são únicos e os alvos já estão definidos nos arquivos correspondentes.
- [ ] Cada etapa e cada lista ordenada começa em 1, sem saltos.
- [ ] Cada etapa tem objetivos possíveis naquele momento, inclusive o caminho de volta.
- [ ] `OFFER` e os diálogos `COMPLETE` exigidos estão associados.
- [ ] Flags e efeitos abrem as passagens no momento escolhido pela narrativa.
- [ ] Recompensas e itens intermediários cabem nas regras de capacidade.
- [ ] Um jogo novo consegue iniciar, progredir e concluir a quest; teste uma recusa e uma nova oferta quando houver recusa.
- [ ] Se houver chefes bloqueados ou recrutamento, teste também a situação antes e depois da condição exigida.
- [ ] Salve em um INN e continue quando a mudança afetar estado persistente.

Quest, cenário e loja podem referenciar definições de arquivos diferentes; por isso, o jogo valida as referências ao iniciar. Quando uma linha causar erro, leia o arquivo e o número da linha indicados, corrija o ID, campo ou ordem e execute novamente. Não ajuste Java para contornar um erro de conteúdo.
