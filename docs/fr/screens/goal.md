---
layout: screen
lang: fr
base: "/fr"
key: "screens/goal"
screen: goal
title: Objectif
---
# Objectif

**Ce que c’est.** Un objectif avec son financement. On y accède en touchant une carte dans l’onglet [Objectifs]({{ page.base }}/screens/goals) ; **← Tous les objectifs** permet de revenir en arrière.

**En-tête.** Le nom et le badge, financé / cible, l’échéance, la ligne de projection et **Reste à financer** : le montant cible moins ce qui est financé aujourd’hui.

**Boutons.** **Modifier l’objectif** change le nom, la devise, le montant cible et l’échéance. **Archiver** conserve l’objectif sans le compter ; un objectif archivé affiche **Activer** à la place. **Supprimer** supprime l’objectif et ses connexions après confirmation.

## FIRE {#fire}

Un objectif de type **FIRE** (indépendance financière, retraite anticipée) répond à deux questions : à partir de quand l’argent peut-il payer vos dépenses courantes, et combien de temps dure-t-il. C’est une projection sur vos propres chiffres ; elle ne recommande rien. Définissez le type avec **Type d’objectif** (**Objectif d’épargne** / **FIRE**) dans l’éditeur.

**Champs de l’éditeur.** Tous les champs, sauf le nom et la devise, peuvent rester vides ; l’application calcule ce qu’elle peut à partir du reste.

- **Date de naissance · YYYY-MM-DD · facultatif** : affiche les âges et fixe l’horizon à l’espérance de vie.
- **Date FIRE · YYYY-MM-DD · vide pour la calculer** : le jour où vous arrêtez d’épargner et commencez à dépenser.
- **Je peux investir · vide pour le calculer** : un montant par **semaine**, **2 semaines**, **mois**, **an** ou **N jours** (saisissez alors **Jours**).
- **Croissance du versement % par an · vide = inflation** : de combien vous pouvez investir de plus chaque année, par exemple une augmentation annuelle de salaire. Vide, le versement conserve son pouvoir d’achat.
- **Dépense par mois après FIRE · vide pour la calculer**.
- **Montant à avoir après FIRE · vide pour le calculer** : le montant FIRE, la cible de l’objectif.
- **Hypothèses avancées** : **Inflation % · moyenne / optimiste / pessimiste**, **Croissance % · moyenne / optimiste / pessimiste**, **Taux de retrait %** et **Espérance de vie · années**. Chaque libellé se termine par « · actuellement {valeur} », la valeur appliquée lorsque le champ est vide.

Tous les montants sont en valeur d’aujourd’hui, dans la devise de l’objectif. L’écran affiche aussi la valeur à la date FIRE, en monnaie future. L’argent dont l’objectif dispose aujourd’hui provient de ses poches connectées, comme pour tout objectif ; les [Prévisions]({{ page.base }}/screens/plans) n’entrent pas dans le calcul FIRE.

**Ce qui est calculé.**

- Date FIRE : le premier mois à partir duquel la dépense est couverte jusqu’à l’espérance de vie (50 ans sans date de naissance) et, si une cible est donnée, la cible est atteinte. Sans cible, le montant FIRE devient la somme dont cette durée de vie a besoin.
- Versement : à partir de la cible et de la date FIRE.
- Cible : à partir de la dépense (dépense × 12 ÷ taux de retrait) lorsque la date FIRE est donnée, ou à partir de la date FIRE et du versement.
- Dépense : à partir de la cible (cible × taux de retrait ÷ 12).

Si la date FIRE, le versement et la cible sont tous renseignés, la date est conservée et l’écran affiche le manque et la date la plus proche qui soit atteignable. La cible et la date calculées sont écrites dans la cible et l’échéance de l’objectif ; le classement, l’affectation, la Synthèse et les Prévisions fonctionnent donc comme pour tout objectif. Elles sont recalculées à chaque modification et à chaque actualisation.

**La section FIRE.** D’abord les valeurs identiques dans tous les scénarios (ce que vous avez saisi, et le **Taux de retrait**), une par ligne. Ensuite une ligne dépliable par scénario, **Moyen**, **Optimiste** et **Pessimiste**, avec ses taux de croissance et d’inflation, la date FIRE calculée lorsque vous l’avez laissée vide, et un badge : **L’argent suffit** (vert) lorsque l’argent dure au-delà de l’espérance de vie, **l’argent ne s’épuisera jamais 🥳** (vert) lorsqu’il n’est jamais épuisé, **Argent insuffisant** (jaune) lorsque la date FIRE n’est pas atteignable ou que l’argent s’épuise plus tôt. Touchez la ligne pour déplier les valeurs qui diffèrent selon le scénario :

| Ligne | Signification |
|---|---|
| **Date FIRE** | La date, avec « N ans » lorsque la date de naissance est connue, ou *Inatteignable avant le <date>* |
| **Versement par mois** | Ce qu’il faut investir chaque mois |
| **Montant FIRE · valeur d’aujourd’hui** | Le montant à avoir, tel que l’éditeur l’indique |
| **Montant FIRE · à la date FIRE** | Le même montant après inflation |
| **Dépense par mois · valeur d’aujourd’hui** | La dépense mensuelle après FIRE |
| **Dépense par mois · à la date FIRE** | La même dépense après inflation |
| **L’argent dure** | *jusqu’au <date> · N ans*, *N ans · au-delà de l’espérance de vie*, *l’argent ne s’épuisera jamais 🥳*, ou *il manque …* lorsque la date n’est pas atteignable |

Sous les lignes dépliables, une ligne « Taux : … » nomme le fournisseur dont les taux proviennent, ou *valeurs par défaut intégrées*. Le graphique en dessous est une courbe d’épuisement : la valeur de l’argent en valeur d’aujourd’hui, d’aujourd’hui jusqu’à l’épuisement de l’argent ou l’horizon, avec trois courbes colorées, une par scénario. La ligne pointillée marque la date FIRE. La note en dessous indique *Une projection sur vos propres chiffres ; elle ne recommande rien.* Dans l’onglet [Objectifs]({{ page.base }}/screens/goals), l’objectif affiche *FIRE le <date> · N ans*, ou *inatteignable*.

**Mode de calcul.** Par pas mensuels. Jusqu’à la date FIRE, le versement augmente de **Croissance du versement % par an** (de l’inflation si le champ est vide, donc il conserve son pouvoir d’achat), et l’argent croît au taux du scénario. Après la date FIRE, la dépense augmente avec l’inflation et ce qui reste continue de croître au taux du scénario. L’horizon est la date de naissance plus l’espérance de vie (95 ans par défaut), ou 50 ans après la date FIRE sans date de naissance. *L’argent ne s’épuisera jamais* signifie que l’argent n’est pas épuisé en 200 ans et qu’il vaut, en valeur d’aujourd’hui, au moins autant à la fin qu’à la date FIRE.

**Valeurs par défaut et sources.** Sans valeur saisie ni taux récupérés, l’application utilise une croissance de 7 % (optimiste 10 %, pessimiste 4 %), une inflation de 3 % (2 %, 5 %) et un taux de retrait de 4 %. Ils proviennent du UBS Global Investment Returns Yearbook 2026 (monde, 1900–2024 : actions 9,8 % par an en nominal, obligations 4,6 %, inflation 2,9 % ; 7 % correspond à peu près à un mix 60/40), des cibles d’inflation de 2 % des banques centrales, et de Bengen (1994) et de l’étude Trinity (1998) pour la règle des 4 %. Voir aussi [FIRE]({{ page.base }}/manual#fire) dans le manuel.

**Taux en direct.** **Actualiser** récupère l’inflation et la croissance des marchés pour la devise de chaque objectif FIRE, auprès des fournisseurs choisis dans les [Paramètres]({{ page.base }}/screens/settings). L’ordre est le suivant : votre valeur dans **Hypothèses avancées**, puis la valeur récupérée, puis la valeur par défaut. Un échec de récupération conserve la série précédente et affiche l’erreur. Les deux sources de croissance sont des indices de prix sans dividendes ; elles sous-estiment donc le rendement total.

**Messages.**

| Message | Que faire |
|---|---|
| *Saisissez une cible, une dépense mensuelle, ou à la fois une date FIRE et un versement* | Renseignez l’un des trois ; sans eux, rien ne peut être calculé. |
| *Saisissez une date FIRE ou un versement* | La cible est connue, mais ni la date ni le versement ne le sont ; donnez l’un des deux. |
| *La date FIRE est dans le passé* | Choisissez une date future. |
| *La date de naissance est dans le futur* | Corrigez la date de naissance. |
| *Le taux de retrait doit être supérieur à zéro* | Saisissez un taux de retrait supérieur à 0 dans **Hypothèses avancées**, ou videz le champ pour utiliser la valeur par défaut. |
| *Inatteignable avant le <date>* | Avec ce versement et ces taux, la cible n’est pas atteinte avant cette date (la fin de l’horizon) ; augmentez le versement, baissez la cible ou la dépense, ou choisissez un autre scénario. |
| *Aucune donnée de taux pour <devise>* | Le fournisseur d’inflation ou de croissance n’a pas de série pour cette devise ; choisissez un autre fournisseur ou saisissez les taux dans **Hypothèses avancées**. |
| *ECB ne couvre pas <devise>* | L’ECB ne fournit que l’inflation de la zone euro ; choisissez un autre fournisseur d’**Inflation** dans les Paramètres. |
| *L’OCDE ne couvre pas <devise>* | L’OCDE n’a pas de série pour cette devise (par exemple RUB) ; choisissez un autre fournisseur d’**Inflation** dans les Paramètres. |

**Sources de financement.** Les poches qui peuvent financer cet objectif. **Connecter une poche** en ajoute une avec une limite de contribution :

- **Auto — jusqu’au besoin restant** : la poche fournit tout ce dont l’objectif a encore besoin, une fois que les objectifs prioritaires ont pris leur part.
- **Montant fixe dans la devise de l’objectif**.
- **% de la poche** : au plus cette part de la valeur de la poche.
- **% de l’objectif** : au plus cette part du montant cible.

L’aperçu dans l’éditeur montre ce que la connexion apporterait aujourd’hui. Les limites sont des plafonds : l’ordre des objectifs, l’épargne disponible et les autres connexions peuvent réduire la contribution. Chaque source listée indique ce qu’elle apporte actuellement et pourquoi elle n’apporte pas davantage ; **Modifier la connexion** change la limite, **Déconnecter** supprime la connexion.

**Versements prévus.** Les versements prévus qui alimentent cet objectif, chacun avec le montant que la projection lui attribue. Un versement prévu entièrement absorbé par des objectifs prioritaires n’apparaît pas ici.

**Lire la projection.** « Objectif atteint le 20 déc. 2026 grâce aux versements prévus · dans les temps » signifie que le cumul des versements prévus jusqu’à cette date couvre le besoin restant avant l’échéance. « Les versements prévus couvrent jusqu’à … · il manque … » signifie que ce n’est pas le cas ; ajoutez un versement prévu, déplacez l’échéance ou réduisez le montant cible.
