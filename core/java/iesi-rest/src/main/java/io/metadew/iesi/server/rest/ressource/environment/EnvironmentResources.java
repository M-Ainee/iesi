package io.metadew.iesi.server.rest.ressource.environment;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

import java.util.List;

import org.springframework.hateoas.ResourceSupport;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.metadew.iesi.metadata.definition.Environment;
import io.metadew.iesi.server.rest.controller.EnvironmentsController;
import lombok.Getter;

@Getter
public class EnvironmentResources extends ResourceSupport {
	@JsonProperty(value = "result")
	private final List<Environment> environment;

	public EnvironmentResources(final List<Environment> environment) {

		this.environment = (List<Environment>) environment;
		add(linkTo(EnvironmentsController.class).withSelfRel());
		add(linkTo(EnvironmentsController.class).withRel("environment"));

	}

}