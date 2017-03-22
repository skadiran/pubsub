/**
 * Implementation of Publisher/Subscriber with event filter capability
 */
package eventbus2;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import eventbus.Subscriber;

/**
 * 
 * EventBus keeps list of subscribers with the their events and filters.
 *
 */
class Subscription { 
	  public Subscription(Event anEvent, Filter aFilter, Subscriber aSubscriber) { 
	    this.event = anEvent; 
	    this.filter = aFilter;
	    this.subscriber = aSubscriber; 
	  }

	  public Event event; 
	  public Filter filter;
	  public Subscriber subscriber; 
	}
/**
 * Event Bus for Multithreaded environment. 
 */

class ThreadSafePub implements EventBus{

	private Set<Subscription> subscribers = Collections.newSetFromMap(new ConcurrentHashMap<Subscription, Boolean>());
	
	@Override
	public void publishEvent(Object o) {
		for (Subscription s: subscribers){
			Event e = ((Event)o);
			if (isAssignable(e, s.event)
				&& (s.filter == null || s.filter.check(e))){
				s.subscriber.process_event(o);
			}
		}
	}
	/**
	 * 
	 * @param event Event that publisher sends to its subscribers.
	 * @param s_event Event that the registered subscriber is interested in.
	 * @return returns true if the subscriber is interested in receiving this event.
	 * 
	 */
	private boolean isAssignable(Event event, Event s_event){
		if (s_event == null) { return true;}
		else if (event.getClass() == s_event.getClass()){ return true;}
		else return false;
	}

	@Override
	public void addSubscriber(Subscriber sub) {
		if (sub == null) throw  new NullPointerException("null subscriber");
		subscribers.add(new Subscription(null, null, sub));
	}

	@Override
	public void removeSubscriber(Subscriber sub) {
		if (subscribers != null && sub != null){
			subscribers.remove(sub);
		}
	}

	@Override
	public void addSubscriberForFilteredEvents(Event event, 
			Filter filter,
			Subscriber sub) {
		
		if (sub == null) throw  new NullPointerException("null subscriber");
		if (subscribers == null){
			subscribers = new HashSet<Subscription>();
		}
		subscribers.add(new Subscription(event, filter, sub));
	}
}
/**
 * 
 * A Multithreaded Subscriber which uses blocking Queue for sharing events with publisher
 *
 */
class MultiThreadSub implements Subscriber{
	private String name;
	private final BlockingQueue<Event> eventQ = new LinkedBlockingQueue<Event>();
	private Thread event_handler;
	
	public MultiThreadSub(String name, int sleeptime){
		this.name = name;
		this.event_handler = new Thread(new EventHandler(eventQ, name, sleeptime));
	}
	public void start(){
		this.event_handler.start();
	}
	/**
	 * Called by the Publisher thread
	 */
	public void process_event(Object event){
		try {
			eventQ.put((Event) event);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	private class EventHandler implements Runnable{
		private final BlockingQueue<Event> eventQ;
		private int thread_sleep_time;
		private String name;
		
		public EventHandler(BlockingQueue<Event> eventQ, String name, int sleep_time){
			this.eventQ = eventQ;
			this.thread_sleep_time = sleep_time;
			this.name = name;
		}
		
		@Override
		public void run() {
			Event event;
			try {
				while((event = eventQ.take()).filter()!=ExitEvent.EXITEVENT){  
					Thread.sleep(thread_sleep_time);
				    System.out.println(name+":"+event+": Filter="+event.filter());
				}
				System.out.println(name+"exits");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
/**
 * 
 * Producer thread definition. Sends out events.
 *
 */

class Producer implements Runnable{
	private ThreadSafePub pub;
	private String tid;
	public Producer(ThreadSafePub p, String tid){
		this.pub = p;
		this.tid = tid;
	}

	@Override
	public void run() {
		try{
			Event[] events = {
					new MarketDataL1Event(1),new MarketDataL1Event(2), 
					new MarketDataL1Event(3), new MarketDataL2Event(1),
					new MarketDataL2Event(2), new MarketDataL2Event(3)
					};
			for (int i=0;i<100;i++){
				pub.publishEvent(events[i%6]);
				Thread.sleep(50);
			}
			pub.publishEvent(new ExitEvent());
		}
		catch(InterruptedException ex){
			System.out.println("Thread interrupted.");
		}
	}
}

public class PubSub {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		ThreadSafePub mp1, mp2, mp3;
		mp1=new ThreadSafePub();
		
		MultiThreadSub c1 = new MultiThreadSub("Consumer1", 10); //sleeps 10 millisecond between each event
		MultiThreadSub c2 = new MultiThreadSub("Consumer2", 500); //sleeps 500 millisecond between each event 
		
		c1.start();
		c2.start();
		
		//Consumer #1 listens to Market Data L1 event with Close Filter.
		mp1.addSubscriberForFilteredEvents(new MarketDataL1Event(), 
				new MarketCloseFilter(), 
				c1);
		
		//Consumer #2 listens to L2 Market data with filters set to 'All'
		mp1.addSubscriberForFilteredEvents(new MarketDataL2Event(),
				new MarketAllFilter(), c2);
		
		//Register Exit Event for c1
		mp1.addSubscriberForFilteredEvents(new ExitEvent(), 
				new Filter(){public boolean check(Event e){ return true;}}, 
				c1);
		
		//Register Exit Event for c1
		mp1.addSubscriberForFilteredEvents(new ExitEvent(), 
				new Filter(){public boolean check(Event e){ return true;}}, 
				c2);

		//Starts producer thread
		Thread t1=new Thread(new Producer(mp1, "thread1_producer"));
		t1.start();
		
		try {
			t1.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Done");
	}
}

