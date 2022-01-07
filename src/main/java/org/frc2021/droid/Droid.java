/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package org.frc2021.droid;

import org.xero1425.simulator.engine.ModelFactory;
import org.xero1425.simulator.engine.SimulationEngine;
import org.frc2021.droid.automodes.DroidAutoController;
import org.frc2021.droid.droidsubsystem.DroidRobotSubsystem;
import org.xero1425.base.XeroRobot;
import org.xero1425.base.controllers.AutoController;
import org.xero1425.misc.BadParameterTypeException;
import org.xero1425.misc.MissingParameterException;
import org.xero1425.misc.SimArgs;
import org.xero1425.misc.XeroPathType;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the TimedRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Droid extends XeroRobot {
    static private byte[] practice_bot_mac_addr = new byte[] { 0x00, -128, 0x2F, 0x17, -119, -111 };

    Droid() {
        super(0.02);
    }

    public String getSimulationFileName() {
        String ret = SimArgs.InputFileName ;
        if (ret != null)
            return ret ;

        return "testmode" ;
    }

    protected void addRobotSimulationModels() {
        ModelFactory factory = SimulationEngine.getInstance().getModelFactory() ;
        factory.registerModel("conveyor", "org.frc2021.models.ConveyorModel");
        factory.registerModel("droidoi", "org.frc2021.models.DroidOIModel");
        factory.registerModel("intake", "org.frc2021.models.IntakeModel");
        factory.registerModel("shooter", "org.frc2021.models.ShooterModel");
        factory.registerModel("droid_limelight", "org.frc2021.models.DroidLimelightModel") ;
        factory.registerModel("turret", "org.frc2021.models.TurretModel") ;
        factory.registerModel("climber", "org.frc2021.models.ClimberModel");
    }

    public String getName() {
        return "droid";
    }

    protected AutoController createAutoController() throws MissingParameterException, BadParameterTypeException {
        return new DroidAutoController(this) ;
    }

    protected byte[] getPracticeBotMacAddress() {
        return practice_bot_mac_addr ;
    }

    protected void hardwareInit() throws Exception {
        //
        // We pulled the climber off for the at home challenges.  Set this to true to
        // enable support for the climber in the software.
        //
        boolean climber_attached = true ;

        DroidRobotSubsystem robotsub = new DroidRobotSubsystem(this, climber_attached) ;
        setRobotSubsystem(robotsub);
    }


    protected void loadPathsFile() throws Exception {
        super.loadPathsFile();
    }

    protected XeroPathType getPathType() {
        return XeroPathType.TankPathFollowing ;
    }
}
