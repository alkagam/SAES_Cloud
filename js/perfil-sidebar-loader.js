document.addEventListener("DOMContentLoaded", function() {
    const navContainer = document.querySelector('.nav-container');
    if (!navContainer) return;

    // 1. Definir menú de PERFIL ACADÉMICO (COORDINACIÓN)
    const menuItems = [
        {
            title: "COORDINACIÓN",
            links: [
                { text: "Panel General", href: "dashboard-perfil.html", icon: "📊" },
                { text: "Asignación Docente", href: "perfil-asignacion.html", icon: "👨‍🏫" },
                { text: "Asignación ETS", href: "perfil-ets.html", icon: "📝" },
                { text: "Jurados Titulación", href: "perfil-titulacion.html", icon: "🎓" }
            ]
        },
        {
            title: "PLANEACIÓN",
            links: [
                { text: "Carga Académica", href: "perfil-carga.html", icon: "📑" },
                { text: "Disponibilidad de Aulas", href: "perfil-aulas.html", icon: "🏢" },
                { text: "Reportes por Academia", href: "perfil-reportes.html", icon: "📈" }
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

    // 3. Estilos específicos para PERFIL (Púrpura)
    const style = document.createElement('style');
    style.textContent = `
        :root { --perfil-primary: #bf00ff; } /* Electric Purple */
        .nav-item:hover { background-color: rgba(191, 0, 255, 0.15) !important; color: var(--perfil-primary) !important; }
        .nav-item.active { background-color: rgba(191, 0, 255, 0.15) !important; color: var(--perfil-primary) !important; border-left: 3px solid var(--perfil-primary) !important; font-weight: 600; }
        .section-title { color: var(--perfil-primary) !important; opacity: 0.7; }
        .user-dot { background: var(--perfil-primary) !important; box-shadow: 0 0 10px var(--perfil-primary) !important; }
        .ipn-logo { text-shadow: 0 0 15px rgba(191, 0, 255, 0.5) !important; }
        .system-v { color: var(--perfil-primary) !important; }
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