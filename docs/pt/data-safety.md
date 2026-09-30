---
layout: default
lang: pt
base: "/pt"
key: "data-safety"
title: Declaração de segurança dos dados
class: doc
---
# Declaração de segurança dos dados

<p class="meta">Respostas para o formulário do Google Play Console (Política e programas → Conteúdo do app → Segurança dos dados), com a justificativa de cada uma. Revisadas com base na versão 2.2.1 em 30 de setembro de 2026. A <a href="{{ page.base }}/privacy">Política de Privacidade</a> é a declaração dos mesmos fatos voltada ao usuário.</p>

## Como o app trata os dados

O Capital não tem back-end. Tudo o que o usuário informa fica em uma pasta no dispositivo. Os únicos dados que saem do dispositivo são os que o app envia, por instrução do usuário, aos operadores de dados terceiros que o usuário seleciona em Ajustes: endereços públicos de carteiras, IDs de contratos de tokens, códigos de moeda e qualquer chave de API que o usuário tenha informado para aquele operador. Os operadores respondem à solicitação; o app armazena localmente os saldos e preços retornados e não guarda nenhuma cópia da solicitação. Nenhum SDK do app envia dados ao fabricante: as dependências são apenas AndroidX, Kotlin, OkHttp e Bouncy Castle.

O Google Play considera dados como *coletados* quando são transmitidos para fora do dispositivo, mesmo que nenhum servidor do desenvolvedor esteja envolvido e o processamento seja efêmero; por isso, a declaração não é "não coleta nada". Trata-se de um único tipo de dado, efêmero e opcional.

## Respostas do formulário

### Visão geral

| Pergunta | Resposta |
|---|---|
| O app coleta ou compartilha algum dos tipos de dados do usuário obrigatórios? (Does your app collect or share any of the required user data types?) | **Sim** |
| Todos os dados do usuário coletados pelo app são criptografados em trânsito? (Is all of the user data collected by your app encrypted in transit?) | **Sim** — somente HTTPS; o tráfego em texto não criptografado está desativado no manifesto |
| Você oferece uma maneira de os usuários solicitarem a exclusão dos dados? (Do you provide a way for users to request that their data is deleted?) | **Sim** — nada é retido após a conclusão da solicitação, o que atende à regra de "excluídos em até 90 dias após a coleta" exigida para o selo. Os usuários excluem os dados do dispositivo apagando a pasta e desinstalando o app; veja a Política de Privacidade. |

### Tipos de dados

Selecione exatamente um tipo.

| Categoria | Tipo de dado | Coletado | Compartilhado | Efêmero | Obrigatório ou opcional | Finalidades |
|---|---|---|---|---|---|---|
| Informações financeiras (Financial info) | Outras informações financeiras (Other financial info) | Sim | Não | **Sim** | **Opcional** | Funcionalidade do app (App functionality) |

O que o tipo abrange: endereços públicos de blockchain que o usuário acompanha, os contratos de tokens encontrados neles e os códigos de moeda das posições do usuário. Eles são transmitidos ao operador de dados selecionado pelo usuário para que saldos e preços possam ser obtidos, mantidos na memória durante a solicitação e descartados.

Por que **não compartilhado**: a transferência vai diretamente do dispositivo para o operador escolhido pelo usuário, em uma atualização iniciada pelo usuário, depois que o app informou em Ajustes qual operador será consultado e que a solicitação revela o endereço e o IP a esse operador. Esta é a exceção de "ação iniciada pelo usuário, em que ele espera razoavelmente que os dados sejam compartilhados". O desenvolvedor não recebe nada e não tem prestadores de serviço.

Por que **opcional**: o app pode ser usado por completo apenas com posições manuais. Endereços e chaves de API são informados por escolha do usuário.

As chaves de API informadas pelo usuário são enviadas apenas ao operador que as emitiu. Elas são credenciais do usuário para o próprio serviço daquele operador e não são declaradas como um tipo de dado do usuário separado; se um revisor perguntar, descreva-as como acima.

### Tipos que **não** são coletados

Todas as outras categorias são "Não": sem localização, sem informações pessoais, sem contatos, sem mensagens, sem fotos ou mídia, sem arquivos e documentos, sem atividade no app, sem navegação na Web, sem informações e desempenho do app (sem registros de falhas, sem diagnósticos), sem identificadores do dispositivo ou outros. Endereços IP chegam aos operadores como parte de qualquer solicitação HTTPS e não são usados pelo app para nenhuma finalidade.

Os registros financeiros do usuário (posições, metas, planos) são processados apenas no dispositivo e estão fora do escopo do formulário.

### Práticas de segurança

| Item | Resposta |
|---|---|
| Análise de segurança independente (MASA) | Não |
| Compromisso de seguir a política para famílias (Families policy) | Não (não é um app infantil) |

## Declarações relacionadas na página Conteúdo do app

| Declaração | Resposta |
|---|---|
| URL da Política de Privacidade | `{{ site.url }}/privacy` |
| Anúncios | Não, o app não contém anúncios |
| Acesso ao app | Toda a funcionalidade está disponível sem acesso especial. Sem login. As chaves de API de provedores são opcionais; todo provedor tem uma opção padrão que dispensa chave. |
| Classificação do conteúdo (IARC) | Questionário de utilitário / produtividade; sem violência, conteúdo sexual, jogos de azar, substâncias controladas, interação entre usuários ou compartilhamento de localização. Resultado esperado: Livre (Everyone) / PEGI 3. |
| Público-alvo e conteúdo | 18 anos ou mais (ferramenta de finanças pessoais; não foi criada para crianças) |
| App de notícias | Não |
| Rastreamento de contatos e status da COVID-19 | Não |
| Segurança dos dados | Conforme acima |
| App governamental | Não |
| Recursos financeiros | Veja a [Declaração de recursos financeiros]({{ page.base }}/financial-features) |
| Apps de saúde | Sem recursos de saúde |

## O que atualizar quando o app mudar

Revise esta página quando uma versão adicionar análises de uso, relatórios de falhas, contas, um servidor operado pelo desenvolvedor, um novo SDK com acesso à rede ou compartilhamento no dispositivo com outro app. Qualquer uma dessas mudanças altera o formulário.
