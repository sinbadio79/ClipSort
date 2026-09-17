# Audit et trajectoire production

Audit du 17 septembre 2026, référence `85eb9c296886fb8534293fc110ef6294fb191ca0`.
Les observations ci-dessous viennent de la lecture du dépôt. Les résultats de CI
doivent être distingués de la couverture fonctionnelle et des tests sur appareils.

## Existant à conserver

Les couches domain/data/presentation, les interfaces repository, les mappers Room,
MVVM, Hilt et Compose constituent une base cohérente. Pas de justification pour
réécrire l'application Android. Les couches sont des packages d'un module Android,
pas encore des modules compilés séparément. Le domaine utilise `javax.inject` et
les implémentations utilisent l'horloge JVM : une extraction KMP exige des adaptations.

## Écarts observés

| Priorité | Observation | Conséquence / action |
| --- | --- | --- |
| Haute | Room v1, schéma non exporté, repli destructif | Exporter le schéma, retirer le repli, tester chaque future migration et toute la chaîne depuis v1. |
| Haute | CI limitée à main, tests unitaires et APK debug | Ajouter lint, intégration et release ; vérifier chaque push. |
| Haute | Sept tests domain, aucun test data/UI | Ajouter tests Room puis parcours partage, navigation et erreurs. |
| Haute | Le texte partagé complet devient `sharedUrl` | Extraire et valider le lien avant stockage ; conserver séparément le texte utile. |
| Haute | Détection par `contains` dans toute l'URL | Vérifier l'hôte et ses sous-domaines ; couvrir les domaines trompeurs. |
| Haute | Activity `singleTask`, sans `onNewIntent` | Tester un second partage lorsque l'activité existe déjà. |
| Haute | Aucun index d'unicité URL | Définir normalisation et traitement des doublons existants avant migration. |
| Haute | Appui sur un clip change son statut sans ouvrir le lien | Définir et tester le parcours de revisionnage. |
| Moyenne | Appui long supprime le clip directement | Prévoir action explicite accessible et confirmation/annulation. |
| Moyenne | Échecs de suppression ignorés, erreurs bibliothèque non affichées | Tester et afficher les erreurs sans perdre l'état. |
| Moyenne | Chaînes françaises en dur, statut visuel sans description | Ressources FR/EN, sémantique TalkBack, tailles de texte, audit contraste. |
| Moyenne | README annonce gradlew absent, état catégories obsolète | Ajouter wrapper reproductible et actualiser la documentation. |
| Moyenne | Fichier ProGuard référencé absent | Vérifier release avec R8, pas seulement debug. |

Pas encore de backend, synchronisation, IA, miniatures réelles, recherche full-text,
tags, catégories partagées, rappels, widgets, achats ou export. La distribution
F-Droid est décrite dans le README ; son acceptation effective n'a pas été vérifiée.

## Décision validée par le propriétaire

Kotlin Multiplatform pour le métier partagé, Compose Android conservé, SwiftUI iOS.
Supabase managé en région européenne, synchronisation optionnelle, usage local hors
connexion conservé. Validation reçue pendant cette session le 17 septembre 2026.

| Option mobile | Compromis pour ClipSort |
| --- | --- |
| KMP + UI natives (retenu) | Réutilise le Kotlin et l'UI Android. Nécessite interop Swift, adaptation de l'injection et intégrations natives partage/widgets. |
| Flutter | UI mutualisable, mais migration vers Dart et remplacement ou pontage de la v1. Coût de migration peu justifié ici. |
| Deux codebases natives | Intégration plateforme directe, mais règles, tests et synchronisation dupliqués à maintenir. |

| Backend | Compromis |
| --- | --- |
| Supabase (retenu) | PostgreSQL, règles d'accès RLS, option auto-hébergement. Le moteur de synchronisation hors ligne, conflits et suppressions reste à construire. |
| Firebase | Services mobiles intégrés et capacités hors ligne selon produit. Dépendance au fournisseur et vérification des SDK/licences/Play Services par produit pour F-Droid. |
| Custom | Contrôle maximal, mais authentification, exploitation, sauvegardes et surveillance à financer et maintenir. |

Le coût cloud dépendra des utilisateurs, stockage, trafic, miniatures, inférence IA
et sauvegardes : pas de tarif fixe promis. L'auto-hébergement déplace les coûts vers
l'exploitation ; ce n'est pas une option sans coût. Sources :
[KMP existant](https://kotlinlang.org/docs/multiplatform-mobile-integrate-in-existing-app.html),
[Flutter intégré](https://docs.flutter.dev/add-to-app),
[Supabase auto-hébergé](https://supabase.com/docs/guides/self-hosting),
[tarification Supabase](https://supabase.com/pricing),
[Firebase Android](https://firebase.google.com/docs/android/setup).

## Ordre des incréments proposés

1. Référence CI, schéma v1, protection des données, intégration Room et lint.
2. Partage robuste, ouverture des clips, doublons et migration testée ; tests UI.
3. Recherche full-text, tags et filtres croisés, ressources FR/EN et accessibilité.
4. Extraction KMP progressive et tranche iOS compilée sur macOS ; partage natif.
5. Authentification facultative, synchronisation, conflits, suppression et isolation
   entre utilisateurs ; tests RLS et catégories partagées.
6. Miniatures via sources autorisées, suggestions IA validées par l'utilisateur,
   quotas et comportement explicite si métadonnées indisponibles.
7. Rappels et widgets natifs, exports et droits premium après validation commerciale.
8. Recette appareils, audits confidentialité/accessibilité, signatures et publication.

L'enveloppe de 50 000 USD doit financer aussi QA, sécurité, design et exploitation.
Ce périmètre n'est pas une promesse de livraison intégrale en 2–3 mois : la vélocité
des premières tranches et l'accès aux plateformes vidéo détermineront les arbitrages.

## Qualité attendue

Objectif proposé : au moins 85 % des branches du domaine, 80 % des lignes data,
et couverture de tous les parcours critiques UI (partage, navigation, erreur,
suppression, achat/restauration). Ce sont des objectifs, pas des mesures actuelles.
Ils concentrent l'effort sur les règles et la persistance plutôt qu'un pourcentage
global trompeur incluant du code généré. Tester toutes les migrations depuis v1,
les conflits de sync et les accès inter-utilisateurs indépendamment du pourcentage.

Lint ne certifie ni WCAG AA ni TalkBack/VoiceOver : recette manuelle avec lecteurs
d'écran, grande police, contrastes mesurés et cibles tactiles sur les deux plateformes.

## Confidentialité et décisions encore nécessaires

Avant backend : identifier le responsable de traitement et contact, inventorier
compte/URLs/commentaires/tags/partages/journaux, documenter finalités et bases légales,
région effective, sous-traitants/DPA, transferts, rétention et effacement des sauvegardes.
Tester export des données, suppression du compte et révocation des accès partagés.
L'hébergement européen seul ne démontre pas la conformité RGPD.

L'IA ne doit pas recevoir silencieusement une bibliothèque ou transcription ; définir
les données minimales envoyées, le fournisseur, sa rétention et l'information utilisateur.
Les requêtes de miniatures divulguent aussi des informations aux serveurs contactés.
Analytics optionnels : évaluer Plausible/Matomo sans URLs, commentaires, tags ni
identifiants utilisateurs dans les événements. Ne pas présumer d'une exemption de
consentement uniquement à partir du nom du produit.

La monétisation reste à valider : quotas gratuits, prix, droits multi-appareils,
restauration, gestion des expirations et builds F-Droid sans SDK propriétaire.
Distinguer exports premium de présentation (PDF/Notion) et exercice gratuit des
droits d'accès/portabilité applicables ; pas de politique de confidentialité fictive.
Référence : [recommandations CNIL pour applications mobiles](https://www.cnil.fr/fr/recommandations-applications-mobiles).

## Dépendances externes

Builds/tests demandés sur GitHub Actions. iOS nécessite un runner macOS/Xcode et
des tests sur appareil ; signature/distribution nécessitent compte Apple, certificats
et profils. Publication Google et facturation nécessitent compte et configuration
correspondants. Supabase, fournisseur IA, analytics et Notion nécessiteront leurs
comptes/configurations ; les secrets devront rester côté serveur ou GitHub Secrets,
jamais dans le dépôt ni dans les applications clientes.
