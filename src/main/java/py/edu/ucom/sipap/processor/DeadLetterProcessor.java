package py.edu.ucom.sipap.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import py.edu.ucom.sipap.domain.ResultadoTransferencia;

public class DeadLetterProcessor implements Processor {
    @Override
    public void process(Exchange exchange) {
        Exception cause = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
        String id = exchange.getMessage().getHeader(
                TransactionIdProcessor.TRANSACTION_ID_HEADER, String.class);
        String reason = cause == null ? "Error no identificado" : cause.getMessage();
        exchange.getMessage().setBody(ResultadoTransferencia.rechazada(id,
                "Error de parsing o procesamiento: " + reason));
    }
}
