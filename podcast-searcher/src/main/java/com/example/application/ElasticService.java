package com.example.application;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.Suggestion;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.example.application.views.searcher.Clip;
import com.example.application.views.searcher.Fragment;
import com.example.application.views.searcher.Podcast;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.ssl.SSLContexts;
import org.elasticsearch.client.RestClient;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.InputStream;
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
import java.util.*;

@Service
public class ElasticService {
    private final ElasticsearchClient client;

    private final static String ELASTIC_URL = "localhost";
    private final static int ELASTIC_PORT = 9200;
    private final static String ELASTIC_USERNAME = "elastic";
    private final static String ELASTIC_PASSWORD = "2HDh8FRFBlcQ6oe4IY*G";
    private final static Path caCertificatePath = Paths.get("C:\\Users\\pppp\\Desktop\\DD2477-Podcast-search\\DD2477-Podcast-search\\podcast-searcher\\http_ca.crt");

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

    public static class Result {
        public List<Podcast> podcasts;
        public String suggestion;

        public Long time;

        public Result(List<Podcast> podcasts, String suggestion, Long time) {
            this.podcasts = podcasts;
            this.suggestion = suggestion;
            this.time = time;
        }

        public Result() {
            this.podcasts = new ArrayList<>();
            this.suggestion = "";
            this.time = -1L;
        }
    }
    public Result search(String query, float seconds) {
        try {

            String finalQuery = query.replaceAll("[^\\w\\s]", "").toLowerCase();
            String[] queryList = query.split(" ");

            SearchResponse<Podcast> response = client.search(
                    s -> s.index("spotify-podcasts-test")
                            .query(q -> q.match(m -> m.field("clips.words.word")
                                                        .query(finalQuery)
                                                )
                            )
                            .explain(true)
                            /*
                            .suggest(sug -> sug.text(finalQuery)
                                                .suggesters("suggestion",
                                                              sugg -> sugg.term(t -> t.field("clips.words.word"))
                                                )
                            )
                            */
                    , Podcast.class
            );


            boolean correction = false;
            StringBuilder suggestion = new StringBuilder();
            /*
            for(Suggestion<Podcast> s : response.suggest().get("suggestion")) {
                if(s.term().length() > 0) {
                    correction = true;
                    suggestion.append("<b>").append(s.term().options().text()).append("</b> ");
                }else{
                    suggestion.append(s.term().text()).append(" ");
                }
            }*/

            HashMap<String, Float> idf = new HashMap<>();
            List<Podcast> podcasts = new ArrayList<>();
            for (Hit<Podcast> result : response.hits().hits()) {
                if(queryList.length > 1){
                    result.explanation().details().forEach(detail -> {
                        String term = detail.description().replaceAll("(^weight\\(clips.words.word:)|( .*$)","");
                        if(!idf.containsKey(term)) {
                            Float idf_t = detail.value();
                            idf.put(term, idf_t);
                        }
                    });
                }else{
                    String term = result.explanation().description().replaceAll("(^weight\\(clips.words.word:)|( .*$)","");
                    if(!idf.containsKey(term)) {
                        Float idf_t = result.explanation().details().get(0).value();
                        idf.put(term, idf_t);
                    }
                }
                System.out.println("size: "+idf.size());
                for (Map.Entry<String, Float> entry : idf.entrySet()){
                    System.out.println("key= " + entry.getKey() + " and value= " + entry.getValue());
                }

                Podcast r = result.source();
                List<Fragment> fragments = new ArrayList<>();
                for(Clip clip : r.getClips()) {
                    for(int i = 0; i < clip.getWords().size(); i++) {
                        String clip_word = clip.getWords().get(i).getWord().replaceAll("[^\\w\\s]", "").toLowerCase();
                        if(containsTerm(queryList, clip_word)) {
                            Float score_t = 0.0f;
                            StringBuilder fragment = new StringBuilder();
                            int j = Math.max(i-5, 0);
                            double begin = clip.getWords().get(j).getStartTimeAsDouble();
                            String begin_s = convertSeconds(begin);
                            String end_s = "";
                            while(j < clip.getWords().size()) {
                                if(clip.getWords().get(j).getEndTimeAsDouble() - begin > seconds) {
                                    i = Math.max(i, j - 1);
                                    break;
                                }
                                String fragment_word = clip.getWords().get(j).getWord().replaceAll("[^\\w\\s]", "").toLowerCase();
                                if(containsTerm(queryList, fragment_word)) {
                                    fragment.append("<font color=\"#FFFF00\">").append(clip.getWords().get(j).getWord()).append("</font> ");
                                    System.out.println(clip.getWords().get(j).getWord());
                                    System.out.println(idf.getOrDefault(clip.getWords().get(j).getWord(), 0.0f));
                                    score_t += idf.getOrDefault(clip.getWords().get(j).getWord(), 0.0f);
                                }else{
                                    fragment.append(clip.getWords().get(j).getWord()).append(" ");
                                }
                                end_s = convertSeconds(clip.getWords().get(j).getEndTimeAsDouble());
                                j++;
                            }
                            if(fragment.length() > 0) {
                                //fragments.add(new Fragment("..."+fragment.toString()+"...",score_t,begin_s,end_s));
                                fragments.add(new Fragment("..."+fragment.toString()+"...",score_t,""+i,""+j));
                            }
                        }
                    }
                }
//                System.out.println("-----------");
//                System.out.println(fragments.get(0).getScore() + " : " + fragments.get(0).getFragment());
                Collections.sort(fragments);
//                System.out.println(fragments.get(0).getScore() + " : " + fragments.get(0).getFragment());
//                System.out.println(fragments.get(fragments.size()-1).getScore() + " : " + fragments.get(fragments.size()-1).getFragment());
//                System.out.println("-----------");
                r.setResultFragments(fragments);
                podcasts.add(r);
            }

            return new Result(podcasts, correction?suggestion.toString():"", response.took());
        } catch (Exception e) {
            e.printStackTrace();
            return new Result();
        }
    }

    public boolean containsTerm(String[] queryList, String word){
        for (String term: queryList){
            if (term.toLowerCase().equals(word)){
                return true;
            }
        }
        return false;
    }

    private String convertSeconds(double seconds){
        int temp = (int) seconds;
        int hh = temp / 3600;
        int mm = (temp % 3600) / 60;
        int ss = (temp % 3600) % 60;
        return (hh < 10 ? ("0" + hh) : hh) + ":" +
                (mm < 10 ? ("0" + mm) : mm) + ":" +
                (ss < 10 ? ("0" + ss) : ss);
    }
}
