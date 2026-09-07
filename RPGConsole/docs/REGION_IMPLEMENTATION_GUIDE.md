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
| Cidade | `CITY` | `scenarios.txt` e `travel_routes.txt` |
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
marcego;Marcego;60;0;0;10;5;5;5;20;0;COMMON;Monstrinho nadador e agressivo que se assemelha a um animal voador.
```

Ranks aceitos:

- `COMMON`
- `VETERAN`
- `LEADER`
- `BOSS`
- `EPIC`
- `LEGENDARY`

Use `\n` dentro da descrição quando quiser uma quebra de linha na exibição. Não insira `;` nos textos, pois o ponto e vírgula separa os campos.

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

## 5. Configure a exploração da região de combate

Esta etapa vale apenas para `FIELD` e `DUNGEON`.

Arquivo: `data/combat_scenarios.txt`

```text
# scenarioId;baseExperienceReward;baseGoldReward;explorationLength;battleChance;rewardChance;merchantChance;epicEncounterChance;specialAreaChance;minEnemies;maxEnemies;normalAreaMessage;specialAreaMessage
```

Exemplo:

```text
ruinas_sanguemar;100;100;11;70;20;0;10;30;2;2;Você avança pela entrada submersa das Ruínas de Sanguemar.;Você desce pelas paredes da fossa e alcança uma região abissal.
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

Regras importantes:

- Cada chance individual deve estar entre 0 e 100.
- A soma de `battleChance`, `rewardChance`, `merchantChance` e `epicEncounterChance` não pode ultrapassar 100.
- `specialAreaChance` é independente dessa soma.
- `minEnemies` deve ser pelo menos 1.
- `maxEnemies` não pode ser menor que `minEnemies` nem maior que o limite suportado pela batalha, atualmente 3.
- Cidades não recebem uma linha neste arquivo.

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

Esta etapa vale para `FIELD` e `DUNGEON` quando `rewardChance` for maior que zero.

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
- Se `rewardChance` for zero, a região não precisa possuir entradas.
- Se recompensas forem possíveis, toda área normal ou especial que possa ocorrer precisa possuir ao menos uma entrada elegível.
- Entradas completamente idênticas não são permitidas.
- O mesmo item pode aparecer em várias entradas quando algum outro campo for diferente.
- Para desativar temporariamente uma recompensa, comente a linha em vez de definir peso zero.
- Estes pools são repetíveis e não devem ser usados para recompensas únicas, narrativas ou de quests.

## 8. Configure os chefes da masmorra

Esta etapa vale apenas para `DUNGEON`.

Arquivo: `data/dungeons.txt`

```text
# scenarioId;bossEnemyIds;epicEnemyIds
```

Exemplo com batalha dupla de chefes:

```text
ruinas_sanguemar;defleto,refrata;colossangue
```

Regras:

- Separe IDs do mesmo grupo com vírgulas, sem espaços.
- Cada grupo deve possuir de 1 a 3 inimigos.
- Todos os IDs precisam existir em `enemies.txt`.
- O grupo de boss aceita somente inimigos de rank `BOSS`.
- O grupo épico aceita somente inimigos de rank `EPIC`.
- Não repita um ID dentro do grupo nem entre os dois grupos da mesma masmorra.
- Campos e cidades não recebem uma linha neste arquivo.

Na última posição de uma masmorra, a exploração não gera um encontro aleatório: o jogador deve usar a ação de enfrentar o boss. A exploração só é concluída após a vitória.

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
4. Se for uma região de combate, adicione `combat_scenarios.txt`.
5. Se a região possuir lojas ou mercadores, adicione suas definições em `shops.txt` e suas possíveis ofertas em `shop_stock.txt`.
6. Se `rewardChance` for maior que zero, adicione as entradas necessárias em `exploration_rewards.txt`.
7. Se for uma masmorra, adicione `dungeons.txt`.
8. Adicione os encontros em `encounters.txt`.
9. Adicione drops opcionais em `enemy_drops.txt`.
10. Por último, conecte a região em `travel_routes.txt` e `scenario_branches.txt`, quando aplicável.
11. Inicialize o jogo e realize o teste manual.

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

### Campos e masmorras

- [ ] A região existe em `combat_scenarios.txt`.
- [ ] Todas as posições dos encontros são válidas.
- [ ] Todos os inimigos dos encontros existem em `enemies.txt`.
- [ ] Há candidatos suficientes para formar os grupos configurados.
- [ ] As restrições `NORMAL_ONLY`, `SPECIAL_ONLY` e `ANY` correspondem às áreas corretas nos encontros e pools de recompensa.
- [ ] Se `rewardChance` for maior que zero, todas as áreas possíveis possuem ao menos uma recompensa elegível.
- [ ] Todos os IDs de recompensas `ITEM` existem em `items.txt`.
- [ ] Recompensas `GOLD` mantêm o campo `rewardId` vazio.
- [ ] Quantidades e pesos das recompensas são maiores que zero e seus intervalos são válidos.
- [ ] Se `merchantChance` for maior que zero, existe ao menos um mercador `EXPLORATION` associado ao cenário.

### Masmorras

- [ ] A masmorra existe em `dungeons.txt`.
- [ ] Existe de 1 a 3 bosses de rank `BOSS`.
- [ ] Existe de 1 a 3 épicos de rank `EPIC`.
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

## 13. Diagnóstico rápido

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
