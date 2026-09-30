# Posto de Trocas

## Visão geral

O Posto de Trocas é um mercado global compartilhado por todas as cidades e por todas as instâncias do jogo conectadas à mesma `marketplace-api`.

O cliente mantém apenas os recursos da campanha:

- GOLD;
- inventário;
- identidade do jogador, obtida pelo nome do líder permanente da campanha.

A API mantém o estado compartilhado:

- anúncios ativos, vendidos e cancelados;
- transações concluídas;
- recompensas pendentes e resgatadas.

Por isso, `save.txt` continua usando o formato existente da V5. Os itens e o GOLD alterados pelo Posto são salvos normalmente na próxima hospedaria, enquanto anúncios e recompensas permanecem no banco da API.

## Executando localmente

1. Abra `marketplace-api` como projeto Maven e execute `MarketplaceApiApplication`.
2. Confirme `http://localhost:8080/api/health` no navegador.
3. Inicie `RPGConsole` normalmente.
4. Entre em qualquer cidade e escolha `Posto de Trocas`.

No Windows, a API também pode ser iniciada na pasta `marketplace-api` com:

```bat
mvnw.cmd spring-boot:run
```

O cliente usa `http://localhost:8080` por padrão. Outro endereço pode ser informado ao iniciar o Java:

```text
-Drpg.marketplace.api.url=http://servidor:8080
```

Para testes rápidos, a opção `-dev-party` cria uma party em Bree com 5000G, dez Merumãs e cinco unidades de Ferro.

## Persistência da API

O H2 usa arquivos dentro de `marketplace-api/marketplace-data`. Reiniciar a API não apaga os anúncios. A pasta é ignorada pelo Git.

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
