package eu.compassresearch.core.interpreter.debug;

import eu.compassresearch.core.interpreter.debug.messaging.AbstractMessage;
import eu.compassresearch.core.interpreter.debug.messaging.MessageType;

public class CmlDbgCommandMessage extends AbstractMessage
{

	protected CmlDebugCommand command;

	protected CmlDbgCommandMessage()
	{
	}

	public CmlDbgCommandMessage(CmlDebugCommand command)
	{
		setCommand(command);
	}

	public CmlDbgCommandMessage(CmlDebugCommand command, Object content)
	{
		setCommand(command);
		setContent(content);
	}

	public CmlDebugCommand getCommand()
	{
		return command;
	}

	public void setCommand(CmlDebugCommand command)
	{
		this.command = command;
	}

	@Override
	public MessageType getType()
	{
		return MessageType.COMMAND;
	}

	@Override
	public String getKey()
	{
		return getCommand().toString();
	}

	@Override
	public String toString()
	{

		return getCommand().toString();
	}
}
