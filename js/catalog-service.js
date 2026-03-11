// Utilities for fetching catalog data (careers, subjects) from the API

/**
 * Perform a GET request to the catalog careers endpoint.
 * @param {boolean} [activeOnly] - if true, append ?activa=true to the URL
 * @returns {Promise<Array<{id_carrera:number,nombre:string,clave:string,duracion_semestres:number}>>}
 */
async function fetchCareers(activeOnly) {
    let url = `${API_BASE_URL}/saes-cloud-api/api/careers`;
    if (activeOnly === true) {
        url += '?activa=true';
    } else if (activeOnly === false) {
        url += '?activa=false';
    }
    const headers = {};
    const token = sessionStorage.getItem('accessToken');
    if (token) headers.Authorization = `Bearer ${token}`;
    const resp = await fetch(url, { headers });
    if (!resp.ok) throw new Error(`failed to load careers: ${resp.status}`);
    return resp.json();
}

let _careerCache = null;

/**
 * Ensure career map is loaded and return it.
 * @returns {Promise<Object<number,string>>}
 */
async function careerMap() {
    if (_careerCache) return _careerCache;
    const careers = await fetchCareers(false);
    _careerCache = {};
    careers.forEach(c => { _careerCache[c.id_carrera] = c.nombre; });
    return _careerCache;
}

/**
 * Get career name by id (loads cache if needed)
 * @param {number|string} id
 * @returns {Promise<string>}
 */
async function getCareerName(id) {
    const map = await careerMap();
    return map[id] || String(id);
}

/**
 * Fill a <select> element with career options.
 * @param {HTMLSelectElement} selectEl
 * @param {boolean} [activeOnly=true]
 */
async function populateCareers(selectEl, activeOnly = true) {
    if (!selectEl) return;
    const careers = await fetchCareers(activeOnly);
    selectEl.innerHTML = '<option value="">-- selecciona carrera --</option>';
    careers.forEach(c => {
        const opt = document.createElement('option');
        opt.value = c.id_carrera;
        opt.textContent = c.nombre;
        selectEl.appendChild(opt);
    });
}

/**
 * Fetch subjects with optional filters (page/limit/clave) and return result {data,total,page,limit}
 */
async function fetchSubjects({ page=1, limit=50, clave } = {}) {
    let url = new URL(`${API_BASE_URL}/saes-cloud-api/api/subjects`);
    url.searchParams.set('page', page);
    url.searchParams.set('limit', limit);
    if (clave) url.searchParams.set('clave', clave);
    const headers = {};
    const token = sessionStorage.getItem('accessToken');
    if (token) headers.Authorization = `Bearer ${token}`;
    const resp = await fetch(url.toString(), { headers });
    if (!resp.ok) throw new Error(`failed to load subjects: ${resp.status}`);
    return resp.json();
}