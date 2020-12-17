package payrollBridge;
import java.math.BigDecimal;
import java.util.ArrayList; 

/**
 * represents general ledger report as an array of report rows where 
 * each row consists of a general ledger number, a distribution code, 
 * a debit dollar amount, and a credit dollar amount
 * @author ethan horton
 *
 */
public class Report {
	private ReportRow[] report; //array containing rows
	private int nextRow; //index of next available row in report

	/**
	 * constructor for report representation
	 * @param size number of rows in report
	 */
	public Report(int size) {
		report = new ReportRow[size]; 
		nextRow = 0; 
	}

	/**
	 * creates a reportRow and adds it to the report array 
	 * @param GLNum GL number of row
	 * @param distCode distribution code of row
	 * @param debit dollar amount of debit for specified row
	 * @param credit dollar amount of credit for specified row
	 */
	public void addRow(String GLNum, String distCode, BigDecimal debit, BigDecimal credit) {
		if (nextRow < report.length) {
			report[nextRow] = new ReportRow(GLNum, distCode, debit, credit); 
			nextRow++; 
		}
		else {
			System.out.println("No room in report for another row. This should not happen.");
		}
	}

	/**
	 * 
	 * @param index index of row to be returned
	 * @return specified row
	 */
	public ReportRow getRow(int index) {
		return report[index]; 
	}


	/**
	 * return number of rows in ledger report
	 * @return number of rows in ledger report
	 */
	public int getSize() {
		return report.length; 
	}
	
	/**
	 * removes lines in report with identical distcode and GL number so that report is much smaller and easier to work with
	 * also allows output to be much smaller than it originally was
	 */
	public void removeDuplicates() {
		
		//temporary lists to hold purged data 
		ReportRow[] newReport; 
		ArrayList<ReportRow> purgedList = new ArrayList<>(); 
		
		//index of current row being examined in original report array
		int index = 1; 
		
		//index of current row in list representing purged data
		int workingIndex = 0; 
		
		//add first row of report to purged report and initialize GL and distCode values to be used in checking if rows are identical
		purgedList.add(report[workingIndex]); 
		String GLNum = report[workingIndex].getGLNum();
		String distCode = report[workingIndex].getDistCode();
		
		//iterate through entire original report
		while (index < report.length) {
			
			//if GL and distcode of this row are the same as the last row added to purged report, add debit & credit values to purged report
			if (GLNum.equals(report[index].getGLNum()) && distCode.equals(report[index].getDistCode())) {
				purgedList.get(workingIndex).addCredit(report[index].getCredit());
				purgedList.get(workingIndex).addDebit(report[index].getDebit());
				index++; 
			}
			
			//if GL or distcode values are different, add new row to purged report
			else {
				purgedList.add(report[index]); 
				workingIndex++; 
				GLNum = purgedList.get(workingIndex).getGLNum();
				distCode = purgedList.get(workingIndex).getDistCode();
				index++; 
				
			}
		}
		
		//array representing purged report; use new array bc it should be much smaller than original report
		newReport = new ReportRow[purgedList.size()]; 
		
		//copy data from the purged list to the new report
		for (int i = 0; i < newReport.length; i++) {
			newReport[i] = purgedList.get(i); 
		}
		
		//new, smaller report is now the report
		report = newReport; 
	}

	/**
	 * prints report to console in understandable fashion
	 */
	public void print() {
		for (int i = 0; i < report.length; i++) {
			System.out.println(report[i]);
		}
	}


}
