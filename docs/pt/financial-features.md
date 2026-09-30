---
layout: default
lang: pt
base: "/pt"
key: "financial-features"
title: Declaração de recursos financeiros
class: doc
---
# Declaração de recursos financeiros

<p class="meta">Respostas para o formulário do Google Play Console (Política e programas → Conteúdo do app → Recursos financeiros), com a justificativa. Revisadas com base na versão 2.2.1 em 30 de setembro de 2026.</p>

## Resposta do formulário

**Select all of the financial features the app provides** (Selecione todos os recursos financeiros oferecidos pelo app)**:** **The app does not provide any financial features** (O app não oferece nenhum recurso financeiro)**.**

## Por quê

O Capital é um app pessoal para acompanhar economias. Ele registra o que o usuário já possui e mostra como essas economias se distribuem entre as metas do próprio usuário. Comparação com cada recurso do formulário:

| Recurso no formulário | Capital |
|---|---|
| Personal loan direct lender, loan facilitator, payday loans, line of credit, earned wage advances, microfinance, buy now pay later (credor direto de empréstimo pessoal, intermediador de empréstimos, empréstimos de curto prazo, linha de crédito, adiantamento salarial, microfinanças, compre agora e pague depois) | Nenhum tipo de empréstimo |
| Banking (serviços bancários) | Sem contas, depósitos ou acesso a contas. Os saldos bancários são digitados pelo usuário |
| Mobile payments and digital wallets, money transfer and wire services (pagamentos móveis e carteiras digitais, transferência de dinheiro e remessas) | Não consegue enviar, receber nem guardar dinheiro. A alocação para metas é um cálculo exibido na tela; não movimenta nada |
| Cryptocurrency wallet (carteira de criptomoedas) | Lê o saldo de endereços públicos colados pelo usuário. Nunca guarda chaves privadas nem frases-semente e não consegue assinar nem transmitir transações, portanto não é uma carteira |
| Cryptocurrency exchange (corretora de criptomoedas) | Sem negociação, sem roteamento de ordens, sem entrada de moeda fiduciária |
| Rewards and incentives, crowdfunding and chit funds, prediction markets (recompensas e incentivos, financiamento coletivo e consórcios, mercados de previsão) | Nenhum |
| Credit monitoring and reporting (monitoramento e relatórios de crédito) | Nenhum |
| Financial advice (consultoria financeira) | Nenhuma. A projeção apresenta cálculos aritméticos sobre os números do próprio usuário ("as economias planejadas concluem esta meta em …"); não recomenda nenhum produto, ativo ou ação. A calculadora de rebalanceamento lista as compras necessárias para atingir percentuais definidos pelo próprio usuário |
| Insurance (seguros) | Nenhum |
| In-app purchases, donations (compras no app, doações) | Nenhuma processada pelo app. Uma tela de gorjetas mostra os endereços públicos de carteira do desenvolvedor (os mesmos deste site); a transferência acontece no app de carteira do próprio usuário, não desbloqueia nada e é invisível para o app |

O app também não oferece compras no app nem recursos pagos. A versão do Google Play é gerada sem a tela de gorjetas.

## Se o revisor discordar

Se a revisão do Play classificar o app como oferecendo algum recurso financeiro mesmo assim, a opção mais próxima é **Other** (Outro) com esta descrição:

> Read-only personal savings tracker. Users type in their balances or paste public blockchain addresses; the app fetches balances and market prices from third-party data sources and shows how the savings cover the user's own goals. No custody, no keys, no transactions, no lending, no trading, no advice.

Tradução: app pessoal, somente leitura, para acompanhar economias. Os usuários digitam seus saldos ou colam endereços públicos de blockchain; o app obtém saldos e preços de mercado de fontes de dados terceiras e mostra como as economias cobrem as metas do próprio usuário. Sem custódia, sem chaves, sem transações, sem empréstimos, sem negociação, sem consultoria.

Os requisitos específicos por país para apps de empréstimo pessoal e as perguntas sobre criptomoedas para os Estados Unidos não se aplicam, porque nenhum desses recursos foi selecionado.

## Fatos relacionados que um revisor pode perguntar

- Os dados de mercado vêm de operadores terceiros selecionados pelo usuário (veja a [Política de Privacidade]({{ page.base }}/privacy)). O app mostra o nome e o site do operador em Ajustes.
- As consultas de carteiras usam APIs públicas de blockchain, somente leitura.
- O app roda inteiramente no dispositivo e não tem servidor operado pelo desenvolvedor.
