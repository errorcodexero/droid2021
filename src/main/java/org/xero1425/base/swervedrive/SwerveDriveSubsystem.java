package org.xero1425.base.swervedrive;

import org.xero1425.base.DriveBase;
import org.xero1425.base.LoopType;
import org.xero1425.base.Subsystem;
import org.xero1425.base.motors.BadMotorRequestException;
import org.xero1425.base.motors.MotorController;
import org.xero1425.base.motorsubsystem.EncoderConfigException;
import org.xero1425.base.motorsubsystem.XeroEncoder;
import org.xero1425.misc.BadParameterTypeException;
import org.xero1425.misc.MessageLogger;
import org.xero1425.misc.MessageType;
import org.xero1425.misc.MissingParameterException;
import org.xero1425.misc.Speedometer;

public class SwerveDriveSubsystem extends DriveBase {
    private double dist_fl_ ;
    private double dist_fr_ ;
    private double dist_bl_ ;
    private double dist_br_ ;
    private double inches_per_tick_ ;

    private int ticks_fl_ ;
    private int ticks_fr_ ;
    private int ticks_bl_ ;
    private int ticks_br_ ;

    private Speedometer fl_linear_ ;
    private Speedometer fr_linear_ ;
    private Speedometer bl_linear_ ;
    private Speedometer br_linear_ ;

    private MotorController.NeutralMode automode_neutral_ ;
    private MotorController.NeutralMode teleop_neutral_ ;
    private MotorController.NeutralMode disabled_neutral_ ;

    private MotorController fl_drive_ ;
    private MotorController fl_steer_ ;
    private MotorController fr_drive_ ;
    private MotorController fr_steer_ ;
    private MotorController bl_drive_ ;
    private MotorController bl_steer_ ;
    private MotorController br_drive_ ;
    private MotorController br_steer_ ;

    private XeroEncoder fl_steer_angle_ ;
    private XeroEncoder fr_steer_angle_ ;
    private XeroEncoder bl_steer_angle_ ;
    private XeroEncoder br_steer_angle_ ;

    private double fl_drive_power_, fl_steer_power_ ;
    private double fr_drive_power_, fr_steer_power_ ;
    private double bl_drive_power_, bl_steer_power_ ;
    private double br_drive_power_, br_steer_power_ ;

    public class WheelData {
        public double fl ;
        public double fr ;
        public double bl ;
        public double br ;
    }

    public SwerveDriveSubsystem(Subsystem parent, String name, String config)  throws 
        BadParameterTypeException, MissingParameterException , EncoderConfigException, BadMotorRequestException {

        super(parent, name) ;

        dist_fl_ = 0.0 ;
        dist_fr_ = 0.0 ;
        dist_bl_ = 0.0 ;
        dist_br_ = 0.0 ;

        fl_drive_power_ = 0.0 ;
        fl_steer_power_ = 0.0 ;
        fr_drive_power_ = 0.0 ;
        fr_steer_power_ = 0.0 ;
        bl_drive_power_ = 0.0 ;
        bl_steer_power_ = 0.0 ;
        br_drive_power_ = 0.0 ;
        br_steer_power_ = 0.0 ;

        inches_per_tick_ = getRobot().getSettingsParser().get("swervedrive:inches_per_tick").getDouble() ;

        fl_linear_ = new Speedometer("fl", 2, true) ;
        fr_linear_ = new Speedometer("fr", 2, true) ;
        bl_linear_ = new Speedometer("bl", 2, true) ;
        br_linear_ = new Speedometer("br", 2, true) ;

        automode_neutral_ = MotorController.NeutralMode.Brake;
        teleop_neutral_ = MotorController.NeutralMode.Brake;
        disabled_neutral_ = MotorController.NeutralMode.Coast;

        attachHardware() ;
    }

    public WheelData getDistances() {
        WheelData ret = new WheelData() ;

        ret.fl = fl_linear_.getDistance() ;
        ret.fr = fr_linear_.getDistance() ;
        ret.bl = bl_linear_.getDistance() ;
        ret.br = br_linear_.getDistance() ;
        return ret ;
    }

    public double getVelocity() {
        return (fl_linear_.getVelocity() + fr_linear_.getVelocity() + 
                    bl_linear_.getVelocity() + br_linear_.getVelocity()) / 4.0 ;
    }

    public double getAcceleration() {
        return (fl_linear_.getAcceleration() + fr_linear_.getAcceleration() + 
                    bl_linear_.getAcceleration() + br_linear_.getAcceleration()) / 4.0 ;
    }

    public void reset() {
        super.reset();

        try {
            fl_drive_.setNeutralMode(disabled_neutral_);
            fl_steer_.setNeutralMode(disabled_neutral_);
            fr_drive_.setNeutralMode(disabled_neutral_);
            fr_steer_.setNeutralMode(disabled_neutral_);
            bl_drive_.setNeutralMode(disabled_neutral_);
            bl_steer_.setNeutralMode(disabled_neutral_);
            br_drive_.setNeutralMode(disabled_neutral_);
            br_steer_.setNeutralMode(disabled_neutral_);
        } catch (Exception ex) {
        }
    }

    public void init(LoopType ltype) {
        super.init(ltype);

        MotorController.NeutralMode nm = MotorController.NeutralMode.Brake ;
        switch (ltype) {
        case Autonomous:
            nm = automode_neutral_ ;
            break;

        case Teleop:
            nm = teleop_neutral_ ;
            break;

        case Test:
            nm = disabled_neutral_;
            break;

        case Disabled:
            nm = disabled_neutral_;        
            break ;
        }

        try {
            fl_drive_.setNeutralMode(nm);
            fl_steer_.setNeutralMode(nm);
            fr_drive_.setNeutralMode(nm);
            fr_steer_.setNeutralMode(nm);
            bl_drive_.setNeutralMode(nm);
            bl_steer_.setNeutralMode(nm);
            br_drive_.setNeutralMode(nm);
            br_steer_.setNeutralMode(nm);
        } catch (Exception ex) {
        }
    }

    public void run() throws Exception {
        super.run();
    }    

    public void computeMyState() {
        super.computeMyState();

        try {
            if (fl_drive_.hasPosition() && fr_drive_.hasPosition() && bl_drive_.hasPosition() && br_drive_.hasPosition()) {
                ticks_fl_ = (int)fl_drive_.getPosition();
                ticks_fr_ = (int)fr_drive_.getPosition();
                ticks_bl_ = (int)bl_drive_.getPosition();
                ticks_br_ = (int)br_drive_.getPosition();
            }

            dist_fl_ = ticks_fl_ * inches_per_tick_;
            dist_fr_ = ticks_fr_ * inches_per_tick_;
            dist_bl_ = ticks_bl_ * inches_per_tick_;
            dist_br_ = ticks_br_ * inches_per_tick_;

            fl_linear_.update(getRobot().getDeltaTime(), dist_fl_);
            fr_linear_.update(getRobot().getDeltaTime(), dist_fr_);
            bl_linear_.update(getRobot().getDeltaTime(), dist_bl_);
            br_linear_.update(getRobot().getDeltaTime(), dist_br_);                                    

        } catch (Exception ex) {
            //
            // This should never happen
            //
        }

        putDashboard("swfl", DisplayType.Verbose, fl_linear_.getDistance());
        putDashboard("swfr", DisplayType.Verbose, fr_linear_.getDistance());
        putDashboard("swbl", DisplayType.Verbose, bl_linear_.getDistance());
        putDashboard("swbr", DisplayType.Verbose, br_linear_.getDistance());                
        putDashboard("swangle", DisplayType.Verbose, getAngle()) ;

        MessageLogger logger = getRobot().getMessageLogger() ;
        logger.startMessage(MessageType.Debug, getLoggerID()) ;
        logger.add("swervedrive:") ;
        logger.add(" fldrive", fl_drive_power_).add(" flsteer", fl_steer_power_) ;
        logger.add(" frdrive", fr_drive_power_).add(" frsteer", fr_steer_power_) ;
        logger.add(" bldrive", bl_drive_power_).add(" blsteer", bl_steer_power_) ;
        logger.add(" brdrive", br_drive_power_).add(" brsteer", br_steer_power_) ;
        logger.add(" ticksfl", ticks_fl_).add(" ticks_fr ", ticks_fr_).add(" ticks_bl", ticks_bl_).add(" ticks_br", ticks_br_) ;
        logger.add(" distfl", dist_fl_).add(" dist_fr ", dist_fr_).add(" dist_bl", dist_bl_).add(" dist_br", dist_br_) ;
        logger.add(" velfl", fl_linear_.getVelocity()).add(" velfr", fr_linear_.getVelocity()).add(" velbl", bl_linear_.getVelocity()).add(" velbr", br_linear_.getVelocity()) ;
        logger.add(" speed", getVelocity()).add(" angle", getAngle()) ;
        logger.endMessage();
    }

    public WheelData getWheelAngles() {
        WheelData ret = new WheelData() ;

        ret.fl = fl_steer_angle_.getPosition() ;
        ret.fr = fr_steer_angle_.getPosition() ;
        ret.bl = bl_steer_angle_.getPosition() ;
        ret.br = br_steer_angle_.getPosition() ;
        return ret ;
    }

    protected void setPower(double fld, double fls, double frd, double frs, double bld, double bls, double brd, double brs) {
        fl_drive_power_ = fld ;
        fl_steer_power_ = fls ;
        fr_drive_power_ = frd ;
        fr_steer_power_ = frs ;
        bl_drive_power_ = bld ;
        bl_steer_power_ = bls ;
        br_drive_power_ = brd ;
        br_steer_power_ = brs ;

        try {
            fl_drive_.set(fl_drive_power_) ;
            fl_steer_.set(fl_steer_power_) ;
            fr_drive_.set(fr_drive_power_) ;
            fr_steer_.set(fr_steer_power_) ;
            bl_drive_.set(bl_drive_power_) ;
            bl_steer_.set(bl_steer_power_) ;
            br_drive_.set(br_drive_power_) ;
            br_steer_.set(br_steer_power_) ;
        }
        catch(BadMotorRequestException ex) {
            MessageLogger logger = getRobot().getMessageLogger() ;
            logger.startMessage(MessageType.Error) ;
            logger.add("subsystem ").addQuoted(getName()).add(": cannot set power -").add(ex.getMessage()).endMessage();
        }
    }

    private void attachHardware() throws BadParameterTypeException, MissingParameterException, EncoderConfigException, BadMotorRequestException {
        fl_drive_ = getRobot().getMotorFactory().createMotor("swervedrive:motors:fldrive", "hw:swervedrive:motors:fldrive") ;
        fl_steer_ = getRobot().getMotorFactory().createMotor("swervedrive:motors:flsteer", "hw:swervedrive:motors:flsteer") ;
        fr_drive_ = getRobot().getMotorFactory().createMotor("swervedrive:motors:frdrive", "hw:swervedrive:motors:frdrive") ;
        fr_steer_ = getRobot().getMotorFactory().createMotor("swervedrive:motors:frsteer", "hw:swervedrive:motors:frsteer") ;
        bl_drive_ = getRobot().getMotorFactory().createMotor("swervedrive:motors:bldrive", "hw:swervedrive:motors:bldrive") ;
        bl_steer_ = getRobot().getMotorFactory().createMotor("swervedrive:motors:blsteer", "hw:swervedrive:motors:blsteer") ;
        br_drive_ = getRobot().getMotorFactory().createMotor("swervedrive:motors:brdrive", "hw:swervedrive:motors:brdrive") ;
        br_steer_ = getRobot().getMotorFactory().createMotor("swervedrive:motors:brsteer", "hw:swervedrive:motors:brsteer") ;

        fl_steer_angle_ = new XeroEncoder(getRobot(), "swervedrive:encoder:flsteer", true, fl_steer_) ;
        fr_steer_angle_ = new XeroEncoder(getRobot(), "swervedrive:encoder:frsteer", true, fr_steer_) ;
        bl_steer_angle_ = new XeroEncoder(getRobot(), "swervedrive:encoder:blsteer", true, bl_steer_) ;
        br_steer_angle_ = new XeroEncoder(getRobot(), "swervedrive:encoder:brsteer", true, br_steer_) ;                        
    }
}
