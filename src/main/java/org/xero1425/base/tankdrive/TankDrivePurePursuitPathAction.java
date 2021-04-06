package org.xero1425.base.tankdrive;

import org.xero1425.base.XeroRobot;
import org.xero1425.base.utils.LineSegment;
import org.xero1425.misc.BadParameterTypeException;
import org.xero1425.misc.MessageDestination;
import org.xero1425.misc.MessageLogger;
import org.xero1425.misc.MessageType;
import org.xero1425.misc.MissingParameterException;
import org.xero1425.misc.MissingPathException;
import org.xero1425.misc.PIDCtrl;
import org.xero1425.misc.SettingsParser;
import org.xero1425.misc.XeroPath;
import org.xero1425.misc.XeroPathSegment;

import edu.wpi.first.wpilibj.geometry.Pose2d;
import edu.wpi.first.wpilibj.geometry.Rotation2d;
import edu.wpi.first.wpilibj.geometry.Translation2d;
import edu.wpi.first.wpilibj.geometry.Twist2d;



public class TankDrivePurePursuitPathAction extends TankDriveAction {
    private double start_time_ ;
    private String path_name_;
    private double look_ahead_distance_;
    private XeroPath path_;
    private PIDCtrl left_pid_;
    private PIDCtrl right_pid_;
    private int plot_id_ ;
    private Double[] plot_data_ ;

    static final String[] plot_columns_ = {             
        "time", 
        "lavel", "ltvel", "lout", 
        "ravel", "rtvel", "rout", 
    } ;

    private final int MainRobot = 0;

    public TankDrivePurePursuitPathAction(TankDriveSubsystem drive, String path, boolean reverse)
            throws MissingPathException, MissingParameterException, BadParameterTypeException {
        super(drive);

        path_name_ = path;
        look_ahead_distance_ = 24.0;

        path_ = getSubsystem().getRobot().getPathManager().getPath(path) ;
        plot_id_ = drive.initPlot(toString(0)) ;
        plot_data_= new Double[plot_columns_.length] ;

        SettingsParser settings = getSubsystem().getRobot().getSettingsParser() ;
        left_pid_ = new PIDCtrl(settings, "tankdrive:purepursuit:left", false) ;
        right_pid_ = new PIDCtrl(settings, "tankdrive:purepursuit:right", false) ;
    }

    @Override
    public void start() {

        start_time_ = getSubsystem().getRobot().getTime() ;

        XeroPathSegment seg = path_.getSegment(MainRobot, 0) ;
        Pose2d start = new Pose2d(seg.getX(), seg.getY(), Rotation2d.fromDegrees(seg.getHeading())) ;
        getSubsystem().setPose(start);

        getSubsystem().startPlot(plot_id_, plot_columns_);
    }

    @Override
    public void run() {
        //
        // Get the tank drive subsystem
        //
        TankDriveSubsystem sub = getSubsystem() ;

        //
        // Get the robot
        //
        XeroRobot robot = sub.getRobot() ;

        //
        // Get the robots current position
        //
        Pose2d current = getSubsystem().getPose() ;

        //
        // Find the point on the path that is closest in distance to the robots
        // current position
        //
        PathPoint closest = findClosestPoint(current) ;

        //
        // Find point at the look ahead distance from here to the robot
        //
        LookAheadPoint look = findLookAheadPoint(closest) ;

        //
        // Find the curved arc we need to drive from the current
        // position to the look ahead position
        //
        Twist2d twist = findDrivingPath(current, look.getPose()) ;

        //
        // Compute the left and right drive velocities
        //
        TankDriveVelocities vel = inverseKinematics(twist) ;

        double left_out = left_pid_.getOutput(vel.getLeft(), sub.getLeftVelocity(), sub.getRobot().getDeltaTime()) ;
        double right_out = right_pid_.getOutput(vel.getRight(), sub.getRightVelocity(), sub.getRobot().getDeltaTime());

        MessageLogger logger = sub.getRobot().getMessageLogger() ;
        logger.startMessage(MessageType.Debug, sub.getLoggerID()) ;
        logger.add("rx", current.getX()) ;
        logger.add("ry", current.getY()) ;
        logger.add("ra", current.getRotation().getDegrees()) ;
        logger.add("lx", look.getPose().getX()) ;
        logger.add("ly", look.getPose().getY()) ;
        logger.add("la", look.getPose().getRotation().getDegrees()) ;
        logger.add("twx", twist.dx) ;
        logger.add("twy", twist.dy) ;
        logger.add("twtheta", twist.dtheta) ;
        logger.add("left", vel.getLeft()) ;
        logger.add("right", vel.getRight()) ;
        logger.endMessage();

        sub.setPower(left_out, right_out) ;

        plot_data_[0] = robot.getTime() - start_time_ ;
        plot_data_[1] = sub.getLeftVelocity() ;
        plot_data_[2] = vel.getLeft() ;
        plot_data_[3] = left_out ;
        plot_data_[4] = sub.getRightVelocity() ;
        plot_data_[5] = vel.getRight() ;
        plot_data_[6] = right_out ;
    }

    @Override
    public void cancel() {
        super.cancel() ;

        getSubsystem().setPower(0.0, 0.0) ;
    }
    
    @Override
    public String toString(int indent) {
        String ret = prefix(indent) + "TankDrivePurePursuitPathAction-" + path_name_ ;
        return ret ;
    }

    TankDriveVelocities inverseKinematics(Twist2d twist) {
        TankDriveVelocities ret ;

        if (Math.abs(twist.dtheta) < 1e-9)
        {
            ret = new TankDriveVelocities(twist.dx, twist.dx) ;
        }
        else
        {
            double dv = getSubsystem().getWidth() * twist.dtheta / (2 * getSubsystem().getScrub()) ;
            ret = new TankDriveVelocities(twist.dx - dv, twist.dx + dv) ;
        }

        return ret;
    }

    private Twist2d findDrivingPath(Pose2d current, Pose2d target) {
        return current.log(target) ;
    }

    private PathPoint findClosestPoint(Pose2d pos) {
        double dist = Double.MAX_VALUE ;
        int which = -1 ;
        double pcnt = 0.0 ;

        for(int i = 0 ; i < path_.getSize() - 1; i++) {
            XeroPathSegment seg0 = path_.getSegment(MainRobot, i) ;
            XeroPathSegment seg1 = path_.getSegment(MainRobot, i + 1) ;

            LineSegment ls = new LineSegment(seg0.getX(), seg0.getY(), seg1.getX(), seg1.getY()) ;
            Translation2d closest = ls.closest(pos.getTranslation()) ;
            double clpcnt = ls.dotProdParam(pos.getTranslation()) ;

            double ptdist = closest.getDistance(pos.getTranslation()) ;
            if (ptdist < dist)
            {
                which = i ;
                dist = ptdist ;
                pcnt = clpcnt ;
            }
        }

        return new PathPoint(which, pcnt) ;
    }

    private Pose2d interpolate(Pose2d p1, Pose2d p2, double param) {
        double x = p1.getX() + (p2.getX() - p1.getX()) * param ;
        double y = p1.getY() + (p2.getY() - p1.getY()) * param ;
        double heading = p1.getRotation().getRadians() + (p2.getRotation().getRadians() - p1.getRotation().getRadians()) * param ;

        return new Pose2d(x, y, new Rotation2d(heading)) ;
    }

    private Pose2d segmentToPose(XeroPathSegment seg) {
        return new Pose2d(seg.getX(), seg.getY(), Rotation2d.fromDegrees(seg.getHeading())) ;
    }

    private LookAheadPoint findLookAheadPoint(PathPoint pt) {
        LookAheadPoint ret = null ;

        double remaining = look_ahead_distance_ ;

        for(int i = pt.which() ; i < path_.getSize() - 1 ; i++)
        {
            XeroPathSegment seg0 = path_.getSegment(MainRobot, i);
            double prev = 0.0 ;
            if (i != 0)
                prev = path_.getSegment(MainRobot, i - 1).getPosition() ;

            double segsize = seg0.getPosition() - prev ;

            if (remaining < segsize)
            {
                //
                // The look ahead point is in this segment
                //

                XeroPathSegment seg1 = path_.getSegment(MainRobot, i + 1) ;

                // Calculate how far is the look ahead point between this segment and the next
                double pcnt = 1 - remaining / segsize ;
                ret = new LookAheadPoint(interpolate(segmentToPose(seg0), segmentToPose(seg1), pcnt), false) ;
                break ;
            }

            remaining -= segsize ;
        }

        if (ret == null)
        {
            //
            // The look ahead point exceeds the path
            //
            XeroPathSegment seg = path_.getSegment(MainRobot, path_.getSize() - 1) ;
            Pose2d endpt = segmentToPose(seg) ;
            ret = new LookAheadPoint(endpt, true) ;
        }

        return ret ;
    }
}
