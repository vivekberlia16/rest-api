package com.mkyong;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mkyong.error.RestClientUnavailableException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.ServiceUnavailableRetryStrategy;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultServiceUnavailableRetryStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

@Component
public class HttpClientUtil {
    private ObjectMapper mapper;


    /*
     * HttpClient -in order to create parameterized mapper value for j-unit
     *
     */
    public HttpClientUtil(ObjectMapper mapper) {
        this.mapper = mapper;

    }

    public HttpClientUtil() {
        mapper = new ObjectMapper();
    }

    private static <T extends HttpRequestBase> T addHeaders(T object, Map<String, String> headers) {

        for (String key : headers.keySet()) {
            object.addHeader(key, headers.get(key));
        }
        return object;
    }

    @Retryable(maxAttempts=3,value= RestClientUnavailableException.class,backoff = @Backoff(delay = 1000 ,multiplier=2))
    public String apiServiceUtil(Map<String, String> headers, String endPoint, Object requestObject, HttpMethod httpMethod) throws BizException {
        System.out.println("Inside api service call");
        HttpRequestBase httpGet = null;
        HttpClient client = null;
        ResponseHandler<String> handler = null;
        String memPro = null;
        StringEntity input = null;
        try {


               HttpClientBuilder builder=HttpClientBuilder.create();
//           ServiceUnavailableRetryStrategy serviceUnavailableRetryStrategy = new DefaultServiceUnavailableRetryStrategy(
//                  3, 3000);
//           builder.setServiceUnavailableRetryStrategy(serviceUnavailableRetryStrategy);
            client = builder.build();
            handler = new BasicResponseHandler();
            HttpResponse resp = null;
            try {
                if (null != requestObject) {
                    memPro = mapper.writeValueAsString(requestObject);
                    input = new StringEntity(memPro, ContentType.APPLICATION_JSON);
                }
            } catch (JsonProcessingException e) {
                throw new BizException(e.getMessage());
            }
            if (httpMethod.equals(HttpMethod.GET)) {
                httpGet = new HttpGet(endPoint);
                httpGet = addHeaders(httpGet, headers);
                resp = client.execute(httpGet);
            }

            if (httpMethod.equals(HttpMethod.PUT)) {
                HttpPut httpPut = new HttpPut(endPoint);
                httpPut = addHeaders(httpPut, headers);
                if (null != memPro && !"".equals(memPro))
                    httpPut.setEntity(input);
                resp = client.execute(httpPut);
            }
            if (httpMethod.equals(HttpMethod.POST)) {
                HttpPost httpPost = new HttpPost(endPoint);
                httpPost = addHeaders(httpPost, headers);
                if (null != memPro && !"".equals(memPro))
                    httpPost.setEntity(input);
                resp = client.execute(httpPost);
            }
            if (httpMethod.equals(HttpMethod.DELETE)) {
                HttpDelete httpDelete = new HttpDelete(endPoint);
                httpDelete = addHeaders(httpDelete, headers);
                resp = client.execute(httpDelete);
            }

            if (resp.getStatusLine().getStatusCode() == 200) {
                return handler.handleResponse(resp);
            }
            else if(resp.getStatusLine().getStatusCode()==503)
            {
                System.out.println("throwing rest client unaviable exception");
                throw  new  RestClientUnavailableException();
            }
            else {
                captureError(resp);
                return null;
            }
        } catch (IOException e) {
            throw new BizException("Error in making api call.", e);
        }
    }

    public void captureError(HttpResponse resp) throws BizException {
        Logger LOG = LoggerFactory.getLogger(this.getClass());

        StringBuffer result = new StringBuffer();
        String line = "";
        BufferedReader rd = null;
        String errorResponse = null;
        try {
            rd = new BufferedReader(
                    new InputStreamReader(resp.getEntity().getContent()));
            while ((line = rd.readLine()) != null) {
                if (line.equals("")) {
                    result.append("\\n");
                } else {
                    result.append(line);
                }
            }
            JsonParser jsonParser;
            LOG.error(result.toString());
            jsonParser = mapper.getFactory().createParser(result.toString());
            JsonNode jsonNode = mapper.readTree(jsonParser);
            errorResponse = (null != jsonNode.get("error")) ? jsonNode.get("error").toString()
                    : ((null != jsonNode.get("httpMessage")) ? (jsonNode.get("httpMessage").toString() + " : " + jsonNode.get("moreInformation").toString())
                    : result.toString());
            BizException exception = mapper.readValue(errorResponse, BizException.class);
            throw exception;
        } catch (UnsupportedOperationException | IOException e1) {
            LOG.error("Error in Reading Response. Error message: " + result.toString(), e1);
            throw new BizException("Error in Reading Response. Error message: " + result.toString(), e1);
        }
    }
}