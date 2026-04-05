# DRUDS API — Fechamento Financeiro DJ

API REST em Spring Boot com PostgreSQL para gerenciar shows e calcular fechamentos mensais.

---

## Estrutura do projeto

```
druds-api/
├── pom.xml
└── src/main/
    ├── resources/
    │   └── application.properties        ← configuração do banco
    └── java/com/druds/
        ├── DrudsApplication.java         ← ponto de entrada Spring Boot
        ├── model/
        │   ├── Show.java                 ← entidade JPA (tabela: shows)
        │   ├── Pessoa.java               ← entidade JPA (tabela: pessoas)
        │   └── FechamentoMensal.java     ← entidade JPA (tabela: fechamentos_mensais)
        ├── dto/
        │   ├── ShowDTO.java              ← dados do show nas requisições/respostas
        │   └── FechamentoResponseDTO.java← resposta do fechamento calculado
        ├── repository/
        │   ├── ShowRepository.java       ← acesso ao banco (JPA)
        │   └── PessoaRepository.java
        ├── service/
        │   ├── ShowService.java          ← regras de negócio dos shows
        │   └── FechamentoService.java    ← cálculo do fechamento mensal
        ├── controller/
        │   ├── ShowController.java       ← endpoints REST /shows
        │   └── FechamentoController.java ← endpoint REST /fechamento
        └── exception/
            ├── ShowNotFoundException.java
            └── GlobalExceptionHandler.java
```

---

## Pré-requisitos

- Java 17+
- Maven 3.6+
- PostgreSQL instalado e rodando

---

## Passo a passo para rodar

### 1. Criar o banco de dados no PostgreSQL

Abra o psql ou pgAdmin e execute:

```sql
CREATE DATABASE druds_db;
```

### 2. Configurar o application.properties

Edite o arquivo `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/druds_db
spring.datasource.username=postgres
spring.datasource.password=SUA_SENHA_AQUI
```

### 3. Compilar e rodar

```bash
mvn clean package
java -jar target/druds-api-1.0.0.jar
```

Ou direto pelo Maven:

```bash
mvn spring-boot:run
```

### 4. Verificar se subiu

Acesse no navegador: http://localhost:8080/shows

Se retornar `[]` (array vazio), está funcionando!

---

## Endpoints disponíveis

### Shows

| Método | Endpoint      | Descrição           |
|--------|--------------|---------------------|
| POST   | /shows        | Cadastrar show      |
| GET    | /shows        | Listar todos        |
| GET    | /shows/{id}   | Buscar por ID       |
| DELETE | /shows/{id}   | Deletar show        |

### Fechamento

| Método | Endpoint                        | Descrição                    |
|--------|---------------------------------|------------------------------|
| GET    | /fechamento?mes=3&ano=2026      | Calcular fechamento do mês   |

---

## Exemplos de requisições

### Cadastrar um show

```bash
curl -X POST http://localhost:8080/shows \
  -H "Content-Type: application/json" \
  -d '{
    "data": "2026-03-06",
    "valorCache": 700.00,
    "local": "Chalezinho",
    "observacoes": "Show das 01:30"
  }'
```

Resposta:
```json
{
  "id": 1,
  "data": "2026-03-06",
  "valorCache": 700.00,
  "local": "Chalezinho",
  "observacoes": "Show das 01:30"
}
```

### Cadastrar show sem cachê definido

```bash
curl -X POST http://localhost:8080/shows \
  -H "Content-Type: application/json" \
  -d '{
    "data": "2026-03-07",
    "local": "Ouro Minas"
  }'
```

Sem `valorCache`, o sistema usa R$110 no cálculo.

### Calcular fechamento de março/2026

```bash
curl "http://localhost:8080/fechamento?mes=3&ano=2026"
```

Resposta:
```json
{
  "mes": 3,
  "ano": 2026,
  "quantidadeShows": 2,
  "totalBruto": 810.00,
  "totalDaniel": 186.50,
  "totalYuri": 600.00,
  "lucroLiquido": 23.50,
  "shows": [
    {
      "id": 1,
      "data": "2026-03-06",
      "valorCache": 700.00,
      "local": "Chalezinho",
      "observacoes": "Show das 01:30"
    },
    {
      "id": 2,
      "data": "2026-03-07",
      "valorCache": null,
      "local": "Ouro Minas",
      "observacoes": null
    }
  ]
}
```

---

## Regras de negócio implementadas

| Pessoa  | Regra                                                    |
|---------|----------------------------------------------------------|
| Daniel  | 15% do cachê + R$40 transporte **por dia** (não por show) |
| Yuri    | R$300 fixo por show                                      |
| Cachê   | Se não informado, usa R$110 como padrão                  |

> Detalhe importante: se Daniel fizer 2 shows no mesmo dia, recebe R$40 de transporte uma única vez (não R$80).

---

## O que acontece automaticamente

Com `spring.jpa.hibernate.ddl-auto=update`, o Spring Boot cria as tabelas no PostgreSQL sozinho ao iniciar. Você não precisa criar as tabelas manualmente.
