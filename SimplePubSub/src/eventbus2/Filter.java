/**
 * Filters and their definitions. Consumers can specify set of filters 
 * for each type of events which are defined in the Events.java file 
 */
package eventbus2;

public interface Filter{
	public boolean check(Event event);
}
/**
 * 
 * Can be used for Level1 or Level2 MarketData event for example
 *
 */
class MarketOpenFilter implements Filter{
	public final int OPEN = 1;

	@Override
	public boolean check(Event event) {
		return OPEN == event.filter();
	}
}
/**
 * 
 * second example filter
 *
 */
class MarketCloseFilter implements Filter{
	public final int CLOSE = 3;

	@Override
	public boolean check(Event event) {
		return CLOSE == event.filter();
	}
}
/**
 * 
 * another filter
 *
 */
class MarketAllFilter implements Filter{
	public final int ALL = 2;

	@Override
	public boolean check(Event event) {
		return ALL == event.filter();
	}
}
