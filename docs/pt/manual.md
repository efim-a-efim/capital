---
layout: default
lang: pt
base: "/pt"
key: "manual"
title: Manual do usuário
class: doc
---
# Manual do usuário

<p class="meta">Capital 2.2 · Android 8.0 ou mais recente</p>

## A ideia

Você guarda dinheiro em vários lugares: uma conta de poupança, dinheiro em espécie, uma corretora, uma carteira de cripto. O Capital chama cada lugar de **caixinha**. Você quer esse dinheiro para várias coisas: uma reserva de emergência, uma viagem, um notebook. O Capital chama cada uma de **meta**. Você vincula caixinhas a metas, e o app calcula quanto de cada meta já está coberto pelo que você tem hoje. Adicione as economias que você **planeja** fazer e ele também mostra a data em que cada meta será concluída.

Nada no app movimenta dinheiro. Ele é um espelho do que você possui e uma calculadora do quanto isso cobre.

## Primeiro uso {#first-launch}

1. **Escolha uma pasta.** Selecione uma pasta exclusiva no dispositivo, por exemplo `Documents/Capital`. É nela que todos os registros são gravados. Uma pasta que já contém dados do Capital é aberta diretamente.
2. **Ajustes → Moeda padrão.** Os totais e a Visão geral são exibidos nela.
3. Se quiser, defina **chaves de provedores** em Ajustes para os operadores que oferecem limites maiores com uma chave gratuita (Alchemy, TronGrid, TON Center, CoinGecko). Toda rede e toda fonte de preços tem uma opção padrão que dispensa chave.

## Caixinhas {#buckets}

Aba Caixinhas → **+**. Dê à caixinha um nome e uma moeda. Abra-a para adicionar posições:

- **Posição manual**: um nome, um código de moeda ou de ativo (EUR, USD, BTC, o ticker de uma ação que você mesmo avalia…) e uma quantidade. Use para saldos bancários, dinheiro em espécie, qualquer coisa que o app não consiga ler.
- **Posição de carteira**: escolha a rede (BTC, ETH, TON, TRX) e cole um endereço público. Ao atualizar, o app lê o saldo nativo e, em ETH, TON e TRX, os tokens fungíveis do endereço.

Os valores aceitam ponto ou vírgula como separador decimal, sem separadores de milhar. Cada caixinha mostra as quantidades nativas e o valor delas na sua moeda padrão. Se faltar uma cotação, o total é marcado como incompleto; um valor em cache desatualizado continua sendo usado, com um aviso.

**Tokens.** Um token é identificado pelo endereço do contrato, nunca pelo nome. Ele só é contabilizado quando a fonte de preços selecionada lista exatamente aquele contrato; todo o resto aparece como *Token desconhecido · não contabilizado* e fica fora dos totais. Abra o editor de uma posição de carteira para buscar os tokens e desativar os que você não quer.

O **Modo portfólio** (ajustes da caixinha) trata a caixinha como uma carteira de investimentos: defina um percentual-alvo por ativo, compare a participação real com o alvo e use **Rebalancear** para obter uma lista do que comprar com determinado valor. Vendas só são sugeridas quando *Permitir vendas no rebalanceamento* está ativado. É uma calculadora; não altera nada.

## Metas {#goals}

Aba Metas → **+**. Uma meta tem nome, moeda, valor-alvo e data de vencimento. Abra a meta e use **Vincular caixinha** para indicar quais caixinhas podem cobri-la, opcionalmente com um limite: um valor fixo, um percentual da caixinha ou um percentual da meta.

Como o dinheiro é alocado:

- Metas com data de vencimento mais próxima recebem recursos primeiro. Metas com a mesma data recebem recursos na ordem exibida; arraste a alça para reordená-las.
- Uma caixinha vinculada a várias metas é dividida entre elas de acordo com os limites e nunca é contada duas vezes.
- O resultado aparece como *coberto / valor-alvo* e *Ainda falta*. Na Visão geral você vê o total, o que está alocado para metas e o que sobra.

**Selos.** *Atingida* (verde) quando as economias de hoje já cobrem a meta. *Será atingida no prazo* (verde) quando as economias planejadas a concluem até a data de vencimento. *Não atingida* (amarelo) nos demais casos. O texto abaixo da meta diz quando ela será concluída ou quanto vai faltar.

Use **Arquivar** para manter uma meta sem contabilizá-la. As metas arquivadas ficam listadas no final.

## Planos {#plans}

Aba Planos → **+**. Uma economia planejada é um valor que você pretende adicionar em determinada data, por exemplo o que você guarda do salário no fim de cada mês. Os planos não fazem parte das suas economias; eles apenas estendem a projeção: "as economias planejadas concluem esta meta em 30 de out. de 2026 · no prazo".

O dinheiro dos planos é aplicado depois das caixinhas atuais, às metas em ordem de vencimento, então ele só completa o que ainda está em aberto. Quando a data de um plano passa, ele vai para a seção **Arquivada** e deixa de ser contabilizado: ou você já transferiu o dinheiro para uma caixinha e o app o vê lá, ou o plano não se concretizou. Altere a data para o futuro para reativá-lo; exclua-o se não fizer mais sentido.

## Atualização

O ícone de atualizar no topo recarrega todos os saldos de carteiras e preços. Também é possível atualizar uma caixinha sozinha. O app atualiza uma vez ao ser iniciado do zero; ao voltar do segundo plano, ele apenas recarrega os arquivos locais. Atualizar exige internet; sem conexão, os valores anteriores são mantidos e marcados como desatualizados.

## Segurança {#security}

Ajustes → Segurança.

- **Criptografia** criptografa com uma senha todos os arquivos da pasta, inclusive as revisões antigas. Desativá-la descriptografa os arquivos. **Não há recuperação de senha**: se você perder a senha, não será possível abrir os dados. Backups sem criptografia feitos antes de você ativar a criptografia continuam legíveis; o app avisa sobre eles, mas não pode excluí-los.
- **PIN** e **biometria** ficam disponíveis quando a criptografia está ativada. *Usar senha* está sempre disponível na tela do PIN. Após 10 PINs incorretos, o PIN é removido e só a senha funciona. Tentativas incorretas nunca excluem dados.
- Com a criptografia ativada, as capturas de tela e a prévia na lista de apps recentes ficam bloqueadas.

## Sincronização, backup, recuperação {#sync-backup-recovery}

O Capital grava na sua pasta arquivos de versões salvas com IDs de revisão e somas de verificação, e nunca faz sincronização própria. Coloque a pasta em qualquer ferramenta de sincronização que você já usa. Se dois dispositivos editarem ao mesmo tempo, o app mostra uma tela de conflito e deixa você escolher uma versão; os dois originais permanecem no disco.

- **Exportar backup** (Ajustes) grava um único arquivo portátil. **Restaurar** valida o arquivo antes de alterar qualquer coisa.
- Se uma gravação falhar, suas edições ficam na memória com *Tentar salvar de novo* e *Salvar cópia*.
- Se a permissão de acesso à pasta for perdida, reconecte a mesma pasta.
- Um arquivo gravado por uma versão mais recente do app é recusado por uma versão mais antiga; atualize o app.

## Idioma {#language}

O app abre no idioma do dispositivo quando ele é um dos 15 idiomas compatíveis; caso contrário, em inglês. Altere-o em Ajustes → Idioma.

## Instalação fora do Google Play

Baixe o APK da [versão mais recente]({{ site.repo }}/releases/latest) e abra-o; permita a instalação dessa fonte quando o Android pedir. Todas as versões são assinadas com a mesma chave, então as novas são instaladas por cima das antigas e mantêm seus ajustes. A pasta com seus dados nunca é tocada por uma atualização nem por uma desinstalação.
