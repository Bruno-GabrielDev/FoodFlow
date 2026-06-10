package br.edu.ifsp.foodflow.app.suites;

import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("Suite de Testes de Integração - FoodFlow")
@SelectPackages("br.edu.ifsp.foodflow.app.integration")
@IncludeTags("IntegrationTest")
public class IntegrationTestSuite {
}
