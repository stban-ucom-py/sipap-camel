package py.edu.ucom.sipap.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.ALWAYS)
public record Transferencia(
        @JsonProperty("payload_format_indicator") String payloadFormatIndicator,
        @JsonProperty("point_of_initiation_method") String pointOfInitiationMethod,
        @JsonProperty("merchant_account_information") MerchantAccountInformation merchantAccountInformation,
        @JsonProperty("merchant_category_code") String merchantCategoryCode,
        @JsonProperty("transaction_currency") String transactionCurrency,
        @JsonProperty("transaction_amount") Long transactionAmount,
        @JsonProperty("country_code") String countryCode,
        @JsonProperty("merchant_name") String merchantName,
        @JsonProperty("merchant_city") String merchantCity,
        @JsonProperty("crc") String crc) {
}
