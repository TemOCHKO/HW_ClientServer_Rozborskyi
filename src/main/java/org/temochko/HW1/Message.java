package org.temochko.HW1;

public class Message {

    private byte uniqueIdentifier;
    private long messageNumber;
    private int commandId;

    private int userId;
    private String messageString;

    public Message(byte uniqueIdentifier, long messageNumber, int commandId, int userId, String messageString) {
        this.uniqueIdentifier = uniqueIdentifier;
        this.messageNumber = messageNumber;
        this.commandId = commandId;
        this.userId = userId;
        this.messageString = messageString;
    }

    public long getMessageNumber() {
        return messageNumber;
    }

    public void setMessageNumber(long messageNumber) {
        this.messageNumber = messageNumber;
    }

    public int getCommandId() {
        return commandId;
    }

    public void setCommandId(int commandId) {
        this.commandId = commandId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getMessageString() {
        return messageString;
    }

    public void setMessageString(String messageString) {
        this.messageString = messageString;
    }

    public byte getUniqueIdentifier() {
        return uniqueIdentifier;
    }

    public void setUniqueIdentifier(byte uniqueIdentifier) {
        this.uniqueIdentifier = uniqueIdentifier;
    }
}
