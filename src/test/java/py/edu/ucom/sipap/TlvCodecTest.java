package py.edu.ucom.sipap;

import org.junit.jupiter.api.Test;
import py.edu.ucom.sipap.tlv.QrParsingException;
import py.edu.ucom.sipap.tlv.TlvCodec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TlvCodecTest {
    @Test
    void encodesAndDecodesUsingRealValueLength() {
        String encoded = TlvCodec.encode("59", "TIENDA EJEMPLO");
        assertEquals("5914TIENDA EJEMPLO", encoded);
        assertEquals("TIENDA EJEMPLO", TlvCodec.decode(encoded).get("59"));
    }

    @Test
    void rejectsDeclaredLengthLongerThanAvailableValue() {
        assertThrows(QrParsingException.class, () -> TlvCodec.decode("0005ABC"));
    }
}
