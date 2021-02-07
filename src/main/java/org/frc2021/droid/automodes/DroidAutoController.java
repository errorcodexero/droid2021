package org.frc2021.droid.automodes;

import org.frc2021.droid.Droid;
import org.xero1425.base.controllers.AutoController;
import org.xero1425.base.controllers.AutoMode;
import org.xero1425.misc.BadParameterTypeException;
import org.xero1425.misc.MessageLogger;
import org.xero1425.misc.MessageType;
import org.xero1425.misc.MissingParameterException;

//
// This class is the automode controller for Droid.  It basically creates all of the
// automodes and picks one based on test mode and the automode controller selected.
//
public class DroidAutoController extends AutoController {
    private AutoMode test_mode_ ;
    private AutoMode bounce_ ;
    private AutoMode galactic_search_ ;
    private AutoMode barrel_ ;
    private AutoMode slalom_ ;

    public DroidAutoController(Droid robot) throws MissingParameterException, BadParameterTypeException {
        super(robot, "droid-auto");

        MessageLogger logger = getRobot().getMessageLogger() ;
        
        try {
            test_mode_ = new DroidTestAutoMode(this);
        }
        catch(Exception e) {
            logger.startMessage(MessageType.Error) ;
            logger.add("cannot create automode 'DroidTestAutoMode', exception caught - ") ;
            logger.add(e.getMessage()) ;
            logger.endMessage();
        }

        try {
            bounce_ = new NearSideEightAuto(this);
        }
        catch(Exception e) {
            logger.startMessage(MessageType.Error) ;
            logger.add("cannot create automode 'NearSideEightAuto', exception caught - ") ;
            logger.add(e.getMessage()) ;
            logger.endMessage();
        }
    }

    public void updateAutoMode(int mode, String gamedata) {
        if (isTestMode()) {
            setAutoMode(test_mode_) ;
        }
        else {
            switch(mode) {
                case 0:
                    setAutoMode(bounce_) ;
                    break ;
            }
        }
    }
}