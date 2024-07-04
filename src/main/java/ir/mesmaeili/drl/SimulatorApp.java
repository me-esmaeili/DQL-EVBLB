package ir.mesmaeili.drl;

import ir.mesmaeili.drl.config.SimulationConfig;
import ir.mesmaeili.drl.simulator.Simulation;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.BasicConfigurator;

import java.util.*;

@Slf4j
@Getter
public class SimulatorApp {
    public static void main(String[] args) {
        BasicConfigurator.configure();

        SimulationConfig simulationConfig = new SimulationConfig();
        simulationConfig.setServerCount(10);
        simulationConfig.setSpaceX(100);
        simulationConfig.setSpaceX(100);
        simulationConfig.setR_Delta(2);

        log.info("Start simulation at {}", new Date());
        Simulation simulation = new Simulation(simulationConfig);
        simulation.run();
        log.info("Finish simulation at {}", new Date());
    }
}
