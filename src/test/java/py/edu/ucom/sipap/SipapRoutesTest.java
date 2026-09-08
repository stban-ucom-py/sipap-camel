package py.edu.ucom.sipap;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Test;
import py.edu.ucom.sipap.domain.ResultadoTransferencia;
import py.edu.ucom.sipap.processor.TransactionIdProcessor;
import py.edu.ucom.sipap.routes.SipapRoutes;
import py.edu.ucom.sipap.samples.QrSamples;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SipapRoutesTest extends CamelTestSupport {
    @Override
    protected RouteBuilder createRouteBuilder() {
        return new SipapRoutes(false);
    }

    @Test
    void processesValidItauTransfer() {
        assertProcessed(QrSamples.validDynamic("0015", "1234567890", 15000), "ITAU", "TX-ITAU");
    }

    @Test
    void processesValidAtlasTransfer() {
        assertProcessed(QrSamples.validDynamic("0007", "9876543210", 25000), "ATLAS", "TX-ATLAS");
    }

    @Test
    void processesValidFamiliarTransfer() {
        assertProcessed(QrSamples.validStatic("0020", "5555555555"), "FAMILIAR", "TX-FAMILIAR");
    }

    @Test
    void rejectsUnknownBank() {
        assertRejected(QrSamples.unknownBank(), "Banco destino desconocido", "TX-UNKNOWN");
    }

    @Test
    void rejectsMissingMandatoryField() {
        assertRejected(QrSamples.missingMandatoryField(), "campos obligatorios", "TX-MISSING");
    }

    @Test
    void rejectsAmountAtMaximumLimit() {
        assertRejected(QrSamples.validDynamic("0015", "1234567890", 10_000_000),
                "menor a 10.000.000", "TX-LIMIT");
    }

    @Test
    void rejectsInvalidChecksum() {
        assertRejected(QrSamples.invalidChecksum(), "Checksum inválido", "TX-CRC");
    }

    @Test
    void deadLetterChannelConvertsMalformedTlvToCommonResult() {
        assertRejected("0005ABC", "Error de parsing", "TX-TLV");
    }

    private void assertProcessed(String qr, String bank, String transactionId) {
        ResultadoTransferencia result = send(qr, transactionId);
        assertEquals(transactionId, result.idTransaccion());
        assertEquals("PROCESADA", result.estado());
        assertTrue(result.mensaje().contains(bank));
    }

    private void assertRejected(String qr, String expectedReason, String transactionId) {
        ResultadoTransferencia result = send(qr, transactionId);
        assertEquals(transactionId, result.idTransaccion());
        assertEquals("RECHAZADA", result.estado());
        assertTrue(result.mensaje().contains(expectedReason), result.mensaje());
    }

    private ResultadoTransferencia send(String qr, String transactionId) {
        return template.requestBodyAndHeader("direct:sipap-in", qr,
                TransactionIdProcessor.TRANSACTION_ID_HEADER,
                transactionId, ResultadoTransferencia.class);
    }
}
