package com.jurosys.extension.com;

import org.slf4j.Logger;
import com.uro.DaoService;
import com.uro.log.LoggerMg;
import com.uro.service.sql.SQLServiceException;
import com.uro.transfer.ListParam;
import com.uro.transfer.Param;

public class DataTrans5 {
    Logger log = LoggerMg.getInstance().getLogger();

    public void execute(DaoService dao) {

        String a = dao.getStringValue("a");
        log.info("Received String: " + a);
        
        String[] keyValuePairs = a.split("&"); // Splitting the string by '&'
        ListParam listParam = new ListParam(new String[]{"key", "value"});

        for (String pair : keyValuePairs) {
            String[] entry = pair.split("=");
            if (entry.length == 2) {
                int rowIndex = listParam.createRow();
                listParam.setValue(rowIndex, "key", entry[0]);
                listParam.setValue(rowIndex, "value", entry[1]);
            }
        }

        if (listParam != null) {
            log.info("Converted ListParam: " + listParam.toString());
        } else {
            log.error("Conversion failed, ListParam is null");
        }

        dao.setValue("InsertUserJobTp6", listParam);
        try {
            dao.sqlexe("s_insertUserJobTpHstr6", true);
        } catch (SQLServiceException e) {
            e.printStackTrace();
        }
    }
}
