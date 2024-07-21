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
    private double alpha = 0.1; // memory coefficient
    private double beta = 0.1; // disk coefficient
    private double gamma = 0.8; // cpu coefficient
    private int PSI = 10;     // radius for select neighbors
    private Geometry voronoiTessellation;
}
