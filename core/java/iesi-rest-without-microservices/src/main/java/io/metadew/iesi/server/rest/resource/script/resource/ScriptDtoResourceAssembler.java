package io.metadew.iesi.server.rest.resource.script.resource;


import io.metadew.iesi.metadata.definition.Script;
import io.metadew.iesi.server.rest.controller.ScriptController;
import io.metadew.iesi.server.rest.resource.script.dto.ScriptActionDto;
import io.metadew.iesi.server.rest.resource.script.dto.ScriptDto;
import io.metadew.iesi.server.rest.resource.script.dto.ScriptVersionDto;
import org.modelmapper.ModelMapper;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Component
public class ScriptDtoResourceAssembler extends ResourceAssemblerSupport<Script, ScriptDto> {

    private final ModelMapper modelMapper;

    public ScriptDtoResourceAssembler() {
        super(ScriptController.class, ScriptDto.class);
        this.modelMapper = new ModelMapper();
    }

    @Override
    public ScriptDto toResource(Script script) {
        ScriptDto scriptByNameDto = convertToDto(script);
        Link selfLink = linkTo(methodOn(ScriptController.class).getScriptsAndVersion(script.getName(),
                script.getVersion().getNumber()))
                .withRel("script:" + scriptByNameDto.getName() + "-" + scriptByNameDto.getVersion().getNumber());
        scriptByNameDto.add(selfLink);
        Link versionLink = linkTo(methodOn(ScriptController.class).getByNameScript(script.getName()))
                .withRel("script");
        scriptByNameDto.add(versionLink);
        return scriptByNameDto;
    }

    private ScriptDto convertToDto(Script script) {
        return new ScriptDto(script.getName(), script.getType(), script.getDescription(),
                ScriptVersionDto.convertToDto(script.getVersion()), script.getParameters(),
                script.getActions().stream().map(ScriptActionDto :: convertToDto).collect(Collectors.toList()));
    }
}