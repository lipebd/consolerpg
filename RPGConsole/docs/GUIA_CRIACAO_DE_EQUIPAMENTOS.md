# Guia rápido de criação de equipamentos

Este guia explica como transformar uma ideia de arma, armadura ou berloque em dados que o jogo consegue carregar. Ele foi escrito para ajudar principalmente na criação e revisão dos equipamentos; não é necessário conhecer as classes Java do projeto.

## A ideia principal: cada equipamento possui duas linhas

Todo equipamento precisa aparecer em dois lugares:

1. Em `data/items.txt`, com sua identidade, categoria, preços, nome e descrição.
2. No arquivo especializado da sua categoria, com requisito de nível, atributos e regras próprias.

| Categoria | Arquivo especializado |
|---|---|
| Arma | `data/weapons.txt` |
| Armadura | `data/armors.txt` |
| Berloque | `data/trinkets.txt` |

As duas linhas são ligadas pelo mesmo ID.

Por exemplo, uma arma chamada Adaga Improvisada possui:

```text
items.txt:   adaga_improvisada;WEAPON;...
weapons.txt: adaga_improvisada;PHYSICAL;DAGGER;...
```

Se o ID estiver diferente, se a categoria não corresponder ao arquivo ou se uma das linhas estiver faltando, o jogo interrompe a inicialização e informa o cadastro inválido.

## Primeiro passo: cadastrar o item-base

O cabeçalho de `data/items.txt` é:

```text
# id;category;playerTradable;baseShopSalePrice;shopPurchasePrice;name;description
```

| Coluna | Tradução prática |
|---|---|
| `id` | Identificador interno e estável. Use letras minúsculas, números e `_`, sem acentos. |
| `category` | Use `WEAPON`, `ARMOR` ou `TRINKET`. |
| `playerTradable` | Reserva para futura troca entre jogadores. Use o valor decidido para o item. |
| `baseShopSalePrice` | Preço-base usado quando uma loja vende o item. Pode ficar vazio se lojas não o venderem. |
| `shopPurchasePrice` | Valor pago quando uma loja compra o item do jogador. Pode ficar vazio se o item não puder ser vendido. |
| `name` | Nome apresentado ao jogador. |
| `description` | Texto narrativo apresentado ao jogador. Use `\n` para inserir uma quebra de linha. |

Exemplo de formato:

```text
adaga_improvisada;WEAPON;true;100;25;Adaga Improvisada;Uma lâmina simples, mas melhor do que lutar com as mãos vazias.
```

Os preços acima são apenas ilustrativos. Antes de cadastrar valores definitivos, compare o item com os preços e a progressão econômica já existentes.

## Bônus disponíveis para todos os equipamentos

Armas, armaduras e berloques usam a mesma ordem de nove bônus:

```text
hpBonus;mpBonus;spBonus;attackBonus;defenseBonus;magicBonus;resistanceBonus;speedBonus;luckBonus
```

| Campo | O que aumenta |
|---|---|
| `hpBonus` | HP máximo |
| `mpBonus` | MP máximo |
| `spBonus` | SP máximo |
| `attackBonus` | ATK |
| `defenseBonus` | DEF |
| `magicBonus` | MAG |
| `resistanceBonus` | RES |
| `speedBonus` | VEL |
| `luckBonus` | LCK |

Todos esses valores são opcionais e não podem ser negativos. Campo vazio significa bônus zero.

Exemplo dos mesmos bônus escritos de duas formas:

```text
;;;10;;;;;;
0;0;10;0;0;0;0;0;0
```

Nos dois casos, o equipamento concede apenas `SP +10`.

Mesmo quando um campo fica vazio, sua coluna continua existindo. Preserve todos os `;` para que os valores seguintes não mudem de posição.

## Criação de armas

O cabeçalho de `data/weapons.txt` é:

```text
# itemId;damageType;weaponType;levelRequirement;normalAttackName;baseWeaponDamage;hpBonus;mpBonus;spBonus;attackBonus;defenseBonus;magicBonus;resistanceBonus;speedBonus;luckBonus
```

| Coluna | Tradução prática |
|---|---|
| `itemId` | ID da arma em `items.txt`. |
| `damageType` | Tipo do ataque normal: `PHYSICAL` ou `MAGICAL`. |
| `weaponType` | Categoria da arma, como `DAGGER`, `SWORD` ou `LUTE`. |
| `levelRequirement` | Nível mínimo necessário para equipar. Deve ser positivo. |
| `normalAttackName` | Nome narrativo mostrado quando o personagem usa o ataque básico. |
| `baseWeaponDamage` | Poder-base da arma. Na interface aparece simplesmente como **Poder**. Deve ser positivo. |
| Demais colunas | Os nove bônus opcionais na ordem padrão. |

### Tipos de arma disponíveis

| Valor no arquivo | Tipo |
|---|---|
| `DAGGER` | Adaga |
| `SCEPTER` | Cetro |
| `GAUNTLET` | Manopla |
| `LUTE` | Alaúde |
| `SCYTHE` | Foice |
| `PISTOL` | Pistola |
| `RIFLE` | Rifle |
| `SWORD` | Espada |
| `BOW` | Arco |
| `STAFF` | Cajado |
| `GRIMOIRE` | Grimório |

### Como o dano da arma funciona

O ataque básico usa:

- o `damageType` da arma;
- o `baseWeaponDamage` da arma;
- o ATK ou MAG efetivo do personagem.

Uma arma `PHYSICAL` usa ATK contra DEF. Uma arma `MAGICAL` usa MAG contra RES.

Toda habilidade que causa dano também recebe 100% do poder da arma equipada. Porém, o tipo da arma não altera o tipo da habilidade: uma habilidade física continua física mesmo quando o personagem usa uma arma mágica.

O ataque básico não consome MP ou SP e usa poder de habilidade igual a zero. Uma habilidade ofensiva soma seu próprio poder ao poder da arma.

### Exemplo de arma

```text
adaga_improvisada;PHYSICAL;DAGGER;1;Corte Rápido;1;;;10;;;;;;
```

Essa linha descreve uma adaga de nível 1:

- ataque normal físico chamado Corte Rápido;
- Poder 1;
- SP máximo +10;
- nenhum outro bônus.

Qualquer classe pode equipar qualquer arma quando cumprir o requisito de nível.

## Criação de armaduras

O cabeçalho de `data/armors.txt` é:

```text
# itemId;armorWeight;levelRequirement;hpBonus;mpBonus;spBonus;attackBonus;defenseBonus;magicBonus;resistanceBonus;speedBonus;luckBonus
```

| Coluna | Tradução prática |
|---|---|
| `itemId` | ID da armadura em `items.txt`. |
| `armorWeight` | Peso aceito pela classe: `LIGHT`, `MEDIUM` ou `HEAVY`. |
| `levelRequirement` | Nível mínimo necessário para equipar. Deve ser positivo. |
| Demais colunas | Os nove bônus opcionais na ordem padrão. |

### Pesos disponíveis

| Valor no arquivo | Peso |
|---|---|
| `LIGHT` | Leve |
| `MEDIUM` | Médio |
| `HEAVY` | Pesado |

A classe só pode equipar uma armadura do seu peso exato. Uma classe de peso médio não pode usar armadura leve nem pesada.

As armaduras não possuem um valor separado de “poder de defesa”. Toda proteção deve ser representada pelos bônus normais, como HP, DEF ou RES.

### Exemplo de armadura

```text
armadura_exemplo;MEDIUM;7;25;0;0;0;5;0;3;0;0
```

Essa linha descreve uma armadura média de nível 7 com:

- HP máximo +25;
- DEF +5;
- RES +3.

Lembre-se de cadastrar `armadura_exemplo` em `items.txt` com a categoria `ARMOR`.

## Criação de berloques

O cabeçalho de `data/trinkets.txt` é:

```text
# itemId;trinketType;levelRequirement;hpBonus;mpBonus;spBonus;attackBonus;defenseBonus;magicBonus;resistanceBonus;speedBonus;luckBonus;experienceModifierPercent;goldModifierPercent
```

| Coluna | Tradução prática |
|---|---|
| `itemId` | ID do berloque em `items.txt`. |
| `trinketType` | Tipo narrativo: `JEWELRY`, `AMULET` ou `TALISMAN`. |
| `levelRequirement` | Nível mínimo necessário para equipar. Deve ser positivo. |
| Bônus de atributos | Os nove bônus opcionais na ordem padrão. |
| `experienceModifierPercent` | Porcentagem extra de EXP recebida apenas pelo personagem equipado. |
| `goldModifierPercent` | Porcentagem extra de GOLD recebida pelo grupo inteiro. |

### Tipos de berloque disponíveis

| Valor no arquivo | Tipo |
|---|---|
| `JEWELRY` | Joia |
| `AMULET` | Amuleto |
| `TALISMAN` | Talismã |

Todo berloque precisa conceder pelo menos um bônus de atributo, EXP ou GOLD. Um berloque completamente vazio é rejeitado.

Qualquer classe pode equipar qualquer berloque quando cumprir o requisito de nível.

### Modificadores de recompensa

O bônus de EXP pertence somente ao personagem equipado. Se Flagelo usa `M.EXP +5%`, os outros aliados não recebem esse bônus.

O bônus de GOLD pertence ao grupo. Os bônus de todos os berloques equipados são somados. Por exemplo:

```text
Bardo:   M.GOLD +5%
Oráculo: M.GOLD +10%
Total do grupo: M.GOLD +15%
```

Modificadores temporários também entram nessa soma. Um bônus temporário de `M.GOLD +25%` junto dos dois berloques acima produz `M.GOLD +40%`. O jogo soma as porcentagens primeiro e arredonda a recompensa uma única vez.

Na interface, `+5%` aparece como multiplicador `1.05x`.

### Exemplo de berloque

```text
talisma_exemplo;TALISMAN;1;0;0;0;0;0;0;0;0;0;5;10
```

Esse talismã de nível 1 não altera atributos, mas concede:

- M.EXP +5% ao usuário;
- M.GOLD +10% ao grupo.

## O que acontece ao equipar

Cada personagem possui um slot de arma, um de armadura e um de berloque.

Ao equipar uma unidade:

- ela sai do inventário compartilhado;
- passa a existir no slot do personagem;
- o equipamento anterior daquele slot volta ao inventário;
- os atributos e modificadores passam a valer imediatamente.

Ao remover um equipamento, ele volta ao inventário. Por isso o inventário não mostra uma marca `(E)`: uma unidade equipada não está na lista de itens guardados.

É possível equipar por dois caminhos:

```text
Inventário -> categoria -> item -> Equipar -> personagem -> confirmar
Status -> personagem -> Equipamentos -> slot -> item -> confirmar
```

A tela de confirmação mostra apenas o que realmente muda. Bônus de HP, MP ou SP aumentam o máximo, mas não restauram o recurso atual. Se um equipamento for removido e o máximo diminuir, o valor atual é limitado ao novo máximo.

## Regras para nome e descrição

- O ID é técnico, não tem acentos e não deve mudar depois de publicado.
- O nome é apresentado ao jogador e pode usar acentos, espaços e pontuação.
- A descrição deve explicar a fantasia do item e, quando útil, sua característica marcante.
- Não é necessário repetir todos os números na descrição: a ficha do equipamento já mostra requisito, Poder, bônus e multiplicadores.
- O nome do ataque normal deve funcionar em uma frase como: `Flagelo executou Corte Rápido com sua arma!`

## Erros comuns

### O jogo diz que o item não existe

Confirme se o `itemId` foi cadastrado primeiro em `items.txt` e se está escrito exatamente igual nos dois arquivos.

### O jogo diz que a categoria está errada

Confira a correspondência:

```text
weapons.txt  -> WEAPON
armors.txt   -> ARMOR
trinkets.txt -> TRINKET
```

### Um bônus apareceu na coluna errada

Algum campo vazio ou `;` provavelmente foi removido. Compare a linha com o cabeçalho e conte as colunas.

### A armadura não aparece para o personagem

Confira o nível exigido e o peso da classe. A correspondência de peso precisa ser exata.

### A arma ou o berloque não aparece para o personagem

Confira o requisito de nível e se ainda existe pelo menos uma unidade no inventário. Uma unidade já equipada em outro personagem não continua disponível.

### O berloque foi rejeitado

Ele precisa conceder ao menos um bônus de atributo, EXP ou GOLD.

## Checklist antes de enviar um equipamento

- O item possui uma linha em `items.txt`?
- A categoria em `items.txt` é `WEAPON`, `ARMOR` ou `TRINKET` conforme o arquivo especializado?
- O ID está escrito exatamente da mesma forma nos dois arquivos?
- O ID usa apenas letras minúsculas, números e `_`, sem acentos?
- Existe somente uma definição especializada para esse ID?
- O requisito de nível é positivo?
- Os campos obrigatórios foram preenchidos?
- Os bônus estão na ordem HP, MP, SP, ATK, DEF, MAG, RES, VEL e LCK?
- Todos os bônus e modificadores são inteiros não negativos?
- Todos os `;` dos campos vazios foram preservados?
- A arma possui tipo de dano, tipo de arma, nome de ataque e Poder positivos?
- A armadura possui um peso válido e coerente com as classes que devem usá-la?
- O berloque concede pelo menos um bônus?
- O nome e a descrição estão em português natural?
- O equipamento foi comparado com itens próximos de nível semelhante antes de definir seus valores finais?

Quando o jogo compilar e iniciar sem erros, ainda vale testar o item pelos dois caminhos de equipamento, confirmar seus atributos no status e, no caso de armas ou berloques, testar também o ataque e as recompensas de batalha.
