package org.frc2021.droid.climber;

import org.xero1425.base.Subsystem;
import org.xero1425.base.motorsubsystem.MotorEncoderSubsystem;

public class LifterSubsystem extends MotorEncoderSubsystem {
    public LifterSubsystem(Subsystem parent, String name) throws Exception {
        super(parent, name, false);

        low_power_limit_ = getSettingsValue("low_power_limit").getDouble() ;
        low_power_height_ = getSettingsValue("low_power_height").getDouble() ;
        calibrated_ = false ;
    }

    public boolean isCalibarated() {
        return calibrated_ ;
    }

    @Override
    public void computeMyState() throws Exception {
        super.computeMyState(); 

        if (getAction() == null)
            putDashboard("ClimberState", DisplayType.Always, "IDLE") ;
    }

    protected void setCalibrated() {
        calibrated_ = true ;
        reset() ;
    }

    protected void setPower(double power) {
        ClimberSubsystem climber = (ClimberSubsystem)getParent() ;

        if (power < 0.0) {
            //
            // We are going down
            //
            if (climber.isInFieldMode())
            {
                if (getPosition() < 0)
                {
                    //
                    // We are at the bottom, do not go any further
                    //
                    power = 0 ;
                }
                else if (getPosition() < low_power_height_)
                {
                    if (Math.abs(power) > low_power_limit_)
                        power = Math.signum(power) * low_power_limit_ ;
                }
            }
            else
            {
                //
                // We are in the PIT mode here
                //
                if (Math.abs(power) > low_power_limit_)
                    power = Math.signum(power) * low_power_limit_ ;
            }
        }
        else
        {
            //
            // We are going up
            //
            if (getPosition() > climber.getMaxHeight())
                power = 0 ;
        }

        super.setPower(power) ;
    }

    private double low_power_height_ ;
    private double low_power_limit_ ;
    private boolean calibrated_ ;
}