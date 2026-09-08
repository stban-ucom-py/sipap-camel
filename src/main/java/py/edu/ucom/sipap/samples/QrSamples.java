package py.edu.ucom.sipap.samples;

import py.edu.ucom.sipap.tlv.TlvCodec;

public final class QrSamples {
    private QrSamples() {
    }

    public static String validDynamic(String bankCode, String accountNumber, long amount) {
        return build(bankCode, accountNumber, "12", amount, "A1B2", true);
    }

    public static String validStatic(String bankCode, String accountNumber) {
        return build(bankCode, accountNumber, "11", null, "A1B2", true);
    }

    public static String unknownBank() {
        return build("9999", "1234567890", "12", 15000L, "A1B2", true);
    }

    public static String missingMandatoryField() {
        return build("0015", "1234567890", "12", 15000L, "A1B2", false);
    }

    public static String invalidChecksum() {
        return build("0015", "1234567890", "12", 15000L, "FFFF", true);
    }

    private static String build(String bankCode, String accountNumber, String initiationMethod,
                                Long amount, String crc, boolean includeMerchantName) {
        String merchantAccount = TlvCodec.encode("00", "py.gov.bcp.sip")
                + TlvCodec.encode("01", bankCode)
                + TlvCodec.encode("02", accountNumber);

        StringBuilder qr = new StringBuilder()
                .append(TlvCodec.encode("00", "01"))
                .append(TlvCodec.encode("01", initiationMethod))
                .append(TlvCodec.encode("32", merchantAccount))
                .append(TlvCodec.encode("52", "5731"))
                .append(TlvCodec.encode("53", "600"));

        if (amount != null) {
            qr.append(TlvCodec.encode("54", String.valueOf(amount)));
        }
        qr.append(TlvCodec.encode("58", "PY"));
        if (includeMerchantName) {
            qr.append(TlvCodec.encode("59", "TIENDA EJEMPLO"));
        }
        return qr.append(TlvCodec.encode("60", "ASUNCION"))
                .append(TlvCodec.encode("63", crc))
                .toString();
    }
}
