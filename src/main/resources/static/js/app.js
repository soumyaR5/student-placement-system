/* Placement Management UI. All data comes from the Spring Boot API (see api.js). */
(function () {
  'use strict';

  const VIEWS = ['dashboard', 'students', 'eligibility', 'criteria'];
  const state = { students: [], criteria: [] };
  const editing = { studentId: null, criteriaId: null };
  let pendingEligibilityStudent = null;
  let dashboardRun = 0;
  let checking = false;

  /* ---------- small helpers ---------- */
  const $ = (sel, root) => (root || document).querySelector(sel);
  const $$ = (sel, root) => Array.from((root || document).querySelectorAll(sel));
  const esc = (v) => String(v == null ? '' : v).replace(/[&<>"']/g, (c) =>
    ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
  const fmt2 = (n) => Number(n).toFixed(2);

  function toast(message, type) {
    const t = document.createElement('div');
    t.className = 'toast ' + (type || 'success');
    t.setAttribute('role', type === 'error' ? 'alert' : 'status');
    const text = document.createElement('span');
    text.textContent = message;
    const close = document.createElement('button');
    close.type = 'button';
    close.textContent = '\u00d7';
    close.setAttribute('aria-label', 'Dismiss');
    close.addEventListener('click', () => t.remove());
    t.append(text, close);
    $('#toasts').appendChild(t);
    setTimeout(() => t.remove(), type === 'error' ? 8000 : 4000);
  }

  function showAlert(el, message, retry) {
    el.hidden = false;
    el.className = 'alert error';
    el.innerHTML = '<span>' + esc(message) + '</span>' + (retry ? '<button class="btn small" type="button">Retry</button>' : '');
    if (retry) $('button', el).addEventListener('click', retry);
  }

  function hideAlert(el) {
    el.hidden = true;
    el.innerHTML = '';
  }

  function stateRow(cols, message) {
    return '<tr><td class="state" colspan="' + cols + '">' + esc(message) + '</td></tr>';
  }

  function placementBadge(s) {
    return s.placementStatus === 'PLACED'
      ? '<span class="badge ok">Placed' + (s.companyPlaced ? ' \u00b7 ' + esc(s.companyPlaced) : '') + '</span>'
      : '<span class="badge neutral">Not placed</span>';
  }

  function confirmAction(title, message, okLabel) {
    return new Promise((resolve) => {
      const dlg = $('#confirm-dialog');
      $('#confirm-title').textContent = title;
      $('#confirm-message').textContent = message;
      $('#confirm-ok').textContent = okLabel;
      dlg.returnValue = '';
      dlg.addEventListener('close', () => resolve(dlg.returnValue === 'confirm'), { once: true });
      dlg.showModal();
    });
  }

  /* ---------- form helpers ---------- */
  function clearFormErrors(form) {
    $$('[data-error-for]', form).forEach((e) => { e.textContent = ''; });
    $$('.invalid', form).forEach((e) => e.classList.remove('invalid'));
    const general = $('.form-error', form);
    general.hidden = true;
    general.textContent = '';
  }

  function applyErrors(form, fieldErrors, message) {
    const general = [];
    const entries = Object.entries(fieldErrors || {});
    entries.forEach(([name, text]) => {
      const slot = $('[data-error-for="' + name + '"]', form);
      if (slot) {
        slot.textContent = text;
        if (form.elements[name]) form.elements[name].classList.add('invalid');
      } else {
        general.push(text);
      }
    });
    if (!entries.length && message) general.push(message);
    if (general.length) {
      const box = $('.form-error', form);
      box.hidden = false;
      box.textContent = general.join(' ');
    }
    const firstInvalid = $('.invalid', form);
    if (firstInvalid) firstInvalid.focus();
  }

  /* Shared submit logic: validate, block double submit, call API, surface errors. */
  function wireForm(opts) {
    let submitting = false;
    opts.form.addEventListener('submit', async (event) => {
      event.preventDefault();
      if (submitting) return;
      clearFormErrors(opts.form);
      const built = opts.build(opts.form);
      if (Object.keys(built.errors).length) {
        applyErrors(opts.form, built.errors);
        return;
      }
      const button = $('button[type="submit"]', opts.form);
      const label = button.textContent;
      submitting = true;
      button.disabled = true;
      button.textContent = 'Saving\u2026';
      try {
        const saved = await opts.save(built.payload);
        opts.dialog.close();
        await opts.onSaved(saved);
      } catch (err) {
        applyErrors(opts.form, err.fieldErrors, err.message || 'Something went wrong.');
      } finally {
        submitting = false;
        button.disabled = false;
        button.textContent = label;
      }
    });
    opts.dialog.addEventListener('cancel', (e) => { if (submitting) e.preventDefault(); });
  }

  /* ---------- routing ---------- */
  function route() {
    const name = location.hash.slice(1);
    const view = VIEWS.includes(name) ? name : 'dashboard';
    VIEWS.forEach((v) => { $('#view-' + v).hidden = v !== view; });
    $$('#nav a').forEach((a) => a.classList.toggle('active', a.dataset.view === view));
    ({ dashboard: showDashboard, students: showStudents, eligibility: showEligibility, criteria: showCriteria })[view]();
  }

  function goTo(view) {
    if (location.hash === '#' + view) route(); else location.hash = view;
  }

  /* ---------- dashboard ---------- */
  async function showDashboard() {
    const run = ++dashboardRun;
    const alertBox = $('#dashboard-alert');
    hideAlert(alertBox);
    ['total', 'eligible', 'ineligible', 'placed'].forEach((k) => { $('#stat-' + k).textContent = '\u2026'; });
    $('#stat-eligible-hint').textContent = 'Eligible for at least one company';
    $('#dashboard-recent').innerHTML = stateRow(6, 'Loading\u2026');

    try {
      const [students, criteria] = await Promise.all([Api.listStudents(), Api.listCriteria()]);
      if (run !== dashboardRun) return;
      state.students = students;
      state.criteria = criteria;
    } catch (err) {
      if (run !== dashboardRun) return;
      ['total', 'eligible', 'ineligible', 'placed'].forEach((k) => { $('#stat-' + k).textContent = '\u2014'; });
      $('#dashboard-recent').innerHTML = stateRow(6, 'Could not load data.');
      showAlert(alertBox, 'Could not load dashboard data: ' + err.message, showDashboard);
      return;
    }

    const students = state.students;
    $('#stat-total').textContent = students.length;
    $('#stat-placed').textContent = students.filter((s) => s.placementStatus === 'PLACED').length;

    const recent = students.slice().sort((a, b) => b.id - a.id).slice(0, 5);
    $('#dashboard-recent').innerHTML = recent.length ? recent.map((s) =>
      '<tr><td>' + esc(s.rollNumber) + '</td><td>' + esc(s.fullName) + '</td>' +
      '<td class="num">' + fmt2(s.cgpa) + '</td><td class="num">' + s.activeBacklogs + '</td>' +
      '<td>' + placementBadge(s) + '</td><td class="elig-cell" data-id="' + s.id + '">\u2026</td></tr>').join('')
      : stateRow(6, 'No students registered yet.');

    if (!state.criteria.length) {
      $('#stat-eligible').textContent = '\u2014';
      $('#stat-ineligible').textContent = '\u2014';
      $('#stat-eligible-hint').textContent = 'No placement criteria configured';
      $$('.elig-cell').forEach((c) => { c.textContent = '\u2014'; });
      return;
    }

    // Eligibility is always computed by the backend, one call per student.
    const results = await Promise.allSettled(students.map((s) => Api.eligibilityForStudent(s.id)));
    if (run !== dashboardRun) return;
    let eligible = 0;
    let ineligible = 0;
    let failed = 0;
    results.forEach((r, i) => {
      const cell = $('.elig-cell[data-id="' + students[i].id + '"]');
      if (r.status === 'fulfilled') {
        const ok = r.value.filter((x) => x.eligible).length;
        if (ok > 0) eligible++; else ineligible++;
        if (cell) cell.innerHTML = '<span class="badge ' + (ok > 0 ? 'ok' : 'no') + '">' + ok + ' of ' + r.value.length + ' companies</span>';
      } else {
        failed++;
        if (cell) cell.textContent = 'Unavailable';
      }
    });
    $('#stat-eligible').textContent = eligible;
    $('#stat-ineligible').textContent = ineligible;
    if (failed) {
      showAlert(alertBox, 'Eligibility could not be loaded for ' + failed + ' student(s); totals exclude them.', showDashboard);
    }
  }

  /* ---------- students ---------- */
  async function showStudents() {
    const alertBox = $('#students-alert');
    hideAlert(alertBox);
    $('#students-body').innerHTML = stateRow(7, 'Loading\u2026');
    try {
      state.students = await Api.listStudents();
    } catch (err) {
      $('#students-body').innerHTML = stateRow(7, 'Could not load students.');
      $('#students-count').textContent = '';
      showAlert(alertBox, 'Could not load students: ' + err.message, showStudents);
      return;
    }
    renderStudents();
  }

  function renderStudents() {
    const q = $('#student-search').value.trim().toLowerCase();
    const rows = state.students.filter((s) =>
      !q || s.fullName.toLowerCase().includes(q) || s.rollNumber.toLowerCase().includes(q));
    $('#students-count').textContent = state.students.length
      ? rows.length + ' of ' + state.students.length + ' students' : '';
    if (!rows.length) {
      $('#students-body').innerHTML = stateRow(7, state.students.length
        ? 'No students match your search.' : 'No students registered yet. Use "Register student" to add one.');
      return;
    }
    $('#students-body').innerHTML = rows.map((s) =>
      '<tr><td>' + esc(s.rollNumber) + '</td>' +
      '<td>' + esc(s.fullName) + '<div class="sub">' + esc(s.email) + '</div></td>' +
      '<td>' + esc(s.department) + '</td>' +
      '<td class="num">' + fmt2(s.cgpa) + '</td><td class="num">' + s.activeBacklogs + '</td>' +
      '<td>' + placementBadge(s) + '</td>' +
      '<td class="actions">' +
      '<button class="btn small" type="button" data-action="eligibility" data-id="' + s.id + '">Eligibility</button>' +
      '<button class="btn small" type="button" data-action="edit" data-id="' + s.id + '">Edit</button>' +
      '<button class="btn small danger-outline" type="button" data-action="delete" data-id="' + s.id + '">Delete</button>' +
      '</td></tr>').join('');
  }

  const studentForm = $('#student-form');
  const studentDialog = $('#student-dialog');

  function togglePlacedFields() {
    const placed = studentForm.elements.placementStatus.value === 'PLACED';
    $$('.placed-only', studentForm).forEach((el) => { el.hidden = !placed; });
  }

  function openStudentForm(student) {
    studentForm.reset();
    clearFormErrors(studentForm);
    editing.studentId = student ? student.id : null;
    $('#student-dialog-title').textContent = student ? 'Edit student' : 'Register student';
    if (student) {
      const f = studentForm.elements;
      f.rollNumber.value = student.rollNumber;
      f.fullName.value = student.fullName;
      f.email.value = student.email;
      f.department.value = student.department;
      f.cgpa.value = student.cgpa;
      f.activeBacklogs.value = student.activeBacklogs;
      f.placementStatus.value = student.placementStatus || 'NOT_PLACED';
      f.companyPlaced.value = student.companyPlaced || '';
      f.packageLpa.value = student.packageLpa == null ? '' : student.packageLpa;
    }
    togglePlacedFields();
    studentDialog.showModal();
  }

  function buildStudent(form) {
    const f = form.elements;
    const errors = {};
    const text = (name, label, max) => {
      const v = f[name].value.trim();
      if (!v) errors[name] = label + ' is required';
      else if (v.length > max) errors[name] = label + ' must be at most ' + max + ' characters';
      return v;
    };
    const rollNumber = text('rollNumber', 'Roll number', 30);
    const fullName = text('fullName', 'Full name', 100);
    const email = text('email', 'Email', 150);
    if (email && !errors.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) errors.email = 'Enter a valid email address';
    const department = text('department', 'Department', 100);

    const cgpaRaw = f.cgpa.value.trim();
    if (!cgpaRaw) errors.cgpa = 'CGPA is required';
    else if (!/^\d{1,2}(\.\d{1,2})?$/.test(cgpaRaw) || Number(cgpaRaw) > 10) errors.cgpa = 'CGPA must be between 0.00 and 10.00 (max 2 decimals)';

    const backRaw = f.activeBacklogs.value.trim();
    if (!backRaw) errors.activeBacklogs = 'Active backlogs is required';
    else if (!/^\d+$/.test(backRaw) || Number(backRaw) > 50) errors.activeBacklogs = 'Active backlogs must be a whole number from 0 to 50';

    const status = f.placementStatus.value;
    let companyPlaced = null;
    let packageLpa = null;
    if (status === 'PLACED') {
      companyPlaced = f.companyPlaced.value.trim();
      if (!companyPlaced) errors.companyPlaced = 'Company is required when the student is placed';
      else if (companyPlaced.length > 100) errors.companyPlaced = 'Company must be at most 100 characters';
      const pkg = f.packageLpa.value.trim();
      if (pkg) {
        if (!/^\d{1,6}(\.\d{1,2})?$/.test(pkg)) errors.packageLpa = 'Package must be a positive number (max 2 decimals)';
        else packageLpa = Number(pkg);
      }
    }
    return {
      errors,
      payload: {
        rollNumber, fullName, email, department,
        cgpa: Number(cgpaRaw), activeBacklogs: Number(backRaw),
        placementStatus: status, companyPlaced, packageLpa
      }
    };
  }

  const busyIds = new Set();

  async function deleteStudent(student) {
    if (busyIds.has('s' + student.id)) return;
    const ok = await confirmAction('Delete student',
      'Delete ' + student.fullName + ' (' + student.rollNumber + ')? This cannot be undone.', 'Delete');
    if (!ok) return;
    busyIds.add('s' + student.id);
    try {
      await Api.deleteStudent(student.id);
      toast('Deleted ' + student.fullName + '.');
    } catch (err) {
      toast('Could not delete student: ' + err.message, 'error');
    } finally {
      busyIds.delete('s' + student.id);
    }
    showStudents();
  }

  /* ---------- eligibility ---------- */
  async function showEligibility() {
    const alertBox = $('#eligibility-alert');
    hideAlert(alertBox);
    $('#elig-summary').innerHTML = '';
    $('#elig-results').innerHTML = '';
    try {
      const [students, criteria] = await Promise.all([Api.listStudents(), Api.listCriteria()]);
      state.students = students;
      state.criteria = criteria;
    } catch (err) {
      showAlert(alertBox, 'Could not load students and criteria: ' + err.message, showEligibility);
      return;
    }
    const studentSelect = $('#elig-student');
    const previous = pendingEligibilityStudent != null ? String(pendingEligibilityStudent) : studentSelect.value;
    studentSelect.innerHTML = '<option value="">Select a student\u2026</option>' + state.students.map((s) =>
      '<option value="' + s.id + '">' + esc(s.rollNumber) + ' \u2013 ' + esc(s.fullName) + '</option>').join('');
    if (state.students.some((s) => String(s.id) === previous)) studentSelect.value = previous;

    $('#elig-company').innerHTML = '<option value="">All companies</option>' + state.criteria.map((c) =>
      '<option value="' + c.id + '">' + esc(c.companyName) + '</option>').join('');

    if (!state.students.length) {
      $('#elig-results').innerHTML = '<div class="alert info">No students registered yet.</div>';
    } else if (!state.criteria.length) {
      $('#elig-results').innerHTML = '<div class="alert info">No placement criteria configured. Add criteria first.</div>';
    }

    if (pendingEligibilityStudent != null) {
      pendingEligibilityStudent = null;
      runEligibilityCheck();
    }
  }

  async function runEligibilityCheck() {
    if (checking) return;
    const studentId = $('#elig-student').value;
    const criteriaId = $('#elig-company').value;
    const results = $('#elig-results');
    if (!studentId) {
      results.innerHTML = '<div class="alert info">Select a student to check.</div>';
      return;
    }
    checking = true;
    const button = $('#btn-check');
    button.disabled = true;
    results.innerHTML = '<p class="loading">Checking eligibility\u2026</p>';
    try {
      const list = criteriaId
        ? [await Api.eligibility(studentId, criteriaId)]
        : await Api.eligibilityForStudent(studentId);
      renderEligibility(studentId, list);
    } catch (err) {
      $('#elig-summary').innerHTML = '';
      results.innerHTML = '';
      showAlert($('#eligibility-alert'), 'Eligibility check failed: ' + err.message);
    } finally {
      checking = false;
      button.disabled = false;
    }
  }

  function renderEligibility(studentId, list) {
    hideAlert($('#eligibility-alert'));
    const student = state.students.find((s) => String(s.id) === String(studentId));
    $('#elig-summary').innerHTML = student
      ? '<div class="card"><strong>' + esc(student.fullName) + '</strong> (' + esc(student.rollNumber) + ') \u00b7 ' +
        'CGPA ' + fmt2(student.cgpa) + ' \u00b7 ' + student.activeBacklogs + ' active backlog(s)</div>'
      : '';
    if (!list.length) {
      $('#elig-results').innerHTML = '<div class="alert info">No placement criteria configured. Add criteria first.</div>';
      return;
    }
    $('#elig-results').innerHTML = list.map((r) => {
      const c = state.criteria.find((x) => x.id === r.criteriaId);
      const requirement = c
        ? 'Requires CGPA \u2265 ' + fmt2(c.minCgpa) + ' and at most ' + c.maxActiveBacklogs + ' active backlog(s)'
        : '';
      return '<div class="result ' + (r.eligible ? 'eligible' : 'ineligible') + '">' +
        '<div class="result-head"><h3>' + esc(r.companyName) + '</h3>' +
        '<span class="badge ' + (r.eligible ? 'ok' : 'no') + '">' + (r.eligible ? 'Eligible' : 'Not eligible') + '</span></div>' +
        (requirement ? '<div class="requirement">' + esc(requirement) + '</div>' : '') +
        '<ul>' + (r.reasons || []).map((x) => '<li>' + esc(x) + '</li>').join('') + '</ul></div>';
    }).join('');
  }

  /* ---------- criteria ---------- */
  async function showCriteria() {
    const alertBox = $('#criteria-alert');
    hideAlert(alertBox);
    $('#criteria-body').innerHTML = stateRow(4, 'Loading\u2026');
    try {
      state.criteria = await Api.listCriteria();
    } catch (err) {
      $('#criteria-body').innerHTML = stateRow(4, 'Could not load criteria.');
      showAlert(alertBox, 'Could not load criteria: ' + err.message, showCriteria);
      return;
    }
    $('#criteria-body').innerHTML = state.criteria.length ? state.criteria.map((c) =>
      '<tr><td>' + esc(c.companyName) + '</td><td class="num">' + fmt2(c.minCgpa) + '</td>' +
      '<td class="num">' + c.maxActiveBacklogs + '</td>' +
      '<td class="actions">' +
      '<button class="btn small" type="button" data-action="edit" data-id="' + c.id + '">Edit</button>' +
      '<button class="btn small danger-outline" type="button" data-action="delete" data-id="' + c.id + '">Delete</button>' +
      '</td></tr>').join('')
      : stateRow(4, 'No placement criteria yet. Use "Add criteria" to create one.');
  }

  const criteriaForm = $('#criteria-form');
  const criteriaDialog = $('#criteria-dialog');

  function openCriteriaForm(criteria) {
    criteriaForm.reset();
    clearFormErrors(criteriaForm);
    editing.criteriaId = criteria ? criteria.id : null;
    $('#criteria-dialog-title').textContent = criteria ? 'Edit criteria' : 'Add criteria';
    if (criteria) {
      criteriaForm.elements.companyName.value = criteria.companyName;
      criteriaForm.elements.minCgpa.value = criteria.minCgpa;
      criteriaForm.elements.maxActiveBacklogs.value = criteria.maxActiveBacklogs;
    }
    criteriaDialog.showModal();
  }

  function buildCriteria(form) {
    const f = form.elements;
    const errors = {};
    const companyName = f.companyName.value.trim();
    if (!companyName) errors.companyName = 'Company name is required';
    else if (companyName.length > 100) errors.companyName = 'Company name must be at most 100 characters';
    const cgpaRaw = f.minCgpa.value.trim();
    if (!cgpaRaw) errors.minCgpa = 'Minimum CGPA is required';
    else if (!/^\d{1,2}(\.\d{1,2})?$/.test(cgpaRaw) || Number(cgpaRaw) > 10) errors.minCgpa = 'Minimum CGPA must be between 0.00 and 10.00 (max 2 decimals)';
    const backRaw = f.maxActiveBacklogs.value.trim();
    if (!backRaw) errors.maxActiveBacklogs = 'Maximum active backlogs is required';
    else if (!/^\d+$/.test(backRaw) || Number(backRaw) > 50) errors.maxActiveBacklogs = 'Maximum active backlogs must be a whole number from 0 to 50';
    return { errors, payload: { companyName, minCgpa: Number(cgpaRaw), maxActiveBacklogs: Number(backRaw) } };
  }

  async function deleteCriteria(criteria) {
    if (busyIds.has('c' + criteria.id)) return;
    const ok = await confirmAction('Delete criteria',
      'Delete the criteria for ' + criteria.companyName + '? Eligibility for this company will no longer be reported.', 'Delete');
    if (!ok) return;
    busyIds.add('c' + criteria.id);
    try {
      await Api.deleteCriteria(criteria.id);
      toast('Deleted criteria for ' + criteria.companyName + '.');
    } catch (err) {
      toast('Could not delete criteria: ' + err.message, 'error');
    } finally {
      busyIds.delete('c' + criteria.id);
    }
    showCriteria();
  }

  /* ---------- wiring ---------- */
  function init() {
    $('#btn-refresh').addEventListener('click', showDashboard);
    $('#btn-add-student').addEventListener('click', () => openStudentForm(null));
    $('#btn-add-criteria').addEventListener('click', () => openCriteriaForm(null));
    $('#student-search').addEventListener('input', renderStudents);
    studentForm.elements.placementStatus.addEventListener('change', togglePlacedFields);
    $('#eligibility-form').addEventListener('submit', (e) => { e.preventDefault(); runEligibilityCheck(); });

    $$('dialog [data-close]').forEach((b) => b.addEventListener('click', () => b.closest('dialog').close()));

    $('#students-body').addEventListener('click', (e) => {
      const btn = e.target.closest('button[data-action]');
      if (!btn) return;
      const student = state.students.find((s) => String(s.id) === btn.dataset.id);
      if (!student) return;
      if (btn.dataset.action === 'edit') openStudentForm(student);
      else if (btn.dataset.action === 'delete') deleteStudent(student);
      else if (btn.dataset.action === 'eligibility') { pendingEligibilityStudent = student.id; goTo('eligibility'); }
    });

    $('#criteria-body').addEventListener('click', (e) => {
      const btn = e.target.closest('button[data-action]');
      if (!btn) return;
      const criteria = state.criteria.find((c) => String(c.id) === btn.dataset.id);
      if (!criteria) return;
      if (btn.dataset.action === 'edit') openCriteriaForm(criteria);
      else if (btn.dataset.action === 'delete') deleteCriteria(criteria);
    });

    wireForm({
      form: studentForm, dialog: studentDialog, build: buildStudent,
      save: (payload) => (editing.studentId == null
        ? Api.createStudent(payload) : Api.updateStudent(editing.studentId, payload)),
      onSaved: async (saved) => {
        toast((editing.studentId == null ? 'Registered ' : 'Updated ') + saved.fullName + '.');
        await showStudents();
      }
    });

    wireForm({
      form: criteriaForm, dialog: criteriaDialog, build: buildCriteria,
      save: (payload) => (editing.criteriaId == null
        ? Api.createCriteria(payload) : Api.updateCriteria(editing.criteriaId, payload)),
      onSaved: async (saved) => {
        toast('Saved criteria for ' + saved.companyName + '.');
        await showCriteria();
      }
    });

    window.addEventListener('hashchange', route);
    route();
  }

  init();
})();
