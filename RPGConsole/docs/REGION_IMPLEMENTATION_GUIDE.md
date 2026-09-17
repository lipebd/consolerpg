# RPGConsole — Guia de implementação de novas regiões

Este guia explica quais arquivos devem ser atualizados ao adicionar uma nova cidade, campo ou masmorra ao jogo.

O carregamento é orientado por dados: normalmente, uma região nova não exige uma nova classe Java. Ela precisa apenas de um conjunto coerente de registros nos arquivos da pasta `data`.

## 1. Escolha o ID estável

Antes de editar os arquivos, defina um ID único para a região.

Regras:

- Use letras minúsculas e underscores.
- Não use espaços, acentos ou caracteres especiais.
- Use exatamente o mesmo ID em todos os arquivos.
- Não altere o ID depois que outras definições começarem a referenciá-lo.

Exemplo:

```text
Nome exibido: Ruínas de Sanguemar
ID estável: ruinas_sanguemar
```

## 2. Identifique o tipo da região

| Tipo | Constante | Arquivos obrigatórios |
|---|---|---|
| Cidade | `CITY` | `scenarios.txt`, `inns.txt` e `travel_routes.txt` |
| Campo | `FIELD` | `scenarios.txt`, `combat_scenarios.txt`, `encounters.txt` e `travel_routes.txt` |
| Masmorra | `DUNGEON` | `scenarios.txt`, `combat_scenarios.txt`, `dungeons.txt`, `encounters.txt` e `travel_routes.txt` |

Lojas são opcionais. Quando uma região possuir uma loja fixa ou mercador de exploração, também será necessário atualizar `shops.txt` e `shop_stock.txt`.

Se a região introduzir inimigos novos, eles também devem ser cadastrados em `enemies.txt` antes de serem referenciados. Tabelas de drops em `enemy_drops.txt` são opcionais.

## 3. Cadastre inimigos novos, se houver

Arquivo: `data/enemies.txt`

```text
# id;name;maxHp;maxMp;maxSp;attack;defense;magic;resistance;speed;luck;rank;description
```

Exemplo:

```text
marcego;Marcego;60;0;0;10;5;5;5;20;1;COMMON;Monstrinho nadador e agressivo que se assemelha a um animal voador.
```

Ranks aceitos:

- `COMMON`
- `VETERAN`
- `LEADER`
- `BOSS`
- `EPIC`
- `LEGENDARY`

Use `\n` dentro da descrição quando quiser uma quebra de linha na exibição. Não insira `;` nos textos, pois o ponto e vírgula separa os campos.

`maxHp`, ATK, DEF, MAG, RES, VEL e LCK devem ser maiores que zero. `maxMp` e `maxSp` podem ser zero, mas nunca negativos. Quando um atributo é inválido, `EnemyLoader` informa a linha e o ID do inimigo para que o cadastro seja localizado rapidamente.

Os inimigos comuns, veteranos e líderes podem ser usados em `encounters.txt`. Inimigos `BOSS` e `EPIC` de masmorras são associados em `dungeons.txt` e não precisam entrar na tabela de encontros aleatórios.

## 4. Cadastre a região

Arquivo: `data/scenarios.txt`

```text
# id;type;name;description
```

Exemplo:

```text
ruinas_sanguemar;DUNGEON;Ruínas de Sanguemar;Uma profunda depressão no leito oceânico da Praia de Sanguemar.\nCriaturas das profundezas protegem aquilo que permanece oculto em seu interior.
```

O tipo deve ser exatamente `CITY`, `FIELD` ou `DUNGEON`.

Todas as regiões precisam aparecer neste arquivo. Os demais arquivos apenas complementam a definição criada aqui.

### INN obrigatório das cidades

Toda região `CITY` precisa possuir exatamente um INN em `data/inns.txt`.

```text
# id;scenarioId;name;description;price
```

Exemplo:

```text
nova_cidade_inn;nova_cidade;Estalagem da Nova Cidade;Um local seguro para descanso e recuperação.;50
```

Regras:

- `scenarioId` deve referenciar uma região `CITY` existente em `scenarios.txt`.
- O ID, o nome e a descrição não podem estar vazios.
- O preço deve ser um inteiro maior que zero e se aplica à party inteira.
- Uma cidade não pode possuir mais de um INN.
- `InnLoader` rejeita a inicialização quando qualquer cidade registrada não possui um INN.
- Descrições aceitam sequências literais `\n`.

## 5. Configure a exploração da região de combate

Esta etapa vale apenas para `FIELD` e `DUNGEON`.

Arquivo: `data/combat_scenarios.txt`

```text
# scenarioId;baseExperienceReward;baseGoldReward;explorationLength;battleChance;rewardChance;merchantChance;epicEncounterChance;specialAreaChance;minEnemies;maxEnemies;normalAreaMessage;specialAreaMessage;preferredRescueScenarioId;fallbackRescueScenarioId;rescueDescription
```

Exemplo:

```text
ruinas_sanguemar;100;100;11;70;20;0;10;30;2;2;Você avança pela entrada submersa das Ruínas de Sanguemar.;Você desce pelas paredes da fossa e alcança uma região abissal.;deep_valley;entrelugar;A mudança da maré arrastou os feridos para longe das profundezas, onde foram encontrados junto à entrada submersa.
```

Significado dos campos principais:

| Campo | Finalidade |
|---|---|
| `explorationLength` | Quantidade total de posições da exploração |
| `battleChance` | Chance de ocorrer uma batalha comum |
| `rewardChance` | Chance de ocorrer um evento de recompensa |
| `merchantChance` | Chance de ocorrer um encontro com mercador |
| `epicEncounterChance` | Chance de oferecer o encontro épico da masmorra |
| `specialAreaChance` | Chance de a posição usar a área especial |
| `minEnemies` / `maxEnemies` | Tamanho permitido para grupos inimigos |
| `normalAreaMessage` | Texto exibido na área normal |
| `specialAreaMessage` | Texto exibido na área especial |
| `preferredRescueScenarioId` | Cidade preferencial de resgate, usada apenas quando já foi descoberta |
| `fallbackRescueScenarioId` | Cidade obrigatória usada quando o destino preferencial está ausente ou ainda não foi descoberto |
| `rescueDescription` | Narrativa exibida após a derrota completa da party |

Regras importantes:

- Cada chance individual deve estar entre 0 e 100.
- A soma de `battleChance`, `rewardChance`, `merchantChance` e `epicEncounterChance` não pode ultrapassar 100.
- `specialAreaChance` é independente dessa soma.
- `minEnemies` deve ser pelo menos 1.
- `maxEnemies` não pode ser menor que `minEnemies` nem maior que o limite suportado pela batalha, atualmente 3.
- Cidades não recebem uma linha neste arquivo.
- `preferredRescueScenarioId` é opcional e deve manter o campo vazio quando não existir.
- `fallbackRescueScenarioId` é obrigatório e não pode estar vazio.
- Todo destino de resgate deve referenciar uma região `CITY` que possua um INN registrado.
- Os destinos preferencial e fallback devem ser diferentes quando o preferencial estiver presente.
- O destino fallback pode ser usado e descoberto pelo resgate mesmo que ainda não tenha sido visitado.
- Descobrir uma cidade por resgate não desbloqueia automaticamente rotas relacionadas.
- `rescueDescription` é obrigatória, aceita sequências literais `\n` e deve funcionar para qualquer tamanho de party.

## 6. Configure lojas e mercadores, se houver

Esta etapa é opcional para cidades, campos e masmorras.

Uma cidade pode possuir uma ou mais lojas fixas. Um campo ou masmorra pode possuir um ou mais mercadores encontrados por meio do evento de exploração.

### Defina a loja

Arquivo: `data/shops.txt`

```text
# id;scenarioId;accessType;offerSlots;name;description
```

Exemplo de loja urbana:

```text
mercado_nova_cidade;nova_cidade;CITY;3;Mercado da Nova Cidade;Comerciantes locais oferecem provisões aos viajantes.
```

Exemplo de mercador de exploração:

```text
mascate_ruinas;ruinas_sanguemar;EXPLORATION;2;Mascate das Ruínas;Um mercador protegido por uma cobertura improvisada aguarda viajantes.
```

Regras:

- O ID da loja deve ser único e usar lowercase e underscores.
- `scenarioId` deve existir em `scenarios.txt`.
- `CITY` somente pode referenciar um cenário `CITY`.
- `EXPLORATION` somente pode referenciar um cenário `FIELD` ou `DUNGEON`.
- `offerSlots` deve ser maior que zero.
- Uma região pode possuir múltiplas lojas ou mercadores.
- Se `merchantChance` for maior que zero, o cenário precisa possuir ao menos uma loja `EXPLORATION`.
- Uma definição `EXPLORATION` associada a um cenário com `merchantChance` igual a zero permanece inacessível.

### Defina as possíveis ofertas

Arquivo: `data/shop_stock.txt`

```text
# id;shopId;itemId;shopSalePricePercent;stock;weight
```

Exemplo:

```text
ruinas_meruma_regular;mascate_ruinas;meruma;100;5;80
ruinas_meruma_discount;mascate_ruinas;meruma;75;1;20
```

Cada linha representa uma possível oferta comercial, não apenas um item.

Regras:

- O ID da oferta deve ser estável e único.
- A loja precisa existir em `shops.txt`.
- O item precisa existir em `items.txt`.
- O item precisa definir `baseShopSalePrice`.
- Percentual de preço, estoque e peso devem ser maiores que zero.
- Toda loja precisa possuir ao menos uma possível oferta.
- A mesma loja não pode repetir item, percentual e estoque na mesma configuração comercial, mesmo usando outro ID ou peso.
- O mesmo item pode aparecer em ofertas diferentes quando o percentual ou o estoque forem diferentes.
- O preço final calculado deve permanecer positivo e dentro do limite suportado.
- `shopPurchasePrice` não pode superar o menor preço final pelo qual o item é vendido em qualquer loja.

O preço unitário final é calculado com arredondamento half-up:

```text
roundedPrice = floor((baseShopSalePrice × shopSalePricePercent + 50) / 100)
```

### Planeje os pesos

Os pesos são relativos e não precisam somar 100.

Em cada seleção, a chance de uma oferta é:

```text
P(oferta) = pesoDaOferta / somaDosPesosRestantes
```

A seleção ocorre sem reposição. Depois que uma oferta é escolhida, ela é removida do conjunto e os pesos relativos das ofertas restantes são recalculados para o próximo slot.

Exemplo inicial:

| Oferta | Peso | Chance no primeiro slot |
|---|---:|---:|
| Regular | 80 | 80% |
| Desconto | 20 | 20% |

Se outra oferta tivesse peso 100, o total seria 200. Nesse caso, os pesos 80 e 20 representariam respectivamente 40% e 10% no primeiro slot, enquanto a nova oferta representaria 50%.

Multiplicar todos os pesos pelo mesmo valor não altera as probabilidades relativas.

Quando `offerSlots` for igual ou maior que a quantidade de possíveis ofertas, todas serão selecionadas, independentemente dos pesos.

As ofertas ativas são geradas uma vez quando o programa inicia. Ofertas esgotadas não são substituídas por registros não selecionados, e lojas não são reabastecidas durante a execução atual.

## 7. Configure o pool de recompensas

Esta etapa vale para `FIELD` e `DUNGEON` sempre que alguma área configurada puder resolver a exploração como `REWARD`, diretamente ou por fallback.

Arquivo: `data/exploration_rewards.txt`

```text
# scenarioId;rewardType;rewardId;minQuantity;maxQuantity;weight;areaRestriction
```

Exemplos:

```text
verdes_ermos;ITEM;meruma;1;2;60;NORMAL_ONLY
verdes_ermos;ITEM;pelo_lupino;1;3;25;ANY
verdes_ermos;GOLD;;5;15;15;ANY
```

Tipos de recompensa:

- `ITEM`: exige um `rewardId` existente em `items.txt`.
- `GOLD`: exige que `rewardId` permaneça vazio.

Restrições de área:

- `NORMAL_ONLY`: somente na área normal.
- `SPECIAL_ONLY`: somente na área especial.
- `ANY`: nas duas áreas.

Regras importantes:

- `minQuantity` deve ser pelo menos 1.
- `maxQuantity` não pode ser menor que `minQuantity`.
- `weight` deve ser maior que zero.
- Os pesos são relativos e não precisam somar 100.
- Exatamente uma entrada elegível é selecionada em cada evento de recompensa.
- Entradas `ANY` complementam as entradas específicas da área atual.
- Uma área normal exige uma entrada elegível quando `rewardChance` for maior que zero.
- Uma área especial exige uma entrada elegível quando puder chegar a `REWARD` pela chance direta, pelo percentual restante de `NOTHING` ou pela repetição de um mercador já encontrado.
- Em áreas especiais, resultados diretos de `NOTHING` e repetições do evento de mercador são convertidos em `REWARD`.
- Uma rolagem épica indisponível é convertida em batalha regular e não cria uma exigência de recompensa.
- Entradas completamente idênticas não são permitidas.
- O mesmo item pode aparecer em várias entradas quando algum outro campo for diferente.
- Para desativar temporariamente uma recompensa, comente a linha em vez de definir peso zero.
- Estes pools são repetíveis e não devem ser usados para recompensas únicas, narrativas ou de quests.

## 8. Configure a progressão da masmorra

Esta etapa vale apenas para `DUNGEON`.

Arquivo: `data/dungeons.txt`

```text
# scenarioId;bossEnemyIds;recurringBossEnemyIds;recurringBossCooldownHours;epicEnemyIds;epicMinPosition;epicAreaRestriction;epicEnemyCooldownHours
```

Exemplo com batalha dupla de bosses:

```text
ruinas_sanguemar;defleto,refrata;defleto,refrata;4;colossangue;1;ANY;6
```

Campos:

- `scenarioId`: ID da masmorra.
- `bossEnemyIds`: grupo narrativo enfrentado na primeira conclusão.
- `recurringBossEnemyIds`: grupo usado nas reexplorações após o cooldown.
- `recurringBossCooldownHours`: horas fictícias necessárias para o boss recorrente reaparecer.
- `epicEnemyIds`: grupo épico aleatório e opcional para o jogador.
- `epicMinPosition`: primeira posição em que o épico pode ser oferecido.
- `epicAreaRestriction`: permite `NORMAL_ONLY`, `SPECIAL_ONLY` ou `ANY`.
- `epicEnemyCooldownHours`: horas fictícias necessárias para o épico reaparecer depois de uma vitória.

Regras:

- Separe IDs do mesmo grupo com vírgulas, sem espaços.
- Cada grupo deve possuir de 1 a 3 inimigos.
- Todos os IDs precisam existir em `enemies.txt`.
- Os grupos narrativo e recorrente aceitam somente inimigos de rank `BOSS`.
- O grupo épico aceita somente inimigos de rank `EPIC`.
- Os grupos narrativo e recorrente podem reutilizar os mesmos IDs.
- Não repita um ID dentro do mesmo grupo.
- Um inimigo épico não pode pertencer a nenhum dos grupos de boss.
- Os dois cooldowns devem ser inteiros positivos.
- `epicMinPosition` deve ser pelo menos `1` e anterior à posição final.
- `SPECIAL_ONLY` exige uma chance positiva de área especial e ao menos uma posição intermediária.
- Toda masmorra precisa definir `epicEncounterChance` maior que zero em `combat_scenarios.txt`.
- Campos e cidades não recebem uma linha neste arquivo.

Na primeira exploração, a posição final exige a vitória contra o boss narrativo. Essa vitória registra permanentemente a conclusão da masmorra e inicia o primeiro cooldown recorrente.

Nas reexplorações:

- o boss recorrente é obrigatório na posição final quando estiver disponível;
- a exploração termina sem boss final quando ele ainda estiver em cooldown;
- a masmorra continua acessível durante o cooldown;
- somente uma vitória reinicia o cooldown.

O encontro épico permanece aleatório, independente do boss final e opcional para o jogador. Ele pode ser oferecido no máximo uma vez por sessão quando posição, área e cooldown permitirem. Recusar, fugir ou perder não inicia seu cooldown.

## 9. Monte a tabela de encontros

Esta etapa vale apenas para `FIELD` e `DUNGEON`.

Arquivo: `data/encounters.txt`

```text
# scenarioId;enemyId;minPosition;maxPosition;weight;maxCopies;areaRestriction
```

Exemplo:

```text
ruinas_sanguemar;nada_nada;1;9;40;3;NORMAL_ONLY
ruinas_sanguemar;estrelume;2;9;35;2;ANY
ruinas_sanguemar;jorge;10;10;100;1;ANY
```

Restrições de área:

- `NORMAL_ONLY`: somente na área normal.
- `SPECIAL_ONLY`: somente na área especial.
- `ANY`: nas duas áreas.

Regras importantes:

- `weight` é um peso relativo entre candidatos elegíveis, não uma porcentagem direta.
- Os pesos não precisam somar 100.
- `maxCopies` limita quantas cópias daquele inimigo podem aparecer no mesmo grupo.
- As faixas de posição devem existir dentro da exploração configurada.
- Em masmorras, reserve a última posição para o boss. Portanto, encontros aleatórios devem terminar no máximo em `explorationLength - 1`.
- Se uma posição exigir um grupo maior que 1, deve haver candidatos e cópias suficientes para o gerador formar esse grupo.
- Um líder garantido pode ocupar sozinho a penúltima posição quando essa for a intenção do cenário.
- Chefes e épicos configurados em `dungeons.txt` não devem ser adicionados como encontros comuns.

## 10. Crie as rotas e bifurcações

### Rotas externas

Arquivo: `data/travel_routes.txt`

```text
# id;sourceScenarioId;destinationScenarioId;initiallyUnlocked;displayName;description;destinationExplorationPosition
```

Exemplos:

```text
praia_sanguemar_ruinas_sanguemar;praia_sanguemar;ruinas_sanguemar;false;Descer às Ruínas;As condições da maré revelam uma passagem para as Ruínas de Sanguemar.;1
ruinas_sanguemar_praia_sanguemar;ruinas_sanguemar;praia_sanguemar;true;Retornar à Praia;Você sobe em direção à faixa de areia da Praia de Sanguemar.;1
praia_sanguemar_entrelugar;praia_sanguemar;entrelugar;true;Retornar a Entrelugar;Você deixa a praia e retorna à cidade de Entrelugar.;
```

Regras importantes:

- O ID da rota deve ser único e estável.
- Origem e destino precisam existir em `scenarios.txt`.
- As rotas são direcionais. Uma viagem de ida não cria automaticamente a viagem de volta.
- Crie outra linha quando o caminho puder ser percorrido nos dois sentidos.
- Use `true` quando a rota estiver disponível desde o início.
- Use `false` quando ela depender do progresso do jogo.
- A última coluna deve ficar vazia quando o destino for uma cidade.
- Quando o destino for um campo ou uma masmorra, informe uma posição válida entre `1` e o tamanho da exploração.
- Use a posição `1` para entradas convencionais.
- Uma rota inversa pode entrar depois de uma bifurcação para impedir que o grupo repita imediatamente a mesma escolha.
- Rotas que partem de campos e masmorras continuam sujeitas às ações liberadas pelo estado da exploração.
- Quebras de linha nos textos devem ser representadas por `\n`.

Exemplo de retorno após uma bifurcação:

```text
setegalhos_estrada_leste;setegalhos;verdes_ermos;true;Estrada Leste;A estrada leste deixa o povoado para trás e retorna aos Verdes Ermos.;4
```

Não cadastre uma rota para uma região que ainda não exista em `scenarios.txt`. O carregamento será interrompido com uma mensagem semelhante a:

```text
Scenario definition not found: id_da_regiao
```

### Bifurcações durante a exploração

Arquivo: `data/scenario_branches.txt`

```text
# id;sourceScenarioId;position;destinationScenarioId;displayName;description
```

Exemplo:

```text
verdes_ermos_bifurcacao_setegalhos;verdes_ermos;3;setegalhos;Seguir para Setegalhos;Você encontra uma bifurcação entre os campos.\nUma estrada parcialmente escondida pela vegetação conduz ao Povoado de Setegalhos.
```

Regras importantes:

- O ID da bifurcação deve ser único e estável.
- A origem deve ser um campo ou uma masmorra.
- A posição precisa existir dentro da exploração do cenário de origem.
- A última posição de uma masmorra é reservada para o boss e não pode conter uma bifurcação.
- O destino pode ser uma cidade, um campo ou uma masmorra.
- Mais de um destino pode ser oferecido na mesma posição.
- A ação `Seguir outro caminho` aparece somente na posição exata da bifurcação.
- Avançar sem escolher a bifurcação remove a opção durante o restante daquela sessão.
- A bifurcação reaparece quando uma nova sessão de exploração começa.
- Entrar no destino encerra a sessão de exploração anterior.
- O destino começa normalmente na posição `1`.
- Quando o caminho inverso precisar retornar a outra posição, configure essa chegada em `travel_routes.txt`.
- Quebras de linha nos textos devem ser representadas por `\n`.

## 11. Configure drops, se necessário

Arquivo opcional: `data/enemy_drops.txt`

```text
# enemyId;itemId;dropChance;minQuantity;maxQuantity
```

Exemplo:

```text
lobermo;meruma;50;1;1
```

O inimigo e o item referenciados precisam existir em seus respectivos catálogos.

## 12. Ordem recomendada de implementação

Para evitar referências quebradas, siga esta ordem:

1. Defina o ID e o tipo da região.
2. Cadastre novos inimigos e itens, quando necessários, em seus arquivos correspondentes.
3. Cadastre a região em `scenarios.txt`.
4. Se for uma cidade, adicione exatamente um INN em `inns.txt`.
5. Se for uma região de combate, confirme primeiro que suas cidades de resgate e os respectivos INNs já estão cadastrados e então adicione `combat_scenarios.txt`.
6. Se a região possuir lojas ou mercadores, adicione suas definições em `shops.txt` e suas possíveis ofertas em `shop_stock.txt`.
7. Se alguma área puder resolver a exploração como `REWARD`, adicione os pools elegíveis necessários em `exploration_rewards.txt`.
8. Se for uma masmorra, adicione `dungeons.txt`.
9. Adicione os encontros em `encounters.txt`.
10. Adicione drops opcionais em `enemy_drops.txt`.
11. Por último, conecte a região em `travel_routes.txt` e `scenario_branches.txt`, quando aplicável.
12. Inicialize o jogo e realize o teste manual.

Se precisar interromper o trabalho antes de concluir todas as etapas, não inicialize o jogo enquanto houver arquivos que referenciem dados ainda inexistentes.

## 13. Checklist antes do commit

### Todas as regiões

- [ ] O ID usa lowercase e underscores, sem acentos.
- [ ] O mesmo ID foi usado em todos os arquivos.
- [ ] A região existe em `scenarios.txt`.
- [ ] Toda origem e todo destino de rota existem.
- [ ] As rotas de ida e volta foram criadas quando necessárias.
- [ ] Nenhum texto contém `;`.
- [ ] Se houver lojas, seus IDs são únicos e seus tipos de acesso correspondem ao tipo da região.
- [ ] Toda loja cadastrada possui ao menos uma possível oferta em `shop_stock.txt`.
- [ ] Todos os itens ofertados existem e definem `baseShopSalePrice`.
- [ ] Percentuais de preço, estoques e pesos são positivos.
- [ ] Não existem ofertas comercialmente duplicadas na mesma loja.

### Cidades

- [ ] A cidade possui exatamente um INN em `inns.txt`.
- [ ] O ID, o nome e a descrição do INN não estão vazios.
- [ ] O preço do INN é maior que zero.

### Campos e masmorras

- [ ] A região existe em `combat_scenarios.txt`.
- [ ] O destino fallback de resgate referencia uma cidade com INN.
- [ ] O destino preferencial está vazio ou referencia uma cidade diferente do fallback que também possui INN.
- [ ] A descrição de resgate não está vazia e funciona para parties de qualquer tamanho.
- [ ] Todas as posições dos encontros são válidas.
- [ ] Todos os inimigos dos encontros existem em `enemies.txt`.
- [ ] Há candidatos suficientes para formar os grupos configurados.
- [ ] As restrições `NORMAL_ONLY`, `SPECIAL_ONLY` e `ANY` correspondem às áreas corretas nos encontros e pools de recompensa.
- [ ] Toda área que possa resolver a exploração como `REWARD` possui ao menos uma recompensa elegível.
- [ ] Todos os IDs de recompensas `ITEM` existem em `items.txt`.
- [ ] Recompensas `GOLD` mantêm o campo `rewardId` vazio.
- [ ] Quantidades e pesos das recompensas são maiores que zero e seus intervalos são válidos.
- [ ] Se `merchantChance` for maior que zero, existe ao menos um mercador `EXPLORATION` associado ao cenário.

### Masmorras

- [ ] A masmorra existe em `dungeons.txt`.
- [ ] Existem de 1 a 3 bosses narrativos de rank `BOSS`.
- [ ] Existem de 1 a 3 bosses recorrentes de rank `BOSS`.
- [ ] Existe de 1 a 3 inimigos épicos de rank `EPIC`.
- [ ] Os dois cooldowns são positivos.
- [ ] A posição mínima do épico é anterior à posição final.
- [ ] A restrição de área do épico é compatível com a configuração da exploração.
- [ ] `epicEncounterChance` é maior que zero.
- [ ] A posição final foi reservada para o boss.
- [ ] Bosses e épicos não foram incluídos nos encontros aleatórios.

### Validação

- [ ] O jogo inicializa sem erros de carregamento.
- [ ] A região aparece no menu de viagem quando deveria.
- [ ] A descrição e as quebras de linha são exibidas corretamente.
- [ ] A exploração começa em `1/N` e nunca exibe uma posição maior que `N/N`.
- [ ] Áreas normal e especial exibem as mensagens corretas.
- [ ] Os encontros respeitam posição, área e tamanho do grupo.
- [ ] Em masmorras, fugir do boss mantém o jogador na posição final.
- [ ] Vencer o boss conclui a exploração e libera a viagem esperada.
- [ ] `git diff --check` não aponta whitespace errors.
- [ ] Recompensas de item e ouro respeitam cenário, área, peso e intervalo de quantidade.
- [ ] Lojas urbanas aparecem entre os locais funcionais da cidade.
- [ ] Mercadores aparecem somente quando o evento de exploração está disponível.
- [ ] As ofertas respeitam slots, pesos, preços e estoques configurados.
- [ ] Reabrir uma loja preserva suas ofertas ativas e quantidades restantes.
- [ ] Uma oferta esgotada desaparece sem ser substituída.
- [ ] A derrota completa exibe a narrativa e o destino de resgate configurados.
- [ ] O destino preferencial é usado somente quando já foi descoberto.
- [ ] O fallback funciona mesmo quando ainda não foi descoberto.
- [ ] O resgate não avança a posição derrotada nem desbloqueia rotas não relacionadas.

## 14. Diagnóstico rápido

Para localizar todas as referências de uma região:

```bash
git --no-pager grep -n "id_da_regiao" -- data
```

Para localizar a região e todos os seus inimigos:

```bash
git --no-pager grep -n \
"id_da_regiao\|inimigo_1\|inimigo_2" \
-- data
```

Erros de carregamento devem ser corrigidos na definição que contém a referência inválida. Não crie registros falsos apenas para fazer o loader aceitar os arquivos.
