package fr.radiofrance.alarm.exception;

public class RfAlarmException extends Exception {

    public RfAlarmException() {
    }

    public RfAlarmException(final String message) {
        super(message);
    }

    public RfAlarmException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public RfAlarmException(final Throwable cause) {
        super(cause);
    }

}
