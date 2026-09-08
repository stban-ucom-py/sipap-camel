package py.edu.ucom.sipap.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.edu.ucom.sipap.domain.ResultadoTransferencia;
import py.edu.ucom.sipap.domain.Transferencia;

public class BankConsumerProcessor implements Processor {
    private static final Logger LOG = LoggerFactory.getLogger(BankConsumerProcessor.class);

    private final String bank;
    private final ObjectMapper mapper;

    public BankConsumerProcessor(String bank, ObjectMapper mapper) {
        this.bank = bank;
        this.mapper = mapper;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        Transferencia transfer = exchange.getMessage().getBody(Transferencia.class);
        String transactionId = exchange.getMessage().getHeader(
                TransactionIdProcessor.TRANSACTION_ID_HEADER, String.class);
        LOG.info("Consumidor {} recibió modelo canónico para {}:\n{}",
                bank, transactionId, mapper.writerWithDefaultPrettyPrinter().writeValueAsString(transfer));
        exchange.getMessage().setBody(ResultadoTransferencia.procesada(transactionId, bank));
    }
}
