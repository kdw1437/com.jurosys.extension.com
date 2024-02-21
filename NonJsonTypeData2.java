package com.jurosys.extension.com;

import com.uro.DaoService;
import com.uro.log.LoggerMg;
import com.uro.service.sql.SQLServiceException;
import com.uro.transfer.ListParam;
import org.slf4j.Logger;

public class NonJsonTypeData2 {
    private Logger log = LoggerMg.getInstance().getLogger();

    public void execute(DaoService dao) {
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
            dao.setValue("InsertUserJobTp9", listParam);
            try {
                dao.sqlexe("s_insertUserJobTpHstr9", true);
            } catch (SQLServiceException e) {
                log.error("SQL execution failed", e);
            }
        } else {
            log.error("Conversion failed, ListParam is null");
        }
    }
}
