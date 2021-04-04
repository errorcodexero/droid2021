package org.frc2021.droid.gamepiecemanipulator.shooter;

import com.revrobotics.CANEncoder;
import com.revrobotics.CANPIDController;
import com.revrobotics.ControlType;
import org.xero1425.base.actions.Action;
import org.xero1425.misc.BadParameterTypeException;
import org.xero1425.misc.MessageLogger;
import org.xero1425.misc.MessageType;
import org.xero1425.misc.MissingParameterException;
import org.xero1425.misc.SettingsParser;
import org.xero1425.misc.XeroMath;

public class ShooterVelocityAction extends Action {
    private ShooterSubsystem sub_ ;
    private double ready_percent_ ;
    private ShooterSubsystem.HoodPosition pos_ ;
    private boolean setready_ ;
    private double target_ ;
    private double kp_ ;
    private double ki_ ;
    private double kd_ ;
    private double kf_ ;
    private double kmin_ ;
    private double kmax_ ;

    public ShooterVelocityAction(ShooterSubsystem shooter, double target, ShooterSubsystem.HoodPosition pos, boolean setready)
            throws BadParameterTypeException, MissingParameterException {
        super(shooter.getRobot().getMessageLogger()) ;

        SettingsParser sp = shooter.getRobot().getSettingsParser() ;

        kp_ = sp.get("shooter:velocity:kp").getDouble() ;
        ki_ = sp.get("shooter:velocity:ki").getDouble() ;
        kd_ = sp.get("shooter:velocity:kd").getDouble() ;
        kf_ = sp.get("shooter:velocity:kf").getDouble() ;
        kmin_ = sp.get("shooter:velocity:min").getDouble() ;
        kmax_ = sp.get("shooter:velocity:max").getDouble() ;

        ready_percent_ = sp.get("shooter:velocity:ready_margin_percent").getDouble() ;
        pos_ = pos ;
        sub_ = shooter ;
        setready_ = setready ;
    }

    public void setTarget(double target) {
        target_ = target ;
        CANPIDController pid = sub_.getRawPIDController() ;
        pid.setReference(target, ControlType.kVelocity) ;
        updateReadyToFire() ;
    }

    public void setHoodPosition(ShooterSubsystem.HoodPosition pos) {
        pos_ = pos ;
    }

    @Override
    public void start() throws Exception {
        super.start() ;

        CANPIDController pid = sub_.getRawPIDController() ;
        pid.setP(kp_) ;
        pid.setI(ki_) ;
        pid.setD(kd_) ;
        pid.setFF(kf_) ;
        pid.setOutputRange(kmin_, kmax_) ;

        pid.setReference(target_, ControlType.kVelocity) ;

        updateReadyToFire() ;
    }

    @Override
    public void run() throws Exception {
        super.run() ;

        if (sub_.getHood() != pos_)
            sub_.setHood(pos_);

        updateReadyToFire() ;

        MessageLogger logger = sub_.getRobot().getMessageLogger() ;
        logger.startMessage(MessageType.Debug, sub_.getLoggerID()) ;
        logger.add("shooter velocity:") ;
        logger.add("target", target_) ;
        logger.add("actual", getRawVelocity()) ;
        logger.add("ready", sub_.isReadyToFire()) ;
        logger.endMessage();
    }

    private double getRawVelocity() {
        CANEncoder encoder = sub_.getRawEncoder() ;
        return encoder.getVelocity() ;
    }

    @Override
    public void cancel() {
        super.cancel() ;
        sub_.getRawMotorController().stopMotor();
    }

    @Override
    public String toString(int indent) {
        String ret = prefix(indent) + "ShooterVelocityAction" ;
        ret += " target = " +  Double.toString(target_) ;
        if (pos_ == ShooterSubsystem.HoodPosition.Up)
            ret += " Up" ;
        else
            ret += " Down" ;
        return ret ;
    }

    @Override
    public String toString() {
        String ret = "ShooterVelocity " + Double.toString(target_) ;
        return ret ;
    }

    private void updateReadyToFire() {
        if (setready_ == false)
        {
            sub_.setReadyToFire(false);
        }
        else
        {
            if (XeroMath.equalWithinPercentMargin(target_, sub_.getVelocity(), ready_percent_))
                sub_.setReadyToFire(true);
            else
                sub_.setReadyToFire(false);
        }
    }
}
