# Contagio Client

* **contagio-client**
* **contagio-server**

# Contagio Server

## Selbstsigniertes Zertifikat erzeugt für Test
```
# create ca key and certificate
# Subject: C = DE, ST = Bavaria, L = Munich, O = Contagio, OU = Development, CN = Contagio Root Certificate, emailAddress = stefan.t42@gmx.de
openssl genrsa -out contagioRootCA.key 2048
openssl req -x509 -new -nodes -key contagioRootCA.key -sha256 -days 1024 -out contagioRootCA.crt
openssl x509 -in contagioRootCA.crt -text -noout

# create client key and certificate sign request
openssl genrsa -out www-contagio-de.key 2048
openssl req -new -key www-contagio-de.key -out www-contagio-de.csr

# sign client csr and create ca-bundle
openssl x509 -req -in www-contagio-de.csr -CA contagioRootCA.crt -CAkey contagioRootCA.key -CAcreateserial -out www-contagio-de.crt -days 365 -sha256 -extfile ./openssl.conf -extensions v3_req
openssl x509 -in www-contagio-de.crt -noout -text
cat contagioRootCA.crt www-contagio-de.crt > www-contagio-de.ca-bundle
```

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
