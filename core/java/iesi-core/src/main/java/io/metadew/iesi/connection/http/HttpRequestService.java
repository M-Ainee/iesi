package io.metadew.iesi.connection.http;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

public class HttpRequestService {

    public HttpResponse send(HttpRequest httpRequest) throws IOException, KeyManagementException, NoSuchAlgorithmException {
        CloseableHttpClient httpClient = noSSLCertificateVerification();
        HttpResponse httpResponse = new HttpResponse(httpClient.execute(httpRequest.getHttpRequest()));
        httpClient.close();
        return httpResponse;
    }

    private static CloseableHttpClient noSSLCertificateVerification() throws NoSuchAlgorithmException, KeyManagementException {
        TrustManager[] trustAllCerts = new TrustManager[]{new X509ExtendedTrustManager() {
            @Override
            public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
            }

            @Override
            public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
            }

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkClientTrusted(java.security.cert.X509Certificate[] arg0, String arg1, Socket arg2) {
            }

            @Override
            public void checkClientTrusted(java.security.cert.X509Certificate[] arg0, String arg1, SSLEngine arg2) {
            }

            @Override
            public void checkServerTrusted(java.security.cert.X509Certificate[] arg0, String arg1, Socket arg2) {
            }

            @Override
            public void checkServerTrusted(java.security.cert.X509Certificate[] arg0, String arg1, SSLEngine arg2) {
            }
        }};

        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

        // we can optionally disable hostname verification.
        // if you don't want to further weaken the security, you don't have to include this.
        HostnameVerifier allowAllHosts = new NoopHostnameVerifier();

        // create an SSL Socket Factory to use the SSLContext with the trust self signed certificate strategy
        // and allow all hosts verifier.
        SSLConnectionSocketFactory connectionFactory = new SSLConnectionSocketFactory(sslContext, allowAllHosts);
        // finally create the HttpClient using HttpClient factory methods and assign the ssl socket factory
        return HttpClients.custom().setSSLSocketFactory(connectionFactory).build();
    }
}
