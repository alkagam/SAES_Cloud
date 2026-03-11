document.addEventListener("DOMContentLoaded", function() {
    const navContainer = document.querySelector('.nav-container');
    if (!navContainer) return;

    // 1. Definir menú de DAE (CONTROL ESCOLAR)
    const menuItems = [
        {
            title: "ADMINISTRACIÓN",
            links: [
                { text: "Panel de Control", href: "dashboard-dae.html", icon: "📊" },
                { text: "Gestión de Alumnos", href: "dae-alumnos.html", icon: "👥" },
                { text: "Inscripciones", href: "dae-inscripciones.html", icon: "📝" }
            ]
        },
        {
            title: "ACADÉMICO",
            links: [
                { text: "Actas y Calificaciones", href: "dae-actas.html", icon: "📚" },
                { text: "Dictámenes COSIE", href: "dae-dictamenes.html", icon: "⚖️" }
            ]
        },
        {
            title: "SISTEMA",
            links: [
                { text: "Reportes Globales", href: "dae-reportes.html", icon: "📈" },
                { text: "Configuración", href: "dae-settings.html", icon: "⚙️" },
                { text: "API Reference", href: "api-reference.html", icon: "📖" }
            ]
        }
    ];

    // 2. Generar HTML
    const currentFile = window.location.pathname.split("/").pop();
    let navHTML = "";

    menuItems.forEach(section => {
        navHTML += `<div class="nav-section">`;
        navHTML += `<p class="section-title">${section.title}</p>`;
        section.links.forEach(link => {
            const isActive = link.href === currentFile ? "active" : "";
            navHTML += `<a href="${link.href}" class="nav-item ${isActive}">
                            <span style="margin-right: 10px;">${link.icon}</span> ${link.text}
                        </a>`;
        });
        navHTML += `</div>`;
    });

    navContainer.innerHTML = navHTML;

    // 3. Estilos específicos para DAE (Rojo Neón / Guinda)
    const style = document.createElement('style');
    style.textContent = `
        :root { --dae-primary: #ff0055; }
        .nav-item:hover { background-color: rgba(255, 0, 85, 0.15) !important; color: var(--dae-primary) !important; }
        .nav-item.active { background-color: rgba(255, 0, 85, 0.15) !important; color: var(--dae-primary) !important; border-left: 3px solid var(--dae-primary) !important; font-weight: 600; }
        .section-title { color: var(--dae-primary) !important; opacity: 0.7; }
        .user-dot { background: var(--dae-primary) !important; box-shadow: 0 0 10px var(--dae-primary) !important; }
    `;
    document.head.appendChild(style);

    // 4. Toggle Menú Móvil
    const menuToggle = document.querySelector('.menu-toggle');
    if (menuToggle) {
        menuToggle.addEventListener('click', () => {
            const sidebar = document.querySelector('.sidebar-fixed');
            sidebar.classList.toggle('active');
            menuToggle.innerHTML = sidebar.classList.contains('active') ? '✕' : '☰';
        });
    }
});