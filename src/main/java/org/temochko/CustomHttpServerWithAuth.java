package org.temochko;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.*;
import org.temochko.DTOs.AuthResponseDto;
import org.temochko.DTOs.CredentialsDTO;
import org.temochko.DTOs.ProductCreateDto;
import org.temochko.DTOs.ProductUpdateDto;
import org.temochko.Models.Product;
import org.temochko.Models.ProductCriteria;
import org.temochko.Repositories.SqlLiteProductRepository;
import org.temochko.Services.IProductService;
import org.temochko.Services.ProductService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

public class CustomHttpServerWithAuth {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final IProductService productService;
    private final HttpServer server;

    public CustomHttpServerWithAuth(IProductService productService) throws IOException {
        this.server = HttpServer.create(new InetSocketAddress(8080), 0);
        this.productService = productService;

        createEndpoints();
    }

    public static void main(String[] args) throws IOException {
        SqlLiteProductRepository sqlLiteProductRepository = new SqlLiteProductRepository("products.db");
        IProductService productService = new ProductService(sqlLiteProductRepository);

        CustomHttpServerWithAuth customHttpServerWithAuth = new CustomHttpServerWithAuth(productService);

       customHttpServerWithAuth.startServer();
    }

    private void startServer() {
        server.start();
    }

    private void createEndpoints() {
        /*server.createContext("/basic/", exchange -> {
            String responseBody = """
                {
                  "id": 1,
                  "firstName": "John",
                  "lastName": "Smith"
                }
                """;

            exchange.getPrincipal();

            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, responseBody.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBody.getBytes());
            }
        });

        basicContext.setAuthenticator(new BasicAuthenticator());*/

        server.createContext("/login", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                System.out.printf("Request to %s %s%n", exchange.getRequestMethod(), exchange.getRequestURI());
                System.out.println("Headers:");
                exchange.getRequestHeaders().forEach((header, value) -> System.out.printf("  %s: %s%n", header, value));

                CredentialsDTO credentialsDTO = objectMapper.readValue(exchange.getRequestBody(), CredentialsDTO.class);

                System.out.println(credentialsDTO);

                try {
                    Algorithm algorithm = Algorithm.HMAC256("secret".getBytes());
                    String token = JWT.create()
                            .withSubject(credentialsDTO.getUsername())
                            .withExpiresAt(Instant.now().plusSeconds(240))
                            .withClaim("role", "admin")
                            .withClaim("id", 1)
                            .sign(algorithm);


                    AuthResponseDto responseDto = new AuthResponseDto(token);
                    byte[] responseBody = objectMapper.writeValueAsBytes(responseDto);

                    exchange.getResponseHeaders().add("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, responseBody.length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(responseBody);
                    }
                } catch (JWTCreationException exception){
                    // Invalid Signing configuration / Couldn't convert Claims.
                }

            }
        });

        HttpContext productsRestContext = server.createContext("/products/", exchange -> {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            String idParam = path.replace("/products/", "").trim();

            try {
                switch (method) {
                    case "GET":
                        if (idParam.isEmpty()) {
                            respondWith(exchange, 400, "Missing product Id");
                            break;
                        }
                        int getValidId = Integer.parseInt(idParam);
                        var product = productService.getProductById(getValidId);
                        if (product == null) {
                            respondWith(exchange, 404, "Product not found");
                        } else {
                            respondWith(exchange, 200, objectMapper.writeValueAsString(product));
                        }
                        break;

                    case "PUT":
                        try (InputStream is = exchange.getRequestBody()) {

                            ProductCreateDto createDto = objectMapper.readValue(is, ProductCreateDto.class);

                            if (productService.nameExists(createDto.getName())) {
                                respondWith(exchange, 400, "Product with this name already exists");
                            }

                            int newId = productService.createProduct(createDto);
                            respondWith(exchange, 201, "Product created successfully");
                        } catch (IllegalArgumentException e) {
                            respondWith(exchange, 400, "error");
                        }
                        break;

                    case "POST":
                        if (idParam.isEmpty()) {
                            respondWith(exchange, 400, "Missing product Id");
                            break;
                        }

                        int postValidId = Integer.parseInt(idParam);

                        try (InputStream is = exchange.getRequestBody()) {
                            ProductCreateDto bodyData = objectMapper.readValue(is, ProductCreateDto.class);
                            ProductUpdateDto updateDto = new ProductUpdateDto(postValidId, bodyData.getName(), bodyData.getPrice(), bodyData.getQuantity());
                            boolean updated = productService.updateProduct(updateDto);

                            if (updated)
                                respondWith(exchange, 200, "Product updated successfully");
                            else
                                respondWith(exchange, 404, "Product not found");

                        } catch (IllegalArgumentException e) {
                            respondWith(exchange, 400, e.getMessage().trim());
                        }
                        break;

                    case "DELETE":
                        if (idParam.isEmpty()) {
                            respondWith(exchange, 400, "Missing product Id");
                            break;
                        }
                        int deleteValidId = Integer.parseInt(idParam);
                        boolean deleted = productService.deleteProductById(deleteValidId);
                        respondWith(exchange, deleted ? 204 : 404, "");
                        break;

                    default:
                        exchange.sendResponseHeaders(405, -1);
                }
            } catch (NumberFormatException e) {
                respondWith(exchange, 400, "Invalid Id");
            } catch (Exception e) {
                respondWith(exchange, 500, "Server Error");
            }

        });

        productsRestContext.setAuthenticator(new BasicAuthenticator());
    }

private void respondWith(HttpExchange exchange, int status, String responseBody) throws IOException {
    byte[] bytes = responseBody.getBytes();

    exchange.getResponseHeaders().add("Content-Type", "application/json");
    exchange.sendResponseHeaders(status, bytes.length);
    try (OutputStream os = exchange.getResponseBody()) {
        os.write(bytes);
    }
}

    private static class BasicAuthenticator extends Authenticator {
        @Override
        public Result authenticate(HttpExchange exch) {
            List<String> values = exch.getRequestHeaders().get("Authorization");
            // header doesn't exists
            if (values == null || values.isEmpty()) {
                return new Failure(401);
            }

            // wrong auth type
            String[] credentialParts = values.getFirst().split(" ");
            if (credentialParts.length != 2 || !credentialParts[0].equals("Bearer")) {
                return new Failure(401);
            }

            String token = credentialParts[1];
            DecodedJWT decodedJWT;
            try {
                Algorithm algorithm = Algorithm.HMAC256("secret".getBytes());
                JWTVerifier verifier = JWT.require(algorithm)
                        // specify any specific claim validations
                        .withClaim("role", "admin")
                        .withClaim("id", 1)
                        // reusable verifier instance
                        .build();

                decodedJWT = verifier.verify(token);

                return new Success(new HttpPrincipal(decodedJWT.getSubject(), decodedJWT.getClaim("role").asString()));
            } catch (JWTVerificationException exception){
                // Invalid signature/claims
            }

            // correct format, but wrong user for this endpoint
            return new Failure(403);
        }
    }

}

