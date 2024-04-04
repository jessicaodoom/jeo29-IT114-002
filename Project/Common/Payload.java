package Project.common;

import java.io.Serializable;

public class Payload implements Serializable {
    // read https://www.baeldung.com/java-serial-version-uid
    private static final long serialVersionUID = 1L; // change this if the class changes
    //UCID:jeo29
    //Date:March 30,
    /**
     * Determines how to process the data on the receiver's side
     */
    private PayloadType payloadType;
    /**
     * Get the payload type.
     * 
     * @return
     */
    public PayloadType getPayloadType() {
        return payloadType;
    }
    /**
     * Set the payload type.
     * 
     * @param payloadType
     */
    public void setPayloadType(PayloadType payloadType) {
        this.payloadType = payloadType;
    }
    private String clientName;

    /**
     * Get the client name.
     * 
     * @return 
     */
    public String getClientName() {
        return clientName;
    }
    /**
     * Set the client name.
     * 
     * @param clientName
     */
    public void setClientName(String clientName) {
        this.clientName = clientName;
    }
    private long clientId;
    /**
     * Get the client ID.
     * 
     * @return The client ID.
     */
    public long getClientId() {
        return clientId;
    }
    /**
     * Set the client ID.
     * 
     * @param clientId 
     */
    public void setClientId(long clientId) {
        this.clientId = clientId;
    }

    /**
     * Generic text-based message
     */
    private String message;

    /**
     * Get the message.
     * 
     * @return The message.
     */
    public String getMessage() {
        return message;
    }
    /**
     * Set the message.
     * 
     * @param message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Returns a string representation of the payload.
     * 
     * @return
     */
    @Override
    public String toString() {
        return String.format("Type[%s],ClientId[%s,] ClientName[%s], Message[%s]", getPayloadType().toString(),
                getClientId(), getClientName(),
                getMessage());
    }
}
//UCID:jeo29
//Date:March 30,