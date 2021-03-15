Automatic API Testing Tool Document

**Zichen YANG** 

# Purpose & Brief Introduction
API testing is an important part of project development, it helps to find bugs as early as possible to improve the quality of ongoing project. However, most API testing tool is not open-source and free. 

Here I proposed and established an automatic API Testing Tool. This testing tool read input testing cases from excel file, send http request and check if the response from testing API meets the acceptance criteria automatically. The workflow of the testing tool could be described as follow: 

![](Aspose.Words.878f2690-eae9-464c-b246-3fd6d085cbb8.001.png)

The testing tool would firstly read testing cases from excel file. For each testing case, if the run switch is off, then the testing case would not be run. If the run switch is on, parameters of http request would be read and packaged, then the http request would be send to the testing API. After receiving response, it would check if the response information meets the acceptance criteria. If yes, which means the testing case has passed, the run switch of the testing case would be turned off. If no, then the testing case would be moved to the top of excel file. Finally, all testing result would be write into a HTML report. 

# Technology Stack
1. **TestNG**: TestNG is a testing framework designed to simplify a broad range of testing needs, from unit testing (testing a class in isolation of the others) to integration testing (testing entire systems made of several classes, several packages and even several external frameworks, such as application servers). The mainly used feature in this project is the data-driven feature, which allows me to provide the information of each testing case easily to the short testing code. 
1. **HttpComponent**: Here HttpClient is mainly used to handle POST, PUT, DELETE, and GET http request and receive the response from testing API. It can be used to request HTTP resources over the network. It supports HTTP/1.1 and HTTP/2, both synchronous and asynchronous programming models, handles request and response bodies as [reactive-streams](http://reactive-streams.org/), and follows the familiar builder pattern.
1. **FastJSON:** FastJSON is a Java library that can be used to convert Java Objects into their JSON representation. It can also be used to convert a JSON string to an equivalent Java object. FastJSON can work with arbitrary Java objects including pre-existing objects that you do not have source-code of. In this project FastJSON is used to parse JSON data received from testing API. 
1. **POI:** The Apache POI Project's mission is to create and maintain Java APIs for manipulating various file formats based upon the Office Open XML standards (OOXML) and Microsoft's OLE 2 Compound Document format (OLE2). Here it is used to read and operate on the excel testing cases file. 
1. **ReportNG:** ReportNG is a simple HTML reporting plug-in for the [TestNG](http://www.testng.org/) unit-testing framework. It is intended as a replacement for the default TestNG HTML report. The default report is comprehensive but is not so easy to understand at-a-glance. ReportNG provides a simple, color-coded view of the test results.
# Environment setup
Before using the automatic testing tool, please make sure following environment values are set up:

1. **IntelliJ IDEA**: Since I used a important plugin in IntelliJ IDEA, it would be more convenient to choose IntelliJ IDEA to do the testing work. 
1. **JDK**: The java JDK version for this project is 1.8.

Please use IntelliJ IDEA to create a new project from existing source. Click File > New > Project from Existing Sources…, and select the folder where source code is stored. Please import it as a MAVEN project. 

![](Aspose.Words.878f2690-eae9-464c-b246-3fd6d085cbb8.002.png)The structure of the project is as follow: 

1. **automatic.testing.tool.utils.ExcelProcess**:  This class is used to process data in excel file, it takes String Excel filePath and Integer sheetId as input, returns a 2-dimension object, which saves excel data in corresponding cell index. At the same time, it helps to read parameters of each testing case from excel file, rearrange the order of testing case based on result, and turn off passing testing cases. 
1. **automatic.testing.tool.utils.JSONParser**: JSONParser is used to parse responding information. It helps to get corresponding information from API response. 
1. **automatic.testing.tool.utils.RestfulClient**: RestfulClient is a packaged http client, which could pack and send POST, GET, PUT, DELETE requests. It helps to get response body in JSON format and do acceptance checking. 
1. **automatic.testing.tool.utils.TestAPIReader**: The class is used to read config.properties, which contains the host name of API and the path of excel file. It is also the parent class of ExcelReadTest.
1. **automatic.testing.tool.utils.ExcelReadTest**: This is the core class of doing automatic testing work. Detailed information of each test case would be read and do the testing. Data Driven pattern is used here to do efficient testing. 
1. ![](Aspose.Words.878f2690-eae9-464c-b246-3fd6d085cbb8.003.png) Meanwhile, the resources of testing class need to be prepared as following: 

The path of testResources is:

**/AutomaticTestingTool /src/test/resources/testResources**

1. **APITestCase.xls**: This is the excel file that stores all the test cases. Detailed information about how to input testing case would be introduced in Workflow part. 
1. **config.properties**: In this file, host name and testData would be provided. Host name is the URL of testing API, testData would be the path to the excel file. 


##
# How to use it? 
The workflow of this testing class is worked as following:

1. **Enter the testing case in APITestCase.xls, please remember to save the change before the next step.**

![](Aspose.Words.878f2690-eae9-464c-b246-3fd6d085cbb8.004.png)

1. **Test No.**: Order number of testing case. 
1. **Test Name:** The description of testing case, this part would be printed in the report. 
1. **Run Switch:** This is the running switch for each testing cases. If it is marked as 1, then the testing case would be run. If it is marked as 0, then the testing case would not be run. 
1. **Address:** the detailed address of testing API. 
1. **httpRequest:** Type of httpRequest, now only support for POST and GET. Would add DELETE and PUT in the future. 
1. **checkpoint:** parameter names that need to be checked in this testing case. The name need to be the same as the name in API response.** For multiple layer JSON parameter check, please use “/” to clarify that the layer. For example, in Test No. 6, the checkpoint is data/User/loginName, which means that the checkpoint is loginName under User, and User is under data. 
1. **Acceptance Criteria:** parameter values that need to be checked. When the responding value is the same as input acceptance criteria, the testing case would pass. 
1. **key/keyValue:** Here key means the name of parameter and keyValue of the parameters. For GET, it would be added as parameters, while POST would use it as JSON body form. The number of key and keyValue pair is not restricted, it could read as many parameters as the API required. For multiple layer JSON, please put the entire first layer data into the keyValue part.** 

**There are several special cases:** 

1. **If one single parameter without name is required by the API:** such as in the DCHR project, while /recognition/like require only one single id as the body of the request body, (single) would be marked as one single parameter and leave the key name as blank.** 

![](Aspose.Words.878f2690-eae9-464c-b246-3fd6d085cbb8.005.png)

![](Aspose.Words.878f2690-eae9-464c-b246-3fd6d085cbb8.006.png)

1. **If one parameter in the request body is a list of JSON pattern data:** As in DCHR project /recognition/create API, parameter entry is a JSON list. ![](Aspose.Words.878f2690-eae9-464c-b246-3fd6d085cbb8.007.png)

To handle this situation, use [] to mark it as a list and use “, ”(dot and one single space) to mark the separation of different JSON data. Please make sure that parameters in one single JSON is separated by only “,”(dot). 

![](Aspose.Words.878f2690-eae9-464c-b246-3fd6d085cbb8.008.png)

1. **If the checkpoint is a parameter of response body’s JSON data:** Take project DCHR as an example, here I want to check if the total list number is equal to what I know, the total number is in body/total as following: ![](Aspose.Words.878f2690-eae9-464c-b246-3fd6d085cbb8.009.png)

Therefore, I fill in the checkpoint as: /body/total:

![](Aspose.Words.878f2690-eae9-464c-b246-3fd6d085cbb8.010.png)

1. **If there are multiple checkpoints in one single testing case:** Also take DCHR as an example, when I want to check multiple different checkpoint in one testing case, the checkpoints and corresponding acceptance criteria could be marked as following: ![](Aspose.Words.878f2690-eae9-464c-b246-3fd6d085cbb8.011.png)

Here I check if the body/fromUserName is equal to tao, and body/backgroundColor is equal to tao and yellow. Only when all checkpoints meet acceptance criteria, the testing case would be marked as passed. 

1. **Check in config.properties if the host and testData is matched as required.** 

![](Aspose.Words.878f2690-eae9-464c-b246-3fd6d085cbb8.012.png)

Please make sure that the API is running and the testData path is the place where the APITEstCase.xls is stored. 

1. **Run the configure Automatic Testing.** 

Please edit the configuration of AutomaticTesting and set the path to the testng.xml as stored in the folder: 

![](Aspose.Words.878f2690-eae9-464c-b246-3fd6d085cbb8.013.png)

![](Aspose.Words.878f2690-eae9-464c-b246-3fd6d085cbb8.014.png)

1. **Run the config AutomaticTesting.** 

![](Aspose.Words.878f2690-eae9-464c-b246-3fd6d085cbb8.015.png)

As we could see here, for failing testing cases, it would be marked as yellow cross, while passing testing case would be marked as green tick. At the same time, after run the testing, failed testing cases would be moved to the top of the excel file and keep turning on. 

![](Aspose.Words.878f2690-eae9-464c-b246-3fd6d085cbb8.016.png)
##
Finally, the report would be generated and put in folder test-output. In html folder, overview.html would have an overview of the testing result: 

![](Aspose.Words.878f2690-eae9-464c-b246-3fd6d085cbb8.017.png)

Click on the test suit, more detailed information would be demonstrated: 

![](Aspose.Words.878f2690-eae9-464c-b246-3fd6d085cbb8.018.png)

For failed testing cases, their detailed input read from testing case and response information would be demonstrated at the same time. 

# References:
<https://www.cnblogs.com/yingyingja/p/9973960.html>

<https://testng.org/doc/documentation-main.html>

<https://hc.apache.org/>

<https://www.w3cschool.cn/fastjson/>

<https://mvnrepository.com/>
