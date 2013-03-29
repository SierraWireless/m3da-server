package m3da.server.store;

import java.util.List;

/**
 * A dated collection of Message received from a client.
 */
public class Envelope {
	private Long receptionTime;
	private List<Message> messages;
	
	/**
	 * Create a message from its reception time and 
	 * 
	 * @param timestamp
	 * @param messages
	 */
	public Envelope(Long timestamp, List<Message> messages) {
		this.receptionTime = timestamp;
		this.messages = messages;
	}
	
	public Long getReceptionTime() {
		return receptionTime;
	}
	
	public List<Message> getMessages() {
		return messages;
	}
}
