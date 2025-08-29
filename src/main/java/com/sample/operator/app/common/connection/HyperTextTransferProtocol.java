package com.sample.operator.app.common.connection;

import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class HyperTextTransferProtocol {

    public String httpConn(String url, String bodyData, int timeout)
    {

        try
        {
            SimpleClientHttpRequestFactory reqFac = new SimpleClientHttpRequestFactory();
            reqFac.setConnectTimeout(timeout);
            reqFac.setReadTimeout(timeout);

            RestTemplate restTemplate = new RestTemplate(reqFac);

            MediaType mt = MediaType.valueOf("application/json;charset=utf-8");
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(mt);
            headers.setAccept(List.of(mt));

            HttpEntity<String> entity = new HttpEntity<String>(bodyData, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            return response.getBody();
        }catch(Exception e)
        {
            System.out.println("오류");
            return null;
        }
    }

}
