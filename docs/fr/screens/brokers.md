---
layout: screen
lang: fr
base: "/fr"
key: "screens/brokers"
screen: brokers
title: Courtiers
---
# Courtiers

**Ce que c’est.** Vos connexions en lecture seule à des comptes de courtage et de forex. Un compte de courtage se compose du courtier, de l’identifiant du compte ou de la requête et de l’identifiant du courtier ; à l’actualisation, l’application lit la valeur totale du compte dans sa devise de référence. Les comptes vivent sur cet écran, pas dans les poches : une poche ne fait que s’y lier, et les poches restent l’endroit où votre épargne est comptée.

**Ce que montre chaque ligne.** Le nom du compte, le courtier et l’identifiant, la dernière valeur lue dans la devise du compte et dans votre devise par défaut, la date d’observation et la date de récupération, ainsi que la poche à laquelle il est lié : **Lié à …** ouvre cette poche, *Non lié à une poche* signifie que rien ne le compte encore. **Modifier** change le nom, le courtier ou l’identifiant ; **Supprimer** retire le compte et, s’il était lié, la position qui le liait.

**Ajouter un compte.** Appuyez sur **+**, saisissez un nom, choisissez le courtier et saisissez l’identifiant qu’il utilise : le Flex Query id pour Interactive Brokers, l’identifiant de compte pour OANDA, le numéro de compte pour Trading 212. Pour SnapTrade, appuyez sur **Connecter un courtier via SnapTrade**, revenez, appuyez sur **Récupérer les comptes** et choisissez-en un. Enregistrez. La devise et la valeur apparaissent après la prochaine actualisation.

**Ignorer les soldes inférieurs à.** Cochez cette case et saisissez un montant dans votre devise par défaut (1 par défaut) pour garder la poussière hors de votre épargne : lorsque la valeur du compte, convertie avec les taux en cache, est inférieure à ce montant, la position liée compte pour 0 et la ligne indique *Compté comme 0 : inférieur à …*. La valeur réelle reste visible sur cet écran. Sans taux pour la devise du compte, rien n’est ignoré.

**Le lier à une poche.** Ouvrez la poche, appuyez sur **Ajouter une position**, réglez **Suivi** sur **Compte de courtage** et choisissez le compte ; laissez le nom vide pour utiliser celui du compte. Un compte ne peut être que dans une poche à la fois. **Modifier / déplacer** sur la position la déplace vers une autre poche ; supprimer la position délie le compte sans le supprimer.

**Identifiants.** Le jeton ou la clé de chaque courtier pris en charge (Interactive Brokers, OANDA, Trading 212, SnapTrade) ; un seul jeu par courtier couvre tous les comptes de ce courtier. Ils sont chiffrés avec une clé conservée dans Android Keystore, ne sont jamais écrits dans le dossier de données, sont exclus des exports et des sauvegardes du système, et sont envoyés uniquement au courtier qui les a émis. **Guide de configuration des comptes de courtage** ouvre [Comptes de courtage et de forex]({{ page.base }}/accounts), qui indique les étapes pour chaque courtier.

**Actualisation.** L’icône d’actualisation de cet écran lit tous les comptes ; l’actualisation d’une poche ne lit que les comptes qui y sont liés. Un compte qui ne peut pas être lu garde sa dernière valeur et affiche le message du courtier sous sa ligne. L’application se contente de lire : elle ne passe jamais d’ordres et ne déplace jamais d’argent.
