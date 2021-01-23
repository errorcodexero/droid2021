package org.frc2020.droid.gamepiecemanipulator.intake;

import org.xero1425.base.Subsystem;
import org.xero1425.base.motors.MotorController;
import org.xero1425.base.motorsubsystem.MotorEncoderSubsystem ;
import org.xero1425.base.motors.BadMotorRequestException ;

public class IntakeSubsystem extends MotorEncoderSubsystem {
    public static final String SubsystemName = "intake" ;

    private MotorController collector_ ;

    public IntakeSubsystem(Subsystem parent) throws Exception {
        super(parent, SubsystemName, false) ;                       // Motor 1, in the base class

        // Motor 2, explicitly create it
        collector_ = getRobot().getMotorFactory().createMotor("intake-collector", "hw:intake:collector:motor") ;
    }

    public void setCollectorPower(double p) throws BadMotorRequestException {
        collector_.set(p) ;
    }
}
