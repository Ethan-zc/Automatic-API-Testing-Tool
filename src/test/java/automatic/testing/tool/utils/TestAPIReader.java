package automatic.testing.tool.utils;

import java.io.FileInputStream;
import java.util.Properties;

public class TestAPIReader {

    public Properties props;
    public String excelPath;
    public String host;

    public TestAPIReader() {
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
    }
}