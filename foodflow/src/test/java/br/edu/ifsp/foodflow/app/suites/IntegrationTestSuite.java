package br.edu.ifsp.foodflow.app.suites;

import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * Test suite que executa todos os testes de Integração (anotados com @IntegrationTest).
 * Valida fluxos completos do FoodFlow ponta-a-ponta.
 */
@Suite
@SuiteDisplayName("Suite de Testes de Integração - FoodFlow")
@SelectPackages("br.edu.ifsp.foodflow.app.integration")
@IncludeTags("IntegrationTest")
public class IntegrationTestSuite {
}