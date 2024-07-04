package ir.mesmaeili.drl;

import ir.mesmaeili.drl.alg.EVBLBAlgorithm;
import ir.mesmaeili.drl.alg.EvblbConfig;
import ir.mesmaeili.drl.config.SimulationConfig;
import ir.mesmaeili.drl.simulator.Simulation;
import ir.mesmaeili.drl.util.VoronoiBuilder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.BasicConfigurator;
import org.locationtech.jts.geom.Coordinate;

import java.util.Date;
import java.util.List;

@Slf4j
@Getter
public class SimulatorApp {

    private static final VoronoiBuilder vb = new VoronoiBuilder();

    public static void main(String[] args) {
        BasicConfigurator.configure();

        SimulationConfig simulationConfig = new SimulationConfig();
        simulationConfig.setServerCount(10);
        simulationConfig.setSpaceX(100);
        simulationConfig.setSpaceX(100);
        simulationConfig.setDeltaT(2);

        log.info("Start simulation at {}", new Date());
        SimulateEVBLB(simulationConfig);
        log.info("Finish simulation at {}", new Date());
    }

    private static void SimulateEVBLB(SimulationConfig simulationConfig) {
        EvblbConfig config = new EvblbConfig();
        // Compute the Voronoi Tessellation (VT)
        List<Coordinate> points = vb.generatePoints(simulationConfig.getPointCount(), simulationConfig.getSpaceX(), simulationConfig.getSpaceY());
        config.setVoronoiTessellation(vb.generateDiagram(points));
        EVBLBAlgorithm evblbAlgorithm = new EVBLBAlgorithm(simulationConfig, config);

        Simulation simulation = new Simulation(evblbAlgorithm, simulationConfig);
        simulation.run();
    }
}
