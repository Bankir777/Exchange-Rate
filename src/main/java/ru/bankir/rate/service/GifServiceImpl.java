package ru.bankir.rate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.bankir.rate.feign_client.FeignClientGif;
import ru.bankir.rate.service.serviceinterface.GifService;
import java.util.Map;

@Service
public class GifServiceImpl implements GifService {
    private final FeignClientGif gifClient;
    @Value("n2yqEUnH1RjsEXAa4amhmAH87yFSVl4D")
    private String apiKey;

    @Autowired
    public GifServiceImpl(FeignClientGif gifClient) {
        this.gifClient = gifClient;
    }

    public ResponseEntity<Map> getGif(String tag) {
        ResponseEntity<Map> result = gifClient.getRandomGif(this.apiKey, tag);
        result.getBody().put("compareResult", tag);
        return result;
    }
}