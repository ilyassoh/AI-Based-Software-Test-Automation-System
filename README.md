
# Système d'Automatisation de Tests Logiciels

Ce projet propose un logiciel basé sur l'IA capable de générer automatiquement des scénarios de tests unitaires et fonctionnels. 

## Fonctionnalités

1. **Clonage de projets GitHub** : Extraction des projets pour une analyse approfondie.
2. **Analyse AST (Abstract Syntax Tree)** : Analyse syntaxique pour détecter les classes et méthodes.
3. **Génération de tests** : Création de tests unitaires à partir des informations extraites.
4. **Visualisation des résultats** : Une interface utilisateur interactive pour afficher les statuts des tests.

## Architecture du Système

- **Eureka Server** : Eureka est utilisé pour la découverte de services. Tous les services, y compris le Gateway, le Service Projet et le Service Gemini, s'enregistrent auprès du serveur Eureka. Cela permet aux services de se découvrir dynamiquement, garantissant ainsi une communication fluide et une évolutivité.
- **Service Projet** : Ce service gère les processus liés aux projets, tels que la gestion des données de projet et l'interaction avec sa base de données dédiée. Lorsque la génération de tests est requise, il communique avec le service Gemini via un point de terminaison.
- **Base de données** : La base de données stocke les données persistantes gérées par le Service Projet. Cela inclut les informations sur les projets, les configurations et d'autres enregistrements associés.
- **Service Gemini** : Le service Gemini agit comme un module centralisé pour la génération de tests. Il reçoit des requêtes provenant de divers services métiers (comme le Service Projet) et traite les données nécessaires à la génération des tests. Les résultats sont ensuite renvoyés aux services demandeurs.
- **Gateway** : Le Gateway est le point d'entrée central pour toutes les requêtes provenant du frontend. Il achemine ces requêtes vers les services backend appropriés, tels que le Service Projet ou le Service Gemini. De plus, le Gateway assure que les requêtes sont authentifiées et équilibrées pour garantir une évolutivité et des performances élevées.
- **Frontend (Angular)** : L'interface utilisateur est développée avec Angular. Cette application frontend envoie des requêtes HTTP au Gateway pour interagir avec les services backend. Elle est responsable de fournir une expérience utilisateur fluide en récupérant et en affichant dynamiquement les données.

![image](https://github.com/user-attachments/assets/c6e1d305-91d7-4758-a421-1152fc709d46)



## Outils Utilisés

- **Spring Boot** : Framework backend pour les APIs et la logique métier.
- **Angular** : Développement de l'interface utilisateur.
- **MySQL** : Base de données relationnelle pour stocker les projets et résultats.
- **Docker** : Conteneurisation de l'application.
- **Postman** : Outil pour tester les APIs.
- **IntelliJ IDEA** et **VSCode** : Environnements de développement intégrés.

### Prérequis :

1. *Git :*
   - Assurez-vous que Git est installé. Sinon, téléchargez-le et installez-le depuis [git-scm.com](https://git-scm.com/).

2. *XAMPP :*
   - Installez XAMPP depuis [apachefriends.org](https://www.apachefriends.org/).
   - Démarrez les serveurs Apache et MySQL dans XAMPP.
   - Assurez-vous que MySQL utilise le port 3306.

3. *Node Js Version   :*
   - Installez node depuis [node](https://nodejs.org/en/download/package-manager).

### Configuration du Backend :

1. *Cloner le Projet :*
```bash
   git clone https://github.com/Oussama-benrkia/Testunit.git
   cd Testunit
```

2. *Installer les Dépendances Backend :*
   - Ouvrez un terminal dans le dossier du projet backend.
   - Exécutez les commandes suivantes :
```bash
   mvn clean install
```

3. *Exécuter le Backend :*
   - Démarrez vos serveurs Apache et MySQL via XAMPP.
   - Lancez l'application Spring Boot (Eureka -> Projet -> Gemini -> Gateway). La base de données et les entités seront créées automatiquement.

### Configuration du Frontend :

1. *Installer Node.js et Angular :*
   - Ouvrez un nouveau terminal pour le projet frontend.
cdd    - Installez Angular CLI globalement : `npm install -g @angular/cli`.

2. *Installer les Dépendances Frontend :*
   - Exécutez les commandes suivantes dans le dossier du projet frontend :
```bash
   npm install
```
- Si vous rencontrez des erreurs pendant l'installation, utilisez la commande suivante :
```bash
   npm install --save --legacy-peer-deps
```

3. *Exécuter le Frontend :*
   - Après avoir installé les dépendances, lancez le serveur de développement Angular :
```bash
   ng serve
```

- Accédez à l'interface frontend à [http://localhost:4200](http://localhost:4200) dans votre navigateur.

Votre projet full-stack devrait maintenant être opérationnel en local. Si vous rencontrez des problèmes, consultez les journaux de la console pour les messages d'erreur et assurez-vous que toutes les dépendances et prérequis sont correctement installés.

## Vidéo de Démonstration

Cliquez sur le lien ci-dessous pour regarder une vidéo de démonstration :
[click ici :](https://drive.google.com/file/d/1wK-G16si_1OjPYiB6nUjni810F4J0DZb/view)


