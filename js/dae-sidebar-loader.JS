document.addEventListener("DOMContentLoaded", function() {
    const navContainer = document.querySelector('.nav-container');
    if (!navContainer) return;

    // 1. Definir la estructura del menú de DAE
    const menuItems = [
        {
            title: "ADMINISTRACIÓN",
            links: [
                { text: "Panel de Control", href: "dashboard-dae.html", icon: "📊" },
                { text: "Alta de Alumno", href: "dae-alta-alumno.html", icon: "➕" },
                { text: "Gestión de Alumnos", href: "#", icon: "👥" } // Placeholder
            ]
        },
        {
            title: "ACADÉMICO",
            links: [
                { text: "Actas y Calificaciones", href: "#", icon: "📚" }, // Placeholder
                { text: "Gestión de Grupos", href: "#", icon: "⚖️" } // Placeholder
            ]
        }
    ];

    // 2. Generar el HTML del menú
    const currentFile = window.location.pathname.split("/").pop();
    let navHTML = "";

    menuItems.forEach(section => {
        navHTML += `<div class="nav-section">`;
        navHTML += `<p class="section-title">${section.title}</p>`;
        section.links.forEach(link => {
            const isActive = link.href === currentFile ? "active" : "";
            navHTML += `<a href="${link.href}" class="nav-item ${isActive}"><span style="margin-right: 10px;">${link.icon}</span> ${link.text}</a>`;
        });
        navHTML += `</div>`;
    });

    navContainer.innerHTML = navHTML;

    // 3. Lógica del Menú Hamburguesa para Móvil
    const menuToggle = document.querySelector('.menu-toggle');
    if (menuToggle) {
        menuToggle.addEventListener('click', () => document.querySelector('.sidebar-fixed').classList.toggle('active'));
    }
});