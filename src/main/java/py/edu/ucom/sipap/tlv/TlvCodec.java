package py.edu.ucom.sipap.tlv;

import java.util.LinkedHashMap;
import java.util.Map;

public final class TlvCodec {
    private TlvCodec() {
    }

    public static String encode(String tag, String value) {
        if (tag == null || !tag.matches("\\d{2}")) {
            throw new IllegalArgumentException("El tag debe contener exactamente dos dígitos");
        }
        if (value == null || value.length() > 99) {
            throw new IllegalArgumentException("El valor TLV debe tener entre 0 y 99 caracteres");
        }
        return tag + "%02d".formatted(value.length()) + value;
    }

    public static Map<String, String> decode(String input) {
        if (input == null || input.isBlank()) {
            throw new QrParsingException("La cadena QR está vacía");
        }

        Map<String, String> fields = new LinkedHashMap<>();
        int offset = 0;
        while (offset < input.length()) {
            if (input.length() - offset < 4) {
                throw new QrParsingException("Cabecera TLV incompleta en la posición " + offset);
            }

            String tag = input.substring(offset, offset + 2);
            String rawLength = input.substring(offset + 2, offset + 4);
            if (!tag.matches("\\d{2}") || !rawLength.matches("\\d{2}")) {
                throw new QrParsingException("Tag o longitud no numéricos en la posición " + offset);
            }

            int length = Integer.parseInt(rawLength);
            int valueStart = offset + 4;
            int valueEnd = valueStart + length;
            if (valueEnd > input.length()) {
                throw new QrParsingException("Longitud inválida para el tag " + tag
                        + ": declara " + length + " caracteres y la cadena terminó antes");
            }
            if (fields.containsKey(tag)) {
                throw new QrParsingException("Tag duplicado: " + tag);
            }

            fields.put(tag, input.substring(valueStart, valueEnd));
            offset = valueEnd;
        }
        return fields;
    }
}
