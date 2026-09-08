package py.edu.ucom.sipap;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;
import py.edu.ucom.sipap.domain.ResultadoTransferencia;
import py.edu.ucom.sipap.processor.TransactionIdProcessor;
import py.edu.ucom.sipap.routes.SipapRoutes;
import py.edu.ucom.sipap.samples.QrSamples;

import java.util.List;

/**
 * Ejecutor manual para generar evidencia de la aplicación, no es una prueba JUnit.
 */
public final class SipapEvidenceApplication {
    private record Scenario(String id, String name, String qr) {
    }

    private SipapEvidenceApplication() {
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "off");
        String group = args.length == 0 ? "all" : args[0].toLowerCase();
        System.out.print(execute(group));
    }

    public static String execute(String group) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        StringBuilder output = new StringBuilder();

        try (CamelContext context = new DefaultCamelContext()) {
            context.addRoutes(new SipapRoutes(false));
            context.start();
            ProducerTemplate producer = context.createProducerTemplate();

            List<Scenario> scenarios = switch (group) {
                case "valid" -> validScenarios();
                case "invalid" -> invalidScenarios();
                case "all" -> {
                    var all = new java.util.ArrayList<>(validScenarios());
                    all.addAll(invalidScenarios());
                    yield all;
                }
                default -> throw new IllegalArgumentException("Use valid, invalid o all");
            };

            output.append("============================================================\n")
                    .append(" APLICACION SIPAP + APACHE CAMEL - EVIDENCIA ")
                    .append(group.toUpperCase()).append('\n')
                    .append("============================================================\n");

            int processed = 0;
            int rejected = 0;
            for (Scenario scenario : scenarios) {
                ResultadoTransferencia result = producer.requestBodyAndHeader(
                        "direct:sipap-in",
                        scenario.qr(),
                        TransactionIdProcessor.TRANSACTION_ID_HEADER,
                        scenario.id(),
                        ResultadoTransferencia.class);

                output.append('\n')
                        .append("CASO: ").append(scenario.name()).append('\n')
                        .append("QR: ").append(scenario.qr()).append('\n')
                        .append("RESULTADO: ").append(mapper.writeValueAsString(result)).append('\n');
                if ("PROCESADA".equals(result.estado())) {
                    processed++;
                } else {
                    rejected++;
                }
            }

            output.append('\n')
                    .append("RESUMEN: ").append(processed).append(" procesadas | ")
                    .append(rejected).append(" rechazadas | ")
                    .append(scenarios.size()).append(" ejecutadas\n")
                    .append("EJECUCION FINALIZADA CORRECTAMENTE\n");
        }
        return output.toString();
    }

    private static List<Scenario> validScenarios() {
        return List.of(
                new Scenario("TX-ITAU", "Transferencia valida a ITAU",
                        QrSamples.validDynamic("0015", "1234567890", 15000)),
                new Scenario("TX-ATLAS", "Transferencia valida a ATLAS",
                        QrSamples.validDynamic("0007", "9876543210", 25000)),
                new Scenario("TX-FAMILIAR", "Transferencia valida a FAMILIAR",
                        QrSamples.validStatic("0020", "5555555555")));
    }

    private static List<Scenario> invalidScenarios() {
        return List.of(
                new Scenario("TX-BANCO", "Banco destino desconocido", QrSamples.unknownBank()),
                new Scenario("TX-CAMPO", "Campo obligatorio ausente", QrSamples.missingMandatoryField()),
                new Scenario("TX-MONTO", "Monto mayor o igual a 10.000.000",
                        QrSamples.validDynamic("0015", "1234567890", 10_000_000)),
                new Scenario("TX-CRC", "Checksum diferente de A1B2", QrSamples.invalidChecksum()),
                new Scenario("TX-TLV", "Longitud TLV incorrecta", "0005ABC"));
    }
}
