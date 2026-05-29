package br.edu.ifsp.foodflow.app.util;

import io.restassured.RestAssured;
import br.edu.ifsp.foodflow.app.ui.pages.LoginPage;
import org.openqa.selenium.WebDriver;

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

    /**
     * Cria um usuário via API, faz login pela UI e espera o redirecionamento ao dashboard.
     * Retorna as credenciais usadas, para o caso de o teste precisar delas.
     */
    public static AuthHelper.RegisteredUser loginViaUi(WebDriver driver, String baseUrl) {
        AuthHelper.RegisteredUser user = createUserViaApi();
        new LoginPage(driver)
                .open(baseUrl)
                .login(user.username(), user.password());
        // Aguarda redirecionamento ao dashboard antes de devolver o controle ao teste
        new LoginPage(driver).urlContains("/dashboard");
        return user;
    }
}