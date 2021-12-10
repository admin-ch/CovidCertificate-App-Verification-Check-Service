package ch.admin.bag.covidcertificate.backend.verification.check.ws.verification

import ch.admin.bag.covidcertificate.sdk.core.models.healthcert.CertificateHolder
import ch.admin.bag.covidcertificate.sdk.core.models.state.VerificationState
import ch.admin.bag.covidcertificate.sdk.core.models.trustlist.TrustList
import ch.admin.bag.covidcertificate.sdk.core.verifier.CertificateVerifier
import ch.admin.bag.covidcertificate.sdk.core.verifier.VerificationType
import kotlinx.coroutines.runBlocking

object VerifyWrapper {

    @JvmStatic
    fun verifyWallet(
            certificateVerifier: CertificateVerifier,
            certificateHolder: CertificateHolder,
            trustList: TrustList
    ): VerificationState = runBlocking {
        certificateVerifier.verify(certificateHolder, trustList, trustList.ruleSet.modeRules.activeModes.map { it -> it.id }.toSet(), VerificationType.WALLET)
    }

    @JvmStatic
    fun verifyVerifier(
        certificateVerifier: CertificateVerifier,
        certificateHolder: CertificateHolder,
        trustList: TrustList,
        mode: String
    ): VerificationState = runBlocking {
        certificateVerifier.verify(certificateHolder, trustList, setOf(mode), VerificationType.VERIFIER)
    }

}