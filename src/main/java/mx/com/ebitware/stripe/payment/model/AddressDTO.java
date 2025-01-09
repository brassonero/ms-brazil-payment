package mx.com.ebitware.stripe.payment.model;

import lombok.Data;

@Data
public class AddressDTO {
    private String street;
    private String neighborhood;
    private String postalCode;
    private String country;
    private String state;
    private String city;
}
