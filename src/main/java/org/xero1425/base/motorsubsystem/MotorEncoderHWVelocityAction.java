package org.xero1425.base.motorsubsystem;

import org.xero1425.base.motors.BadMotorRequestException;
import org.xero1425.misc.BadParameterTypeException;
import org.xero1425.misc.MissingParameterException;

public class MotorEncoderHWVelocityAction extends MotorAction {

    private double target_;
    private boolean running_ ;

    public MotorEncoderHWVelocityAction(MotorEncoderSubsystem sub, double target)
            throws MissingParameterException, BadParameterTypeException {
        super(sub);

        target_ = target;
    }

    public void setTarget(double target) {

        target_ = target;

        MotorEncoderSubsystem sub = (MotorEncoderSubsystem)getSubsystem() ;
        double ticks_per_second = sub.toTicks(target) ;

        if (running_) {
            try {
                getSubsystem().getMotorController().setVelocity(ticks_per_second);
            } catch (BadMotorRequestException e) {
            }
        }
    }

    public double getTarget() {
        return target_ ;
    }

    @Override
    public void start() throws Exception {
        super.start() ;

        running_ = true ;
        setTarget(target_) ;
    }

    @Override
    public void run() throws Exception {
        super.run() ;
    }

    @Override
    public void cancel() {
        super.cancel() ;
        running_ = false ;
        getSubsystem().setPower(0.0);
    }

    @Override
    public String toString(int indent) {
        return prefix(indent) + "MotorEncoderHWVelocityAction, " + getSubsystem().getName() + ", " +  Double.toString(target_) ;
    }
}