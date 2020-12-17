package payrollBridge;
import java.math.BigDecimal;

/**
 * represents one row of the distribution table 
 * @author ethan horton
 *
 */
public class DistributionRow {

	//fields to be read from excel document
	private String distCode, programNum, grantNum, loanNum, fasNum; 
	private BigDecimal percent; 

	/**
	 * constructor initializes row with given values
	 * @param distCode distribution code of row
	 * @param programNum program number of row
	 * @param grantNum grant number of row
	 * @param loanNum loan number of row
	 * @param fasNum fas number of row
	 * @param percent percentage associated with row
	 */
	public DistributionRow(String distCode, String programNum, String grantNum, String loanNum, String fasNum, BigDecimal percent) {
		this.distCode = distCode; 
		this.programNum = programNum; 
		this.grantNum = grantNum; 
		this.loanNum = loanNum; 
		this.fasNum = fasNum;
		this.percent = percent; 
	}


	/**
	 * getter methods return specified field from row
	 * @return specified field
	 */
	public String getDistCode() {
		return distCode; 
	}
	public String getProgramNum() {
		return programNum;
	}
	public String getGrantNum() {
		return grantNum; 
	}
	public String getLoanNum() {
		return loanNum; 
	}
	public String getFasNum() {
		return fasNum; 
	}
	public BigDecimal getPercent() {
		return percent; 
	}

	/**
	 * override toString so that printed row is legible and makes sense
	 */
	@Override
	public String toString() {
		return "distCode: " + distCode + ", programNum: " + programNum + ", grantNum: " 
				+ grantNum + ", loanNum: " + loanNum + ", fasNum: " + fasNum + ", percent: " + percent; 
	}
}
