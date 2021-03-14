package org.frc2021.droid.gamepiecemanipulator;

import org.frc2021.droid.gamepiecemanipulator.conveyor.ConveyorEmitAction;
import org.frc2021.droid.gamepiecemanipulator.shooter.ShooterSubsystem;
import org.frc2021.droid.gamepiecemanipulator.shooter.ShooterVelocityAction;
import org.frc2021.droid.gamepiecemanipulator.shooter.ShooterSubsystem.HoodPosition;
import org.frc2021.droid.targettracker.TargetTrackerSubsystem;
import org.frc2021.droid.turret.TurretSubsystem;
import org.xero1425.base.Subsystem.DisplayType;
import org.xero1425.base.actions.Action;
import org.xero1425.base.tankdrive.TankDriveSubsystem;
import org.xero1425.misc.MessageLogger;
import org.xero1425.misc.MessageType;
import org.xero1425.misc.SettingsParser;

public class FireAction extends Action {

    static public final String moduleName = "fireaction" ;

    static private int logger_id_ = -1 ;
    
    private GamePieceManipulatorSubsystem sub_ ;
    private TargetTrackerSubsystem tracker_ ;
    private TurretSubsystem turret_ ;
    private TankDriveSubsystem db_ ;

    ShooterSubsystem.HoodPosition hood_pos_ ;

    private boolean is_firing_ ;
    private double db_velocity_threshold_ ;

    private double hood_down_a_ ;
    private double hood_down_b_ ;
    private double hood_down_c_ ;
    
    private double hood_up_a_ ;
    private double hood_up_b_ ;
    private double hood_up_c_ ;

    private double max_hood_up_distance_ ;
    private double min_hood_down_distance_ ;

    private ShooterVelocityAction shooter_velocity_action_ ;
    private ShooterVelocityAction shooter_stop_action_ ;
    private ConveyorEmitAction emit_action_ ;

    private double start_time_ ;
    private int plot_id_ ;
    static final String[] plot_columns_ = { 
        "time",

        "is_firing",
        "ready",
        "ready_except_shooter",

        "limelight_ready",
        "turret_ready",
        "shooter_ready",
        "drivebase_ready",
    } ;
    
    public FireAction(GamePieceManipulatorSubsystem gp, TargetTrackerSubsystem tracker, 
                    TurretSubsystem turret, TankDriveSubsystem db) throws Exception {
        super(gp.getRobot().getMessageLogger()) ;

        if (logger_id_ == -1)
        {
            logger_id_ = db.getRobot().getMessageLogger().registerSubsystem(moduleName) ;
        }

        sub_ = gp ;
        tracker_ = tracker ;
        turret_ = turret ;
        db_ = db ;

        shooter_velocity_action_ = new ShooterVelocityAction(gp.getShooter(), 4500.0, ShooterSubsystem.HoodPosition.Down, true) ;
        shooter_stop_action_ = new ShooterVelocityAction(gp.getShooter(), 0.0, ShooterSubsystem.HoodPosition.Down, false) ;
        emit_action_ = new ConveyorEmitAction(gp.getConveyor()) ;

        SettingsParser settings = gp.getRobot().getSettingsParser() ;
        db_velocity_threshold_ = settings.get("gamepiecemanipulator:fire:max_drivebase_velocity").getDouble() ;

        hood_down_a_ = settings.get("shooter:aim:hood_down:a").getDouble() ;
        hood_down_b_ = settings.get("shooter:aim:hood_down:b").getDouble() ;
        hood_down_c_ = settings.get("shooter:aim:hood_down:c").getDouble() ;
        
        hood_up_a_ = settings.get("shooter:aim:hood_up:a").getDouble() ;
        hood_up_b_ = settings.get("shooter:aim:hood_up:b").getDouble() ;
        hood_up_c_ = settings.get("shooter:aim:hood_up:c").getDouble() ;

        max_hood_up_distance_ = settings.get("shooter:aim:max_hood_up").getDouble() ;
        min_hood_down_distance_ = settings.get("shooter:aim:min_hood_down").getDouble() ;

        plot_id_ = gp.initPlot("FireAction") ;

        hood_pos_ = HoodPosition.Down ;        
    }

    @Override
    public void start() throws Exception {
        super.start() ;

        is_firing_ = false ;
        shooter_velocity_action_.setTarget(4500.0) ;
        sub_.getShooter().setAction(shooter_velocity_action_, true) ;

        start_time_ = sub_.getRobot().getTime() ;
        sub_.startPlot(plot_id_, plot_columns_) ;
    }

    @Override
    public void run() {
        MessageLogger logger = sub_.getRobot().getMessageLogger() ;
        ShooterSubsystem shooter = sub_.getShooter() ;

        boolean tracker_ready = tracker_.hasTarget() ;
        boolean turret_ready = turret_.isReadyToFire() ;
        boolean shooter_ready = shooter.isReadyToFire() ;
        boolean db_ready = (db_.getVelocity() < db_velocity_threshold_) ;

        boolean ready_to_fire_except_shooter = tracker_ready && db_ready && turret_ready ;
        boolean ready_to_fire = tracker_ready && db_ready && turret_ready && shooter_ready ;

        sub_.putDashboard("tracker-ready", DisplayType.Always, tracker_ready) ;
        sub_.putDashboard("turret-ready", DisplayType.Always, turret_ready) ;
        sub_.putDashboard("shooter-ready", DisplayType.Always, shooter_ready);
        sub_.putDashboard("db-ready", DisplayType.Always, db_ready) ;
        sub_.putDashboard("rtf", DisplayType.Always, ready_to_fire);
        sub_.putDashboard("isfiring", DisplayType.Always, is_firing_);

        if (tracker_ready)
        {
            setTargetVelocity(); 
        }

        if (is_firing_) {
            if (sub_.getConveyor().isEmpty() && !sub_.getConveyor().isBusy()) {
                //
                // We are out of balls
                //
                sub_.endPlot(plot_id_);
                setDone() ;
                stopChildActions() ;

                logger.startMessage(MessageType.Debug, logger_id_) ;
                logger.add("fire-action: stopped firing, out of balls") ;
                logger.endMessage();

                //
                // When we are out of balls, stop the shooter motor
                //
                sub_.getShooter().setAction(shooter_stop_action_, true) ;
            }
            // else if (!ready_to_fire_except_shooter) {
            else if (!ready_to_fire) {
                //
                // We lost the target or the driver started driving or we got bumped and
                // are no longer aiming at the target
                // 
                emit_action_.stopFiring();
                is_firing_ = false ;

                logger.startMessage(MessageType.Debug, logger_id_) ;
                logger.add("fire-action: stopped firing, lost target") ;
                logger.add(",tracker_ready", tracker_ready) ;
                logger.add(",db_ready", db_ready) ;
                logger.add(",turret_ready", turret_ready) ;
                logger.add(",shooter_ready", shooter_ready) ;
                logger.endMessage();                
            }
        }
        else {
            if (sub_.getConveyor().isEmpty()) {
                sub_.endPlot(plot_id_);
                setDone() ;
                stopChildActions() ;  

                logger.startMessage(MessageType.Debug, logger_id_) ;
                logger.add("fire-action: out of balls, completing action") ;
                logger.endMessage();    
            }
            else if (ready_to_fire && !sub_.getConveyor().isBusy()) {
                sub_.getConveyor().setAction(emit_action_, true);
                is_firing_ = true ;

                logger.startMessage(MessageType.Debug, logger_id_) ;
                logger.add("fire-action: fire away ... !!!") ;
                logger.endMessage();                   
            }
        }

        logger.startMessage(MessageType.Debug, logger_id_) ;
        logger.add("fire-action:") ;
        logger.add("tracker", tracker_ready) ;
        logger.add("turret", turret_ready) ;
        logger.add("shooter", shooter_ready) ;
        logger.add("drivebase", db_ready) ;
        logger.add("hood", hood_pos_.toString()) ;
        logger.add("balls", sub_.getConveyor().getBallCount()) ;
        logger.endMessage();

        Double[] data = new Double[plot_columns_.length] ;
        data[0] = sub_.getRobot().getTime() - start_time_ ;
        data[1] = is_firing_ ? 1.0 : 0.0 ;
        data[2] = ready_to_fire ? 1.1 : 0.0 ;
        data[3] = ready_to_fire_except_shooter ? 1.2 : 0.0 ;
        data[4] = tracker_ready ? 1.3 : 0.0 ;
        data[5] = turret_ready ? 1.4 : 0.0 ;
        data[6] = shooter_ready ? 1.5 : 0.0 ;
        data[7] = db_ready ? 1.6 : 0.0 ;

        sub_.addPlotData(plot_id_, data);

        if (sub_.getConveyor().isEmpty()) {
            sub_.endPlot(plot_id_) ;
            setDone() ;            
            stopChildActions() ;
        }
    }

    @Override
    public void cancel() {
        super.cancel();
        sub_.endPlot(plot_id_);
        stopChildActions();
    }

    @Override
    public String toString(int indent) {
        return prefix(indent) + "FireAction" ;
    }

    private void stopChildActions() {
        shooter_velocity_action_.cancel() ;
        emit_action_.cancel() ;
    }

    private void setTargetVelocity() {
        double dist = tracker_.getDistance() ;

        if (dist > max_hood_up_distance_)
            hood_pos_ = HoodPosition.Down ;
        else if (dist < min_hood_down_distance_)
            hood_pos_ = HoodPosition.Up ;

        double a, b, c ;
        if (hood_pos_ == HoodPosition.Down) {
            a = hood_down_a_ ;
            b = hood_down_b_ ;
            c = hood_down_c_ ;
        }
        else {
            a = hood_up_a_ ;
            b = hood_up_b_ ;
            c = hood_up_c_ ;
        }
        double target = a * dist * dist + b * dist + c ;

        shooter_velocity_action_.setHoodPosition(hood_pos_);
        shooter_velocity_action_.setTarget(target);

        sub_.putDashboard("shoot-distance", DisplayType.Always, dist);
        sub_.putDashboard("shoot-target", DisplayType.Always, target);
        sub_.putDashboard("shoot-velocity", DisplayType.Always, sub_.getShooter().getVelocity());

        MessageLogger logger = sub_.getRobot().getMessageLogger() ;
        logger.startMessage(MessageType.Info, logger_id_) ;
        logger.add("FIRE ACTION: ") ;
        logger.add("distance", dist) ;
        logger.add(" target", target) ;
        logger.add(" velocity", sub_.getShooter().getVelocity()) ;
        logger.add(" hood", hood_pos_.toString()) ;
        logger.endMessage();
    }
}