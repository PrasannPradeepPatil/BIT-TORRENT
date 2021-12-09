package edu.ufl.cise.model;

/**
 * @author Aryan
 */
public enum MessageType {

    choke(0),
    unchoke(1),
    interested(2),
    not_interested(3),
    have(4),
    bitfield(5),
    request(6),
    piece(7),
    finish(8),
    all_finish(9);

    private int messageTypeValue;

    MessageType(int messageTypeValue) {
        this.messageTypeValue =  messageTypeValue;
    }

    public int getMessageTypeValue() {
        return messageTypeValue;
    }

    public void setMessageTypeValue(int messageTypeValue) {
        this.messageTypeValue = messageTypeValue;
    }
}
