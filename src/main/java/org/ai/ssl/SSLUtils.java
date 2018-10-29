package org.ai.ssl;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

public class SSLUtils {

    private static TrustManager[] _trustManagers;


    public static SSLContext getAllTrustedSSLContext() {
        SSLContext context;

        // Create a trust manager that does not validate certificate chains
        if (_trustManagers == null) {
            _trustManagers = new TrustManager[]{new FakeX509TrustManager()};
        } // if
        // Install the all-trusting trust manager:
        try {
            context = SSLContext.getInstance("SSL");
            context.init(null, _trustManagers, new SecureRandom());
        } catch (GeneralSecurityException gse) {
            throw new IllegalStateException(gse.getMessage());
        } // catch
        return context;
    }


    public static class FakeX509TrustManager implements X509TrustManager {

        /**
         * Empty array of certificate authority certificates.
         */
        private static final X509Certificate[] _AcceptedIssuers = new X509Certificate[]{};

        /**
         * Always trust for client SSL chain peer certificate chain with any
         * authType authentication types.
         *
         * @param chain    the peer certificate chain.
         * @param authType the authentication type based on the client
         *                 certificate.
         */
        public void checkClientTrusted(X509Certificate[] chain, String authType) {
        }

        /**
         * Always trust for server SSL chain peer certificate chain with any
         * authType exchange algorithm types.
         *
         * @param chain    the peer certificate chain.
         * @param authType the key exchange algorithm used.
         */
        public void checkServerTrusted(X509Certificate[] chain, String authType) {
        }

        /**
         * Return an empty array of certificate authority certificates which are
         * trusted for authenticating peers.
         *
         * @return a empty array of issuer certificates.
         */
        public X509Certificate[] getAcceptedIssuers() {
            return (_AcceptedIssuers);
        }
    }

}
