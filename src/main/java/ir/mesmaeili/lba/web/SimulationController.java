package ir.mesmaeili.lba.web;

import ir.mesmaeili.lba.config.SimulationState;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/")
public class SimulationController {

    @GetMapping("/main")
    public String simulationPage() {
        return "simulation";
    }

    @MessageMapping("/simulation")
    @SendTo("/topic/simulationStatus")
    public SimulationState sendSimulationStatus() {
        return WebApplication.getSimulationState();
    }
}

