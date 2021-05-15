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
    private String last_which_ ;

    public GalacticSearchAutoMode(DroidAutoController ctrl) throws Exception {
        super(ctrl, "GalacticSearchAutoMode");

        which_ = "" ;
        last_which_ = "" ;
        update("");
    }

    public void update(String gamedata) throws Exception {

        which_ = getWhich() ;
        if (!which_.equals(last_which_))
        {
            clear() ;

            if (!which_.isEmpty())
            {
                TankDriveSubsystem db = getDroidSubsystem().getTankDrive();
                GamePieceManipulatorSubsystem gm = getDroidSubsystem().getGamePieceManipulator();

                SmartDashboard.putString("GSearch", which_);

                ParallelAction pact = new ParallelAction(getMessageLogger(), DonePolicy.All);
                SequenceAction collseq = new SequenceAction(getMessageLogger()) ;
                SequenceAction pathseq = new SequenceAction(getMessageLogger()) ;
        
                collseq.addSubActionPair(gm, new StartCollectAction(gm), false);
                collseq.addAction(new DelayAction(db.getRobot(), 4.5));
                collseq.addSubActionPair(gm, new StopCollectAction(gm), false) ;
                pact.addAction(collseq) ;
        
                if (which_.equals("red-a")) {
                    pathseq.addAction(new DelayAction(db.getRobot(), 0.125));
                    pathseq.addSubActionPair(db, new TankDriveFollowPathAction(db, "GalacticSearch_RedA", false), true);
                } else if (which_.equals("red-b")) {
                    pathseq.addAction(new DelayAction(db.getRobot(), 0.125));
                    pathseq.addSubActionPair(db, new TankDriveFollowPathAction(db, "GalacticSearch_RedB", false), true);
                } else if (which_.equals("blue-a")) {
                    pathseq.addSubActionPair(db, new TankDriveFollowPathAction(db, "GalacticSearch_BlueA", false), true);
                } else if (which_.equals("blue-b")) {
                    pathseq.addSubActionPair(db, new TankDriveFollowPathAction(db, "GalacticSearch_BlueB", false), true);
                }
        
                pact.addAction(pathseq);
                addAction(pact);
            }
        }

        last_which_ = which_ ;
    }

    private String getWhich() {
        NetworkTable table = NetworkTableInstance.getDefault().getTable("Shuffleboard").getSubTable("Vision");
        String which = table.getEntry("Result").getString("");

        if (which.length() == 0)
            which = "red-b" ;

        return which ;
    }
}
