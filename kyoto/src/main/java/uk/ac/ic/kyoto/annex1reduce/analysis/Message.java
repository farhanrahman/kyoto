package uk.ac.ic.kyoto.annex1reduce.analysis;

/**
 * Part of Testing, Do not use.
 * @author cs2309
 */
@Deprecated
public class Message{
	
	private final int quantity;
	private final float price;
	
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
	
	
}
