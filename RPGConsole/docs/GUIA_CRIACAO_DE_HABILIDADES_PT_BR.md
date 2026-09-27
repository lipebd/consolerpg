# Guia de criação de habilidades — RPGConsole

Este guia ajuda a transformar uma ideia de habilidade em linhas dos arquivos `data/skills.txt` e `data/skill_effects.txt`. Ele é voltado a quem escreve conteúdo para o jogo. Cada exemplo é uma receita independente: escolha um ID novo, uma classe apropriada e adapte os números antes de acrescentá-lo aos dados reais.

## Comece pela descrição

Escreva a ação em passos: “aumenta a própria sorte; golpeia um inimigo três vezes; o terceiro golpe fica mais forte se o alvo estiver ferido”. Cada passo costuma corresponder a uma linha de `skill_effects.txt`. A ordem muda o resultado: um bônus antes do golpe já pode fortalecê-lo; um bônus depois do golpe prepara as ações seguintes.

| Fase | Quando acontece | Exemplo |
|---|---|---|
| `PRE_ACTION` | Antes do corpo principal. | Aumentar ATK antes de atacar. |
| `PRIMARY` | Corpo principal, na ordem das linhas. | Golpear, curar, aplicar debuff. |
| `POST_ACTION` | Depois de encerrar o turno do usuário. | Conceder buff que começa após o próprio turno. |

As linhas usam números de sequência positivos, únicos e crescentes. Podem existir intervalos na numeração; as fases não podem voltar de `PRIMARY` para `PRE_ACTION`, nem de `POST_ACTION` para qualquer fase anterior. Em `POST_ACTION`, a duração dos efeitos antigos do usuário já passou pela redução de fim de turno.

## Primeiro arquivo: `skills.txt`

```text
# id;classId;acquisitionType;levelRequirement;mpCost;spCost;name;description
exemplo_eco;bardo;LEVEL;7;20;0;Eco de Exemplo;Atinge um inimigo com dano mágico.\nSe ele ficar ferido, reduz sua DEF.
```

| Coluna | O que escrever |
|---|---|
| `id` | Identificador exclusivo em letras minúsculas, números e `_`, como `exemplo_eco`. |
| `classId` | ID de uma classe jogável existente. |
| `acquisitionType` | `LEVEL` para aprender ao atingir o nível; `SCROLL` para aprender com um pergaminho compatível. |
| `levelRequirement` | Nível de aquisição ou nível mínimo do pergaminho, dentro do intervalo de níveis do jogo. |
| `mpCost`, `spCost` | Custos inteiros não negativos; use `0` quando não houver custo. |
| `name` | Nome mostrado ao jogador. |
| `description` | Explicação mostrada ao jogador; escreva `\n` para uma quebra de linha. |

Uma habilidade `SCROLL` também precisa de um vínculo válido e exclusivo em `skill_scrolls.txt`; a linha de `skills.txt` sozinha não cria o pergaminho. Cada habilidade precisa ter ao menos um efeito que possa agir sobre alguém: apenas modificadores de dano não bastam. Antes de copiar um exemplo com `LEVEL`, confira os espaços de habilidades naturais da classe e o nível pretendido.

## Segundo arquivo: `skill_effects.txt`

```text
# skillId;sequence;phase;effectType;targetType;damageType;power;attribute;stages;duration;triggerChancePercent;specialStateType;conditionType;conditionValue
exemplo_eco;1;PRIMARY;DAMAGE;SINGLE_ENEMY;MAGICAL;30;;;;;;NONE;
exemplo_eco;2;POST_ACTION;ATTRIBUTE_STAGE;SINGLE_ENEMY;;;DEF;-1;2;;;TARGET_HP_LESS_THAN_PERCENT;51
```

Cada linha tem **14 campos**, inclusive quando os campos intermediários ou o último ficam vazios. Preserve todos os `;`. As linhas de um mesmo `skillId` formam uma habilidade; a primeira coluna deve corresponder exatamente a um ID existente em `skills.txt`. O cabeçalho acima mostra a posição de cada valor.

| Coluna | Para que serve |
|---|---|
| `sequence` | Ordem de execução dentro da habilidade. |
| `phase` | `PRE_ACTION`, `PRIMARY` ou `POST_ACTION`. |
| `effectType` | O que acontece; veja a tabela de efeitos abaixo. |
| `targetType` | Quem pode receber o efeito; veja a tabela de alvos. |
| `damageType` | `PHYSICAL` ou `MAGICAL`, somente para `DAMAGE`. |
| `power` | Número com significado diferente conforme o efeito: força, porcentagem ou quantidade de atributos. |
| `attribute` | Atributo usado no dano ou na cura, ou atributo que recebe estágios. |
| `stages` | Quantidade de estágios, positiva ou negativa, somente em `ATTRIBUTE_STAGE`. |
| `duration` | Duração de estágios convencionais; fica vazia para VEL. |
| `triggerChancePercent` | Chance de ativação, de `1` a `100`; vazio significa `100`. |
| `specialStateType` | `DISTORTED_ATTRIBUTES` somente em `SPECIAL_STATE`. |
| `conditionType` | Uma das condições da tabela; use `NONE` quando não houver condição. |
| `conditionValue` | Limite percentual ou nome de atributo exigido pela condição; vazio com `NONE`. |

`ATK`, `DEF`, `MAG`, `RES`, `VEL` e `LCK` são os atributos disponíveis onde um atributo for permitido. Deixe em branco os campos que não pertencem ao efeito; preencher “por garantia” pode impedir o carregamento.

## Tipos de alvo

| Valor | Quem recebe o componente |
|---|---|
| `SELF` | Quem usa a habilidade. |
| `SINGLE_ALLY` | Um aliado escolhido para a habilidade. |
| `ALL_ALLIES` | Todos os aliados elegíveis, incluindo o usuário. |
| `ALL_BUT_SELF` | Os outros aliados elegíveis. Se estiver sozinho, pode não haver alvo. |
| `RANDOM_ALLY` | Um aliado elegível sorteado para **cada componente**; em `REVIVE`, o sorteio procura aliados derrotados. |
| `SINGLE_ENEMY` | Um inimigo escolhido para a habilidade. |
| `ALL_ENEMIES` | Todos os inimigos elegíveis; uma condição referente ao alvo vale individualmente para cada um. |
| `RANDOM_ENEMY` | Um inimigo vivo sorteado para **cada componente**. Pode repetir um inimigo em golpes diferentes. |

Se um golpe aleatório derrotar seu alvo, o próximo sorteia entre os sobreviventes. Se todos caírem, golpes restantes deixam de encontrar alvo. Em pares especiais de modificador `PRIMARY` e dano, os dois componentes usam **o mesmo** sorteio; a condição do modificador é examinada nesse alvo. Uma condição que falha não provoca novo sorteio. Um alvo aleatório reservado antes de encerrar o turno para um efeito `POST_ACTION` continua sendo o alvo desse efeito depois do turno.

## Tipos de efeito

| Valor | O que faz | Campos principais |
|---|---|---|
| `DAMAGE` | Causa dano físico ou mágico. | `damageType`, `power` positivo; `attribute` ofensivo opcional. |
| `DAMAGE_POWER_MODIFIER` | Aumenta em porcentagem o poder de dano compatível. | `power` positivo; regras de posição abaixo. |
| `RECOIL_DAMAGE` | Faz o usuário perder uma porcentagem do HP efetivamente retirado pelo golpe anterior, sem derrotá-lo. | `PRIMARY`, `SELF`, `power` de `1` a `100`. |
| `HP_SACRIFICE` | Sacrifica até uma porcentagem do HP **atual** do usuário; nunca o deixa abaixo de 1 HP. | `PRIMARY`, `SELF`, `power` de `1` a `100`. |
| `HP_RECOVERY` | Recupera HP conforme o ATK ou MAG efetivo do usuário. | `power` positivo, `attribute=ATK` ou `MAG`, alvo aliado. |
| `MP_RECOVERY`, `SP_RECOVERY` | Recuperam quantidade fixa de MP ou SP. | `power` positivo, alvo aliado; `attribute` vazio. |
| `REVIVE` | Revive um aliado derrotado, com HP escalado por ATK ou MAG. | `power` positivo, `attribute=ATK` ou `MAG`, alvo aliado que não seja `SELF`. |
| `HP_BALANCE` | Calcula a média dos HP atuais dos aliados vivos e define o HP de cada um até seu máximo individual. | `PRIMARY`, `ALL_ALLIES`, sem `power` nem condição. |
| `ATTRIBUTE_STAGE` | Aplica estágios positivos ou negativos de um atributo. | `attribute`, `stages` de `-3` a `3` exceto zero; `duration` positiva fora de VEL. |
| `PURIFICATION` | Remove estágios negativos de atributos do aliado. | Alvo aliado; `power` vazio para todos ou positivo para até essa quantidade de atributos. |
| `DISSIPATION` | Tenta remover estágios positivos de atributos do inimigo; pode ser resistida. | Alvo inimigo; `power` vazio para todos ou positivo para até essa quantidade de atributos. |
| `SPECIAL_STATE` | Aplica o estado especial de Distorção Ancestral. | `SINGLE_ENEMY`, `specialStateType=DISTORTED_ATTRIBUTES`. |

### Dano, cura e sacrifício

`PHYSICAL` usa ATK como ataque padrão e enfrenta DEF; `MAGICAL` usa MAG como ataque padrão e enfrenta RES. Em `DAMAGE`, preencher `attribute` troca **só o atributo ofensivo**: `MAGICAL` com `attribute=ATK` continua enfrentando RES. O poder declarado não é o HP final removido: defesa, atributos efetivos, arma e outros efeitos influenciam o resultado. Para `HP_RECOVERY` e `REVIVE`, as opções de `attribute` continuam restritas a `ATK` ou `MAG`; MP e SP têm valores fixos.

`HP_SACRIFICE` usa uma porcentagem do HP **atual** e pode ser tentado mesmo com 1 HP: nesse caso perde 0. Não é recuo calculado a partir do dano no inimigo. `HP_BALANCE` soma os HP atuais dos aliados vivos, divide pela quantidade de vivos e arredonda para o inteiro mais próximo. Em seguida, define o HP de cada vivo como essa média, limitada ao máximo **daquele aliado**. Com aliados em `300/300` e `100/150`, a média é `200`: eles terminam com `200/300` e `150/150`. Derrotados ficam de fora. Se nenhum HP mudar e esse for o único efeito aplicável, a habilidade não produz uma tentativa válida.

### Modificador de dano e recuo

Em `PRE_ACTION`, `DAMAGE_POWER_MODIFIER` prepara bônus para danos posteriores compatíveis no mesmo alvo durante o uso da habilidade. Em `PRIMARY`, ele deve estar **imediatamente antes** de um `DAMAGE` também `PRIMARY` e com o mesmo alvo `SINGLE_ENEMY` ou `RANDOM_ENEMY`: o bônus vale somente para esse golpe. Se a condição do modificador falhar, o golpe ainda acontece, sem o bônus. Porcentagens de modificadores aplicáveis se somam; `50` representa +50% de poder, não 50 pontos de dano final.

`RECOIL_DAMAGE` deve vir imediatamente depois de um `DAMAGE` `PRIMARY` contra `SINGLE_ENEMY`, usando `SELF` como alvo. Seu `power=50` calcula metade do HP **realmente perdido pelo inimigo**. Se o golpe não retirar HP, não há recuo. O recuo nunca nocauteia o usuário; pode ter chance própria de ativação.

### Estágios, purificação e resistência

`ATTRIBUTE_STAGE` usa `stages` positivo para buff e negativo para debuff. Escreva `-1`, `-2` ou `-3` para queda; `1`, `2` ou `3` para aumento. Todo atributo exceto VEL precisa de `duration` positiva. VEL fica pendente para o próximo cálculo de rodada, portanto sua duração fica **vazia**. Uma purificação/dissipação com `power=2` escolhe aleatoriamente até dois **atributos afetados** e remove todos os estágios do sinal correspondente nesses atributos. Com `power` vazio, remove todos os atributos afetados. Debuffs e dissipação fazem teste de resistência; para dissipação, é uma tentativa de resistência por alvo e componente, mesmo quando vários atributos são removidos. Purificação, buffs, dano e recuperação não fazem esse teste. Distorção Ancestral tem regra própria, reversível, e não é apagada por purificação nem dissipação.

O `triggerChancePercent` é sorteado **uma vez por componente** quando sua condição e seus alvos permitem uma tentativa. Em alvos múltiplos, quem é resistível faz teste de resistência separadamente. Um trigger que falha ou uma resistência bem-sucedida ainda podem gastar o turno e os recursos; uma condição falsa ou ausência total de alvos afetáveis não constitui tentativa válida.

## Condições

O limite de HP é percentual do HP máximo. Comparações são **estritas**: `<50` não inclui exatamente 50%; `>50` também não inclui exatamente 50%. Se o efeito deve valer em 50% exatos ou abaixo, use `<51`; com máximo de HP que permita porcentagens fracionárias, esse ajuste também pode abranger valores entre 50% e 51%.

| `conditionType` | `conditionValue` | Quando passa |
|---|---|---|
| `NONE` | Vazio | Sempre, se houver alvo e efeito possível. |
| `SELF_HP_LESS_THAN_PERCENT` | `1` a `100` | HP do usuário é menor que o limite. |
| `SELF_HP_GREATER_THAN_PERCENT` | `1` a `100` | HP do usuário é maior que o limite. |
| `TARGET_HP_LESS_THAN_PERCENT` | `1` a `100` | HP do alvo é menor que o limite. |
| `TARGET_HP_GREATER_THAN_PERCENT` | `1` a `100` | HP do alvo é maior que o limite. |
| `SELF_GREATER` | `ATK`, `DEF`, `MAG`, `RES`, `VEL` ou `LCK` | Atributo efetivo do usuário é maior que o mesmo atributo efetivo do alvo. |
| `SELF_LOWER` | `ATK`, `DEF`, `MAG`, `RES`, `VEL` ou `LCK` | Atributo efetivo do usuário é menor que o mesmo atributo efetivo do alvo. |

Em uma condição `TARGET_...`, o alvo é avaliado quando **aquele componente** chega à vez: um dano anterior já pode ter reduzido seu HP. Para uma área `ALL_ENEMIES`, cada inimigo é avaliado separadamente. Nas condições `SELF_GREATER`/`SELF_LOWER`, o atributo escrito em `conditionValue` serve **apenas para a comparação**: `attribute` pode indicar outro atributo usado pelo efeito. Condição `NONE` exige `conditionValue` vazio.

## Receitas válidas — use uma habilidade por vez

As linhas a seguir são modelos independentes: o `skillId` indicado precisa existir em `skills.txt` e os outros efeitos daquela habilidade devem respeitar as mesmas regras de ordem. Os números ilustram o preenchimento; não são propostas de balanceamento.

**Dano mágico escalado por ATK e debuff depois do golpe:**

```text
exemplo_eco;1;PRIMARY;DAMAGE;SINGLE_ENEMY;MAGICAL;30;ATK;;;;;NONE;
exemplo_eco;2;POST_ACTION;ATTRIBUTE_STAGE;SINGLE_ENEMY;;;DEF;-1;2;;;TARGET_HP_LESS_THAN_PERCENT;51
```

**Três sorteios independentes; o terceiro recebe +30% se o inimigo sorteado estiver abaixo de 50%:**

```text
exemplo_tres_golpes;1;PRIMARY;DAMAGE;RANDOM_ENEMY;PHYSICAL;20;;;;;;NONE;
exemplo_tres_golpes;2;PRIMARY;DAMAGE;RANDOM_ENEMY;PHYSICAL;20;;;;;;NONE;
exemplo_tres_golpes;3;PRIMARY;DAMAGE_POWER_MODIFIER;RANDOM_ENEMY;;30;;;;;;TARGET_HP_LESS_THAN_PERCENT;50
exemplo_tres_golpes;4;PRIMARY;DAMAGE;RANDOM_ENEMY;PHYSICAL;20;;;;;;NONE;
```

**Modificador pontual no inimigo escolhido; o golpe ainda ocorre quando a condição falha:**

```text
exemplo_finalizador;1;PRIMARY;DAMAGE_POWER_MODIFIER;SINGLE_ENEMY;;50;;;;;;TARGET_HP_LESS_THAN_PERCENT;31
exemplo_finalizador;2;PRIMARY;DAMAGE;SINGLE_ENEMY;PHYSICAL;40;;;;;;NONE;
```

**Purificação limitada, dissipação limitada e purificação total** (três habilidades separadas):

```text
exemplo_limpeza;1;PRIMARY;PURIFICATION;SINGLE_ALLY;;2;;;;;;NONE;
exemplo_dissipar;1;PRIMARY;DISSIPATION;SINGLE_ENEMY;;2;;;;;;NONE;
exemplo_limpeza_total;1;PRIMARY;PURIFICATION;ALL_ALLIES;;;;;;;;NONE;
```

**Sacrifício seguido de um ataque; equilíbrio dos HP dos aliados vivos** (duas habilidades separadas):

```text
exemplo_sacrificio;1;PRIMARY;HP_SACRIFICE;SELF;;50;;;;;;NONE;
exemplo_sacrificio;2;PRIMARY;DAMAGE;SINGLE_ENEMY;MAGICAL;25;;;;;;NONE;
exemplo_equilibrio;1;PRIMARY;HP_BALANCE;ALL_ALLIES;;;;;;;;NONE;
```

**Cura escalada por MAG, regeneração fixa de SP, recuo e VEL pendente:**

```text
exemplo_cura;1;PRIMARY;HP_RECOVERY;SINGLE_ALLY;;30;MAG;;;;;NONE;
exemplo_folego;1;PRIMARY;SP_RECOVERY;ALL_ALLIES;;30;;;;;;NONE;
exemplo_investida;1;PRIMARY;DAMAGE;SINGLE_ENEMY;PHYSICAL;20;;;;;;NONE;
exemplo_investida;2;PRIMARY;RECOIL_DAMAGE;SELF;;50;;;;25;;NONE;
exemplo_agilidade;1;POST_ACTION;ATTRIBUTE_STAGE;SELF;;;VEL;2;;;;NONE;
```

**Comparação de VEL com dano físico baseado em LCK; ressurreição aleatória; estado especial** (três habilidades separadas):

```text
exemplo_duelo;1;PRIMARY;DAMAGE;SINGLE_ENEMY;PHYSICAL;25;LCK;;;;;SELF_GREATER;VEL
exemplo_retorno;1;PRIMARY;REVIVE;RANDOM_ALLY;;30;MAG;;;;;NONE;
exemplo_distorcao;1;PRIMARY;SPECIAL_STATE;SINGLE_ENEMY;;;;;;;DISTORTED_ATTRIBUTES;NONE;
```

No primeiro caso, `VEL` em `conditionValue` decide se o usuário é mais rápido que o alvo; `LCK` em `attribute` determina o ataque. A condição não troca o atributo ofensivo.

## Exemplos rejeitados e escolhas pouco recomendadas

**Rejeitados ao carregar** (fragmentos de linha; só as colunas relevantes aparecem):

| Trecho | Por que é inválido | Como ajustar |
|---|---|---|
| `HP_RECOVERY` com `attribute=VEL` | A cura escalada aceita ATK ou MAG. | Escolher `ATK` ou `MAG`. |
| `DAMAGE` com `stages=2` | Estágios pertencem a `ATTRIBUTE_STAGE`. | Separar o buff em outro componente. |
| `ATTRIBUTE_STAGE` em `VEL` com `duration=2` | VEL deixa a duração vazia. | Apagar a duração. |
| `ATTRIBUTE_STAGE` com `stages=0` | Zero não é estágio válido. | Usar de `-3` a `-1` ou de `1` a `3`. |
| `HP_BALANCE` com `power=50`, alvo `SINGLE_ALLY` ou condição | Usa `ALL_ALLIES`, sem poder e sem condição. | Calcular equilíbrio com a regra única de grupo. |
| `HP_SACRIFICE` com `power=0` ou alvo inimigo | Sacrifício exige porcentagem positiva até 100 e alvo `SELF`. | Usar `SELF`, `PRIMARY`, poder entre `1` e `100`. |
| `PURIFICATION` com `power=0` | Só aceita valor positivo ou campo vazio. | Deixar vazio para limpar todos. |
| `RECOIL_DAMAGE` sem `DAMAGE;SINGLE_ENEMY` imediatamente anterior | O recuo precisa da perda real de HP daquele golpe. | Colocá-lo logo após o dano compatível. |
| Modificador `PRIMARY;SINGLE_ENEMY` seguido de dano `RANDOM_ENEMY` | O par precisa ter o mesmo alvo e aparecer junto. | Usar `SINGLE_ENEMY` em ambos ou `RANDOM_ENEMY` em ambos. |
| `TARGET_HP_AT_MOST_PERCENT;50` | A condição antiga foi substituída pelas comparações estritas. | Usar `TARGET_HP_LESS_THAN_PERCENT;51` se quiser abranger 50% exatos. |
| `NONE;50` no fim da linha | `NONE` não aceita valor. | Terminar em `NONE;`. |
| 13 ou 15 campos separados por `;` | Toda linha de efeito precisa de 14 campos. | Revisar os separadores, inclusive os vazios finais. |

**Podem carregar, mas merecem atenção:**

- Uma habilidade cuja única ação é condicionada a `RANDOM_ALLY` pode sortear alguém fora da condição. Como não há novo sorteio quando ela falha, o jogador pode tentar de novo sem consumir recursos quando nada é aplicável. Prefira um efeito garantido adicional se essa não for a intenção.
- Um golpe aleatório seguido de um debuff `RANDOM_ENEMY` faz **dois sorteios independentes**: o debuff pode atingir outro inimigo. Use `SINGLE_ENEMY` nos componentes quando eles precisam seguir um alvo escolhido; para bônus pontual em ataque aleatório, use o par de modificador `PRIMARY` imediatamente antes de `DAMAGE`.
- Um `DAMAGE_POWER_MODIFIER` em `PRE_ACTION` pode fortalecer vários golpes posteriores compatíveis; se a descrição promete bônus só no próximo golpe, use a combinação `PRIMARY`.
- `POST_ACTION` acontece depois da redução de duração dos efeitos anteriores do usuário; escolha a fase conforme quando o novo bônus deve começar. VEL continua pendente para a próxima rodada.
- Ao combinar `HP_SACRIFICE` e outros efeitos, descreva o sacrifício como porcentagem do **HP atual**. Não prometa perda fixa, nem dano igual ao HP sacrificado, pois cada efeito tem suas próprias regras.
- Não use `ALL_BUT_TARGET`: esse alvo não está disponível. Para dano em área, use `ALL_ENEMIES`; para várias ações pontuais, escreva componentes separados.

## Checklist para revisão

- Os dois arquivos usam exatamente o mesmo ID, exclusivo, e a classe existe?
- `SCROLL` tem seu vínculo correspondente com o pergaminho? `LEVEL` cabe na progressão pretendida?
- Cada linha de efeito contém 14 campos, inclusive os vazios? Sequências crescem e fases não retrocedem?
- O efeito tem os campos que usa e deixa os demais vazios? O alvo é permitido para esse efeito?
- `DAMAGE` tem tipo e poder? Se usa outro atributo ofensivo, `damageType` ainda representa a defesa desejada?
- `HP_RECOVERY` e `REVIVE` escalam apenas por ATK ou MAG? MP/SP estão com `attribute` vazio?
- Buff/debuff tem estágios válidos? VEL ficou sem duração? Purificação/dissipação limitada usa `power` positivo?
- O modificador `PRIMARY` vem imediatamente antes do dano com mesmo alvo? O recuo vem imediatamente após dano `SINGLE_ENEMY`?
- As condições estritas descrevem o limite pretendido? `NONE` termina com valor vazio?
- A descrição diz quantos golpes acontecem, quando o alvo é sorteado e quando os efeitos condicionais podem ocorrer?

Depois de acrescentar conteúdo, carregue o jogo e teste ao menos o comportamento distintivo da habilidade em batalha. Se a configuração for rejeitada, confira primeiro o ID, o número de campos, o alvo, a fase e os valores obrigatórios.
