# Signature stable des APK release

La clé RSA 3072 bits a été générée une seule fois le 17 septembre 2026.
Le certificat est valable 30 ans. Son empreinte publique SHA-256 est versionnée
dans `signing/release-certificate.sha256` ; ce fichier ne contient aucune clé privée.

Les secrets Actions du dépôt sont :

- `CLIPSORT_RELEASE_KEYSTORE_BASE64` : keystore PKCS12 chiffré, encodé en base64 ;
- `CLIPSORT_RELEASE_STORE_PASSWORD` : mot de passe aléatoire du keystore et de la clé.

L'alias est `clipsort`. Ne jamais générer une nouvelle clé pendant un build ni
remplacer ces secrets pour résoudre un problème de compilation. Une clé différente
empêcherait normalement la mise à jour des installations signées avec la précédente.

## CI

Après réussite des tests, lint, builds debug/release et contrôle du schéma, les push
et lancements manuels produisent `clipsort-release-apk`. Cet artefact contient
l'APK signé, son checksum SHA-256 et le rapport public de vérification du certificat.
Le job séparé de signature ne lance pas Gradle. Les secrets sont injectés uniquement
dans l'étape de signature ; le keystore temporaire est supprimé à la sortie.
Un secret absent ou un certificat différent de l'empreinte versionnée fait échouer
le job. Les PR compilent et testent sans lancer ce job ni utiliser les secrets.
Seuls les contributeurs de confiance doivent pouvoir pousser des workflows dans
ce dépôt : les secrets de dépôt sont accessibles aux workflows autorisés des branches.

`clipsort-debug-apk` reste un artefact de développement signé par une clé debug
éphémère. Pour installer puis mettre à jour ClipSort avec la même identité de
signature, utiliser exclusivement `clipsort-release-apk`. Passer d'un ancien APK
debug à cette release peut nécessiter une désinstallation et perdre les données
locales : conserver/exporter celles-ci avant toute désinstallation.

## Sauvegarde et récupération

Une sauvegarde privée existe sur le poste de provisionnement dans
`%LOCALAPPDATA%\ClipSort\signing` :

- `clipsort-release.p12` : keystore chiffré ;
- `password.dpapi` : mot de passe protégé avec DPAPI pour le compte Windows créateur ;
- `release-certificate.der` : certificat public.

Le dossier a une ACL restreinte au compte créateur. DPAPI n'est pas une sauvegarde
portable du mot de passe : avant de réinstaller Windows ou changer de poste,
exporter ce mot de passe vers un gestionnaire de secrets et sauvegarder le P12
séparément. Les secrets GitHub ne sont pas récupérables en clair depuis l'API.
Ne pas perdre l'accès au compte Windows sans avoir organisé cette copie indépendante.

La restauration consiste à réimporter le même P12 et son mot de passe dans les deux
secrets ci-dessus puis lancer la CI. L'empreinte épinglée vérifie qu'il s'agit de
la même identité. Ne jamais publier le P12 ni le mot de passe dans un artefact,
un log, une issue ou un commit.

Cette clé concerne la distribution directe des APK de ce dépôt. Play App Signing
et F-Droid peuvent utiliser d'autres clés selon leur configuration ; ne pas présumer
que des APK provenant de canaux différents sont interchangeables.
