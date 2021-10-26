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

The service is now exposed on port 8080 (change the first number in the `-p` argument to change the port).

### Verifying certificates

Certificates can be verified by sending POST requests to `/v1/verify`, where the payload should be JSON with the following format:
```json
{
  "hcert": "The raw payload of the certificate QR code beginning with 'HC1:'"
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
where only one of the three fields `successState`, `errorState`, `invalidState` is set to a non-null value, thereby indicating the corresponding verification-status.

<details>
  <summary>Examples</summary>
  
Request payload:
  ```json
  {"hcert": "HC1:NCFJ60EG0/3WUWGSLKH47GO0KNJ9DSWQIIWT9CK+500XKY-CE59-G80:84F3ZKG%QU2F30GK JEY50.FK6ZK7:EDOLOPCF8F746KG7+59.Q6+A80:6JM8SX8RM8.A8TL6IA7-Q6.Q6JM8WJCT3EYM8XJC +DXJCCWENF6OF63W5$Q69L6%JC+QE$.32%E6VCHQEU$DE44NXOBJE719$QE0/D+8D-ED.24-G8$:8.JCBECB1A-:8$96646AL60A60S6Q$D.UDRYA 96NF6L/5QW6307KQEPD09WEQDD+Q6TW6FA7C466KCN9E%961A6DL6FA7D46JPCT3E5JDJA76L68463W5/A6..DX%DZJC3/DH$9- NTVDWKEI3DK2D4XOXVD1/DLPCG/DU2D4ZA2T9GY8MPCG/DY-CAY81C9XY8O/EZKEZ96446256V50G7AZQ4CUBCD9-FV-.6+OJROVHIBEI3KMU/TLRYPM0FA9DCTID.GQ$NYE3NPBP90/9IQH24YL7WMO0CNV1 SDB1AHX7:O26872.NV/LC+VJ75L%NGF7PT134ERGJ.I0 /49BB6JA7WKY:AL19PB120CUQ37XL1P9505-YEFJHVETB3CB-KE8EN9BPQIMPRTEW*DU+X2STCJ6O6S4XXVJ$UQNJW6IIO0X20D4S3AWSTHTA5FF7I/J9:8ALF/VP 4K1+8QGI:N0H 91QBHPJLSMNSJC BFZC5YSD.9-9E5R8-.IXUB-OG1RRQR7JEH/5T852EA3T7P6 VPFADBFUN0ZD93MQY07/4OH1FKHL9P95LIG841 BM7EXDR/PLCUUE88+-IX:Q"}
  ```  

  Response for valid certificate:
  ```json
{
   "certificate":{
      "version":"1.0.0",
      "person":{
         "familyName":"vaccine",
         "standardizedFamilyName":"VACCINE",
         "givenName":"valid from today",
         "standardizedGivenName":"VALID<FROM<TODAY"
      },
      "dateOfBirth":"15.01.1970",
      "personName":{
         "familyName":"vaccine",
         "standardizedFamilyName":"VACCINE",
         "givenName":"valid from today",
         "standardizedGivenName":"VALID<FROM<TODAY"
      },
      "formattedDateOfBirth":"15.01.1970"
   },
   "successState":{
      "isLightCertificate":false,
      "validityRange":{
         "validFrom":[
            2021,
            10,
            13,
            0,
            0
         ],
         "validUntil":[
            2022,
            10,
            12,
            0,
            0
         ]
      }
   },
   "errorState":null,
   "invalidState":null
}
  ```
  
  Response for invalid certificate (e.g. expired)
  ```json
{
   "certificate":{
      "version":"1.0.0",
      "person":{
         "familyName":"vaccine",
         "standardizedFamilyName":"VACCINE",
         "givenName":"valid until today",
         "standardizedGivenName":"VALID<UNTIL<TODAY"
      },
      "dateOfBirth":"15.01.1970",
      "personName":{
         "familyName":"vaccine",
         "standardizedFamilyName":"VACCINE",
         "givenName":"valid until today",
         "standardizedGivenName":"VALID<UNTIL<TODAY"
      },
      "formattedDateOfBirth":"15.01.1970"
   },
   "successState":null,
   "errorState":null,
   "invalidState":{
      "signatureState":{
         
      },
      "revocationState":{
         
      },
      "nationalRulesState":{
         "validityRange":{
            "validFrom":[
               2020,
               10,
               13,
               0,
               0
            ],
            "validUntil":[
               2021,
               10,
               12,
               0,
               0
            ]
         },
         "ruleId":"VR-CH-0006"
      },
      "validityRange":{
         "validFrom":[
            2020,
            10,
            13,
            0,
            0
         ],
         "validUntil":[
            2021,
            10,
            12,
            0,
            0
         ]
      }
   }
}
  ```
  
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
