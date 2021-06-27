#!/bin/sh

TEAM_ID=4V27BGKSLA
TOKEN_KEY_FILE_NAME=../stt-certs/cert/AuthKey_8F55355FW2.p8
AUTH_KEY_ID=8F55355FW2
TOPIC=de.contagio.Teststation
DEVICE_TOKEN=55f52e9c72bbca51fe925aad8f854cc97f98ba8471accbe63c924c20e053d7d4
# APNS_HOST_NAME=api.sandbox.push.apple.com
APNS_HOST_NAME=api.push.apple.com

# openssl s_client -connect "${APNS_HOST_NAME}":443

JWT_ISSUE_TIME=$(date +%s)
JWT_HEADER=$(printf '{ "alg": "ES256", "kid": "%s" }' "${AUTH_KEY_ID}" | openssl base64 -e -A | tr -- '+/' '-_' | tr -d =)
JWT_CLAIMS=$(printf '{ "iss": "%s", "iat": %d }' "${TEAM_ID}" "${JWT_ISSUE_TIME}" | openssl base64 -e -A | tr -- '+/' '-_' | tr -d =)
JWT_HEADER_CLAIMS="${JWT_HEADER}.${JWT_CLAIMS}"
JWT_SIGNED_HEADER_CLAIMS=$(printf "${JWT_HEADER_CLAIMS}" | openssl dgst -binary -sha256 -sign "${TOKEN_KEY_FILE_NAME}" | openssl base64 -e -A | tr -- '+/' '-_' | tr -d =)
AUTHENTICATION_TOKEN="${JWT_HEADER}.${JWT_CLAIMS}.${JWT_SIGNED_HEADER_CLAIMS}"
PAYLOAD="{\"aps\":{\"alert\":\"$1\"}}"

echo "sending $PAYLOAD ..."

/usr/bin/curl \
    --header "apns-topic: $TOPIC" \
     --header "apns-push-type: alert" \
     --header "authorization: bearer $AUTHENTICATION_TOKEN" \
     --data "$PAYLOAD" \
     --http2 https://${APNS_HOST_NAME}/3/device/${DEVICE_TOKEN}
