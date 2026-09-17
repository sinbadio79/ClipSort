#!/usr/bin/env bash
set -euo pipefail

# Signing is separate from Gradle: build scripts never receive the private key.
: "${CLIPSORT_RELEASE_KEYSTORE_BASE64:?Missing stable release keystore secret}"
: "${CLIPSORT_RELEASE_STORE_PASSWORD:?Missing release keystore password secret}"
: "${RUNNER_TEMP:?Missing runner temporary directory}"
: "${ANDROID_HOME:?Missing Android SDK}"
input_apk="${1:?Unsigned APK path required}"
output_apk="${2:?Signed APK path required}"
script_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
expected="$(tr -d '\r\n' < "$script_dir/../signing/release-certificate.sha256")"
[[ "$expected" =~ ^[0-9a-f]{64}$ ]] || { echo 'Invalid pinned certificate fingerprint' >&2; exit 1; }

umask 077
signing_dir="$(mktemp -d "$RUNNER_TEMP/clipsort-signing.XXXXXX")"
keystore="$signing_dir/release.p12"
trap 'rm -f -- "$keystore"; rmdir -- "$signing_dir"' EXIT
printf '%s' "$CLIPSORT_RELEASE_KEYSTORE_BASE64" | base64 --decode > "$keystore"
unset CLIPSORT_RELEASE_KEYSTORE_BASE64

apksigner="$ANDROID_HOME/build-tools/35.0.0/apksigner"
mkdir -p -- "$(dirname -- "$output_apk")"
"$apksigner" sign --ks "$keystore" --ks-type PKCS12 --ks-key-alias clipsort \
    --ks-pass env:CLIPSORT_RELEASE_STORE_PASSWORD \
    --key-pass env:CLIPSORT_RELEASE_STORE_PASSWORD \
    --out "$output_apk" "$input_apk"
unset CLIPSORT_RELEASE_STORE_PASSWORD

"$apksigner" verify --verbose --print-certs "$output_apk" > "$output_apk.certificate.txt"
actual="$(sed -n 's/^Signer #1 certificate SHA-256 digest: //p' "$output_apk.certificate.txt")"
if [[ "$actual" != "$expected" ]]; then
    echo 'APK certificate does not match the pinned release certificate' >&2
    exit 1
fi
(
    cd -- "$(dirname -- "$output_apk")"
    sha256sum -- "$(basename -- "$output_apk")" > "$(basename -- "$output_apk").sha256"
)
echo "Verified stable release certificate SHA-256: $actual"
