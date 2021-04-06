package org.xero1425.base.tankdrive;

/// \brief describes a point along a path that might lie between two segments
public class PathPoint {
    //
    // The first of the two segments that contain the point
    //
    private int which_ ;

    //
    // The percentage the point is between the two segments
    //
    private double pcnt_ ;

    public PathPoint(int which, double pcnt) {
        which_ = which ;
        pcnt_ = pcnt ;
    }

    public int which() {
        return which_ ;
    }

    public double percent() {
        return pcnt_ ;
    }
}
