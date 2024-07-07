package ir.mesmaeili.lba;

import ir.mesmaeili.lba.algorithm.EVBLBAlgorithm;
import ir.mesmaeili.lba.algorithm.EvblbConfig;
import ir.mesmaeili.lba.config.SimulationConfig;
import ir.mesmaeili.lba.result.SimulationChart;
import ir.mesmaeili.lba.simulator.Simulation;
import ir.mesmaeili.lba.statistic.SimulationStatisticResult;
import ir.mesmaeili.lba.util.VoronoiUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.BasicConfigurator;
import org.locationtech.jts.geom.Coordinate;

import java.util.Date;
import java.util.List;

@Slf4j
@Getter
public class SimulatorApp {

    private static final VoronoiUtils vb = new VoronoiUtils();

    public static void main(String[] args) {
        BasicConfigurator.configure();

        SimulationConfig simulationConfig = new SimulationConfig();
        simulationConfig.setServerCount(20);
        simulationConfig.setSpaceX(100);
        simulationConfig.setSpaceX(100);
        simulationConfig.setDeltaT(2.);
        simulationConfig.setTotalSimulationTime(100);

        log.info("Start simulation at {}", new Date());
        SimulationStatisticResult result = SimulateEVBLB(simulationConfig);
        log.info("Finish simulation at {}", new Date());

        SimulationChart simulationChart = new SimulationChart();
        simulationChart.generateCharts(result);

        result.writeToCsv();
    }

    private static SimulationStatisticResult SimulateEVBLB(SimulationConfig simulationConfig) {
        EvblbConfig config = new EvblbConfig();
        // Compute the Voronoi Tessellation (VT)
        List<Coordinate> points = vb.generatePoints(simulationConfig.getServerCount(), simulationConfig.getSpaceX(), simulationConfig.getSpaceY());
        config.setVoronoiTessellation(vb.generateDiagram(points));
        EVBLBAlgorithm evblbAlgorithm = new EVBLBAlgorithm(simulationConfig, config);

        Simulation simulation = new Simulation(evblbAlgorithm, simulationConfig);
        return simulation.run();
    }
}
