/* Thin wrapper around fetch for the Spring Boot REST API (same origin, so no CORS needed). */
(function (global) {
  'use strict';

  class ApiError extends Error {
    constructor(status, message, fieldErrors) {
      super(message);
      this.status = status;
      this.fieldErrors = fieldErrors || {};
    }
  }

  async function request(method, url, body) {
    const options = { method, headers: { Accept: 'application/json' } };
    if (body !== undefined) {
      options.headers['Content-Type'] = 'application/json';
      options.body = JSON.stringify(body);
    }

    let response;
    try {
      response = await fetch(url, options);
    } catch (networkError) {
      throw new ApiError(0, 'Cannot reach the server. Check that the application is running.');
    }

    // Read the body defensively: it may be empty (204) or not JSON.
    const text = await response.text();
    let data = null;
    if (text) {
      try { data = JSON.parse(text); } catch (parseError) { data = null; }
    }

    if (!response.ok) {
      const message = (data && data.message) || response.statusText || ('Request failed (' + response.status + ')');
      throw new ApiError(response.status, message, data && data.fieldErrors);
    }
    return data;
  }

  const list = async (url) => {
    const data = await request('GET', url);
    if (!Array.isArray(data)) throw new ApiError(200, 'Unexpected response format from server.');
    return data;
  };

  const one = async (method, url, body) => {
    const data = await request(method, url, body);
    if (!data || typeof data !== 'object') throw new ApiError(200, 'Unexpected response format from server.');
    return data;
  };

  global.Api = {
    ApiError,
    listStudents: () => list('/api/students'),
    createStudent: (p) => one('POST', '/api/students', p),
    updateStudent: (id, p) => one('PUT', '/api/students/' + id, p),
    deleteStudent: (id) => request('DELETE', '/api/students/' + id),

    listCriteria: () => list('/api/criteria'),
    createCriteria: (p) => one('POST', '/api/criteria', p),
    updateCriteria: (id, p) => one('PUT', '/api/criteria/' + id, p),
    deleteCriteria: (id) => request('DELETE', '/api/criteria/' + id),

    eligibilityForStudent: (studentId) => list('/api/eligibility/students/' + studentId),
    eligibility: (studentId, criteriaId) =>
      one('GET', '/api/eligibility/students/' + studentId + '/criteria/' + criteriaId)
  };
})(window);
