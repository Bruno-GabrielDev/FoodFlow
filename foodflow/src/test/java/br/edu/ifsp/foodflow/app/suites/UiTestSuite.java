package br.edu.ifsp.foodflow.app.suites;

import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * Test suite que executa todos os testes de UI (anotados com @UiTest).
 * Roda os testes Selenium contra o frontend do FoodFlow usando Page Object Pattern.
 */
@Suite
@SuiteDisplayName("Suite de Testes de UI - FoodFlow")
@SelectPackages("br.edu.ifsp.foodflow.app.ui")
@IncludeTags("UiTest")
public class UiTestSuite {
}