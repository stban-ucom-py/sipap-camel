package py.edu.ucom.sipap.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import java.util.concurrent.atomic.AtomicLong;

public class TransactionIdProcessor implements Processor {
    public static final String TRANSACTION_ID_HEADER = "SipapTransactionId";
    private final AtomicLong sequence = new AtomicLong();

    @Override
    public void process(Exchange exchange) {
        if (exchange.getMessage().getHeader(TRANSACTION_ID_HEADER) == null) {
            exchange.getMessage().setHeader(TRANSACTION_ID_HEADER,
                    "TX%06d".formatted(sequence.incrementAndGet()));
        }
    }
}
