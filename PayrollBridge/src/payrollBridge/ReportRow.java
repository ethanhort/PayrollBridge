package payrollBridge;
import java.math.BigDecimal;

/**
 * represents one row in the general ledger report 
 * @author Ethan Horton
 *
 */
public class ReportRow {
	private String GLNum; //ledger number for this row
	private String distCode; //distribution code
	private BigDecimal debit; //amount debit in USD
	private BigDecimal credit; // amount credit in USD

	/**
	 * reportRow constructor initializes row with given values 
	 * @param GLNum GL number from ADP file
	 * @param distCode distribution code from ADP
	 * @param debit    Debit amt in USD from AdP
	 * @param credit   Credit amt in USD from ADP
	 */
	public ReportRow (String GLNum, String distCode, BigDecimal debit, BigDecimal credit) {
		this.GLNum = GLNum; 
		this.distCode = distCode;
		this.debit = debit;
		this.credit = credit; 
	}

	/**
	 * getter methods return specified field from row
	 * @return specified field
	 */
	public String getGLNum() {
		return GLNum; 
	}
	public String getDistCode() {
		return distCode; 
	}
	public BigDecimal getDebit() {
		return debit; 
	}
	public BigDecimal getCredit() {
		return credit; 
	}
	
	
	/**
	 * methods allow debit or credit value to be added so that report can be consolidated
	 * @param amt
	 */
	public void addDebit(BigDecimal amt) {
		debit = debit.add(amt);
	}
	
	public void addCredit(BigDecimal amt) {
		credit = credit.add(amt); 
	}

	/**
	 * override toString such that report rows are printed in a way that makes sense
	 */
	@Override
	public String toString() {
		return "GLNum: "+ GLNum + ", distCode: " + distCode + ", debit: " + debit + ", credit: " + credit;  
	}
}
