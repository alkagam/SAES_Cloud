document.addEventListener("DOMContentLoaded", function() {
    const navContainer = document.querySelector('.nav-container');
    if (!navContainer) return;

    // 1. Definir la estructura del menú del Alumno
    // Como estamos dentro de la carpeta "alumno", los enlaces son directos
    const menuItems = [
        {
            title: "A. PANEL PRINCIPAL",
            links: [
                { text: "Dashboard / Inicio", href: "alumno-dashboard.html" },
                { text: "Datos Personales", href: "alumno-datos-personales.html" },
                { text: "Datos Académicos", href: "alumno-datos-academicos.html" },
                { text: "Calificaciones", href: "alumno-calificaciones.html" },
                { text: "Kardex / Boleta Global", href: "alumno-kardex.html" }
            ]
        },
        {
            title: "B. INFORMACIÓN ACADÉMICA",
            links: [
                { text: "Calendarios", href: "alumno-calendarios.html" },
                { text: "Mapa Curricular", href: "alumno-mapa.html" },
                { text: "Horarios", href: "alumno-horarios.html" },
                { text: "Evaluación Docente", href: "alumno-evaluacion.html" }
            ]
        },
        {
            title: "C. TRÁMITES E INSCRIPCIONES",
            links: [
                { text: "Cita de Reinscripción", href: "alumno-cita.html" },
                { text: "Inscripción / Reinscripción", href: "alumno-inscripcion.html" },
                { text: "Exámenes a Título (ETS)", href: "alumno-ets.html" },
                { text: "Saberes Previos", href: "alumno-saberes.html" },
                { text: "Servicio Social", href: "alumno-servicio.html" },
                { text: "Titulación", href: "alumno-titulacion.html" },
                { text: "Solicitud de Constancias", href: "alumno-constancias.html" }
            ]
        },
        {
            title: "D. SOPORTE",
            links: [
                { text: "Manual de Usuario", href: "alumno-manual.html" },
                { text: "Levantar Ticket", href: "alumno-ticket.html" }
            ]
        },
        {
            title: "E. SEGURIDAD",
            links: [
                { text: "Gestión de Seguridad", href: "alumno-seguridad.html" },
                { text: "Autenticación 2FA", href: "alumno-2fa.html" },
                { text: "Sesiones Activas", href: "alumno-sesiones.html" }
            ]
        },
        {
            title: "F. SERVICIOS ESTUDIANTILES",
            links: [
                { text: "Datos Médicos", href: "alumno-medico.html" },
                { text: "Actividades (Dep/Cult)", href: "alumno-actividades.html" },
                { text: "Estado de Beca", href: "alumno-becas.html" }
            ]
        }
    ];

    // 2. Generar el HTML
    const currentFile = window.location.pathname.split("/").pop(); // Ej: alumno-dashboard.html
    let navHTML = "";

    menuItems.forEach(section => {
        navHTML += `<div class="nav-section">`;
        navHTML += `<p class="section-title">${section.title}</p>`;
        
        section.links.forEach(link => {
            // Verificar si este link corresponde a la página actual para marcarlo "active"
            const linkFile = link.href.split("/").pop();
            const isActive = linkFile === currentFile ? "active" : "";
            
            navHTML += `<a href="${link.href}" class="nav-item ${isActive}">${link.text}</a>`;
        });
        
        navHTML += `</div>`;
    });

    // 3. Inyectar el menú
    navContainer.innerHTML = navHTML;

    // 4. Lógica de Persistencia de Scroll (Guardar donde estaba)
    const savedScroll = sessionStorage.getItem('sidebarScroll');
    if (savedScroll) {
        navContainer.scrollTop = savedScroll;
    }

    navContainer.addEventListener('click', (e) => {
        if (e.target.tagName === 'A') {
            sessionStorage.setItem('sidebarScroll', navContainer.scrollTop);
        }
    });

    // 5. Lógica visual del Menú Hamburguesa (Cambio de Icono)
    const menuToggle = document.querySelector('.menu-toggle');
    const sidebar = document.querySelector('.sidebar-fixed');

    if (menuToggle && sidebar) {
        menuToggle.addEventListener('click', () => {
            // El toggle de la clase 'active' ya lo hace el HTML inline.
            // Aquí solo nos encargamos de la estética del botón.
            if (menuToggle.innerHTML.includes('☰')) {
                menuToggle.innerHTML = '✕'; // Cambiar a X
            } else {
                menuToggle.innerHTML = '☰'; // Volver a hamburguesa
            }
        });
    }
});