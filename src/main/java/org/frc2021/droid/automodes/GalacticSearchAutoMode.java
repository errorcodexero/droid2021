package org.frc2021.droid.automodes;

import org.xero1425.base.actions.InvalidActionRequest;
import org.xero1425.base.tankdrive.TankDriveFollowPathAction;
import org.xero1425.base.tankdrive.TankDriveSubsystem;
import org.xero1425.misc.BadParameterTypeException;
import org.xero1425.misc.MissingParameterException;
import org.xero1425.misc.MissingPathException;

public class GalacticSearchAutoMode extends DroidAutoMode {
    public GalacticSearchAutoMode(DroidAutoController ctrl) throws InvalidActionRequest, MissingPathException, BadParameterTypeException, MissingParameterException {
        super(ctrl, "GalacticSearchAutoMode") ;

        TankDriveSubsystem db = getDroidSubsystem().getTankDrive() ; 
        String which= "reda" ;

        if (which.equals("reda")) {
            addSubActionPair(db, new TankDriveFollowPathAction(db, "GalacticSearch_RedA", false), true);
        }
        else if (which.equals("redb")) {
            addSubActionPair(db, new TankDriveFollowPathAction(db, "GalacticSearch_RedB", false), true);
        }
        else if (which.equals("bluea")) {
            addSubActionPair(db, new TankDriveFollowPathAction(db, "GalacticSearch_BlueA", false), true);
        }
        else if (which.equals("blueb")) {
            addSubActionPair(db, new TankDriveFollowPathAction(db, "GalacticSearch_BlueB", false), true);
        }
        
    }
}
