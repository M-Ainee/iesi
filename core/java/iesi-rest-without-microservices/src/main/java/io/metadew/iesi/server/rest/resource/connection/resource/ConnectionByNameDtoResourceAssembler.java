package io.metadew.iesi.server.rest.resource.connection.resource;

import io.metadew.iesi.metadata.definition.connection.Connection;
import io.metadew.iesi.server.rest.controller.ConnectionsController;
import io.metadew.iesi.server.rest.resource.connection.dto.ConnectionByNameDto;
import org.modelmapper.ModelMapper;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ConnectionByNameDtoResourceAssembler extends RepresentationModelAssemblerSupport<List<Connection>, ConnectionByNameDto> {

    private final ModelMapper modelMapper;

    public ConnectionByNameDtoResourceAssembler() {
        super(ConnectionsController.class, ConnectionByNameDto.class);
        this.modelMapper = new ModelMapper();
    }

    @Override
    public ConnectionByNameDto toModel(List<Connection> connections) {
        ConnectionByNameDto connectionByNameDto = convertToDto(connections);
        for (String environment : connectionByNameDto.getEnvironments()) {
            connectionByNameDto.add(
                    linkTo(methodOn(ConnectionsController.class).get(connectionByNameDto.getName(), environment))
                    .withRel("connection:"+connectionByNameDto.getName()+"-"+environment));
        }
        return connectionByNameDto;
    }

    private ConnectionByNameDto convertToDto(List<Connection> connections) {

        ConnectionByNameDto connectionByNameDto = modelMapper.map(connections.get(0), ConnectionByNameDto.class);
        connectionByNameDto.setEnvironments(connections.stream()
                .map(Connection::getEnvironment)
                .collect(Collectors.toList()));
        return connectionByNameDto;
    }
}
