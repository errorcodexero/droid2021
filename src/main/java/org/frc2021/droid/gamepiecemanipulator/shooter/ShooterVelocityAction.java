package org.frc2021.droid.gamepiecemanipulator.shooter;

import org.xero1425.base.Subsystem.DisplayType;
import org.xero1425.base.motors.BadMotorRequestException;
import org.xero1425.base.motors.MotorRequestFailedException;
import org.xero1425.base.motorsubsystem.MotorEncoderSubsystem;
import org.xero1425.base.motorsubsystem.MotorEncoderVelocityAction;
import org.xero1425.misc.BadParameterTypeException;
import org.xero1425.misc.MessageLogger;
import org.xero1425.misc.MessageType;
import org.xero1425.misc.MissingParameterException;
import org.xero1425.misc.XeroMath;

public class ShooterVelocityAction extends MotorEncoderVelocityAction {
    private ShooterSubsystem sub_ ;
    private double ready_percent_ ;
    private ShooterSubsystem.HoodPosition pos_ ;
    private boolean setready_ ;

    public ShooterVelocityAction(ShooterSubsystem shooter, double target, ShooterSubsystem.HoodPosition pos, boolean setready)
            throws BadParameterTypeException, MissingParameterException, BadMotorRequestException {
        super(shooter, target) ;

        ready_percent_ = shooter.getRobot().getSettingsParser().get("shooter:velocity:ready_margin_percent").getDouble() ;
        pos_ = pos ;
        sub_ = shooter ;
        setready_ = setready ;
    }

    @Override
    public void setTarget(double target) throws BadMotorRequestException, MotorRequestFailedException {
        super.setTarget(target) ;
        updateReadyToFire() ;
    }

    public void setHoodPosition(ShooterSubsystem.HoodPosition pos) {
        pos_ = pos ;
    }

    @Override
    public void start() throws Exception {
        super.start() ;
    }

    @Override
    public void run() throws Exception {
        super.run() ;
        if (sub_.getHood() != pos_)
            sub_.setHood(pos_);

        updateReadyToFire() ;

        MotorEncoderSubsystem me = (MotorEncoderSubsystem)getSubsystem() ;
        MessageLogger logger = getSubsystem().getRobot().getMessageLogger() ;
        logger.startMessage(MessageType.Debug, getSubsystem().getLoggerID()) ;
        logger.add("shooter velocity:") ;
        logger.add("target", getTarget()) ;
        logger.add("actual", me.getVelocity()) ;
        logger.add("ready", sub_.isReadyToFire()) ;
        logger.add("inputV", sub_.getMotorController().getInputVoltage()) ;
        logger.add("appliedV", sub_.getMotorController().getAppliedVoltage()) ;
        logger.endMessage();

        sub_.putDashboard("sh-target", DisplayType.Verbose, getTarget());
        sub_.putDashboard("sh-actual", DisplayType.Verbose, me.getVelocity());
    }

    @Override
    public void cancel() {
        super.cancel() ;
    }

    @Override
    public String toString(int indent) {
        String ret = prefix(indent) + "ShooterVelocityAction" ;
        ret += " target = " +  Double.toString(getTarget()) ;
        if (pos_ == ShooterSubsystem.HoodPosition.Up)
            ret += " Up" ;
        else
            ret += " Down" ;
        return ret ;
    }

    @Override
    public String toString() {
        String ret = "ShooterVelocity " + Double.toString(getTarget()) ;
        return ret ;
    }

    private void updateReadyToFire() {
        if (setready_ == false)
        {
            sub_.setReadyToFire(false);
        }
        else
        {
            if (XeroMath.equalWithinPercentMargin(getTarget(), sub_.getVelocity(), ready_percent_))
                sub_.setReadyToFire(true);
            else
                sub_.setReadyToFire(false);
        }
    }
}