package com.saes.api.resource;

import com.saes.api.dto.CareerDTO;
import com.saes.api.service.CareerService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.util.List;

@Path("/careers")
@Produces("application/json")
@Consumes("application/json")
public class CareerResource {

    @Inject
    private CareerService careerService;

    @GET
    public Response list(@QueryParam("activa") Boolean activa) {
        List<CareerDTO> careers = careerService.list(activa);
        return Response.ok(careers).build();
    }

    @POST
    public Response create(CareerDTO payload, @Context javax.ws.rs.core.UriInfo uriInfo) {
        CareerDTO created = careerService.create(payload);
        URI uri = uriInfo.getAbsolutePathBuilder().path(String.valueOf(created.getId_carrera())).build();
        return Response.created(uri).entity(created).build();
    }

    @GET
    @Path("{id}")
    public Response getById(@PathParam("id") Long id) {
        CareerDTO career = careerService.findById(id);
        if (career == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(career).build();
    }

    @DELETE
    @Path("{id}")
    public Response delete(@PathParam("id") Long id) {
        careerService.delete(id);
        return Response.noContent().build();
    }
}
