package payrollBridge;
import java.math.BigDecimal;

/**
 * class is almost exactly like report row or distribution row but formatted according to output specifications
 * @author ethan horton
 *
 */
public class OutputRow {
	private String progCode, grantCode, GLCode, loanNum, fas;
	private BigDecimal debit, credit; 
	
	public OutputRow(String progCode, String grantCode, String GLCode, String loanNum, String fas, BigDecimal debit, BigDecimal credit) {
		this.progCode = progCode; 
		this.grantCode = grantCode;
		this.GLCode = GLCode; 
		this.loanNum = loanNum;
		this.fas = fas; 
		this.debit = debit; 
		this.credit = credit; 
		
	}
	
	public String getProgCode() {
		return progCode; 
	}
	public String getGrantCode() {
		return grantCode; 
	}
	public String getGLCode() {
		return GLCode; 
	}
	public String getLoanNum() {
		return loanNum; 
	}
	public String getFas() {
		return fas; 
	}
	public BigDecimal getDebit() {
		return debit; 
	}
	public BigDecimal getCredit() {
		return credit; 
	}
	
	//methods allow for debit and credit to be added (or subtracted if passed negative values) so that rounding errors can be accounted for
	public BigDecimal addDebit(BigDecimal amt) {
		debit = debit.add(amt); 
		return debit; 
	}
	public BigDecimal addCredit(BigDecimal amt) {
		credit = credit.add(amt);
		return credit;
	}
	
	@Override 
	/**
	 * string representation of output row
	 */
	public String toString() {
		return "progCode: " + progCode + ", grantCode: " + grantCode + ", GLCode: " + GLCode + ", loanNum: " + loanNum + ", fas: " + fas + ", debit: " +
					debit + ", credit: " + credit;
	}
}
