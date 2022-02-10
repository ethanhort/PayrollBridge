package payrollBridge;
import java.math.BigDecimal;

/**
 * Functions almost exactly the same as the Report class but this 
 * one represents a distribution table instead
 * If I had thought about this a little beforehand, I probably 
 * could've used the same class to represent both, but I didn't do
 * that. 
 * @author ethan horton
 *
 */
public class DistributionTable {
	private DistributionRow[] table; //array containing rows
	private int nextRow; //index of next available row in table

	/**
	 * constructor for distribution table representation 
	 * @param size number of rows in distribution table
	 */
	public DistributionTable(int size) {
		table = new DistributionRow[size]; 
		nextRow = 0; 
	}

	/**
	 * create a distribution row and add it to the array 
	 * @param distCode distribution code of row
	 * @param programNum program number of row
	 * @param grantNum grant number of row
	 * @param loanNum loan number of row
	 * @param fasNum fas number of row
	 * @param percent percentage associated with row
	 */
	public void addRow(String distCode, String programNum, String grantNum, String loanNum, String fasNum, BigDecimal percent) {
		if (nextRow < table.length) {
			table[nextRow] = new DistributionRow(distCode, programNum, grantNum, loanNum, fasNum, percent); 
		}
		else {
			System.out.println("No room in Distribution Table for another row. This should not happen.");
		}
		nextRow++; 
	}

	/**
	 * return specified row from distribution table
	 * @param index row to be gotten
	 * @return row at given index
	 */
	public DistributionRow getRow(int index) {
		return table[index]; 
	}

	/**
	 * get the number of rows in the distribution table
	 * @return length of array containing table
	 */
	public int getSize() {
		return table.length; 
	}

	/**
	 * prints distribution table to console
	 */
	public void print() {
		for (int i = 0; i < table.length; i++) {
			System.out.println(table[i]);
		}
	}
}
