package org.frc2020.droid.automodes;

import org.frc2020.droid.Droid;
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
    private AutoMode near_side_eight_ ;
    private AutoMode near_side_six_ ;
    private AutoMode near_side_ten_ ;
    private AutoMode middle_three_ ;
    private AutoMode far_side_five_ ;    

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
            near_side_eight_ = new NearSideEightAuto(this);
        }
        catch(Exception e) {
            logger.startMessage(MessageType.Error) ;
            logger.add("cannot create automode 'NearSideEightAuto', exception caught - ") ;
            logger.add(e.getMessage()) ;
            logger.endMessage();
        }

        try {
            near_side_six_ = new NearSideSixAuto(this) ;
        }
        catch(Exception e) {
            logger.startMessage(MessageType.Error) ;
            logger.add("cannot create automode 'NearSideSixAuto', exception caught - ") ;
            logger.add(e.getMessage()) ;
            logger.endMessage();
        }
        
        try {
            near_side_ten_ = new NearSideTenAuto(this) ;
        }
        catch(Exception e) {
            logger.startMessage(MessageType.Error) ;
            logger.add("cannot create automode 'NearSideTenAuto', exception caught - ") ;
            logger.add(e.getMessage()) ;
            logger.endMessage();
        }
        
        try {
            middle_three_  = new MiddleAuto(this) ;
        }
        catch(Exception e) {
            logger.startMessage(MessageType.Error) ;
            logger.add("cannot create automode 'MiddleAuto', exception caught - ") ;
            logger.add(e.getMessage()) ;
            logger.endMessage();
        }
        
        try {
            far_side_five_ = new FarSideAuto(this) ;
        }
        catch(Exception e) {
            logger.startMessage(MessageType.Error) ;
            logger.add("cannot create automode 'FarSideAuto', exception caught - ") ;
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
                    setAutoMode(near_side_eight_) ;
                    break ;

                case 1:
                    setAutoMode(near_side_six_) ;
                    break ;

                case 2:
                    setAutoMode(near_side_ten_) ;
                    break ;                        

                case 3:
                    setAutoMode(middle_three_) ;
                    break ;

                case 4:
                    setAutoMode(far_side_five_) ;
                    break ;
            }
        }
    }
}
