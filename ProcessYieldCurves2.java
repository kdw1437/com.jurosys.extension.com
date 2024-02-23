package com.jurosys.extension.com;

import com.uro.DaoService;
import com.jurosys.extension.com.NoBaseDtException;
import com.jurosys.extension.com.NoDataIdsException;
import com.uro.log.LoggerMg;
import com.uro.service.sql.SQLServiceException;
import com.uro.transfer.ListParam;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import java.util.*;
public class ProcessYieldCurves2 {
    Logger log = LoggerMg.getInstance().getLogger("fw");

    public void execute(DaoService dao) {
        try {
        	// Validation Check (다음주 월요일 까지 완성하도록 하기)
        	String errorMsg = ""; //error message 빈 string에 error Message 내용을 추가해주도록 하기. 고로 error가 걸릴 경우, errorMsg string에 error 내용을 추가해주고, error내용을 retrun
        	//Exception 새로운 클래스를 짜서, Exception클래스 상속받아서 사용하는 것은 지양하도록 하기.
        	//illegalArgumentException학습해서 처리하기. Exception새로운 클래스를 만들어서 사용하지 말고, illegalArgumentException 사용하도록 하기.
        	//1. baseDt가 null인 경우
        	if( dao.getParam().getMap().get("baseDt") == null){
        		//err
        	}
        	
        	//2. dataIds가 null인 경우
        	if(dao.getParam().getMap().get("baseDt") == null || dao.getParam().getMap().get("dataIds").equals(""))	{
        		
        	}
        	//3. 위에가 있어도 결과물이 없는 경우 (dataIds=빈 경우)
        	//4. 결과물이 있는 데, 결과물에 해당하는 내용이 없을 경우 (dataIds = KRSIRSZ<<이런 경우) <<이 경우는 rowSize에서 처리하도록 한다.
            // SQL query 실행
            dao.sqlexe("s_selectYieldCurves3", false); // sqlquery ID
            ListParam result = dao.getNowListParam(); // query의 결과를 받는다.
            
            if (result.rowSize() == 0) {
                JSONObject errorJson = new JSONObject();
                errorJson.put("error", "URL에 필요한 parameter를 제대로 입력하지 않았습니다.");
                dao.setValue("response", errorJson.toMap()); //errorJson을 HashMap객체로 바꿔준 뒤에, response로 설정한다.
                log.info(errorJson.toString()); // error message 로그를 console에 남긴다.
                //return; // execute 메소드를 빠져 나간다.
                throw new IllegalArgumentException();        
            }
            
            if (result.rowSize() == 0 && dao.getParam().getMap().get("dataIds") == null) {
            	JSONObject errorJson = new JSONObject();
                errorJson.put("error", "URL에 필요한 parameter를 제대로 입력하지 않았습니다.");
                dao.setValue("response", errorJson.toMap()); //errorJson을 HashMap객체로 바꿔준 뒤에, response로 설정한다.
                log.info(errorJson.toString()); // error message 로그를 console에 남긴다.
                //throw new NoDataIdsException();
                return;
            }
            // JSONObject를 초기화 한다.
            JSONObject finalJson = new JSONObject();
            JSONArray yieldsArray = new JSONArray();

            // result의 각 열을 iterate한다.
            for (int i = 0; i < result.rowSize(); i++) {
                String baseDt = result.getValue(i, "baseDt", "");
                String dataSetId = result.getValue(i, "dataSetId", "");
                String dataId = result.getValue(i, "dataId", "");
                String currency = result.getValue(i, "currency", "");

                JSONObject yieldObject = new JSONObject();
                String tenorStr = result.getValue(i, "yields.tenor", "");
                double tenorNum = Double.parseDouble(tenorStr);  // Converts string to double
                yieldObject.put("tenor", tenorNum);  // Automatically uses numeric JSON representation
                //yieldObject.put("tenor", result.getValue(i, "yields.tenor", ""));
                
                String rateStr = result.getValue(i, "yields.rate", "");
                double rateNum = Double.parseDouble(rateStr);  // Converts string to double
                yieldObject.put("rate", rateNum);
                //yieldObject.put("rate", result.getValue(i, "yields.rate", ""));

                // 새로운 dataId 인지 있는 것인지 확인한다.
                JSONObject existingDataIdObject = findInArray(yieldsArray, dataId);
                if (existingDataIdObject != null) {
                    // Existing dataId, yields array에 추가
                    existingDataIdObject.getJSONArray("yields").put(yieldObject);
                } else {
                    // 새로운 dataId, 새로운 entry 만들기
                    JSONObject newDataIdObject = new JSONObject();
                    newDataIdObject.put("baseDt", baseDt);
                    newDataIdObject.put("dataSetId", dataSetId);
                    newDataIdObject.put("dataId", dataId);
                    newDataIdObject.put("currency", currency);

                    JSONArray newYieldsArray = new JSONArray();
                    newYieldsArray.put(yieldObject);
                    newDataIdObject.put("yields", newYieldsArray);

                    yieldsArray.put(newDataIdObject);
                }
            }

            finalJson.put("yieldCurves", yieldsArray);
            Map<String, Object> hashMap = finalJson.toMap();
            String jsonResponse = finalJson.toString().replace("\\", "");
            dao.setValue("response", hashMap);
            log.info("HashMap contents: " + hashMap.toString());
            //dao.setValue("response", finalJson.toString());
            log.info(finalJson.toString());

        } catch (SQLServiceException e) {
            log.error("Error processing yield curves", e);
            dao.setError("Error processing yield curves: " + e.getMessage());
		} catch (IllegalArgumentException e) {
			dao.setError("baseDt를 입력하세요");
		}
    }

    private JSONObject findInArray(JSONArray array, String dataId) {
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            if (obj.getString("dataId").equals(dataId)) {
                return obj;
            }
        }
        return null;
    }
}
