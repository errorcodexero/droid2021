package org.frc2021.droid.climber;

import org.xero1425.base.Subsystem.DisplayType;
import org.xero1425.base.motorsubsystem.MotorEncoderGotoAction;
import org.xero1425.misc.BadParameterTypeException;
import org.xero1425.misc.MissingParameterException;

public class ClimberDeployAction extends MotorEncoderGotoAction {
    public ClimberDeployAction(LifterSubsystem lifter) throws BadParameterTypeException, MissingParameterException {
        super(lifter, "climb_height", true) ;
    }

    @Override
    public void start() throws Exception {
        getSubsystem().putDashboard("ClimberState", DisplayType.Always, "DEPLOYING");
        super.start() ;
    }

    @Override
    public String toString(int indent) {
        return prefix(indent) + "ClimberDeployAction" ;
    }

    @Override
    public void run() throws Exception {
        super.run() ;
    }
}
