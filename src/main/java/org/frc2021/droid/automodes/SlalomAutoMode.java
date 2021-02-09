package org.frc2021.droid.automodes;

import org.xero1425.base.actions.InvalidActionRequest;
import org.xero1425.base.tankdrive.TankDriveFollowPathAction;
import org.xero1425.base.tankdrive.TankDriveSubsystem;
import org.xero1425.misc.BadParameterTypeException;
import org.xero1425.misc.MissingParameterException;
import org.xero1425.misc.MissingPathException;

public class SlalomAutoMode extends DroidAutoMode {
    public SlalomAutoMode(DroidAutoController ctrl) throws InvalidActionRequest, MissingPathException, BadParameterTypeException, MissingParameterException {
        super(ctrl, "SlalomAutoMode") ;

        TankDriveSubsystem db = getDroidSubsystem().getTankDrive() ; 

        addSubActionPair(db, new TankDriveFollowPathAction(db, "SutoNav_Slalom", false), true);
    }
}
