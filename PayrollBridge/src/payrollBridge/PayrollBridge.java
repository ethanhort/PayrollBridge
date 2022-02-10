<<<<<<< HEAD
package payrollBridge;
import java.io.FileInputStream;  
import java.io.FileNotFoundException;  
import java.io.IOException;  
import org.apache.poi.ss.usermodel.Cell;  
import org.apache.poi.ss.usermodel.*;  
import org.apache.poi.ss.usermodel.Sheet;  
import org.apache.poi.ss.usermodel.Workbook;  
import org.apache.poi.xssf.usermodel.XSSFWorkbook;  
import java.math.BigDecimal; 
import java.io.FileWriter; 
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.BoxLayout;
import java.awt.Color;
import java.awt.event.*;
import java.util.concurrent.TimeUnit;

/**
 * This does something
 * @author Ethan Horton
 *
 */
public class PayrollBridge {

	//constants containing the column index of specified value in spreadsheet from ADP
	//i.e. if ledger number is in first column, set GL_COL_INDEX to 0, if in second column then set to 1
	//(remember the counting starts at 0)
	public static final int GL_COL_INDEX = 0; 
	public static final int GL_DIST_COL_INDEX = 1; 
	public static final int DEBIT_COL_INDEX = 3;
	public static final int CREDIT_COL_INDEX = 4; 

	//constants containing the column index of specified value in distribution code spreadsheet
	//same rules as above apply
	public static final int DIST_COL_INDEX = 0;
	public static final int PROGRAM_COL_INDEX = 2;
	public static final int GRANT_COL_INDEX = 3;
	public static final int LOAN_COL_INDEX = 4; 
	public static final int FAS_COL_INDEX = 5; 
	public static final int PERCENT_COL_INDEX = 6; 
	
	private static String reportFilePath, distCodeFilePath; 
	private static String sessID, sessDate, sessDescription, docNum, docDate, docDescription, effectiveDate;
	private static boolean finished; 

	/**
	 * method checks if string is an integer
	 * @param num string to be parsed
	 * @return true if string contains only numeric characters (and no decimals) false otherwise
	 */
	public boolean isNumeric(String num) {
		if (num == null) {
			return false; 
		}
		try {
			Integer.parseInt(num);
		}
		catch (NumberFormatException e){
			return false; 
		}
		return true; 
	}

	/**
	 * generate a report to represent the ledger table consisting of a ledger number,
	 * a distribution code, a credit amount, and a debit amount 
	 * @param filePath path of file to be read from
	 * @return report representation of the ledger table
	 */
	public Report createReport(String filePath)  { 
		Report report = null;		//init report to null so we can return it later
		try  {  
			Workbook wb = null;	 //initialize Workbook to null
			int firstEntryIndex; //index of first entry in GL table
			int GLSize;          //number of rows in GLTable

			//reading data from a file in the form of bytes  
			FileInputStream fis = new FileInputStream(filePath);  

			//constructs an XSSFWorkbook object by buffering the whole stream into the memory  
			wb = new XSSFWorkbook(fis);  
			Sheet sheet = wb.getSheetAt(0); 


			//skip thru header to find first row in ledger table 
			//specifically searching for the first row that has a number in the first column
			int i = 0; 
			while (sheet.getRow(i) == null || !isNumeric(sheet.getRow(i).getCell(GL_COL_INDEX).getStringCellValue())) {
				i++;
			}
			firstEntryIndex = i;

			//find the final row of the GL table by finding the last row with a number in the first column
			while (sheet.getRow(i) != null && isNumeric(sheet.getRow(i).getCell(GL_COL_INDEX).getStringCellValue())) {
				i++; 
			}
			//determine the number of rows in the G/L report
			GLSize = i - firstEntryIndex; 

			//create representation of G/L report from ADP
			report = new Report(GLSize); 
			String distCode; 		//temp value to remove periods from distribution codes
			BigDecimal credit; 		//temp values to convert debit and credit to bigDecimal for precision
			BigDecimal debit; 
			for (int j = firstEntryIndex; j < i; j++) {
				distCode = sheet.getRow(j).getCell(GL_DIST_COL_INDEX).getStringCellValue().replace(".", ""); 
				
				//convert doubles to BigDecimal before creating report for precision
				debit = BigDecimal.valueOf(sheet.getRow(j).getCell(DEBIT_COL_INDEX).getNumericCellValue()); 
				credit = BigDecimal.valueOf(sheet.getRow(j).getCell(CREDIT_COL_INDEX).getNumericCellValue()); 
				report.addRow(sheet.getRow(j).getCell(GL_COL_INDEX).getStringCellValue(), distCode, debit, credit);
			}

			// close workbook and input stream because we are done with this file
			wb.close();
			fis.close();

		}  
		catch(FileNotFoundException e)  {  
			System.out.println("Cannot find file containing ledger report at specified location");;  
		}  
		catch(IOException er)  {  
			System.out.println("Something bad happened. Please try again.");;  
		}  
		return report;                
	}

	/**
	 * generate a DistributionTable to represent the distribution code table consisting of a program #s,
	 * distribution codes, grant #s, loan #s, fas #s, and percentages  
	 * @param filepath location of file to be read from
	 * @return Distribution table representing given excel file
	 */
	public DistributionTable createDistTable(String filepath) {
		DistributionTable distTable = null;		//init a distTable so we can return it later 
		int firstEntryIndex; 	//index of first entry in distTable
		int distTableSize = 0; 		//number of rows in distTable

		try {

			// reading data from file byte by byte
			FileInputStream fis = new FileInputStream(filepath);

			//xssf workbook corresponds to new excel format. use some other workbook form if file does not use 2007+ excel format
			Workbook wb = new XSSFWorkbook(fis); 
			Sheet sheet = wb.getSheetAt(0); 
			
			//skip thru header of file by finding first row with a numeric program number
			int i = 0; 
			while (sheet.getRow(i) == null || !isNumeric(sheet.getRow(i).getCell(PROGRAM_COL_INDEX).getStringCellValue())) {
				i++; 
			}
			
			//save index of first entry in distribution table
			firstEntryIndex = i; 
			
			//determine how many rows our distribution table will need by counting rows with a numeric program number
			while (sheet.getRow(i) != null) {
				if (isNumeric(sheet.getRow(i).getCell(PROGRAM_COL_INDEX).getStringCellValue())) {
					distTableSize++; 
				}
				i++; 
			}
			
			//create distribution table once we have table size
			distTable = new DistributionTable(distTableSize);
			
			//start at first row of distribution table
			i = firstEntryIndex;
			Row row;
			String percentStr; 
			BigDecimal percent; 
			
			//iterate through each line of excel file
			while (sheet.getRow(i) != null) {
				
				//if there is a program number, row should be added to distTable
				if (isNumeric(sheet.getRow(i).getCell(PROGRAM_COL_INDEX).getStringCellValue())) {
					row = sheet.getRow(i); 
					
					//remove percent symbol from string so it can be converted to a double
					percentStr = row.getCell(PERCENT_COL_INDEX).getStringCellValue().replace("%", "");
					
					//change percentage into a double
					percent = BigDecimal.valueOf(Double.parseDouble(percentStr)); 
					percent = percent.divide(new BigDecimal(100)); 
					
					//create new row in distribution table
					distTable.addRow(row.getCell(DIST_COL_INDEX).getStringCellValue().toUpperCase(), 
							row.getCell(PROGRAM_COL_INDEX).getStringCellValue(), row.getCell(GRANT_COL_INDEX).getStringCellValue(),
							row.getCell(LOAN_COL_INDEX).getStringCellValue(), row.getCell(FAS_COL_INDEX).getStringCellValue(), percent);
				}
				i++; 
			}
			wb.close();
			fis.close();
			
		}
		catch(FileNotFoundException e) {
			System.out.println("Cannot find file containing distribution codes at specified location");
		}
		catch(IOException er) {
			System.out.println("Something bad happened. Please try again.");
		}
		return distTable; 
	}

	/**
	 * iterate through purged report, generating output rows for each row in report
	 * @param distTable
	 * @param report
	 * @return output table formatted per MIP specifications but lacking user-input fields
	 */
	public OutputTable createOutputTable(DistributionTable distTable, Report report) {
		OutputTable output = new OutputTable();
		for (int i = 0; i < report.getSize(); i++) {
			output.generateOutput(distTable, report.getRow(i));
		}
		
		return output; 
	}
	
	/**
	 * main
	 * @param args cmd line args
	 */
	public static void main(String[] args) {
		PayrollBridge bridge = new PayrollBridge();   //object of the class  
		Report report; 
		DistributionTable distTable;
		OutputTable output; 
		
		//track whether user is finished inputting text fields
		finished = false; 
		
		String sessStatus = "BP";
		String sourceID = "JV";
		String reverse = "no"; 
		String entryType = "N"; 

		//create file choosers so user can browse for files
		JFileChooser reportChoose = new JFileChooser(); 
		JFileChooser distChoose = new JFileChooser();
		
		//set file choosers so user can only choose excel spreadsheets
		FileNameExtensionFilter filter = new FileNameExtensionFilter(".xlsx", "xlsx");
		reportChoose.setFileFilter(filter);
		distChoose.setFileFilter(filter);
		
		//Make UI
		
		//Create Window that Program will be displayed in
		JFrame frame = new JFrame("Payroll Bridge"); 
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(750, 750);
		
		//create panel containing all other components 
		JPanel panel = new JPanel(); 
		frame.add(panel); 
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		//panel that contains all file browsing components
		JPanel filePanel = new JPanel(); 
		filePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Select Files"));
		filePanel.setLayout(new BoxLayout(filePanel, BoxLayout.X_AXIS));
		panel.add(filePanel); 
		
		//panels for individual file buttons
		JPanel reportPanel = new JPanel(); 
		reportPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Please select file from ADP"));
		filePanel.add(reportPanel);
		JPanel distCodePanel = new JPanel(); 
		distCodePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Please select distribution code file"));
		filePanel.add(distCodePanel); 
		
		//Panel that contains all text submission components
		JPanel textPanel = new JPanel(); 
		textPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "INPUT SHOULD NOT CONTAIN COMMAS"));
		textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.PAGE_AXIS));
		panel.add(textPanel);
		
		//create panels for inputting text
		JPanel IDPanel = new JPanel(); 
		IDPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Session ID"));
		textPanel.add(IDPanel);
		JTextField IDText = new JTextField("");
		IDText.setColumns(10);
		IDPanel.add(IDText);
		
		JPanel sessDatePanel = new JPanel(); 
		sessDatePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Session Date"));
		textPanel.add(sessDatePanel);
		JTextField sessDateText = new JTextField("");
		sessDateText.setColumns(10);
		sessDatePanel.add(sessDateText);
		
		JPanel sessDescriptionPanel = new JPanel(); 
		sessDescriptionPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Session Description"));
		textPanel.add(sessDescriptionPanel);
		JTextField sessDescriptionText = new JTextField("");
		sessDescriptionText.setColumns(10);
		sessDescriptionPanel.add(sessDescriptionText);
		
		JPanel docNumPanel = new JPanel(); 
		docNumPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Document Number"));
		textPanel.add(docNumPanel);
		JTextField docNumText = new JTextField("");
		docNumText.setColumns(10);
		docNumPanel.add(docNumText);
		
		JPanel docDatePanel = new JPanel();
		docDatePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Document Date"));
		textPanel.add(docDatePanel);
		JTextField docDateText = new JTextField("");
		docDateText.setColumns(10);
		docDatePanel.add(docDateText);
		
		JPanel docDescriptionPanel = new JPanel(); 
		docDescriptionPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Document Description"));
		textPanel.add(docDescriptionPanel);
		JTextField docDescriptionText = new JTextField("");
		docDescriptionText.setColumns(10);
		docDescriptionPanel.add(docDescriptionText);
		
		JPanel effectiveDatePanel = new JPanel(); 
		effectiveDatePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Effective Date"));
		textPanel.add(effectiveDatePanel);
		JTextField effectiveDateText = new JTextField("");
		effectiveDateText.setColumns(10);
		effectiveDatePanel.add(effectiveDateText);
		
		
		//put buttons in the window to choose files 
		JButton reportButton = new JButton("Browse"); 
		JButton distCodeButton = new JButton("Browse"); 
		JLabel reportLabel = new JLabel("");
		JLabel distCodeLabel = new JLabel(""); 
		reportPanel.add(reportLabel); 
		distCodePanel.add(distCodeLabel);
		reportButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int chosen = reportChoose.showOpenDialog(null);
				if (chosen == JFileChooser.APPROVE_OPTION) {
					reportFilePath = reportChoose.getSelectedFile().getAbsolutePath();	
					reportLabel.setText(reportFilePath);
				}
				else {
					//I don't think these need to be here but I'm scared to take them out
				}
			}
		});
		reportPanel.add(reportButton);
		
		distCodeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int chosen = distChoose.showOpenDialog(null);
				if (chosen == JFileChooser.APPROVE_OPTION) {
					distCodeFilePath = distChoose.getSelectedFile().getAbsolutePath();
					distCodeLabel.setText(distCodeFilePath);
				}
				else {
					//they probably get removed at compile anyway
				}
			}
		});
		distCodePanel.add(distCodeButton);
		
		
		//final panel includes finished button and message location
		JPanel finishedPanel = new JPanel(); 
		finishedPanel.setBorder(BorderFactory.createEtchedBorder());
		panel.add(finishedPanel); 
		
		//label contains message if user doesn't enter a file
		JLabel finishedLabel = new JLabel(""); 
		finishedPanel.add(finishedLabel); 
		
		//finish button pulls data from text fields then runs programs
		JButton finishedButton = new JButton("Done");
		finishedButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sessID = IDText.getText();
				sessDate = sessDateText.getText();
				sessDescription = sessDescriptionText.getText();
				docNum = docNumText.getText();
				docDescription = docDescriptionText.getText();
				docDate = docDateText.getText();
				effectiveDate = effectiveDateText.getText();
				
				//if file hasn't been chosen, prompt user to enter one
				if (reportFilePath != null && distCodeFilePath != null) {
					finished = true; 
				}
				else {
					finishedLabel.setText("File hasn't been chosen");
				}
			}
		});
		finishedPanel.add(finishedButton); 
		
		frame.setVisible(true);
		
		//wait for user to finish inputting text fields
		while (!finished) {
			try {
				TimeUnit.SECONDS.sleep(1);  				
			}
			catch (Exception e) {
				
			}
		}
		
		//create report based on file from user  
		report = bridge.createReport(reportFilePath);
		report.removeDuplicates();
		distTable = bridge.createDistTable(distCodeFilePath);
		output = bridge.createOutputTable(distTable, report);
		output.balanceGrant(); 
	
		//Testing print statements, uncomment as needed
		//report.print(); 
		//distTable.print();
		//output.print();
		
		
		//write info to output file
		try {
			FileWriter writer = new FileWriter("output.csv"); 
			for (int i = 0; i < output.getSize(); i++) {
				if (output.getRow(i).getCredit().compareTo(output.getRow(i).getDebit()) == 0) {
					
				}
				else if (output.getRow(i).getDebit().compareTo(new BigDecimal(0.00)) == 0 || output.getRow(i).getCredit().compareTo(new BigDecimal(0.00)) == 0) {
					writer.write(sessID + ", " + sessDate + ", " + sessDescription + ", " + docNum + ", " + docDate + ", " 
							+ docDescription + ", " + effectiveDate + ", " + sessStatus + ", " + sourceID + ", " + reverse + ", " + entryType + ", "
							+ output.getRow(i).getProgCode() + ", " + output.getRow(i).getGrantCode()
							+ ", " + output.getRow(i).getGLCode() + ", " + output.getRow(i).getLoanNum() + ", "
							+ output.getRow(i).getFas() + ", " + output.getRow(i).getDebit() + ", " + output.getRow(i).getCredit() + "\n");	
				}
				else {
					System.out.println("here");
					writer.write(sessID + ", " + sessDate + ", " + sessDescription + ", " + docNum + ", " + docDate + ", " 
							+ docDescription + ", " + effectiveDate + ", " + sessStatus + ", " + sourceID + ", " + reverse + ", " + entryType + ", "
							+ output.getRow(i).getProgCode() + ", " + output.getRow(i).getGrantCode()
							+ ", " + output.getRow(i).getGLCode() + ", " + output.getRow(i).getLoanNum() + ", "
							+ output.getRow(i).getFas() + ", " + output.getRow(i).getDebit() + ", 0.00" + "\n");
					
					writer.write(sessID + ", " + sessDate + ", " + sessDescription + ", " + docNum + ", " + docDate + ", " 
							+ docDescription + ", " + effectiveDate + ", " + sessStatus + ", " + sourceID + ", " + reverse + ", " + entryType + ", "
							+ output.getRow(i).getProgCode() + ", " + output.getRow(i).getGrantCode()
							+ ", " + output.getRow(i).getGLCode() + ", " + output.getRow(i).getLoanNum() + ", "
							+ output.getRow(i).getFas() + ", 0.00" + ", " + output.getRow(i).getCredit() + "\n");
				}
			}
			writer.flush();
			writer.close();
		}
		catch (IOException e) {
			System.out.println("Problem writing to file. Please try again.");
		}
		frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
	}

}
=======
package payrollBridge;
import java.io.FileInputStream;  
import java.io.FileNotFoundException;  
import java.io.IOException;  
import org.apache.poi.ss.usermodel.Cell;  
import org.apache.poi.ss.usermodel.*;  
import org.apache.poi.ss.usermodel.Sheet;  
import org.apache.poi.ss.usermodel.Workbook;  
import org.apache.poi.xssf.usermodel.XSSFWorkbook;  
import java.math.BigDecimal; 
import java.io.FileWriter; 
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.BoxLayout;
import java.awt.Color;
import java.awt.event.*;
import java.util.concurrent.TimeUnit;

/**
 * This does something
 * @author Ethan Horton
 *
 */
public class PayrollBridge {

	//constants containing the column index of specified value in spreadsheet from ADP
	//i.e. if ledger number is in first column, set GL_COL_INDEX to 0, if in second column then set to 1
	//(remember the counting starts at 0)
	public static final int GL_COL_INDEX = 0; 
	public static final int GL_DIST_COL_INDEX = 1; 
	public static final int DEBIT_COL_INDEX = 3;
	public static final int CREDIT_COL_INDEX = 4; 

	//constants containing the column index of specified value in distribution code spreadsheet
	//same rules as above apply
	public static final int DIST_COL_INDEX = 0;
	public static final int PROGRAM_COL_INDEX = 2;
	public static final int GRANT_COL_INDEX = 3;
	public static final int LOAN_COL_INDEX = 4; 
	public static final int FAS_COL_INDEX = 5; 
	public static final int PERCENT_COL_INDEX = 6; 
	
	private static String reportFilePath, distCodeFilePath; 
	private static String sessID, sessDate, sessDescription, docNum, docDate, docDescription, effectiveDate;
	private static boolean finished; 

	/**
	 * method checks if string is an integer
	 * @param num string to be parsed
	 * @return true if string contains only numeric characters (and no decimals) false otherwise
	 */
	public boolean isNumeric(String num) {
		if (num == null) {
			return false; 
		}
		try {
			Integer.parseInt(num);
		}
		catch (NumberFormatException e){
			return false; 
		}
		return true; 
	}

	/**
	 * generate a report to represent the ledger table consisting of a ledger number,
	 * a distribution code, a credit amount, and a debit amount 
	 * @param filePath path of file to be read from
	 * @return report representation of the ledger table
	 */
	public Report createReport(String filePath)  { 
		Report report = null;		//init report to null so we can return it later
		try  {  
			Workbook wb = null;	 //initialize Workbook to null
			int firstEntryIndex; //index of first entry in GL table
			int GLSize;          //number of rows in GLTable

			//reading data from a file in the form of bytes  
			FileInputStream fis = new FileInputStream(filePath);  

			//constructs an XSSFWorkbook object by buffering the whole stream into the memory  
			wb = new XSSFWorkbook(fis);  
			Sheet sheet = wb.getSheetAt(0); 


			//skip thru header to find first row in ledger table 
			//specifically searching for the first row that has a number in the first column
			int i = 0; 
			while (sheet.getRow(i) == null || !isNumeric(sheet.getRow(i).getCell(GL_COL_INDEX).getStringCellValue())) {
				i++;
			}
			firstEntryIndex = i;

			//find the final row of the GL table by finding the last row with a number in the first column
			while (sheet.getRow(i) != null && isNumeric(sheet.getRow(i).getCell(GL_COL_INDEX).getStringCellValue())) {
				i++; 
			}
			//determine the number of rows in the G/L report
			GLSize = i - firstEntryIndex; 

			//create representation of G/L report from ADP
			report = new Report(GLSize); 
			String distCode; 		//temp value to remove periods from distribution codes
			BigDecimal credit; 		//temp values to convert debit and credit to bigDecimal for precision
			BigDecimal debit; 
			for (int j = firstEntryIndex; j < i; j++) {
				distCode = sheet.getRow(j).getCell(GL_DIST_COL_INDEX).getStringCellValue().replace(".", ""); 
				
				//convert doubles to BigDecimal before creating report for precision
				debit = BigDecimal.valueOf(sheet.getRow(j).getCell(DEBIT_COL_INDEX).getNumericCellValue()); 
				credit = BigDecimal.valueOf(sheet.getRow(j).getCell(CREDIT_COL_INDEX).getNumericCellValue()); 
				report.addRow(sheet.getRow(j).getCell(GL_COL_INDEX).getStringCellValue(), distCode, debit, credit);
			}

			// close workbook and input stream because we are done with this file
			wb.close();
			fis.close();

		}  
		catch(FileNotFoundException e)  {  
			System.out.println("Cannot find file containing ledger report at specified location");;  
		}  
		catch(IOException er)  {  
			System.out.println("Something bad happened. Please try again.");;  
		}  
		return report;                
	}

	/**
	 * generate a DistributionTable to represent the distribution code table consisting of a program #s,
	 * distribution codes, grant #s, loan #s, fas #s, and percentages  
	 * @param filepath location of file to be read from
	 * @return Distribution table representing given excel file
	 */
	public DistributionTable createDistTable(String filepath) {
		DistributionTable distTable = null;		//init a distTable so we can return it later 
		int firstEntryIndex; 	//index of first entry in distTable
		int distTableSize = 0; 		//number of rows in distTable

		try {

			// reading data from file byte by byte
			FileInputStream fis = new FileInputStream(filepath);

			//xssf workbook corresponds to new excel format. use some other workbook form if file does not use 2007+ excel format
			Workbook wb = new XSSFWorkbook(fis); 
			Sheet sheet = wb.getSheetAt(0); 
			
			//skip thru header of file by finding first row with a numeric program number
			int i = 0; 
			while (sheet.getRow(i) == null || !isNumeric(sheet.getRow(i).getCell(PROGRAM_COL_INDEX).getStringCellValue())) {
				i++; 
			}
			
			//save index of first entry in distribution table
			firstEntryIndex = i; 
			
			//determine how many rows our distribution table will need by counting rows with a numeric program number
			while (sheet.getRow(i) != null) {
				if (isNumeric(sheet.getRow(i).getCell(PROGRAM_COL_INDEX).getStringCellValue())) {
					distTableSize++; 
				}
				i++; 
			}
			
			//create distribution table once we have table size
			distTable = new DistributionTable(distTableSize);
			
			//start at first row of distribution table
			i = firstEntryIndex;
			Row row;
			String percentStr; 
			BigDecimal percent; 
			
			//iterate through each line of excel file
			while (sheet.getRow(i) != null) {
				
				//if there is a program number, row should be added to distTable
				if (isNumeric(sheet.getRow(i).getCell(PROGRAM_COL_INDEX).getStringCellValue())) {
					row = sheet.getRow(i); 
					
					//remove percent symbol from string so it can be converted to a double
					percentStr = row.getCell(PERCENT_COL_INDEX).getStringCellValue().replace("%", "");
					
					//change percentage into a double
					percent = BigDecimal.valueOf(Double.parseDouble(percentStr)); 
					percent = percent.divide(new BigDecimal(100)); 
					
					//create new row in distribution table
					distTable.addRow(row.getCell(DIST_COL_INDEX).getStringCellValue().toUpperCase(), 
							row.getCell(PROGRAM_COL_INDEX).getStringCellValue(), row.getCell(GRANT_COL_INDEX).getStringCellValue(),
							row.getCell(LOAN_COL_INDEX).getStringCellValue(), row.getCell(FAS_COL_INDEX).getStringCellValue(), percent);
				}
				i++; 
			}
			wb.close();
			fis.close();
			
		}
		catch(FileNotFoundException e) {
			System.out.println("Cannot find file containing distribution codes at specified location");
		}
		catch(IOException er) {
			System.out.println("Something bad happened. Please try again.");
		}
		return distTable; 
	}

	/**
	 * iterate through purged report, generating output rows for each row in report
	 * @param distTable
	 * @param report
	 * @return output table formatted per MIP specifications but lacking user-input fields
	 */
	public OutputTable createOutputTable(DistributionTable distTable, Report report) {
		OutputTable output = new OutputTable();
		for (int i = 0; i < report.getSize(); i++) {
			output.generateOutput(distTable, report.getRow(i));
		}
		
		return output; 
	}
	
	/**
	 * main
	 * @param args cmd line args
	 */
	public static void main(String[] args) {
		PayrollBridge bridge = new PayrollBridge();   //object of the class  
		Report report; 
		DistributionTable distTable;
		OutputTable output; 
		
		//track whether user is finished inputting text fields
		finished = false; 
		
		String sessStatus = "BP";
		String sourceID = "JV";
		String reverse = "no"; 
		String entryType = "N"; 

		//create file choosers so user can browse for files
		JFileChooser reportChoose = new JFileChooser(); 
		JFileChooser distChoose = new JFileChooser();
		
		//set file choosers so user can only choose excel spreadsheets
		FileNameExtensionFilter filter = new FileNameExtensionFilter(".xlsx", "xlsx");
		reportChoose.setFileFilter(filter);
		distChoose.setFileFilter(filter);
		
		//Make UI
		
		//Create Window that Program will be displayed in
		JFrame frame = new JFrame("Payroll Bridge"); 
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(750, 750);
		
		//create panel containing all other components 
		JPanel panel = new JPanel(); 
		frame.add(panel); 
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		//panel that contains all file browsing components
		JPanel filePanel = new JPanel(); 
		filePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Select Files"));
		filePanel.setLayout(new BoxLayout(filePanel, BoxLayout.X_AXIS));
		panel.add(filePanel); 
		
		//panels for individual file buttons
		JPanel reportPanel = new JPanel(); 
		reportPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Please select file from ADP"));
		filePanel.add(reportPanel);
		JPanel distCodePanel = new JPanel(); 
		distCodePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Please select distribution code file"));
		filePanel.add(distCodePanel); 
		
		//Panel that contains all text submission components
		JPanel textPanel = new JPanel(); 
		textPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "INPUT SHOULD NOT CONTAIN COMMAS"));
		textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.PAGE_AXIS));
		panel.add(textPanel);
		
		//create panels for inputting text
		JPanel IDPanel = new JPanel(); 
		IDPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Session ID"));
		textPanel.add(IDPanel);
		JTextField IDText = new JTextField("");
		IDText.setColumns(10);
		IDPanel.add(IDText);
		
		JPanel sessDatePanel = new JPanel(); 
		sessDatePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Session Date"));
		textPanel.add(sessDatePanel);
		JTextField sessDateText = new JTextField("");
		sessDateText.setColumns(10);
		sessDatePanel.add(sessDateText);
		
		JPanel sessDescriptionPanel = new JPanel(); 
		sessDescriptionPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Session Description"));
		textPanel.add(sessDescriptionPanel);
		JTextField sessDescriptionText = new JTextField("");
		sessDescriptionText.setColumns(10);
		sessDescriptionPanel.add(sessDescriptionText);
		
		JPanel docNumPanel = new JPanel(); 
		docNumPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Document Number"));
		textPanel.add(docNumPanel);
		JTextField docNumText = new JTextField("");
		docNumText.setColumns(10);
		docNumPanel.add(docNumText);
		
		JPanel docDatePanel = new JPanel();
		docDatePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Document Date"));
		textPanel.add(docDatePanel);
		JTextField docDateText = new JTextField("");
		docDateText.setColumns(10);
		docDatePanel.add(docDateText);
		
		JPanel docDescriptionPanel = new JPanel(); 
		docDescriptionPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Document Description"));
		textPanel.add(docDescriptionPanel);
		JTextField docDescriptionText = new JTextField("");
		docDescriptionText.setColumns(10);
		docDescriptionPanel.add(docDescriptionText);
		
		JPanel effectiveDatePanel = new JPanel(); 
		effectiveDatePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Effective Date"));
		textPanel.add(effectiveDatePanel);
		JTextField effectiveDateText = new JTextField("");
		effectiveDateText.setColumns(10);
		effectiveDatePanel.add(effectiveDateText);
		
		
		//put buttons in the window to choose files 
		JButton reportButton = new JButton("Browse"); 
		JButton distCodeButton = new JButton("Browse"); 
		JLabel reportLabel = new JLabel("");
		JLabel distCodeLabel = new JLabel(""); 
		reportPanel.add(reportLabel); 
		distCodePanel.add(distCodeLabel);
		reportButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int chosen = reportChoose.showOpenDialog(null);
				if (chosen == JFileChooser.APPROVE_OPTION) {
					reportFilePath = reportChoose.getSelectedFile().getAbsolutePath();	
					reportLabel.setText(reportFilePath);
				}
				else {
					//I don't think these need to be here but I'm scared to take them out
				}
			}
		});
		reportPanel.add(reportButton);
		
		distCodeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int chosen = distChoose.showOpenDialog(null);
				if (chosen == JFileChooser.APPROVE_OPTION) {
					distCodeFilePath = distChoose.getSelectedFile().getAbsolutePath();
					distCodeLabel.setText(distCodeFilePath);
				}
				else {
					//they probably get removed at compile anyway
				}
			}
		});
		distCodePanel.add(distCodeButton);
		
		
		//final panel includes finished button and message location
		JPanel finishedPanel = new JPanel(); 
		finishedPanel.setBorder(BorderFactory.createEtchedBorder());
		panel.add(finishedPanel); 
		
		//label contains message if user doesn't enter a file
		JLabel finishedLabel = new JLabel(""); 
		finishedPanel.add(finishedLabel); 
		
		//finish button pulls data from text fields then runs programs
		JButton finishedButton = new JButton("Done");
		finishedButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sessID = IDText.getText();
				sessDate = sessDateText.getText();
				sessDescription = sessDescriptionText.getText();
				docNum = docNumText.getText();
				docDescription = docDescriptionText.getText();
				docDate = docDateText.getText();
				effectiveDate = effectiveDateText.getText();
				
				//if file hasn't been chosen, prompt user to enter one
				if (reportFilePath != null && distCodeFilePath != null) {
					finished = true; 
				}
				else {
					finishedLabel.setText("File hasn't been chosen");
				}
			}
		});
		finishedPanel.add(finishedButton); 
		
		frame.setVisible(true);
		
		//wait for user to finish inputting text fields
		while (!finished) {
			try {
				TimeUnit.SECONDS.sleep(1);  				
			}
			catch (Exception e) {
				
			}
		}
		
		//create report based on file from user  
		report = bridge.createReport(reportFilePath);
		report.removeDuplicates();
		distTable = bridge.createDistTable(distCodeFilePath);
		output = bridge.createOutputTable(distTable, report);
		output.balanceGrant(); 
	
		//Testing print statements, uncomment as needed
		//report.print(); 
		//distTable.print();
		//output.print();
		
		
		//write info to output file
		try {
			FileWriter writer = new FileWriter("output.csv"); 
			for (int i = 0; i < output.getSize(); i++) {
				if (output.getRow(i).getCredit().compareTo(output.getRow(i).getDebit()) == 0) {
					
				}
				else if (output.getRow(i).getDebit().compareTo(new BigDecimal(0.00)) == 0 || output.getRow(i).getCredit().compareTo(new BigDecimal(0.00)) == 0) {
					writer.write(sessID + ", " + sessDate + ", " + sessDescription + ", " + docNum + ", " + docDate + ", " 
							+ docDescription + ", " + effectiveDate + ", " + sessStatus + ", " + sourceID + ", " + reverse + ", " + entryType + ", "
							+ output.getRow(i).getProgCode() + ", " + output.getRow(i).getGrantCode()
							+ ", " + output.getRow(i).getGLCode() + ", " + output.getRow(i).getLoanNum() + ", "
							+ output.getRow(i).getFas() + ", " + output.getRow(i).getDebit() + ", " + output.getRow(i).getCredit() + "\n");	
				}
				else {
					System.out.println("here");
					writer.write(sessID + ", " + sessDate + ", " + sessDescription + ", " + docNum + ", " + docDate + ", " 
							+ docDescription + ", " + effectiveDate + ", " + sessStatus + ", " + sourceID + ", " + reverse + ", " + entryType + ", "
							+ output.getRow(i).getProgCode() + ", " + output.getRow(i).getGrantCode()
							+ ", " + output.getRow(i).getGLCode() + ", " + output.getRow(i).getLoanNum() + ", "
							+ output.getRow(i).getFas() + ", " + output.getRow(i).getDebit() + ", 0.00" + "\n");
					
					writer.write(sessID + ", " + sessDate + ", " + sessDescription + ", " + docNum + ", " + docDate + ", " 
							+ docDescription + ", " + effectiveDate + ", " + sessStatus + ", " + sourceID + ", " + reverse + ", " + entryType + ", "
							+ output.getRow(i).getProgCode() + ", " + output.getRow(i).getGrantCode()
							+ ", " + output.getRow(i).getGLCode() + ", " + output.getRow(i).getLoanNum() + ", "
							+ output.getRow(i).getFas() + ", 0.00" + ", " + output.getRow(i).getCredit() + "\n");
				}
			}
			writer.flush();
			writer.close();
		}
		catch (IOException e) {
			System.out.println("Problem writing to file. Please try again.");
		}
		frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
	}

}
>>>>>>> branch 'master' of https://github.com/ethanhort/PayrollBridge.git
