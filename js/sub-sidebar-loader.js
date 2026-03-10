document.addEventListener("DOMContentLoaded", function() {
    const navContainer = document.querySelector('.nav-container');
    if (!navContainer) return;

    // 1. Definir menú de SUBDIRECCIÓN
    const menuItems = [
        {
            title: "BIENESTAR ESTUDIANTIL",
            links: [
                { text: "Panel General", href: "dashboard-sub.html", icon: "📊" },
                { text: "Servicio Médico", href: "sub-medico.html", icon: "⚕️" },
                { text: "Becas y Apoyos", href: "sub-becas.html", icon: "💰" },
                { text: "Reportes", href: "sub-reportes.html", icon: "📈" }
            ]
        },
        {
            title: "EXTENSIÓN",
            links: [
                { text: "Actividades Deportivas", href: "sub-deportes.html", icon: "⚽" },
                { text: "Actividades Culturales", href: "sub-culturales.html", icon: "🎭" }
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

    // 3. Estilos específicos para SUBDIRECCIÓN (Ámbar/Dorado)
    const style = document.createElement('style');
    style.textContent = `
        :root { --sub-orange: #ffaa00; }
        .nav-item:hover { background-color: rgba(255, 170, 0, 0.15) !important; color: var(--sub-orange) !important; }
        .nav-item.active { background-color: rgba(255, 170, 0, 0.15) !important; color: var(--sub-orange) !important; border-left: 3px solid var(--sub-orange) !important; font-weight: 600; }
        .section-title { color: var(--sub-orange) !important; opacity: 0.7; }
        .user-dot { background: var(--sub-orange) !important; box-shadow: 0 0 10px var(--sub-orange) !important; }
        .ipn-logo { text-shadow: 0 0 15px rgba(255, 170, 0, 0.5) !important; }
        .system-v { color: var(--sub-orange) !important; }
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