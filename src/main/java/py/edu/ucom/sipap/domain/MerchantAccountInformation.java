package py.edu.ucom.sipap.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MerchantAccountInformation(
        @JsonProperty("globally_unique_identifier") String globallyUniqueIdentifier,
        @JsonProperty("codigo_entidad") String codigoEntidad,
        @JsonProperty("numero_cuenta") String numeroCuenta) {
}
