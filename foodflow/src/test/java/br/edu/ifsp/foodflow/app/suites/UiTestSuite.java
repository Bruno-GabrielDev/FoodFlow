package br.edu.ifsp.foodflow.app.suites;

import br.edu.ifsp.foodflow.app.util.DbTestHelper;
import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("Suite de Testes de UI - FoodFlow")
@SelectPackages("br.edu.ifsp.foodflow.app.ui")
@IncludeTags("UiTest")
public class UiTestSuite {
    static {

        DbTestHelper.resetDatabase();
    }
}
