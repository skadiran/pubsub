/**
 * Definition of Events
 */
package eventbus2;

public interface Event{
	public int filter();
}
/**
 * 
 * Example Level1 Market Data Event published by the publisher
 *
 */
class MarketDataL1Event implements Event{
	/**
	 * Event Filters
	 */
	public static final int OPEN = 1; 
	public static final int ALL = 2; 
	public static final int CLOSE = 3;
	MarketDataL1Event(){
		this.filter=ALL;
	}
	MarketDataL1Event(int filter){
		this.filter=filter;
	}
	public int filter;
	
	@Override
	public int filter() {
		// TODO Auto-generated method stub
		return this.filter;
	}
}
/**
 * 
 * Another example of event - Level2 Market Data
 *
 */

class MarketDataL2Event implements Event{
	public static final int OPEN = 1; 
	public static final int ALL = 2; 
	public static final int CLOSE = 3;
	MarketDataL2Event(int filter){
		this.filter = filter;
	}
	MarketDataL2Event(){
		this.filter=OPEN;
	}
	public int filter;
	@Override
	public int filter() {
		return this.filter;
	}
}
/**
 * Publisher sends out Exit or End of day event
 */
class ExitEvent implements Event{
	public static final int EXITEVENT = 999;//To terminate the queue polling.
	ExitEvent(int filter){
	}
	ExitEvent(){
	}
	@Override
	public int filter() {
		return EXITEVENT; 
	}
}
