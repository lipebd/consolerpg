# Guia rápido de criação de habilidades

Este guia explica como transformar uma ideia de habilidade em dados que o jogo consegue carregar. Ele foi escrito para ajudar principalmente na criação e revisão das habilidades; não é necessário conhecer as classes Java do projeto.

## A ideia principal: uma habilidade é uma sequência de componentes

Cada habilidade possui duas partes:

- uma definição geral em `data/skills.txt`, com nome, classe, nível, custos e descrição;
- um ou mais componentes em `data/skill_effects.txt`, indicando o que acontece e em qual ordem.

Por exemplo, Quebra-Armadura não é cadastrada como um único efeito misto. Ela é formada por:

1. causar dano físico;
2. tentar reduzir a DEF do mesmo inimigo.

Isso permite criar habilidades compostas combinando dano, cura, buffs, debuffs, purificação e outros efeitos.

Antes de cadastrar uma habilidade, escreva seu funcionamento como uma pequena sequência de ações. Essa sequência ajuda a decidir a ordem dos componentes.

## A ordem dos componentes importa

Os componentes são executados de acordo com o número de sequência e com sua fase.

### Fases possíveis

| Valor no arquivo | Significado |
|---|---|
| `PRE_ACTION` | Acontece antes da ação principal. Pode alterar o resultado dos componentes seguintes. |
| `PRIMARY` | Forma o corpo principal da habilidade. Os componentes são executados na ordem em que foram numerados. |
| `POST_ACTION` | Acontece depois que o turno usado pela ação já foi encerrado. |

As fases sempre seguem esta ordem:

```text
PRE_ACTION -> PRIMARY -> POST_ACTION
```

Não é permitido voltar para uma fase anterior. Depois que uma habilidade entra em `PRIMARY`, por exemplo, não pode haver outro componente `PRE_ACTION`.

### Exemplos de como a ordem muda o resultado

Se um buff de ATK vier antes do dano, o ataque daquela própria habilidade já usa o ATK aumentado:

```text
1. PRE_ACTION: ATK +2
2. PRIMARY: dano físico
```

Se o dano vier antes do buff, o fortalecimento só beneficiará ações posteriores:

```text
1. PRIMARY: dano físico
2. PRIMARY: ATK +2
```

Condições também são verificadas quando cada componente chega à sua vez:

- Cálculo Terminal verifica o HP do inimigo em `PRE_ACTION`, antes do disparo;
- Subjugamento causa dano primeiro e depois verifica o HP restante, então o próprio ataque pode habilitar seus debuffs adicionais;
- Transfiguração aumenta LCK em `PRE_ACTION`, portanto seus autodebuffs posteriores usam a LCK já fortalecida.

## Efeitos disponíveis

| Valor no arquivo | O que faz |
|---|---|
| `DAMAGE` | Causa dano físico ou mágico. |
| `RECOIL_DAMAGE` | Faz o usuário sofrer uma porcentagem do dano real causado pelo componente imediatamente anterior. O recuo nunca nocauteia o usuário. |
| `DAMAGE_POWER_MODIFIER` | Aumenta o poder de componentes de dano posteriores contra o mesmo alvo durante aquela utilização. |
| `HP_RECOVERY` | Recupera HP usando ATK ou MAG efetivos do usuário como escala. |
| `MP_RECOVERY` | Recupera uma quantidade fixa de MP. |
| `SP_RECOVERY` | Recupera uma quantidade fixa de SP. |
| `REVIVE` | Revive um aliado e restaura HP usando ATK ou MAG efetivos do usuário como escala. |
| `ATTRIBUTE_STAGE` | Aplica um buff ou debuff de atributo. |
| `PURIFICATION` | Remove todos os estágios negativos de atributo do alvo. |
| `DISSIPATION` | Tenta remover todos os estágios positivos de atributo de um inimigo. Pode ser resistida. |
| `SPECIAL_STATE` | Aplica um estado especial. Atualmente é usado para Distorção Ancestral. |

### Dano

Um componente `DAMAGE` precisa informar:

- `PHYSICAL` para dano baseado em ATK contra DEF;
- `MAGICAL` para dano baseado em MAG contra RES;
- um poder positivo.

O poder não é a quantidade final de HP removida. Atributos, defesa, equipamentos e outros modificadores ainda participam do cálculo.

### Recuo

`RECOIL_DAMAGE` é usado depois de um `DAMAGE` contra um único inimigo. Seu poder representa uma porcentagem.

Com poder `50`, o usuário sofre metade do dano que realmente retirou do inimigo. Se o ataque retirar apenas 3 HP porque o inimigo estava quase derrotado, o recuo será calculado a partir desses 3 pontos, não do dano teórico.

O recuo:

- usa `SELF` como alvo;
- fica em `PRIMARY` imediatamente depois do dano que o alimenta;
- ignora DEF e o estado de defesa do usuário;
- não reduz o usuário abaixo de 1 HP;
- pode possuir chance própria de ativação.

### Recuperação

`HP_RECOVERY` e `REVIVE` precisam indicar em `attribute` se escalam com `ATK` ou `MAG`.

`MP_RECOVERY` e `SP_RECOVERY` usam apenas o poder fixo e deixam o atributo vazio.

### Buffs e debuffs

`ATTRIBUTE_STAGE` precisa indicar:

- o atributo alterado: `ATK`, `DEF`, `MAG`, `RES`, `VEL` ou `LCK`;
- estágios entre `-3` e `3`, sem usar zero;
- duração positiva para todos os atributos, exceto VEL.

Valores positivos são buffs e valores negativos são debuffs.

VEL possui uma regra especial: sua alteração fica pendente para a próxima rodada e, por isso, o campo de duração deve ficar vazio.

Estágios negativos podem ser resistidos. Isso também vale para um autodebuff: o personagem usa a própria LCK como atributo aplicador e a própria RES como atributo defensor.

### Trigger e resistência são testes diferentes

`triggerChancePercent` é a chance do componente ser ativado. Campo vazio significa `100%`.

O trigger é sorteado uma vez para o componente inteiro, mesmo quando ele possui vários alvos. Se o componente for ativado, cada alvo resistível faz seu próprio teste de resistência.

Assim, um efeito em área pode ser ativado uma vez, ser aplicado em alguns inimigos e ser resistido por outros.

A ordem é:

```text
condição -> aplicabilidade -> trigger -> resistência -> aplicação
```

Dano, cura, buffs positivos, purificação e Distorção Ancestral não fazem teste de resistência. Debuffs de atributo e dissipação fazem.

## Alvos disponíveis

| Valor no arquivo | Alvo utilizado |
|---|---|
| `SELF` | Apenas o usuário. |
| `SINGLE_ALLY` | Um aliado escolhido. |
| `ALL_ALLIES` | Todos os aliados, incluindo o usuário. |
| `ALL_BUT_SELF` | Todos os outros aliados, menos o usuário. |
| `RANDOM_ALLY` | Um aliado aleatório. |
| `SINGLE_ENEMY` | Um inimigo escolhido. |
| `ALL_ENEMIES` | Todos os inimigos presentes. |
| `RANDOM_ENEMY` | Um inimigo aleatório. |

Cada componente possui seu próprio alvo. Uma habilidade pode, por exemplo, causar dano em `SINGLE_ENEMY` e depois aplicar um buff em `SELF`.

## Condições disponíveis

| Valor no arquivo | Significado |
|---|---|
| `NONE` | O componente não possui condição. |
| `SELF_HP_AT_MOST_PERCENT` | Só executa se o usuário estiver com, no máximo, a porcentagem de HP informada. |
| `TARGET_HP_AT_MOST_PERCENT` | Só executa se o alvo estiver com, no máximo, a porcentagem de HP informada. |

Quando a condição for `NONE`, `conditionValue` fica vazio. Nas condições de HP, o valor deve estar entre `1` e `100`.

## Cadastro em `skills.txt`

O cabeçalho é:

```text
# id;classId;acquisitionLevel;mpCost;spCost;name;description
```

| Coluna | Tradução prática |
|---|---|
| `id` | Identificador interno da habilidade. Use letras minúsculas, números e `_`, como `quebra_armadura`. |
| `classId` | ID da classe que aprende a habilidade. |
| `acquisitionLevel` | Nível em que a habilidade é aprendida. |
| `mpCost` | MP consumido. Use `0` quando não houver custo de MP. |
| `spCost` | SP consumido. Use `0` quando não houver custo de SP. |
| `name` | Nome apresentado ao jogador. |
| `description` | Texto narrativo e mecânico apresentado ao jogador. Use `\n` para inserir uma quebra de linha. |

Exemplo:

```text
quebra_armadura;flagelo;3;0;20;Quebra-Armadura;Golpeia e estilhaça a proteção do inimigo, causando dano físico.\nReduz a DEF do alvo em 1 estágio por 2 turnos.
```

A descrição deve explicar a fantasia e os efeitos relevantes, mas não precisa prometer o dano ou a cura finais. Esses valores mudam conforme os atributos e a situação da batalha.

## Cadastro em `skill_effects.txt`

O cabeçalho é:

```text
# skillId;sequence;phase;effectType;targetType;damageType;power;attribute;stages;duration;triggerChancePercent;specialStateType;conditionType;conditionValue
```

| Coluna | Tradução prática |
|---|---|
| `skillId` | ID da habilidade à qual o componente pertence. Deve existir em `skills.txt`. |
| `sequence` | Posição do componente dentro da habilidade: `1`, `2`, `3` etc. |
| `phase` | Momento da execução: `PRE_ACTION`, `PRIMARY` ou `POST_ACTION`. |
| `effectType` | Operação realizada, como dano, cura, buff ou debuff. |
| `targetType` | Quem recebe este componente. |
| `damageType` | `PHYSICAL` ou `MAGICAL`. Fica vazio quando o componente não causa dano normal. |
| `power` | Força-base do componente. Dependendo do efeito, pode representar dano, recuperação, percentual de recuo ou modificador. |
| `attribute` | Atributo alterado ou usado como escala de cura. |
| `stages` | Quantidade de estágios aplicados. Positivo para buff e negativo para debuff. |
| `duration` | Quantidade de turnos. Fica vazia para VEL e para efeitos que não usam duração. |
| `triggerChancePercent` | Chance de ativação do componente. Vazio equivale a `100`. |
| `specialStateType` | Estado especial. Atualmente o valor disponível é `DISTORTED_ATTRIBUTES`. |
| `conditionType` | Condição do componente. Use `NONE` quando não houver. |
| `conditionValue` | Porcentagem usada pela condição. Fica vazia com `NONE`. |

Mesmo quando uma coluna não se aplica, sua posição continua existindo. Por isso os `;` vazios devem ser preservados.

### Exemplo simples: dano seguido de debuff

```text
quebra_armadura;1;PRIMARY;DAMAGE;SINGLE_ENEMY;PHYSICAL;10;;;;;;NONE;
quebra_armadura;2;PRIMARY;ATTRIBUTE_STAGE;SINGLE_ENEMY;;;DEF;-1;2;;;NONE;
```

### Exemplo com condição anterior ao dano

```text
calculo_terminal;1;PRE_ACTION;DAMAGE_POWER_MODIFIER;SINGLE_ENEMY;;50;;;;;;TARGET_HP_AT_MOST_PERCENT;30
calculo_terminal;2;PRIMARY;DAMAGE;SINGLE_ENEMY;PHYSICAL;40;;;;;;NONE;
```

### Exemplo com recuo probabilístico

```text
investida;1;PRIMARY;DAMAGE;SINGLE_ENEMY;PHYSICAL;5;;;;;;NONE;
investida;2;PRIMARY;RECOIL_DAMAGE;SELF;;50;;;;25;;NONE;
```

### Exemplo com LCK anterior a autodebuffs

```text
transfiguracao;1;PRE_ACTION;ATTRIBUTE_STAGE;SELF;;;LCK;3;3;;;NONE;
transfiguracao;2;PRIMARY;DAMAGE;ALL_ENEMIES;MAGICAL;35;;;;;;NONE;
transfiguracao;3;PRIMARY;ATTRIBUTE_STAGE;SELF;;;MAG;3;3;;;NONE;
transfiguracao;4;PRIMARY;ATTRIBUTE_STAGE;SELF;;;VEL;3;;;;NONE;
transfiguracao;5;PRIMARY;ATTRIBUTE_STAGE;SELF;;;DEF;-2;3;;;NONE;
transfiguracao;6;PRIMARY;ATTRIBUTE_STAGE;SELF;;;RES;-2;3;;;NONE;
```

## Checklist antes de enviar uma habilidade

- A habilidade possui uma linha em `skills.txt`?
- O ID está escrito exatamente da mesma forma nos dois arquivos?
- A classe informada existe?
- As sequências começam em `1`, são únicas e estão em ordem crescente?
- As fases seguem `PRE_ACTION -> PRIMARY -> POST_ACTION`?
- A ordem dos componentes corresponde à descrição?
- Dano possui tipo e poder?
- Cura de HP ou ressurreição possui poder e atributo `ATK` ou `MAG`?
- Buff ou debuff possui atributo, estágios e duração adequada?
- Componentes de VEL deixaram a duração vazia?
- Trigger vazio realmente deve significar ativação garantida?
- Condição `NONE` deixou o valor da condição vazio?
- Todos os campos vazios mantiveram seus separadores `;`?
- A descrição evita informar um dano ou uma cura final que será calculada durante a batalha?

Depois do cadastro, compile e inicialize o jogo. O carregador rejeitará combinações inválidas antes de uma habilidade defeituosa chegar à batalha.
