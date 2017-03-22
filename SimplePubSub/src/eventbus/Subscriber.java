/**
 * 
 */
package eventbus;

/**
 * Subscriber interface
 *
 */
public interface Subscriber {
	public void process_event(Object vent_message);

}
