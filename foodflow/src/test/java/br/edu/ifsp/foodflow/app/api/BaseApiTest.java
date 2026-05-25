package br.edu.ifsp.foodflow.app.api;

import io.restassured.RestAssured;
import io.restassured.config.RestAssuredConfig;
import org.junit.jupiter.api.BeforeAll;

/**
 * Classe base para os testes de API.
 * Configura a URL base do RestAssured e utilitários comuns.
 */
public abstract class BaseApiTest {

    protected static final String BASE_URI = "http://localhost:8080";

    @BeforeAll
    static void setupRestAssured() {
        RestAssured.baseURI = BASE_URI;
    }
}