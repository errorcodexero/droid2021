package org.xero1425.base.motors;

import com.revrobotics.CANEncoder;
import com.revrobotics.CANPIDController;
import com.revrobotics.CANSparkMax;
import com.revrobotics.ControlType;
import com.revrobotics.CANSparkMax.IdleMode;

import edu.wpi.first.hal.SimBoolean;
import edu.wpi.first.hal.SimDevice;
import edu.wpi.first.hal.SimDouble;
import edu.wpi.first.wpilibj.RobotBase;

public class SparkMaxMotorController extends MotorController
{
    private CANSparkMax controller_ ;
    private CANEncoder encoder_ ;
    private boolean inverted_ ;
    private boolean brushless_ ;
    private CANPIDController pid_ ;
    private PidType ptype_ ;

    private SimDevice sim_ ;
    private SimDouble sim_power_ ;
    private SimDouble sim_encoder_ ;
    private SimBoolean sim_motor_inverted_ ;
    private SimBoolean sim_neutral_mode_ ;

    public final static String SimDeviceNameBrushed = "SparkMaxBrushed" ;
    public final static String SimDeviceNameBrushless = "SparkMaxBrushless" ;
    public final static int TicksPerRevolution = 42 ;

    public SparkMaxMotorController(String name, int index, boolean brushless) {
        super(name) ;

        inverted_ = false ;
        brushless_ = brushless ;
        pid_ = null ;

        if (RobotBase.isSimulation()) {
            if (brushless)
                sim_ = SimDevice.create(SimDeviceNameBrushless, index) ;
            else
                sim_ = SimDevice.create(SimDeviceNameBrushed, index) ;

            sim_power_ = sim_.createDouble(MotorController.SimPowerParamName, SimDevice.Direction.kBidir, 0.0) ;
            sim_encoder_ = sim_.createDouble(MotorController.SimEncoderParamName, SimDevice.Direction.kBidir, 0.0) ;
            sim_motor_inverted_ = sim_.createBoolean(MotorController.SimInvertedParamName, SimDevice.Direction.kBidir, false) ;
            sim_neutral_mode_ = sim_.createBoolean(MotorController.SimNeutralParamName, SimDevice.Direction.kBidir, false) ;  
            sim_.createBoolean(MotorController.SimEncoderStoresTicksParamName, SimDevice.Direction.kBidir, false) ;            
        }
        else {
            if (brushless)
            {
                controller_ = new CANSparkMax(index, CANSparkMax.MotorType.kBrushless) ;
            }
            else
            {
                controller_ = new CANSparkMax(index, CANSparkMax.MotorType.kBrushed) ;
            }

            controller_.restoreFactoryDefaults() ;
            controller_.enableVoltageCompensation(12.0) ;
            encoder_ = controller_.getEncoder() ;
        }
    }

    public String typeName() {
        String ret = "SparkMaxBrushed" ;

        if (brushless_)
            ret = "SparkMaxBrushless" ;

        return ret ;
    }

    public double getVoltage() throws BadMotorRequestException {
        return controller_.getBusVoltage() ;
    }

    public boolean hasPID() throws BadMotorRequestException {
        return false ;
    }

    public void setTarget(double target) throws BadMotorRequestException {
        if (pid_ == null)
            throw new BadMotorRequestException(this, "called setTarget() before calling setPID()") ;

        if (ptype_ == PidType.Position)
            pid_.setReference(target, ControlType.kPosition) ;
        else if (ptype_ == PidType.Velocity)
            pid_.setReference(target, ControlType.kVelocity) ;
    }

    public void setPID(PidType type, double p, double i, double d, double f, double outmin, double outmax) throws BadMotorRequestException {
        if (pid_ == null)
            pid_ = controller_.getPIDController() ;

        pid_.setP(p) ;
        pid_.setI(i) ;
        pid_.setD(d) ;
        pid_.setFF(f) ;
        pid_.setOutputRange(outmin, outmax) ;
        ptype_ = type ;
    }

    public void stopPID() throws BadMotorRequestException {
    }

    public void setPositionConversion(double factor) throws BadMotorRequestException {
        encoder_.setPositionConversionFactor(factor) ;
    }

    public void setVelocityConversion(double factor) throws BadMotorRequestException {
        encoder_.setVelocityConversionFactor(factor) ;
    }

    public void set(double percent) {
        if (sim_ != null) {
            sim_power_.set(percent) ;
        } else {
            controller_.set(percent) ;
        }
    }

    public void setInverted(boolean inverted) {
        if (sim_ != null) {
            sim_motor_inverted_.set(inverted) ;
        } else {
            controller_.setInverted(inverted);
        }

        inverted_ = inverted ;
    }

    public boolean isInverted() {
        return inverted_ ;
    }    

    public void reapplyInverted() {
        if (sim_ != null) {
            sim_motor_inverted_.set(inverted_) ;
        } else {
            controller_.setInverted(inverted_);
        }
    }

    public void setNeutralMode(NeutralMode mode) throws BadMotorRequestException {
        if (sim_ != null) {
            switch(mode)
            {
                case Coast:
                    sim_neutral_mode_.set(false) ;
                    break ;

                case Brake:
                    sim_neutral_mode_.set(true) ;
                    break ;
            }
        }
        else {
            switch(mode)
            {
                case Coast:
                    controller_.setIdleMode(IdleMode.kCoast) ;
                    break ;

                case Brake:
                    controller_.setIdleMode(IdleMode.kBrake) ;
                break ;
            }
        }
    }

    public void follow(MotorController ctrl, boolean invert) throws BadMotorRequestException {
        if (sim_ == null) {
            try {
                SparkMaxMotorController other = (SparkMaxMotorController)ctrl ;
                controller_.follow(other.controller_, invert) ;
            }
            catch(ClassCastException ex)
            {
                throw new BadMotorRequestException(this, "cannot follow a motor that is of another type") ;
            }
        }
    }

    public String getType() {
        String ret = null ;

        if (brushless_)
        {
            ret = "SparkMax:brushless" ;
        }
        else
        {
            ret = "SparkMax:brushed" ;
        }

        return ret ;
    }

    public boolean hasPosition() {
        return brushless_ ;
    }

    public double getPosition() throws BadMotorRequestException {
        double ret = 0 ;

        if (!brushless_)
            throw new BadMotorRequestException(this, "brushed motor does not support getPosition()") ;

        if (sim_ != null) {
            ret = sim_encoder_.get() * (double)TicksPerRevolution ;
        } else {
            ret = encoder_.getPosition() * TicksPerRevolution ;
        }

        return ret ;
    }

    public void resetEncoder() throws BadMotorRequestException {
        if (!brushless_)
            throw new BadMotorRequestException(this, "brushed motor does not support getPosition()") ;

        if (sim_ != null) {
            sim_encoder_.set(0.0) ;
        }
        else {
            encoder_.setPosition(0.0) ;
        }
    }

    public void setCurrentLimit(double limit) throws BadMotorRequestException {
        if (sim_ == null) {
            controller_.setSmartCurrentLimit((int)limit) ;
        }
    }      

    public void setOpenLoopRampRate(double limit) throws BadMotorRequestException {
        if (sim_ == null) {
            controller_.setOpenLoopRampRate(limit) ;
        }
    } 

} ;