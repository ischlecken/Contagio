# Contagio Client

* **contagio-client**
* **contagio-server**


## Vorbereitung

## Selbstsigniertes Zertifikat erzeugt für Test
```
# create ca key and certificate
openssl genrsa -out fritzboxRootCA.key 2048
openssl req -x509 -new -nodes -key fritzboxRootCA.key -sha256 -days 1024 -out fritzboxRootCA.crt\n\n

# create client key and certificate sign request
openssl x509 -in fritzboxRootCA.crt -text -noout
openssl genrsa -out gy-fritz-box.key 2048
openssl req -new -key gy-fritz-box.key -out gy-fritz-box.csr

# sign client csr and create ca-bundle
openssl x509 -req -in gy-fritz-box.csr -CA fritzboxRootCA.crt -CAkey fritzboxRootCA.key -CAcreateserial -out gy-fritz-box.crt -days 365 -sha256 -extfile ./openssl.conf -extensions v3_req
openssl x509 -in gy-fritz-box.crt -noout -text
cat fritzboxRootCA.crt gy-fritz-box.crt > gy-fritz-box.ca-bundle
```

## Links
