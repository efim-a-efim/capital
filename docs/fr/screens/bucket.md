---
layout: screen
lang: fr
base: "/fr"
key: "screens/bucket"
screen: bucket
title: Poche
---
# Poche

**Ce que c’est.** Une poche avec ses positions. On y accède en touchant une carte dans l’onglet [Poches]({{ page.base }}/screens/buckets) ; **← Toutes les poches** permet de revenir en arrière.

**En-tête.** La valeur de la poche, puis deux chiffres qui n’ont de sens qu’ensemble : **Affecté**, la part réclamée par les objectifs connectés, et **Disponible**, le reste. **Modifier la poche** donne accès au nom, à la devise et aux options du mode portefeuille. **Supprimer la poche** supprime la poche et ses positions après confirmation.

**Positions.** Chaque position affiche son nom, sa valeur dans la devise de la poche, son mode de suivi (*Manuel*, *Portefeuille crypto* ou *Compte de courtage*), la quantité native, la date d’observation de la valeur et la date de sa dernière récupération. **Modifier / déplacer** la modifie ou la déplace vers une autre poche ; **Supprimer** la supprime.

**Ajouter une position** ouvre l’éditeur de position :

- **Manuel** : un nom, un code de devise ou d’actif et la quantité. Utilisez ce mode pour tout ce que l’application ne peut pas lire.
- **Portefeuille crypto** : choisissez la blockchain (BTC, ETH, TON, TRX) et collez une adresse publique. L’application lit le solde natif à l’actualisation et, sur ETH, TON et TRX, les jetons fongibles détenus à cette adresse. Rouvrez l’éditeur et appuyez sur **Récupérer les jetons** pour les voir et désactiver ceux que vous ne voulez pas compter.
- **Compte de courtage** : choisissez l’un des comptes ajoutés sur l’écran [Courtiers]({{ page.base }}/screens/brokers) ; laissez le nom vide pour utiliser celui du compte. À l’actualisation, l’application lit la valeur totale du compte dans la devise de référence du compte. Un compte ne peut être lié qu’à une seule poche ; supprimer la position la délie sans supprimer le compte.

**Jetons et « non compté ».** Un jeton est identifié par l’adresse de son contrat. Il n’est compté que si votre source de cours référence exactement ce contrat ; sinon, il est listé comme *Jeton inconnu · non compté* et reste exclu des totaux. C’est ce qui empêche un faux « USDT » reçu par airdrop de gonfler votre épargne.

**PnL d’investissement.** Activez-le dans **Modifier la poche** (case **PnL d’investissement**). À chaque actualisation, l’application enregistre alors la quantité de chaque position ainsi que les versements et retraits de ses comptes de courtage, et l’écran gagne une section **Investissement** :

- Cinq lignes, **Depuis le début**, **Dernière année**, **Dernier mois**, **Dernière semaine** et **Dernier jour**, chacune avec le gain ou la perte dans la devise de la poche et le rendement pondéré dans le temps en pourcentage, en vert s’il est positif et en rouge s’il est négatif. Une ligne affiche *depuis le <date>* lorsque l’historique commence après le début de cette période.
- Un graphique **Valeur** de la valeur de la poche et un graphique **Gains et pertes** du résultat cumulé, tous deux dans la devise de la poche. Les puces **Tout**, **1Y**, **1M**, **1W** et **1D** recentrent les deux graphiques sur cette période ; la courbe du PnL repart de zéro au début de la période.
- *Depuis le <date> · en <devise>* indique où commence l’historique. *Il manque un taux certains jours ; ces valeurs reprennent le dernier taux connu* apparaît lorsque l’historique des taux comporte des lacunes.

**Comment il est calculé.** Gain ou perte = variation de valeur moins les versements plus les retraits. Le pourcentage est pondéré dans le temps : le rendement de chaque jour est enchaîné, de sorte que le moment de vos versements ne le modifie pas. Pour les comptes liés, les versements et retraits viennent du courtier ([quels courtiers]({{ page.base }}/accounts#pnl)) ; pour les positions manuelles et les portefeuilles crypto, une variation de la quantité compte comme un versement ou un retrait au taux de ce jour, jamais comme un gain. Les valeurs passées utilisent l’historique quotidien des taux (**Paramètres → Historique des taux**). L’activation de l’option construit l’historique à partir des instantanés déjà présents dans votre dossier et des courtiers (*Construction de l’historique du PnL…*, puis *Historique du PnL prêt : N jours*) ; jusqu’à la première actualisation, la section indique *Pas encore d’historique*.

**Mode portefeuille.** Lorsqu’il est activé, l’écran ajoute un tableau indiquant pour chaque actif la valeur, la part réelle, la cible et l’écart, ainsi qu’un bouton **Rééquilibrer** qui demande un montant et liste ce qu’il faut acheter. Des ventes n’apparaissent que si *Autoriser les ventes lors du rééquilibrage* est activé. Aucune opération n’est exécutée.
