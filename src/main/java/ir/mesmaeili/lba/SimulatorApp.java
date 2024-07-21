package ir.mesmaeili.lba;

import ir.mesmaeili.lba.algorithm.DQL_LBAlgorithm;
import ir.mesmaeili.lba.algorithm.EvblbAlgorithm;
import ir.mesmaeili.lba.algorithm.EvblbConfig;
import ir.mesmaeili.lba.algorithm.LBAlgorithm;
import ir.mesmaeili.lba.config.SimulationConfig;
import ir.mesmaeili.lba.config.SimulationState;
import ir.mesmaeili.lba.result.SimulationChart;
import ir.mesmaeili.lba.simulator.Simulation;
import ir.mesmaeili.lba.statistic.SimulationStatisticResult;
import ir.mesmaeili.lba.util.VoronoiUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.locationtech.jts.geom.Coordinate;

import java.util.Date;
import java.util.List;

@Slf4j
@Getter
public class SimulatorApp {

    public static void main(String[] args) {
        VoronoiUtils vu = new VoronoiUtils();
        SimulationConfig simulationConfig = new SimulationConfig();
        simulationConfig.setServerCount(50);
        simulationConfig.setServerMaxQueueSize(500);
        simulationConfig.setSpaceX(1000);
        simulationConfig.setSpaceY(1000);
        simulationConfig.setDeltaT(2);
        simulationConfig.setTaskUniformRange(Pair.of(200, 300));
        simulationConfig.setTotalSimulationTime(200);

        log.info("Start simulation at {}", new Date());
        EvblbConfig config = new EvblbConfig();
        // Compute the Voronoi Tessellation (VT)
        List<Coordinate> points = vu.generatePoints(simulationConfig.getServerCount(), simulationConfig.getSpaceX(), simulationConfig.getSpaceY());
        config.setVoronoiTessellation(vu.generateDiagram(points));

        LBAlgorithm dql = new DQL_LBAlgorithm(simulationConfig, config, 100, 50);
        LBAlgorithm evblb = new EvblbAlgorithm(simulationConfig, config);

        SimulationState simulationState = new SimulationState();
        Simulation simulation = new Simulation(evblb, simulationConfig, simulationState);
        SimulationStatisticResult result = simulation.run();
        log.info("Finish simulation at {}", new Date());

        SimulationChart simulationChart = new SimulationChart();
        simulationChart.generateCharts(result);

        // print complete simulation report
        result.printReport();
        result.writeToCsv();
    }
}
