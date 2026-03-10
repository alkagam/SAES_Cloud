document.addEventListener("DOMContentLoaded", function() {
    const navContainer = document.querySelector('.nav-container');
    if (!navContainer) return;

    // 1. Definir menú de GESTIÓN
    const menuItems = [
        {
            title: "OPERACIONES",
            links: [
                { text: "Panel General", href: "dashboard-gestion.html", icon: "📊" }
            ]
        },
        {
            title: "SEGUIMIENTO ALUMNOS",
            links: [
                { text: "Trayectoria Escolar", href: "gestion-trayectoria.html", icon: "📈" },
                { text: "Inscripciones", href: "gestion-inscripciones.html", icon: "📝" },
                { text: "Boletas y Actas", href: "gestion-boletas.html", icon: "🖨️" }
            ]
        },
        {
            title: "INFRAESTRUCTURA",
            links: [
                { text: "Asignación de Salones", href: "gestion-salones.html", icon: "🏢" },
                { text: "Horarios Maestros", href: "gestion-horarios.html", icon: "📅" }
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

    // 3. Estilos específicos para GESTIÓN (Cyan)
    const style = document.createElement('style');
    style.textContent = `
        :root { --gestion-cyan: #00d4ff; }
        .nav-item:hover { background-color: rgba(0, 212, 255, 0.15) !important; color: var(--gestion-cyan) !important; }
        .nav-item.active { background-color: rgba(0, 212, 255, 0.15) !important; color: var(--gestion-cyan) !important; border-left: 3px solid var(--gestion-cyan) !important; font-weight: 600; }
        .section-title { color: var(--gestion-cyan) !important; opacity: 0.7; }
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