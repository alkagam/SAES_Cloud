document.addEventListener("DOMContentLoaded", function() {
    const navContainer = document.querySelector('.nav-container');
    if (!navContainer) return;

    // 1. Definir menú de ROOT
    const menuItems = [
        {
            title: "SYSTEM CORE",
            links: [
                { text: "System Monitor", href: "dashboard-root.html", icon: "🖥️" },
                { text: "Terminal Console", href: "root-terminal.html", icon: ">_" }
            ]
        },
        {
            title: "SECURITY",
            links: [
                { text: "Access Logs", href: "root-logs.html", icon: "🔒" },
                { text: "Firewall Rules", href: "root-firewall.html", icon: "🛡️" }
            ]
        },
        {
            title: "DATABASE",
            links: [
                { text: "Backups", href: "root-backups.html", icon: "💾" },
                { text: "Query Analyzer", href: "root-query.html", icon: "🔍" }
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

    // 3. Estilos específicos para ROOT (Terminal Green)
    const style = document.createElement('style');
    style.textContent = `
        :root { --root-green: #00ff00; }
        .nav-item:hover { background-color: rgba(0, 255, 0, 0.15) !important; color: var(--root-green) !important; }
        .nav-item.active { background-color: rgba(0, 255, 0, 0.15) !important; color: var(--root-green) !important; border-left: 3px solid var(--root-green) !important; font-weight: 600; }
        .section-title { color: var(--root-green) !important; opacity: 0.7; }
        .user-dot { background: var(--root-green) !important; box-shadow: 0 0 10px var(--root-green) !important; }
        .ipn-logo { text-shadow: 0 0 15px rgba(0, 255, 0, 0.5) !important; color: var(--root-green) !important; }
        .system-v { color: var(--root-green) !important; }
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