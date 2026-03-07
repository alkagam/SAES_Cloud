document.addEventListener("DOMContentLoaded", function() {
    const navContainer = document.querySelector('.nav-container');
    if (!navContainer) return;

    // 1. Definir la estructura del menú DOCENTE
    const menuItems = [
        {
            title: "DOCENTE - PRINCIPAL",
            links: [
                { text: "Panel de Control", href: "dashboard-docente.html", icon: "📊" }
            ]
        },
        {
            title: "ACADÉMICO",
            links: [
                { text: "Mis Grupos", href: "docente-grupos.html", icon: "📚" },
                { text: "Lista de Alumnos", href: "docente-alumnos.html", icon: "👥" },
                { text: "Calificaciones", href: "docente-calificaciones.html", icon: "📝" },
                { text: "Asistencias", href: "docente-asistencias.html", icon: "✅" }
            ]
        },
        {
            title: "ADMINISTRATIVO",
            links: [
                { text: "Horario", href: "docente-horario.html", icon: "📅" },
                { text: "Perfil", href: "docente-perfil.html", icon: "👤" }
            ]
        }
    ];

    // 2. Generar el HTML
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

    // 3. Persistencia de Scroll
    const savedScroll = sessionStorage.getItem('docSidebarScroll');
    if (savedScroll) navContainer.scrollTop = savedScroll;

    navContainer.addEventListener('click', (e) => {
        if (e.target.closest('a')) sessionStorage.setItem('docSidebarScroll', navContainer.scrollTop);
    });

    // 4. Menú Hamburguesa
    const menuToggle = document.querySelector('.menu-toggle');
    if (menuToggle) {
        menuToggle.addEventListener('click', () => {
            menuToggle.innerHTML = menuToggle.innerHTML.includes('☰') ? '✕' : '☰';
        });
    }

    // 5. Inyectar estilos VERDES (TEMA ALUMNO) para DOCENTE
    const style = document.createElement('style');
    style.textContent = `
        :root {
            --doc-cyan: #00ff88; /* Verde Neón */
            --doc-cyan-dim: rgba(0, 255, 136, 0.15);
        }
        
        /* Sobrescribir estilos de nav-item para el tema verde */
        .nav-item:hover {
            background-color: var(--doc-cyan-dim) !important;
            color: var(--doc-cyan) !important;
        }

        .nav-item.active {
            background-color: var(--doc-cyan-dim) !important;
            color: var(--doc-cyan) !important;
            border-left: 3px solid var(--doc-cyan) !important;
            font-weight: 600;
        }
    `;
    document.head.appendChild(style);
});