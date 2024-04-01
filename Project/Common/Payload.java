package Server; // jeo29/March31,2024: Package declaration.

import java.io.Serializable;

public class Payload implements Serializable {
    private PayloadType payloadType; // jeo29/March31,2024: Base payload type.

    public Payload(PayloadType payloadType) { // jeo29/March31,2024: Constructor.
        this.payloadType = payloadType;
    }

    public PayloadType getPayloadType() { // jeo29/March31,2024: Get payload type.
        return payloadType;
    }

    public void setPayloadType(PayloadType payloadType) { // jeo29/March31,2024: Set payload type.
        this.payloadType = payloadType;
    }
    // Common payload properties and methods
}
