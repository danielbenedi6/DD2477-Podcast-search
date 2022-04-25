package com.example.application;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.example.application.views.searcher.Podcast;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
public class ElasticService {
    private final ElasticsearchClient client;

    private static String ELASTIC_URL = "localhost";
    private static int ELASTIC_PORT = 9200;
    private static String ELASTIC_USERNAME = "elastic";
    private static String ELASTIC_PASSWORD = "e256a8b0c6688382";
    private static Path caCertificatePath = Paths.get("../es01.crt");

    public ElasticService() throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        Certificate trustedCa;
        try (InputStream is = Files.newInputStream(caCertificatePath)) {
            trustedCa = factory.generateCertificate(is);
        }
        KeyStore trustStore = KeyStore.getInstance("pkcs12");
        trustStore.load(null, null);
        trustStore.setCertificateEntry("ca", trustedCa);
        final SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(trustStore, null).build();

        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(ELASTIC_USERNAME, ELASTIC_PASSWORD));

        RestClient restClient = RestClient.builder(new HttpHost(ELASTIC_URL, ELASTIC_PORT, "https"))
                .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder
                        .setDefaultCredentialsProvider(credentialsProvider).setSSLContext(sslContext)).build();

        ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());

        client = new ElasticsearchClient(transport);
    }

    public List<Podcast> search(String query, float seconds){
        try {
            List<FieldValue> fields = Stream.of(query.split(" ")).map(FieldValue::of).collect(java.util.stream.Collectors.toList());

            Query episode_description = new Query.Builder().terms(t -> t.field("episode_description").terms(ft->ft.value(fields))).build();
            Query show_description = new Query.Builder().terms(t -> t.field("show_description").terms(ft->ft.value(fields))).build();
            Query transcript = new Query.Builder().terms(t -> t.field("transcript").terms(ft->ft.value(fields))).build();

            SearchResponse<Podcast> search = client.search(s -> s.index("spotify-podcasts")
                    .query(q -> q.bool(b -> b.should(episode_description, show_description, transcript))), Podcast.class);

            List<Podcast> results = new ArrayList<>();
            for(Hit<Podcast> hit : search.hits().hits()){
                results.add(hit.source());
                //double tf_idf = hit.score();
            }
            return results;
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
