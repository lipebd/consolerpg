# Escopo da Seleção de Alvos dos Inimigos

> **Historical brainstorming, not an implementation contract.**
> This document predates the V7 Part 9 study. For the current recommendation and open decisions, see `V7_ROADMAP.md` and `V8_ROADMAP_DRAFT.md`.

## TL;DR

A seleção de alvos dos inimigos utilizará um único sistema de pontuação compartilhado por todos eles.

Cada inimigo definirá duas propriedades independentes em `enemies.txt`:

- `targetAwareness`: determina quais informações do campo de batalha o inimigo consegue avaliar.
- `targetProfile`: determina a importância que o inimigo atribui a cada fator disponível.

Os inimigos adquirirão alvos por meio de uma seleção ponderada, mas se lembrarão do alvo escolhido e normalmente continuarão atacando-o. A troca acontecerá somente quando o alvo atual for derrotado ou quando outro personagem se tornar significativamente mais atraente.

Isso permitirá que inimigos simples, como slimes, ajam de maneira instintiva, enquanto inimigos treinados e bosses poderão explorar HP, estado defensivo e defesas relevantes. A implementação permanecerá limitada a um seletor e dois enums, sem árvores de comportamento, subclasses de estratégia ou um sistema genérico de eventos de combate.

---

## Objetivos

O sistema de seleção de alvos deve:

- Fazer com que as decisões dos inimigos pareçam intencionais, em vez de completamente aleatórias.
- Preservar alguma imprevisibilidade para impedir que o comportamento seja explorado com total segurança.
- Produzir diferenças perceptíveis entre criaturas simples, inimigos treinados e bosses.
- Permitir que inimigos do mesmo rank apresentem comportamentos diferentes.
- Utilizar os atributos efetivos e estados de combate já mantidos pelo sistema.
- Permanecer simples de inspecionar, testar e balancear.
- Suportar futuras habilidades de inimigos sem exigir que a implementação atual antecipe todo o sistema de ações deles.

## Fora do escopo

A implementação inicial não incluirá:

- Árvores de comportamento.
- Uma classe de estratégia de alvo para cada personalidade.
- Distância, formação ou posicionamento no campo de batalha.
- Conhecimento direto das classes jogáveis ou de funções como tanque, curandeiro e mago.
- Histórico de dano, cura ou acertos críticos.
- Um sistema genérico de eventos ou ameaça.
- Provocação ou seleção forçada de alvo.
- Seleção de habilidades ou ações dos inimigos.
- Coordenação entre vários inimigos.
- Modificadores aleatórios aplicados à pontuação a cada turno.
- Determinação automática da inteligência a partir de `EnemyRank`.

Essas mecânicas poderão ser consideradas futuramente quando ações concretas dos inimigos exigirem sua existência.

---

## Dados externos dos inimigos

Duas colunas obrigatórias serão adicionadas a `enemies.txt`:

```text
...;targetAwareness;targetProfile
```

As duas propriedades pertencerão a `EnemyDefinition` e permanecerão imutáveis durante a batalha.

O loader deverá:

- Rejeitar valores desconhecidos de percepção.
- Rejeitar valores desconhecidos de perfil.
- Exigir os dois campos em todas as definições de inimigo.
- Preservar o comportamento atual de validação dos demais campos.

`EnemyRank` permanecerá separado do comportamento de seleção de alvos.

O rank descreve a posição, dificuldade e classificação de recompensas do inimigo. Ele não deve determinar automaticamente sua inteligência ou personalidade. Isso preserva combinações como:

- Um boss poderoso, porém instintivo.
- Um inimigo comum fisicamente fraco, mas taticamente treinado.
- Uma fera oportunista.
- Um líder implacável que se recusa a trocar de alvo.

---

## Percepção de alvo

`EnemyTargetAwareness` definirá quais informações o inimigo é capaz de avaliar.

### `INSTINCTIVE`

O inimigo sabe apenas:

- Quais membros do grupo estão vivos.
- Qual alvo ele está atacando atualmente.

Comportamento:

- A aquisição inicial é uniforme entre os personagens vivos.
- O alvo escolhido é preservado enquanto estiver vivo.
- O inimigo não troca voluntariamente de alvo.
- Um novo alvo é adquirido somente quando o atual se torna inválido.

Exemplos pretendidos:

- Slimes.
- Criaturas irracionais.
- Monstros extremamente simples.

### `OPPORTUNISTIC`

O inimigo também avalia:

- Percentual atual de HP.
- Se o alvo está defendendo.

Comportamento:

- Personagens feridos tornam-se mais atraentes.
- Um personagem defendendo torna-se menos atraente.
- O inimigo pode trocar de alvo quando uma alternativa supera o alvo atual pela margem exigida.
- Pequenas variações na pontuação não devem provocar trocas constantes.

Exemplos pretendidos:

- Lobermos e outras criaturas caçadoras.
- Bandidos comuns.
- Inimigos capazes de reconhecer oportunidades evidentes.

### `TACTICAL`

O inimigo avalia tudo que está disponível para inimigos `OPPORTUNISTIC` e também:

- DEF efetiva ao executar dano físico.
- RES efetiva ao executar dano mágico.

Comportamento:

- O inimigo consegue explorar a fraqueza defensiva relevante para o ataque atual.
- Buffs, debuffs e estados especiais são refletidos naturalmente nos valores efetivos dos atributos.
- O seletor não precisa inspecionar nem compreender os efeitos de combate individualmente.

Exemplos pretendidos:

- Combatentes treinados.
- Inimigos de elite.
- Estrategistas.
- A maioria dos bosses inteligentes.

---

## Perfis de alvo

`EnemyTargetProfile` determinará a importância atribuída pelo inimigo a cada fator disponível por meio de seu nível de percepção.

Os perfis iniciais serão:

### `BALANCED`

- Considera ferimento e vulnerabilidade defensiva sem favorecer excessivamente nenhum dos dois.
- Utiliza uma margem moderada para troca de alvo.
- Funciona como perfil de uso geral.

### `PREDATOR`

- Dá maior importância ao HP perdido.
- Persegue alvos feridos com mais facilidade.
- Utiliza uma margem de troca menor que `BALANCED`.

### `VULNERABILITY_HUNTER`

- Dá maior importância à defesa relevante mais baixa.
- Em ataques físicos, favorece alvos com menor DEF efetiva.
- Em ataques mágicos, favorece alvos com menor RES efetiva.
- Possui efeito prático limitado abaixo da percepção `TACTICAL`.

### `RELENTLESS`

- Favorece fortemente a permanência no alvo atual.
- Utiliza uma margem elevada para troca.
- É adequado para inimigos que fixam sua atenção em um oponente.

Os coeficientes dos perfis e suas margens de troca permanecerão internos ao enum ou à implementação da seleção de alvos. Eles não se tornarão colunas adicionais em `enemies.txt`.

Os valores numéricos exatos serão ajustados por meio de testes determinísticos e observação manual das batalhas.

Exemplos de combinações:

```text
Slime:
INSTINCTIVE + RELENTLESS

Lobermo:
OPPORTUNISTIC + PREDATOR

Soldado treinado:
TACTICAL + BALANCED

Assassino:
TACTICAL + PREDATOR

Boss estrategista:
TACTICAL + VULNERABILITY_HUNTER
```

---

## Pontuação dos alvos

Somente membros vivos do grupo são candidatos válidos.

Todo candidato válido recebe uma pontuação-base positiva. Componentes adicionais são habilitados de acordo com a percepção do atacante e ponderados conforme seu perfil.

Os fatores iniciais de pontuação serão:

- Percentual de HP perdido.
- Penalidade por estado defensivo.
- Vulnerabilidade defensiva relevante.

### Percentual de HP perdido

O valor deve ser baseado na proporção de HP perdido, não no valor absoluto de HP atual.

Isso impede que personagens com HP máximo naturalmente menor sejam sempre tratados como feridos apenas porque possuem um valor bruto de HP mais baixo.

### Estado defensivo

Um personagem que esteja defendendo recebe uma penalidade em sua pontuação.

Isso permite que inimigos oportunistas e táticos reconheçam que outro alvo pode ser mais fácil de atingir.

Uma consulta somente de leitura, como `isDefending()`, poderá ser adicionada a `GameCharacter` caso ainda não exista uma operação pública equivalente.

### Vulnerabilidade defensiva relevante

Somente inimigos `TACTICAL` utilizam esse fator.

O atributo avaliado depende do tipo de dano da ação executada:

- `PHYSICAL` utiliza DEF efetiva.
- `MAGICAL` utiliza RES efetiva.

Quanto menor a defesa relevante, maior será a pontuação do alvo.

O seletor deve utilizar as consultas existentes de atributos efetivos. Dessa forma, estágios, buffs, debuffs e Distorção Ancestral já estarão refletidos nos valores recebidos. Ele não deve reproduzir os cálculos dos efeitos de combate.

A pontuação final de cada candidato deve permanecer em pelo menos um, garantindo que nenhum alvo válido se torne matematicamente impossível durante a aquisição ponderada.

---

## Aquisição de alvo

Quando um inimigo não possui um alvo atual válido:

1. Todos os membros vivos do grupo são reunidos.
2. A pontuação de cada candidato é calculada.
3. As pontuações são utilizadas como pesos relativos.
4. Um candidato é selecionado com o `Random` já compartilhado pela batalha.
5. O alvo escolhido é armazenado para aquela instância específica de inimigo.

Uma pontuação maior aumenta a chance de seleção, mas não garante o resultado.

Quando todos os candidatos possuem pontuações iguais, a aquisição é efetivamente uniforme.

A aleatoriedade é utilizada durante a aquisição de alvo, não como uma nova escolha irrestrita a cada ação do inimigo.

---

## Persistência e troca de alvo

Cada instância de inimigo se lembra de seu alvo atual de maneira independente.

Dois inimigos criados a partir da mesma `EnemyDefinition` não devem compartilhar memória de alvo.

### Alvo inválido

O alvo atual se torna inválido quando é derrotado ou removido da lista de candidatos válidos por qualquer outro motivo.

Nesse caso, o inimigo realiza uma nova aquisição ponderada.

### Troca voluntária

Inimigos `INSTINCTIVE` nunca trocam voluntariamente de alvo.

Inimigos `OPPORTUNISTIC` e `TACTICAL` podem reconsiderar sua escolha no momento de agir.

O seletor calcula:

- A pontuação do alvo atual.
- A maior pontuação entre as alternativas.

O inimigo troca de alvo somente quando:

```text
bestAlternativeScore
    > currentTargetScore + retargetMargin
```

A margem é definida pelo perfil de alvo do inimigo.

Essa margem produz persistência de alvo sem exigir um bônus separado para o alvo atual. Pequenas mudanças no campo de batalha, portanto, não fazem os inimigos alternarem constantemente entre personagens.

A troca voluntária é determinística depois que as pontuações são calculadas. Nenhum segundo sorteio é realizado apenas para decidir se o inimigo mudará de alvo.

---

## Dependência do tipo de dano

A avaliação dos alvos deve corresponder à ação que realmente será executada.

Conceitualmente, a seleção segue esta ordem:

```text
selecionar a ação do inimigo
    -> determinar o tipo de dano e os requisitos de alvo
    -> selecionar um alvo válido
    -> executar a ação
```

Na implementação atual, os ataques básicos dos inimigos são físicos. Portanto, o seletor poderá receber diretamente `DamageType.PHYSICAL`.

Quando habilidades ou ataques mágicos dos inimigos forem implementados, a ação escolhida fornecerá o tipo de dano relevante.

Ações que não exigem um único alvo adversário devem ignorar esse seletor. Exemplos:

- Ações aplicadas ao próprio usuário.
- Ações direcionadas a aliados.
- Ataques em área contra todo o grupo.

O escopo atual não deve criar uma hierarquia especulativa de ações de inimigos apenas para preparar essa integração futura.

---

## Responsabilidade em runtime

A memória dos alvos existe somente durante uma batalha.

A batalha possui um único `EnemyTargetSelector`, criado com o `Random` compartilhado que ela já utiliza.

O seletor armazena o alvo atual associado a cada instância de `Enemy`. Quando a batalha termina, o seletor e toda sua memória de alvos são descartados.

O estado dos alvos não será:

- Armazenado em `EnemyDefinition`.
- Compartilhado globalmente.
- Preservado entre batalhas.
- Persistido externamente.
- Adicionado a `EnemyCatalog`.

---

## Arquitetura mínima

A implementação inicial deve exigir somente:

- `EnemyTargetSelector`
- `EnemyTargetAwareness`
- `EnemyTargetProfile`
- Dois novos campos imutáveis em `EnemyDefinition`
- Suporte correspondente no loader
- Uma consulta somente de leitura ao estado de defesa, caso necessária
- Integração com o fluxo atual do turno dos inimigos em `Battle`

A implementação não deve introduzir objetos separados como:

- `TargetScore`
- `TargetCandidate`
- `TargetDecision`
- Uma subclasse de seletor para cada percepção ou perfil
- Uma implementação de estratégia para cada tipo de inimigo

As pontuações dos candidatos podem permanecer como valores locais dentro de `EnemyTargetSelector`.

---

## Validações e testes

A implementação deverá verificar:

- Somente membros vivos do grupo podem ser selecionados.
- Um grupo com apenas um membro vivo sempre produz esse membro como alvo.
- A aquisição inicial respeita corretamente os pesos.
- Pontuações iguais produzem aquisição uniforme.
- Instâncias de `Random` com seed produzem testes reproduzíveis.
- Cada instância de inimigo preserva seu próprio alvo.
- Dois inimigos baseados na mesma definição não compartilham estado de alvo.
- Inimigos `INSTINCTIVE` preservam um alvo vivo independentemente das mudanças de pontuação.
- Inimigos `OPPORTUNISTIC` reagem ao percentual de HP e ao estado de defesa.
- Ataques físicos de inimigos `TACTICAL` avaliam DEF efetiva.
- Ataques mágicos de inimigos `TACTICAL` avaliam RES efetiva.
- Buffs e debuffs afetam a avaliação tática por meio dos atributos efetivos.
- Pequenas variações de pontuação não ultrapassam a margem de troca.
- Mudanças significativas de pontuação podem provocar troca de alvo.
- A derrota do alvo atual provoca uma nova aquisição imediata.
- Todo candidato válido preserva um peso positivo.
- O comportamento de alvo não é inferido a partir de `EnemyRank`.

Os testes manuais devem incluir:

- Uma slime preservando o alvo adquirido inicialmente.
- Um predador mudando o foco para um personagem gravemente ferido.
- Um atacante físico tático favorecendo um alvo vulnerável em DEF.
- Um atacante mágico tático favorecendo um alvo vulnerável em RES.
- Um inimigo implacável mantendo a pressão apesar da existência de alternativas moderadamente melhores.
- Vários inimigos escolhendo e preservando alvos independentes.

---

## Extensões futuras

As seguintes informações poderão futuramente se tornar fatores adicionais de pontuação:

- Dano recente causado ao inimigo.
- Cura realizada recentemente.
- Acertos críticos.
- Provocação ou seleção forçada.
- Ameaça gerada por habilidades específicas.
- Marcas temporárias de alvo.
- Coordenação entre inimigos.

Essas adições deverão ser motivadas por mecânicas concretamente implementadas.

Quando a provocação for implementada, ela deverá sobrescrever a pontuação normal, em vez de funcionar como um pequeno bônus.

A memória de ações recentes deverá permanecer interna ao sistema de seleção de alvos da batalha, a menos que uma abstração mais ampla de eventos de combate se torne necessária para vários sistemas independentes.

---

## Resultado esperado

Inimigos simples agirão de maneira consistente sem aparentar pensamento tático. Inimigos oportunistas reconhecerão personagens feridos ou expostos. Inimigos táticos explorarão a defesa relevante para o ataque escolhido. Os perfis criarão preferências diferentes sem duplicar o algoritmo de seleção.

Os bosses poderão, portanto, tomar decisões de alvo significativamente melhores que criaturas comuns, sem depender apenas de HP e atributos maiores.

A seleção de alvos fornecerá uma dimensão da inteligência dos inimigos. Futuramente, a escolha de ações e habilidades poderá fornecer outra, mas esse sistema permanece fora do escopo atual.