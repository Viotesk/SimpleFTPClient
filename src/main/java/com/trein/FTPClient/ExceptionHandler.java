package com.trein.FTPClient;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class ExceptionHandler {
        private Logger log = Logger.getLogger(com.trein.FTPClient.ExceptionHandler.class);
        public void catchException(Level level, Exception e) {
            switch (level.toInt()){
                case Level.ALL_INT:
                case Level.OFF_INT:
                case Level.TRACE_INT:
                    log.trace(e);
                    break;
                case Level.DEBUG_INT:
                    log.debug(e);
                    break;
                case Level.INFO_INT:
                    log.info(e);
                    break;
                case Level.WARN_INT:
                    log.warn(e);
                    break;
                case Level.ERROR_INT:
                    log.error(e);
                    break;
                case Level.FATAL_INT:
                    log.fatal(e);
                    break;
            }
        }
}
