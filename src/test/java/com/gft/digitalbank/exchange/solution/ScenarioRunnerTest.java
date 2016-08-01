package com.gft.digitalbank.exchange.solution;

import org.junit.Test;

import com.gft.digitalbank.exchange.model.SolutionResult;
import com.gft.digitalbank.exchange.verification.scenario.ScenarioRunner;
import com.gft.digitalbank.exchange.verification.scenario.functional.Scenario02;

public class ScenarioRunnerTest {

    @Test
    public void test() {
        ScenarioRunner runner = new ScenarioRunner(new Scenario02());

        SolutionResult result = runner.run();

        System.out.println(result);

    }

}
