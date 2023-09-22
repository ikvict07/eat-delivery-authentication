package com.delivery.authentication.controller;

import com.delivery.authentication.DTO.SignInRequest;
import com.delivery.authentication.DTO.SignUpRequest;
import com.delivery.authentication.Entity.Customer;
import com.delivery.authentication.jwt.JwtCore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/authentication")
public class AuthenticationController {
    private PasswordEncoder passwordEncoder;
    private JwtCore jwtCore;
    private AuthenticationManager authenticationManager;

    @Value("${auth.customer.url}")
    private String customer_url;
    private String dbAPI = "/api/customer-db/";

    @Autowired
    private void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Autowired
    private void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Autowired
    private void setJwtCore(JwtCore jwtCore) {
        this.jwtCore = jwtCore;
    }

    @GetMapping("/login")
    private ResponseEntity<?> login(@RequestBody SignInRequest request) {

        Authentication auth;
        try {
            auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));


        } catch (BadCredentialsException bce) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Incorrect credentials");
        }
        SecurityContextHolder.getContext().setAuthentication(auth);
        String jwt = jwtCore.generateToken(auth);
        return ResponseEntity.ok().body(jwt);
    }

    @PostMapping("/register")
    private ResponseEntity<?> register(@RequestBody SignUpRequest request) {
        if (existByEmail(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This email is already used is already used");
        }

        String hashedPassword = passwordEncoder.encode(request.getPassword());
        switch (request.getRole()) {
            case "CUSTOMER" -> {
                Customer customer = new Customer();
                customer.setName(request.getName());
                customer.setPassword(hashedPassword);
                customer.setPhone(request.getPhone());
                customer.setEmail(request.getEmail());
                if (saveCustomer(customer).equals(HttpStatus.BAD_REQUEST)) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
                } else {
                    return ResponseEntity.ok("Customer was successfully registered");
                }
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

        String jwt = jwtCore.generateTokenForApi();

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + jwt);

        HttpEntity<Customer> request = new HttpEntity<>(customer, headers);

        HttpStatusCode status = restTemplate.postForEntity(
                customer_url + dbAPI + "register-customer",
                request,
                Void.class).getStatusCode();
        return status;
    }
}
