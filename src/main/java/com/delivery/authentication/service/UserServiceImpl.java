package com.delivery.authentication.service;

import com.delivery.authentication.Entity.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;

@Service
public class UserServiceImpl implements UserDetailsService {
    private final WebClient webClient;
    @Value("${auth.customer.url}")
    private String customer_api;

    @Autowired
    public UserServiceImpl(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(customer_api).build();
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        HashMap<String, String> map = new HashMap<>();
        map.put("email", email);


        WebClient webClient = WebClient.create("http://localhost:8080");

        return
                webClient.post()
                        .uri("/api/customer/get-by-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(map)
                        .retrieve()
                        // ... обработка ошибок, если это необходимо
                        .bodyToMono(Customer.class)
                        .map(UserDetailsImpl::build) // convert Customer to UserDetailsImpl
                        .block();

    }
}

