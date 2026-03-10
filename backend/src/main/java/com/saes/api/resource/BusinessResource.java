package com.saes.api.resource;

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

    private static final Map<Long, Plan> plans = new ConcurrentHashMap<>();
    private static final AtomicLong planId = new AtomicLong(1);

    private static final Map<Long, Subject> subjects = new ConcurrentHashMap<>();
    private static final AtomicLong subjectId = new AtomicLong(1);

    private static final Map<Long, Academy> academies = new ConcurrentHashMap<>();
    private static final AtomicLong academyId = new AtomicLong(1);

    private static final Map<Long, Period> periods = new ConcurrentHashMap<>();
    private static final AtomicLong periodId = new AtomicLong(1);

    private static final Map<Long, Group> groups = new ConcurrentHashMap<>();
    private static final AtomicLong groupId = new AtomicLong(1);

    private static final Map<Long, Schedule> schedules = new ConcurrentHashMap<>();
    private static final AtomicLong scheduleId = new AtomicLong(1);

    private static final Map<Long, Enrollment> enrollments = new ConcurrentHashMap<>();
    private static final AtomicLong enrollmentId = new AtomicLong(1);

    private static final Map<Long, Grade> grades = new ConcurrentHashMap<>();
    private static final AtomicLong gradeId = new AtomicLong(1);

    private static final Map<String, Classroom> classrooms = new ConcurrentHashMap<>();

    public BusinessResource() {
        if (classrooms.isEmpty()) {
            classrooms.put("D-401", new Classroom("D-401", 40, "Teórica"));
            classrooms.put("Lab-3", new Classroom("Lab-3", 24, "Laboratorio"));
        }
    }

    //*********** API-CATALOG ***********

    @POST
    @Path("/plans")
    public Response createPlan(Plan payload, @Context UriInfo uriInfo) {
        long id = planId.getAndIncrement();
        payload.id_plan = id;
        plans.put(id, payload);
        URI uri = uriInfo.getAbsolutePathBuilder().path(String.valueOf(id)).build();
        return Response.created(uri).entity(payload).build();
    }

    @GET
    @Path("/plans/{id}")
    public Response getPlan(@PathParam("id") long id) {
        Plan p = plans.get(id);
        return p == null ? Response.status(Response.Status.NOT_FOUND).build() : Response.ok(p).build();
    }

    @DELETE
    @Path("/plans/{id}")
    public Response deletePlan(@PathParam("id") long id) {
        plans.remove(id);
        return Response.noContent().build();
    }

    @POST
    @Path("/subjects")
    public Response createSubject(Subject payload, @Context UriInfo uriInfo) {
        long id = subjectId.getAndIncrement();
        payload.id_materia = id;
        subjects.put(id, payload);
        URI uri = uriInfo.getAbsolutePathBuilder().path(String.valueOf(id)).build();
        return Response.created(uri).entity(payload).build();
    }

    @GET
    @Path("/subjects")
    public Response listSubjects(@QueryParam("page") @DefaultValue("1") int page,
                                 @QueryParam("limit") @DefaultValue("50") int limit,
                                 @QueryParam("clave") String clave) {
        List<Subject> list = subjects.values().stream()
                .filter(s -> clave == null || clave.equals(s.clave))
                .skip((long) (page - 1) * limit)
                .limit(limit)
                .collect(Collectors.toList());
        Map<String, Object> result = new HashMap<>();
        result.put("data", list);
        result.put("total", subjects.size());
        result.put("page", page);
        result.put("limit", limit);
        return Response.ok(result).build();
    }

    @GET
    @Path("/subjects/{id}")
    public Response getSubject(@PathParam("id") long id) {
        Subject s = subjects.get(id);
        return s == null ? Response.status(Response.Status.NOT_FOUND).build() : Response.ok(s).build();
    }

    @DELETE
    @Path("/subjects/{id}")
    public Response deleteSubject(@PathParam("id") long id) {
        subjects.remove(id);
        return Response.noContent().build();
    }

    @POST
    @Path("/academies")
    public Response createAcademy(Academy payload, @Context UriInfo uriInfo) {
        long id = academyId.getAndIncrement();
        payload.id_academia = id;
        academies.put(id, payload);
        URI uri = uriInfo.getAbsolutePathBuilder().path(String.valueOf(id)).build();
        return Response.created(uri).entity(payload).build();
    }

    @GET
    @Path("/academies")
    public Response listAcademies() {
        return Response.ok(new ArrayList<>(academies.values())).build();
    }

    @GET
    @Path("/academies/{id}")
    public Response getAcademy(@PathParam("id") long id) {
        Academy a = academies.get(id);
        return a == null ? Response.status(Response.Status.NOT_FOUND).build() : Response.ok(a).build();
    }

    @DELETE
    @Path("/academies/{id}")
    public Response deleteAcademy(@PathParam("id") long id) {
        academies.remove(id);
        return Response.noContent().build();
    }

    //*********** API-SCHEDULE ***********

    @POST
    @Path("/periods")
    public Response createPeriod(Period payload, @Context UriInfo uriInfo) {
        long id = periodId.getAndIncrement();
        payload.id_periodo = id;
        payload.estado = "Proximo";
        periods.put(id, payload);
        URI uri = uriInfo.getAbsolutePathBuilder().path(String.valueOf(id)).build();
        return Response.created(uri).entity(payload).build();
    }

    @GET
    @Path("/periods")
    public Response listPeriods(@QueryParam("estado") String estado) {
        List<Period> result = periods.values().stream().filter(p -> estado == null || p.estado.equalsIgnoreCase(estado)).collect(Collectors.toList());
        return Response.ok(result).build();
    }

    @GET
    @Path("/periods/active")
    public Response activePeriod() {
        Optional<Period> opt = periods.values().stream().filter(p -> "Activo".equalsIgnoreCase(p.estado)).findFirst();
        return opt.map(Response::ok).orElse(Response.status(Response.Status.NOT_FOUND)).build();
    }

    @GET
    @Path("/periods/{id}")
    public Response getPeriod(@PathParam("id") long id) {
        Period p = periods.get(id);
        return p == null ? Response.status(Response.Status.NOT_FOUND).build() : Response.ok(p).build();
    }

    @POST
    @Path("/groups")
    public Response createGroup(Group payload, @Context UriInfo uriInfo) {
        if (!periods.containsKey(payload.id_periodo)) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", "Periodo no existe")).build();
        }
        long id = groupId.getAndIncrement();
        payload.id_grupo = id;
        payload.inscritos = 0;
        groups.put(id, payload);
        URI uri = uriInfo.getAbsolutePathBuilder().path(String.valueOf(id)).build();
        return Response.created(uri).entity(payload).build();
    }

    @GET
    @Path("/groups")
    public Response listGroups(@QueryParam("id_periodo") Long id_periodo, @QueryParam("id_materia") Long id_materia, @QueryParam("turno") String turno, @QueryParam("page") @DefaultValue("1") int page, @QueryParam("limit") @DefaultValue("50") int limit) {
        List<Group> filtered = groups.values().stream().filter(g -> (id_periodo == null || Objects.equals(g.id_periodo, id_periodo))
                && (id_materia == null || Objects.equals(g.id_materia, id_materia))
                && (turno == null || turno.equalsIgnoreCase(g.turno))).collect(Collectors.toList());
        int start = (page - 1) * limit;
        int end = Math.min(start + limit, filtered.size());
        List<Group> pageList = start < end ? filtered.subList(start, end) : Collections.emptyList();
        Map<String, Object> result = new HashMap<>();
        result.put("data", pageList);
        result.put("total", filtered.size());
        result.put("page", page);
        result.put("limit", limit);
        return Response.ok(result).build();
    }

    @GET
    @Path("/groups/{id}")
    public Response getGroup(@PathParam("id") long id) {
        Group g = groups.get(id);
        if (g == null) return Response.status(Response.Status.NOT_FOUND).build();
        // calculate inscritos and docente details (dummy)
        g.inscritos = (int) enrollments.values().stream().filter(e -> Objects.equals(e.id_grupo, g.id_grupo) && "Activa".equals(e.estado)).count();
        return Response.ok(g).build();
    }

    @PUT
    @Path("/groups/{id}/teacher")
    public Response assignTeacher(@PathParam("id") long id, Map<String, Object> body) {
        Group g = groups.get(id);
        if (g == null) return Response.status(Response.Status.NOT_FOUND).build();
        Integer id_docente = body.get("id_docente") instanceof Number ? ((Number) body.get("id_docente")).intValue() : null;
        if (id_docente == null) return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error","id_docente requerido")).build();
        g.id_docente = id_docente.longValue();
        Map<String,Object> result = new HashMap<>();
        result.put("id_grupo", g.id_grupo);
        result.put("id_docente", g.id_docente);
        result.put("id_asignacion", id);
        return Response.ok(result).build();
    }

    @POST
    @Path("/groups/{id}/schedules")
    public Response createSchedule(@PathParam("id") long id, Schedule payload, @Context UriInfo uriInfo) {
        if (!groups.containsKey(id)) return Response.status(Response.Status.NOT_FOUND).build();
        long id_horario = scheduleId.getAndIncrement();
        payload.id_horario = id_horario;
        payload.id_grupo = id;
        schedules.put(id_horario, payload);
        URI uri = uriInfo.getAbsolutePathBuilder().path(String.valueOf(id_horario)).build();
        return Response.created(uri).entity(payload).build();
    }

    @GET
    @Path("/groups/{id}/schedules")
    public Response getSchedules(@PathParam("id") long id) {
        if (!groups.containsKey(id)) return Response.status(Response.Status.NOT_FOUND).build();
        List<Schedule> list = schedules.values().stream().filter(s -> Objects.equals(s.id_grupo, id)).collect(Collectors.toList());
        return Response.ok(list).build();
    }

    @GET
    @Path("/schedules/conflicts")
    public Response schedulesConflicts(@QueryParam("id_periodo") Long id_periodo) {
        if (id_periodo == null) return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error","id_periodo requerido")).build();
        List<Map<String,Object>> conflicts = new ArrayList<>();
        List<Schedule> periodSchedules = schedules.values().stream().filter(s -> {
            Group g = groups.get(s.id_grupo);
            return g != null && Objects.equals(g.id_periodo, id_periodo);
        }).collect(Collectors.toList());
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
            boolean disponibile = schedules.values().stream()
                    .filter(s -> c.aula_desc.equals(s.aula_desc) && dia.equalsIgnoreCase(s.dia))
                    .noneMatch(s -> overlap(hora_inicio, hora_fin, s.hora_inicio, s.hora_fin));
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
        Period active = periods.values().stream().filter(p -> periodKey.equals(p.clave)).findFirst().orElse(null);
        if (active == null) return Response.status(Response.Status.NOT_FOUND).build();

        List<Map<String,Object>> available = new ArrayList<>();
        for (Group g : groups.values()) {
            if (!Objects.equals(g.id_periodo, active.id_periodo)) continue;
            int inscritos = (int) enrollments.values().stream().filter(e -> Objects.equals(e.id_grupo, g.id_grupo) && "Activa".equals(e.estado)).count();
            boolean conflicto = false;
            available.add(Map.of("id_grupo", g.id_grupo,
                    "clave_grupo", g.clave_grupo,
                    "materia", subjects.get(g.id_materia) != null ? subjects.get(g.id_materia).nombre : "",
                    "cupo_disponible", Math.max(g.cupo_max - inscritos, 0),
                    "conflicto_horario", conflicto));
        }
        return Response.ok(available).build();
    }

    @POST
    @Path("/enrollments")
    public Response createEnrollment(Enrollment payload, @Context UriInfo uriInfo) {
        if (!groups.containsKey(payload.id_grupo)) return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error","Grupo no existe")).build();
        long id = enrollmentId.getAndIncrement();
        payload.id_inscripcion = id;
        payload.estado = "Activa";
        payload.fecha = LocalDateTime.now();
        payload.fecha_cierre_seleccion = null;
        enrollments.put(id, payload);
        URI uri = uriInfo.getAbsolutePathBuilder().path(String.valueOf(id)).build();
        return Response.created(uri).entity(payload).build();
    }

    @GET
    @Path("/enrollments")
    public Response listEnrollments(@QueryParam("id_alumno") Long id_alumno, @QueryParam("id_grupo") Long id_grupo, @QueryParam("estado") String estado) {
        List<Enrollment> filtered = enrollments.values().stream().filter(e -> (id_alumno == null || Objects.equals(e.id_alumno,id_alumno)) && (id_grupo == null || Objects.equals(e.id_grupo,id_grupo)) && (estado == null || estado.equalsIgnoreCase(e.estado))).collect(Collectors.toList());
        Map<String,Object> result = new HashMap<>();
        result.put("data", filtered);
        result.put("total", filtered.size());
        result.put("page", 1);
        result.put("limit", filtered.size());
        return Response.ok(result).build();
    }

    @GET
    @Path("/enrollments/{id}")
    public Response getEnrollment(@PathParam("id") long id) {
        Enrollment e = enrollments.get(id);
        return e == null ? Response.status(Response.Status.NOT_FOUND).build() : Response.ok(e).build();
    }

    @DELETE
    @Path("/enrollments/{id}")
    public Response deleteEnrollment(@PathParam("id") long id) {
        Enrollment e = enrollments.get(id);
        if (e == null) return Response.status(Response.Status.NOT_FOUND).build();
        // Check conditions
        Period period = periods.get(groups.get(e.id_grupo).id_periodo);
        if (period != null && "Activo".equalsIgnoreCase(period.estado)) {
            return Response.status(Response.Status.FORBIDDEN).entity(Map.of("error","Periodo activo no permite baja")).build();
        }
        if (e.fecha_cierre_seleccion != null) {
            return Response.status(Response.Status.FORBIDDEN).entity(Map.of("error","Cierre de selección ya ejecutado")).build();
        }
        e.estado = "Baja";
        return Response.noContent().build();
    }

    @POST
    @Path("/enrollments/confirm")
    public Response confirmEnrollments(@HeaderParam("Authorization") String auth) {
        // Simula identificar ID alumno del token; aquí 1
        long idAlumno = 1L;
        Period active = periods.values().stream().filter(p -> "Activo".equalsIgnoreCase(p.estado)).findFirst().orElse(null);
        if (active == null) return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error","No hay periodo activo")).build();
        List<Enrollment> list = enrollments.values().stream().filter(e -> Objects.equals(e.id_alumno,idAlumno) && "Activa".equalsIgnoreCase(e.estado) && Objects.equals(groups.get(e.id_grupo).id_periodo, active.id_periodo)).collect(Collectors.toList());
        LocalDateTime now = LocalDateTime.now();
        list.forEach(e -> e.fecha_cierre_seleccion = now);
        return Response.ok(Map.of("confirmadas", list.size(), "fecha_cierre_seleccion", now)).build();
    }

    @PUT
    @Path("/enrollments/{id}/group")
    public Response moveEnrollment(@PathParam("id") long id, Map<String,Object> body) {
        Enrollment e = enrollments.get(id);
        if (e == null) return Response.status(Response.Status.NOT_FOUND).build();
        Long id_grupo = body.get("id_grupo") instanceof Number ? ((Number) body.get("id_grupo")).longValue() : null;
        if (id_grupo == null || !groups.containsKey(id_grupo)) return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error","Grupo destino invalido")).build();
        long anterior = e.id_grupo;
        e.id_grupo = id_grupo;
        return Response.ok(Map.of("id_inscripcion", e.id_inscripcion, "id_grupo_anterior", anterior, "id_grupo_nuevo", id_grupo, "estado", e.estado)).build();
    }

    //*********** API-GRADES ***********

    @PUT
    @Path("/grades/{enrollmentId}")
    public Response updateGrade(@PathParam("enrollmentId") long enrollmentId, Map<String,Object> body) {
        Enrollment e = enrollments.get(enrollmentId);
        if (e == null) return Response.status(Response.Status.NOT_FOUND).build();
        Grade g = grades.values().stream().filter(gr -> Objects.equals(gr.id_inscripcion, enrollmentId)).findFirst().orElse(null);
        if (g == null) {
            g = new Grade();
            g.id_calificacion = gradeId.getAndIncrement();
            g.id_inscripcion = enrollmentId;
        }
        if (body.containsKey("parcial_1")) g.parcial_1 = toDecimal(body.get("parcial_1"));
        if (body.containsKey("parcial_2")) g.parcial_2 = toDecimal(body.get("parcial_2"));
        if (body.containsKey("parcial_3")) g.parcial_3 = toDecimal(body.get("parcial_3"));
        g.definitiva = computeDefinitiva(g);
        g.acreditada = g.definitiva != null && g.definitiva >= 6.0;
        grades.put(g.id_calificacion, g);
        return Response.ok(g).build();
    }

    @GET
    @Path("/grades/students/{id}")
    public Response getGradesByStudent(@PathParam("id") Long id, @QueryParam("id_periodo") Long id_periodo) {
        List<Map<String,Object>> result = new ArrayList<>();
        for (Enrollment e : enrollments.values()) {
            if (!Objects.equals(e.id_alumno, id)) continue;
            Group gr = groups.get(e.id_grupo);
            if (gr == null) continue;
            if (id_periodo != null && !Objects.equals(gr.id_periodo, id_periodo)) continue;
            Grade g = grades.values().stream().filter(grd -> Objects.equals(grd.id_inscripcion, e.id_inscripcion)).findFirst().orElse(new Grade());
            Subject sub = subjects.get(gr.id_materia);
            result.add(Map.of("id_inscripcion", e.id_inscripcion,
                    "materia", sub != null ? sub.nombre : "",
                    "grupo", gr.clave_grupo,
                    "parcial_1", g.parcial_1,
                    "parcial_2", g.parcial_2,
                    "parcial_3", g.parcial_3,
                    "definitiva", g.definitiva,
                    "acreditada", g.acreditada));
        }
        return Response.ok(result).build();
    }

    @GET
    @Path("/grades/groups/{id}")
    public Response getGradesByGroup(@PathParam("id") Long id) {
        List<Map<String,Object>> result = new ArrayList<>();
        for (Enrollment e : enrollments.values()) {
            if (!Objects.equals(e.id_grupo, id)) continue;
            Grade g = grades.values().stream().filter(gr -> Objects.equals(gr.id_inscripcion, e.id_inscripcion)).findFirst().orElse(new Grade());
            result.add(Map.of("id_alumno", e.id_alumno,
                    "boleta", "",
                    "nombre", "",
                    "parcial_1", g.parcial_1,
                    "parcial_2", g.parcial_2,
                    "parcial_3", g.parcial_3,
                    "definitiva", g.definitiva,
                    "acreditada", g.acreditada));
        }
        return Response.ok(result).build();
    }

    private static Double toDecimal(Object v) {
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).doubleValue();
        try { return Double.valueOf(v.toString()); } catch (Exception ex) { return null; }
    }

    private static Double computeDefinitiva(Grade g) {
        if (g.parcial_1 == null || g.parcial_2 == null || g.parcial_3 == null) return null;
        return (g.parcial_1 + g.parcial_2 + g.parcial_3) / 3.0;
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

    // DTO / model simple classes

    public static class Plan { public Long id_plan; public Long id_carrera; public String clave_plan; public String nombre; public Integer total_creditos; public Integer total_semestres; }
    public static class Subject { public Long id_materia; public String clave; public String nombre; public Integer creditos; public Integer horas_teoria; public Integer horas_practica; }
    public static class Academy { public Long id_academia; public String nombre; public Long id_carrera; public Long id_jefe; }
    public static class Period { public Long id_periodo; public String clave; public LocalDate fecha_inicio; public LocalDate fecha_fin; public String tipo; public String estado; }
    public static class Group { public Long id_grupo; public Long id_materia; public Long id_periodo; public String clave_grupo; public Integer cupo_max; public String turno; public Long id_docente; public Integer inscritos; }
    public static class Schedule { public Long id_horario; public Long id_grupo; public String dia; public String hora_inicio; public String hora_fin; public String aula_desc; }
    public static class Enrollment { public Long id_inscripcion; public Long id_alumno; public Long id_grupo; public String estado; public String tipo; public LocalDateTime fecha; public LocalDateTime fecha_cierre_seleccion; }
    public static class Grade { public Long id_calificacion; public Long id_inscripcion; public Double parcial_1; public Double parcial_2; public Double parcial_3; public Double definitiva; public Boolean acreditada; }
    public static class Classroom { public String aula_desc; public Integer capacidad; public String tipo; public Classroom() {} public Classroom(String aula_desc, Integer capacidad, String tipo){this.aula_desc=aula_desc;this.capacidad=capacidad;this.tipo=tipo;} }
    public static class ClassroomAvailability { public String aula_desc; public Boolean disponible; public ClassroomAvailability(String aula_desc, Boolean disponible){this.aula_desc=aula_desc;this.disponible=disponible;} }
}
