package src.com.messages;

import java.sql.Timestamp;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Object representing the messages exchanged between the threads through the different queues
 */
public class Message {
    private Integer requestId;
    private String threadName;
    private MessageSide side;
    private String ball;
    private Boolean response;
    private Timestamp timestamp;
    private static ThreadLocalRandom random = ThreadLocalRandom.current();

    /**
     * Constructor of the {@code Message} without parameter to be able to intialize it without knowing the content beforehand
     * Its ID is automatically generated when executed
     */
    public Message() {
        generateID();
    }

    /**
     * Constructor of the {@code Message} with all the required parameters
     * Its ID is automatically generated when executed
     * @param  ball
     *         {@code String} representing the targeted ball
     * @param  side
     *         {@code MessageSide} representing type of the message. It can be either {@code MessageSide.REQUEST} or {@code MessageSide.RESPONSE}
     *          @see MessageSide
     * @param  threadName
     *         {@code String} representing the name of the requesting thread
     *
     *
     * We use 3 values for response:
     *  - true : it means the request was accepted
     *  - false : it means that the request was rejected because 2 threads have the same target, so the requesting thread need to change its target
     *  - null: it means that the requested thread  doesn't have the ball we ask for
     */
    public Message(String ball, MessageSide side, String threadName) {
        generateID();
        this.threadName = threadName;
        this.side = side;
        this.ball = ball;
        this.response = null;
    }

    private void generateID() {
        this.requestId = random.nextInt(999999);
    }

    public String getBall() {
        return ball;
    }

    public void setBall(String ball) {
        this.ball = ball;
    }

    public MessageSide getSide() {
        return side;
    }

    public void setSide(MessageSide side) {
        this.side = side;
    }

    public Boolean getResponse() {
        return response;
    }

    public void setResponse(Boolean response) {
        this.response = response;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public Integer getRequestId() {
        return requestId;
    }

    public void setRequestId(Integer requestId) {
        this.requestId = requestId;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Message{" +
                "requestId=" + requestId +
                ", threadName='" + threadName + '\'' +
                ", side=" + side +
                ", ball='" + ball + '\'' +
                ", response=" + response +
                ", timestamp=" + timestamp +
                '}';
    }
}
