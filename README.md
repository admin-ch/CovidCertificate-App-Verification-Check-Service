# CovidCertificate-App-Verification-Check-Service

[![License: MPL 2.0](https://img.shields.io/badge/License-MPL%202.0-brightgreen.svg)](https://github.com/admin-ch/CovidCertificate-App-Verifier-Service/blob/main/LICENSE)
[![Build](https://github.com/admin-ch/CovidCertificate-App-Verification-Check-Service/actions/workflows/build.yml/badge.svg)](https://github.com/admin-ch/CovidCertificate-App-Verification-Check-Service/actions/workflows/build.yml)
[![Code Quality](https://sonarcloud.io/api/project_badges/measure?project=admin-ch_CovidCertificate-App-Verification-Check-Service&metric=alert_status)](https://sonarcloud.io/dashboard?id=admin-ch_CovidCertificate-App-Verification-Check-Service)

This project is released by the the [Federal Office of Information Technology, Systems and Telecommunication FOITT](https://www.bit.admin.ch/)
on behalf of the [Federal Office of Public Health FOPH](https://www.bag.admin.ch/).
The app design, UX and implementation was done by [Ubique](https://www.ubique.ch?app=github).

## Services
This service provides an API, which is consumed by other CovidCertificate backend services. It offers an endpoint to allow the decoding and verification of a given Covid Certificate.
It regularly sends a request to the [Verifier Service](https://github.com/admin-ch/CovidCertificate-App-Verifier-Service) to keep an up-to-date list of trusted DSCs with which Covid Certificates could be signed.

## Simple Verification

In order to allow the independent usage of the Verification-Check-Service without deeper knowledge of the source code, the repository includes a Dockerfile to start the service in a separate container. However, one needs to be in possession of an api-token to access the Verifier-Service.

### Starting the service

To build the image, clone the repository, `cd` into the cloned directory and run
```bash
docker build -t simple-verification .
```

Then start the container by running
```bash
docker run --name simple-verification --build-arg VERSION=... -e PROFILE=... -e APIKEY=... simple-verification
```
where `PROFILE` will is one of `DEV`, `ABN` or `PROD`, and `VERSION` is at least `v2.6.0-prerelease`.

The service is now exposed on port 8080.

### Verifying certificates

Certificates can be verified by sending POST requests to `/simple/verify`, where the payload should be JSON with the following format:
```json
{
  "hcert": "Base-45-encoded covid certificate with prefix 'HC1:' to be decoded and verified"
}
```

The endpoint will respond with status code 400 if the certificate couldn't be decoded. Otherwise, it will respond with status code 200 and the following JSON:
```json
{
  "certificate": {
    "version": "1.0.0",
    "person": {
      "familyName": "Müller",
      "standardizedFamilyName": "MUELLER",
      "givenName": "Chloé",
      "standardizedGivenName": "CHLOE"
    },
    "dateOfBirth": "28.12.1962",
    "formattedDateOfBirth": "28.12.1962"
  },
  "successState": "...",
  "errorState": "...",
  "invalidState": "..."
}
```
where only one of the three fields `successState`, `errorState` and `invalidState` is set to a non-null value, thereby indicating the corresponding verification-status.

## Contribution Guide

This project is truly open-source and we welcome any feedback on the code regarding both the implementation and security aspects.

Bugs or potential problems should be reported using Github issues.
We welcome all pull requests that improve the quality of the source code.
Please note that the app will be available with approved translations in English, German, French, Italian.

## Repositories

* Android App: [CovidCertificate-App-Android](https://github.com/admin-ch/CovidCertificate-App-Android)
* Android SDK: [CovidCertificate-SDK-Android](https://github.com/admin-ch/CovidCertificate-SDK-Android)
* iOS App: [CovidCertificate-App-iOS](https://github.com/admin-ch/CovidCertificate-App-iOS)
* iOS SDK: [CovidCertificate-SDK-iOS](https://github.com/admin-ch/CovidCertificate-SDK-iOS)
* Config Service: [CovidCertificate-App-Config-Service](https://github.com/admin-ch/CovidCertificate-App-Config-Service)
* Verifier Service: [CovidCertificate-App-Verifier-Service](https://github.com/admin-ch/CovidCertificate-App-Verifier-Service)
* Certificate Delivery: [CovidCertificate-App-Certificate-Delivery](https://github.com/admin-ch/CovidCertificate-App-Certificate-Delivery)
* Transformation Service: [CovidCertificate-App-Transformation-Service](https://github.com/admin-ch/CovidCertificate-App-Transformation-Service)
* Verification-Check Service: [CovidCertificate-App-Verification-Check-Service](https://github.com/admin-ch/CovidCertificate-App-Verification-Check-Service)

## License

This project is licensed under the terms of the MPL 2 license. See the [LICENSE](LICENSE) file for details.
