// Helper functions for DAE-specific API calls

/**
 * Generic fetch helper attaching auth header when token present
 * @param {string|URL} input
 * @param {RequestInit} init
 */
async function apiFetch(input, init={}) {
    const headers = init.headers || {};
    const token = sessionStorage.getItem('accessToken');
    if (token) headers.Authorization = `Bearer ${token}`;
    init.headers = headers;
    const resp = await fetch(input, init);
    if (!resp.ok) throw new Error(`API fetch failed ${resp.status}`);
    return resp.json();
}

/**
 * Retrieve groups with optional filters
 * @param {{id_periodo?:string,id_materia?:string,turno?:string,page?:number,limit?:number}} opts
 */
async function fetchGroups(opts={}) {
    const url = new URL(`${API_BASE_URL}/saes-cloud-api/api/groups`);
    Object.entries(opts).forEach(([k,v]) => {
        if (v != null) url.searchParams.set(k, v);
    });
    return apiFetch(url.toString());
}

/**
 * Retrieve available groups for enrollment (requires period & student)
 * @param {{period:string,student:number}} opts
 */
async function fetchAvailableGroups({period, student}) {
    const url = new URL(`${API_BASE_URL}/saes-cloud-api/api/enrollments/groups/available`);
    if (period) url.searchParams.set('period', period);
    if (student) url.searchParams.set('student', student);
    return apiFetch(url.toString());
}

/**
 * Fetch grades for a given group id
 * @param {string} groupId
 */
async function fetchGrades(groupId) {
    const url = `${API_BASE_URL}/saes-cloud-api/api/grades/grades/groups/${groupId}`;
    return apiFetch(url);
}

/**
 * Placeholder for fetching pending requests; backend endpoint unknown
 */
async function fetchPendingRequests() {
    // If there were a /requests endpoint this would call it.
    try {
        return await apiFetch(`${API_BASE_URL}/saes-cloud-api/api/requests`);
    } catch(e) {
        console.warn('pending requests endpoint missing', e);
        return [];
    }
}

/**
 * Save DAE configuration (stub)
 * @param {Object} config
 * @returns {Promise<any>}
 */
async function saveSettings(config) {
    // backend may expose /config or similar
    return apiFetch(`${API_BASE_URL}/saes-cloud-api/api/config`, {method:'PUT', body: JSON.stringify(config), headers:{'Content-Type':'application/json'}});
}
