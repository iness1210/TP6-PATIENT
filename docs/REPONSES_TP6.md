# TP6 – Chapitre 5 : Persistance locale (DataStore, Room, cache offline)

## Partie 1 – Exercice 1 : une donnée, une solution

| Donnée | Solution | Justification |
| --- | --- | --- |
| Langue choisie par l'utilisateur | Preferences DataStore | Une seule valeur simple (clé → valeur). |
| 2 000 consultations d'un patient, filtrées par date | Room | Gros volume d'enregistrements et requêtes (filtre par date). |
| Date de dernière synchronisation | Preferences DataStore | Un seul `Long` (millisecondes). |
| Profil du soignant (nom, spécialité, n° d'ordre) | DataStore typé (JSON) | Un petit objet structuré dont les champs restent cohérents. |
| Jeton JWT renvoyé par /login | Stockage chiffré (Tink / Keystore) | C'est un secret : jamais en clair. |
| Photo de la carte d'identité | Fichier | Donnée binaire volumineuse ; Room ne garde que le chemin. |

## Partie 2 – Preferences DataStore

**Pourquoi stocker le tri sous forme de String et non directement l'enum ?**
Preferences DataStore ne sait stocker que des types simples (String, Int, Long, Boolean, Float, Double, Set<String>).
On enregistre donc `Tri.NOM.name` et on relit avec `Tri.valueOf(...)`. Le nom est lisible et reste valable si
l'on réordonne l'enum (l'ordinal, lui, changerait).

**Bonus (Device Explorer).** Dans `/data/data/com.example.patients/files/datastore/` on voit le fichier
`parametres.preferences_pb` (format protobuf). Après « Effacer les données » de l'application, il est supprimé :
au prochain lancement les valeurs par défaut reviennent (tri NOM, thème clair, pas de date de synchro).

## Partie 3 – DataStore typé

**Pourquoi `copy(...)` et pas une modification directe de l'objet ?**
`ProfilSoignant` est une `data class` immuable (`val`). `updateData { it.copy(...) }` lit l'ancien objet, en crée un
nouveau et l'écrit de façon atomique : DataStore émet ensuite le nouvel objet dans son Flow. Modifier l'objet « sur place »
n'est pas possible et casserait la cohérence entre ce qui est affiché et ce qui est écrit sur disque.

**Et si le profil contenait un mot de passe ?** Il serait écrit **en clair** dans `profil.json`, lisible
dans Device Explorer (ou sur un appareil rooté). Un secret doit être chiffré (Tink / Android Keystore), jamais stocké tel quel.

## Partie 4 – Room

**Exercice 10 – que faire si `Patient.id` est null ?** Dans ce projet `Patient.id` est une `String` qui vaut `""`
quand l'API ne renvoie ni `id` ni `_id`. Une clé primaire doit être unique et réelle : on ignore donc ces patients au
moment de remplir Room (`filter { it.id.isNotBlank() }` dans `refresh()`).

**Pourquoi `observerTous()` n'est-elle pas `suspend` alors que `upsertTous()` l'est ?**
`observerTous()` renvoie un `Flow` : Room l'exécute lui-même en arrière-plan et réémet la liste à chaque modification de la table.
`upsertTous()` est une écriture ponctuelle : elle doit s'exécuter hors du thread principal, donc depuis une coroutine (`suspend`).

**À quoi sert `@Transaction` dans `remplacerTout` ?** À rendre « tout supprimer » + « insérer la nouvelle liste » **atomiques** :
si l'insertion échoue, la suppression est annulée (on ne perd pas les anciennes données) et l'écran ne voit jamais une table vide intermédiaire.

**Adaptation.** L'entité contient aussi `telephone`, `medecin` et `statut` (en plus de `id`, `nom`, `prenom`, `age`) :
l'écran les affiche et le filtre Actif / Inactif en dépend.

## Partie 5 – Offline first

L'écran Patients lit **uniquement Room** (`observePatients()`). `refresh()` appelle l'API, remplace Room, puis écrit la date de synchro
dans DataStore ; Room réémet et l'écran se met à jour tout seul. Si l'appel échoue (`IOException` / `HttpException`), les données locales restent
affichées avec le bandeau orange « Hors ligne — dernière synchro : … ».
Création, modification et suppression passent par l'API puis mettent Room à jour.

## Exercice 15 – Scénario de test (à compléter avec tes observations)

| Étape | Action | Résultat attendu | Observation |
| --- | --- | --- | --- |
| 1 | API lancée, ouvrir l'appli | Liste affichée, pas de bandeau | Liste affichée ; l'API a répondu `200 OK`, sans bandeau hors ligne. |
| 2 | Activer le mode avion, tuer puis relancer l'appli | Même liste, bandeau « Hors ligne » avec la date | La capture montre la liste conservée et le bandeau « Hors ligne — dernière synchro : 21/11/2026 00:32 ». |
| 3 | Changer le tri dans Paramètres, revenir | Liste retriée, toujours hors ligne | La capture montre le thème sombre et la liste triée par âge croissant ; le bandeau hors ligne est toujours présent. |
| 4 | Ajouter un patient via Postman, couper le mode avion, Actualiser | Nouveau patient visible, bandeau disparu | Postman a répondu `201 Created` pour ER NOUSSA ; après reconnexion, l'API a répondu `200 OK` et ER NOUSSA apparaît dans l'application sans bandeau hors ligne. |
| 5 | Supprimer un patient via Postman, Actualiser | Patient disparu de la liste locale | Patient supprimé avec Postman ; après actualisation, il a disparu de la liste locale. |

**À rendre :** le projet zippé + captures d'écran des **étapes 2 et 4**.

## Questions de vérification

- `prefs[Cles.THEME_SOMBRE]` renvoie **`null`** si l'utilisateur n'a jamais touché au Switch (d'où `?: false`).
- On n'attrape pas `Exception` dans le `catch` car on masquerait des erreurs qui ne sont pas des problèmes de lecture du fichier (bugs, `CancellationException`…) : on ne traite que `IOException`.
- Créer deux fois `preferencesDataStore(name = "parametres")` donne deux instances sur le même fichier : l'application plante (`IllegalStateException`).
- Dans l'approche offline first, l'écran lit les patients **dans Room**, jamais directement dans l'API.
- L'écran se met à jour seul après `refresh()` car `observerTous()` est un `Flow` : Room réémet la liste dès que la table change.
