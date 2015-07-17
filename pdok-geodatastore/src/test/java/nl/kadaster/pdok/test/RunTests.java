package nl.kadaster.pdok.test;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

/**
 * User: bloemj
 * Date: 17-7-2015
 * Time: 12:59
 */
@CucumberOptions(tags = {"~@not_implemented", "~@ignore"},
        format={"json:target/cucumber.json", "html:target/cucumber-html-report/registratie/initieel"},
        features="classpath:features/")
public class RunTests {
}
