# ClipSort

Application Android qui intercepte le partage natif (TikTok, Instagram, YouTube, Facebook)
pour catégoriser un clip en 2 taps, au lieu de l'envoyer dans une liste WhatsApp sans tri.

## Architecture

Clean Architecture en 3 couches, MVVM côté presentation :

```
domain/          Kotlin pur, aucune dépendance Android. Modèles, interfaces repository, use cases.
data/            Implémentation concrète : Room (entities, DAO), mappers, repositories.
presentation/    Compose + ViewModel + UiState par écran, injectés via Hilt.
```

Règle stricte : la couche `presentation` ne connaît que des modèles `domain`, jamais une
entité Room. La conversion se fait exclusivement dans `data/mapper/EntityMappers.kt`.

## État actuel

- [x] Modèles domain (`Category`, `Clip`) + interfaces repository
- [x] Use cases : détection de source, création de catégorie, sauvegarde de clip, lecture bibliothèque
- [x] Persistance Room (entities, DAO, base de données)
- [x] Repositories concrets + module Hilt
- [x] Écrans Compose : catégorisation (bottom sheet de partage), bibliothèque, détail de catégorie
- [x] Tests unitaires de démonstration sur la couche domain (JUnit + MockK + Truth)
- [x] Renommage et suppression des catégories (fusion non implémentée)
- [x] CI GitHub Actions à chaque push : tests domain et intégration Room, lint, builds debug et release R8
- [x] Schéma Room v1 versionné ; absence de migration explicite = erreur, jamais effacement automatique

L'audit et la direction KMP/SwiftUI + Supabase validée sont documentés dans
[docs/production-audit.md](docs/production-audit.md). Ces composants ne sont pas
encore implémentés. La v1 reste une application Android locale.

## Build local

Nécessite JDK 17 et Android SDK 35. Le Wrapper télécharge Gradle 8.9 avec
vérification SHA-256. Android Studio est facultatif pour la ligne de commande.

```
./gradlew testDebugUnitTest lintDebug assembleDebug assembleRelease
```

L'APK debug est généré dans `app/build/outputs/apk/debug/`.
Sous Windows, utiliser `./gradlew.bat`. Configurer `ANDROID_HOME` ou `sdk.dir`
dans un fichier `local.properties` non versionné.

La compilation et les tests de ce chantier sont exécutés sur GitHub Actions.
Les artefacts `verification-reports` contiennent les résultats JUnit, le lint et
les schémas Room ; `clipsort-debug-apk` contient l'APK installable de développement.
La release vérifie R8 mais n'est pas signée pour publication. Les tests Room
utilisent Robolectric avec SQLite ; ils ne remplacent pas les tests UI sur appareil.

### Évolution de la base de données

Le schéma v1 dans `app/schemas/` est généré par Room, pas écrit à la main.
Pour modifier la structure : incrémenter la version, ajouter une migration explicite,
tester la conservation des données depuis chaque version distribuée et versionner
le nouveau schéma généré. Ne pas réécrire les anciens schémas. La CI refuse un
schéma généré non versionné. Aucun changement de structure n'est introduit dans
ce premier incrément, donc aucune migration v1 → v2 n'est encore nécessaire.

## Mise en ligne sur GitHub

Depuis le dossier du projet :

```
git init
git branch -M main
git add .
git commit -m "Initial commit: architecture MVVM ClipSort"
```

Puis crée un repo vide sur https://github.com/new (sans README ni .gitignore, pour éviter un conflit),
et relie-le :

```
git remote add origin https://github.com/<ton-compte>/ClipSort.git
git push -u origin main
```

## Licence

Publié sous licence **Apache 2.0** — voir le fichier `LICENSE`. Utilisation, modification
et redistribution libres, y compris commerciale, avec obligation de conserver la notice
de copyright et de mentionner les modifications apportées.

## Distribution open source (F-Droid)

Le Play Store n'est pas adapté à une distribution open source classique. Pour une app
Android FOSS, **F-Droid** est le canal naturel : gratuit, pas de compte développeur, pas
de vérification d'identité — seule la source doit être publique et sous licence libre
(ce qui est le cas ici).

Étapes :
1. Rendre le repo GitHub public
2. Créer un tag de version (`git tag v1.0.0 && git push --tags`) — F-Droid build depuis un tag, pas depuis `main`
3. Vérifier l'absence de dépendances non-libres : ce projet n'utilise que des bibliothèques
   AndroidX/Jetpack (Room, Compose, Hilt), toutes Apache 2.0 — aucun service Google
   propriétaire (pas de Firebase, pas de Play Services), donc conforme aux critères F-Droid
4. Soumettre une demande d'inclusion via **https://gitlab.com/fdroid/rfp/-/issues/new**
   (Requests For Packaging), en indiquant l'URL du repo et la licence
5. L'équipe F-Droid crée ensuite le fichier de métadonnées de build et publie l'app une
   fois la demande validée — délai variable (bénévoles), compter plusieurs semaines

Rien n'empêche de publier en parallèle sur GitHub Releases (APK signé téléchargeable
directement) pendant que la demande F-Droid est en cours.
