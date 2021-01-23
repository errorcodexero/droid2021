package org.frc2020.droid.automodes;

import org.frc2020.droid.climber.ClimberMoveAction;
import org.frc2020.droid.climber.ClimberSubsystem;
import org.frc2020.droid.droidlimelight.DroidLimeLightSubsystem;
import org.frc2020.droid.droidsubsystem.DroidRobotSubsystem;
import org.frc2020.droid.gamepiecemanipulator.FireAction;
import org.frc2020.droid.gamepiecemanipulator.GamePieceManipulatorSubsystem;
import org.frc2020.droid.gamepiecemanipulator.ShootTestingAction;
import org.frc2020.droid.gamepiecemanipulator.conveyor.ConveyorEmitAction;
import org.frc2020.droid.gamepiecemanipulator.conveyor.ConveyorOnAction;
import org.frc2020.droid.gamepiecemanipulator.conveyor.ConveyorPrepareToEmitAction;
import org.frc2020.droid.gamepiecemanipulator.conveyor.ConveyorPrepareToReceiveAction;
import org.frc2020.droid.gamepiecemanipulator.conveyor.ConveyorReceiveAction;
import org.frc2020.droid.gamepiecemanipulator.conveyor.ConveyorStopAction;
import org.frc2020.droid.gamepiecemanipulator.conveyor.ConveyorSubsystem;
import org.frc2020.droid.gamepiecemanipulator.intake.CollectOffAction;
import org.frc2020.droid.gamepiecemanipulator.intake.CollectOnAction;
import org.frc2020.droid.gamepiecemanipulator.intake.IntakeSubsystem;
import org.frc2020.droid.gamepiecemanipulator.shooter.ShooterSubsystem;
import org.frc2020.droid.gamepiecemanipulator.shooter.ShooterVelocityAction;
import org.frc2020.droid.gamepiecemanipulator.shooter.ShooterSubsystem.HoodPosition;
import org.frc2020.droid.targettracker.TargetTrackerSubsystem;
import org.frc2020.droid.turret.FollowTargetAction;
import org.frc2020.droid.turret.TurretSubsystem;
import org.xero1425.base.actions.DelayAction;
import org.xero1425.base.actions.ParallelAction;
import org.xero1425.base.actions.SequenceAction;
import org.xero1425.base.controllers.TestAutoMode;
import org.xero1425.base.motorsubsystem.MotorEncoderGotoAction;
import org.xero1425.base.motorsubsystem.MotorEncoderPowerAction;
import org.xero1425.base.motorsubsystem.MotorEncoderVelocityAction;
import org.xero1425.base.tankdrive.TankDriveFollowPathAction;
import org.xero1425.base.tankdrive.TankDrivePowerAction;
import org.xero1425.base.tankdrive.TankDriveScrubCharAction;
import org.xero1425.base.tankdrive.TankDriveSubsystem;

public class DroidTestAutoMode extends TestAutoMode {

    public DroidTestAutoMode(DroidAutoController ctrl)
            throws Exception {
        super(ctrl, "TestMode") ;

        ParallelAction parallel ;
        SequenceAction seq ;
        
        DroidRobotSubsystem droid = (DroidRobotSubsystem)ctrl.getRobot().getRobotSubsystem() ;
        TankDriveSubsystem db = droid.getTankDrive() ;
        GamePieceManipulatorSubsystem gp = droid.getGamePieceManipulator() ;
        IntakeSubsystem intake = droid.getGamePieceManipulator().getIntake() ;
        ConveyorSubsystem conveyor = droid.getGamePieceManipulator().getConveyor() ;
        ShooterSubsystem shooter = droid.getGamePieceManipulator().getShooter() ;
        TurretSubsystem turret = droid.getTurret() ;
        DroidLimeLightSubsystem ll = droid.getLimeLight() ;
        ClimberSubsystem climber = droid.getClimber() ;
        TargetTrackerSubsystem tracker = droid.getTracker() ;        

        switch(getTestNumber()) {
            //
            // Numbers 0 - 9 are for the driverbase
            //
            case 0:         // Drive straight, used to test and get Kv number
                addSubActionPair(db, new TankDrivePowerAction(db, getPower(), getPower(), getDuration()), true);
                break ;

            case 1:         // Drive curve to the right
                addSubActionPair(db, new TankDrivePowerAction(db, getPower(), getPower() / 2, getDuration()), true);
                break ;

            case 2:         // Drive curve to the left
                addSubActionPair(db, new TankDrivePowerAction(db, getPower() / 2, getPower(), getDuration()), true);
                break ;

            case 3:         // Rotate robot to get scrub factor
                addSubActionPair(db, new TankDriveScrubCharAction(db, getPower(), getPosition()), true);
                break ; 

            case 4:         // Run the path follower to follow a named path
                addSubActionPair(db, new TankDriveFollowPathAction(db, getNameParam(), false), true) ;
                break ;

            //
            // Numbers 10 - 19 are for the intake
            //
            case 10:        // Calculate the Kv number
                addSubActionPair(intake, new MotorEncoderPowerAction(intake, getPower(), getDuration()), true);
                break ;

            case 11:        // Test to goto position action
                addSubActionPair(intake, new MotorEncoderGotoAction(intake, getPosition(), true), true) ;
                break ;                

            case 12:        // Test the high level collect on and collect off action
                addSubActionPair(intake, new CollectOnAction(intake), true);
                addAction(new DelayAction(ctrl.getRobot(), 3.0));
                addSubActionPair(intake, new CollectOffAction(intake), true);
                break ;

            //
            // Numbers 20 - 29 are for the conveyor
            //
            case 20:            // Run intake side of conveyor in forward direction
                addSubActionPair(conveyor, new ConveyorOnAction(conveyor, 1.0, 0.0), true);
                addAction(new DelayAction(ctrl.getRobot(), 3.0));
                addSubActionPair(conveyor, new ConveyorOnAction(conveyor, 0.0, 0.0), true);
                break ;

            case 21:            // Run intake side of conveyor in backward direction
                addSubActionPair(conveyor, new ConveyorOnAction(conveyor, -1.0, 0.0), true);
                addAction(new DelayAction(ctrl.getRobot(), 3.0));                
                addSubActionPair(conveyor, new ConveyorOnAction(conveyor, 0.0, 0.0), true);
                break ;

            case 22:            // Run shooter side of conveyor in forward direction
                addSubActionPair(conveyor, new ConveyorOnAction(conveyor, 0.0, 1.0), true);                
                addAction(new DelayAction(ctrl.getRobot(), 3.0));                
                addSubActionPair(conveyor, new ConveyorOnAction(conveyor, 0.0, 0.0), true);
                break ;

            case 23:            // Run shooter side of conveyor in backward direction
                addSubActionPair(conveyor, new ConveyorOnAction(conveyor, 0.0, -1.0), true);
                addAction(new DelayAction(ctrl.getRobot(), 3.0));                
                addSubActionPair(conveyor, new ConveyorOnAction(conveyor, 0.0, 0.0), true);
                break ;

            case 24:            // Run shooter and intake side of conveyor in forward direction
                addSubActionPair(conveyor, new ConveyorOnAction(conveyor, 1.0, 1.0), true);
                addAction(new DelayAction(ctrl.getRobot(), 10.0));                
                addSubActionPair(conveyor, new ConveyorOnAction(conveyor, 0.0, 0.0), true);
                break ;

            case 25:            // Run shooter and intake side of conveyor in backward direction
                addSubActionPair(conveyor, new ConveyorOnAction(conveyor, -1.0, -1.0), true);
                addAction(new DelayAction(ctrl.getRobot(), 3.0));                
                addSubActionPair(conveyor, new ConveyorOnAction(conveyor, 0.0, 0.0), true);
                break ;

            case 26:            // Test collect path
                parallel = new ParallelAction(ctrl.getRobot().getMessageLogger(), ParallelAction.DonePolicy.All) ;
                addAction(parallel);
                parallel.addSubActionPair(intake, new CollectOnAction(intake), false);
                seq = new SequenceAction(ctrl.getRobot().getMessageLogger()) ;
                parallel.addAction(seq) ;
                seq.addSubActionPair(conveyor, new ConveyorPrepareToReceiveAction(conveyor), true);
                seq.addSubActionPair(conveyor, new ConveyorReceiveAction(conveyor), true);
                break ;

            case 27:            // Test shoot path
                addSubActionPair(conveyor, new ConveyorPrepareToEmitAction(conveyor), true);
                addSubActionPair(conveyor, new ConveyorEmitAction(conveyor), true);
                addAction(new DelayAction(ctrl.getRobot(), 3.0));
                addSubActionPair(conveyor, new ConveyorStopAction(conveyor), true);
                break ;

            //
            // Numbers 30 - 39 are for the shooter
            //
            case 30:            // Run the shooter at a fixed power for a fixed duration, gets Kf for velocity
                addSubActionPair(shooter, new MotorEncoderPowerAction(shooter, getPower(), getDuration()), true);
                break ;

            case 31:            // Run the base motor encoder velocity action
                addSubActionPair(shooter, new MotorEncoderVelocityAction(shooter, getPower(), getDuration()), true) ;
                break ;

            case 32:            // Set shooter to fixed velocity (hood down)
                addSubActionPair(shooter, new ShooterVelocityAction(shooter, getPower(), HoodPosition.Down), true);
                break ;

            case 33:            // Set shooter to fixed velocity (hood up)
                addSubActionPair(shooter, new ShooterVelocityAction(shooter, getPower(), HoodPosition.Up), true);
                break ;
                
            case 34:            // Characterize the shooter, gets velocity from smartdashboard
                addSubActionPair(gp, new ShootTestingAction(gp, HoodPosition.Down), true) ;
                break ;

            case 35:            // Characterize the shooter, gets velocity from smartdashboard
                addSubActionPair(gp, new ShootTestingAction(gp, HoodPosition.Up), true) ;
                break ;

            case 36:            // Test the hood
                addSubActionPair(shooter, new ShooterVelocityAction(shooter, 0.0, HoodPosition.Down), true);
                addAction(new DelayAction(ctrl.getRobot(), 3.0));
                addSubActionPair(shooter, new ShooterVelocityAction(shooter, 0.0, HoodPosition.Up), true);
                addAction(new DelayAction(ctrl.getRobot(), 3.0));
                addSubActionPair(shooter, new ShooterVelocityAction(shooter, 0.0, HoodPosition.Down), true);
                addAction(new DelayAction(ctrl.getRobot(), 3.0));
                addSubActionPair(shooter, new ShooterVelocityAction(shooter, 0.0, HoodPosition.Up), true);
                addAction(new DelayAction(ctrl.getRobot(), 3.0));
                addSubActionPair(shooter, new ShooterVelocityAction(shooter, 0.0, HoodPosition.Down), true);
                addAction(new DelayAction(ctrl.getRobot(), 3.0));
                break ;                                                                

            //
            // Numbers 40 - 49 are for the turret
            //
            case 40:               // Run the turret at a fixed power for a fixed duration, gets Kv for velocity
                addSubActionPair(turret, new MotorEncoderPowerAction(turret, getPower(), getDuration()), true);
                break ;

            case 41:                // Go to specific angle
                addSubActionPair(turret, new MotorEncoderGotoAction(turret, getPosition(), false), true);
                break ;

            case 42:                // Follow the target
                addSubActionPair(turret, new FollowTargetAction(turret, ll, db, tracker), true);
                break ;
                
            //
            // Numbers 50 - 59 are for the control panel spinner
            //                
            case 50:
                break ;

            //
            // Numbers 60 - 69 are for the climber
            //
            case 60:
                addSubActionPair(climber.getLifter(), new MotorEncoderPowerAction(climber.getLifter(), getPower(), getDuration()), true);
                break ;

            case 61:
                addSubActionPair(climber.getLifter(), new MotorEncoderGotoAction(climber.getLifter(), getPosition(), true), true);
                break ;

            case 62:
                addSubActionPair(climber, new ClimberMoveAction(climber, getPower(), 0.0), true);
                addAction(new DelayAction(ctrl.getRobot(), getDuration()));
                addSubActionPair(climber, new ClimberMoveAction(climber, 0.0, 0.0), true);                
                break ;

            // 100+ Whole robot test modes
            case 100:
                addSubActionPair(turret, new FollowTargetAction(turret, ll, db, tracker), false);
                addSubActionPair(intake, new CollectOnAction(intake), true);
                addSubActionPair(conveyor, new ConveyorPrepareToReceiveAction(conveyor), true);
                addSubActionPair(conveyor, new ConveyorReceiveAction(conveyor), true);
                addSubActionPair(intake, new CollectOffAction(intake), true);   
                addSubActionPair(conveyor, new ConveyorPrepareToEmitAction(conveyor), true);
                addSubActionPair(gp, new FireAction(gp, tracker, turret, db), true) ;
                break ;
        }
    }
}