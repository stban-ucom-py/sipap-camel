package py.edu.ucom.sipap.tlv;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import py.edu.ucom.sipap.domain.MerchantAccountInformation;
import py.edu.ucom.sipap.domain.Transferencia;

import java.util.Map;

public class QrParser implements Processor {
    @Override
    public void process(Exchange exchange) {
        String rawQr = exchange.getMessage().getBody(String.class);
        Map<String, String> topLevel = TlvCodec.decode(rawQr);

        String merchantValue = null;
        for (int tag = 32; tag <= 45; tag++) {
            String candidate = topLevel.get(String.valueOf(tag));
            if (candidate != null) {
                if (merchantValue != null) {
                    throw new QrParsingException("Se encontró más de un bloque Merchant Account Information");
                }
                merchantValue = candidate;
            }
        }

        Map<String, String> merchant = merchantValue == null
                ? Map.of()
                : TlvCodec.decode(merchantValue);

        Long amount = null;
        String rawAmount = topLevel.get("54");
        if (rawAmount != null && !rawAmount.isBlank()) {
            if (!rawAmount.matches("\\d+")) {
                throw new QrParsingException("El monto del tag 54 debe ser un entero positivo en PYG");
            }
            try {
                amount = Long.valueOf(rawAmount);
            } catch (NumberFormatException exception) {
                throw new QrParsingException("El monto del tag 54 está fuera del rango permitido");
            }
        }

        MerchantAccountInformation account = new MerchantAccountInformation(
                merchant.get("00"), merchant.get("01"), merchant.get("02"));

        Transferencia transferencia = new Transferencia(
                topLevel.get("00"),
                topLevel.get("01"),
                account,
                topLevel.get("52"),
                topLevel.get("53"),
                amount,
                topLevel.get("58"),
                topLevel.get("59"),
                topLevel.get("60"),
                topLevel.get("63"));

        exchange.getMessage().setBody(transferencia);
    }
}
