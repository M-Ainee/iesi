package io.metadew.iesi.script.execution.data_instruction.time;

import java.time.format.DateTimeFormatter;

import io.metadew.iesi.data.generation.execution.GenerationObjectExecution;
import io.metadew.iesi.script.execution.data_instruction.DataInstruction;

/**
 * @author robbe.berrevoets
 */
public class TimeNow implements DataInstruction
{

	private final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

	private final GenerationObjectExecution generationObjectExecution;

	public TimeNow(GenerationObjectExecution generationObjectExecution)
	{
		this.generationObjectExecution = generationObjectExecution;
	}

	@Override
	public String generateOutput(String parameters)
	{
		return generationObjectExecution.getTime().now().format(TIME_FORMAT);
	}

	@Override
	public String getKeyword()
	{
		return "time.now";
	}

}
