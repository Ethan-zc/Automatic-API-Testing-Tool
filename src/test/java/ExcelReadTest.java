import automatic.testing.tool.utils.ExcelProcess;
import automatic.testing.tool.utils.RestfulClient;
import com.alibaba.fastjson.JSONObject;
import org.testng.Reporter;
import org.testng.annotations.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

public class ExcelReadTest {

    // client to handle http request and response
    private RestfulClient client;
    // save response code for justify
    private String url;
    // token for authorization
    private String token;
    // read data from APITestCase.xls
    private Object[][] excelData;
    // http head information
    private Map<String, String> hashHead;
    // testing cases index mapping hash map.
    private Map<String, List<Integer>> testcaseIndexMap;
    // List that stores the index of failed testing cases.
    private List<Integer> failedCaseIndex;
    private Properties props;
    private String excelPath;
    private String host;

    @BeforeSuite (alwaysRun = true)
    public void setup() throws IOException {

        try {
            props = new Properties();
            FileInputStream file = new FileInputStream(System.getProperty("user.dir") +
                    "\\src\\test\\resources\\config.properties");
            props.load(file);
        } catch (Exception e) {
            e.printStackTrace();
        }

        host = props.getProperty("Host");
        excelPath = props.getProperty("testData");
        // Read excel file and initialize essential parameters.
        excelData = ExcelProcess.processExcel(excelPath, 0);

        // token not used yet.
        token = null;
        client = new RestfulClient();
        hashHead = new HashMap<>();
        testcaseIndexMap = new HashMap<>();
        failedCaseIndex = new ArrayList<>();

        // Read through all test cases in excel file and hash them based on request
        // type and requesting address.
        for ( int i = 1; i < excelData.length; i++ ) {
            // Based on the type of http request, the sequence number of corresponding
            // testing cases would be hashed into List which indexed by http request types.
            // Determine if the testing case should be run: 1 as yes, 0 as no.
            String runSwitch = excelData[i][2].toString();

            if (runSwitch.equals("1") || runSwitch.equals("true")) {
                String httpRequest = excelData[i][4].toString();
                if ( !testcaseIndexMap.containsKey( httpRequest ) ) {
                    List<Integer> index = new ArrayList<>();
                    index.add(i);
                    testcaseIndexMap.put(httpRequest,index);
                } else {
                    List<Integer> index_new = testcaseIndexMap.get(httpRequest);
                    index_new.add(i);
                    testcaseIndexMap.put(httpRequest, index_new);
                }
                failedCaseIndex.add(i);
            }
        }
    }

    // Get the loginInfo based on the Excel and provided to loginTest case.
    @DataProvider( name = "postRequestInfoProvider" )
    public Object[][] PostInfoProvider() {
        List<Integer> indexList = testcaseIndexMap.get( "post" );
        return ExcelProcess.readParameters(indexList);
    }

    @Test ( dataProvider = "postRequestInfoProvider" )
    public void postRequestTest( String address, String checkPoints, String checkValues, String rowNum )
            throws IOException {
        url = host + address;
        // Read parameters from excel file
        JSONObject responseBodyJSON;
        int row = Integer.parseInt(rowNum);
        String sendingKeys = ExcelProcess.readKeyPairs(row);

        // send request through client and analyze response
        hashHead.put( "Content-Type", "application/json; charset=utf8" );
        client.sendPost(url, sendingKeys, (HashMap<String, String>) hashHead);
        responseBodyJSON = client.getBodyInJSON();
        System.out.println( "This is response body: " + responseBodyJSON );
        Reporter.log("Response body is: " + responseBodyJSON);

        client.acceptanceChecking(checkPoints,checkValues);
        failedCaseIndex.removeIf(index -> Integer.parseInt(rowNum) == index);
    }


    @DataProvider ( name = "getRequestInfoProvider" )
    public Object[][] getInfoProvider() {
        List<Integer> indexList = testcaseIndexMap.get( "get" );
        return ExcelProcess.readParameters(indexList);
    }

    @Test ( dataProvider = "getRequestInfoProvider" )
    public void getRequestTest ( String address, String checkPoints, String checkValues, String rowNum )
            throws IOException, URISyntaxException {
        url = host + address;

        HashMap<String, String> keys = new HashMap<>();
        int row = Integer.parseInt( rowNum );
        for (int cell = 7;cell <= excelData[row].length-2; cell = cell+2) {
            // Not sure about the # of parameters, therefore doing not null judgement here.
            if (excelData[row][cell] == null) {
                break;
            }
            keys.put( excelData[row][cell].toString(),excelData[row][cell+1].toString() );
        }
        // send request through client and analyze response
        // hashHead.put( "Authorization", "Bearer "+token );
        client.sendGet(url, keys, (HashMap<String, String>) hashHead);

        // check if response value is equal to checkValue
        client.acceptanceChecking(checkPoints,checkValues);
        failedCaseIndex.removeIf(index -> Integer.parseInt(rowNum) == index);
    }

    @DataProvider( name = "deleteRequestInfoProvider")
    public Object[][] deleteInfoProvider() {
        List<Integer> indexList = testcaseIndexMap.get( "delete" );
        return ExcelProcess.readParameters(indexList);
    }

    @Test ( dataProvider = "deleteRequestInfoProvider" )
    public void deleteRequestTest ( String address, String checkPoints, String checkValues, String rowNum )
            throws IOException, URISyntaxException {
        url = host + address;

        HashMap<String, String> keys = new HashMap<>();
        int row = Integer.parseInt( rowNum );
        for (int cell = 7;cell <= excelData[row].length-2; cell = cell+2) {
            // Not sure about the # of parameters, therefore doing not null judgement here.
            if (excelData[row][cell] == null) {
                break;
            }
            keys.put( excelData[row][cell].toString(),excelData[row][cell+1].toString() );
        }

        // send request through client and analyze response
        // hashHead.put( "Authorization", "Bearer "+token );
        client.sendDelete(url, keys, (HashMap<String, String>) hashHead);

        // check if response value is equal to checkValue
        client.acceptanceChecking(checkPoints,checkValues);
        failedCaseIndex.removeIf(index -> Integer.parseInt(rowNum) == index);
    }

    @DataProvider ( name = "putRequestInfoProvider" )
    public Object[][] putInfoProvider() {
        List<Integer> indexList = testcaseIndexMap.get( "put" );
        return ExcelProcess.readParameters(indexList);
    }

    @Test ( dataProvider = "putRequestInfoProvider" )
    public void putRequestTest( String address, String checkPoints, String checkValues, String rowNum )
            throws IOException {
        url = host + address;
        // Read parameters from excel file
        JSONObject responseBodyJSON;
        int row = Integer.parseInt(rowNum);
        String sendingKeys = ExcelProcess.readKeyPairs(row);

        // send request through client and analyze response
        hashHead.put( "Content-Type", "application/json" );
        client.sendPut(url, sendingKeys, (HashMap<String, String>) hashHead);
        responseBodyJSON = client.getBodyInJSON();
        System.out.println( "This is response body: " + responseBodyJSON );
        Reporter.log("Response body is: " + responseBodyJSON);

        // check if response value is equal to checkValue
        client.acceptanceChecking(checkPoints,checkValues);
        failedCaseIndex.removeIf(index -> Integer.parseInt(rowNum) == index);
    }

    @AfterTest(alwaysRun = true)
    public void excelRearrange() throws IOException {
        System.out.println("This is failed testing case index: " + failedCaseIndex);
        if ( failedCaseIndex.size() != 0) {
            ExcelProcess.rearrangeCertainRows(failedCaseIndex);
        }
        System.out.println("number of failed case is : " + failedCaseIndex.size());
        ExcelProcess.turnOffPassedCase(failedCaseIndex.size());
    }
}