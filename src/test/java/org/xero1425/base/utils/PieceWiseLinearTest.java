package org.xero1425.base.utils;

import java.util.ArrayList;
import java.util.List;

import org.junit.*;

import edu.wpi.first.wpilibj.geometry.Translation2d;

public class PieceWiseLinearTest {
    @Before
    public void init() {
    }

    @Test
    public void emptyListTest() {
        boolean except = false;
        List<Translation2d> points = new ArrayList<Translation2d>();

        try {
            PieceWiseLinear pwl = new PieceWiseLinear(points);
        } catch (Exception ex) {
            except = true;
        }

        Assert.assertEquals(true, except);
    }

    @Test
    public void oneSegTest() throws Exception {
        List<Translation2d> points = new ArrayList<Translation2d>();
        points.add(new Translation2d(0.0, 0.0));
        points.add(new Translation2d(10.0, 10.0));

        PieceWiseLinear pwl = new PieceWiseLinear(points);

        Assert.assertEquals(0.0, pwl.getValue(-10.0), 1e-6) ;
        Assert.assertEquals(10.0, pwl.getValue(20.0), 1e-6) ;
        Assert.assertEquals(5.0, pwl.getValue(5.0), 1e-6) ;
    }

    @Test
    public void twoSegTest() throws Exception {
        List<Translation2d> points = new ArrayList<Translation2d>();
        points.add(new Translation2d(0.0, 0.0));
        points.add(new Translation2d(10.0, 10.0));
        points.add(new Translation2d(20.0, 0.0));

        PieceWiseLinear pwl = new PieceWiseLinear(points);

        Assert.assertEquals(0.0, pwl.getValue(-10.0), 1e-6) ;
        Assert.assertEquals(0.0, pwl.getValue(30.0), 1e-6) ;
        Assert.assertEquals(5.0, pwl.getValue(5.0), 1e-6) ;
        Assert.assertEquals(5.0, pwl.getValue(15.0), 1e-6) ;
    }

    
    @Test
    public void shooterTest() throws Exception {
        List<Translation2d> points = new ArrayList<Translation2d>();
        points.add(new Translation2d(116.0, 5950.0));
        points.add(new Translation2d(145.0, 5400.0));
        points.add(new Translation2d(166.0, 5300.0));
        points.add(new Translation2d(195.0, 5320.0));
        points.add(new Translation2d(210.0, 5425.0));
        points.add(new Translation2d(236.0, 5530.0));
        points.add(new Translation2d(268.0, 5555.0));
        points.add(new Translation2d(292.0, 5650.0));

        PieceWiseLinear pwl = new PieceWiseLinear(points);

        double d = pwl.getValue(210.42) ;
        System.out.println("Value " + Double.toString(d)) ;
        Assert.assertEquals(5425, d, 1e-6) ;
    }
} ;