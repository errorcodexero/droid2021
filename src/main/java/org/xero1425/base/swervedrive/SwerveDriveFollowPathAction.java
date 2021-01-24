package org.xero1425.base.swervedrive;

import org.xero1425.base.Subsystem;
import org.xero1425.base.XeroRobot;
import org.xero1425.misc.BadParameterTypeException;
import org.xero1425.misc.MissingParameterException;
import org.xero1425.misc.PIDACtrl;
import org.xero1425.misc.XeroPath;

public class SwerveDriveFollowPathAction extends SwerveDriveAction {
    private String path_name_;
    private boolean reversed_;
    private XeroPath path_;
    private PIDACtrl fl_follower_, fr_follower_, bl_follower_, br_follower_;
    private int plot_id_;
    private Double[] plot_data_;
    private SwerveDriveSubsystem.WheelData start_ ;
    private int index_ ;
    private double start_time_ ;
    private double start_angle_ ;

    static final String[] plot_columns_ = { "time", "ltpos", "lapos", "ltvel", "lavel", "ltaccel", "laaccel", "lout",
            "lticks", "lvout", "laout", "lpout", "ldout", "lerr", "rtpos", "rapos", "rtvel", "ravel", "rtaccel",
            "raaccel", "rout", "rticks", "rvout", "raout", "rpout", "rdout", "rerr", "thead", "ahead", "angcorr" };

    public SwerveDriveFollowPathAction(SwerveDriveSubsystem drive, String path, boolean reverse)
            throws BadParameterTypeException, MissingParameterException {
        super(drive);

        path_name_ = path;
        reversed_ = reverse;

        fl_follower_ = new PIDACtrl(drive.getRobot().getSettingsParser(), "swervedrive:follower:fl", false);
        fr_follower_ = new PIDACtrl(drive.getRobot().getSettingsParser(), "swervedrive:follower:fr", false);
        bl_follower_ = new PIDACtrl(drive.getRobot().getSettingsParser(), "swervedrive:follower:bl", false);
        br_follower_ = new PIDACtrl(drive.getRobot().getSettingsParser(), "swervedrive:follower:br", false);

        plot_id_ = drive.initPlot(toString(0));
        plot_data_ = new Double[plot_columns_.length];
    }

    @Override
    public void start() throws Exception {
        super.start();

        path_ = getSubsystem().getRobot().getPathManager().getPath(path_name_);

        start_ = getSubsystem().getDistances() ;

        start_time_ = getSubsystem().getRobot().getTime();

        getSubsystem().startPlot(plot_id_, plot_columns_);
    }

    @Override
    public void run() {
        SwerveDriveSubsystem td = getSubsystem();
        XeroRobot robot = td.getRobot() ;

        if (index_ < path_.getSize())
        {
            double dt = robot.getDeltaTime();
            XeroPathSegment lseg = path_.getLeftSegment(index_) ;
            XeroPathSegment rseg = path_.getRightSegment(index_) ;

            double laccel, lvel, lpos ;
            double raccel, rvel, rpos ;
            double thead, ahead ;

            if (reverse_)
            {
                laccel = -rseg.getAccel() ;
                lvel = -rseg.getVelocity() ;
                lpos = -rseg.getPosition() ;
                raccel = -lseg.getAccel() ;
                rvel = -lseg.getVelocity() ;
                rpos = -lseg.getPosition() ;
                thead = XeroMath.normalizeAngleDegrees(lseg.getHeading() - target_start_angle_) ;
                ahead = XeroMath.normalizeAngleDegrees(getSubsystem().getAngle() - start_angle_) ;                 
            }
            else
            {
                laccel = lseg.getAccel() ;
                lvel = lseg.getVelocity() ;
                lpos = lseg.getPosition() ;
                raccel = rseg.getAccel() ;
                rvel = rseg.getVelocity() ;
                rpos = rseg.getPosition() ;
                thead = XeroMath.normalizeAngleDegrees(lseg.getHeading() - target_start_angle_) ;  
                ahead = XeroMath.normalizeAngleDegrees(getSubsystem().getAngle() - start_angle_) ;
            }

            double ldist, rdist ;
            ldist = td.getLeftDistance() - left_start_ ;
            rdist = td.getRightDistance() - right_start_ ;
            
            double lout = left_follower_.getOutput(laccel, lvel, lpos, ldist, dt) ;
            double rout = right_follower_.getOutput(raccel, rvel, rpos, rdist, dt) ;

            double angerr = XeroMath.normalizeAngleDegrees(thead - ahead) ;
            double turn = angle_correction_ * angerr ;
            lout += turn ;
            rout -= turn ;

            td.setPower(lout, rout) ;

            plot_data_[0] = robot.getTime() - start_time_ ;

            // Left side
            plot_data_[1] = lpos ;
            plot_data_[2] = td.getLeftDistance() - left_start_ ;
            plot_data_[3] = lvel ;
            plot_data_[4] = td.getLeftVelocity() ;
            plot_data_[5] = laccel ;
            plot_data_[6] = td.getLeftAcceleration() ;
            plot_data_[7] = lout ;
            plot_data_[8] = (double)td.getLeftTick() ;
            plot_data_[9] = left_follower_.getVPart() ;
            plot_data_[10] = left_follower_.getAPart() ;
            plot_data_[11] = left_follower_.getPPart() ;
            plot_data_[12] = left_follower_.getDPart() ;
            plot_data_[13] = left_follower_.getLastError() ;                                                

            // Right side
            plot_data_[14] = rpos ;
            plot_data_[15] = td.getRightDistance() - right_start_ ;
            plot_data_[16] = rvel ;
            plot_data_[17] = td.getRightVelocity() ;
            plot_data_[18] = raccel ;
            plot_data_[19] = td.getRightAcceleration() ;
            plot_data_[20] = rout ;
            plot_data_[21] = (double)td.getRightTick() ;
            plot_data_[22] = right_follower_.getVPart() ;
            plot_data_[23] = right_follower_.getAPart() ;
            plot_data_[24] = right_follower_.getPPart() ;
            plot_data_[25] = right_follower_.getDPart() ;
            plot_data_[26] = right_follower_.getLastError() ;

            plot_data_[27] = thead ;
            plot_data_[28] = ahead ;
            plot_data_[29] = turn ;

            td.addPlotData(plot_id_, plot_data_);
        }
        index_++ ;

        if (index_ == path_.getSize())
        {
            td.endPlot(plot_id_);
            td.setPower(0.0, 0.0) ;
            setDone() ;
        }
    }

    @Override
    public void cancel() {
        super.cancel() ;
        index_ = path_.getSize() ;

        getSubsystem().setPower(0.0, 0.0) ;
        getSubsystem().endPlot(plot_id_);
    }


    @Override
    public String toString(int indent) {
        String ret = prefix(indent) + "SwerveDriveFollowPath-" + path_name_ ;
        if (reversed_)
            ret += "[R]" ;
        return ret ;
    }   
}
