# Posto de Trocas

## Visão geral

O Posto de Trocas é acessado pelo menu das hospedarias e é um mercado global compartilhado por todas as cidades e por todas as instâncias do jogo conectadas à mesma `marketplace-api`.

O cliente mantém apenas os recursos da campanha:

- GOLD;
- inventário;
- identidade do jogador, obtida pelo nome do líder permanente da campanha.

A API mantém o estado compartilhado:

- anúncios ativos, vendidos e cancelados;
- transações concluídas;
- recompensas pendentes e resgatadas.

Na V9, `save.txt` continua usando o formato de salvamento 4 da V8. A campanha é salva antes de abrir o Posto. Cada criação de anúncio, compra, cancelamento e resgate concluído salva imediatamente os itens e o GOLD atualizados, enquanto anúncios e recompensas permanecem no banco da API. Se o salvamento inicial falhar, o Posto não abre. Se falhar após uma negociação, o jogador deve tentar salvar novamente antes de continuar; essa tentativa não repete a operação remota.

## Executando localmente

1. Com Java 25, abra `marketplace-api` como projeto Maven e execute `MarketplaceApiApplication`.
2. Confirme `http://localhost:8080/api/health` no navegador.
3. Inicie `RPGConsole` normalmente.
4. Entre em uma cidade, escolha `Visitar um local`, entre na hospedaria e selecione `[4] Posto de Trocas`.

No Windows, a API também pode ser iniciada na pasta `marketplace-api` com:

```bat
mvnw.cmd spring-boot:run
```

O cliente usa `http://localhost:8080` por padrão. Outro endereço pode ser informado ao iniciar o Java:

```text
-Drpg.marketplace.api.url=http://servidor:8080
```

Com o JAR executável da versão, inicie a API em um terminal na pasta onde deseja guardar o banco:

```bat
java -jar marketplace-api-0.0.1-SNAPSHOT.jar --server.address=127.0.0.1
```

Para testar duas campanhas, use nomes de líder diferentes e um `save.txt` para cada uma. O argumento `--dev-party` é destinado a sessões de desenvolvimento.

## Persistência da API

O H2 usa a pasta `marketplace-data` no diretório de trabalho da API. Reiniciar a API não apaga os anúncios. A pasta é ignorada pelo Git.

## Concorrência

Compra, cancelamento e resgate abrem transações no servidor e bloqueiam o registro correspondente para escrita. Se dois jogadores tentarem comprar o mesmo anúncio, somente a primeira transação pode mudar o status de `ACTIVE` para `SOLD`; a segunda recebe conflito e não altera GOLD nem inventário.

## Endpoints

| Método | Endpoint | Função |
| --- | --- | --- |
| `GET` | `/api/health` | Verifica se a API está ativa |
| `GET` | `/api/listings` | Lista anúncios ativos |
| `GET` | `/api/listings?sellerId=...` | Lista os anúncios de um vendedor |
| `POST` | `/api/listings` | Cria um anúncio |
| `POST` | `/api/listings/{id}/purchase` | Compra um anúncio ativo |
| `POST` | `/api/listings/{id}/cancel` | Cancela um anúncio do vendedor |
| `GET` | `/api/rewards?sellerId=...` | Lista recompensas pendentes |
| `POST` | `/api/rewards/{id}/claim` | Resgata uma recompensa |

## Limites do salvamento automático

O salvamento imediato reduz a possibilidade de duplicação por recarga após uma negociação confirmada. A operação no servidor e o arquivo local continuam sem uma transação única: perda de resposta, encerramento forçado antes do save e restauração de backups antigos ainda podem causar divergências. A API foi integrada para uso local. A identidade usada pelo servidor é o nome do líder da campanha, sem autenticação.
