package br.edu.ifsp.foodflow.app.suites;

import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * Test suite que executa todos os testes de persistencia
 * anotados com @PersistenceTest.
 */
@Suite
@SuiteDisplayName("Suite de Testes de Persistencia - FoodFlow")
@SelectPackages("br.edu.ifsp.foodflow.app.persistence")
@IncludeTags("PersistenceTest")
public class PersistenceTestSuite {
}
