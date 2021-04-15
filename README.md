# Contagio Client

* **contagio-client**
* **contagio-server**

# Contagio Server

## Spring Boot Konfigurieren für https

```
openssl pkcs12 -export -in www-contagio-de.crt -inkey www-contagio-de.key -out www-contagio-de.p12 -name contagio -CAfile contagioRootCA.crt -caname root
keytool -importkeystore -deststorepass contagio123 -destkeypass contagio123 -destkeystore contagio.jks -srckeystore www-contagio-de.p12 -srcstoretype PKCS12 -srcstorepass contagio123 -alias contagio
keytool -import -trustcacerts -alias root -file contagioRootCA.crt -keystore contagio.jks -storepass contagio123

keytool -list -keystore contagio.jks

```

## Lokalen DNS manipulieren
auf macos /etc/hosts editieren
```
192.168.169.59  www.contagio.de
```

## Mongodb
MongoDb in Dockercontainer starten
```
docker run -d --name mongodb -p 27017:27017 mongo:4.4.4
```

# Links
PKPass Validator
https://pkpassvalidator.com/
