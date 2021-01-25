package org.xero1425.misc;

import java.util.Map;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser ;
import org.apache.commons.csv.CSVRecord ;


/// \file

/// \brief This class loads an projects all of the paths stored in path files.
/// A path is loaded given a name.  The paths directory is searched for a set of
/// files that have the given name as the base name.  These files are loaded and
/// the path can be retreived using the name provided at any future time.
///
public class XeroPathManager
{
    //
    // The message logger, for logging path manager related messages to the log file
    //
    private MessageLogger logger_ ;

    //
    // The message ID for the path logger
    //
    private int logger_id_ ;

    //
    // The set of paths loaded into the path manager
    //
    private Map<String, XeroPath> paths_ ;

    //
    // The base directory for finding path files
    //
    private String basedir_ ;

    //
    // The name of the messages for the logger
    //
    static final private String LoggerName = "pathmanager" ;

    static final private String[] TankExtensions = { "_left.csv", "_right.csv"} ;
    static final private String[] SwerveExtensions = { "_fl.csv", "_fr.csv", "_bl.csv", "_br.csv"} ;

    static final public int LeftWheel = 0 ;
    static final public int RightWheel = 1 ;

    static final public int FLWheel = 0 ;
    static final public int FRWheel = 1 ;
    static final public int BLWheel = 2 ;
    static final public int BRWheel = 3 ;            

    /// \brief create the path manager
    /// \param logger the message logger
    /// \param basedir the base directory where all paths are found
    public XeroPathManager(MessageLogger logger, String basedir) {
        basedir_ = basedir ;
        paths_ = new HashMap<String, XeroPath>() ;
        logger_id_ = logger.registerSubsystem(LoggerName) ;

    }

    /// \brief return the base directory for the path manager
    /// \returns the base directory for the path manager
    public String getBaseDir() {
        return basedir_ ;
    }

    /// \brief load a path from the path data files
    /// The path manager will look for two files named BASEDIR/name.left_ext and BASEDIR/name.right_ext
    /// where BASEDIR is the base directory specified when the path manager was created, name is the
    /// name given in thie call, and left_ext and right_ext are the extensions set in the setExtensions()
    /// call.
    /// \param name the name of the path to load
    /// \param tank if true, load path for tank drive, otherwise 
    public boolean loadPath(String name, DriveType dtype) throws Exception {
        String filename = null ;
        String[] exts ;

        if (dtype == DriveType.Tank)
        {
            exts = TankExtensions ;
        }
        else if (dtype == DriveType.Swerve)
        {
            exts = SwerveExtensions ;
        }
        else
        {
            logger_.startMessage(MessageType.Error, logger_id_) ;
            logger_.add("unknown drive type - cannot load path") ;
            logger_.endMessage();
            return false ;            
        }

        Reader rdr = null ;
        CSVParser parser = null ;
        XeroPath path = new XeroPath(name, dtype) ;
        for(int i = 0 ; i < exts.length ; i++)
        {
            boolean first = true ;

            try {
                filename = basedir_ + "/" + name + exts[i];
                rdr = Files.newBufferedReader(Paths.get(filename)) ;
            }
            catch(Exception ex) {
                logger_.startMessage(MessageType.Error, logger_id_) ;
                logger_.add("cannot load path file (left) '").add(filename).add("' - ").add(ex.getMessage()) ;
                logger_.endMessage();
                return false ;
            }

            try {
                parser = new CSVParser(rdr, CSVFormat.DEFAULT) ;
            }   
            catch(Exception ex) {
                try {
                    parser.close() ;
                    rdr.close() ;
                }
                catch(Exception ex2) {                
                }

                logger_.startMessage(MessageType.Error, logger_id_) ;
                logger_.add("cannot load path '").add(name).add("' - ").add(ex.getMessage()) ;
                logger_.endMessage();                
            }

            Iterator<CSVRecord> iter = parser.iterator() ;
            while (iter.hasNext())
            {
                CSVRecord rec = iter.next() ;

                if (first)
                {
                    first = false ;
                    continue ;
                }

                if (rec.size() != 8)
                {
                    logger_.startMessage(MessageType.Error, logger_id_) ;
                    logger_.add("cannot load path '").add(name) ;
                    logger_.add("' - file '").add(filename).add("' has invalid data") ;
                    logger_.add(rec.getRecordNumber()) ;
                    logger_.endMessage();   
                    return false ;
                }

                XeroPathSegment seg ;
                try {
                    seg = parseCSVRecord(rec) ;
                }
                catch(Exception ex) {
                    logger_.startMessage(MessageType.Error, logger_id_) ;
                    logger_.add("cannot load path '").add(name) ;
                    logger_.add("' - file '").add(filename).add("' has invalid data") ;
                    logger_.add(rec.getRecordNumber()) ;
                    logger_.endMessage();   
                    return false ;
                }

                path.addPathSegment(i, seg) ;
            }
        }

        if (!path.isValid())
        {
            logger_.startMessage(MessageType.Error, logger_id_) ;
            logger_.add("cannot load path '").add(name) ;
            logger_.add("' - file '").add(filename).add("' has invalid data") ;
            logger_.endMessage();   
            return false ;            
        }

        paths_.put(name, path) ;
        return true ;
    }

    /// \brief returns a path given the path name
    /// \exception MissingPathException thrown when asking for a path that does not exist, see hasPath()
    /// \param name the name of the path to return
    /// \returns a path given its name
    public XeroPath getPath(String name) throws MissingPathException {
        XeroPath p = paths_.get(name) ;
        if (p == null)
            throw new MissingPathException(name) ;

        return p ;
    }

    /// \brief returns true if the path manager has loaded a path with the name given
    /// \returns true if the path manager has loaded a path with the name given
    public boolean hasPath(String name) {
        return paths_.containsKey(name) ;
    }

    private XeroPathSegment parseCSVRecord(CSVRecord r) throws NumberFormatException {
        double time, x, y, pos, vel, accel, jerk, heading ;

        time = Double.parseDouble(r.get(0)) ;
        x = Double.parseDouble(r.get(1)) ;
        y = Double.parseDouble(r.get(2)) ;
        pos = Double.parseDouble(r.get(3)) ;
        vel = Double.parseDouble(r.get(4)) ;
        accel = Double.parseDouble(r.get(5)) ;
        jerk = Double.parseDouble(r.get(6)) ;
        heading = Double.parseDouble(r.get(7)) ;

        return new XeroPathSegment(time, x, y, pos, vel, accel, jerk, heading) ;
    }

}