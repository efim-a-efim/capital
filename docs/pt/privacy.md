---
layout: default
lang: pt
base: "/pt"
key: "privacy"
title: Política de Privacidade
class: doc
---
# Política de Privacidade

<p class="meta">Capital para Android (pacote <code>dev.capital</code>) · Desenvolvedor: {{ site.developer }} · Em vigor desde 30 de setembro de 2026</p>

## Resumo

- O Capital não tem contas de usuário, análises de uso, publicidade, relatórios de falhas nem servidores operados pelo desenvolvedor. O desenvolvedor nunca recebe seus dados.
- Seus registros financeiros ficam armazenados apenas no seu dispositivo, em uma pasta escolhida por você. Você pode criptografá-los com uma senha.
- O único tráfego de rede são as solicitações que o app faz, por sua instrução, aos operadores de dados de preços e de blockchain que você seleciona em Ajustes e às corretoras cujas contas você conecta. Essas solicitações contêm os endereços públicos de carteiras, os contratos de tokens e os códigos de moeda que você acompanha, qualquer chave de API que você tenha informado para aquele operador e, no caso de uma conta de corretora, o token de acesso que você criou e o ID da conta ou da consulta.

## O que o app armazena no seu dispositivo

**Na pasta que você escolhe.** Caixinhas, posições, endereços de carteiras, metas, vínculos, economias planejadas, cotações em cache e os ajustes que pertencem a esses dados. Os arquivos são texto simples, a menos que você ative a criptografia (Ajustes → Segurança). Com a criptografia ativada, todos os arquivos são criptografados com AES-256-GCM usando uma chave derivada da sua senha com Argon2id. Não há recuperação de senha.

**No armazenamento privado do app** (inacessível a outros apps):

| Item | Finalidade |
|---|---|
| Permissão de acesso à pasta selecionada | Reabrir a pasta na próxima inicialização |
| Chaves de API de provedores e tokens de acesso de corretoras que você informou | Enviados apenas ao operador ou à corretora que os emitiu; criptografadas com uma chave mantida no Android Keystore; excluídas das versões salvas, das exportações e dos backups do sistema |
| Ajustes de bloqueio | Desbloquear a pasta criptografada sem a senha: uma cópia da chave dos dados, criptografada com uma chave derivada do seu PIN e vinculada ao Android Keystore. O PIN em si não é armazenado |
| Idioma e tema escolhidos | Preferências da interface |

O backup do Android e a transferência entre dispositivos estão desativados para o app, então nada disso é copiado pelo sistema para o Google ou para outro dispositivo.

## O que sai do seu dispositivo

O Capital se comunica apenas com os operadores que você escolhe em Ajustes, apenas por HTTPS e apenas quando você atualiza ou testa as fontes. Cada solicitação é respondida e descartada; o app guarda na sua pasta os saldos e preços retornados, não a solicitação.

| Dados enviados | Para quem | Por quê |
|---|---|---|
| Endereços públicos de carteiras que você adicionou | O operador de dados de blockchain selecionado para aquela rede | Ler o saldo e os tokens do endereço |
| Endereços de contratos de tokens e IDs de ativos | O operador de preços de cripto que você selecionou | Precificar os ativos |
| Códigos de moeda | O operador de câmbio que você selecionou | Converter entre moedas |
| A chave de API que você informou para um operador | Somente esse operador | Autenticar a sua própria conta com ele |
| O token de acesso ou a chave de API e o ID da conta ou da consulta de uma conta de corretora | Somente essa corretora (Interactive Brokers, OANDA, Trading 212, SnapTrade) | Ler o valor total da conta |

Todo operador também vê seu endereço IP, como em qualquer solicitação pela internet. Os operadores são independentes do desenvolvedor e processam a solicitação conforme seus próprios termos e políticas de privacidade, com links em Ajustes → Fontes / atribuição no app:

| Dados | Operadores |
|---|---|
| Bitcoin | [Blockstream](https://blockstream.info), [mempool.space](https://mempool.space) |
| Ethereum e tokens ERC-20 | [PublicNode](https://publicnode.com), [Alchemy](https://www.alchemy.com), [Blockscout](https://www.blockscout.com), [Ethplorer](https://ethplorer.io) |
| TON e jettons | [TON Center](https://toncenter.com), [TonAPI](https://tonapi.io) |
| TRON e tokens TRC-20 | [TronGrid](https://www.trongrid.io), [PublicNode](https://publicnode.com) |
| Preços de cripto | [DefiLlama](https://defillama.com), [CoinGecko](https://www.coingecko.com), [CoinPaprika](https://coinpaprika.com) |
| Câmbio de moedas fiduciárias | [Frankfurter](https://frankfurter.dev), [Banco Central Europeu](https://www.ecb.europa.eu) |
| Contas de corretora | [Interactive Brokers](https://www.interactivebrokers.com), [OANDA](https://www.oanda.com), [Trading 212](https://www.trading212.com), [SnapTrade](https://snaptrade.com) |

Nada é enviado para nenhum outro lugar. Nenhum dado é vendido, compartilhado para fins de publicidade ou usado para criar perfis. Consultas a blockchains públicas revelam que o endereço que você acompanha interessa a alguém no seu endereço IP; use uma VPN se isso for importante para você.

## O que o app nunca faz

- Nunca pede, armazena ou transmite chaves privadas ou frases-semente. Não consegue assinar nem enviar transações.
- Nunca transfere dinheiro. As alocações para metas são cálculos exibidos para você e nada mais.
- Nunca envia uma ordem nem uma instrução a uma corretora. O acesso à corretora serve apenas para ler o valor da conta; veja [Contas de corretora e de forex]({{ page.base }}/accounts).
- Nunca se comunica com o desenvolvedor. Não há telemetria, verificação de atualizações dentro do app nem notificações push.

## Permissões

| Permissão | Uso |
|---|---|
| Internet | Solicitações aos operadores listados acima |
| Acesso à pasta | Concedido por você, pelo seletor de pastas do Android, para a pasta que você escolher; o app não consegue ler outras pastas |
| Biometria | Desbloqueio com impressão digital ou rosto pela solicitação do próprio Android; o app recebe apenas sucesso ou falha, nunca dados biométricos |

## Sincronização e backups

O Capital não sincroniza nada por conta própria. Se você colocar a pasta em uma ferramenta de sincronização (Syncthing, Nextcloud, Google Drive, …), os termos de privacidade dessa ferramenta se aplicam às cópias que ela fizer. Os arquivos são texto simples, a menos que a criptografia esteja ativada; cópias sem criptografia feitas antes de você ativar a criptografia continuam legíveis por quem as tiver.

**Exportar backup** em Ajustes grava um único arquivo no local que você escolher. Ele contém os mesmos registros e só é tão protegido quanto esse local.

## Exclusão dos seus dados

Exclua a pasta que você escolheu (e todas as cópias feitas pela sua ferramenta de sincronização) e desinstale o app. A desinstalação remove o armazenamento privado do app, inclusive as chaves de provedores e os ajustes de bloqueio. O desenvolvedor não guarda nada que precise ser excluído e não pode excluir nada em seu nome. Os operadores que você consultou podem manter registros das solicitações conforme suas próprias regras de retenção.

## Doações {#donations}

O app (botão de coração na Visão geral) e este site mostram os endereços de carteira do desenvolvedor para gorjetas voluntárias. Uma gorjeta é uma transferência que você faz da sua própria carteira para um desses endereços, nos termos da sua carteira e da rede que você usa. O Capital não participa dela: não processa nenhum pagamento, não consegue ver se você enviou algo, não registra nada a respeito e não altera nada — nenhum recurso é desbloqueado ou modificado. O único objetivo de uma gorjeta é apoiar o desenvolvedor. Como em qualquer transação em blockchain, enviar para um endereço público revela o seu endereço de envio nessa rede.

## Crianças

O Capital é uma ferramenta de finanças pessoais para adultos. Ele não é direcionado a crianças menores de 13 anos e não coleta intencionalmente nenhum dado delas.

## Alterações nesta política

A versão atual está sempre em [{{ site.url }}{{ page.base }}/privacy]({{ page.base }}/privacy). Alterações relevantes são listadas nas notas da versão que as introduz.

## Contato

{{ site.developer }} · [{{ site.contact }}](mailto:{{ site.contact }}) · [Rastreador de problemas]({{ site.repo }}/issues)
