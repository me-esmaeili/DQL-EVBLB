package ir.mesmaeili.lba;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.mesmaeili.lba.algorithm.DQL_LBAlgorithm;
import ir.mesmaeili.lba.algorithm.EvblbAlgorithm;
import ir.mesmaeili.lba.algorithm.EvblbConfig;
import ir.mesmaeili.lba.algorithm.LBAlgorithm;
import ir.mesmaeili.lba.config.SimulationConfig;
import ir.mesmaeili.lba.config.SimulationState;
import ir.mesmaeili.lba.model.EdgeServer;
import ir.mesmaeili.lba.model.Point;
import ir.mesmaeili.lba.result.SimulationChart;
import ir.mesmaeili.lba.simulator.Simulation;
import ir.mesmaeili.lba.statistic.SimulationStatisticResult;
import ir.mesmaeili.lba.util.CustomObjectMapper;
import ir.mesmaeili.lba.util.PoissonDiskSampling;
import ir.mesmaeili.lba.util.VoronoiUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

@Slf4j
@Getter
public class SimulatorApp {
    private static final String SIMULATION_CONFIG_FILE = "simulation_config.json";
    private static final String EVBLB_CONFIG_FILE = "evblb_config.json";
    private static final boolean loadFromFile = true;
    private static final ObjectMapper objectMapper = new CustomObjectMapper();

    public static void main(String[] args) throws IOException {
        SimulationConfig simulationConfig;
        if (loadFromFile) {
            simulationConfig = loadSimulationConfig();
            if (simulationConfig == null) {
                simulationConfig = createNewSimulationConfig();
                saveSimulationConfig(simulationConfig, SIMULATION_CONFIG_FILE);
            }
        } else {
            simulationConfig = createNewSimulationConfig();
            saveSimulationConfig(simulationConfig, SIMULATION_CONFIG_FILE);
        }

// EVBLB configuration
//        log.info("Start simulation at {}", new Date());
//        EvblbConfig evblbConfig;
//        if (loadFromFile) {
//            evblbConfig = loadEvblbConfig();
//            if (evblbConfig == null) {
//                evblbConfig = createNewEvblbConfig(simulationConfig);
//                saveSimulationConfig(evblbConfig, EVBLB_CONFIG_FILE);
//            }
//        } else {
//            evblbConfig = createNewEvblbConfig(simulationConfig);
//            saveSimulationConfig(evblbConfig, EVBLB_CONFIG_FILE);
//        }

        EvblbConfig evblbConfig = new EvblbConfig();
        evblbConfig.setVoronoiTessellation(VoronoiUtils.generateDiagram(
                simulationConfig.getSpaceX(),
                simulationConfig.getSpaceY(),
                simulationConfig.getServerLocations()));

        LBAlgorithm dql = new DQL_LBAlgorithm(simulationConfig, evblbConfig, 20, 50);
        LBAlgorithm evblb = new EvblbAlgorithm(simulationConfig, evblbConfig);

        SimulationState simulationState = new SimulationState();
        Simulation simulation = new Simulation(evblb, simulationConfig, simulationState);
        SimulationStatisticResult result = simulation.run();
        log.info("Finish simulation at {}", new Date());

        SimulationChart simulationChart = new SimulationChart();
        simulationChart.plot(result);

        // print complete simulation report
        result.printReport();
        result.writeToCsv();
    }

    private static SimulationConfig createNewSimulationConfig() {
        SimulationConfig config = new SimulationConfig();
        config.setTotalSimulationTime(500);
        config.setTaskUniformRange(new ImmutablePair<>(910, 910));
        config.setServerCount(100);
        config.setServerMaxQueueSize(Integer.MAX_VALUE);
        config.setSpaceX(100);
        config.setSpaceY(100);
        config.setPoisonDskSamplingMinDistance(7.5f);
        config.setDeltaT(2.0);

        // Compute the Voronoi Tessellation (VT)
        List<Point> points = PoissonDiskSampling.generatePoints(
                config.getPoisonDskSamplingMinDistance(),
                config.getSpaceX(),
                config.getSpaceY()
        );
        config.setServerLocations(points);
        config.setServerCount(points.size());
        for (int i = 0; i < points.size(); i++) {
            EdgeServer edgeServer = new EdgeServer(i, config);
            edgeServer.setLocation(points.get(i));
            config.addServer(edgeServer);
        }
        log.info("Number of edge Servers is {}", points.size());
        return config;
    }

    private static EvblbConfig createNewEvblbConfig(SimulationConfig simulationConfig) {
        EvblbConfig config = new EvblbConfig();
        config.setVoronoiTessellation(VoronoiUtils.generateDiagram(
                simulationConfig.getSpaceX(),
                simulationConfig.getSpaceY(),
                simulationConfig.getServerLocations()));
        return config;
    }

    private static void saveSimulationConfig(Object config, String fileName) {
        try {
            objectMapper.writeValue(new File(fileName), config);
            log.info("Config saved to {}", fileName);
        } catch (IOException e) {
            log.error("Failed to save config", e);
        }
    }

    private static EvblbConfig loadEvblbConfig() {
        try {
            EvblbConfig config = objectMapper.readValue(new File(SimulatorApp.EVBLB_CONFIG_FILE), EvblbConfig.class);
            log.info("Simulation config loaded from {}", SimulatorApp.EVBLB_CONFIG_FILE);
            return config;
        } catch (Exception e) {
            log.error("Failed to load simulation config", e);
            return null;
        }
    }

    private static SimulationConfig loadSimulationConfig() {
        try {
            SimulationConfig config = objectMapper.readValue(new File(SimulatorApp.SIMULATION_CONFIG_FILE), SimulationConfig.class);
            log.info("Simulation config loaded from {}", SimulatorApp.SIMULATION_CONFIG_FILE);
            return config;
        } catch (Exception e) {
            log.error("Failed to load simulation config", e);
            return null;
        }
    }
}