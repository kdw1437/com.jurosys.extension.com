package com.jurosys.extension.com;

import org.slf4j.Logger;
import com.uro.DaoService;
import com.uro.log.LoggerMg;
import com.uro.service.sql.SQLServiceException;
import com.uro.transfer.ListParam;

public class DataTrans4 {
    Logger log = LoggerMg.getInstance().getLogger();

    public void execute(DaoService dao) {
        String a = dao.getStringValue("a");
        log.info("Received String: " + a);

        // Split 'a' into individual key-value pairs
        String[] keyValuePairs = a.split("&");
        
        // Define columns including all keys
        String[] keys = new String[keyValuePairs.length]; // including itemCodes
        String[][] values = new String[2][keyValuePairs.length]; // two rows for values

        int itemCodesIndex = -1; // To track the index of itemCodes

        for (int i = 0; i < keyValuePairs.length; i++) {
            String[] entry = keyValuePairs[i].split("=");

            keys[i] = entry[0];
            if (!entry[0].equals("itemCodes")) {
                values[0][i] = entry[1]; // Set the same value for both rows
                values[1][i] = entry[1];
            } else {
                itemCodesIndex = i; // Save the index of itemCodes
                // Split the itemCodes into separate entries
                String[] items = entry[1].split(",");
                values[0][i] = items[0]; // Set ELS1 for the first row
                values[1][i] = items[1]; // Set ELS2 for the second row
            }
        }

        // Check if itemCodesIndex is not set correctly
        if (itemCodesIndex == -1) {
            log.error("itemCodes key not found in the input string");
            return;
        }

        // Create ListParam and populate with values
        ListParam listParam = new ListParam(keys);
        for (int row = 0; row < 2; row++) {
            int rowIndex = listParam.createRow();
            for (int col = 0; col < keys.length; col++) {
                listParam.setValue(rowIndex, keys[col], values[row][col]);
            }
        }

        if (listParam != null) {
            log.info("Converted ListParam: " + listParam.toString());
        } else {
            log.error("Conversion failed, ListParam is null");
        }

        dao.setValue("InsertUserJobTp5", listParam);
        try {
            dao.sqlexe("s_insertUserJobTpHstr5", true);
        } catch (SQLServiceException e) {
            e.printStackTrace();
        }
    }
}

