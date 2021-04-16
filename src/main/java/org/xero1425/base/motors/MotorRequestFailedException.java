package org.xero1425.base.motors;

public class MotorRequestFailedException extends Exception {
    // The motor controller that had a failed request        
    private MotorController motor_ ;

    /// \brief The UID for the class for serialization
    static final long serialVersionUID = 42 ;

    /// \brief This method creates a new BadMotorRequestException
    /// \param motor the motor that causeed the exception
    /// \param msg a string describing the cause of the exception
    public MotorRequestFailedException(MotorController motor, String msg) {
        super("motor '" + motor.getName() + "' - " + msg) ;

        motor_ = motor ;
    }

    /// \brief Return the motor that caused the exception
    /// \returns the motor that caused the exception
    public MotorController getMotor() {
        return motor_ ;
    }
}
