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

## Local Deployment

In order to allow the independent usage of the Verification-Check-Service without deeper knowledge of the source code, Docker container images are provided on [DockerHub](https://hub.docker.com/r/adminch/covidcertificate-app-verification-check-service). In order for the service to connect to the Verifier-Service, an API token is required. If you intend to deploy your own instance of the Verification-Check-Service, please get in touch with the [BAG](mailto:Covid-Zertifikat@bag.admin.ch) to get a token assigned.


### Starting the service

Download and start the container by running
```bash
docker run -p 8080:8080 -e APIKEY=<API obtained from BAG> adminch/covidcertificate-app-verification-check-service:latest
```

The service is now exposed on port 8080 (change the first number in the `-p` argument to change the port). By default, the service will connect to the `prod` environment, which should be used to verify actual certificates. For testing and development, add `-e ENV=dev` or `-e ENV=abn` to change to the respective environment.

### Verifying certificates

Certificates can be verified by sending POST requests to `/v1/verify`, where the payload should be JSON with the following format:
```json
{ 
  "mode": "The verification mode, e.g. THREE_G",
  "hcert": "The raw payload of the certificate QR code beginning with 'HC1:'"
}
```
The `mode` field must be one of the currently valid verification modes. A list of modes can be obtained with a `GET` request to the `v1/modes` endpoint:

```json
[
  "THREE_G",
  "TWO_G",
  "TWO_G_PLUS",
  "TEST_CERT"
]
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
where only one of the three fields `successState`, `errorState`, `invalidState` is set to a non-null value, thereby indicating the corresponding verification-status. Since the introduction of different verification modes, the presence of the `successState` object is no longer sufficient to indicate the validity of a certificate. You must also check the value of the `successState.successState.modeValidity.modeValidityState` field. It can currently take one of the following values:

| Value        | Meaning                                            |
|--------------|----------------------------------------------------|
| SUCCESS      | The certificate fulfills all criteria of this mode |
| SUCCESS_2G   | The certificate fulfills the 2G part of 2G+ (i.e. vaccine/recovery) |
| SUCCESS_2G_PLUS | The certificate fulfills the plus part of 2G+ (i.e. test) |
| IS_LIGHT     | The certificate is a light certificate which is not valid in this mode |
| INVALID      | The certificate is not valid under this mode       |
| UNKNOWN_MODE | An invalid verification mode was specified         |

Note that in modes requiring multiple certificates (currently 2G+), each certificate needs to be verified separately and it is the client's responsibility to keep track of which certificates have been verified.


<details>
  <summary>Examples</summary>
  
Request payload:
  ```json
  {"mode": "TWO_G", "hcert": "HC1:NCF260VG0/3WUWGSLKH47GO0.TSZV5ZBHU3M8CK8PV*70YM8FN0E$C$T1WY0-FCA0LD97TK0F90IECRHGWJC0FDL:4:KEPH7M/ESDD746KG7+592X61:6WA7Q46PF6XW6M*83:64R6XX8T%61A6WJCT3EYM8%JC+QE$.32%E6VCHQEU$DE44NXOBJE719$QE0/D+8D-ED.24-G8$:83KCZPCNF6OF64W5KF6-96/SA5R6%961G73564KCJPCITA2OA4KCD3DX47B46IL6646H*6KWEKDDC%6-Q6QW66464KCAWE6T9G%6G%67W5JPCT3E6JD646+/6C464W51S6..DX%DZJC1/DI-AXVD3VCI3DYUC6$C5WEW.C7WEV+A:S92T8I3D6WEITA2OA$PC5$CUZC$$5Y$5FBBY10D-ABIHJSEJVDKIDPRT/7D4BN/+40SDH4EIBR2:4$0G4MCT0N6BSWBVN.9AYA.UA4ANCEOUJEKOR04R36QT7A2I20KE JK5N47THHSU..7U05V/6UAKGZ3DPO.KB8-P2DM0RB:PC$65LHU3638$N WI*AP OPCGO:MAF4G*39D8T5C4BR3UMNB1R*Q8I5G35HCPC*V9OZJ0/K:OKTC1N E:JO17HKWQOR81JP0BCBL8%5IA6J8VJMM0L$60$BLNACPAN4NOKQ6XPC8Q2F9B-B$YGHDAFGKY$MS/I1YLYTVC$OHJAX LLKDLHGYAB-SON0A807AMH/CUPQ99TUXEAJ5486FQK1B%A2VM0C569O9AK-DH6MPU:UVPTCZLA:Q-+0EK8G4"}
  ```  

  Response for valid certificate:
  ```json
{
  "certificate": {
    "version": "1.0.0",
    "person": {
      "familyName": "Valid",
      "standardizedFamilyName": "VALID",
      "givenName": "Vaccine",
      "standardizedGivenName": "VACCINE"
    },
    "dateOfBirth": "05.02.2003",
    "personName": {
      "familyName": "Valid",
      "standardizedFamilyName": "VALID",
      "givenName": "Vaccine",
      "standardizedGivenName": "VACCINE"
    },
    "formattedDateOfBirth": "05.02.2003"
  },
  "successState": {
    "successState": {
      "modeValidity": {
        "mode": "THREE_G",
        "modeValidityState": "SUCCESS"
      }
    },
    "isLightCertificate": false
  },
  "errorState": null,
  "invalidState": null
}
  ```
  
  Response for invalid certificate (e.g. expired)
  ```json
{
  "certificate": {
    "version": "1.0.0",
    "person": {
      "familyName": "Expired",
      "standardizedFamilyName": "EXPIRED",
      "givenName": "Vaccine",
      "standardizedGivenName": "VACCINE"
    },
    "dateOfBirth": "03.02.1999",
    "personName": {
      "familyName": "Expired",
      "standardizedFamilyName": "EXPIRED",
      "givenName": "Vaccine",
      "standardizedGivenName": "VACCINE"
    },
    "formattedDateOfBirth": "03.02.1999"
  },
  "successState": null,
  "errorState": null,
  "invalidState": {
    "signatureState": {},
    "revocationState": {},
    "nationalRulesState": {
      "validityRange": {
        "validFrom": [
          2020,
          12,
          13,
          0,
          0
        ],
        "validUntil": [
          2021,
          12,
          12,
          0,
          0
        ]
      },
      "ruleId": "VR-CH-0006"
    },
    "validityRange": {
      "validFrom": [
        2020,
        12,
        13,
        0,
        0
      ],
      "validUntil": [
        2021,
        12,
        12,
        0,
        0
      ]
    }
  }
}
  ```
  
Response for a certificate that is basically valid (i.e. recognized in Switzerland and within its validity period), but not sufficient for the current verification mode, e.g. a test certificate in 2G (vaccine/recovery only) mode:
  
  ```json
  {
  "certificate": {
    "version": "1.0.0",
    "person": {
      "familyName": "Valid",
      "standardizedFamilyName": "VALID",
      "givenName": "Test",
      "standardizedGivenName": "TEST"
    },
    "dateOfBirth": "14.06.2007",
    "personName": {
      "familyName": "Valid",
      "standardizedFamilyName": "VALID",
      "givenName": "Test",
      "standardizedGivenName": "TEST"
    },
    "formattedDateOfBirth": "14.06.2007"
  },
  "successState": {
    "successState": {
      "modeValidity": {
        "mode": "TWO_G",
        "modeValidityState": "INVALID"
      }
    },
    "isLightCertificate": false
  },
  "errorState": null,
  "invalidState": null
}
```
  Pay special attention to the `successState` object. It is present, indicating that the certificate itself is valid, but the `modeValidityState` indicates that it is invalid under the selected mode.
  
</details>

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
