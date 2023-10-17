package co.odisea.framework.exceptions.preconfigured;


import static co.odisea.utils.Extensions.isNullOrEmpty;

public class NoPermissionException extends PreConfiguredException {

	public NoPermissionException() {
		this(null);
	}

	public NoPermissionException(String extra) {
		super("You don't have permission to do that!" + (isNullOrEmpty(extra) ? "" : " " + extra));
	}

}
