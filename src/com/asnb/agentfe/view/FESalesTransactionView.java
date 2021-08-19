package com.asnb.agentfe.view;

import com.asnb.agentfe.main.readConfigFile;
import com.asnb.agentfe.model.AS_MonthlyTransaction;
import com.asnb.agentfe.controller.FESalesTransactionController;
import com.asnb.agentfe.repositories.FESalesTransactionRepo;

import java.io.FileWriter;   // Import the FileWriter class
import java.io.IOException;  // Import the IOException class to handle errors
import java.util.ArrayList;
import org.apache.commons.lang3.StringUtils;
import java.text.DecimalFormat;
import java.util.List;


public class FESalesTransactionView {

        private static DecimalFormat df2 = new DecimalFormat("#.##");

        private static FESalesTransactionController controller;
        private static FESalesTransactionRepo repo;

        public static void view(List<AS_MonthlyTransaction> a, String yesterday){

                String totalAmount = "";

                try {

                    String output = readConfigFile.main("app.output");

                    String monthStr = "";
                    String configMonth = readConfigFile.main("app.month");
                    String configYear = readConfigFile.main("app.year");
                    String agentCodeConfig = readConfigFile.main("app.agentCode");
                    String agentCodePadded = "";
                    agentCodePadded = StringUtils.rightPad(agentCodeConfig, 4, "0") ;
                    String transactionCode = readConfigFile.main("app.transactionCode");
                    String subTransactionCode = readConfigFile.main("app.subTransactionCode");
                    String frequency = readConfigFile.main("app.frequency");

                    //------------------------
                    //GENERATE BODY CONTENT
                    //------------------------

                    totalAmount = controller.getTotalAmount(yesterday, agentCodeConfig, transactionCode);

                    FileWriter myWriter = new FileWriter(output+"/"+agentCodePadded+transactionCode
                            +subTransactionCode+frequency+yesterday+".txt");

                    //write header
                    String header = "";
                    header = "0"+yesterday+StringUtils.rightPad(Integer.toString(a.size()), 20)
                            +StringUtils.rightPad(totalAmount, 30)+StringUtils.rightPad("", 100)+"XXXEOR\n";
                    myWriter.write(header);

                    //write content
                    for(AS_MonthlyTransaction i : a) {
                        myWriter.write("1" + i.getProcessingDate() + i.getAgentCode()
                                + i.getPersonalSaleId()	+ i.getLeadGenId() + i.getTransactionRefNo()
                                + "02" + i.getTransactionDate() + i.getTransactionUnit()
                                + i.getTransactionAmount() + i.getSalesChargePercentage()
                                + i.getSalesChargeAmount() + i.getAsnbSchemeCode()
                                + i.getUhAccountNo() + i.getUhName() + i.getUhIdType()
                                + i.getUhIdNo() + i.getFundShortName()
                                + StringUtils.rightPad(" ", 85)+ "XXXEOR\n");
                    }

                    //write footer
                    String footer = "";
                    footer = "9"+StringUtils.rightPad("", 158)+"XXXEOF";
                    myWriter.write(footer);

                    myWriter.close();
                    System.out.println("\nSuccessfully wrote to the file.");

                } catch (IOException e) {
                        System.out.println("An error occurred.");
                        e.printStackTrace();
                }

        }


}
