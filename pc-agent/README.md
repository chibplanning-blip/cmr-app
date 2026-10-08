# Jarvis PC agent

Un petit serveur local (Python standard, aucune dépendance à installer) qui
permet à l'app Jarvis sur ton téléphone de contrôler cet ordinateur :
exécuter des commandes, lire des fichiers, lister un dossier, ouvrir une
app/fichier/lien.

## Démarrer

```bash
python3 jarvis_pc_agent.py
```

Au premier lancement, un jeton d'appairage aléatoire est généré et affiché
dans le terminal (aussi stocké dans `~/.jarvis-pc-agent/token`). Copie ce
jeton et l'adresse de la machine dans Jarvis, écran **Paramètres > Contrôle
du PC**.

Par défaut le serveur écoute sur le port `8765`, sur toutes les interfaces
(`--host 0.0.0.0`), ce qui le rend joignable depuis le réseau local. Change
le port avec `--port`.

## ⚠️ Sécurité - à lire avant de lancer ça

Ce serveur donne à quiconque connaît le jeton un contrôle **total** sur cet
ordinateur (n'importe quelle commande shell, avec tes propres droits). Ce
n'est pas un bac à sable.

- **Ne jamais exposer ce port directement sur Internet** (pas de redirection
  de port sur ta box internet, pas de `0.0.0.0` sur un VPS public).
- Pour une utilisation uniquement sur ton Wi-Fi domestique : connecte le
  téléphone au même réseau et utilise l'adresse IP locale de l'ordinateur
  (ex: `http://192.168.1.42:8765`).
- Pour un accès depuis l'extérieur (4G, autre réseau) : utilise un réseau
  privé type **Tailscale** (gratuit pour un usage perso) entre ton téléphone
  et ton PC, et renseigne l'adresse Tailscale de l'ordinateur dans Jarvis.
  N'utilise pas de tunnel public (ngrok, etc.) sans authentification
  supplémentaire.
- Le jeton est l'unique protection : traite-le comme un mot de passe. Pour
  le révoquer, supprime `~/.jarvis-pc-agent/token` et relance le serveur
  (un nouveau jeton sera généré - il faudra le remettre à jour dans l'app).
- `run_command` exécute vraiment la commande sur ta machine : c'est pour ça
  que Jarvis demande toujours une confirmation avant de l'utiliser.

## Endpoints

Tous les appels nécessitent l'en-tête `Authorization: Bearer <jeton>`.

| Méthode | Route          | Corps JSON          | Effet |
|---------|----------------|----------------------|-------|
| GET     | `/ping`        | -                    | Vérifie que l'agent répond |
| POST    | `/run_command` | `{"command": "..."}` | Exécute une commande shell (timeout 30s) |
| POST    | `/read_file`   | `{"path": "..."}`    | Lit un fichier texte |
| POST    | `/list_files`  | `{"path": "..."}`    | Liste un dossier |
| POST    | `/open_path`   | `{"path": "..."}`    | Ouvre un fichier/app/lien avec le programme par défaut |

## Lancer au démarrage de l'ordinateur (optionnel)

Pas encore automatisé par ce dépôt. Sur macOS/Linux, un service
`launchd`/`systemd --user` fonctionne bien ; sur Windows, le Planificateur
de tâches. À faire dans un second temps une fois la connexion testée
manuellement.
