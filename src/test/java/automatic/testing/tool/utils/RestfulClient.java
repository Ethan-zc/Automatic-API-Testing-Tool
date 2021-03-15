package automatic.testing.tool.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class RestfulClient {
    private final HttpClient httpClient;
    private HttpResponse httpResponse;
    private JSONObject responseBody;

    public RestfulClient() {
        httpClient = HttpClients.createDefault();
    }

    public void sendPost(String url, String inputValue, HashMap<String, String> headers) throws IOException {

        HttpPost httpPost = new HttpPost(url);

        // Setting body format
        StringEntity entity = new StringEntity(inputValue, Consts.UTF_8);
        httpPost.setEntity(entity);
        System.out.println("This is sending body: " + inputValue);

        // Setting header information
        Set<String> set = headers.keySet();
        for (String key : set) {
            String value = headers.get(key);
            httpPost.addHeader(key, value);
        }
        httpResponse = httpClient.execute(httpPost);

    }

    public void sendGet(String url, HashMap<String, String> hashParams, HashMap<String,String> headers) throws URISyntaxException, IOException {

        URIBuilder uri = new URIBuilder(url);
        Set<String> paramsSet = hashParams.keySet();
        for (String key : paramsSet) {
            String value = hashParams.get(key);
            uri.setParameter(key, value);
        }
        URI uriAddress = uri.build();
        HttpGet httpGet = new HttpGet(uriAddress);

        Set<String> headerSet = headers.keySet();
        for (String key : headerSet) {
            String value = headers.get(key);
            httpGet.addHeader(key, value);
        }
        httpResponse = httpClient.execute(httpGet);

    }

    public void sendDelete(String url, HashMap<String,String> hashParams, HashMap<String,String> headers) throws IOException, URISyntaxException {

        URIBuilder uri = new URIBuilder(url);
        Set<String> paramsSet = hashParams.keySet();
        for (String key : paramsSet) {
            String value = hashParams.get(key);
            uri.setParameter(key, value);
        }
        URI uriAddress = uri.build();
        HttpDelete httpDelete = new HttpDelete(uriAddress);

        Set<String> headerSet = headers.keySet();
        for (String key : headerSet) {
            String value = headers.get(key);
            httpDelete.addHeader(key, value);
        }
        httpResponse = httpClient.execute(httpDelete);
    }

    public void sendPut(String url, String inputValue, HashMap<String, String> headers) throws IOException {

        HttpPut httpPut = new HttpPut(url);

        // Setting body format
        StringEntity entity = new StringEntity(inputValue, Consts.UTF_8);
        httpPut.setEntity(entity);

        // Setting header information
        Set<String> set = headers.keySet();
        for (String key : set) {
            String value = headers.get(key);
            httpPut.addHeader(key, value);
        }
        httpResponse = httpClient.execute(httpPut);

    }

    // Return responseCode in String
    public String getCodeInString() {

        return String.valueOf(httpResponse.getStatusLine().getStatusCode());

    }

    // Return Body
    public JSONObject getBodyInJSON() throws IOException {
        HttpEntity entity;
        String entityToString;
        entity = httpResponse.getEntity();
        entityToString = EntityUtils.toString(entity);
        entityToString = new String(entityToString.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        System.out.println( "This is response entity: " + entityToString);
        responseBody = JSON.parseObject(entityToString);

        return responseBody;
    }

    // Determine if the corresponding info is matched with acceptance criteria
    public void acceptanceChecking(String checkPoints, String checkValues) {
        JSONParser jParser = new JSONParser();
        String[] checkPointList = checkPoints.split(",");
        String[] checkValueList = checkValues.split(",");
        for (int checkPointNum = 0; checkPointNum < checkPointList.length; checkPointNum++) {
            String result = jParser.getCorrespondingValue( responseBody, checkPointList[checkPointNum] );
            Assert.assertEquals( result, checkValueList[checkPointNum] );
        }
    }


}