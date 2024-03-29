package org.frc2021.droid.gamepiecemanipulator.intake;

import org.xero1425.base.motorsubsystem.MotorEncoderGotoAction;
import org.xero1425.misc.MessageLogger;
import org.xero1425.misc.MessageType;

public class CollectOffAction extends MotorEncoderGotoAction {
    private IntakeSubsystem sub_ ;
    private double collect_power_ ;

    public CollectOffAction(IntakeSubsystem sub)  throws Exception {
        super(sub, "collect:offpos", true) ;

        sub_ = sub ;
        collect_power_ = sub.getSettingsValue("collector:revpower").getDouble() ;
    }

    @Override
    public void start() throws Exception {
        super.start() ;

        MessageLogger logger = getSubsystem().getRobot().getMessageLogger() ;
        logger.startMessage(MessageType.Debug, getSubsystem().getLoggerID()) ;
        logger.add("start") ;
        logger.add("isdone", isDone()) ;
        logger.add("power", collect_power_) ;
        logger.endMessage();

        if (!isDone())
            sub_.setCollectorPower(collect_power_);
        else
            sub_.setCollectorPower(0.0);
    }

    @Override
    public void run() throws Exception {
        super.run() ;

        MessageLogger logger = getSubsystem().getRobot().getMessageLogger() ;
        logger.startMessage(MessageType.Debug, getSubsystem().getLoggerID()) ;
        logger.add("run") ;
        logger.add("isdone", isDone()) ;
        logger.add("power", collect_power_) ;
        logger.endMessage();

        if (isDone())
            sub_.setCollectorPower(0.0);
    }

    @Override
    public void cancel() {
        super.cancel() ;

        MessageLogger logger = getSubsystem().getRobot().getMessageLogger() ;
        logger.startMessage(MessageType.Debug, getSubsystem().getLoggerID()) ;
        logger.add("cancel") ;
        logger.add("isdone", isDone()) ;
        logger.add("power", collect_power_) ;
        logger.endMessage();

        try {
            sub_.setCollectorPower(0.0) ;
        }
        catch(Exception ex) {
        }
    }
}
