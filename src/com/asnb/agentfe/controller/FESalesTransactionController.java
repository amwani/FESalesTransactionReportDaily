package com.asnb.agentfe.controller;

import com.asnb.agentfe.main.readConfigFile;
import com.asnb.agentfe.model.*;
import com.asnb.agentfe.view.FESalesTransactionView;
import com.asnb.agentfe.repositories.FESalesTransactionRepo;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.sun.deploy.security.SelectableSecurityManager;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.*;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

public class FESalesTransactionController {
    private static SessionFactory factory;
    private AS_MonthlyTransaction model;
    private FESalesTransactionView view;
    private FESalesTransactionRepo repo;


    private static DecimalFormat df2 = new DecimalFormat("#.##");

    public static void main(String[] args) {

        List a = new ArrayList();
        List<AS_MonthlyTransaction> fcisDataList = new ArrayList<AS_MonthlyTransaction>() ;

        try {

            String agentCodeConfig = readConfigFile.main("app.agentCode");
            String transactionCode = readConfigFile.main("app.transactionCode");

            //--------------------
            //TO GENERATE FILENAME
            //--------------------

            Calendar cal = Calendar.getInstance();

            cal.add(Calendar.DATE, -1);
            SimpleDateFormat dateOnly = new SimpleDateFormat("ddMMyyyy");
            String yesterday = dateOnly.format(cal.getTime());

            //for testing only set yesterday date to 01072021
            //yesterday = "01072021";

            System.out.println("\nreport date (yesterday): " + yesterday);

            //----------------------
            //GET FCIS DATA (ORACLE)
            //----------------------

            System.out.println("\nFetching data from FCIS...");
            fcisDataList =  getDataDailyFCIS(yesterday, agentCodeConfig, transactionCode);

            //----------------------------------------
            //INSERT FCIS DATA IN AS_MonthlyTrasaction
            //----------------------------------------

            System.out.println("\nInsert data into AS_MonthlyTransaction...");

            insertMonthlyTransaction(fcisDataList);

            //--------------------
            //GET FE DATA (MSSQL)
            //--------------------

            System.out.println("\nGenerating daily report from AS_MonthlyTransaction...");
            a = getDataDaily(yesterday, agentCodeConfig, transactionCode);

            //-----------------
            //WRITE IN NOTEPAD
            //-----------------

            System.out.println("\nCreating Report Text file...");
            FESalesTransactionView.view(a, yesterday);


        } catch (Exception e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

    }



    private static List<AS_MonthlyTransaction> getDataDaily(String yesterday, String agentCodeConfig, String transactionCode) {

        int j = 0;
        Connection conn = null;

        List agentFe = new ArrayList<AS_MonthlyTransaction>();
        try {

            String dbURL = readConfigFile.main("app.dbURL");
            String user = readConfigFile.main("app.user");
            String pass = readConfigFile.main("app.password");

            conn = DriverManager.getConnection(dbURL, user, pass);

            if (conn != null) {
                DatabaseMetaData dm = (DatabaseMetaData) conn.getMetaData();

                //declare the statement object
                Statement sqlStatement = conn.createStatement();

                //declare the result set
                ResultSet rs = null;

                //Build the query string, making sure to use column aliases
                String queryString =  FESalesTransactionRepo.getDataDaily(transactionCode);
                PreparedStatement preparedStatement = conn.prepareStatement(queryString);

                preparedStatement.setString(1, yesterday);
                preparedStatement.setString(2, agentCodeConfig + "%");

                rs = preparedStatement.executeQuery();

                //loop through the result set and call method to print the result set row
                while (rs.next()) {

                    String processingDate = checkData(rs.getString("Process_Date"), "Process_Date",  8, "M");
                    String agentCode = checkData(rs.getString("Agent_Code"), "Agent_Code", 12, "M");
                    String personalSaleId = checkData(rs.getString("Personal_Sale_ID"), "Personal_Sale_ID", 15, "M");
                    String leadGenId = checkData(rs.getString("Lead_Gen_ID"), "Lead_Gen_ID", 15, "M");
                    String transactionRefNo = checkData(rs.getString("Transaction_Ref_No"), "Transaction_Ref_No", 25, "O");
                    String transactionType = checkData(rs.getString("Transaction_Type"), "Transaction_Ref_No", 2, "O");
                    String transactionDate = checkData(rs.getString("Transaction_Date"), "Transaction_Date", 8, "M");
                    String transactionUnit = checkData(rs.getString("Transaction_Unit"), "Transaction_Unit", 30, "M");
                    String transactionAmount = checkData(rs.getString("Transaction_Amount"), "Transaction_Amount", 30, "M");
                    String salesChargePercentage = checkData(rs.getString("Sales_Charge_Percentage"), "Sales_Charge_Percentage",  30, "O");
                    String salesChargeAmount = checkData(rs.getString("Sales_Charge_Amount"), "Sales_Charge_Amount", 30, "O");
                    String asnbSchemeCode = checkData(rs.getString("ASNB_Scheme_Code"), "ASNB_Scheme_Code", 6, "M");
                    String uhAccountNo = checkData(rs.getString("UH_AccountNo"), "UH_AccountNo", 12, "M");
                    String uhName = checkData(rs.getString("UH_Name"), "UH_Name", 100, "O");
                    String uhIdType = checkData(rs.getString("UH_IDType"), "UH_IDType", 2, "O");
                    String uhIdNo = checkData(rs.getString("UH_IDNo"), "UH_IDNo", 15, "O");
                    String fundShortName = checkData(rs.getString("fund_short_name"), "fund_short_name", 15, "M");
                    String filler = "";

                    try {

                        agentFe.add(new AS_MonthlyTransaction(processingDate, agentCode, personalSaleId,
                                leadGenId, transactionRefNo, transactionType, transactionDate, transactionUnit,
                                transactionAmount, salesChargePercentage, salesChargeAmount,
                                asnbSchemeCode, uhAccountNo, uhName, uhIdType, uhIdNo,
                                fundShortName, filler));

                        j = j + 1;

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

                //close the result set
                rs.close();

                //close the database connection
                conn.close();
            }

        } catch (SQLException ex) {
            System.err.println("Error connecting to the database");
            ex.printStackTrace(System.err);
            System.exit(0);
        } finally {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        return agentFe;
    }

    public static String getTotalAmount(String yesterday, String agentCodeConfig, String transactionCode) {

        int j = 0;
        ArrayList<AS_MonthlyTransaction> a = new ArrayList();
        Connection conn = null;
        double sumAmount = 0.0;
        String totalAmount = "";

        try {

            String dbURL = readConfigFile.main("app.dbURL");
            String user = readConfigFile.main("app.user");
            String pass = readConfigFile.main("app.password");

            conn = DriverManager.getConnection(dbURL, user, pass);

            if (conn != null) {
                DatabaseMetaData dm = (DatabaseMetaData) conn.getMetaData();

                //declare the statement object
                Statement sqlStatement = conn.createStatement();

                //declare the result set
                ResultSet rs = null;

                //Build the query string, making sure to use column aliases
                String queryString =  FESalesTransactionRepo.getTotalAmount(transactionCode);

                PreparedStatement preparedStatement = conn.prepareStatement(queryString);

                preparedStatement.setString(1, yesterday);
                preparedStatement.setString(2, agentCodeConfig + "%");
                rs = preparedStatement.executeQuery();

                //loop through the result set and call method to print the result set row
                while (rs.next()) {
                    //printResultSetRow(rs);

                    totalAmount = rs.getString("totalAmount");

                    try {

                        if (totalAmount != null){
                            sumAmount = sumAmount + Double.parseDouble(totalAmount);
                        }else {
                            sumAmount = sumAmount;
                        }

                        j = j + 1;

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    totalAmount = df2.format(sumAmount);

                }

                //close the result set
                rs.close();

                //close the database connection
                conn.close();
            }

        } catch (SQLException ex) {
            System.err.println("Error connecting to the database");
            ex.printStackTrace(System.err);
            System.exit(0);
        } finally {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        return totalAmount;
    }

    public static List getDataDailyFCIS(String yesterday, String agentCodeConfig, String transactionCode) {

        int j = 0;

        List<AS_MonthlyTransaction> fcisDataList = new ArrayList<AS_MonthlyTransaction>() ;

        try {

            //step1 load the driver class
            Class.forName("oracle.jdbc.driver.OracleDriver");

            //step2 create  the connection object
            Connection con = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe", "system", "password");

            //step3 create the statement object
            Statement stmt = con.createStatement();

            //Build the query string, making sure to use column aliases
            String sql =  FESalesTransactionRepo.getDataDailyFCIS();

            //temp for testing only
            sql = "select * from emp where agent_code like ? and processing_date = ? ";

            PreparedStatement preparedStatement = con.prepareStatement(sql);

            preparedStatement.setString(1, agentCodeConfig + "%");
            preparedStatement.setString(2, yesterday);

            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                System.out.println(rs.getString(1) + "  " + rs.getString(2) + "  " + rs.getString(3));

                String processingDate = checkData(rs.getString("PROCESSING_DATE"), "PROCESSING_DATE",  8, "M");
                String agentCode = checkData(rs.getString("AGENT_CODE"), "AGENT_CODE", 12, "M");
                String personalSaleId = checkData(rs.getString("PERSONAL_SALE_ID"), "PERSONAL_SALE_ID", 15, "M");
                String leadGenId = checkData(rs.getString("LEAD_GENERATOR_ID"), "LEAD_GENERATOR_ID", 15, "M");
                String transactionRefNo = checkData(rs.getString("TRANSACTION_REFERENCE_NUMBER"), "TRANSACTION_REFERENCE_NUMBER", 25, "O");
                String transactionType = checkData(rs.getString("TRANSACTION_TYPE"), "TRANSACTION_TYPE", 25, "O");
                String transactionDate = checkData(rs.getString("TRANSACTION_DATE"), "TRANSACTION_DATE", 8, "M");
                String transactionUnit = checkData(rs.getString("TRANSACTION_UNIT"), "TRANSACTION_UNIT", 30, "M");
                String transactionAmount = checkData(rs.getString("TRANSACTION_AMOUNT"), "TRANSACTION_AMOUNT", 30, "M");
                String salesChargePercentage = checkData(rs.getString("SALES_CHARGE_PERCENTAGE"), "SALES_CHARGE_PERCENTAGE",  30, "O");
                String salesChargeAmount = checkData(rs.getString("SALES_CHARGE_AMOUNT"), "SALES_CHARGE_AMOUNT", 30, "O");
                //String asnbSchemeCode = checkData(rs.getString("ASNB_Scheme_Code"), "ASNB_Scheme_Code", 6, "M");
                String uhAccountNo = checkData(rs.getString("FUND_ID"), "FUND_ID", 12, "M");
                String uhName = checkData(rs.getString("UH_NAME"), "UH_NAME", 100, "O");
                String uhIdType = checkData(rs.getString("UH_ID_TYPE"), "UH_ID_TYPE", 2, "O");
                String uhIdNo = checkData(rs.getString("UH_IC"), "UH_IC", 15, "O");
                //String fundShortName = checkData(rs.getString("fund_short_name"), "fund_short_name", 15, "M");
                String filler = " ";


                try {

                    fcisDataList.add(new AS_MonthlyTransaction(processingDate, agentCode, personalSaleId,
                            leadGenId, transactionRefNo, transactionType, transactionDate,
                            transactionUnit, transactionAmount, salesChargePercentage, salesChargeAmount,
                            "", uhAccountNo, uhName, uhIdType, uhIdNo, "fundShortName", filler));

                    j = j + 1;

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            //step5 close the connection object
            con.close();

        } catch (Exception e) {
            System.out.println(e);

        }

        return fcisDataList;
    }

    public static void insertMonthlyTransaction(List<AS_MonthlyTransaction> fcisData) {

        int j = 0;
        Connection conn = null;

        List agentFe = new ArrayList<AS_MonthlyTransaction>();
        try {

            String dbURL = readConfigFile.main("app.dbURL");
            String user = readConfigFile.main("app.user");
            String pass = readConfigFile.main("app.password");

            conn = DriverManager.getConnection(dbURL, user, pass);

            if (conn != null) {
                DatabaseMetaData dm = (DatabaseMetaData) conn.getMetaData();

                //declare the statement object
                Statement sqlStatement = conn.createStatement();

                for(int i=0; i<fcisData.size(); i++){

                    sqlStatement.executeUpdate("INSERT INTO AS_MonthlyTransaction(" +
                            "Indicator," +
                            "Process_Date, Agent_Code, Personal_Sale_ID, Lead_Gen_ID, Transaction_Ref_No, Transaction_Type, Transaction_Date," +
                            "Transaction_Unit, Transaction_Amount, Sales_Charge_Percentage, Sales_Charge_Amount, ASNB_Scheme_Code, UH_AccountNo," +
                            "UH_Name, UH_IDType, UH_IDNo, fund_short_name) " +
                            "VALUES (1, " +
                            "'"+fcisData.get(i).getProcessingDate()+"', " +
                            "'"+fcisData.get(i).getAgentCode()+"', " +
                            "'"+fcisData.get(i).getPersonalSaleId()+"', " +
                            "'"+fcisData.get(i).getLeadGenId()+"', " +
                            "'"+fcisData.get(i).getTransactionRefNo()+"', " +
                            "'"+fcisData.get(i).getTransactionType()+"', " +
                            "'"+fcisData.get(i).getTransactionDate()+"', " +
                            "'"+fcisData.get(i).getTransactionUnit()+"', " +
                            "'"+fcisData.get(i).getTransactionAmount()+"', " +
                            "'"+fcisData.get(i).getSalesChargePercentage()+"', " +
                            "'"+fcisData.get(i).getSalesChargeAmount()+"', " +
                            "'"+fcisData.get(i).getAsnbSchemeCode()+"', " +
                            "'"+fcisData.get(i).getUhAccountNo()+"', " +
                            "'"+fcisData.get(i).getUhName()+"', " +
                            "'"+fcisData.get(i).getUhIdType()+"', " +
                            "'"+fcisData.get(i).getUhIdNo()+"', " +
                            "'"+fcisData.get(i).getFundShortName()+"' " +
                            ")");
                }

                conn.close();
            }

        } catch (SQLException ex) {
            System.err.println("Error connecting to the database");
            ex.printStackTrace(System.err);
            System.exit(0);
        } finally {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

    }

    public static String checkData(String data, String column,  int length, String mandatory){

        //check data length must <= data length in SDD
        if(data != null){
            if(data.length() > length) {
                System.out.println("ERROR\nInvalid column length: "+column+ ", \ndata: " + data + ", \nlength: "+data.length());
                System.exit(0);
            }

            //should be mandatory but no value provided
            if(mandatory.equals("M") && data.length() == 0){
                System.out.println("ERROR\nMissing mandatory field: "+column);
                System.exit(0);
            }
        }

        return data;
    }

}
