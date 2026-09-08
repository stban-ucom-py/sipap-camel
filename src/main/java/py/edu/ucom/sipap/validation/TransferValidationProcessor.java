package py.edu.ucom.sipap.validation;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import py.edu.ucom.sipap.domain.MerchantAccountInformation;
import py.edu.ucom.sipap.domain.Transferencia;

import java.util.LinkedHashMap;
import java.util.Map;

public class TransferValidationProcessor implements Processor {
    public static final String VALID_HEADER = "SipapValid";
    public static final String REJECTION_HEADER = "SipapRejectionReason";
    public static final String BANK_CODE_HEADER = "SipapBankCode";
    public static final String BANK_NAME_HEADER = "SipapBankName";
    public static final long MAX_EXCLUSIVE_AMOUNT = 10_000_000L;

    private static final Map<String, String> BANKS = new LinkedHashMap<>();

    static {
        BANKS.put("0015", "ITAU");
        BANKS.put("0007", "ATLAS");
        BANKS.put("0020", "FAMILIAR");
    }

    @Override
    public void process(Exchange exchange) {
        Transferencia transfer = exchange.getMessage().getBody(Transferencia.class);
        String error = validate(transfer);
        exchange.getMessage().setHeader(VALID_HEADER, error == null);
        exchange.getMessage().setHeader(REJECTION_HEADER, error);

        if (transfer != null && transfer.merchantAccountInformation() != null) {
            String bankCode = transfer.merchantAccountInformation().codigoEntidad();
            exchange.getMessage().setHeader(BANK_CODE_HEADER, bankCode);
            exchange.getMessage().setHeader(BANK_NAME_HEADER, BANKS.get(bankCode));
        }
    }

    private String validate(Transferencia transfer) {
        if (transfer == null) {
            return "No se pudo construir el modelo canónico";
        }
        if (!"01".equals(transfer.payloadFormatIndicator())) {
            return "Payload Format Indicator ausente o distinto de 01";
        }
        if (!"11".equals(transfer.pointOfInitiationMethod())
                && !"12".equals(transfer.pointOfInitiationMethod())) {
            return "Point of Initiation Method debe ser 11 o 12";
        }

        MerchantAccountInformation account = transfer.merchantAccountInformation();
        if (account == null
                || isBlank(account.globallyUniqueIdentifier())
                || isBlank(account.codigoEntidad())
                || isBlank(account.numeroCuenta())) {
            return "Merchant Account Information debe incluir los sub-tags 00, 01 y 02";
        }
        if (!"py.gov.bcp.sip".equals(account.globallyUniqueIdentifier())) {
            return "Globally Unique Identifier no reconocido";
        }
        if (!BANKS.containsKey(account.codigoEntidad())) {
            return "Banco destino desconocido: " + account.codigoEntidad();
        }
        if (isBlank(transfer.merchantCategoryCode())
                || isBlank(transfer.countryCode())
                || isBlank(transfer.merchantName())
                || isBlank(transfer.merchantCity())) {
            return "Falta uno o más campos obligatorios del comercio";
        }
        if (!"600".equals(transfer.transactionCurrency())) {
            return "La moneda debe ser 600 (PYG)";
        }
        if ("12".equals(transfer.pointOfInitiationMethod())
                && (transfer.transactionAmount() == null || transfer.transactionAmount() <= 0)) {
            return "El monto debe ser positivo para un QR dinámico";
        }
        if (transfer.transactionAmount() != null
                && transfer.transactionAmount() >= MAX_EXCLUSIVE_AMOUNT) {
            return "El monto debe ser menor a 10.000.000 PYG";
        }
        if (!"A1B2".equals(transfer.crc())) {
            return "Checksum inválido: se esperaba A1B2";
        }
        return null;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
