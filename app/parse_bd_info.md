#Déscription de la base de données Parse de PikiChat

---

##USER
* **objectId** : auto par Parse
* **username** : automatique par le facebooklogin du SDK de Parse
* **password** : loginfb SDK Parse
* **authData** : loginfb SDK Parse
* **emailVerified** : `undifined`
* **facebookId** : le UserId de facebook
* **email** : email du User fb, peut être null !
* **first_name** : firstname du user fb
* **gender** : gender du user fb
* **hasSendWelcomePush** : `undifined`
* **has_phone_number** : `undifined`
* **has_rate** : false (pour débloquer couleur)
* **has_share** : false (pour débloquer couleur)
* **last_name** : lastname du user fb
* **last_session** : maj à chaque ouverture
* **location_name** : location du user fb
* **name** : name complet du user fb
* **nb_groups** : à maj en bg, juste pour les stat
* **nb_groups_created** : à maj en bg, juste pour les stat
* **nb_piki** : à maj en bg, juste pour les stat
* **phone_number** : num du user
* **significant_other** : `undifined`
* **timezone** : timezone du user fb
* **createdAt** : auto par Parse
* **updateAt** : auto par Parse
* **ACL** : pour la sécu, pour l'instant pas utilisé

##GROUP
* **objectId** : auto par Parse
* **add_friends** : plus utilisé, à mettre toujours à true
* **creator** : le USER qui a créé le group
* **facebook_id** : `undifined`
* **name** : nom du group
* **nb_membres** : à maintenir pour ne pas recalculer
* **nb_piki** : à maintenir pour ne pas recalculer
* **nb_prospects** : nb de personne invitée, à maintenir pour ne pas recalculer
* **type_color** : couleur piki du groupe [1-5]
* **createdAt** : auto par Parse
* **updateAt** : auto par Parse
* **ACL** : pour la sécu, pour l'instant pas utilisé

##MEMBER
* **objectId** : auto par Parse
* **has_invited** : plus utilisé, toujours à false
* **nb_view_unique** : `undifined`
* **prospect_facebook** : plus utilisé
* **prospect_name** : nom du prospect
* **prospect_phone_number** : num du prospect
* **user** : USER du Member
* **createdAt** : auto par Parse
* **updateAt** : auto par Parse
* **ACL** : pour la sécu, pour l'instant pas utilisé

##PIKI
* **objectId** : auto par Parse
* **group** : GROUP dans le quel est le piki
* **hasSendWelcomePush** : `undifined`
* **isSecret** : est incignito ?
* **nb_interactions** : `undifined`
* **nb_view** : `undifined`
* **nb_view_unique** : `undifined`
* **photo** : File de la photo
* **photograph** : USER qui a pris al photo
* **pushSend** : `undifined`
* **title** : `undifined`
* **createdAt** : auto par Parse
* **updateAt** : auto par Parse
* **ACL** : pour la sécu, pour l'instant pas utilisé
