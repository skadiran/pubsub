/**
 * 3. This version of EventBus provides additional interface to filter events. 
 * Please see the package eventbus for its simple version
 */
package eventbus2;

import eventbus.Subscriber;

/**
 * @author Shajan
 *
 */
public interface EventBus {

    // Feel free to replace Object with something more specific,
    // but be prepared to justify it
    void publishEvent(Object o);

    // How would you denote the subscriber?
    void addSubscriber(Subscriber clazz);

    // Would you allow clients to filter the events they receive? How would the interface look like?
    /**
     * 
     * @param event Type of Events subscriber is interested in receiving
     * @param filter type of filters (e.g. Market Open/Close events) that subscriber is interested in.
     * @param sub subscriber object
     */
    void addSubscriberForFilteredEvents(Event event, Filter filter, Subscriber sub);
    
    void removeSubscriber(Subscriber sub);
}
