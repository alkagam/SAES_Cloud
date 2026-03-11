package com.saes.api.resource;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.eq;
import org.bson.Document;
import org.bson.types.ObjectId;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Path("")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BusinessResource {

    @Inject
    private MongoDatabase database;

    private MongoCollection<Document> plansCollection;
    private MongoCollection<Document> subjectsCollection;
    private MongoCollection<Document> academiesCollection;
    private MongoCollection<Document> periodsCollection;
    private MongoCollection<Document> groupsCollection;
    private MongoCollection<Document> schedulesCollection;
    private MongoCollection<Document> enrollmentsCollection;
    private MongoCollection<Document> gradesCollection;
    private MongoCollection<Document> studentsCollection;
    private MongoCollection<Document> usersCollection;

    private static final Map<String, Classroom> classrooms = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        if (classrooms.isEmpty()) {
            classrooms.put("D-401", new Classroom("D-401", 40, "Teórica"));
            classrooms.put("Lab-3", new Classroom("Lab-3", 24, "Laboratorio"));
        }
        this.plansCollection = database.getCollection("plans");
        this.subjectsCollection = database.getCollection("subjects");
        this.academiesCollection = database.getCollection("academies");
        this.periodsCollection = database.getCollection("periods");
        this.groupsCollection = database.getCollection("groups");
        this.schedulesCollection = database.getCollection("schedules");
        this.enrollmentsCollection = database.getCollection("enrollments");
        this.gradesCollection = database.getCollection("grades");
        this.studentsCollection = database.getCollection("students");
        this.usersCollection = database.getCollection("users");
    }

    //*********** API-AUTH ***********

    @POST
    @Path("/login")
    public Response login(Map<String, String> credentials) {
        String identifier = credentials.get("identifier");
        String password = credentials.get("password");

        if (identifier == null || password == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", "Identifier y password son requeridos")).build();
        }

        Document userDoc = usersCollection.find(eq("identifier", identifier)).first();

        if (userDoc == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(Map.of("error", "Credenciales incorrectas")).build();
        }

        // WARNING: Plain text password comparison. In a real application, use a secure hashing library like bcrypt.
        String storedPassword = userDoc.getString("password_hash");
        if (!password.equals(storedPassword)) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(Map.of("error", "Credenciales incorrectas")).build();
        }

        // Dummy token generation for the prototype
        String accessToken = UUID.randomUUID().toString().replace("-", "");
        String refreshToken = UUID.randomUUID().toString().replace("-", "");

        Map<String, Object> response = new HashMap<>();
        response.put("access_token", accessToken);
        response.put("refresh_token", refreshToken);
        response.put("expires_in", 900); // 15 minutes
        response.put("rol", userDoc.getString("rol"));
        response.put("id_usuario", userDoc.getObjectId("_id").toString());

        return Response.ok(response).build();
    }

    //*********** API-CATALOG ***********

    @POST
    @Path("/plans")
    public Response createPlan(Plan payload, @Context UriInfo uriInfo) {
        Document doc = new Document("id_carrera", payload.id_carrera)
                .append("clave_plan", payload.clave_plan)
                .append("nombre", payload.nombre)
                .append("total_creditos", payload.total_creditos)
                .append("total_semestres", payload.total_semestres);

        plansCollection.insertOne(doc);
        String id = doc.getObjectId("_id").toString();
        payload.id_plan = id;

        URI uri = uriInfo.getAbsolutePathBuilder().path(id).build();
        return Response.created(uri).entity(payload).build();
    }

    @GET
    @Path("/plans/{id}")
    public Response getPlan(@PathParam("id") String id) {
        Document doc;
        try {
            doc = plansCollection.find(eq("_id", new ObjectId(id))).first();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", "ID de plan inválido")).build();
        }

        if (doc == null) return Response.status(Response.Status.NOT_FOUND).build();

        Plan p = new Plan();
        p.id_plan = doc.getObjectId("_id").toString();
        p.id_carrera = doc.getLong("id_carrera");
        p.clave_plan = doc.getString("clave_plan");
        p.nombre = doc.getString("nombre");
        p.total_creditos = doc.getInteger("total_creditos");
        p.total_semestres = doc.getInteger("total_semestres");
        return Response.ok(p).build();
    }

    @DELETE
    @Path("/plans/{id}")
    public Response deletePlan(@PathParam("id") String id) {
        try {
            plansCollection.deleteOne(eq("_id", new ObjectId(id)));
        } catch (IllegalArgumentException e) {
            // Ignorar si el ID es inválido, el plan no existe de todos modos.
        }
        return Response.noContent().build();
    }

    @POST
    @Path("/subjects")
    public Response createSubject(Subject payload, @Context UriInfo uriInfo) {
        Document doc = new Document("clave", payload.clave)
                .append("nombre", payload.nombre)
                .append("creditos", payload.creditos)
                .append("horas_teoria", payload.horas_teoria)
                .append("horas_practica", payload.horas_practica);

        subjectsCollection.insertOne(doc);
        String id = doc.getObjectId("_id").toString();
        payload.id_materia = id;

        URI uri = uriInfo.getAbsolutePathBuilder().path(id).build();
        return Response.created(uri).entity(payload).build();
    }

    @GET
    @Path("/subjects")
    public Response listSubjects(@QueryParam("page") @DefaultValue("1") int page,
                                 @QueryParam("limit") @DefaultValue("50") int limit,
                                 @QueryParam("clave") String clave) {
        List<Subject> list = new ArrayList<>();
        Document filter = (clave != null && !clave.isEmpty()) ? new Document("clave", clave) : new Document();

        long total = subjectsCollection.countDocuments(filter);

        subjectsCollection.find(filter)
                .skip((page - 1) * limit)
                .limit(limit)
                .forEach(doc -> {
                    Subject s = new Subject();
                    s.id_materia = doc.getObjectId("_id").toString();
                    s.clave = doc.getString("clave");
                    s.nombre = doc.getString("nombre");
                    s.creditos = doc.getInteger("creditos");
                    s.horas_teoria = doc.getInteger("horas_teoria");
                    s.horas_practica = doc.getInteger("horas_practica");
                    list.add(s);
                });

        Map<String, Object> result = new HashMap<>();
        result.put("data", list);
        result.put("total", total);
        result.put("page", page);
        result.put("limit", limit);
        return Response.ok(result).build();
    }

    @GET
    @Path("/subjects/{id}")
    public Response getSubject(@PathParam("id") String id) {
        Document doc;
        try {
            doc = subjectsCollection.find(eq("_id", new ObjectId(id))).first();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", "ID de materia inválido")).build();
        }
        if (doc == null) return Response.status(Response.Status.NOT_FOUND).build();

        Subject s = new Subject();
        s.id_materia = doc.getObjectId("_id").toString();
        s.clave = doc.getString("clave");
        s.nombre = doc.getString("nombre");
        s.creditos = doc.getInteger("creditos");
        s.horas_teoria = doc.getInteger("horas_teoria");
        s.horas_practica = doc.getInteger("horas_practica");
        return Response.ok(s).build();
    }

    @DELETE
    @Path("/subjects/{id}")
    public Response deleteSubject(@PathParam("id") String id) {
        try {
            subjectsCollection.deleteOne(eq("_id", new ObjectId(id)));
        } catch (IllegalArgumentException e) {
            // Ignorar si el ID es inválido, la materia no existe de todos modos.
        }
        return Response.noContent().build();
    }

    @POST
    @Path("/academies")
    public Response createAcademy(Academy payload, @Context UriInfo uriInfo) {
        Document doc = new Document("nombre", payload.nombre)
                .append("id_carrera", payload.id_carrera)
                .append("id_jefe", payload.id_jefe);

        academiesCollection.insertOne(doc);
        String id = doc.getObjectId("_id").toString();
        payload.id_academia = id;

        URI uri = uriInfo.getAbsolutePathBuilder().path(id).build();
        return Response.created(uri).entity(payload).build();
    }

    @GET
    @Path("/academies")
    public Response listAcademies() {
        List<Academy> list = new ArrayList<>();
        academiesCollection.find().forEach(doc -> {
            Academy a = new Academy();
            a.id_academia = doc.getObjectId("_id").toString();
            a.nombre = doc.getString("nombre");
            a.id_carrera = doc.getLong("id_carrera");
            a.id_jefe = doc.getLong("id_jefe");
            list.add(a);
        });
        return Response.ok(list).build();
    }

    @GET
    @Path("/academies/{id}")
    public Response getAcademy(@PathParam("id") String id) {
        Document doc;
        try {
            doc = academiesCollection.find(eq("_id", new ObjectId(id))).first();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", "ID de academia inválido")).build();
        }
        if (doc == null) return Response.status(Response.Status.NOT_FOUND).build();

        Academy a = new Academy();
        a.id_academia = doc.getObjectId("_id").toString();
        a.nombre = doc.getString("nombre");
        a.id_carrera = doc.getLong("id_carrera");
        a.id_jefe = doc.getLong("id_jefe");
        return Response.ok(a).build();
    }

    @DELETE
    @Path("/academies/{id}")
    public Response deleteAcademy(@PathParam("id") String id) {
        try {
            academiesCollection.deleteOne(eq("_id", new ObjectId(id)));
        } catch (IllegalArgumentException e) {
            // Ignorar si el ID es inválido
        }
        return Response.noContent().build();
    }

    //*********** API-USERS (Students part) ***********

    @POST
    @Path("/students")
    public Response createStudent(Student payload, @Context UriInfo uriInfo) {
        // Basic validation
        if (payload.boleta == null || payload.boleta.isBlank() || payload.password == null || payload.password.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", "Boleta y password son requeridos")).build();
        }

        // 1. Create the User account document
        // WARNING: Storing plain text passwords is a major security risk.
        // In a real system, use a strong hashing algorithm like Argon2id or bcrypt.
        Document userDoc = new Document("identifier", payload.boleta)
                .append("password_hash", payload.password) // Storing plain text for now.
                .append("rol", "alumno");
        usersCollection.insertOne(userDoc);
        String userId = userDoc.getObjectId("_id").toString();

        // 2. Create the Student profile document
        Document studentDoc = new Document("boleta", payload.boleta)
                .append("nombre", payload.nombre)
                .append("apellido_paterno", payload.apellido_paterno)
                .append("apellido_materno", payload.apellido_materno)
                .append("curp", payload.curp)
                .append("correo_institucional", payload.correo_institucional)
                .append("correo_personal", payload.correo_personal)
                .append("telefono", payload.telefono)
                .append("fecha_nacimiento", payload.fecha_nacimiento != null ? payload.fecha_nacimiento.toString() : null)
                .append("sexo", payload.sexo)
                .append("id_carrera", payload.id_carrera)
                .append("turno", payload.turno)
                .append("semestre_actual", payload.semestre_actual)
                .append("fecha_ingreso", payload.fecha_ingreso != null ? payload.fecha_ingreso.toString() : null)
                .append("situacion", "Regular") // Default status on creation
                .append("id_usuario", new ObjectId(userId)); // Link to the user account

        studentsCollection.insertOne(studentDoc);
        String studentId = studentDoc.getObjectId("_id").toString();

        // 3. Prepare the response payload as per the documentation
        Map<String, Object> responsePayload = new HashMap<>();
        responsePayload.put("id_alumno", studentId);
        responsePayload.put("boleta", payload.boleta);
        responsePayload.put("id_usuario", userId);

        URI uri = uriInfo.getAbsolutePathBuilder().path(studentId).build();
        return Response.created(uri).entity(responsePayload).build();
    }

    @GET
    @Path("/students/{id}")
    public Response getStudent(@PathParam("id") String id) {
        Document doc;
        try {
            doc = studentsCollection.find(eq("_id", new ObjectId(id))).first();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", "ID de alumno inválido")).build();
        }
        if (doc == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(documentToStudent(doc)).build();
    }

    //*********** API-SCHEDULE ***********

    @POST
    @Path("/periods")
    public Response createPeriod(Period payload, @Context UriInfo uriInfo) {
        Document doc = new Document("clave", payload.clave)
                .append("fecha_inicio", payload.fecha_inicio.toString())
                .append("fecha_fin", payload.fecha_fin.toString())
                .append("tipo", payload.tipo)
                .append("estado", "Proximo"); // Estado inicial

        periodsCollection.insertOne(doc);
        String id = doc.getObjectId("_id").toString();
        payload.id_periodo = id;
        payload.estado = "Proximo";

        URI uri = uriInfo.getAbsolutePathBuilder().path(id).build();
        return Response.created(uri).entity(payload).build();
    }

    @GET
    @Path("/periods")
    public Response listPeriods(@QueryParam("estado") String estado) {
        List<Period> result = new ArrayList<>();
        Document filter = (estado != null && !estado.isEmpty()) ? new Document("estado", new Document("$regex", estado).append("$options", "i")) : new Document();

        periodsCollection.find(filter).forEach(doc -> result.add(documentToPeriod(doc)));
        return Response.ok(result).build();
    }

    @GET
    @Path("/periods/active")
    public Response activePeriod() {
        Document doc = periodsCollection.find(eq("estado", "Activo")).first();
        if (doc == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(documentToPeriod(doc)).build();
    }

    @GET
    @Path("/periods/{id}")
    public Response getPeriod(@PathParam("id") String id) {
        Document doc;
        try {
            doc = periodsCollection.find(eq("_id", new ObjectId(id))).first();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", "ID de período inválido")).build();
        }
        if (doc == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(documentToPeriod(doc)).build();
    }

    private Period documentToPeriod(Document doc) {
        if (doc == null) return null;
        Period p = new Period();
        p.id_periodo = doc.getObjectId("_id").toString();
        p.clave = doc.getString("clave");
        p.fecha_inicio = LocalDate.parse(doc.getString("fecha_inicio"));
        p.fecha_fin = LocalDate.parse(doc.getString("fecha_fin"));
        p.tipo = doc.getString("tipo");
        p.estado = doc.getString("estado");
        return p;
    }

    @POST
    @Path("/groups")
    public Response createGroup(Group payload, @Context UriInfo uriInfo) {
        try {
            if (periodsCollection.countDocuments(eq("_id", new ObjectId(payload.id_periodo))) == 0) {
                return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", "Periodo no existe")).build();
            }
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", "Periodo no existe")).build();
        }

        Document doc = new Document("id_materia", payload.id_materia)
                .append("id_periodo", payload.id_periodo)
                .append("clave_grupo", payload.clave_grupo)
                .append("cupo_max", payload.cupo_max)
                .append("turno", payload.turno)
                .append("id_docente", null)
                .append("inscritos", 0);

        groupsCollection.insertOne(doc);
        String id = doc.getObjectId("_id").toString();
        payload.id_grupo = id;
        payload.inscritos = 0;

        URI uri = uriInfo.getAbsolutePathBuilder().path(id).build();
        return Response.created(uri).entity(payload).build();
    }

    @GET
    @Path("/groups")
    public Response listGroups(@QueryParam("id_periodo") String id_periodo, @QueryParam("id_materia") String id_materia, @QueryParam("turno") String turno, @QueryParam("page") @DefaultValue("1") int page, @QueryParam("limit") @DefaultValue("50") int limit) {
        Document filter = new Document();
        if (id_periodo != null) filter.append("id_periodo", id_periodo);
        if (id_materia != null) filter.append("id_materia", id_materia);
        if (turno != null) filter.append("turno", new Document("$regex", turno).append("$options", "i"));

        long total = groupsCollection.countDocuments(filter);
        List<Group> pageList = new ArrayList<>();
        groupsCollection.find(filter)
                .skip((page - 1) * limit)
                .limit(limit)
                .forEach(doc -> pageList.add(documentToGroup(doc)));

        Map<String, Object> result = new HashMap<>();
        result.put("data", pageList);
        result.put("total", total);
        result.put("page", page);
        result.put("limit", limit);
        return Response.ok(result).build();
    }

    @GET
    @Path("/groups/{id}")
    public Response getGroup(@PathParam("id") String id) {
        Document doc;
        try {
            doc = groupsCollection.find(eq("_id", new ObjectId(id))).first();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", "ID de grupo inválido")).build();
        }
        if (doc == null) return Response.status(Response.Status.NOT_FOUND).build();

        Group group = documentToGroup(doc);
        // calculate inscritos (dummy, from in-memory map)
        group.inscritos = (int) enrollmentsCollection.countDocuments(new Document("id_grupo", group.id_grupo).append("estado", "Activa"));
        return Response.ok(group).build();
    }

    @PUT
    @Path("/groups/{id}/teacher")
    public Response assignTeacher(@PathParam("id") String id, Map<String, Object> body) {
        Integer id_docente = body.get("id_docente") instanceof Number ? ((Number) body.get("id_docente")).intValue() : null;
        if (id_docente == null) return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error","id_docente requerido")).build();

        Document update = new Document("$set", new Document("id_docente", id_docente.longValue()));
        try {
            groupsCollection.updateOne(eq("_id", new ObjectId(id)), update);
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", "ID de grupo inválido")).build();
        }

        Map<String,Object> result = new HashMap<>();
        result.put("id_grupo", id);
        result.put("id_docente", id_docente.longValue());
        result.put("id_asignacion", id);
        return Response.ok(result).build();
    }

    @POST
    @Path("/groups/{id}/schedules")
    public Response createSchedule(@PathParam("id") String groupId, Schedule payload, @Context UriInfo uriInfo) {
        try {
            if (groupsCollection.countDocuments(eq("_id", new ObjectId(groupId))) == 0) {
                return Response.status(Response.Status.NOT_FOUND).entity(Map.of("error", "Grupo no encontrado")).build();
            }
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", "ID de grupo inválido")).build();
        }

        Document doc = new Document("id_grupo", groupId)
                .append("dia", payload.dia)
                .append("hora_inicio", payload.hora_inicio)
                .append("hora_fin", payload.hora_fin)
                .append("aula_desc", payload.aula_desc);

        schedulesCollection.insertOne(doc);
        String id = doc.getObjectId("_id").toString();
        payload.id_horario = id;
        payload.id_grupo = groupId;

        URI uri = uriInfo.getAbsolutePathBuilder().path(id).build();
        return Response.created(uri).entity(payload).build();
    }

    @GET
    @Path("/groups/{id}/schedules")
    public Response getSchedules(@PathParam("id") String groupId) {
        List<Schedule> list = new ArrayList<>();
        schedulesCollection.find(eq("id_grupo", groupId)).forEach(doc -> list.add(documentToSchedule(doc)));
        return Response.ok(list).build();
    }

    @GET
    @Path("/schedules/conflicts")
    public Response schedulesConflicts(@QueryParam("id_periodo") String id_periodo) {
        if (id_periodo == null || id_periodo.isBlank()) return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error","id_periodo requerido")).build();
        List<Map<String,Object>> conflicts = new ArrayList<>();

        // 1. Find all group IDs for the given period
        List<String> groupIds = new ArrayList<>();
        try {
            groupsCollection.find(eq("id_periodo", id_periodo)).forEach(doc -> groupIds.add(doc.getObjectId("_id").toString()));
        } catch (Exception e) {
            // Handle potential invalid period ID if it's an ObjectId
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", "ID de período inválido")).build();
        }

        if (groupIds.isEmpty()) {
            return Response.ok(conflicts).build(); // No groups, no conflicts
        }

        // 2. Find all schedules for those groups
        List<Schedule> periodSchedules = new ArrayList<>();
        schedulesCollection.find(new Document("id_grupo", new Document("$in", groupIds))).forEach(doc -> periodSchedules.add(documentToSchedule(doc)));

        for (int i = 0; i < periodSchedules.size(); i++){
            for (int j = i+1; j < periodSchedules.size(); j++){
                Schedule a = periodSchedules.get(i);
                Schedule b = periodSchedules.get(j);
                if (a.dia.equalsIgnoreCase(b.dia) && overlap(a.hora_inicio,a.hora_fin,b.hora_inicio,b.hora_fin)){
                    conflicts.add(Map.of("id_grupo_a", a.id_grupo, "id_grupo_b", b.id_grupo, "dia", a.dia, "hora_inicio", maxTime(a.hora_inicio,b.hora_inicio), "hora_fin", minTime(a.hora_fin,b.hora_fin)));
                }
            }
        }
        return Response.ok(conflicts).build();
    }

    @GET
    @Path("/classrooms")
    public Response listClassrooms() {
        return Response.ok(new ArrayList<>(classrooms.values())).build();
    }

    @GET
    @Path("/classrooms/availability")
    public Response classroomsAvailability(@QueryParam("dia") String dia,
                                           @QueryParam("hora_inicio") String hora_inicio,
                                           @QueryParam("hora_fin") String hora_fin) {
        List<ClassroomAvailability> list = new ArrayList<>();
        for (Classroom c : classrooms.values()) {
            Document filter = new Document("aula_desc", c.aula_desc)
                    .append("dia", new Document("$regex", dia).append("$options", "i"));

            boolean disponibile = true;
            for (Document scheduleDoc : schedulesCollection.find(filter)) {
                if (overlap(hora_inicio, hora_fin, scheduleDoc.getString("hora_inicio"), scheduleDoc.getString("hora_fin"))) {
                    disponibile = false;
                    break;
                }
            }
            list.add(new ClassroomAvailability(c.aula_desc, disponibile));
        }
        return Response.ok(list).build();
    }

    //*********** API-ENROLLMENTS ***********

    @GET
    @Path("/groups/available")
    public Response groupsAvailable(@QueryParam("period") String periodKey, @QueryParam("student") Long student) {
        if (periodKey == null || student == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", "period y student requeridos")).build();
        }
        Document activePeriodDoc = periodsCollection.find(eq("clave", periodKey)).first();
        if (activePeriodDoc == null) return Response.status(Response.Status.NOT_FOUND).build();
        String activePeriodId = activePeriodDoc.getObjectId("_id").toString();

        List<Map<String,Object>> available = new ArrayList<>();
        groupsCollection.find(eq("id_periodo", activePeriodId)).forEach(doc -> {
            Group g = documentToGroup(doc);
            if (g == null) return;

            int inscritos = (int) enrollmentsCollection.countDocuments(new Document("id_grupo", g.id_grupo).append("estado", "Activa"));
            boolean conflicto = false; // TODO: Implement conflict detection

            String subjectName = "";
            try {
                Document subjectDoc = subjectsCollection.find(eq("_id", new ObjectId(g.id_materia))).first();
                if (subjectDoc != null) {
                    subjectName = subjectDoc.getString("nombre");
                }
            } catch (IllegalArgumentException e) {
                // Invalid subject ID in group, ignore
            }

            available.add(Map.of("id_grupo", g.id_grupo,
                    "clave_grupo", g.clave_grupo,
                    "materia", subjectName,
                    "cupo_disponible", Math.max(g.cupo_max - inscritos, 0),
                    "conflicto_horario", conflicto));
        });
        return Response.ok(available).build();
    }

    @POST
    @Path("/enrollments")
    public Response createEnrollment(Enrollment payload, @Context UriInfo uriInfo) {
        try {
            if (groupsCollection.countDocuments(eq("_id", new ObjectId(payload.id_grupo))) == 0) return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error","Grupo no existe")).build();
        } catch (IllegalArgumentException e) { return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error","Grupo no existe")).build(); }

        Document doc = new Document("id_alumno", payload.id_alumno)
                .append("id_grupo", payload.id_grupo)
                .append("estado", "Activa")
                .append("tipo", payload.tipo)
                .append("fecha", LocalDateTime.now().toString())
                .append("fecha_cierre_seleccion", null);

        enrollmentsCollection.insertOne(doc);
        String id = doc.getObjectId("_id").toString();
        payload.id_inscripcion = id;
        payload.estado = "Activa";
        payload.fecha = LocalDateTime.now();
        payload.fecha_cierre_seleccion = null;

        URI uri = uriInfo.getAbsolutePathBuilder().path(id).build();
        return Response.created(uri).entity(payload).build();
    }

    @GET
    @Path("/enrollments")
    public Response listEnrollments(@QueryParam("id_alumno") Long id_alumno, @QueryParam("id_grupo") String id_grupo, @QueryParam("estado") String estado, @QueryParam("page") @DefaultValue("1") int page, @QueryParam("limit") @DefaultValue("50") int limit) {
        Document filter = new Document();
        if (id_alumno != null) filter.append("id_alumno", id_alumno);
        if (id_grupo != null) filter.append("id_grupo", id_grupo);
        if (estado != null) filter.append("estado", new Document("$regex", estado).append("$options", "i"));

        long total = enrollmentsCollection.countDocuments(filter);
        List<Enrollment> pageList = new ArrayList<>();
        enrollmentsCollection.find(filter)
                .skip((page - 1) * limit)
                .limit(limit)
                .forEach(doc -> pageList.add(documentToEnrollment(doc)));

        Map<String,Object> result = new HashMap<>();
        result.put("data", pageList);
        result.put("total", total);
        result.put("page", page);
        result.put("limit", limit);
        return Response.ok(result).build();
    }

    @GET
    @Path("/enrollments/{id}")
    public Response getEnrollment(@PathParam("id") String id) {
        Document doc;
        try {
            doc = enrollmentsCollection.find(eq("_id", new ObjectId(id))).first();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", "ID de inscripción inválido")).build();
        }
        return doc == null ? Response.status(Response.Status.NOT_FOUND).build() : Response.ok(documentToEnrollment(doc)).build();
    }

    @DELETE
    @Path("/enrollments/{id}")
    public Response deleteEnrollment(@PathParam("id") String id) {
        Document doc;
        try {
            doc = enrollmentsCollection.find(eq("_id", new ObjectId(id))).first();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", "ID de inscripción inválido")).build();
        }
        if (doc == null) return Response.status(Response.Status.NOT_FOUND).build();

        Enrollment e = documentToEnrollment(doc);
        // Check conditions
        Document groupDoc = groupsCollection.find(eq("_id", new ObjectId(e.id_grupo))).first();
        if (groupDoc != null) {
            Document periodDoc = periodsCollection.find(eq("_id", new ObjectId(groupDoc.getString("id_periodo")))).first();
            if (periodDoc != null && "Activo".equalsIgnoreCase(periodDoc.getString("estado")))
                return Response.status(Response.Status.FORBIDDEN).entity(Map.of("error","Periodo activo no permite baja")).build();
        }
        if (doc.get("fecha_cierre_seleccion") != null) {
            return Response.status(Response.Status.FORBIDDEN).entity(Map.of("error","Cierre de selección ya ejecutado")).build();
        }
        enrollmentsCollection.updateOne(eq("_id", new ObjectId(id)), new Document("$set", new Document("estado", "Baja")));
        return Response.noContent().build();
    }

    @POST
    @Path("/enrollments/confirm")
    public Response confirmEnrollments(@HeaderParam("Authorization") String auth) {
        // Simula identificar ID alumno del token; aquí 1
        long idAlumno = 1L;
        Document activePeriodDoc = periodsCollection.find(eq("estado", "Activo")).first();
        if (activePeriodDoc == null) return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error","No hay periodo activo")).build();
        String activePeriodId = activePeriodDoc.getObjectId("_id").toString();

        List<String> groupIdsInPeriod = new ArrayList<>();
        groupsCollection.find(eq("id_periodo", activePeriodId)).forEach(doc -> groupIdsInPeriod.add(doc.getObjectId("_id").toString()));

        Document filter = new Document("id_alumno", idAlumno)
                .append("estado", "Activa")
                .append("id_grupo", new Document("$in", groupIdsInPeriod));

        String now = LocalDateTime.now().toString();
        long modifiedCount = enrollmentsCollection.updateMany(filter, new Document("$set", new Document("fecha_cierre_seleccion", now))).getModifiedCount();

        return Response.ok(Map.of("confirmadas", modifiedCount, "fecha_cierre_seleccion", now)).build();
    }

    @PUT
    @Path("/enrollments/{id}/group")
    public Response moveEnrollment(@PathParam("id") String id, Map<String,Object> body) {
        String id_grupo = body.get("id_grupo") != null ? body.get("id_grupo").toString() : null;
        Document groupDoc = groupsCollection.find(eq("_id", new ObjectId(id_grupo))).first();
        if (id_grupo == null || groupDoc == null) return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error","Grupo destino invalido")).build();
        Document enrollmentDoc = enrollmentsCollection.findOneAndUpdate(eq("_id", new ObjectId(id)), new Document("$set", new Document("id_grupo", id_grupo)));
        if (enrollmentDoc == null) return Response.status(Response.Status.NOT_FOUND).build();
        String anterior = enrollmentDoc.getString("id_grupo");
        return Response.ok(Map.of("id_inscripcion", id, "id_grupo_anterior", anterior, "id_grupo_nuevo", id_grupo, "estado", "Activa")).build();
    }

    //*********** API-GRADES ***********

    @PUT
    @Path("/grades/{enrollmentId}")
    public Response updateGrade(@PathParam("enrollmentId") String enrollmentId, Map<String,Object> body) {
        try { // Validar que la inscripción exista
            if (enrollmentsCollection.countDocuments(eq("_id", new ObjectId(enrollmentId))) == 0)
                return Response.status(Response.Status.NOT_FOUND).entity(Map.of("error", "Inscripción no encontrada")).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", "ID de inscripción inválido")).build();
        }

        Document currentGradeDoc = gradesCollection.find(eq("id_inscripcion", enrollmentId)).first();
        Document updates = new Document();
        if (body.containsKey("parcial_1")) updates.append("parcial_1", toDecimal(body.get("parcial_1")));
        if (body.containsKey("parcial_2")) updates.append("parcial_2", toDecimal(body.get("parcial_2")));
        if (body.containsKey("parcial_3")) updates.append("parcial_3", toDecimal(body.get("parcial_3")));

        Document fullDoc = new Document();
        if (currentGradeDoc != null) {
            fullDoc.putAll(currentGradeDoc);
        }
        fullDoc.putAll(updates); // Sobrescribe con los nuevos valores

        Double definitiva = computeDefinitiva(fullDoc);
        updates.append("definitiva", definitiva);
        updates.append("acreditada", definitiva != null && definitiva >= 6.0);

        gradesCollection.updateOne(eq("id_inscripcion", enrollmentId), new Document("$set", updates), new com.mongodb.client.model.UpdateOptions().upsert(true));
        
        Grade g = documentToGrade(gradesCollection.find(eq("id_inscripcion", enrollmentId)).first());
        return Response.ok(g).build();
    }

    @GET
    @Path("/grades/students/{id}")
    public Response getGradesByStudent(@PathParam("id") Long id, @QueryParam("id_periodo") Long id_periodo) {
        List<Map<String,Object>> result = new ArrayList<>();
        enrollmentsCollection.find(eq("id_alumno", id)).forEach(enrollmentDoc -> {
            Enrollment e = documentToEnrollment(enrollmentDoc);
            if (e == null) return;
            Document groupDoc = groupsCollection.find(eq("_id", new ObjectId(e.id_grupo))).first();
            if (groupDoc == null) return;
            if (id_periodo != null && !Objects.equals(groupDoc.getString("id_periodo"), id_periodo.toString())) return;

            Grade g = documentToGrade(gradesCollection.find(eq("id_inscripcion", e.id_inscripcion)).first());
            String subjectName = "";
            Document subjectDoc = subjectsCollection.find(eq("_id", new ObjectId(groupDoc.getString("id_materia")))).first();
            if (subjectDoc != null) {
                subjectName = subjectDoc.getString("nombre");
            }
            result.add(Map.of("id_inscripcion", e.id_inscripcion,
                    "materia", subjectName,
                    "grupo", groupDoc.getString("clave_grupo"),
                    "parcial_1", g != null ? g.parcial_1 : null,
                    "parcial_2", g != null ? g.parcial_2 : null,
                    "parcial_3", g != null ? g.parcial_3 : null,
                    "definitiva", g != null ? g.definitiva : null,
                    "acreditada", g != null ? g.acreditada : null));
        });
        return Response.ok(result).build();
    }

    @GET
    @Path("/grades/groups/{id}")
    public Response getGradesByGroup(@PathParam("id") String id) {
        List<Map<String,Object>> result = new ArrayList<>();
        enrollmentsCollection.find(eq("id_grupo", id)).forEach(enrollmentDoc -> {
            Enrollment e = documentToEnrollment(enrollmentDoc);
            if (e == null) return;

            Grade g = documentToGrade(gradesCollection.find(eq("id_inscripcion", e.id_inscripcion)).first());
            result.add(Map.of("id_alumno", e.id_alumno,
                    "boleta", "",
                    "nombre", "",
                    "parcial_1", g != null ? g.parcial_1 : null,
                    "parcial_2", g != null ? g.parcial_2 : null,
                    "parcial_3", g != null ? g.parcial_3 : null,
                    "definitiva", g != null ? g.definitiva : null,
                    "acreditada", g != null ? g.acreditada : null));
        });
        return Response.ok(result).build();
    }

    private static Double toDecimal(Object v) {
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).doubleValue();
        try { return Double.valueOf(v.toString()); } catch (Exception ex) { return null; }
    }

    private static Double computeDefinitiva(Document gradeDoc) {
        Double p1 = gradeDoc.getDouble("parcial_1");
        Double p2 = gradeDoc.getDouble("parcial_2");
        Double p3 = gradeDoc.getDouble("parcial_3");
        if (p1 == null || p2 == null || p3 == null) return null;
        return (p1 + p2 + p3) / 3.0;
    }

    private static boolean overlap(String startA, String endA, String startB, String endB) {
        if (startA == null || endA == null || startB == null || endB == null) return false;
        LocalDateTime a = LocalDateTime.parse("2000-01-01T" + startA + ":00");
        LocalDateTime b = LocalDateTime.parse("2000-01-01T" + endA + ":00");
        LocalDateTime c = LocalDateTime.parse("2000-01-01T" + startB + ":00");
        LocalDateTime d = LocalDateTime.parse("2000-01-01T" + endB + ":00");
        return a.isBefore(d) && c.isBefore(b);
    }

    private static String maxTime(String a, String b) { return a.compareTo(b) >= 0 ? a : b; }
    private static String minTime(String a, String b) { return a.compareTo(b) <= 0 ? a : b; }

    private Group documentToGroup(Document doc) {
        if (doc == null) return null;
        Group g = new Group();
        g.id_grupo = doc.getObjectId("_id").toString();
        g.id_materia = doc.getString("id_materia");
        g.id_periodo = doc.getString("id_periodo");
        g.clave_grupo = doc.getString("clave_grupo");
        g.cupo_max = doc.getInteger("cupo_max");
        g.turno = doc.getString("turno");
        g.id_docente = doc.getLong("id_docente");
        g.inscritos = doc.getInteger("inscritos");
        return g;
    }

    private Schedule documentToSchedule(Document doc) {
        if (doc == null) return null;
        Schedule s = new Schedule();
        s.id_horario = doc.getObjectId("_id").toString();
        s.id_grupo = doc.getString("id_grupo");
        s.dia = doc.getString("dia");
        s.hora_inicio = doc.getString("hora_inicio");
        s.hora_fin = doc.getString("hora_fin");
        s.aula_desc = doc.getString("aula_desc");
        return s;
    }

    private Enrollment documentToEnrollment(Document doc) {
        if (doc == null) return null;
        Enrollment e = new Enrollment();
        e.id_inscripcion = doc.getObjectId("_id").toString();
        e.id_alumno = doc.getLong("id_alumno");
        e.id_grupo = doc.getString("id_grupo");
        e.estado = doc.getString("estado");
        e.tipo = doc.getString("tipo");
        e.fecha = doc.getString("fecha") != null ? LocalDateTime.parse(doc.getString("fecha")) : null;
        e.fecha_cierre_seleccion = doc.getString("fecha_cierre_seleccion") != null ? LocalDateTime.parse(doc.getString("fecha_cierre_seleccion")) : null;
        return e;
    }

    private Grade documentToGrade(Document doc) {
        if (doc == null) return null;
        Grade g = new Grade();
        g.id_calificacion = doc.getObjectId("_id").toString();
        g.id_inscripcion = doc.getString("id_inscripcion");
        g.parcial_1 = doc.getDouble("parcial_1");
        g.parcial_2 = doc.getDouble("parcial_2");
        g.parcial_3 = doc.getDouble("parcial_3");
        g.definitiva = doc.getDouble("definitiva");
        g.acreditada = doc.getBoolean("acreditada");
        return g;
    }

    private Student documentToStudent(Document doc) {
        if (doc == null) return null;
        Student s = new Student();
        s.id_alumno = doc.getObjectId("_id").toString();
        s.boleta = doc.getString("boleta");
        s.nombre = doc.getString("nombre");
        s.apellido_paterno = doc.getString("apellido_paterno");
        s.apellido_materno = doc.getString("apellido_materno");
        s.correo_institucional = doc.getString("correo_institucional");
        s.correo_personal = doc.getString("correo_personal");
        s.telefono = doc.getString("telefono");
        s.id_carrera = doc.getLong("id_carrera");
        s.turno = doc.getString("turno");
        s.situacion = doc.getString("situacion");
        s.semestre_actual = doc.getInteger("semestre_actual");
        String fechaIngresoStr = doc.getString("fecha_ingreso");
        if (fechaIngresoStr != null) s.fecha_ingreso = LocalDate.parse(fechaIngresoStr);

        ObjectId userId = doc.getObjectId("id_usuario");
        if (userId != null) s.id_usuario = userId.toString();

        return s;
    }


    // DTO / model simple classes

    public static class Plan { public String id_plan; public Long id_carrera; public String clave_plan; public String nombre; public Integer total_creditos; public Integer total_semestres; }
    public static class Subject { public String id_materia; public String clave; public String nombre; public Integer creditos; public Integer horas_teoria; public Integer horas_practica; }
    public static class Academy { public String id_academia; public String nombre; public Long id_carrera; public Long id_jefe; }
    public static class Period { public String id_periodo; public String clave; public LocalDate fecha_inicio; public LocalDate fecha_fin; public String tipo; public String estado; }
    public static class Group { public String id_grupo; public String id_materia; public String id_periodo; public String clave_grupo; public Integer cupo_max; public String turno; public Long id_docente; public Integer inscritos; }
    public static class Schedule { public String id_horario; public String id_grupo; public String dia; public String hora_inicio; public String hora_fin; public String aula_desc; }
    public static class Enrollment { public String id_inscripcion; public Long id_alumno; public String id_grupo; public String estado; public String tipo; public LocalDateTime fecha; public LocalDateTime fecha_cierre_seleccion; }
    public static class Grade { public String id_calificacion; public String id_inscripcion; public Double parcial_1; public Double parcial_2; public Double parcial_3; public Double definitiva; public Boolean acreditada; }
    public static class Classroom { public String aula_desc; public Integer capacidad; public String tipo; public Classroom() {} public Classroom(String aula_desc, Integer capacidad, String tipo){this.aula_desc=aula_desc;this.capacidad=capacidad;this.tipo=tipo;} }
    public static class ClassroomAvailability { public String aula_desc; public Boolean disponible; public ClassroomAvailability(String aula_desc, Boolean disponible){this.aula_desc=aula_desc;this.disponible=disponible;} }
    public static class Student { public String id_alumno; public String boleta; public String nombre; public String apellido_paterno; public String apellido_materno; public String curp; public String correo_institucional; public String correo_personal; public String telefono; public LocalDate fecha_nacimiento; public String sexo; public Long id_carrera; public String turno; public Integer semestre_actual; public LocalDate fecha_ingreso; public String situacion; public String password; public String id_usuario; }
}
