package br.edu.ifsp.foodflow.app.util;

import io.restassured.RestAssured;

/**
 * Utilitário para os testes de UI.
 * Permite preparar dados (como registrar usuários) via API antes
 * de exercitar a interface gráfica.
 */
public class UiTestHelper {

    private static final String API_BASE_URI = "http://localhost:8080";

    /**
     * Registra um novo garçom via API e retorna suas credenciais,
     * para que possam ser usadas no login pela interface.
     */
    public static AuthHelper.RegisteredUser createUserViaApi() {
        RestAssured.baseURI = API_BASE_URI;
        return AuthHelper.registerWaiter();
    }
}