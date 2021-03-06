package piuk.blockchain.android.ui.fingerprint;

import android.os.Bundle;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import piuk.blockchain.android.R;
import piuk.blockchain.androidcore.utils.PrefsUtil;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static piuk.blockchain.android.ui.fingerprint.FingerprintDialog.KEY_BUNDLE_PIN_CODE;
import static piuk.blockchain.android.ui.fingerprint.FingerprintDialog.KEY_BUNDLE_STAGE;

public class FingerprintPresenterTest {

    private FingerprintPresenter subject;
    @Mock private FingerprintHelper fingerprintHelper;
    @Mock private FingerprintView activity;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        subject = new FingerprintPresenter(fingerprintHelper);
        subject.initView(activity);
    }

    @Test
    public void onViewReadyKeysNull() throws Exception {
        // Arrange
        when(activity.getBundle()).thenReturn(mock(Bundle.class));
        // Act
        subject.onViewReady();
        // Assert
        verify(activity).onCanceled();
    }

    @Test
    public void onViewReadyRegister() throws Exception {
        // Arrange
        Bundle bundle = mock(Bundle.class);
        when(bundle.getString(KEY_BUNDLE_STAGE)).thenReturn(FingerprintStage.REGISTER_FINGERPRINT.name());
        String pincode = "1234";
        when(bundle.getString(KEY_BUNDLE_PIN_CODE)).thenReturn(pincode);
        when(activity.getBundle()).thenReturn(bundle);
        // Act
        subject.onViewReady();
        // Assert
        verify(activity).setCancelButtonText(anyInt());
        verify(activity).setDescriptionText(anyInt());
        verify(fingerprintHelper).encryptString(eq(PrefsUtil.KEY_ENCRYPTED_PIN_CODE), eq(pincode), any(FingerprintHelper.AuthCallback.class));
    }

    @Test
    public void onViewReadyAuthenticate() throws Exception {
        // Arrange
        Bundle bundle = mock(Bundle.class);
        when(bundle.getString(KEY_BUNDLE_STAGE)).thenReturn(FingerprintStage.AUTHENTICATE.name());
        String pincode = "1234";
        when(bundle.getString(KEY_BUNDLE_PIN_CODE)).thenReturn(pincode);
        bundle.putString(KEY_BUNDLE_PIN_CODE, pincode);
        when(activity.getBundle()).thenReturn(bundle);
        // Act
        subject.onViewReady();
        // Assert
        verify(activity).setCancelButtonText(anyInt());
        verify(fingerprintHelper).decryptString(eq(PrefsUtil.KEY_ENCRYPTED_PIN_CODE), eq(pincode), any(FingerprintHelper.AuthCallback.class));
    }

    @Test
    public void onFailure() throws Exception {
        Bundle bundle = mock(Bundle.class);
        when(bundle.getString(KEY_BUNDLE_STAGE)).thenReturn(FingerprintStage.REGISTER_FINGERPRINT.name());
        String pincode = "1234";
        when(bundle.getString(KEY_BUNDLE_PIN_CODE)).thenReturn(pincode);
        when(activity.getBundle()).thenReturn(bundle);
        doAnswer(invocation -> {
            ((FingerprintHelper.AuthCallback) invocation.getArguments()[2]).onFailure();
            return null;
        }).when(fingerprintHelper).encryptString(
                anyString(), anyString(), any(FingerprintHelper.AuthCallback.class));
        // Act
        subject.onViewReady();
        // Assert
        verify(activity).setIcon(anyInt());
        verify(activity).setStatusText(anyInt());
        verify(activity).setStatusTextColor(anyInt());
        verify(activity).onRecoverableError();
    }

    @Test
    public void onHelp() throws Exception {
        Bundle bundle = mock(Bundle.class);
        when(bundle.getString(KEY_BUNDLE_STAGE)).thenReturn(FingerprintStage.REGISTER_FINGERPRINT.name());
        String pincode = "1234";
        when(bundle.getString(KEY_BUNDLE_PIN_CODE)).thenReturn(pincode);
        String message = "help";
        when(activity.getBundle()).thenReturn(bundle);
        doAnswer(invocation -> {
            ((FingerprintHelper.AuthCallback) invocation.getArguments()[2]).onHelp(message);
            return null;
        }).when(fingerprintHelper).encryptString(
                anyString(), anyString(), any(FingerprintHelper.AuthCallback.class));
        // Act
        subject.onViewReady();
        // Assert
        verify(activity).setIcon(anyInt());
        verify(activity).setStatusText(message);
        verify(activity).setStatusTextColor(anyInt());
        verify(activity).onRecoverableError();
    }

    @Test
    public void onAuthenticated() throws Exception {
        Bundle bundle = mock(Bundle.class);
        when(bundle.getString(KEY_BUNDLE_STAGE)).thenReturn(FingerprintStage.REGISTER_FINGERPRINT.name());
        String pincode = "1234";
        when(bundle.getString(KEY_BUNDLE_PIN_CODE)).thenReturn(pincode);
        String data = "";
        when(activity.getBundle()).thenReturn(bundle);
        doAnswer(invocation -> {
            ((FingerprintHelper.AuthCallback) invocation.getArguments()[2]).onAuthenticated(data);
            return null;
        }).when(fingerprintHelper).encryptString(
                anyString(), anyString(), any(FingerprintHelper.AuthCallback.class));
        // Act
        subject.onViewReady();
        // Assert
        verify(activity).setIcon(anyInt());
        verify(activity).setStatusText(anyInt());
        verify(activity).setStatusTextColor(anyInt());
        verify(activity).onAuthenticated(data);
        verify(fingerprintHelper).storeEncryptedData(PrefsUtil.KEY_ENCRYPTED_PIN_CODE, data);
    }

    @Test
    public void onKeyInvalidated() throws Exception {
        Bundle bundle = mock(Bundle.class);
        when(bundle.getString(KEY_BUNDLE_STAGE)).thenReturn(FingerprintStage.REGISTER_FINGERPRINT.name());
        String pincode = "1234";
        when(bundle.getString(KEY_BUNDLE_PIN_CODE)).thenReturn(pincode);
        when(activity.getBundle()).thenReturn(bundle);
        doAnswer(invocation -> {
            ((FingerprintHelper.AuthCallback) invocation.getArguments()[2]).onKeyInvalidated();
            return null;
        }).when(fingerprintHelper).encryptString(
                anyString(), anyString(), any(FingerprintHelper.AuthCallback.class));
        // Act
        subject.onViewReady();
        // Assert
        verify(activity).setIcon(anyInt());
        verify(activity, times(2)).setCancelButtonText(anyInt());
        verify(activity).setStatusText(anyInt());
        verify(activity).setStatusTextColor(anyInt());
        verify(activity).onFatalError();
        verify(fingerprintHelper).clearEncryptedData(PrefsUtil.KEY_ENCRYPTED_PIN_CODE);
        verify(fingerprintHelper).setFingerprintUnlockEnabled(false);
    }

    @Test
    public void onFatalErrorWhilstRegistering() throws Exception {
        Bundle bundle = mock(Bundle.class);
        when(bundle.getString(KEY_BUNDLE_STAGE)).thenReturn(FingerprintStage.REGISTER_FINGERPRINT.name());
        String pincode = "1234";
        when(bundle.getString(KEY_BUNDLE_PIN_CODE)).thenReturn(pincode);
        when(activity.getBundle()).thenReturn(bundle);
        doAnswer(invocation -> {
            ((FingerprintHelper.AuthCallback) invocation.getArguments()[2]).onFatalError();
            return null;
        }).when(fingerprintHelper).encryptString(
                anyString(), anyString(), any(FingerprintHelper.AuthCallback.class));
        // Act
        subject.onViewReady();
        // Assert
        verify(activity).setIcon(anyInt());
        verify(activity).setStatusText(anyInt());
        verify(activity).setDescriptionText(R.string.fingerprint_fatal_error_register_description);
        verify(activity).setStatusTextColor(anyInt());
        verify(activity).onFatalError();
        verify(fingerprintHelper).clearEncryptedData(PrefsUtil.KEY_ENCRYPTED_PIN_CODE);
        verify(fingerprintHelper).setFingerprintUnlockEnabled(false);
    }

    @Test
    public void onFatalErrorWhilstAuthenticating() throws Exception {
        Bundle bundle = mock(Bundle.class);
        when(bundle.getString(KEY_BUNDLE_STAGE)).thenReturn(FingerprintStage.AUTHENTICATE.name());
        String pincode = "1234";
        when(bundle.getString(KEY_BUNDLE_PIN_CODE)).thenReturn(pincode);
        when(activity.getBundle()).thenReturn(bundle);
        doAnswer(invocation -> {
            ((FingerprintHelper.AuthCallback) invocation.getArguments()[2]).onFatalError();
            return null;
        }).when(fingerprintHelper).decryptString(
                anyString(), anyString(), any(FingerprintHelper.AuthCallback.class));
        // Act
        subject.onViewReady();
        // Assert
        verify(activity).setIcon(anyInt());
        verify(activity).setStatusText(anyInt());
        verify(activity).setDescriptionText(R.string.fingerprint_fatal_error_authenticate_description);
        verify(activity).setStatusTextColor(anyInt());
        verify(activity).onFatalError();
        verify(fingerprintHelper).clearEncryptedData(PrefsUtil.KEY_ENCRYPTED_PIN_CODE);
        verify(fingerprintHelper).setFingerprintUnlockEnabled(false);
    }

    @Test
    public void destroy() throws Exception {
        // Arrange

        // Act
        subject.onViewDestroyed();
        // Assert
        verify(fingerprintHelper).releaseFingerprintReader();
    }

}