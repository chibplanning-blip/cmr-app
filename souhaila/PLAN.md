# Projet Souhaila – Blanchisserie / repassage à domicile

> Chiffres = **estimations** (France, ordre de grandeur 2026) à confirmer par devis fournisseurs.

## 1. Concept
Collecte à domicile → lavage, séchage, repassage (vêtements, draps) → livraison sous 48 h. Commande + compte client sur le site (`index.html`).

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
**Budget machines : ~8 000 € en occasion / ~15 000 € en neuf** (2 laves + 2 sèches + calandre + presse) + ~1 500 € de petit matériel.

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

## 6. Point site internet
`souhaila/index.html` = **démo fonctionnelle** : tarifs, calcul de commande, inscription/connexion (email + mot de passe haché), historique. Les données sont stockées dans le navigateur uniquement. Pour le **vrai lancement** il faut un back-end (ex. Supabase/Firebase) + paiement (Stripe) + emails de confirmation + mentions légales/CGV/RGPD.

## 7. Prochaines étapes
1. Choisir le nom définitif et le statut (micro-entreprise, SASU…) – partie administrative.
2. Demander 3 devis occasion/neuf (calandre, machines) – ex. fournisseurs pro de blanchisserie.
3. Trouver un local (normes eaux usées, ventilation, ERP).
4. Brancher le site sur un vrai back-end et paiement en ligne.
