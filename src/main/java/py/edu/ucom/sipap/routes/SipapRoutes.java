package py.edu.ucom.sipap.routes;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import py.edu.ucom.sipap.processor.BankConsumerProcessor;
import py.edu.ucom.sipap.processor.DeadLetterProcessor;
import py.edu.ucom.sipap.processor.RejectionProcessor;
import py.edu.ucom.sipap.processor.TransactionIdProcessor;
import py.edu.ucom.sipap.samples.QrSamples;
import py.edu.ucom.sipap.tlv.QrParser;
import py.edu.ucom.sipap.validation.TransferValidationProcessor;

public class SipapRoutes extends RouteBuilder {
    private final boolean timersEnabled;

    public SipapRoutes(boolean timersEnabled) {
        this.timersEnabled = timersEnabled;
    }

    @Override
    public void configure() {
        ObjectMapper mapper = new ObjectMapper();

        errorHandler(deadLetterChannel("direct:dead-letter")
                .useOriginalMessage()
                .maximumRedeliveries(0)
                .onPrepareFailure(new DeadLetterProcessor()));

        if (timersEnabled) {
            configureProducers();
        }

        from("direct:sipap-in")
                .routeId("sipap-intake")
                .process(new TransactionIdProcessor())
                .wireTap("direct:audit")
                .to("direct:parse");

        from("direct:audit")
                .routeId("audit-wire-tap")
                .log(LoggingLevel.INFO, "sipap.audit",
                        "AUDITORÍA ${header.SipapTransactionId}: QR recibido ${body}");

        from("direct:parse")
                .routeId("parse-and-translate")
                .process(new QrParser())
                .marshal().json()
                .unmarshal().json(py.edu.ucom.sipap.domain.Transferencia.class)
                .to("direct:validate");

        from("direct:validate")
                .routeId("validate-and-filter")
                .process(new TransferValidationProcessor())
                .filter(header(TransferValidationProcessor.VALID_HEADER).isEqualTo(true))
                    .to("direct:route-by-bank")
                .end()
                .filter(header(TransferValidationProcessor.VALID_HEADER).isEqualTo(false))
                    .to("direct:rejected")
                .end();

        from("direct:route-by-bank")
                .routeId("content-based-bank-router")
                .choice()
                    .when(header(TransferValidationProcessor.BANK_CODE_HEADER).isEqualTo("0015"))
                        .to("direct:itau")
                    .when(header(TransferValidationProcessor.BANK_CODE_HEADER).isEqualTo("0007"))
                        .to("direct:atlas")
                    .when(header(TransferValidationProcessor.BANK_CODE_HEADER).isEqualTo("0020"))
                        .to("direct:familiar")
                    .otherwise()
                        .setHeader(TransferValidationProcessor.REJECTION_HEADER,
                                simple("Banco destino desconocido: ${header.SipapBankCode}"))
                        .to("direct:rejected")
                .end();

        from("direct:itau")
                .routeId("itau-consumer")
                .process(new BankConsumerProcessor("ITAU", mapper));

        from("direct:atlas")
                .routeId("atlas-consumer")
                .process(new BankConsumerProcessor("ATLAS", mapper));

        from("direct:familiar")
                .routeId("familiar-consumer")
                .process(new BankConsumerProcessor("FAMILIAR", mapper));

        from("direct:rejected")
                .routeId("rejected-transfer")
                .process(new RejectionProcessor())
                .log(LoggingLevel.WARN, "sipap.rejected", "${body}");

        from("direct:dead-letter")
                .routeId("dead-letter-channel")
                .log(LoggingLevel.ERROR, "sipap.dead-letter", "${body}");
    }

    private void configureProducers() {
        from("timer:itauProducer?period=5000&repeatCount=1")
                .routeId("itau-qr-producer")
                .setBody(exchange -> QrSamples.validDynamic("0015", "1234567890", 15000))
                .to("direct:sipap-in")
                .log("Resultado productor ITAU: ${body}");

        from("timer:atlasProducer?period=7000&repeatCount=1")
                .routeId("atlas-qr-producer")
                .setBody(exchange -> QrSamples.validDynamic("0007", "9876543210", 25000))
                .to("direct:sipap-in")
                .log("Resultado productor ATLAS: ${body}");

        from("timer:familiarProducer?period=9000&repeatCount=1")
                .routeId("familiar-qr-producer")
                .setBody(exchange -> QrSamples.validStatic("0020", "5555555555"))
                .to("direct:sipap-in")
                .log("Resultado productor FAMILIAR: ${body}");
    }
}
