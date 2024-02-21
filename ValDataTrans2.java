package com.jurosys.extension.com;

import org.slf4j.Logger;
import com.uro.DaoService;
import com.uro.log.LoggerMg;
import com.uro.service.sql.SQLServiceException;
import com.uro.transfer.ListParam;


public class ValDataTrans2 {
	Logger log = LoggerMg.getInstance().getLogger();
	
	public void execute(DaoService dao) {
		
		
		String a = dao.getStringValue("a");
		
		String[] aArray = a.split("[&]"); //a를 &단위로 분리
		
		String[] columnArr = aArray[0].split("|"); //첫번째 단위체 (column명)을 columnArr에 저장
		
		ListParam aa = new ListParam(columnArr);
		
		int rowIdx = 0;
		int colIdx = 0;
		
		for(String rowValue:aArray) {
			aa.createRow();
			for(String colValue:rowValue.split("[|]")){
				aa.setValue(rowIdx, columnArr[colIdx], colValue);
				colIdx++;
			}
			
			colIdx=0;
			rowIdx++;
		}
		
        if (aa != null) {
            log.info("Converted ListParam: " + aa.toString());
        } else {
            log.error("Conversion failed, ListParam is null");
        }
        
		dao.setValue("InsertUserJobTp2", aa);
		try {
			dao.sqlexe("s_insertUserJobTpHstr2", true);
		} catch (SQLServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}