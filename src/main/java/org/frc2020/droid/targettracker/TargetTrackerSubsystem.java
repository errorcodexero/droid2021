package org.frc2020.droid.targettracker;
import org.frc2020.droid.droidlimelight.DroidLimeLightSubsystem;
import org.frc2020.droid.turret.TurretSubsystem;
import org.xero1425.base.Subsystem;
import org.xero1425.misc.BadParameterTypeException;
import org.xero1425.misc.MessageLogger;
import org.xero1425.misc.MessageType;
import org.xero1425.misc.MissingParameterException;

//
// The purpose of the tracker class is to generate two things.  It generates
// an error value for the TURRET pid controller and it generates the distance
// from the robot camera to the target.  The target tracker returns a boolean
// that indicates if the target tracker is ready to fire.  This goes from 
// false to true once the drivebase is stopped, the target is seen, and a value
// for angle and distacne has been locked in.
//
public class TargetTrackerSubsystem extends Subsystem {
    private boolean enabled_ ;
    private double relative_angle_ ;
    private DroidLimeLightSubsystem ll_ ;
    private TurretSubsystem turret_ ;
    private double distance_ ;
    private int lost_count_ ;
    private int max_lost_count_ ;
    boolean has_target_ ;

    private double camera_offset_angle_ ;

    public static final String SubsystemName = "targettracker" ;

    public TargetTrackerSubsystem(Subsystem parent, DroidLimeLightSubsystem ll, TurretSubsystem turret)
            throws BadParameterTypeException, MissingParameterException {

        super(parent, SubsystemName);

        ll_ = ll ;
        turret_ = turret ;

        relative_angle_ = 0.0 ;
        enabled_ = true ;
        lost_count_ = 0 ;
        has_target_ = false;
        
        camera_offset_angle_ = getRobot().getSettingsParser().get("targettracker:camera_offset_angle").getDouble() ;
        max_lost_count_ = getRobot().getSettingsParser().get("targettracker:lost_count").getInteger() ;
    }

    public void enable(boolean b) {
        enabled_ = b ;
    }

    public boolean hasValidSample() {
        return has_target_ ;
    }

    public double getDesiredTurretAngle() {
        return relative_angle_ ;
    }

    public double getDistance() {
        return distance_ ;
    }

    @Override
    public void computeMyState() {
        MessageLogger logger = getRobot().getMessageLogger() ;

        if (enabled_)
        {
            if (ll_.isTargetDetected())
            {
                distance_ = ll_.getDistance() ;
               
                double yaw = ll_.getYaw() - camera_offset_angle_ ;
                relative_angle_ = -yaw + turret_.getPosition() ;
                logger.startMessage(MessageType.Debug, getLoggerID());
                logger.add("targettracker:").add("yaw", yaw).add("distance", distance_) ;
                logger.add(" ll", ll_.getYaw()).add(" offset", camera_offset_angle_) ;
                logger.add(" tpos", turret_.getPosition()).add(" relative", relative_angle_);
                logger.endMessage();

                has_target_ = true ;
                lost_count_ = 0 ;
            }
            else
            {
                if (lost_count_ > max_lost_count_)
                    has_target_ = false ;

                logger.startMessage(MessageType.Debug, getLoggerID());
                logger.add("targettracker: lost target ").add(" lost count", lost_count_) ;
                logger.add(" has_target", has_target_).endMessage();
            }
        }
    }
}
