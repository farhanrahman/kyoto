/**
 * 
 */
package uk.ac.ic.kyoto.trade;


/**
 * @author cmd08 and farhanrahman
 *
 */
public class Offer{
	final int quantity;
	final int unitCost;
	final TradeType type;

	public static String TRADE_PROPOSAL = "Trade proposal";	

	public Offer(int quantity, int unitCost, TradeType type) {
		this.quantity = quantity;
		this.unitCost = unitCost;
		this.type = type;
	}

	public int getQuantity() {
		return quantity;
	}

	public int getUnitCost() {
		return unitCost;
	}

	public int getTotalCost() {
		return unitCost * quantity;
	}

	public TradeType getType(){
		return this.type;
	}

	@Override
	public String toString() {
		return "Trade: "+quantity+" @ "+unitCost; 
	}


	@Override
	//cs2309: Written using Effective Java (Josh Bloch) as reference
	public boolean equals(Object obj) {
		if (obj == this){
			return true;
		}

		if (!(obj instanceof Offer)){
			return false;
		}

		Offer trade = (Offer) obj;
		return (trade.quantity == this.quantity) &&
				(trade.unitCost == this.unitCost) &&
				(trade.type == this.type);
	}

	@Override
	//cs2309: Written using Effective Java (Josh Bloch) as reference
	public int hashCode() {
		int result = 42;
		result = 69 * result + this.quantity;
		result = 69 * result + this.unitCost;

		switch (this.type) {
		case BUY:
			result = 69 * result + 1;
			break;
		case SELL:
			result = 69 * result + 2;
			break;
		}

		return result;
	}

	public Offer reverse(){
		TradeType t = this.type.equals(TradeType.BUY)?TradeType.SELL:TradeType.BUY;
		return new Offer(this.quantity, this.unitCost, t);
	}

}
