# DJFinance — Back-end

API REST do sistema de fechamento financeiro dos DJs **DRUDS** e **Braichi**: shows, agenda,
estatísticas do CRM e fechamento mensal com o quanto cada pessoa recebe.

Spring Boot 3.2 · Java 17 · PostgreSQL · deploy no Railway (push na `main` publica).

## Como rodar

1. Crie o banco: `CREATE DATABASE druds_db;`
2. Defina a senha do banco no ambiente (ela não fica no código):
   ```bash
   export SPRING_DATASOURCE_PASSWORD=sua_senha
   ```
   `SPRING_DATASOURCE_URL` e `SPRING_DATASOURCE_USERNAME` também podem ser definidas;
   sem elas, o padrão é `jdbc:postgresql://localhost:5432/druds_db` com o usuário `postgres`.
3. Rode: `mvn spring-boot:run` (sobe em http://localhost:8080).

Testes: `mvn test`. Eles não precisam de banco.

## Organização do código (`src/main/java/com/druds`)

| Pasta | O que tem |
|---|---|
| `controller/` | Endpoints HTTP. Só recebem a requisição e chamam um service. |
| `service/` | Regras do sistema (ver abaixo). |
| `repository/` | Consultas ao banco (Spring Data JPA). |
| `model/` | Entidades (`Show`, `BloqueioAgenda`) e constantes de domínio (`Dj`, `StatusShow`). |
| `dto/` | Formatos que trafegam na API. |
| `exception/` | Erros de negócio e como viram resposta HTTP. |

### Services

| Classe | Responsabilidade |
|---|---|
| `RegrasPagamento` | **Quanto Daniel e Yuri recebem em cada show.** Única fonte dessas regras; o front só exibe. |
| `FechamentoService` | Fechamento do mês: bruto, pagamentos, custos, produtor, imposto e lucro. |
| `ShowService` / `ShowMapper` | Cadastro, edição, listagem e remoção de shows, com espelho no Google Agenda. |
| `CorrecaoStatusAgendada` | Ao subir e toda madrugada, acerta o status pela data (futuro = PENDENTE, passado = CONFIRMADO). |
| `ImportacaoService` / `PlanilhaShowsParser` | Importação da planilha CSV, sem duplicar shows já cadastrados. |
| `BloqueioAgendaService` | Períodos em que o DJ não aceita shows. |
| `NormalizacaoService` | Junta grafias diferentes do mesmo local ou contratante (CRM). |
| `StatsService` | Estatísticas por contratante e por local (CRM). |
| `GoogleCalendarService` | Cria, atualiza e apaga eventos na agenda do DJ (conta de serviço; falhas não impedem salvar). |

## Regras de pagamento (resumo)

Detalhes e exemplos em `RegrasPagamento` e `FechamentoServiceTest`.

- **DRUDS, Daniel:** R$ 50 por show em 2025 (a partir de março); a partir de 2026, 10% (jan–mar)
  ou 20% (abr em diante) de cachê − custos, mais R$ 40 de transporte por dia com show.
  Show sem cachê preenchido paga R$ 70. "Sem cachê Daniel" marcado: 10% do cachê bruto, sem transporte.
- **DRUDS, Yuri:** R$ 250 por show a partir de março/2025, salvo "Sem cachê Yuri".
- **Braichi:** Daniel recebe 10% do cachê bruto; não há Yuri; o produtor externo entra no fechamento.

Shows e bloqueios antigos sem DJ gravado pertencem ao DRUDS.

## Endpoints

Todos aceitam `?dj=DRUDS|BRAICHI` (padrão DRUDS).

| Método | Caminho | O que faz |
|---|---|---|
| GET | `/shows` | Lista os shows do DJ, cada um com `pagamentoDaniel` e `pagamentoYuri` |
| POST | `/shows` | Cadastra (409 se a data estiver bloqueada) |
| PUT | `/shows/{id}` | Edita |
| DELETE | `/shows/{id}` | Remove (e apaga o evento do Google Agenda) |
| DELETE | `/shows/duplicados` | Remove shows repetidos (mesma data e evento) |
| POST | `/shows/importar` | Importa a planilha CSV (campo `arquivo`) |
| PUT | `/shows/normalizar-local` · `/shows/normalizar-contratante` | Troca `de` por `para` em todos os shows do DJ |
| GET | `/fechamento?mes=&ano=&imposto=` | Fechamento do mês (`imposto` em %) |
| GET | `/stats/contratantes` · `/stats/locais` | Estatísticas do CRM |
| GET · POST · DELETE | `/bloqueios` · `/bloqueios/{id}` | Bloqueios de agenda |

Erros de validação voltam como 400 e data bloqueada como 409, sempre com `mensagem`.

## Google Agenda

Desligado por padrão. Para ligar, defina no ambiente `GOOGLE_CALENDAR_ENABLED=true`,
`GOOGLE_CREDENTIALS_JSON` (JSON inteiro da conta de serviço), `GOOGLE_CALENDAR_DRUDS` e
`GOOGLE_CALENDAR_BRAICHI` (IDs das agendas, compartilhadas com o e-mail da conta de serviço).
Shows importados por CSV não são enviados à agenda; para enviar, edite e salve o show.
