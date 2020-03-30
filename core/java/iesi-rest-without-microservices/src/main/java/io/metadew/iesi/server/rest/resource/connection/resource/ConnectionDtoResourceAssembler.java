package io.metadew.iesi.server.rest.resource.connection.resource;

import io.metadew.iesi.metadata.definition.connection.Connection;
import io.metadew.iesi.server.rest.controller.ConnectionsController;
import io.metadew.iesi.server.rest.controller.EnvironmentsController;
import io.metadew.iesi.server.rest.resource.connection.dto.ConnectionDto;
import org.modelmapper.ModelMapper;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ConnectionDtoResourceAssembler extends RepresentationModelAssemblerSupport<Connection, ConnectionDto> {

    private ModelMapper modelMapper;

    public ConnectionDtoResourceAssembler() {
        super(ConnectionsController.class, ConnectionDto.class);
        this.modelMapper = new ModelMapper();
    }

    @Override
    public ConnectionDto toModel(Connection connection) {
        ConnectionDto connectionDto = convertToDto(connection);
        Link selfLink = linkTo(methodOn(ConnectionsController.class).get(connection.getName(), connection.getEnvironment()))
                .withSelfRel();
        connectionDto.add(selfLink);
        Link environmentLink = linkTo(methodOn(EnvironmentsController.class).getByName(connection.getEnvironment()))
                .withRel("environment");
        connectionDto.add(environmentLink);
        return connectionDto;
    }

    private ConnectionDto convertToDto(Connection connection) {
        return modelMapper.map(connection, ConnectionDto.class);
    }
}
