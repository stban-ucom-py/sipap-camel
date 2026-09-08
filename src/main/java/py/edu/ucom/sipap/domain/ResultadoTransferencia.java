package py.edu.ucom.sipap.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ResultadoTransferencia(
        @JsonProperty("id_transaccion") String idTransaccion,
        @JsonProperty("estado") String estado,
        @JsonProperty("mensaje") String mensaje) {

    public static ResultadoTransferencia procesada(String id, String banco) {
        return new ResultadoTransferencia(id, "PROCESADA",
                "Transferencia procesada exitosamente por " + banco);
    }

    public static ResultadoTransferencia rechazada(String id, String motivo) {
        return new ResultadoTransferencia(id, "RECHAZADA", motivo);
    }
}
