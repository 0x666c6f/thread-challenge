package src.com.messages;

import java.nio.charset.Charset;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Message {
    private String ball;
    private MessageSide side;
    private Boolean response;
    private String threadName;
    private Integer requestId;
    private static ThreadLocalRandom random = ThreadLocalRandom.current();

    public Message() {
        generateID();
    }

    public Message(String ball, MessageSide side, String threadName) {
        generateID();
        this.threadName = threadName;
        this.ball = ball;
        this.side = side;
        this.response = null;
    }

    private void generateID() {
        requestId = random.nextInt(999999);
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

    @Override
    public String toString() {
        return "Message{" +
                "ball='" + ball + '\'' +
                ", side=" + side +
                ", response=" + response +
                ", threadName='" + threadName + '\'' +
                ", requestId=" + requestId +
                '}';
    }

    public Integer getRequestId() {
        return requestId;
    }

    public void setRequestId(Integer requestId) {
        this.requestId = requestId;
    }

}
