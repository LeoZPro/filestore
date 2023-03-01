## Introduction

## Prérequis

### Keycloak Server

Notre filestore est conçu pour utiliser une authentification externe. Nous avons choisi le serveur d'authentification (IdP) Keycloak.
Pour cela, une instance de keycloak doit être installée et configurée. Cette instance est commune à tous les composants du OneClickFilestore.
Nous pouvons utiliser une image docker de keycloak :

Un documentation est disponible ici :

https://www.keycloak.org/getting-started/getting-started-docker
https://www.keycloak.org/server/containers

Je vous conseille de configurer un volume ce qui vous permettra de détruire le conteneur et de retrouver facilement vos données lors d'une mise à jour par exemple.

```bash
docker run --name keycloak.fs -p 8080:8080 -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin123 -v /var/docker.volumes/keycloak.miage/data:/opt/keycloak/data quay.io/keycloak/keycloak:latest start-dev
```

Il faudra créer le royaume "Miage.22" dans lequel il faudra déclarer un client "filestore".
  - Connectez vous à l'interface d'admin : http://localhost:8080/admin
  - Créez un royaume Miage.22
  - Paramétrez le royaume pour autoriser la création de compte (User Registration)
  - Ajoutez un rôle user au royaume
  - Paramétrez le royaume pour assigner le rôle 'user' aux nouveaux utilisateurs créés (User Registration -> Assign rôle)
  - Créez un client 'filestore' dans le royaume. Ajoutez * dans les redirect urls et dans les web origins.

Le royaume est prêt.

### Consul Server

Pour que les composants puissent se découvrir et communiquer entre eux, nous allons également utiliser un annuaire de service commun à tous : Consul.

https://hub.docker.com/_/consul/

```bash
docker run -d --name consul.fs -p 8500:8500 -p 8600:8600 -e CONSUL_BIND_INTERFACE=eth0 consul
```

Consul tournera sur le port 8500, le port 8600 quant à lui sert à proposer un service de DNS que nous n'utiliserons pas pour le moment.

### PostgreSQL Server

Pour que le filestore fonctionne, nous avons choisi d'utiliser une base de donnée PostgreSQL dédiée et il faut donc un serveur permettant d'héberger cette base.

```bash
docker run -d --name postgres.fs -e POSTGRES_PASSWORD=postgres -p 15432:5432 postgres
```

Il faut alors se connecter au serveur de base de donnée pour créer un rôle filestore ainsi qu'une base de donnée filestore via un client (pgadmin4) ou en ligne de commande avec psql (en local ou dans le conteneur)

```bash
psql -h localhost -p 5432 -U postgres
```

Les commandes SQL suivantes permettent de créer un user et une base de donnée : 

```sql
CREATE USER filestore;
ALTER USER filestore PASSWORD 'filestore';
CREATE DATABASE filestore;
GRANT ALL PRIVILEGES ON DATABASE filestore TO filestore;    
```

## Filestore

Chaque instance du filestore devra pouvoir fonctionner dans l'environnement cible avec consul, keycloak et une base postgresql dédiée. 
Pour cela, il est nécessaire de configurer le serveur d'application wildfly en conséquence, notamment pour Keycloak.
De plus, notre application utilise des sources de données et un topic de messages qu'il faut également configurer dans wildfly.

### Configuration de wildfly (via standalone.xml)

#### OIDC

Pour que Wildfly puisse valider les jetons d'authentification via Keycloak (Bear Token) il est nécessaire de configurer l'authentification OIDC. Depuis Wildfly 25, le support de OIDC est standard dans Elytron (couche de sécurité de wildfly)

http://www.mastertheboss.com/jbossas/jboss-security/secure-wildfly-applications-with-openid-connect/

Il suffit de créer un fichier oidc.json dans le dossier WEB-INF et de configurer l'authentification dans web.xml. 
Il existe cependant un autre moyen depuis le fichier standalone-full.xml et qui permet de modifier la configuration OIDC sans avoir à recompiler le projet (bien utile pour déployer dans le cloud) : 

```xml
<server xmlns="urn:jboss:domain:19.0">
    (...)
    <subsystem xmlns="urn:wildfly:elytron-oidc-client:1.0">
        <secure-deployment name="filestore.war">
            <provider-url>http://localhost:8080/realms/Miage.22</provider-url>
            <ssl-required>none</ssl-required>
            <client-id>filestore</client-id>
            <public-client>true</public-client>
            <confidential-port>0</confidential-port>
        </secure-deployment>
    </subsystem>
    (...)
</server>
```

#### DataSource et Topic JMS

Pour pouvoir utiliser la persistence, ainsi qu'un topic JMS il faut partir du fichier de config standalone-full.xml (il inclut le sous système JMS qui n'est pas présent dans le standalone.xml de base) et ajouter ceci :

```xml
<server xmlns="urn:jboss:domain:19.0">
    (...)
    <subsystem xmlns="urn:jboss:domain:datasources:7.0">
        <datasources>
            (...)
            <datasource jndi-name="java:jboss/datasources/FilestoreDS" pool-name="FilestoreDS" enabled="true" use-java-context="true" statistics-enabled="${wildfly.datasources.statistics-enabled:${wildfly.statistics-enabled:false}}">
                <connection-url>jdbc:postgresql:localhost:15432/filestore</connection-url>
                <driver>postgresql</driver>
                <security>
                    <user-name>filestore</user-name>
                    <password>filestore</password>
                </security>
            </datasource>
            <drivers>
                <driver name="h2" module="com.h2database.h2">
                    <xa-datasource-class>org.h2.jdbcx.JdbcDataSource</xa-datasource-class>
                </driver>
                <driver name="postgresql" module="org.postgresql">
                    <driver-class>org.postgresql.Driver</driver-class>
                </driver>
            </drivers>
        </datasources>
    </subsystem>
    (...)
    <subsystem xmlns="urn:jboss:domain:messaging-activemq:13.1">
        <server name="default">
            (...)
            <jms-topic name="Notification" entries="java:/jms/topic/notification"/>
            (...)
        </server>
    </subsystem>
    (...)
    <subsystem xmlns="urn:wildfly:elytron-oidc-client:1.0">
        <secure-deployment name="filestore.war">
            <provider-url>http://localhost:8080/</provider-url>
            <ssl-required>none</ssl-required>
            <confidential-port>0</confidential-port>
            <client-id>filestore</client-id>
            <public-client>true</public-client>
        </secure-deployment>
    </subsystem>
</server>
```

### Configuration de wildfly (via le CLI)

-> TODO configurer les éléments nécessaires au filestore dans wildfly via le CLI (module postgresql avec driver, datasource, topic jms et config oidc)  

### Packaging docker du filestore

Le filestore peut être packagé comme un conteneur docker ; pour construire l'image il faut utiliser le plugin maven via la commande :

```bash
mvn clean package dockerfile:build
```

Il est alors possible de le lancer avec la commande suivante :

```bash
docker run -d --name=filestore -p 8280:8080  \
    -e WILDFLY_ADMIN_PASSWORD=filestore  \
    -e FILESTORE_OWNER=mmichu  \
    -e FILESTORE_CONSUL_HOST=172.17.0.1  \
    -e DB_USER=filestore  \
    -e DB_PASS=filestore  \
    -e DB_HOST=172.17.0.1  \
    -e DB_PORT=15432  \
    -e DB_NAME=filestore  \
    -e OIDC_PROVIDER_URL=http://172.17.0.1:8080/realms/Miage.22  \
    -e OIDC_CLIENT_ID=filestore  \
    -e FILESTORE_HOME=/opt/jboss/filestore  \
    jayblanc/filestore:22.12.1-SNAPSHOT
```

Question : Comment mettre les données du filestore dans un volume docker ?

### Déploiement sur dokku : 

#### Install IdP (keycloak)  **une seule fois pour tous**

ssh dokku apps:create auth
ssh dokku config:set auth KEYCLOAK_ADMIN=admin KEYCLOAK_ADMIN_PASSWORD=tagada54 DOKKU_DOCKERFILE_START_CMD="start-dev"
ssh dokku storage:ensure-directory --chown heroku auth
ssh dokku storage:mount auth /var/lib/dokku/data/storage/auth:/opt/keycloak/data
ssh dokku git:from-image auth quay.io/keycloak/keycloak:20.0.1
ssh dokku proxy:ports-clear auth
ssh dokku proxy:ports-add auth http:80:8080

#### Install Consul  **une seule fois pour tous**

ssh dokku apps:create consul
ssh dokku config:set consul CONSUL_BIND_INTERFACE=eth0
ssh dokku proxy:ports-add consul http:8500:8500
ssh dokku git:from-image consul consul

#### Install postgresql plugin  **une seule fois pour tous**

sudo dokku plugin:install https://github.com/dokku/dokku-postgres.git postgres

#### Uploader votre image docker

//TODO via registre (docker push) ou via ssh 

#### Install filestore

ssh dokku apps:create jayblanc-filestore
ssh dokku postgresq:create jayblanc-filestore-db
ssh dokku config:set jayblanc-filestore WILDFLY_ADMIN_PASSWORD=filestore FILESTORE_OWNER=jayblanc FILESTORE_CONSUL_HOST=consul.miage22.jayblanc.fr DB_USER=postgres DB_PASS=93427d9d7ad9d97ef8bb4e299d1df5a8 DB_HOST=dokku-postgres-jayblanc-filestore-db DB_PORT=5432 DB_NAME=jayblanc_filestore_db OIDC_PROVIDER_URL=http://auth.miage22.jayblanc.fr/realms/Miage.22 OIDC_CLIENT_ID=filestore FILESTORE_HOME=/opt/jboss/filestore
ssh dokku storage:ensure-directory --chown heroku jayblanc-filestore-data
ssh dokku storage:mount jayblanc-filestore /var/lib/dokku/data/storage/jayblanc-filestore-data:/opt/keycloak/data
ssh dokku git:from-image jayblanc-filestore jablanc/filestore:22.12.1-SNAPSHOT
ssh dokku proxy:ports-clear jayblanc-filestore
ssh dokku proxy:ports-add jayblanc-filestore http:80:8080