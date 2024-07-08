package ir.mesmaeili.lba.algorithm;

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
    private double alpha = 0.34;
    private double beta = 0.33;
    private double gamma = 0.33;
    private Geometry voronoiTessellation;
}
