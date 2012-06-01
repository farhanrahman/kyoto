package uk.ac.ic.kyoto.annex1reduce.analysis;

/**
 * Part of Testing, Do not use.
 * @author cs2309
 */
@Deprecated
public class Message{
	
	protected final int quantity;
	protected final float price;
	
	public Message(int quantity, float price) {
		this.quantity = quantity;
		this.price = price;
	}

	public int getQuantity() {
		return quantity;
	}

	public float getPrice() {
		return price;
	}
	
	@Override
	public boolean equals(Object obj) {
		if ((obj instanceof Message) == false){
			return false;
		}
		
		/*boolean test1 = super.equals(obj);*/
		boolean test2 = ((Message) obj).getPrice() == price;
		boolean test3 = ((Message) obj).getQuantity() == quantity;
		
		return /*test1 &&*/ test2 && test3;
	}
	
}
