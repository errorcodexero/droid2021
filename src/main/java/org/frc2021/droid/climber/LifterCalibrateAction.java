package org.frc2021.droid.climber;

import org.xero1425.base.Subsystem.DisplayType;
import org.xero1425.base.actions.Action;
import org.xero1425.misc.PIDCtrl;

public class LifterCalibrateAction extends Action {
    
    private enum State {
        DownSlowly,
        Holding
    } ;

    private LifterSubsystem sub_ ;
    private State state_ ;
    private double down_power_ ;
    private double threshold_ ;
    private double [] encoders_ ;
    private int samples_ ;
    private int captured_ ;
    private PIDCtrl pid_ ;

    public LifterCalibrateAction(LifterSubsystem lifter) throws Exception {
        super(lifter.getRobot().getMessageLogger());

        sub_ = lifter ;
        samples_ = lifter.getRobot().getSettingsParser().get("climber:lifter:calibrate:samples").getInteger() ;
        encoders_ = new double[samples_] ;
        down_power_ = lifter.getRobot().getSettingsParser().get("climber:lifter:calibrate:down_power").getDouble() ;
        if (down_power_ >= 0.0)
            throw new Exception("lifter calibrate down power must be negative") ;

        threshold_ = lifter.getRobot().getSettingsParser().get("climber:lifter:calibrate:threshold").getDouble() ;
        pid_ = new PIDCtrl(lifter.getRobot().getSettingsParser(), "climber:lifter:stay", false) ;
    }

    @Override
    public void start() throws Exception {
        super.start() ;

        if (sub_.isCalibarated()) {
            state_ = State.Holding ;
        }
        else {
            sub_.putDashboard("ClimberState", DisplayType.Always, "CALIBRATING");
            captured_ = 0 ;
            state_ = State.DownSlowly ;
            sub_.setPower(down_power_) ;
        }
    }

    @Override
    public void run() {
        switch(state_)
        {
            case DownSlowly:
                if (addEncoderPosition(sub_.getPosition())) {
                    sub_.setCalibrated();
                    state_ = State.Holding ;
                    sub_.putDashboard("ClimberState", DisplayType.Always, "HOLDING");
                }
                System.out.print("ENCODERS:") ;
                for(int i = 0 ; i < captured_ ; i++)
                {
                    System.out.print(" " + Double.toString(encoders_[i])) ;
                }
                System.out.println() ;
                break ;
            case Holding:
                double out = pid_.getOutput(0, sub_.getPosition(), sub_.getRobot().getDeltaTime()) ;
                sub_.setPower(out) ;
                break ;
        }
    }

    @Override
    public String toString(int indent) {
        return prefix(indent) + "LifterCalibrationAction" ;
    }

    private boolean checkForStopped() {
        double vmax = encoders_[0] ;
        double vmin = encoders_[0] ;

        for(int i = 1 ; i < samples_ ; i++)
        {
            if (encoders_[i] < vmin)
                vmin = encoders_[i] ;

            if (encoders_[i] > vmax)
                vmax = encoders_[i] ;
        }

        return vmax - vmin < threshold_ ;        
    }

    private boolean addEncoderPosition(double pos) {
        boolean ret = false ;

        if (captured_ == samples_) {
            for(int i = samples_ - 1 ; i > 0 ; i--)
                encoders_[i] = encoders_[i - 1] ;
            encoders_[0] = pos ;
            ret = checkForStopped() ;
        }
        else {
            encoders_[captured_++] = pos ;
        }

        return ret ;
    }
}