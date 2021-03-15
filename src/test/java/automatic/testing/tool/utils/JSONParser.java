package automatic.testing.tool.utils;

import com.alibaba.fastjson.JSONObject;

public class JSONParser {

    public String getCorrespondingValue(JSONObject jo, String checkpoint) {

        if (checkpoint.contains("/")) {
            String subDir[] = checkpoint.split("/");
            String stringSubData = null;
            for (String dir:subDir) {
                stringSubData = jo.get(dir).toString();
                if(stringSubData.contains(":")) {
                    jo = JSONObject.parseObject(stringSubData);
                }
            }
            return stringSubData;
        } else {
            return jo.get(checkpoint).toString();
        }
    }
}