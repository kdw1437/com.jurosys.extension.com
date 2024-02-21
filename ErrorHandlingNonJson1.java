package com.jurosys.extension.com;

import com.uro.DaoService;
import com.uro.log.LoggerMg;
import com.uro.service.sql.SQLServiceException;
import com.uro.transfer.ListParam;
import org.json.JSONObject; // Make sure to import a JSON library
import org.slf4j.Logger;

public class ErrorHandlingNonJson1 {
    private Logger log = LoggerMg.getInstance().getLogger();

    public JSONObject execute(DaoService dao) {
        JSONObject response = new JSONObject();
        try {
            String dataString = dao.getStringValue("a");
            log.info("Received String: " + dataString);

            // Split the dataString into individual rows
            String[] rows = dataString.split("&(?=BASE_DT)");

            // Assuming all rows have the same columns, use the first row to determine the columns
            String[] columns = rows[0].split("&");
            String[] keys = new String[columns.length];
            for (int i = 0; i < columns.length; i++) {
                keys[i] = columns[i].split("=")[0];
            }

            // Create ListParam and populate with values for each row
            ListParam listParam = new ListParam(keys);
            for (String row : rows) {
                String[] keyValuePairs = row.split("&");
                int rowIndex = listParam.createRow();
                for (String pair : keyValuePairs) {
                    String[] keyValue = pair.split("=");
                    String key = keyValue[0];
                    String value = keyValue.length > 1 ? keyValue[1] : "";
                    listParam.setValue(rowIndex, key, value);
                }
            }

            if (listParam != null) {
                log.info("Converted ListParam: " + listParam.toString());
                dao.setValue("InsertUserJobTp11", listParam);
                dao.sqlexe("s_insertUserJobTpHstr11", true);

                // If successful, set the response
                response.put("status", "success");
                response.put("message", "Operation completed successfully");
            } else {
                log.error("Conversion failed, ListParam is null");
                response.put("status", "error");
                response.put("message", "Conversion failed, ListParam is null");
            }
        } catch (SQLServiceException e) {
            log.error("SQL execution failed", e);
            response.put("status", "error");
            response.put("message", e.getMessage());
        } catch (Exception e) {
            log.error("Error occurred", e);
            response.put("status", "error");
            response.put("message", e.getMessage());
        }
        
        log.info("Response: " + response.toString());
        System.out.println(response);
        //String response1 = response.toString();
        return response;
        
        
    }
}
