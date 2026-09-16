# Jarvis — assistant vocal Android

Un assistant personnel façon Iron Man : tu lui parles, il te répond à voix haute, et il peut
agir réellement sur ton téléphone (ouvrir des apps, lire tes notifications, appeler/texter un
contact, régler le volume/la luminosité) — toujours avec ton accord pour les actions sensibles.
Le cerveau de Jarvis est **Google Gemini**, via son forfait gratuit (aucune carte bancaire
requise).

## Ce que c'est (et ce que ça n'est pas)

Android bloque volontairement le contrôle silencieux et total d'un téléphone par une app tierce
(durcissement de la vie privée depuis Android 10/13). Jarvis fait donc de son mieux dans ce cadre :

- **Conversation vocale** : reconnaissance vocale + Gemini comme cerveau + synthèse vocale.
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
4. Dans **Paramètres** (icône en haut à droite), colle ta clé API Google AI :
   - Va sur https://aistudio.google.com/apikey (compte Google normal, **aucune carte
     bancaire requise** pour le forfait gratuit).
   - Clique sur "Create API key", copie la clé (elle commence par `AIza...`).
   - Colle-la dans Jarvis et choisis le modèle (Gemini 2.0 Flash par défaut, dans le forfait
     gratuit).

Le forfait gratuit de Google AI Studio a des limites de requêtes par minute/jour - largement
suffisantes pour un usage personnel, mais si tu discutes énormément avec Jarvis tu peux être
temporairement limité (Gemini répondra alors avec une erreur de quota, réessaie un peu plus
tard).

## Structure du projet

```
app/src/main/java/com/jarvis/assistant/
  MainActivity.kt              — navigation (onboarding / chat / réglages)
  ui/                           — écrans Compose
  viewmodel/ChatViewModel.kt    — boucle de conversation + mode continu
  network/GeminiClient.kt       — appel à l'API Gemini (REST) + boucle d'outils
  network/GeminiSchemas.kt      — les outils que Gemini peut appeler
  tools/                        — implémentation réelle de chaque outil (apps, téléphone, réglages, notifs)
  service/                      — service d'écoute des notifications
  voice/                        — reconnaissance vocale et synthèse vocale
  data/                         — stockage chiffré de la clé API + aide sur les permissions
```

## État de ce projet

Compile et tourne (validé via un build GitHub Actions bout-en-bout, APK généré avec succès).
L'intégration Gemini appelle directement l'API REST publique (`generativelanguage.googleapis.com`)
en JSON, faute de SDK Kotlin/Android officiel activement maintenu pour l'API Gemini — cette
partie n'a pas pu être testée par un vrai appel réseau depuis l'environnement de développement
(domaines Google bloqués), donc teste bien une conversation complète après la première
installation et signale toute erreur inattendue venant de Gemini.

## Pistes pour la suite

- Mot d'activation vocal en continu (service en avant-plan + modèle de wake-word léger).
- Historique de conversation persistant entre les lancements de l'app.
- Intégration domotique (si tu as des appareils compatibles Google Home / Matter).
