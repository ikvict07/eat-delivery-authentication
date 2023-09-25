package com.delivery.authentication.controller;

import com.delivery.authentication.DTO.SignInRequest;
import com.delivery.authentication.DTO.SignUpRequest;
import com.delivery.authentication.Entity.Customer;
import com.delivery.authentication.jwt.JwtCore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/authentication")
public class AuthenticationController {
    private final String dbAPI = "/api/customer-db/";
    private JwtCore jwtCore;
    @Value("${auth.customer.url}")
    private String customer_url;

    @Autowired
    private void setJwtCore(JwtCore jwtCore) {
        this.jwtCore = jwtCore;
    }

    @PostMapping("/login")
    private ResponseEntity<?> login(@RequestBody SignInRequest request) {
        try {
            ResponseEntity<String> response = authenticateWithApi(request);

            if (response.getStatusCode().equals(HttpStatus.OK)) {
                String jwt = jwtCore.generateToken(request.getEmail());
                return ResponseEntity.ok().body(jwt);
            }

            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (HttpClientErrorException.BadRequest ex) {
            // Handle 400 Bad Request error and extract the error message
            String errorMessage = ex.getResponseBodyAsString();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
        }
    }


    @PostMapping("/register")
    private ResponseEntity<?> register(@RequestBody SignUpRequest request) {
        if (existByEmail(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This email is already used");
        }

        switch (request.getRole()) {
            case "CUSTOMER" -> {
                Customer customer = new Customer();
                customer.setName(request.getName());
                customer.setPassword(request.getPassword());
                customer.setPhone(request.getPhone());
                customer.setEmail(request.getEmail());
                if (saveCustomer(customer).equals(HttpStatus.BAD_REQUEST)) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
                } else {
                    return ResponseEntity.ok("Customer was successfully registered");
                }
            }
            case "COURIER" -> {
                return ResponseEntity.ok().build(); //TODO: COURIER ROLE AND OTHERS
            }
            default -> {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Unknown role");
            }
        }
    }


    private boolean existByEmail(String email) {
        RestTemplate restTemplate = new RestTemplate();
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(customer_url + dbAPI + "exists-by-email")
                .queryParam("email", email);
        ResponseEntity<Boolean> responseEntity = restTemplate.getForEntity(builder.toUriString(), Boolean.class);

        Boolean responseBody = responseEntity.getBody();

        if (responseBody == null) {
            return false;
        }

        return responseBody;
    }

    private HttpStatusCode saveCustomer(Customer customer) {
        if (existByEmail(customer.getEmail())) {
            return HttpStatus.BAD_REQUEST;
        }
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = signedHeader();

        HttpEntity<Customer> request = new HttpEntity<>(customer, headers);

        return restTemplate.postForEntity(
                customer_url + dbAPI + "register-customer",
                request,
                Void.class).getStatusCode();
    }

    private ResponseEntity<String> authenticateWithApi(SignInRequest signInRequest) {

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = signedHeader();
        HttpEntity<SignInRequest> request = new HttpEntity<>(signInRequest, headers);

        return restTemplate.postForEntity(
                customer_url + dbAPI + "login-customer",
                request,
                String.class);
    }

    private HttpHeaders signedHeader() {
        String jwt = jwtCore.generateTokenForApi();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + jwt);
        return headers;
    }

}
