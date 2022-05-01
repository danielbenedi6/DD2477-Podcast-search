package com.example.application;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.termvectors.Term;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.example.application.views.searcher.Clip;
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
    private static String ELASTIC_PASSWORD = "2HDh8FRFBlcQ6oe4IY*G";
    private static Path caCertificatePath = Paths.get("C:\\Users\\pppp\\Desktop\\DD2477-Podcast-search\\DD2477-Podcast-search\\podcast-searcher\\http_ca.crt");

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

    public List<Podcast> search(String query, float minutes){
        try {
            List<FieldValue> fields = Stream.of(query.split(" ")).map(FieldValue::of).collect(java.util.stream.Collectors.toList());

            Query transcript = new Query.Builder().terms(t -> t.field("transcript").terms(ft->ft.value(fields))).build();

            SearchResponse<Podcast> search = client.search(s -> s
                        .index("spotify-podcasts-test")
                            .query(q -> q
                                .bool(b -> b
                                    .must(transcript)
                                )
                            )
                            .size(1027),
                    Podcast.class);

            List<Podcast> results = new ArrayList<>();
            for(Hit<Podcast> hit : search.hits().hits()){
                if (hit.source() != null){
                    System.out.println("episode name " + hit.source().getEpisode_name());
                    System.out.println("show name " + hit.source().getShow_name());
                    System.out.println("episode uri " + hit.source().getEpisode_uri());
                    System.out.println("pubDate " + hit.source().getPubDate());
                    System.out.println("enclosure" + hit.source().getEnclosure());
                    //System.out.println("test" + hit.source().getClips().get(0));
                    System.out.println();
                    results.add(hit.source());
                }
            }
            System.out.println(results.size() + " results found");
            return results;
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
