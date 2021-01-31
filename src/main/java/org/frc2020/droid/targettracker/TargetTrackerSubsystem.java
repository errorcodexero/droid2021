package org.frc2020.droid.targettracker;
import org.frc2020.droid.droidlimelight.DroidLimeLightSubsystem;
import org.frc2020.droid.turret.TurretSubsystem;
import org.xero1425.base.Subsystem;
import org.xero1425.base.tankdrive.TankDriveSubsystem;
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

    private double last_camera_sample_time_ ;
    private boolean enabled_ ;
    private double relative_angle_ ;
    private DroidLimeLightSubsystem ll_ ;
    private TankDriveSubsystem db_ ;
    private TurretSubsystem turret_ ;
    private boolean locked_ ;
    private double distance_ ;

    private double camera_offset_angle_ ;
    private double db_velocity_threshold_ ;
    private double lock_threshold_ ;

    public static final String SubsystemName = "targettracker" ;

    public TargetTrackerSubsystem(Subsystem parent, TankDriveSubsystem db, 
                    DroidLimeLightSubsystem ll, TurretSubsystem turret) throws BadParameterTypeException, MissingParameterException {
        super(parent, SubsystemName) ;

        ll_ = ll ;
        db_ = db ;
        turret_ = turret ;

        last_camera_sample_time_ = 0.0 ;
        relative_angle_ = 0.0 ;
        enable(true) ;

        camera_offset_angle_ = getRobot().getSettingsParser().get("targettracker:camera_offset_angle").getDouble() ;
        db_velocity_threshold_ = getRobot().getSettingsParser().get("gamepiecemanipulator:fire:max_drivebase_velocity").getDouble() ;
        lock_threshold_ = getRobot().getSettingsParser().get("targettracker:turret_lock_threshold").getDouble() ;
    }

    public void enable(boolean b) {
        enabled_ = b ;
        locked_ = false ;
    }

    public boolean hasValidSample() {
        return locked_ ;
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

        if (locked_)
        {
            logger.startMessage(MessageType.Debug, getLoggerID()) ;
            logger.add("targettracker: locked").endMessage();

            if (Math.abs(db_.getVelocity()) > db_velocity_threshold_) {
                logger.startMessage(MessageType.Debug, getLoggerID()) ;
                logger.add("targettracker: ").add("dbvelocity", db_.getVelocity()).add(" unlocking") ;
                logger.endMessage(); 
                locked_ = false ;
            }
        }

        if (!locked_)
        {
            if (enabled_ && ll_.getSampleTime() > last_camera_sample_time_)
            {
                last_camera_sample_time_ = ll_.getSampleTime() ;
                distance_ = ll_.getDistance() ;

                double yaw = ll_.getYaw() - camera_offset_angle_ ;
                relative_angle_ = -yaw + turret_.getPosition() ;
                logger.startMessage(MessageType.Debug, getLoggerID());
                logger.add("targettracker:").add("yaw", yaw).add("distance", distance_) ;
                logger.add(" ll", ll_.getYaw()).add(" offset", camera_offset_angle_) ;
                logger.add(" tpos", turret_.getPosition()).add(" relative", relative_angle_);
                logger.endMessage();
                // if (Math.abs(db_.getVelocity()) < db_velocity_threshold_ && Math.abs(yaw) < lock_threshold_) {
                //     locked_ = true ;
                // }
            }
            else
            {
                relative_angle_ = 0 ;
            }
        }
    }
}
