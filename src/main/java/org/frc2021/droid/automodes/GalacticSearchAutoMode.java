package org.frc2021.droid.automodes;

import org.frc2021.droid.gamepiecemanipulator.GamePieceManipulatorSubsystem;
import org.frc2021.droid.gamepiecemanipulator.StartCollectAction;
import org.frc2021.droid.gamepiecemanipulator.StopCollectAction;
import org.xero1425.base.actions.DelayAction;
import org.xero1425.base.actions.ParallelAction;
import org.xero1425.base.actions.SequenceAction;
import org.xero1425.base.actions.ParallelAction.DonePolicy;
import org.xero1425.base.tankdrive.TankDriveFollowPathAction;
import org.xero1425.base.tankdrive.TankDriveSubsystem;
import org.xero1425.misc.MessageLogger;
import org.xero1425.misc.MessageType;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class GalacticSearchAutoMode extends DroidAutoMode {
    private String which_ ;

    public GalacticSearchAutoMode(DroidAutoController ctrl) throws Exception {
        super(ctrl, "GalacticSearchAutoMode");

        TankDriveSubsystem db = getDroidSubsystem().getTankDrive();
        GamePieceManipulatorSubsystem gm = getDroidSubsystem().getGamePieceManipulator();

        which_ = "" ;
        NetworkTable table = NetworkTableInstance.getDefault().getTable("Shuffleboard").getSubTable("Vision");
        which_ = table.getEntry("Result").getString("");

        SmartDashboard.putString("GSearch", which_);

        MessageLogger logger = getAutoController().getRobot().getMessageLogger();
        logger.startMessage(MessageType.Info).add("WHICH", which_);
        logger.endMessage();

        ParallelAction pact = new ParallelAction(getMessageLogger(), DonePolicy.All);

        pact.addSubActionPair(gm, new StartCollectAction(gm), false);

        if (which_.equals("reda")) {
            pact.addSubActionPair(db, new TankDriveFollowPathAction(db, "GalacticSearch_RedA", false), true);
        } else if (which_.equals("redb")) {
            pact.addSubActionPair(db, new TankDriveFollowPathAction(db, "GalacticSearch_RedB", false), true);
        } else if (which_.equals("bluea")) {
            pact.addSubActionPair(db, new TankDriveFollowPathAction(db, "GalacticSearch_BlueA", false), true);
        } else if (which_.equals("blueb")) {
            pact.addSubActionPair(db, new TankDriveFollowPathAction(db, "GalacticSearch_BlueB", false), true);
        }

        addAction(pact);
    }

    public void updateWhich() throws Exception {
        clear() ;

        TankDriveSubsystem db = getDroidSubsystem().getTankDrive();
        GamePieceManipulatorSubsystem gm = getDroidSubsystem().getGamePieceManipulator();

        NetworkTable table = NetworkTableInstance.getDefault().getTable("Shuffleboard").getSubTable("Vision");
        which_ = table.getEntry("Result").getString("");

        if (!which_.isEmpty())
        {
            SmartDashboard.putString("GSearch", which_);

            ParallelAction pact = new ParallelAction(getMessageLogger(), DonePolicy.All);
            SequenceAction sact = new SequenceAction(getMessageLogger()) ;

            sact.addSubActionPair(gm, new StartCollectAction(gm), false);
            sact.addAction(new DelayAction(db.getRobot(), 5.0));
            sact.addSubActionPair(gm, new StopCollectAction(gm), false) ;
            pact.addAction(sact) ;

            if (which_.equals("red-a")) {
                pact.addSubActionPair(db, new TankDriveFollowPathAction(db, "GalacticSearch_RedA", false), true);
            } else if (which_.equals("red-b")) {
                pact.addSubActionPair(db, new TankDriveFollowPathAction(db, "GalacticSearch_RedB", false), true);
            } else if (which_.equals("blue-a")) {
                pact.addSubActionPair(db, new TankDriveFollowPathAction(db, "GalacticSearch_BlueA", false), true);
            } else if (which_.equals("blue-b")) {
                pact.addSubActionPair(db, new TankDriveFollowPathAction(db, "GalacticSearch_BlueB", false), true);
            }

            addAction(pact);
        }
    }
}
