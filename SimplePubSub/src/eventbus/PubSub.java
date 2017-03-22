/**
 * 
 * This class provides basic implementations for the following.
 * 1. A basic subscriber publisher interface
 * 2. A multi-threaded version of subscriber publisher interface
 * 
 * Please see the package eventbus2 for the 3rd implementation where 
 * subscribers can filter events
 * 
 * 
 */
package eventbus;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * 1. A Simple Publisher which implements the EventBus interface. 
 *
 */
class Pub implements EventBus {
	
	private Set<Subscriber> subscribers;

	@Override
	public void publishEvent(Object o) {
		
		for (Subscriber s: subscribers){
			s.process_event(o);
		}
	}

	@Override
	public void addSubscriber(Subscriber sub) {
		if (sub == null) throw  new NullPointerException("null subscriber");
		if (subscribers == null){
			subscribers = new HashSet<Subscriber>();
		}
		subscribers.add(sub);
	}

	@Override
	public void removeSubscriber(Subscriber sub) {
		if (subscribers != null && sub != null){
			subscribers.remove(sub);
		}
	}
}

/**
 * 
 * 1. A Simple Subscriber Implementation
 *
 */
class Sub implements Subscriber{
	
	private String name;

	public Sub(String name){
		this.name=name;
	}
	@Override
	public void process_event(Object event_message) {
		System.out.println(name+":"+event_message);
	}
}

/**
 * 2. Event Bus for Multithread environment. 
 */

class ThreadSafePub implements EventBus{

	private Set<Subscriber> subscribers = Collections.newSetFromMap(
			new ConcurrentHashMap<Subscriber, Boolean>());
	
	@Override
	public void publishEvent(Object o) {
		for (Subscriber s: subscribers){
			s.process_event(o);
		}
	}

	@Override
	public void addSubscriber(Subscriber sub) {
		if (sub == null) throw new NullPointerException("null subscriber");
		subscribers.add(sub);
	}

	@Override
	public void removeSubscriber(Subscriber sub) {
		if (subscribers != null && sub != null){
			subscribers.remove(sub);
		}
	}
	
}
/**
 * 
 * 2. A Multithreaded Subscriber which uses its own blocking Queue for receiving events from publisher
 *
 */
class MTSub implements Subscriber{
	private String name;
	private final BlockingQueue<String> eventQ = new LinkedBlockingQueue<String>();
	private Thread event_handler;
	
	public MTSub(String name, int sleeptime){
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
			eventQ.put((String) event);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	private class EventHandler implements Runnable{
		private final BlockingQueue<String> eventQ;
		private int thread_sleep_time;
		private String name;
		
		public EventHandler(BlockingQueue<String> eventQ, 
				String name, 
				int sleep_time){
			this.eventQ = eventQ;
			this.thread_sleep_time = sleep_time;
			this.name = name;
		}
		
		@Override
		public void run() {
			String event;
			try {
				while((event = (String)eventQ.take()) !="END"){ //Terminate when END event is received
					Thread.sleep(thread_sleep_time);
				    System.out.println(name+":"+event);
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
 * 2. Producer thread which can publish messages to subscribers through the EventBus 
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
			for (int i=0;i<10;i++){
				pub.publishEvent(tid+":event"+i);
				Thread.sleep(500);
			}
			pub.publishEvent("END");
		}
		catch(InterruptedException ex){
			System.out.println("Thread interrupted.");
		}
	}
}
/**
 * 
 * This class is to test the Publisher/Subscriber implementation. 
 *
 */
public class PubSub {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		//Following 6 lines below demonstrates the usage of the simple publisher/subscriber
		Sub s1 = new Sub("Sub1");
		Sub s2 = new Sub("Sub2");
		
		Pub p = new Pub();
		p.addSubscriber(s1); p.addSubscriber(s2);
		p.publishEvent(new Integer(100)); p.publishEvent("Event 2");
		p.removeSubscriber(s1);	p.removeSubscriber(s2);
		
		//Following lines demonstrates multi-threaded version of the publisher/subscriber. 
		//Second parameter to MTSub is for adjusting consumers' event processing time 

		MTSub c1 = new MTSub("Consumer1", 10);
		MTSub c2 = new MTSub("Consumer2", 1000);
		MTSub c3 = new MTSub("Consumer3", 500);
		
		ThreadSafePub mp1, mp2, mp3;
		mp1 = new ThreadSafePub();
		
		//Multiple instances of Eventbus can be started, but it is not required
		mp2 = new ThreadSafePub(); 
		mp3 = new ThreadSafePub();
		
		//Add set of consumers
		mp1.addSubscriber(c1);
		mp1.addSubscriber(c2);
		mp1.addSubscriber(c3);
		mp2.addSubscriber(c3);
		mp3.addSubscriber(c2);
		
		//start the receive queue thread
		c1.start(); c2.start(); c3.start();

		Thread t1=new Thread(new Producer(mp1, "thread1_producer"));
		Thread t2=new Thread(new Producer(mp2, "thread2_producer")); 
		Thread t3=new Thread(new Producer(mp3, "thread3_producer")); 
		
		//start producer thread
		t1.start(); t2.start(); t3.start();
		try {
			t1.join();
			t2.join();
			t3.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		System.out.println("Done");
		
	}

}

