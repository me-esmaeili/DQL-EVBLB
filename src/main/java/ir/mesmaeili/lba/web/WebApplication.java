package ir.mesmaeili.lba.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.mesmaeili.lba.algorithm.EvblbAlgorithm;
import ir.mesmaeili.lba.algorithm.EvblbConfig;
import ir.mesmaeili.lba.config.SimulationConfig;
import ir.mesmaeili.lba.config.SimulationState;
import ir.mesmaeili.lba.simulator.Simulation;
import ir.mesmaeili.lba.util.VoronoiUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.locationtech.jts.geom.Coordinate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;

@Slf4j
@SpringBootApplication
public class WebApplication {

    @Getter
    private static SimulationState simulationState;

    public static void main(String[] args) {
        SpringApplication.run(WebApplication.class, args);
        simulationState = new SimulationState();
        runSimulation();
    }

    @Autowired
    private ApplicationContext context;

    public <T> T getBean(Class<T> beanType) {
        return context.getBean(beanType);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper;
    }

    public static void runSimulation() {
        VoronoiUtils vu = new VoronoiUtils();
        SimulationConfig simulationConfig = new SimulationConfig();
        simulationConfig.setServerCount(10);
        simulationConfig.setServerMaxQueueSize(200);
        simulationConfig.setSpaceX(1000);
        simulationConfig.setSpaceY(1000);
        simulationConfig.setDeltaT(1.5);
        simulationConfig.setTaskUniformRange(Pair.of(100, 200));
        simulationConfig.setTotalSimulationTime(100);

        EvblbConfig config = new EvblbConfig();
        // Compute the Voronoi Tessellation (VT)
        List<Coordinate> points = vu.generatePoints(simulationConfig.getServerCount(), simulationConfig.getSpaceX(), simulationConfig.getSpaceY());
        config.setVoronoiTessellation(vu.generateDiagram(points));
        EvblbAlgorithm evblbAlgorithm = new EvblbAlgorithm(simulationConfig, config);
        Simulation simulation = new Simulation(evblbAlgorithm, simulationConfig, simulationState);

        simulation.run();
    }


    @Configuration
    @EnableWebSocketMessageBroker
    public static class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

        @Override
        public void configureMessageBroker(MessageBrokerRegistry config) {
            config.enableSimpleBroker("/topic");
            config.setApplicationDestinationPrefixes("/app");
        }

        @Override
        public void registerStompEndpoints(StompEndpointRegistry registry) {
            registry.addEndpoint("/simulation-websocket").withSockJS();
        }
    }
}
