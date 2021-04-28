package org.xero1425.base.utils;
 
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import edu.wpi.first.wpilibj.geometry.Translation2d;
 
public class PieceWiseLinear {
    private List<Translation2d> points_;
 
    public PieceWiseLinear(List<Translation2d> points) throws Exception {
        points_ = new ArrayList<Translation2d>() ;
 
        if (points.size() == 0)
            throw new Exception("cannot have empty points list as input") ;
 
        for(int i = 0 ; i < points.size() ; i++)
            points_.add(points.get(i)) ;
 
        Collections.sort(points_, new Comparator<Translation2d>() {
            public int compare(Translation2d p1, Translation2d p2) {
                if (p1.getX() < p2.getX())
                    return -1 ;
 
                if (p1.getX() > p2.getX())
                    return 1 ;
 
                if (p1.getY() < p2.getY())
                    return -1 ;
 
                if (p1.getY() > p2.getY())
                    return 1 ;
 
                return 0 ;
            }
        }) ;        
    }

    public int size() {
        return points_.size() ;
    }
 
    public Translation2d get(int i) {
        return points_.get(i) ;
    }
 
    public double getValue(double x) {
        double ret ;

        if (x < points_.get(0).getX()) {
            ret = points_.get(0).getY() ;
        }
        else if (x > points_.get(points_.size() - 1).getX()) {
            ret = points_.get(points_.size() - 1).getY() ;
        }
        else {
            int which_x = findWhich(x) ;

            Translation2d low = points_.get(which_x) ;
            Translation2d high = points_.get(which_x + 1) ;

            double m = ( (high.getY() - low.getY()) / (high.getX() - low.getX()) ) ;
            double b = high.getY() - (high.getX() * m) ;

            ret = (m * x) + b ;

        }

        return ret ;
    }
    
    int findWhich(double x) {
        for (int i = 0; i < points_.size() ; i++) {
            if (x >= points_.get(i).getX() && x < points_.get(i + 1).getX() {
                return i ;
            }
        }

        return -1 ;
    }

}