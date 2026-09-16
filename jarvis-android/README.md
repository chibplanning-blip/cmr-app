# Jarvis — assistant vocal Android

Un assistant personnel façon Iron Man : tu lui parles, il te répond à voix haute, et il peut
agir réellement sur ton téléphone (ouvrir des apps, lire tes notifications, appeler/texter un
contact, régler le volume/la luminosité) — toujours avec ton accord pour les actions sensibles.

## Ce que c'est (et ce que ça n'est pas)

Android bloque volontairement le contrôle silencieux et total d'un téléphone par une app tierce
(durcissement de la vie privée depuis Android 10/13). Jarvis fait donc de son mieux dans ce cadre :

- **Conversation vocale** : reconnaissance vocale + Claude comme cerveau + synthèse vocale.
  Mode "conversation fluide" : après chaque réponse, Jarvis se remet à écouter automatiquement
  sans qu'il faille rappuyer sur le micro (comme une vraie discussion).
- **Ouvrir des apps** : fonctionne directement.
- **Lire les notifications récentes** : fonctionne, après autorisation manuelle (Android l'exige).
- **Appeler / envoyer un SMS** : fonctionne directement si les permissions sont accordées,
  toujours avec confirmation de ta part avant l'action.
- **Volume** : réglé directement.
- **Luminosité** : nécessite une autorisation spéciale ("Modifier les paramètres système").
- **Wi-Fi / Bluetooth / mode avion** : Android interdit aux apps de les basculer en silence
  depuis quelques années. Jarvis ouvre le bon écran/panneau pour que tu finisses d'un geste.
- **Pas de mot d'activation "Hey Jarvis" en continu** dans cette v1 : ça demanderait un service
  qui écoute le micro en permanence (batterie + vie privée). On part sur un bouton "parler", avec
  reprise automatique de l'écoute pendant une conversation.

## Mise en route

1. Ouvre le dossier `jarvis-android/` dans Android Studio (Gradle Sync se fait automatiquement ;
   s'il manque le wrapper Gradle, Android Studio propose de le créer — accepte).
2. Lance l'app sur un téléphone Android réel (ou un émulateur pour l'UI — le micro/les
   notifications/les appels ne marchent pas bien en émulateur).
3. Suis l'écran d'accueil pour accorder les permissions.
4. Dans **Paramètres** (icône en haut à droite), colle ta clé API Anthropic
   (créée sur https://console.anthropic.com) et choisis le modèle.
   - **Claude Opus 5** (par défaut) : le plus capable, aussi le plus cher.
   - **Claude Sonnet 5** : bon compromis pour un usage quotidien.
   - **Claude Haiku 4.5** : le moins cher, pour limiter la facture si tu parles beaucoup à Jarvis.

Un usage vocal fréquent consomme de vrais tokens API à chaque échange — surveille ta
consommation sur console.anthropic.com si tu utilises Opus 5 intensivement.

## Structure du projet

```
app/src/main/java/com/jarvis/assistant/
  MainActivity.kt              — navigation (onboarding / chat / réglages)
  ui/                           — écrans Compose
  viewmodel/ChatViewModel.kt    — boucle de conversation + mode continu
  network/ClaudeClient.kt       — appel à l'API Claude (SDK officiel Anthropic) + boucle d'outils
  network/ToolSchemas.kt        — les outils que Claude peut appeler
  tools/                        — implémentation réelle de chaque outil (apps, téléphone, réglages, notifs)
  service/                      — service d'écoute des notifications
  voice/                        — reconnaissance vocale et synthèse vocale
  data/                         — stockage chiffré de la clé API + aide sur les permissions
```

## État de ce projet

Ce squelette a été écrit avec soin (les appels au SDK Anthropic ont été vérifiés en compilant
un extrait isolé contre le vrai jar `anthropic-java`), mais **n'a pas pu être compilé en entier
dans cet environnement** car il n'y a pas de SDK Android installé ici. À l'ouverture dans Android
Studio, corrige les éventuelles erreurs de compilation restantes (noms de classes qui auraient
changé de version, imports manquants) avant le premier lancement — c'est un travail de mise au
point normal pour un projet Android généré hors IDE, pas une réécriture.

## Pistes pour la suite

- Mot d'activation vocal en continu (service en avant-plan + modèle de wake-word léger).
- Historique de conversation persistant entre les lancements de l'app.
- Intégration domotique (si tu as des appareils compatibles Google Home / Matter).
