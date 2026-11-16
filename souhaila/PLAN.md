# Projet Souhaila – Blanchisserie / repassage à domicile

> Chiffres = **estimations** (France, ordre de grandeur 2026) à confirmer par devis fournisseurs.

## 1. Concept
Collecte à domicile → lavage, séchage, repassage (vêtements, draps) → livraison sous 48 h. Commande + compte client sur le site (`index.html`).

## 1 bis. Clientèle visée
| Segment | Besoin | Fréquence | Prix indicatif |
|---|---|---|---|
| **Particuliers** | Lavage/repassage vêtements et draps | ponctuelle ou abonnement | 3,50 €/kg ; repassage à la pièce |
| **Restaurants / hôtels** | Nappes, serviettes, torchons, tabliers, draps, serviettes de bain | 2–3 tournées/semaine | 1,80–2,50 €/kg (selon volume et repassage plat) |
| **Maisons de repos / hôpitaux / cliniques** | Draps, alèses, blouses, linge de résidents | quotidien ou 3×/semaine | 2,20–3 €/kg + marquage nominatif |
| **Sociétés** | Tenues de travail, serviettes | hebdomadaire | abonnement mensuel |

**Points clés par segment**
- *Restaurants/hôtels* : gros volumes réguliers = revenu stable ; calandre indispensable (nappes, draps). Livraison tôt le matin.
- *Santé (maisons de repos, hôpitaux)* : le plus exigeant mais le plus rentable à long terme. Il faut : lavage thermique ou désinfectant (60–90 °C ou chimique validé), **séparation stricte linge sale / linge propre** (locaux et véhicules, sens de marche en avant), traçabilité des lots et marquage nominatif des résidents, et idéalement la **certification RABC / norme EN 14065** (souvent exigée par les appels d'offres hospitaliers). Prévoir machines à **barrière sanitaire** (chargement côté sale, déchargement côté propre) → +4 000 à 10 000 € de matériel/aménagement et un local avec deux zones.
- *Particuliers* : bon pour démarrer vite et se faire connaître, marges fortes mais volumes faibles.
- Les clients pro se facturent **à la fin du mois** (factures + contrat) ; paiement en ligne pour les particuliers.

**Stratégie conseillée** : démarrer avec particuliers + restaurants/hôtels (pas de norme médicale), puis ajouter maisons de repos une fois le local en 2 zones et la procédure d'hygiène en place ; l'hôpital public passe en général par appel d'offres.
**Capacité** : 2 lave‑linge de 14 kg + 2 sèche‑linge ≈ 150–200 kg de linge/jour. Un seul contrat de maison de repos de 60 lits ≈ 80–100 kg/jour. Prévoir une machine de plus (ou un 3e lave‑linge de 25 kg) dès le premier gros contrat.

## 2. Équipement nécessaire
| Poste | Modèle type | Neuf | Occasion |
|---|---|---|---|
| Lave-linge pro 10–14 kg (x2 pour débuter) | Miele PWM, Electrolux, Primus | 2 500–4 000 € | 1 000–1 800 € |
| Sèche-linge pro 14–16 kg (x2) | Miele PDR, Electrolux | 2 500–4 000 € | 1 000–1 800 € |
| **Calandre repasseuse (draps, housses, nappes)** – le plus rapide pour le plat : 1 drap en ~1 min | 65–80 cm de large | 3 000–6 000 € | 1 500–3 000 € |
| Presse à chemises / table aspirante chauffante + chaudière vapeur | Silter, Comel, Pony | 1 500–3 500 € | 700–1 500 € |
| Pliage, chariots, cintres, étagères, sacs | – | 500–1 000 € | – |
| Véhicule utilitaire (ou leasing / auto-entrepreneur livreur) | – | 150–300 €/mois | – |

**Solution repassage rapide** : calandre pour tout le plat (draps, housses, taies) + presse/table aspirante à vapeur pour chemises et pantalons. Un drap passe en 1 minute au lieu de 8–10 au fer.
**Machines connectées : +~200 € (modules).** 
**Budget machines : ~8 000 € en occasion / ~15 000 € en neuf** (2 laves + 2 sèches + calandre + presse) + ~1 500 € de petit matériel.

## 2 bis. Machines connectées à l'application (fin de cycle)
Objectif : quand une machine finit son cycle, l'appli prévient Souhaila (et met à jour le statut de la commande du client).

**Option recommandée – prise/relais connecté sur chaque machine (marche avec n'importe quelle machine, même d'occasion)**
- Un module de mesure de puissance Wi‑Fi (type Shelly Plug S / Shelly Pro 1PM, ~20–45 € pièce ; pour une machine triphasée, module avec pince ampèremétrique) par lave‑linge, sèche‑linge, calandre et presse.
- Principe : puissance > seuil = cycle en cours ; puissance < seuil pendant ~2 min = **cycle terminé**. Le module envoie un webhook au serveur du site.
- Le serveur change le statut de la commande (« Lavage terminé », « Séchage terminé »…) et envoie une notification push/SMS/e‑mail à Souhaila et, si voulu, au client.
- Coût : 4 à 6 modules ≈ **100–250 €**.

**Option 2 – machines pro nativement connectées** (Miele Professional, Electrolux Professional myPro) : alertes natives mais machines et abonnement plus chers (+500 à +1 500 €/machine). À envisager seulement en neuf.

Dans la démo du site, le bouton « 🔌 Simuler fin de cycle » (dans Mon compte → commandes) montre ce comportement ; en production, c'est le webhook des machines qui le déclenche. Cela nécessite le back‑end (point 6).

## 3. Prix de revient (hypothèses)
Électricité 0,25 €/kWh · eau 5 €/m³ · main-d'œuvre chargée 16 €/h.

**Lavage + séchage + pliage, par kg**
- Lessive/adoucissant : 0,15 € · Eau (~10 L) : 0,05 € · Électricité lavage+séchage : 0,20 € → **consommables 0,40 €**
- Main-d'œuvre (tri, chargement, pliage ≈ 3 min/kg) : **0,80 €**
- Amortissement machines (~0,15 €) → **≈ 1,35 €/kg**  → vendu 3,50 €/kg : **marge brute ≈ 2,15 €/kg (61 %)**

**Repassage, par pièce**
| Pièce | Temps | Main-d'œuvre | Vapeur/élec. | **Revient** | Prix | Marge |
|---|---|---|---|---|---|---|
| Chemise (presse) | 3 min | 0,80 € | 0,05 € | **0,85 €** | 2,50 € | 1,65 € |
| Pantalon | 4 min | 1,07 € | 0,05 € | **1,12 €** | 3,50 € | 2,38 € |
| Drap 2 pl. (calandre) | 1,5 min | 0,40 € | 0,10 € | **0,50 €** | 5,00 € | 4,50 € |

**Collecte/livraison** : ~2,50 € de coût par commande (carburant + 12 min) → facturée 3 €, offerte dès 30 €.

## 4. Charges fixes mensuelles (estimation)
| Poste | €/mois |
|---|---|
| Local (atelier ~40 m², eau/élec. raccordées) | 600 |
| Véhicule + carburant | 350 |
| Assurances (RC pro, local) | 100 |
| Comptable | 100 |
| Site, hébergement, outils | 40 |
| **Total** | **≈ 1 200** |

## 5. Seuil de rentabilité
Panier moyen estimé : 6 kg lavés (21 €) + 5 chemises (12,50 €) + 1 drap (5 €) ≈ **38 €**, coût variable ≈ **17 €** → marge ≈ **21 €/commande**.
Seuil = 1 200 € / 21 € ≈ **57 commandes/mois** (~2 par jour) pour couvrir les charges fixes, hors salaire de Souhaila. Objectif réaliste : 120 commandes/mois ≈ 1 300 € de bénéfice avant rémunération/impôts.

## 5 bis. Effet des clients professionnels
Pro à 2,40 €/kg avec coût variable ≈ 1,10 €/kg (volume, moins de collecte unitaire) → marge ≈ 1,30 €/kg. Un contrat de 80 kg/jour × 26 jours ≈ 2 080 kg/mois ≈ **2 700 € de marge/mois** à lui seul : un ou deux contrats pro couvrent les charges fixes (1 200 €).

## 6. Point site internet
`souhaila/index.html` = **démo fonctionnelle** : tarifs, calcul de commande, inscription/connexion (email + mot de passe haché), avec type de compte (particulier, restaurant/hôtel, santé, société) et demande de devis pour les pros, historique. Les données sont stockées dans le navigateur uniquement. Pour le **vrai lancement** il faut un back-end (ex. Supabase/Firebase) + paiement (Stripe) + emails de confirmation + mentions légales/CGV/RGPD.

## 7. Prochaines étapes
1. Choisir le nom définitif et le statut (micro-entreprise, SASU…) – partie administrative.
2. Demander 3 devis occasion/neuf (calandre, machines) – ex. fournisseurs pro de blanchisserie.
3. Trouver un local (normes eaux usées, ventilation, ERP).
4. Brancher le site sur un vrai back-end et paiement en ligne.
