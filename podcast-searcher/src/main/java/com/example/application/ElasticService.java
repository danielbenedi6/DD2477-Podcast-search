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
        List<ClipCard> raw_results = new ArrayList<>();
        try {
            //List<FieldValue> fields = Stream.of(query.split(" ")).map(FieldValue::of).collect(java.util.stream.Collectors.toList());
            //Query transcript = new Query.Builder().terms(t -> t.field("transcript").terms(ft->ft.value(fields))).build();
            //it seems we should match a phrase
            SearchResponse<Podcast> search = client.search(s -> s
                        .index("spotify-podcasts-test")
                            .query(q -> q.matchPhrase(m -> m.field("clips.transcript").query(query))
                            )
                            .sort(so->so
                                    .field(f->f
                                            .field("pubDate")
                                            .order(SortOrder.Desc)))
                            .size(1027),
                    Podcast.class);

            if (search.hits().total() != null && search.hits().total().value() > 0){
                for(Hit<Podcast> hit : search.hits().hits()){
                    if (hit.source() != null){
                        System.out.println("episode name " + hit.source().getEpisode_name());
                        System.out.println("show name " + hit.source().getShow_name());
                        System.out.println("episode uri " + hit.source().getEpisode_uri());
                        System.out.println("pubDate " + hit.source().getPubDate());
                        System.out.println("enclosure" + hit.source().getEnclosure());
                        List<Clip> clips = hit.source().getClips();
                        for (Clip clip : clips){
                            if (!Objects.equals(clip.getTranscript(), "")){
                                //System.out.println("Test word: " + clip.getWords().get(0).getWord());
                                System.out.println("Clip start time: " + clip.getWords().get(0).getStartTime());
                                System.out.println("Clip end time: " + clip.getWords().get(clip.getWords().size()-1).getEndTime());
                                //calculate the duration
                                int startLen = clip.getWords().get(0).getStartTime().length();
                                float startTime = Float.parseFloat(clip.getWords().get(0).getStartTime().substring(0,startLen - 1));
                                int endLen = clip.getWords().get(clip.getWords().size()-1).getEndTime().length();
                                float endTime = Float.parseFloat(clip.getWords().get(clip.getWords().size()-1).getEndTime().substring(0, endLen - 1));
                                float duration =  endTime - startTime;
                                System.out.println("The duration of clip: " + duration);
                                ClipCard clipCard = new ClipCard(clip.getTranscript(), hit.source().getEpisode_name(), hit.source().getPubDate(), hit.source().getEnclosure(), hit.source().getPublisher(), duration, startTime, endTime);
                                System.out.println("Test transcript clipCard: " + clipCard.getTranscript());
                                System.out.println();

                                raw_results.add(clipCard);
                            }
                        }
                    }
                }
            }
            System.out.println(raw_results.size() + " raw results found");
            return filtrateClip(query, raw_results, seconds);
        } catch(Exception e) {
            e.printStackTrace();
            return raw_results;
        }
    }

    private List<ClipCard> filtrateClip(String query, List<ClipCard> raw_results, float seconds) {
        List<ClipCard> results = new ArrayList<>();
        //only show the clips of transcript contain the query
        for (ClipCard card : raw_results){
            if (card.getTranscript().contains(query) && seconds > card.getDuration()){
                String durationStr = "\n\n---Duration: " + card.getDuration() + "s\n";
                String startTime = "---Start Timestamp: " + convertSeconds(card.getStartTime()) + "\n";
                String endTime = "---End Timestamp: " + convertSeconds(card.getEndTime()) + "\n";
                card.setTranscript(card.getTranscript() + durationStr + startTime + endTime);
                results.add(card);
            }
        }
        return results;
    }

    private String convertSeconds(float seconds){
        int temp = (int) seconds;
        int hh = temp / 3600;
        int mm = (temp % 3600) / 60;
        int ss = (temp % 3600) % 60;
        return (hh < 10 ? ("0" + hh) : hh) + ":" +
                (mm < 10 ? ("0" + mm) : mm) + ":" +
                 (ss < 10 ? ("0" + ss) : ss);
    }

}
