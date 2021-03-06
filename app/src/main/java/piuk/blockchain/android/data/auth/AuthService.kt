package piuk.blockchain.android.data.auth

import info.blockchain.wallet.api.WalletApi
import info.blockchain.wallet.api.data.Status
import info.blockchain.wallet.api.data.WalletOptions
import info.blockchain.wallet.exceptions.ApiException
import info.blockchain.wallet.exceptions.InvalidCredentialsException
import io.reactivex.Observable
import okhttp3.ResponseBody
import piuk.blockchain.android.data.logging.EventService
import piuk.blockchain.androidcore.utils.annotations.Mockable
import piuk.blockchain.androidcore.utils.annotations.WebRequest
import retrofit2.Response
import javax.inject.Inject

@Mockable
class AuthService @Inject constructor(private val walletApi: WalletApi) {

    /**
     * Returns a [WalletOptions] object, which amongst other things contains information
     * needed for determining buy/sell regions.
     */
    internal fun getWalletOptions(): Observable<WalletOptions> = walletApi.walletOptions

    /**
     * Get encrypted copy of Payload
     *
     * @param guid      A user's GUID
     * @param sessionId The session ID, retrieved from [.getSessionId]
     * @return [<] wrapping an encrypted Payload
     */
    @WebRequest
    internal fun getEncryptedPayload(
            guid: String,
            sessionId: String
    ): Observable<Response<ResponseBody>> = walletApi.fetchEncryptedPayload(guid, sessionId)

    /**
     * Posts a user's 2FA code to the server. Will return an encrypted copy of the Payload if
     * successful.
     *
     * @param sessionId     The current session ID
     * @param guid          The user's GUID
     * @param twoFactorCode The user's generated (or received) 2FA code
     * @return An [Observable] which may contain an encrypted Payload
     */
    @WebRequest
    internal fun submitTwoFactorCode(
            sessionId: String,
            guid: String,
            twoFactorCode: String
    ): Observable<ResponseBody> = walletApi.submitTwoFactorCode(sessionId, guid, twoFactorCode)

    /**
     * Gets a session ID from the server
     *
     * @param guid A user's GUID
     * @return An [Observable] wrapping a [String] response
     */
    @WebRequest
    internal fun getSessionId(guid: String): Observable<String> {
        return walletApi.getSessionId(guid)
                .map { responseBodyResponse ->
                    val headers = responseBodyResponse.headers().get("Set-Cookie")
                    if (headers != null) {
                        val fields = headers.split(";\\s*".toRegex()).dropLastWhile { it.isEmpty() }
                                .toTypedArray()
                        for (field in fields) {
                            if (field.startsWith("SID=")) {
                                return@map field.substring(4)
                            }
                        }
                    } else {
                        throw ApiException("Session ID not found in headers")
                    }
                    ""
                }
    }

    /**
     * Get the encryption password for pairing
     *
     * @param guid A user's GUID
     * @return An [Observable] wrapping the pairing encryption password
     */
    @WebRequest
    internal fun getPairingEncryptionPassword(guid: String): Observable<ResponseBody> =
            walletApi.fetchPairingEncryptionPassword(guid)

    /**
     * Sends the access key to the server
     *
     * @param key   The PIN identifier
     * @param value The value, randomly generated
     * @param pin   The user's PIN
     * @return An [Observable] where the boolean represents success
     */
    @WebRequest
    internal fun setAccessKey(
            key: String,
            value: String,
            pin: String
    ): Observable<Response<Status>> = walletApi.setAccess(key, value, pin)

    /**
     * Validates a user's PIN with the server
     *
     * @param key The PIN identifier
     * @param pin The user's PIN
     * @return A [<] which may or may not contain the field "success"
     */
    @WebRequest
    internal fun validateAccess(key: String, pin: String): Observable<Response<Status>> =
            walletApi.validateAccess(key, pin)
                    .doOnError {
                        if (it.message?.contains("Incorrect PIN") == true) {
                            throw InvalidCredentialsException("Incorrect PIN")
                        }
                    }

    /**
     * Logs an event to the backend for analytics purposes to work out which features are used most
     * often.
     *
     * @param event An event as a String
     * @return An [Observable] wrapping a [Status] object
     * @see EventService
     */
    @WebRequest
    fun logEvent(event: String): Observable<Status> = walletApi.logEvent(event)

}
