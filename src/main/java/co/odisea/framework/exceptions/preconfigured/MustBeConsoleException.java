package co.odisea.framework.exceptions.preconfigured;

public class MustBeConsoleException extends PreConfiguredException {

	public MustBeConsoleException() {
		super("You must be console to use this command");
	}

}