package org.frc2021.droid.automodes;

import org.frc2021.droid.gamepiecemanipulator.GamePieceManipulatorSubsystem;
import org.frc2021.droid.gamepiecemanipulator.StartCollectAction;
import org.xero1425.base.actions.ParallelAction;
import org.xero1425.base.actions.ParallelAction.DonePolicy;
import org.xero1425.base.tankdrive.TankDriveFollowPathAction;
import org.xero1425.base.tankdrive.TankDriveSubsystem;

public class GalacticSearchAutoMode extends DroidAutoMode {
    public GalacticSearchAutoMode(DroidAutoController ctrl) throws Exception {
        super(ctrl, "GalacticSearchAutoMode") ;

        TankDriveSubsystem db = getDroidSubsystem().getTankDrive() ; 
        GamePieceManipulatorSubsystem gm = getDroidSubsystem().getGamePieceManipulator() ;

        String which= "blueb" ;

        ParallelAction pact = new ParallelAction(getMessageLogger(), DonePolicy.All);

        pact.addSubActionPair(gm, new StartCollectAction(gm), false) ;

        if (which.equals("reda")) {
            pact.addSubActionPair(db, new TankDriveFollowPathAction(db, "GalacticSearch_RedA", false), true);
        }
        else if (which.equals("redb")) {
            pact.addSubActionPair(db, new TankDriveFollowPathAction(db, "GalacticSearch_RedB", false), true);
        }
        else if (which.equals("bluea")) {
            pact.addSubActionPair(db, new TankDriveFollowPathAction(db, "GalacticSearch_BlueA", false), true);
        }
        else if (which.equals("blueb")) {
            pact.addSubActionPair(db, new TankDriveFollowPathAction(db, "GalacticSearch_BlueB", false), true);
        }        

        addAction(pact) ;
    }
}
