/**
 * 
 */
package eventbus;

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
    //void addSubscriberForFilteredEvents(Event event, Subscriber sub);
    
    void removeSubscriber(Subscriber sub);
}
