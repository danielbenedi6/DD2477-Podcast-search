package com.example.application;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.termvectors.Term;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.example.application.views.searcher.Clip;
import com.example.application.views.searcher.ClipCard;
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
import java.util.Objects;
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

    public List<ClipCard> search(String query, float seconds){
        List<ClipCard> phrase_results = new ArrayList<>();
        List<ClipCard> intersect_results = new ArrayList<>();
        try {
            //Phrase Match
            phrase_results = phraseMatch(phrase_results, query);
            //Intersection Match 100% match
            intersect_results = IntersectionMatch(intersect_results, query, "100%");

            List<ClipCard> results = filterPhraseClip(query, phrase_results, seconds);
            results.addAll(filterIntersectClip(query, intersect_results, seconds));

            return results;

        } catch(Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private List<ClipCard> IntersectionMatch(List<ClipCard> raw_results, String query, String percentage){
        try {
            //Phrase Match
            SearchResponse<Podcast> intersectionResp = client.search(s -> s
                            .index("spotify-podcasts-test")
                            .query(q -> q.
                                    bool(b -> b.
                                            must(mt -> mt.
                                                    match(m -> m.
                                                            field("clips.transcript").query(query)
                                                    )

                                            )
                                            .minimumShouldMatch(percentage)
                                    )
                            )
                            .size(30),
                    Podcast.class);

            return parseToClipCard(raw_results, intersectionResp);

        }catch(Exception e) {
            e.printStackTrace();
            return raw_results;
        }
    }

    private List<ClipCard> phraseMatch(List<ClipCard> raw_results, String query){
        try {
            //Intersect Match
            SearchResponse<Podcast> phraseResp = client.search(s -> s
                            .index("spotify-podcasts-test")
                            .query(q -> q.matchPhrase(m -> m.field("clips.transcript").query(query))
                            )
                            .sort(so -> so
                                    .field(f -> f
                                            .field("pubDate")
                                            .order(SortOrder.Desc)))
                            .size(30),
                    Podcast.class);
            return parseToClipCard(raw_results, phraseResp);

        }catch(Exception e) {
            e.printStackTrace();
            return raw_results;
        }
    }

    private List<ClipCard> parseToClipCard(List<ClipCard> raw_results, SearchResponse<Podcast> searchResponse){
        if (searchResponse.hits().total() != null && searchResponse.hits().total().value() > 0) {
            for (Hit<Podcast> hit : searchResponse.hits().hits()) {
                if (hit.source() != null) {
                    List<Clip> clips = hit.source().getClips();
                    for (Clip clip : clips) {
                        if (!Objects.equals(clip.getTranscript(), "")) {
                            //calculate the duration
                            int startLen = clip.getWords().get(0).getStartTime().length();
                            float startTime = Float.parseFloat(clip.getWords().get(0).getStartTime().substring(0, startLen - 1));
                            int endLen = clip.getWords().get(clip.getWords().size() - 1).getEndTime().length();
                            float endTime = Float.parseFloat(clip.getWords().get(clip.getWords().size() - 1).getEndTime().substring(0, endLen - 1));
                            float duration = endTime - startTime;
                            ClipCard clipCard = new ClipCard(clip.getTranscript(), hit.source().getEpisode_name(), hit.source().getPubDate(), hit.source().getEpisode_uri(), hit.source().getPublisher(), duration, startTime, endTime);
                            raw_results.add(clipCard);
                        }
                    }
                }
            }
        }
        return raw_results;
    }
    private List<ClipCard> filterPhraseClip(String query, List<ClipCard> raw_results, float seconds) {
        List<ClipCard> results = new ArrayList<>();
        //only show the clips of transcript contain the query
        for (ClipCard card : raw_results){
            if (card.getTranscript().contains(query) && seconds > card.getDuration()){
                card.plusTimeStamp();
                results.add(card);
            }
        }
        return results;
    }

    private List<ClipCard> filterIntersectClip(String query, List<ClipCard> raw_results, float seconds) {
        List<ClipCard> results = new ArrayList<>();
        String[] terms = query.split(" ");
        //only show the clips of transcript contain the query
        for (ClipCard card : raw_results){
            for (String term : terms){
                if (!card.getTranscript().contains(term)){
                    break;
                }
                if (seconds > card.getDuration()){
                    card.plusTimeStamp();
                    results.add(card);
                }
            }
        }
        return results;
    }
}
