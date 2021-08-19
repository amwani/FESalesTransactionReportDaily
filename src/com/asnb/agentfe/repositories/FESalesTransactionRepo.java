package com.asnb.agentfe.repositories;

public class FESalesTransactionRepo {

    public static String getDataDaily(String transactionCode) {

        String queryString = "select * FROM AS_MonthlyTransaction \n" +
                "where Transaction_Date = ? \n" +
                "and Agent_Code like ? \n" ;

        if (transactionCode.equals("01")){
            queryString = queryString + " and ASNB_Scheme_Code in ('ASN', 'ASN2', 'ASN3', 'AASSGD', 'AASSGK', 'AASSGS', 'ASNE05', 'ASNS02', 'ASNI03')\n" ;
        }else if (transactionCode.equals("02")){
            queryString = queryString + " and ASNB_Scheme_Code in ('ASD', 'ASW', 'ASB', 'ASM', 'AS1M', 'ASB2')\n" ;
        }

        queryString =   queryString + " order by Transaction_Date \n";

        System.out.println(queryString);
        return queryString;
    }

    public static String getTotalAmount(String transactionCode) {

        String queryString = "select sum(cast(Transaction_Amount as decimal(38,2))) as totalAmount FROM AS_MonthlyTransaction \n" +
                "where Transaction_Date = ? \n" +
                "and Agent_Code like ? \n" ;

        if (transactionCode.equals("01")){
            queryString = queryString + " and ASNB_Scheme_Code in ('ASN', 'ASN2', 'ASN3', 'AASSGD', 'AASSGK', 'AASSGS', 'ASNE05', 'ASNS02', 'ASNI03')\n" ;
        }else if (transactionCode.equals("02")){
            queryString = queryString + " and ASNB_Scheme_Code in ('ASD', 'ASW', 'ASB', 'ASM', 'AS1M', 'ASB2')\n" ;
        }

        return queryString;
    }

    public static String getDataDailyFCIS() {

        String queryString = "\n" +
                "SELECT TO_CHAR(C1.DATEALLOTED, 'MM/DD/YYYY') PROCESSING_DATE,\n" +
                "\t   C1.BRANCHCODE AGENT_CODE,\n" +
                "\t   DECODE(TA1.OTHERINFO8, NULL, '', TA1.OTHERINFO8) PERSONAL_SALE_ID,\n" +
                "\t   DECODE(TA1.OTHERINFO9, NULL, '', TA1.OTHERINFO9) LEAD_GENERATOR_ID,\n" +
                "\t   C1.TRANSACTIONNUMBER TRANSACTION_REFERENCE_NUMBER,\n" +
                "\t   C1.TRANSACTIONTYPE TRANSACTION_TYPE,\n" +
                "\t   TO_CHAR(C1.TRANSACTIONDATE, 'MM/DD/YYYY') TRANSACTION_DATE,\n" +
                "\t   DECODE(C1.UNITSCONFIRMED, NULL, 0, C1.UNITSCONFIRMED) TRANSACTION_UNIT,\n" +
                "\t   DECODE(C1.GROSSAMTINFBCCY, NULL, 0, C1.GROSSAMTINFBCCY) TRANSACTION_AMOUNT,\n" +
                "\t   NVL((SELECT LPAD(SUBSTR(PARAMTEXT,3,2), 3, '0') FROM PARAMSTBL WHERE PARAMCODE = 'SERVICECHARGE' AND PARAMVALUE = C1.FUNDID), '000') SALES_CHARGE_PERCENTAGE,\n" +
                "\t   DECODE(A1.LOADAMOUNT, NULL, 0, A1.LOADAMOUNT) SALES_CHARGE_AMOUNT,\n" +
                "\t   C1.FUNDID FUND_ID,\n" +
                "\t   C1.UNITHOLDERID UH_ID,\n" +
                "\t   U1.FIRSTNAME UH_NAME,\n" +
                "\t   DECODE(U1.IDENTIFICATIONTYPE,\n" +
                "\t\t\t  'W','01','OL','02','PO','03','A','04','B','05','P','06','O','07') UH_ID_TYPE,\n" +
                "\t   U1.IDENTIFICATIONNUMBER UH_IC\n" +
                "  FROM CONSOLIDATEDTXNTBL C1,\n" +
                "\t   UNITHOLDERTBL      U1,\n" +
                "\t   TXNADDINFOTBL      TA1,\n" +
                "\t   ALLOCATIONTBL      A1\n" +
                " Where C1.TRANSACTIONNUMBER = TA1.TRANSACTIONNUMBER AND\n" +
                "\t   A1.TRANSACTIONNUMBER = C1.TRANSACTIONNUMBER AND\n" +
                "\t   A1.TRANSACTIONNUMBER = TA1.TRANSACTIONNUMBER AND\n" +
                "\t   C1.UNITHOLDERID = U1.UNITHOLDERID AND \n" +
                "\t   C1.POLICYNUMBER = 'UT' AND\n" +
                "\t   C1.ALLOTEDFLAG = 'Y' AND C1.MOD_NO > 0 AND\n" +
                "\t   C1.OLDTRANSACTIONNUMBER IS NULL AND TA1.AUTH_STAT = 'A' AND\n" +
                "\t   U1.AUTH_STAT = 'A' AND\n" +
                "\t   C1.AGENTCODE in ? AND\n" +
                "\t   (TA1.OTHERINFO8 IS NOT NULL OR TA1.OTHERINFO9 IS NOT NULL) AND\n" +
                "\t   C1.TRANSACTIONTYPE = '02' AND C1.REFTYPE IN ('02','PB')  AND\n" +
            //    "\t   to_char(C1.DATEALLOTED, 'dd/mm/yyyy') >= ? and to_char(C1.DATEALLOTED, 'dd/mm/yyyy') <= ? \n" +
                "\t   to_char(C1.DATEALLOTED, 'dd/mm/yyyy') = ? \n" +
                "\t   ORDER BY C1.BRANCHCODE, C1.FUNDID, decode(TA1.OTHERINFO8, null, '', TA1.OTHERINFO8),\n" +
                "\t\tdecode(TA1.OTHERINFO9, null, '', TA1.OTHERINFO9) ASC";

        return queryString;
    }
}
