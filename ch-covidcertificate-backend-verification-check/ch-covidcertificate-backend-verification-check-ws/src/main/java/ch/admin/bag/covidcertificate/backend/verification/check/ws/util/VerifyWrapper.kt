package ch.admin.bag.covidcertificate.backend.verification.check.ws.util

import ch.admin.bag.covidcertificate.sdk.core.models.healthcert.DccHolder
import ch.admin.bag.covidcertificate.sdk.core.models.state.VerificationState
import ch.admin.bag.covidcertificate.sdk.core.models.trustlist.TrustList
import ch.admin.bag.covidcertificate.sdk.core.verifier.CertificateVerifier

import kotlinx.coroutines.runBlocking;

object VerifyWrapper {

    @JvmStatic
    fun verify(
        certificateVerifier: CertificateVerifier,
        dccHolder: DccHolder,
        trustList: TrustList
    ): VerificationState = runBlocking {
        certificateVerifier.verify(dccHolder, trustList);
    }
}