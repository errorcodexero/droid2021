package org.xero1425.base.swervedrive;

import org.xero1425.base.DriveBase;
import org.xero1425.base.LoopType;
import org.xero1425.base.Subsystem;
import org.xero1425.base.XeroRobot;
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
    
    private class PerWheelData {
        private int ticks_ ;
        private double distance_ ;
        private double drive_power_ ;
        private double steer_power_ ;
        private Speedometer linear_ ;
        private XeroEncoder steer_angle_ ;
        private MotorController drive_ ;
        private MotorController steer_ ;
        private String name_ ;

        public PerWheelData(XeroRobot robot, int which, String name) 
                throws BadParameterTypeException, MissingParameterException, EncoderConfigException, BadMotorRequestException {
            name_ = name ;
            ticks_ = 0 ;
            drive_power_ = 0.0 ;
            steer_power_ = 0.0 ;
            linear_ = new Speedometer("swerve" + String.valueOf(which), 2, false) ;
            drive_ = getRobot().getMotorFactory().createMotor("swervedrive:motors:" + name + "drive", "hw:swervedrive:motors:" + name + "drive") ;
            steer_ = getRobot().getMotorFactory().createMotor("swervedrive:motors:" + name + "steer", "hw:swervedrive:motors:" + name + "steer") ;
            steer_angle_ = new XeroEncoder(getRobot(), "swervedrive:encoder:" + name + "steer", true, steer_) ;
        }

        public String name() {
            return name_ ;
        }

        public int getTicks() {
            return ticks_ ;
        }

        public double getDistance() {
            return distance_ ;
        }

        public double getVelocity() {
            return linear_.getVelocity() ;
        }

        public double getAcceleration() {
            return linear_.getAcceleration() ;
        }

        public double getDrivePower() {
            return drive_power_ ;
        }

        public void setDrivePower(double d) throws BadMotorRequestException {
            drive_.set(d) ;
            drive_power_ = d ;
        }

        public double getSteerPower() {
            return steer_power_ ;
        }

        public void setSteerPower(double d) throws BadMotorRequestException  {
            steer_.set(d) ;
            steer_power_ = d ;
        }

        public double getWheelAngle() {
            return steer_angle_.getPosition() ;
        }

        public MotorController driveController() {
            return drive_ ;
        }

        public MotorController steerController() {
            return steer_ ;
        }

        public void computeMyState() throws BadMotorRequestException {
            ticks_ = (int)drive_.getPosition() ;
            distance_ = ticks_ * inches_per_tick_ ;
            linear_.update(getRobot().getDeltaTime(), distance_);
        }
    }

    private double inches_per_tick_ ;

    private MotorController.NeutralMode automode_neutral_ ;
    private MotorController.NeutralMode teleop_neutral_ ;
    private MotorController.NeutralMode disabled_neutral_ ;

    private PerWheelData wheels_[] ;

    public final static String[] WheelNames = { "fl" , "fr", "bl", "br"} ;


    public SwerveDriveSubsystem(Subsystem parent, String name, String config)  throws 
        BadParameterTypeException, MissingParameterException , EncoderConfigException, BadMotorRequestException {

        super(parent, name) ;

        inches_per_tick_ = getRobot().getSettingsParser().get("swervedrive:inches_per_tick").getDouble() ;

        wheels_ = new PerWheelData[4] ;

        for(int i = 0 ; i < wheels_.length ; i++)
        {
            wheels_[i] = new PerWheelData(getRobot(), i, WheelNames[i]) ;
        }

        automode_neutral_ = MotorController.NeutralMode.Brake;
        teleop_neutral_ = MotorController.NeutralMode.Brake;
        disabled_neutral_ = MotorController.NeutralMode.Coast;
    }

    public int getWheelCount() {
        return wheels_.length ;
    }

    public String getWheelName(int index) {
        return wheels_[index].name() ;
    }

    public double[] getDistances() {
        double ret[] = new double[wheels_.length] ;

        for(int i = 0 ; i < wheels_.length ; i++)
        {
            ret[i] = wheels_[i].getDistance() ;
        }
        return ret ;
    }

    public double[] getVelocities() {
        double ret[] = new double[wheels_.length] ;

        for(int i = 0 ; i < wheels_.length ; i++)
        {
            ret[i] = wheels_[i].getVelocity() ;
        }
        return ret ;
    }

    public double[] getAccelerations() {
        double ret[] = new double[wheels_.length] ;

        for(int i = 0 ; i < wheels_.length ; i++)
        {
            ret[i] = wheels_[i].getAcceleration() ;
        }
        return ret ;
    }

    public double getVelocity() {
        double ret = 0.0 ;

        for(int i = 0 ; i < wheels_.length ; i++)
        {
            ret += wheels_[i].getVelocity() ;
        }
        return ret / (double)wheels_.length;
    }

    public double getAcceleration() {
        double ret = 0.0 ;

        for(int i = 0 ; i < wheels_.length ; i++)
        {
            ret += wheels_[i].getAcceleration() ;
        }
        return ret / (double)wheels_.length ;
    }

    public void reset() {
        super.reset();

        for(int i = 0 ; i < wheels_.length ; i++)
        {
            try {
                wheels_[i].driveController().setNeutralMode(disabled_neutral_) ;
                wheels_[i].steerController().setNeutralMode(disabled_neutral_) ;
            } catch (Exception ex) {
            }
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

        for(int i = 0 ; i < wheels_.length ; i++)
        {
            try {
                wheels_[i].driveController().setNeutralMode(nm) ;
                wheels_[i].steerController().setNeutralMode(nm) ;
            } catch (Exception ex) {
            }
        }
    }

    public void run() throws Exception {
        super.run();
    }    

    public void computeMyState() {
        super.computeMyState();

        try {
            for(int i = 0 ; i < wheels_.length ; i++) {
                wheels_[i].computeMyState() ;
                putDashboard("sw" + wheels_[i].name(), DisplayType.Verbose, wheels_[i].getDistance()) ;
            }

        } catch (Exception ex) {
            //
            // This should never happen
            //
        }

        putDashboard("swangle", DisplayType.Verbose, getAngle()) ;

        MessageLogger logger = getRobot().getMessageLogger() ;
        logger.startMessage(MessageType.Debug, getLoggerID()) ;
        logger.add("swervedrive:") ;
        for(int i = 0 ; i < wheels_.length ; i++) {
            logger.add(wheels_[i].name() + "-drive", wheels_[i].getDrivePower()) ;
            logger.add(wheels_[i].name() + "-steer", wheels_[i].getSteerPower()) ;
            logger.add(wheels_[i].name() + "-ticks", wheels_[i].getTicks());
            logger.add(wheels_[i].name() + "-dist", wheels_[i].getDistance()) ;
            logger.add(wheels_[i].name() + "-vel", wheels_[i].getVelocity()) ;
            logger.add(wheels_[i].name() + "-acc", wheels_[i].getAcceleration()) ;
        }
        logger.add(" speed", getVelocity()).add(" angle", getAngle()) ;
        logger.endMessage();
    }

    public double[] getWheelAngles() throws BadMotorRequestException {
        double ret[] = new double[wheels_.length] ;

        for(int i = 0 ; i < wheels_.length ; i++)
        {
            ret[i] = wheels_[i].getWheelAngle() ;
        }
        return ret ;
    }

    public void stop() {
        double [] drive = new double[] { 0.0, 0.0, 0.0,0.0} ;
        setPower(drive, drive) ;
    }

    public void setPower(double drive[], double steer[]) {
        if (drive.length != wheels_.length)
        {
            MessageLogger logger = getRobot().getMessageLogger() ;
            logger.startMessage(MessageType.Error) ;
            logger.add("subsystem ").addQuoted(getName()).add(": cannot set power - drive array size is not four") ;
        }

        if (steer.length != wheels_.length)
        {
            MessageLogger logger = getRobot().getMessageLogger() ;
            logger.startMessage(MessageType.Error) ;
            logger.add("subsystem ").addQuoted(getName()).add(": cannot set power - steer array size is not four") ;
        }

        try {
            for(int i = 0 ; i < wheels_.length ; i++)
            {   
                wheels_[i].setDrivePower(drive[i]);
                wheels_[i].setSteerPower(drive[i]);
            }
        }
        catch(BadMotorRequestException ex) {
                MessageLogger logger = getRobot().getMessageLogger() ;
                logger.startMessage(MessageType.Error) ;
                logger.add("subsystem ").addQuoted(getName()).add(": cannot set power -").add(ex.getMessage()).endMessage();
        }
    }
}
