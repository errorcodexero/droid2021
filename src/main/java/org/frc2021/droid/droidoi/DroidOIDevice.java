package org.frc2021.droid.droidoi;

import org.frc2021.droid.climber.ClimberDeployAction;
import org.frc2021.droid.climber.ClimberMoveAction;
import org.frc2021.droid.climber.ClimberSubsystem;
import org.frc2021.droid.climber.LifterCalibrateAction;
import org.frc2021.droid.droidsubsystem.DroidRobotSubsystem;
import org.frc2021.droid.gamepiecemanipulator.FireAction;
import org.frc2021.droid.gamepiecemanipulator.GamePieceManipulatorSubsystem;
import org.frc2021.droid.gamepiecemanipulator.StartCollectAction;
import org.frc2021.droid.gamepiecemanipulator.StopCollectAction;
import org.frc2021.droid.gamepiecemanipulator.conveyor.ConveyorEjectAction;
import org.frc2021.droid.gamepiecemanipulator.conveyor.ConveyorEmitAction;
import org.frc2021.droid.gamepiecemanipulator.conveyor.ConveyorPrepareToEmitAction;
import org.frc2021.droid.gamepiecemanipulator.conveyor.ConveyorPrepareToReceiveAction;
import org.frc2021.droid.gamepiecemanipulator.conveyor.ConveyorStopAction;
import org.frc2021.droid.gamepiecemanipulator.conveyor.ConveyorSubsystem;
import org.frc2021.droid.gamepiecemanipulator.intake.CollectOffAction;
import org.frc2021.droid.gamepiecemanipulator.intake.IntakeSubsystem;
import org.frc2021.droid.gamepiecemanipulator.shooter.ShooterSubsystem;
import org.frc2021.droid.gamepiecemanipulator.shooter.ShooterVelocityAction;
import org.frc2021.droid.targettracker.TargetTrackerSubsystem;
import org.frc2021.droid.turret.FollowTargetAction;
import org.frc2021.droid.turret.TurretSubsystem;
import org.xero1425.base.actions.Action;
import org.xero1425.base.actions.InvalidActionRequest;
import org.xero1425.base.actions.SequenceAction;
import org.xero1425.base.motorsubsystem.MotorEncoderGotoAction;
import org.xero1425.base.oi.Gamepad;
import org.xero1425.base.oi.OIPanel;
import org.xero1425.base.oi.OIPanelButton;
import org.xero1425.base.tankdrive.TankDriveSubsystem;
import org.xero1425.misc.BadParameterTypeException;
import org.xero1425.misc.MessageLogger;
import org.xero1425.misc.MessageType;
import org.xero1425.misc.MissingParameterException;

public class DroidOIDevice extends OIPanel {
    
    private enum CollectShootState
    {
        PreparingForCollect,
        FinishingCollect,
        WaitForIntake,
        CollectReady,
        Collecting,
        PreparingForShoot,
        ShootReady,
        Ejecting,  
        InvalidMode
    } ;

    private Gamepad gamepad_ ;

    private int automode_ ;
    private int collect_v_shoot_ ;
    private int collect_ ;
    private int eject_ ;

    private int climb_lock_ ;
    private int climb_deploy_ ;
    private int climb_up_ ;
    private int climb_down_ ;
    private int climb_left_ ;
    private int climb_right_ ;

    private int manual_shoot_mode_ ;
    private int manual_shoot_fire_ ;

    private CollectShootState collect_shoot_state_ ;
    private boolean climber_deployed_ ;
    private boolean started_deploy_ ;
    private boolean climber_attached_ ;
    private boolean rumbled_ ;
    private boolean climber_calibrating_ ;

    private Action stop_collect_ ;
    private Action start_collect_ ;
    private Action fire_ ;

    private Action intake_off_ ;

    private Action turret_goto_zero_ ;
    private Action turret_goto_manual_ ;
    private Action turret_follow_target_ ;

    private Action eject_action_ ;
    private Action conveyor_stop_ ;
    private Action queue_prep_collect_ ;
    private Action queue_prep_shoot_ ;
    private Action emit_ ;

    private Action shooter_eject_action_ ;
    private Action shooter_stop_ ;
    private Action shooter_shoot_manual_ ;
    private Action shooter_spinup_ ;    

    private Action deploy_climber_ ;
    private Action stop_climber_ ;
    private Action climber_up_ ;
    private Action climber_up_left_ ;
    private Action climber_up_right_ ;
    private Action climber_down_ ;
    private Action climber_down_left_ ;
    private Action climber_down_right_ ;
    private Action climber_left_ ;
    private Action climber_right_ ;
    private Action climber_calibrate_ ;

    public DroidOIDevice(DroidOISubsystem sub, int index, Gamepad gamepad, boolean climber)
            throws BadParameterTypeException, MissingParameterException {
        super(sub, "DroidOI", index);

        collect_shoot_state_ = CollectShootState.InvalidMode;
        climber_deployed_ = false;
        started_deploy_ = false;
        climber_attached_ = climber ;
        climber_calibrating_ = false ;
        rumbled_ = false;
        gamepad_ = gamepad ;

        initializeGadgets();
    }

    @Override
    public int getAutoModeSelector() {
        return getValue(automode_);
    }

    @Override
    public void createStaticActions() throws Exception {
        GamePieceManipulatorSubsystem gp = getDroidSubsystem().getGamePieceManipulator() ;
        ShooterSubsystem shooter = gp.getShooter() ;
        IntakeSubsystem intake = gp.getIntake() ;
        ConveyorSubsystem conveyor = gp.getConveyor() ;

        TurretSubsystem turret = getDroidSubsystem().getTurret();
        TankDriveSubsystem db = getDroidSubsystem().getTankDrive() ;
        TargetTrackerSubsystem tracker = getDroidSubsystem().getTracker() ;
        ClimberSubsystem climber = null ;
        
        if (climber_attached_)
            climber = getDroidSubsystem().getClimber() ;

        stop_collect_ = new StopCollectAction(gp) ;
        start_collect_ = new StartCollectAction(gp) ;
        fire_ = new FireAction(gp, tracker, turret, db) ;

        intake_off_ = new CollectOffAction(intake);

        turret_goto_zero_ = new MotorEncoderGotoAction(turret, 0.0, true) ;
        turret_goto_manual_ = new MotorEncoderGotoAction(turret, 0.0, true) ;
        turret_follow_target_ = new FollowTargetAction(turret, tracker) ;

        eject_action_ = new ConveyorEjectAction(conveyor) ;
        conveyor_stop_ = new ConveyorStopAction(conveyor) ;
        queue_prep_collect_ = new ConveyorPrepareToReceiveAction(conveyor) ;
        queue_prep_shoot_ = new ConveyorPrepareToEmitAction(conveyor) ;
        emit_ = new ConveyorEmitAction(conveyor) ;

        shooter_eject_action_ = new ShooterVelocityAction(shooter, -3000, ShooterSubsystem.HoodPosition.Down, false) ;
        shooter_stop_ = new ShooterVelocityAction(shooter, 0, ShooterSubsystem.HoodPosition.Down, false) ;
        shooter_shoot_manual_ = new ShooterVelocityAction(shooter, 2500.0, ShooterSubsystem.HoodPosition.Up, true) ;
        shooter_spinup_ = new ShooterVelocityAction(shooter, 4500.0, ShooterSubsystem.HoodPosition.Down, false) ;

        if (climber_attached_)
        {
            climber_calibrate_ = new LifterCalibrateAction(climber.getLifter()) ;
            deploy_climber_ = new ClimberDeployAction(climber.getLifter()) ;
            stop_climber_ = new ClimberMoveAction(climber, 0.0, 0.0, "STOPPED") ;
            climber_left_ = new ClimberMoveAction(climber, 0.0, "power:left", "LEFT") ;
            climber_right_ = new ClimberMoveAction(climber, 0.0, "power:right", "RIGHT") ;
            climber_up_ = new ClimberMoveAction(climber, "power:up", 0.0, "UP") ;
            climber_down_ = new ClimberMoveAction(climber, "power:down", 0.0, "DOWN") ;
            climber_up_left_ = new ClimberMoveAction(climber, "power:up", "power:left", "UP-LEFT") ;
            climber_up_right_ = new ClimberMoveAction(climber, "power:up", "power:right", "UP-RIGHT") ;
            climber_down_left_ = new ClimberMoveAction(climber, "power:down", "power:left", "DOWN-LEFT") ;
            climber_down_right_ = new ClimberMoveAction(climber, "power:down", "power:right", "DOWN-RIGHT") ;
        }
        else
        {
            climber_calibrate_ = null ;
            deploy_climber_ = null ;
            stop_climber_ = null ;
            climber_left_ = null ;
            climber_right_ = null ;
            climber_up_ = null ;
            climber_down_ = null ;
            climber_up_left_ = null ;
            climber_up_right_ = null ;
            climber_down_left_ = null ;
            climber_down_right_ = null ;
        }
    }

    @Override
    public void generateActions(SequenceAction seq) throws InvalidActionRequest {
        generateCollectShootActions(seq);
        if (climber_attached_)
            generateClimbActions(seq);
    }

    private void generateClimbActions(SequenceAction seq) throws InvalidActionRequest {
        ClimberSubsystem climber = getDroidSubsystem().getClimber() ;

        if (getValue(climb_lock_) == 1)
        {
            if (!climber_calibrating_)
            {
                climber.cancelAction();
                climber.getLifter().cancelAction(); ;
            }

            if (!climber.isBusy() && !climber.getLifter().isBusy())
            {
                seq.addSubActionPair(climber.getLifter(), climber_calibrate_, false);
                climber_calibrating_ = true ;
            }
        }
        else
        {
            if (!climber_calibrating_)
            {
                if (!climber.isBusy() && !climber.getLifter().isBusy())
                {
                    generateClimbActionUnlocked(seq);
                }
            }
        }
    }

    private void generateClimbActionUnlocked(SequenceAction seq) throws InvalidActionRequest
    {
        ClimberSubsystem climber = getDroidSubsystem().getClimber() ;

        if (!climber_deployed_) {
            if (!climber.isInFieldMode() && !started_deploy_) {
                climber_deployed_ = true ;
                started_deploy_ = true ;
            }
            else if (!started_deploy_) {
                if (getValue(climb_deploy_) == 1 && !climber.isBusy() && !climber.getLifter().isBusy()) {
                    climber.setDefaultAction(null);
                    climber.setAction(null) ;
                    seq.addSubActionPair(climber.getLifter(), deploy_climber_, false);
                    started_deploy_ = true ;
                }
                else if (started_deploy_ && !climber.isBusy() && !climber.getLifter().isBusy()) {
                    climber_deployed_ = true ;
                }
            }
            else {
                if (!climber.getLifter().isBusy())
                    climber_deployed_ = true ;
            }
        }
        else {
            if (getValue(climb_deploy_) == 1 && !climber.isBusy() && !climber.getLifter().isBusy()) {
                seq.addSubActionPair(climber, deploy_climber_, false);
                started_deploy_ = true ;
                climber_deployed_ = false ;
            }
            else {
                Action act = null ;
                boolean up = (getValue(climb_up_) == 1) ;
                boolean down = (getValue(climb_down_) == 1) ;
                boolean left = (getValue(climb_left_) == 1) ;
                boolean right = (getValue(climb_right_) == 1) ;

                if (up && left) {
                    act = climber_up_left_ ;
                }
                else if (up & right) {
                    act = climber_up_right_ ;
                }
                else if (down && left) {
                    act = climber_down_left_ ;
                }
                else if (down && right) {
                    act = climber_down_right_ ;
                }
                else if (up) {
                    act = climber_up_ ;
                }
                else if (down) {
                    act = climber_down_ ;
                }
                else if (left) {
                    act = climber_left_ ;
                }
                else if (right) {
                    act = climber_right_ ;
                }
                else {
                    act = stop_climber_ ;
                }
            
                seq.addSubActionPair(climber, act, false);
            }
        }
    }

    private void generateCollectShootActions(SequenceAction seq) throws InvalidActionRequest {
        ConveyorSubsystem conveyor = getDroidSubsystem().getGamePieceManipulator().getConveyor();

        if (conveyor.getBallCount() == ConveyorSubsystem.MAX_BALLS && rumbled_ == false && gamepad_ != null) {
            gamepad_.rumble(1.0, 1.0) ;
            rumbled_ = true ;
        }
        else if (conveyor.getBallCount() < ConveyorSubsystem.MAX_BALLS && rumbled_ == true) {
            rumbled_ = false ;
        }

        CollectShootState prev = collect_shoot_state_ ;

        if (getValue(eject_) == 1) {
            if (collect_shoot_state_ != CollectShootState.Ejecting)
                startEject(seq) ;
        } else {
            switch(collect_shoot_state_) {
                case PreparingForCollect:
                    processPreparingForCollect(seq);
                    break ;

                case FinishingCollect:
                    processFinishingCollect(seq);
                    break ;

                case WaitForIntake:
                    processWaitingForIntake(seq);
                    break ;

                case CollectReady:
                    processCollectReady(seq);
                    break ;

                case Collecting:
                    processCollecting(seq);
                    break ;

                case PreparingForShoot:
                    processPrepareForShoot(seq);
                    break ;

                case ShootReady:
                    processShootReady(seq);
                    break ;

                case Ejecting:
                    processEjecting(seq);
                    break ;

                case InvalidMode:
                    processInvalidMode(seq);
                    break ;
            }
        }

        if (prev != collect_shoot_state_) {
            MessageLogger logger = getSubsystem().getRobot().getMessageLogger() ;
            logger.startMessage(MessageType.Debug, getSubsystem().getLoggerID()) ;
            logger.add("OI shoot/collect: ").add(prev.toString()).add(" --> ").add(collect_shoot_state_.toString()) ;
            logger.add(", ballcount ", conveyor.getBallCount()) ;
            logger.endMessage(); 
        }
    }

    private void processInvalidMode(SequenceAction seq) throws InvalidActionRequest {
        if (getValue(collect_v_shoot_) == 1) {
            shootMode(seq) ;
        }
        else {
            collectMode(seq) ;
        }
    }

    private void processEjecting(SequenceAction seq) throws InvalidActionRequest {
        ConveyorSubsystem conveyor = getDroidSubsystem().getGamePieceManipulator().getConveyor();
        if (!conveyor.isBusy())
            stopEject(seq) ;
    }

    private void processShootReady(SequenceAction seq) throws InvalidActionRequest {
        GamePieceManipulatorSubsystem gp = getDroidSubsystem().getGamePieceManipulator();
        ConveyorSubsystem conveyor = getDroidSubsystem().getGamePieceManipulator().getConveyor();
        
        if (getValue(collect_v_shoot_) == 0) {
            if (gp.isBusy())
                gp.cancelAction();
            collectMode(seq) ;
        }
        else if (getValue(manual_shoot_mode_) == 1 && getValue(manual_shoot_fire_) == 1) {
            seq.addSubActionPair(conveyor, emit_, false);
        }
    }

    private void processPrepareForShoot(SequenceAction seq) throws InvalidActionRequest {
        ConveyorSubsystem conveyor = getDroidSubsystem().getGamePieceManipulator().getConveyor();
        GamePieceManipulatorSubsystem gp = getDroidSubsystem().getGamePieceManipulator() ;   
        
        if (!conveyor.isBusy()) {
            collect_shoot_state_ = CollectShootState.ShootReady ;
            if (getValue(manual_shoot_mode_) == 0) {
                seq.addSubActionPair(gp, fire_, false) ;
            }
        }
    }

    private void processCollecting(SequenceAction seq) throws InvalidActionRequest {
        ConveyorSubsystem conveyor = getDroidSubsystem().getGamePieceManipulator().getConveyor();
        GamePieceManipulatorSubsystem gp = getDroidSubsystem().getGamePieceManipulator() ;        

        if (!isCollectButtonPressed() || conveyor.isFull()) {
            //
            // If the gunner or driver released the collect button or the conveyor is full
            //
            if (conveyor.isCollecting()) {
                collect_shoot_state_ = CollectShootState.FinishingCollect ;
            } else {
                collect_shoot_state_ = CollectShootState.WaitForIntake ;
                seq.addSubActionPair(gp, stop_collect_, false);
            }
        }
    }

    private void processCollectReady(SequenceAction seq) throws InvalidActionRequest {
        GamePieceManipulatorSubsystem gp = getDroidSubsystem().getGamePieceManipulator() ;

        if (getValue(collect_v_shoot_) == 1) {
            shootMode(seq) ;
        }
        else if (isCollectButtonPressed()) {
            seq.addSubActionPair(gp, start_collect_, false);
            collect_shoot_state_ = CollectShootState.Collecting ;
        }
    }

    private void processWaitingForIntake(SequenceAction seq) throws InvalidActionRequest {
        GamePieceManipulatorSubsystem gp = getDroidSubsystem().getGamePieceManipulator() ;
        
        if (!gp.isBusy())
            collect_shoot_state_ = CollectShootState.CollectReady ;
    }

    private void processPreparingForCollect(SequenceAction seq) throws InvalidActionRequest {
        ConveyorSubsystem conveyor = getDroidSubsystem().getGamePieceManipulator().getConveyor();
        
        if (conveyor.isStagedForCollect() || conveyor.isFull()) {
            collect_shoot_state_ = CollectShootState.CollectReady ;
        }
    }

    private void processFinishingCollect(SequenceAction seq) throws InvalidActionRequest {
        ConveyorSubsystem conveyor = getDroidSubsystem().getGamePieceManipulator().getConveyor();
        GamePieceManipulatorSubsystem gp = getDroidSubsystem().getGamePieceManipulator() ;
        
        if (!conveyor.isCollecting()) {
            seq.addSubActionPair(gp, stop_collect_, false);
            collect_shoot_state_ = CollectShootState.WaitForIntake ;
        }
    }

    private void collectMode(SequenceAction seq) throws InvalidActionRequest {
        ConveyorSubsystem conveyor = getDroidSubsystem().getGamePieceManipulator().getConveyor() ;
        TurretSubsystem turret = getDroidSubsystem().getTurret() ;
        ShooterSubsystem shooter = getDroidSubsystem().getGamePieceManipulator().getShooter() ;
        GamePieceManipulatorSubsystem gp = getDroidSubsystem().getGamePieceManipulator() ;
        

        collect_shoot_state_ = CollectShootState.PreparingForCollect ;
        gp.cancelAction(); ;
        seq.addSubActionPair(conveyor, queue_prep_collect_, false) ;

        //
        // This is enabled when we are doing the 2021 challenges.  This is used to move the
        // turret to -90 degrees to do the side shot in the accuracy challenge.  The robot is turned
        // sideways with respect to the target and the turret rotated to find the turret.
        //
        // if (getValue(climb_lock_) == 1)
        // {
        //     seq.addSubActionPair(turret, turret_goto_zero_, false) ;
        // }
        // else
        // {
        //     seq.addSubActionPair(turret, turret_goto_90_, false) ;
        // }

        seq.addSubActionPair(turret, turret_goto_zero_, false) ;
        seq.addSubActionPair(shooter, shooter_stop_, false);
    }

    private void shootMode(SequenceAction seq) throws InvalidActionRequest {
        ShooterSubsystem shooter = getDroidSubsystem().getGamePieceManipulator().getShooter() ;
        IntakeSubsystem intake = getDroidSubsystem().getGamePieceManipulator().getIntake() ;
        ConveyorSubsystem conveyor = getDroidSubsystem().getGamePieceManipulator().getConveyor() ;
        TurretSubsystem turret = getDroidSubsystem().getTurret() ;
        
        collect_shoot_state_ = CollectShootState.PreparingForShoot ;

        if (getValue(manual_shoot_mode_) == 1) {
            seq.addSubActionPair(turret, turret_goto_manual_, false) ;
            seq.addSubActionPair(shooter, shooter_shoot_manual_, false);
        }
        else {
            seq.addSubActionPair(turret, turret_follow_target_, false);
            seq.addSubActionPair(shooter, shooter_spinup_, false) ;
        }

        seq.addSubActionPair(conveyor, queue_prep_shoot_, false) ;
        seq.addSubActionPair(intake, intake_off_, false);
    }

    private void startEject(SequenceAction seq) throws InvalidActionRequest {
        GamePieceManipulatorSubsystem gp = getDroidSubsystem().getGamePieceManipulator() ;
        ShooterSubsystem shooter = getDroidSubsystem().getGamePieceManipulator().getShooter() ;
        IntakeSubsystem intake = getDroidSubsystem().getGamePieceManipulator().getIntake() ;
        ConveyorSubsystem conveyor = getDroidSubsystem().getGamePieceManipulator().getConveyor() ;
        TurretSubsystem turret = getDroidSubsystem().getTurret() ;

        collect_shoot_state_ = CollectShootState.Ejecting ;
        gp.cancelAction();
        seq.addSubActionPair(intake, intake_off_, false) ;
        seq.addSubActionPair(turret, turret_goto_zero_, false) ;
        seq.addSubActionPair(conveyor, eject_action_, false);
        seq.addSubActionPair(shooter, shooter_eject_action_, false);
    }

    private void stopEject(SequenceAction seq) throws InvalidActionRequest {
        ShooterSubsystem shooter = getDroidSubsystem().getGamePieceManipulator().getShooter() ;
        ConveyorSubsystem conveyor = getDroidSubsystem().getGamePieceManipulator().getConveyor() ;

        collect_shoot_state_ = CollectShootState.InvalidMode ;
        seq.addSubActionPair(conveyor, conveyor_stop_, false);
        seq.addSubActionPair(shooter, shooter_stop_, false);        
    }

    private DroidRobotSubsystem getDroidSubsystem() {
        return (DroidRobotSubsystem)getSubsystem().getRobot().getRobotSubsystem() ;
    }

    private boolean isCollectButtonPressed() {

        if (getValue(collect_) == 1)
            return true ;

        if (gamepad_ != null && gamepad_.isRTriggerPressed())
            return true ;

        return false ;
    }

    private void initializeGadgets() throws BadParameterTypeException, MissingParameterException {
        int num ;

        num = getSubsystem().getSettingsValue("oi:gadgets:automode").getInteger() ;
        Double [] map = { -0.9, -0.75, -0.5, -0.25, 0.0, 0.2, 0.4, 0.6, 0.8, 1.0 } ;
        automode_ = mapAxisScale(num, map) ;

        num = getSubsystem().getSettingsValue("oi:gadgets:shoot_collect_mode").getInteger() ;
        collect_v_shoot_ = mapButton(num, OIPanelButton.ButtonType.Level) ;

        num = getSubsystem().getSettingsValue("oi:gadgets:collect_onoff").getInteger() ;
        collect_ = mapButton(num, OIPanelButton.ButtonType.Level) ;

        num = getSubsystem().getSettingsValue("oi:gadgets:eject").getInteger() ;
        eject_ = mapButton(num, OIPanelButton.ButtonType.Level) ;

        num = getSubsystem().getSettingsValue("oi:gadgets:climb_lock").getInteger() ;
        climb_lock_ = mapButton(num, OIPanelButton.ButtonType.Level) ;

        if (climber_attached_)
        {          
            num = getSubsystem().getSettingsValue("oi:gadgets:climb_deploy").getInteger() ;
            climb_deploy_ = mapButton(num, OIPanelButton.ButtonType.LowToHigh) ;
            
            num = getSubsystem().getSettingsValue("oi:gadgets:climb_up").getInteger() ;
            climb_up_ = mapButton(num, OIPanelButton.ButtonType.Level) ;
            
            num = getSubsystem().getSettingsValue("oi:gadgets:climb_down").getInteger() ;
            climb_down_ = mapButton(num, OIPanelButton.ButtonType.Level) ;
            
            num = getSubsystem().getSettingsValue("oi:gadgets:traverse_left").getInteger() ;
            climb_left_ = mapButton(num, OIPanelButton.ButtonType.Level) ;
            
            num = getSubsystem().getSettingsValue("oi:gadgets:traverse_right").getInteger() ;
            climb_right_ = mapButton(num, OIPanelButton.ButtonType.Level) ;        
        }
        else
        {
            climb_deploy_ = -1 ;
            climb_up_ = -1 ;
            climb_down_ = -1 ;
            climb_left_ = -1 ;
            climb_right_ = -1 ;
        }

        num = getSubsystem().getSettingsValue("oi:gadgets:manual_shoot_mode").getInteger() ;
        manual_shoot_mode_ = mapButton(num, OIPanelButton.ButtonType.Level) ;  
        
        num = getSubsystem().getSettingsValue("oi:gadgets:manual_shoot_fire").getInteger() ;
        manual_shoot_fire_ = mapButton(num, OIPanelButton.ButtonType.LowToHigh) ;          
    }
}