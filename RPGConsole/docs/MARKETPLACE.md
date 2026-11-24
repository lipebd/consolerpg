# Posto de Trocas

## Visão geral

O Posto de Trocas é acessado pelo menu das hospedarias e é um mercado global compartilhado por todas as cidades e por todas as instâncias do jogo conectadas à mesma `marketplace-api`.

O cliente mantém apenas os recursos da campanha:

- GOLD;
- inventário;
- UUID da campanha, persistido no save e enviado à API para identificar seu dono.

A API mantém o estado compartilhado:

- anúncios ativos, vendidos e cancelados;
- transações concluídas;
- recompensas pendentes e resgatadas.

Na V9.1, `save.txt` usa o formato 5 e inclui o identificador da campanha. Saves no formato 4 recebem um UUID ao serem carregados e gravam esse UUID no próximo salvamento. A campanha é salva antes de abrir o Posto. Cada criação de anúncio, compra, cancelamento e resgate concluído salva imediatamente os itens e o GOLD atualizados, enquanto anúncios e recompensas permanecem no banco da API. Se o salvamento inicial falhar, o Posto não abre. Se falhar após uma negociação, o jogador deve tentar salvar novamente antes de continuar; essa tentativa não repete a operação remota.

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

O H2 da V9.1 usa a pasta `marketplace-data/v9.1` no diretório de trabalho da API. Os registros da V9.0.0 ficam no banco anterior. Use o cliente e a API da mesma versão. Reiniciar a API não apaga os anúncios. A pasta é ignorada pelo Git.

## Concorrência

Compra, cancelamento e resgate abrem transações no servidor e bloqueiam o registro correspondente para escrita. Se dois jogadores tentarem comprar o mesmo anúncio, somente a primeira transação pode mudar o status de `ACTIVE` para `SOLD`; a segunda recebe conflito e não altera GOLD nem inventário.

## Endpoints

As rotas de anúncios e recompensas recebem o cabeçalho `X-Campaign-Id`. O nome do líder é exibido nos anúncios e o UUID do vendedor fica no servidor. A resposta de um anúncio informa se ele pertence à campanha que fez a consulta.

| Método | Endpoint | Função |
| --- | --- | --- |
| `GET` | `/api/health` | Verifica se a API está ativa |
| `GET` | `/api/listings` | Lista anúncios ativos |
| `GET` | `/api/listings?mine=true` | Lista os anúncios da campanha atual |
| `POST` | `/api/listings` | Cria um anúncio |
| `POST` | `/api/listings/{id}/purchase` | Compra um anúncio ativo |
| `POST` | `/api/listings/{id}/cancel` | Cancela um anúncio do vendedor |
| `GET` | `/api/rewards` | Lista recompensas pendentes da campanha atual |
| `POST` | `/api/rewards/{id}/claim` | Resgata uma recompensa |

## Limites do salvamento automático

O salvamento imediato reduz a possibilidade de duplicação por recarga após uma negociação confirmada. A operação no servidor e o arquivo local continuam sem uma transação única: perda de resposta, encerramento forçado antes do save e restauração de backups antigos ainda podem causar divergências. A API foi integrada para uso local. A API compara o UUID recebido com o dono registrado. O fluxo local segue sem autenticação de contas.
