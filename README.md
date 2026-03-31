# DRUDS - Fechamento Financeiro DJ

## Commit 1 — Estrutura base e modelos de domínio

### O que foi adicionado
- `Show.java` — representa um show da agenda (data, evento, cachê, contratante etc.)
- `Pessoa.java` — representa um membro da equipe com tipo de pagamento (% ou fixo)
- `FechamentoMensal.java` — agrupa os resultados calculados de um mês
- `Main.java` — ponto de entrada inicial

### Como rodar
```bash
mvn clean package
java -jar target/druds-fechamento.jar
```

### Mensagem de commit sugerida
```
feat: estrutura base e modelos de domínio (Show, Pessoa, FechamentoMensal)
```
