package ir.mesmaeili.drl.alg;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.locationtech.jts.geom.Geometry;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EvblbConfig {
    private double alpha;
    private double beta;
    private double gamma;
    private double deltaT;
    private Geometry voronoiTessellation;
}
