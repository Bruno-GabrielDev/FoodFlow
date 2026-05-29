package br.edu.ifsp.foodflow.app.suites;

import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * Test suite que executa todos os testes de API (anotados com @ApiTest).
 * Roda os testes de integração HTTP via RestAssured contra os controllers do FoodFlow.
 */
@Suite
@SuiteDisplayName("Suite de Testes de API - FoodFlow")
@SelectPackages("br.edu.ifsp.foodflow.app.api")
@IncludeTags("ApiTest")
public class ApiTestSuite {
}