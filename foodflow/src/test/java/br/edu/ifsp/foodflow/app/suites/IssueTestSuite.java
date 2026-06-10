package br.edu.ifsp.foodflow.app.suites;

import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("Suite de Demonstração de Defeitos (Issues) - FoodFlow")
@SelectPackages("br.edu.ifsp.foodflow.app.ui")
@IncludeTags("IssueTest")
public class IssueTestSuite {
}
