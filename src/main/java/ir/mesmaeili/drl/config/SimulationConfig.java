package ir.mesmaeili.drl.config;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SimulationConfig {
    @NotNull
    private int serverCount = 200;
    @NotNull
    private int BSCount = 1000;
    @NotNull
    private int serverMaxQueueSize = 200;
    @NotNull
    private int spaceX;
    @NotNull
    private int spaceY;
    @NotNull
    private double deltaT;
    @NotNull
    private double totalSimulationTime = 20.; // in seconds
    @NotNull
    private double taskPoissonMean = 10;
}
