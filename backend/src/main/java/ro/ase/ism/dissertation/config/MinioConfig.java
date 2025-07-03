package ro.ase.ism.dissertation.config;

import io.minio.MinioClient;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.Arrays;

@Configuration
public class MinioConfig {
    @Value("${minio.url}")
    private String url;

    @Value("${minio.accessKey}")
    private String accessKey;

    @Value("${minio.secretKey}")
    private String secretKey;

    @Value("${minio.trust-store}")
    private Resource truststore;

    @Value("${minio.trust-store-password}")
    private String truststorePassword;


    @Bean
    public MinioClient minioClient() throws Exception {
        // 1) load  JKS
        KeyStore ks = KeyStore.getInstance("JKS");
        try (InputStream in = truststore.getInputStream()) {
            ks.load(in, truststorePassword.toCharArray());
        }

        // 2) init a TrustManagerFactory with it
        TrustManagerFactory tmf = TrustManagerFactory
                .getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ks);

        // 3) build a SSLContext that uses those trust managers
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, tmf.getTrustManagers(), null);

        // 4) plug that SSLContext into an OkHttpClient
        X509TrustManager trustManager =
                Arrays.stream(tmf.getTrustManagers())
                        .filter(tm -> tm instanceof X509TrustManager)
                        .map(tm -> (X509TrustManager)tm)
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("No X509TrustManager"));

        OkHttpClient httpClient = new OkHttpClient.Builder()
                .sslSocketFactory(sslContext.getSocketFactory(), trustManager)
                .build();

        // 5) hand the OkHttpClient to MinioClient
        return MinioClient.builder()
                .endpoint(url)
                .credentials(accessKey, secretKey)
                .httpClient(httpClient)
                .build();
}
}
