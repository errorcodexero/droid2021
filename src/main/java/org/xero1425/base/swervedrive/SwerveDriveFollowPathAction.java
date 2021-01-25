package org.xero1425.base.swervedrive;

import org.xero1425.base.XeroRobot;
import org.xero1425.base.motors.BadMotorRequestException;
import org.xero1425.misc.BadParameterTypeException;
import org.xero1425.misc.MissingParameterException;
import org.xero1425.misc.PIDACtrl;
import org.xero1425.misc.PIDCtrl;
import org.xero1425.misc.XeroPath;
import org.xero1425.misc.XeroPathSegment;

public class SwerveDriveFollowPathAction extends SwerveDriveAction {
    private String path_name_;
    private XeroPath path_;
    private PIDACtrl[] followers_ ;
    private PIDCtrl[] steer_pids_ ;
    private int plot_id_;
    private Double[] plot_data_;
    private double[] start_ ;
    // private double target_start_angle_ ;
    private int index_ ;
    private double start_time_ ;
    // private double start_angle_ ;

    static final String[] plot_columns_ = { "time", 
        "fltpos", "flapos", "fltvel", "flavel", "fltaccel", "flaaccel", "fldrive", "flsteer",
        "frtpos", "frapos", "frtvel", "fravel", "frtaccel", "fraaccel", "frdrive", "frsteer",
        "bltpos", "blapos", "bltvel", "blavel", "bltaccel", "blaaccel", "bldrive", "blsteer",
        "brtpos", "brapos", "brtvel", "bravel", "brtaccel", "braaccel", "brdrive", "fbrsteer",   
    } ;

    public SwerveDriveFollowPathAction(SwerveDriveSubsystem drive, String path)
            throws BadParameterTypeException, MissingParameterException {
        super(drive);

        path_name_ = path;
        
        followers_ = new PIDACtrl[drive.getWheelCount()] ;
        steer_pids_ = new PIDCtrl[drive.getWheelCount()] ;
        for(int i = 0 ; i < followers_.length ; i++) 
        {
            followers_[i] = new PIDACtrl(drive.getRobot().getSettingsParser(), "swervedrive:follower:" + drive.getWheelName(i), false) ;
            steer_pids_[i] = new PIDCtrl(drive.getRobot().getSettingsParser(), "swervedrive:steerpid:" + drive.getWheelName(i), true) ;
        }

        plot_id_ = drive.initPlot(toString(0));
        plot_data_ = new Double[plot_columns_.length];
    }

    @Override
    public void start() throws Exception {
        super.start() ;

        path_ = getSubsystem().getRobot().getPathManager().getPath(path_name_) ;

        start_ = getSubsystem().getDistances() ;

        index_ = 0 ;
        start_time_ = getSubsystem().getRobot().getTime() ;
        // start_angle_ = getSubsystem().getAngle() ;
        // target_start_angle_ = path_.getSegment(0, 0).getHeading() ;

        getSubsystem().startPlot(plot_id_, plot_columns_);
    }

    @Override
    public void run() throws BadMotorRequestException {
        SwerveDriveSubsystem sw = getSubsystem();
        XeroRobot robot = sw.getRobot() ;
        double [] drive = new double[4] ;
        double [] steer = new double[4] ;
        int pindex = 0 ;

        if (index_ < path_.getSize())
        {
            double dt = robot.getDeltaTime();
            XeroPathSegment[] segs = path_.getSegments(index_) ;
            double[] distances = sw.getDistances() ;
            double[] velocities = sw.getVelocities() ;
            double[] accelerations = sw.getAccelerations() ;
            double[] angles = sw.getWheelAngles() ;

            // thead = XeroMath.normalizeAngleDegrees(segs[0].getHeading() - target_start_angle_) ;  
            // ahead = XeroMath.normalizeAngleDegrees(getSubsystem().getAngle() - start_angle_) ;

            plot_data_[pindex++] = robot.getTime() - start_time_ ;

            for(int i = 0 ; i < segs.length ; i++)
            {
                double dist = distances[i] - start_[i] ;
                drive[i] = followers_[i].getOutput(segs[i].getAccel(), segs[i].getVelocity(), segs[i].getPosition(), dist, dt) ;
                steer[i] = steer_pids_[i].getOutput(segs[i].getHeading(), angles[i], dt) ;

                plot_data_[pindex++] = segs[i].getPosition() ;
                plot_data_[pindex++] = dist ;
                plot_data_[pindex++] = segs[i].getVelocity() ;
                plot_data_[pindex++] = velocities[i] ;
                plot_data_[pindex++] = segs[i].getAccel() ;
                plot_data_[pindex++] = accelerations[i] ;
                plot_data_[pindex++] = drive[i] ;
                plot_data_[pindex++] = steer[i] ;
            }            
            sw.setPower(drive, steer) ;
            sw.addPlotData(plot_id_, plot_data_) ;
        }
        index_++ ;

        if (index_ == path_.getSize())
        {
            sw.endPlot(plot_id_);
            sw.stop() ;
            setDone() ;
        }
    }

    @Override
    public void cancel() {
        super.cancel() ;
        index_ = path_.getSize() ;

        getSubsystem().stop() ;
        getSubsystem().endPlot(plot_id_);
    }

    @Override
    public String toString(int indent) {
        String ret = prefix(indent) + "TankDriveFollowPath-" + path_name_ ;
        return ret ;
    }

}