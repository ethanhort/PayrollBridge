<<<<<<< HEAD
package payrollBridge;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList; 

/**
 * class representing the program's output stored in an array list
 * @author ethan horton
 *
 */
public class OutputTable {
	private ArrayList<OutputRow> table; 
	
	/**
	 * generic constructor initializes array list
	 */
	public OutputTable() {
		table = new ArrayList<>(); 
	}
	
	/**
	 * adds row with specified fields to table
	 * 
	 * i dont think this is going to be used but it seems like a necessary functionality
	 * @param progCode
	 * @param grantCode
	 * @param GLCode
	 * @param loanNum
	 * @param fas
	 * @param debit
	 * @param credit
	 */
	public void addRow(String progCode, String grantCode, String GLCode, String loanNum, String fas, BigDecimal debit, BigDecimal credit) {
		table.add(new OutputRow(progCode, grantCode, GLCode, loanNum, fas, debit, credit));
	}
	
	/**
	 * return specified row of output table
	 * @param i index of row to be returned
	 * @return specified row from table
	 */
	public OutputRow getRow(int i) {
		return table.get(i); 
	}
	
	/**
	 * return number of rows in table 
	 * @return number of rows in output table
	 */
	public int getSize() {
		return table.size(); 
	}
	
	/**
	 * TODO: i wish i knew what this does
	 * @param distTable table containing percentages to be multiplied with
	 * @param row report row holding debit and credit values that need percentages taken
	 */
	public void generateOutput(DistributionTable distTable, ReportRow row) {
		
		//temporary list holds values before being put into final table so they can be adjusted for rounding errors
		ArrayList<OutputRow> temp = new ArrayList<OutputRow>(); 
		
		//iterate through distribution table searching for code that matches code from given report row
		for (int i = 0; i < distTable.getSize(); i++) {
			if (distTable.getRow(i).getDistCode().equals(row.getDistCode())) {
				
				//retrieve data needed for output from distribution table and GL report
				String prog = distTable.getRow(i).getProgramNum();
				String grant = distTable.getRow(i).getGrantNum();
				String GL = row.getGLNum();
				String loanNum = distTable.getRow(i).getLoanNum();
				String fas = distTable.getRow(i).getFasNum();
				
				//multiply debit and credit by percentages in distTable and round them to 2 decimal places
				BigDecimal debit = row.getDebit().multiply(distTable.getRow(i).getPercent());
				debit = debit.setScale(2, RoundingMode.HALF_UP);
				BigDecimal credit = row.getCredit().multiply(distTable.getRow(i).getPercent());
				credit = credit.setScale(2, RoundingMode.HALF_UP); 
				temp.add(new OutputRow(prog, grant, GL, loanNum, fas, debit, credit));
			}
		}
		
		BigDecimal difference;								//rounding difference
		BigDecimal totalDebit = row.getDebit();				//original debit amount 
		BigDecimal totalCredit = row.getCredit(); 			//original credit amount
		BigDecimal debitSum = new BigDecimal(0);			//sum of debit values after being multiplied by percentages
		BigDecimal creditSum = new BigDecimal(0); 			//sum of credit values after being multiplied by percentages
		
		//sum up the percentage-multiplied values so we can make sure they sum to the original dollar amount
		for (int i = 0; i < temp.size(); i++) {
			debitSum = debitSum.add(temp.get(i).getDebit());
			creditSum = creditSum.add(temp.get(i).getCredit()); 
		}
		
		//adjust the largest value if the sum of the percentage values does not equal the original dollar amount. occurs because of rounding
		if (debitSum.compareTo(totalDebit) != 0) {
			difference = debitSum.subtract(totalDebit);
			difference = difference.multiply(new BigDecimal(-1));
			
			//finds the largest debit value in the list and adjust if for rounding error
			//ensures that we don't get negative values
			int maxIndex = findMax(temp, true); 
			temp.get(maxIndex).addDebit(difference);
			
			//if the rounding difference is more than 10 cents, alert the user with what the problem is and how it was fixed
			if (difference.abs().compareTo(new BigDecimal(.1)) >= 0) {
				//TODO: somehow notify the user of this
				System.out.println("wrong");
			}
		}
		
		//if the total of the percentage-multiplied values is not the same as the original dollar amount adjust one of the values to account for rounding 
		if (creditSum.compareTo(totalCredit) != 0) {
			difference = creditSum.subtract(totalCredit); 
			difference = difference.multiply(new BigDecimal(-1));
			
			//find the largest credit value in the list and adjust that one for rounding error
			//this allows us to ensure that we don't accidentally choose a 0 and get a negative value or something
			int maxIndex = findMax(temp, false); 
			temp.get(maxIndex).addCredit(difference); 
			
			//if the rounding difference is more than 10 cents, alert the user with what the problem is and how it was fixed
			if (difference.abs().compareTo(new BigDecimal(.1)) >= 0) {
				//TODO: somehow notify the user of this
				System.out.println("wrong");
			}
		}
		
		//recalculate the sum of debit and credit after adjusting for rounding
		debitSum = new BigDecimal(0); 
		creditSum = new BigDecimal(0); 
		for (int i = 0; i < temp.size(); i++) {
			debitSum = debitSum.add(temp.get(i).getDebit());
			creditSum = creditSum.add(temp.get(i).getCredit()); 
		}
		
		//check one last time to ensure numbers line up after editing
		if (debitSum.compareTo(totalDebit) != 0 || creditSum.compareTo(totalCredit) != 0) {
			System.out.println("something bad happened");
			//TODO: notify user of this in some way and probably close program
		}
		
		//add temp values to output table after checking to make sure they are correct
		for (int i = 0; i < temp.size(); i++) {
			table.add(temp.get(i)); 
		}
	}
	
	/**
	 * given a list of output rows and a debit/credit indicator, return the index of either the largest credit value in the list or the
	 * largest debit value
	 * @param list list of output rows to find max of 
	 * @param debit boolean value indicating whether to search debit or credit for max. true if debit false if credit
	 * @return index of max value from given list
	 */
	public int findMax(ArrayList<OutputRow> list, boolean debit) {
		int max = 0; 
		for (int i = 0; i < list.size(); i++) {
			if (debit) {
				if (list.get(i).getDebit().compareTo(list.get(max).getDebit()) > 0) {
					max = i; 
				}
			}
			else {
				if (list.get(i).getCredit().compareTo(list.get(max).getCredit()) > 0) {
					max = i; 
				}
			}
		}
		return max; 
	}
	
	public int findMaxIndex(ArrayList<Integer> list) {
		int max = list.get(0);
		for (int i = 0; i < list.size(); i++) {
			if (table.get(list.get(i)).getDebit().compareTo(table.get(max).getDebit()) > 0) {
				max = list.get(i); 
			}
		}
		return max; 
	}
	
	public ArrayList<Integer> personalRows(String grantCode, String loanNum) {
		ArrayList<Integer> list = new ArrayList<Integer>(); 
		for (int i = 0; i < table.size(); i++) {
			if (table.get(i).getGrantCode().equals(grantCode) && table.get(i).getLoanNum().equals(loanNum)) {
				list.add(i); 
			}
		}
		return list; 
	}
	
	public BigDecimal sumDebit(ArrayList<Integer> list) {
		BigDecimal sum = new BigDecimal(0);
		for (int i = 0; i < list.size(); i++) {
			sum = sum.add(table.get(list.get(i)).getDebit()); 
		}
		return sum; 
	}
	
	public BigDecimal sumCredit(ArrayList<Integer> list) {
		BigDecimal sum = new BigDecimal(0); 
		for (int i = 0; i < list.size(); i++) {
			sum = sum.add(table.get(list.get(i)).getCredit()); 
		}
		return sum; 
	}
	
	public void balanceGrant() {
		// for each grant number, for each loan number, calculate debit and credit totals
		//adjust largest debit to ensure totals balance
		ArrayList<String> grantCodes = new ArrayList<String>(); 
		ArrayList<String> loanNums = new ArrayList<String>(); 
		for (int i = 0; i < table.size(); i++) {
			if (!grantCodes.contains(table.get(i).getGrantCode())) {
				grantCodes.add(table.get(i).getGrantCode());
			}
			if (!loanNums.contains(table.get(i).getLoanNum())) {
				loanNums.add(table.get(i).getLoanNum()); 
			}
		}
		for (int i = 0; i < grantCodes.size(); i++) {
			for (int j = 0; j < loanNums.size(); j++) {
				ArrayList<Integer> personalList = personalRows(grantCodes.get(i), loanNums.get(j)); 
				BigDecimal debit = sumDebit(personalList); 
				BigDecimal credit = sumCredit(personalList); 
				if (debit.compareTo(credit) != 0) {
					int max = findMaxIndex(personalList); 
					table.get(max).addDebit(credit.subtract(debit)); 
				}
			}
		}
		
		
		
		//for testing purposes 
		//prints one person's output lines for a particular grant number 
		
//		for (int i = 0; i < table.size(); i++) {
//			if (table.get(i).getGrantCode().equals("435") && table.get(i).getLoanNum().equals("8010")) {
//				System.out.println(table.get(i));
//			}
//		}
	}
	
	/**
	 * prints output table to console in reasonable fashion
	 * used for testing purposes only 
	 */
	public void print() {
		for (int i = 0; i < table.size(); i++) {
			System.out.println(table.get(i));
		}
	}
}
=======
package payrollBridge;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList; 

/**
 * class representing the program's output stored in an array list
 * @author ethan horton
 *
 */
public class OutputTable {
	private ArrayList<OutputRow> table; 
	
	/**
	 * generic constructor initializes array list
	 */
	public OutputTable() {
		table = new ArrayList<>(); 
	}
	
	/**
	 * adds row with specified fields to table
	 * 
	 * i dont think this is going to be used but it seems like a necessary functionality
	 * @param progCode
	 * @param grantCode
	 * @param GLCode
	 * @param loanNum
	 * @param fas
	 * @param debit
	 * @param credit
	 */
	public void addRow(String progCode, String grantCode, String GLCode, String loanNum, String fas, BigDecimal debit, BigDecimal credit) {
		table.add(new OutputRow(progCode, grantCode, GLCode, loanNum, fas, debit, credit));
	}
	
	/**
	 * return specified row of output table
	 * @param i index of row to be returned
	 * @return specified row from table
	 */
	public OutputRow getRow(int i) {
		return table.get(i); 
	}
	
	/**
	 * return number of rows in table 
	 * @return number of rows in output table
	 */
	public int getSize() {
		return table.size(); 
	}
	
	/**
	 * TODO: i wish i knew what this does
	 * @param distTable table containing percentages to be multiplied with
	 * @param row report row holding debit and credit values that need percentages taken
	 */
	public void generateOutput(DistributionTable distTable, ReportRow row) {
		
		//temporary list holds values before being put into final table so they can be adjusted for rounding errors
		ArrayList<OutputRow> temp = new ArrayList<OutputRow>(); 
		
		//iterate through distribution table searching for code that matches code from given report row
		for (int i = 0; i < distTable.getSize(); i++) {
			if (distTable.getRow(i).getDistCode().equals(row.getDistCode())) {
				
				//retrieve data needed for output from distribution table and GL report
				String prog = distTable.getRow(i).getProgramNum();
				String grant = distTable.getRow(i).getGrantNum();
				String GL = row.getGLNum();
				String loanNum = distTable.getRow(i).getLoanNum();
				String fas = distTable.getRow(i).getFasNum();
				
				//multiply debit and credit by percentages in distTable and round them to 2 decimal places
				BigDecimal debit = row.getDebit().multiply(distTable.getRow(i).getPercent());
				debit = debit.setScale(2, RoundingMode.HALF_UP);
				BigDecimal credit = row.getCredit().multiply(distTable.getRow(i).getPercent());
				credit = credit.setScale(2, RoundingMode.HALF_UP); 
				temp.add(new OutputRow(prog, grant, GL, loanNum, fas, debit, credit));
			}
		}
		
		BigDecimal difference;								//rounding difference
		BigDecimal totalDebit = row.getDebit();				//original debit amount 
		BigDecimal totalCredit = row.getCredit(); 			//original credit amount
		BigDecimal debitSum = new BigDecimal(0);			//sum of debit values after being multiplied by percentages
		BigDecimal creditSum = new BigDecimal(0); 			//sum of credit values after being multiplied by percentages
		
		//sum up the percentage-multiplied values so we can make sure they sum to the original dollar amount
		for (int i = 0; i < temp.size(); i++) {
			debitSum = debitSum.add(temp.get(i).getDebit());
			creditSum = creditSum.add(temp.get(i).getCredit()); 
		}
		
		//adjust the largest value if the sum of the percentage values does not equal the original dollar amount. occurs because of rounding
		if (debitSum.compareTo(totalDebit) != 0) {
			difference = debitSum.subtract(totalDebit);
			difference = difference.multiply(new BigDecimal(-1));
			
			//finds the largest debit value in the list and adjust if for rounding error
			//ensures that we don't get negative values
			int maxIndex = findMax(temp, true); 
			temp.get(maxIndex).addDebit(difference);
			
			//if the rounding difference is more than 10 cents, alert the user with what the problem is and how it was fixed
			if (difference.abs().compareTo(new BigDecimal(.1)) >= 0) {
				//TODO: somehow notify the user of this
				System.out.println("wrong");
			}
		}
		
		//if the total of the percentage-multiplied values is not the same as the original dollar amount adjust one of the values to account for rounding 
		if (creditSum.compareTo(totalCredit) != 0) {
			difference = creditSum.subtract(totalCredit); 
			difference = difference.multiply(new BigDecimal(-1));
			
			//find the largest credit value in the list and adjust that one for rounding error
			//this allows us to ensure that we don't accidentally choose a 0 and get a negative value or something
			int maxIndex = findMax(temp, false); 
			temp.get(maxIndex).addCredit(difference); 
			
			//if the rounding difference is more than 10 cents, alert the user with what the problem is and how it was fixed
			if (difference.abs().compareTo(new BigDecimal(.1)) >= 0) {
				//TODO: somehow notify the user of this
				System.out.println("wrong");
			}
		}
		
		//recalculate the sum of debit and credit after adjusting for rounding
		debitSum = new BigDecimal(0); 
		creditSum = new BigDecimal(0); 
		for (int i = 0; i < temp.size(); i++) {
			debitSum = debitSum.add(temp.get(i).getDebit());
			creditSum = creditSum.add(temp.get(i).getCredit()); 
		}
		
		//check one last time to ensure numbers line up after editing
		if (debitSum.compareTo(totalDebit) != 0 || creditSum.compareTo(totalCredit) != 0) {
			System.out.println("something bad happened");
			//TODO: notify user of this in some way and probably close program
		}
		
		//add temp values to output table after checking to make sure they are correct
		for (int i = 0; i < temp.size(); i++) {
			table.add(temp.get(i)); 
		}
	}
	
	/**
	 * given a list of output rows and a debit/credit indicator, return the index of either the largest credit value in the list or the
	 * largest debit value
	 * @param list list of output rows to find max of 
	 * @param debit boolean value indicating whether to search debit or credit for max. true if debit false if credit
	 * @return index of max value from given list
	 */
	public int findMax(ArrayList<OutputRow> list, boolean debit) {
		int max = 0; 
		for (int i = 0; i < list.size(); i++) {
			if (debit) {
				if (list.get(i).getDebit().compareTo(list.get(max).getDebit()) > 0) {
					max = i; 
				}
			}
			else {
				if (list.get(i).getCredit().compareTo(list.get(max).getCredit()) > 0) {
					max = i; 
				}
			}
		}
		return max; 
	}
	
	public int findMaxIndex(ArrayList<Integer> list) {
		int max = list.get(0);
		for (int i = 0; i < list.size(); i++) {
			if (table.get(list.get(i)).getDebit().compareTo(table.get(max).getDebit()) > 0) {
				max = list.get(i); 
			}
		}
		return max; 
	}
	
	public ArrayList<Integer> personalRows(String grantCode, String loanNum) {
		ArrayList<Integer> list = new ArrayList<Integer>(); 
		for (int i = 0; i < table.size(); i++) {
			if (table.get(i).getGrantCode().equals(grantCode) && table.get(i).getLoanNum().equals(loanNum)) {
				list.add(i); 
			}
		}
		return list; 
	}
	
	public BigDecimal sumDebit(ArrayList<Integer> list) {
		BigDecimal sum = new BigDecimal(0);
		for (int i = 0; i < list.size(); i++) {
			sum = sum.add(table.get(list.get(i)).getDebit()); 
		}
		return sum; 
	}
	
	public BigDecimal sumCredit(ArrayList<Integer> list) {
		BigDecimal sum = new BigDecimal(0); 
		for (int i = 0; i < list.size(); i++) {
			sum = sum.add(table.get(list.get(i)).getCredit()); 
		}
		return sum; 
	}
	
	public void balanceGrant() {
		// for each grant number, for each loan number, calculate debit and credit totals
		//adjust largest debit to ensure totals balance
		ArrayList<String> grantCodes = new ArrayList<String>(); 
		ArrayList<String> loanNums = new ArrayList<String>(); 
		for (int i = 0; i < table.size(); i++) {
			if (!grantCodes.contains(table.get(i).getGrantCode())) {
				grantCodes.add(table.get(i).getGrantCode());
			}
			if (!loanNums.contains(table.get(i).getLoanNum())) {
				loanNums.add(table.get(i).getLoanNum()); 
			}
		}
		for (int i = 0; i < grantCodes.size(); i++) {
			for (int j = 0; j < loanNums.size(); j++) {
				ArrayList<Integer> personalList = personalRows(grantCodes.get(i), loanNums.get(j)); 
				BigDecimal debit = sumDebit(personalList); 
				BigDecimal credit = sumCredit(personalList); 
				if (debit.compareTo(credit) != 0) {
					int max = findMaxIndex(personalList); 
					table.get(max).addDebit(credit.subtract(debit)); 
				}
			}
		}
		
		
		
		//for testing purposes 
		//prints one person's output lines for a particular grant number 
		
//		for (int i = 0; i < table.size(); i++) {
//			if (table.get(i).getGrantCode().equals("435") && table.get(i).getLoanNum().equals("8010")) {
//				System.out.println(table.get(i));
//			}
//		}
	}
	
	/**
	 * prints output table to console in reasonable fashion
	 * used for testing purposes only 
	 */
	public void print() {
		for (int i = 0; i < table.size(); i++) {
			System.out.println(table.get(i));
		}
	}
}
>>>>>>> branch 'master' of https://github.com/ethanhort/PayrollBridge.git
