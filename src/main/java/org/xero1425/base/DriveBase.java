package org.xero1425.base;

import com.kauailabs.navx.frc.AHRS;

import org.xero1425.misc.MessageLogger;
import org.xero1425.misc.MessageType;
import org.xero1425.misc.Speedometer;

import edu.wpi.first.wpilibj.SPI;

public abstract class DriveBase extends Subsystem {
    private AHRS navx_ ;
    private Speedometer angular_ ;
    private double total_angle_ ;

    public DriveBase(Subsystem parent, String name) {
        super(parent, name) ;

        MessageLogger logger = getRobot().getMessageLogger();

        navx_ = new AHRS(SPI.Port.kMXP) ;
        double start = getRobot().getTime() ;
        while (getRobot().getTime() - start < 3.0) {
            if (navx_.isConnected())
                break ;
        }

        if (!navx_.isConnected()) {
            logger.startMessage(MessageType.Error);
            logger.add("NavX is not connected - cannot perform tankdrive path following functions");
            logger.endMessage();
            navx_ = null;
        }

        angular_ = new Speedometer("angles", 2, true) ;        
    }

    public boolean hasNavX() {
        return navx_ != null ;
    }
    
    public double getAngle() {
        return angular_.getDistance() ;
    }

    public double getTotalAngle() {
        return total_angle_ ;
    }

    public void computeMyState() {
        if (hasNavX()) {
            double angle = -getNavX().getYaw();
            angular_.update(getRobot().getDeltaTime(), angle);
            total_angle_ = getNavX().getAngle() ;
        }        
    }

    protected AHRS getNavX() {
        return navx_ ;
    }
}
