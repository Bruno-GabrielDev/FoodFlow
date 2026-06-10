package br.edu.ifsp.foodflow.app.suites;

import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("Suite de Testes de API - FoodFlow")
@SelectPackages("br.edu.ifsp.foodflow.app.api")
@IncludeTags("ApiTest")
public class ApiTestSuite {
}
