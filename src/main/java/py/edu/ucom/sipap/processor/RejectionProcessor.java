package py.edu.ucom.sipap.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import py.edu.ucom.sipap.domain.ResultadoTransferencia;
import py.edu.ucom.sipap.validation.TransferValidationProcessor;

public class RejectionProcessor implements Processor {
    @Override
    public void process(Exchange exchange) {
        String id = exchange.getMessage().getHeader(
                TransactionIdProcessor.TRANSACTION_ID_HEADER, String.class);
        String reason = exchange.getMessage().getHeader(
                TransferValidationProcessor.REJECTION_HEADER, String.class);
        exchange.getMessage().setBody(ResultadoTransferencia.rechazada(id, reason));
    }
}
