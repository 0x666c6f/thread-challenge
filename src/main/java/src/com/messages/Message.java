package src.com.messages;

import java.nio.charset.Charset;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Message {
    private Integer requestId;
    private String threadName;
    private MessageSide side;
    private String ball;
    private Boolean response;
    private Timestamp timestamp;


    private static ThreadLocalRandom random = ThreadLocalRandom.current();

    public Message() {
        generateID();
    }

    public Message(String ball, MessageSide side, String threadName) {
        //TODO : modifier l'ordre des paramètres
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

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
