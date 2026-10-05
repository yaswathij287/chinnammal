/**
 * CHINNAMMAL — SMART MANAGEMENT PLATFORM
 * Administrator Dashboard & Management Logic
 */

document.addEventListener('DOMContentLoaded', async () => {
  // 1. Session check
  const currentUser = await App.Auth.checkSession('ADMIN');
  if (!currentUser) return;

  // 2. Setup navigation tabs
  App.setupTabs('overview', (tabId) => {
    loadTabData(tabId);
  });

  // 3. Quick action buttons
  document.getElementById('btnQuickAddStudent')?.addEventListener('click', () => openAddStudentModal());
  document.getElementById('btnQuickAddRoom')?.addEventListener('click', () => App.Modal.open('addRoomModal'));
  document.getElementById('btnQuickRecordVisitor')?.addEventListener('click', () => App.Modal.open('addVisitorModal'));
  document.getElementById('btnQuickViewReport')?.addEventListener('click', () => {
    document.querySelector('.sidebar-nav .nav-item[data-tab="reports"]')?.click();
  });

  // Modal open buttons
  document.getElementById('btnAddStudentModalOpen')?.addEventListener('click', () => openAddStudentModal());
  document.getElementById('btnAddWardenModalOpen')?.addEventListener('click', () => openAddWardenModal());
  document.getElementById('btnAddRoomModalOpen')?.addEventListener('click', () => App.Modal.open('addRoomModal'));
  document.getElementById('btnMarkAttendanceOpen')?.addEventListener('click', () => App.Modal.open('markAttendanceModal'));
  document.getElementById('btnAddVisitorModalOpen')?.addEventListener('click', () => App.Modal.open('addVisitorModal'));
  document.getElementById('btnLogSecurityModalOpen')?.addEventListener('click', () => App.Modal.open('logSecurityModal'));

  // 4. Initial cache & data load
  await loadCommonLookups();
  await loadOverviewStats();

  // Search filter
  document.getElementById('studentsSearchInput')?.addEventListener('input', (e) => {
    filterStudents(e.target.value.trim().toLowerCase());
  });

  document.getElementById('roomsBlockFilter')?.addEventListener('change', (e) => {
    loadRooms(e.target.value);
  });

  document.getElementById('feesStatusFilter')?.addEventListener('change', (e) => {
    loadFees(e.target.value);
  });

  document.getElementById('complaintsStatusFilter')?.addEventListener('change', (e) => {
    loadComplaints(e.target.value);
  });

  // Forms setup
  setupForms();
});

// Cache for client filtering
let allStudentsCache = [];
let allWardensCache = [];
let allBlocksCache = [];
let availableRoomsCache = [];

function loadTabData(tabId) {
  switch (tabId) {
    case 'overview': loadOverviewStats(); break;
    case 'students': loadStudents(); break;
    case 'wardens': loadWardens(); break;
    case 'rooms': loadRooms(); break;
    case 'fees': loadFees(); break;
    case 'complaints': loadComplaints(); break;
    case 'leaves': loadLeaves(); break;
    case 'attendance': loadAttendance(); break;
    case 'visitors': loadVisitors(); break;
    case 'security': loadSecurityLogs(); break;
    case 'reports': loadReports(); break;
  }
}

// ==========================================================
// 1. LOOKUPS
// ==========================================================
async function loadCommonLookups() {
  try {
    const blocksRes = await App.API.get('/api/common/blocks');
    if (blocksRes && blocksRes.data) {
      allBlocksCache = blocksRes.data;
      populateBlockDropdowns();
    }
  } catch (e) {
    console.error('Error loading blocks lookup:', e);
  }

  try {
    const roomsRes = await App.API.get('/api/common/available-rooms');
    if (roomsRes && roomsRes.data) {
      availableRoomsCache = roomsRes.data;
      populateRoomDropdown();
    }
  } catch (e) {
    console.error('Error loading available rooms:', e);
  }
}

function populateBlockDropdowns() {
  const roomsFilter = document.getElementById('roomsBlockFilter');
  const wardenBlock = document.getElementById('addWardenBlock');
  const editWardenBlock = document.getElementById('editWardenBlock');
  const addRoomBlock = document.getElementById('addRoomBlock');

  if (roomsFilter) {
    roomsFilter.innerHTML = '<option value="">All Blocks</option>';
    allBlocksCache.forEach(b => {
      roomsFilter.innerHTML += `<option value="${b.blockId}">${App.escapeHTML(b.blockName)}</option>`;
    });
  }

  if (wardenBlock) {
    wardenBlock.innerHTML = '<option value="">Unassigned</option>';
    allBlocksCache.forEach(b => {
      wardenBlock.innerHTML += `<option value="${b.blockId}">${App.escapeHTML(b.blockName)}</option>`;
    });
  }

  if (editWardenBlock) {
    editWardenBlock.innerHTML = '<option value="">Unassigned</option>';
    allBlocksCache.forEach(b => {
      editWardenBlock.innerHTML += `<option value="${b.blockId}">${App.escapeHTML(b.blockName)}</option>`;
    });
  }

  if (addRoomBlock) {
    addRoomBlock.innerHTML = '';
    allBlocksCache.forEach(b => {
      addRoomBlock.innerHTML += `<option value="${b.blockId}">${App.escapeHTML(b.blockName)}</option>`;
    });
  }
}

function populateRoomDropdown() {
  const roomSelect = document.getElementById('addStudentRoom');
  if (roomSelect) {
    roomSelect.innerHTML = '<option value="">Select available room...</option>';
    availableRoomsCache.forEach(r => {
      roomSelect.innerHTML += `<option value="${r.roomId}">${App.escapeHTML(r.roomNumber)} (${r.blockName || 'Block ' + r.blockId}) - ${r.availableBeds} beds left</option>`;
    });
  }
}

// ==========================================================
// 2. OVERVIEW METRICS
// ==========================================================
async function loadOverviewStats() {
  try {
    const res = await App.API.get('/api/admin/stats');
    if (!res || !res.data) return;
    const m = res.data;

    document.getElementById('statTotalStudents').textContent = m.totalStudents ?? 0;
    document.getElementById('statOccupancyPct').textContent = `${m.occupancyPercentage ?? 0}%`;
    document.getElementById('statBedsRatio').textContent = `${m.occupiedBeds ?? 0} / ${m.totalBeds ?? 0} Beds Occupied`;
    document.getElementById('statPendingComplaints').textContent = m.pendingComplaints ?? 0;
    document.getElementById('statPendingLeaves').textContent = m.pendingLeaves ?? 0;
    document.getElementById('statPendingFees').textContent = `₹ ${(m.pendingFees ?? 0).toLocaleString('en-IN')}`;
    document.getElementById('statCollectedFees').textContent = `₹ ${(m.collectedFees ?? 0).toLocaleString('en-IN')} Collected`;
    document.getElementById('statTodayVisitors').textContent = m.pendingVisitors ?? 0;

    // Badges in sidebar
    document.getElementById('badgeStudentsCount').textContent = m.totalStudents ?? 0;
    document.getElementById('badgeComplaintsCount').textContent = m.pendingComplaints ?? 0;
    document.getElementById('badgeLeavesCount').textContent = m.pendingLeaves ?? 0;

    // Meters
    const meterPct = `${m.occupancyPercentage ?? 0}%`;
    document.getElementById('meterOccupancyText').textContent = meterPct;
    document.getElementById('meterOccupancyBar').style.width = meterPct;
    document.getElementById('meterOccupiedBeds').textContent = `Occupied: ${m.occupiedBeds ?? 0} Beds`;
    document.getElementById('meterAvailableBeds').textContent = `Available: ${(m.totalBeds ?? 0) - (m.occupiedBeds ?? 0)} Beds`;
  } catch (err) {
    App.Toast.error('Failed to load dashboard metrics');
  }
}

// ==========================================================
// 3. STUDENTS
// ==========================================================
async function loadStudents() {
  const tbody = document.getElementById('studentsTableBody');
  tbody.innerHTML = '<tr><td colspan="11" style="text-align: center;">Loading students...</td></tr>';
  try {
    const res = await App.API.get('/api/admin/students');
    allStudentsCache = res.data || [];
    renderStudents(allStudentsCache);
    populateStudentSelects(allStudentsCache);
  } catch (err) {
    tbody.innerHTML = `<tr><td colspan="11" style="text-align: center; color: var(--danger);">Failed to load students: ${App.escapeHTML(err.message)}</td></tr>`;
  }
}

function renderStudents(students) {
  const tbody = document.getElementById('studentsTableBody');
  if (!students || students.length === 0) {
    tbody.innerHTML = '<tr><td colspan="11" class="empty-state">No students found.</td></tr>';
    return;
  }

  tbody.innerHTML = students.map(s => {
    const isInactive = (s.status || '').toUpperCase() === 'INACTIVE';
    const statusBadge = isInactive
      ? '<span class="badge" style="background: rgba(239, 68, 68, 0.15); color: #ef4444;">INACTIVE</span>'
      : '<span class="badge badge-available">ACTIVE</span>';

    return `
      <tr style="${isInactive ? 'opacity: 0.65;' : ''}">
        <td><strong>#${s.studentId}</strong></td>
        <td>
          <div style="font-weight: 600; color: var(--text-main);">${App.escapeHTML(s.name)}</div>
        </td>
        <td>${App.escapeHTML(s.department)}</td>
        <td>Yr ${s.year}</td>
        <td>${App.escapeHTML(s.phone)}</td>
        <td>${App.escapeHTML(s.email)}</td>
        <td><span class="badge badge-student">${App.escapeHTML(s.roomNumber || 'None')}</span></td>
        <td>${App.escapeHTML(s.bedNumber || '-')}</td>
        <td><code>${App.escapeHTML(s.username || '-')}</code></td>
        <td>${statusBadge}</td>
        <td style="text-align: right; white-space: nowrap;">
          <button class="btn btn-sm btn-secondary" onclick="resetStudentPassword(${s.studentId}, '${App.escapeHTML(s.name)}')">🔑 Reset Pwd</button>
          <button class="btn btn-sm ${isInactive ? 'btn-primary' : 'btn-outline'}" onclick="toggleStudentStatus(${s.studentId}, '${App.escapeHTML(s.name)}', '${s.status || 'ACTIVE'}')">
            ${isInactive ? 'Activate' : 'Deactivate'}
          </button>
          <button class="btn btn-sm btn-outline" style="color: var(--danger);" onclick="deleteStudent(${s.studentId}, '${App.escapeHTML(s.name)}')">Remove</button>
        </td>
      </tr>
    `;
  }).join('');
}

function filterStudents(q) {
  if (!q) {
    renderStudents(allStudentsCache);
    return;
  }
  const filtered = allStudentsCache.filter(s =>
    (s.name && s.name.toLowerCase().includes(q)) ||
    (s.email && s.email.toLowerCase().includes(q)) ||
    (s.phone && s.phone.includes(q)) ||
    (s.username && s.username.toLowerCase().includes(q)) ||
    (s.department && s.department.toLowerCase().includes(q)) ||
    (s.roomNumber && s.roomNumber.toLowerCase().includes(q))
  );
  renderStudents(filtered);
}

function populateStudentSelects(students) {
  const selects = ['visitorStudentSelect', 'attStudentSelect', 'secStudentSelect'];
  selects.forEach(id => {
    const el = document.getElementById(id);
    if (el) {
      el.innerHTML = id === 'secStudentSelect' ? '<option value="">General / Gate Alert</option>' : '<option value="">Select student...</option>';
      students.forEach(s => {
        el.innerHTML += `<option value="${s.studentId}">#${s.studentId} - ${App.escapeHTML(s.name)} (${App.escapeHTML(s.roomNumber || 'No Room')})</option>`;
      });
    }
  });
}

async function openAddStudentModal() {
  await loadCommonLookups();
  document.getElementById('addStudentForm')?.reset();
  App.Modal.open('addStudentModal');
}

window.deleteStudent = async function(id, name) {
  if (!confirm(`Are you sure you want to remove student "${name}"? This will free their assigned bed.`)) return;
  try {
    await App.API.delete(`/api/admin/students/${id}`);
    App.Toast.success(`Student ${name} removed.`);
    loadStudents();
    loadOverviewStats();
  } catch (err) {
    App.Toast.error(err.message || 'Failed to delete student');
  }
};

window.resetStudentPassword = async function(id, name) {
  if (!confirm(`Generate a new temporary password for student "${name}"?`)) return;
  try {
    const res = await App.API.post(`/api/admin/students/${id}/reset-password`, {});
    if (res && res.success && res.data) {
      document.getElementById('resetCredUsername').textContent = res.data.username;
      document.getElementById('resetCredPassword').textContent = res.data.temporaryPassword;

      const credText = `CHINNAMMAL — SMART MANAGEMENT PLATFORM\nStudent: ${name}\nUsername: ${res.data.username}\nNew Temporary Password: ${res.data.temporaryPassword}`;
      const copyBtn = document.getElementById('btnCopyResetCredentials');
      copyBtn.onclick = () => App.copyToClipboard(credText, copyBtn);

      App.Modal.open('resetPasswordModal');
      App.Toast.success('Temporary password generated.');
      loadStudents();
    }
  } catch (err) {
    App.Toast.error(err.message || 'Failed to reset password');
  }
};

window.toggleStudentStatus = async function(id, name, currentStatus) {
  const newStatus = (currentStatus || '').toUpperCase() === 'INACTIVE' ? 'ACTIVE' : 'INACTIVE';
  const actionText = newStatus === 'ACTIVE' ? 'reactivate' : 'deactivate';
  if (!confirm(`Are you sure you want to ${actionText} the account for student "${name}"?`)) return;

  try {
    await App.API.put(`/api/admin/students/${id}/status`, { status: newStatus });
    App.Toast.success(`Student account marked as ${newStatus}.`);
    loadStudents();
  } catch (err) {
    App.Toast.error(err.message || 'Failed to update account status');
  }
};

// ==========================================================
// 4. WARDENS
// ==========================================================
async function openAddWardenModal() {
  await loadCommonLookups();
  document.getElementById('addWardenForm')?.reset();
  App.Modal.open('addWardenModal');
}

async function loadWardens() {
  const tbody = document.getElementById('wardensTableBody');
  tbody.innerHTML = '<tr><td colspan="9" style="text-align: center;">Loading wardens...</td></tr>';
  try {
    const res = await App.API.get('/api/admin/wardens');
    allWardensCache = res.data || [];
    renderWardens(allWardensCache);
  } catch (err) {
    tbody.innerHTML = `<tr><td colspan="9" style="text-align: center; color: var(--danger);">${App.escapeHTML(err.message)}</td></tr>`;
  }
}

function renderWardens(wardens) {
  const tbody = document.getElementById('wardensTableBody');
  if (!wardens || wardens.length === 0) {
    tbody.innerHTML = '<tr><td colspan="9" class="empty-state">No wardens recorded.</td></tr>';
    return;
  }

  tbody.innerHTML = wardens.map(w => {
    const isInactive = (w.status || '').toUpperCase() === 'INACTIVE';
    const statusBadge = isInactive
      ? '<span class="badge" style="background: rgba(239, 68, 68, 0.15); color: #ef4444;">INACTIVE</span>'
      : '<span class="badge badge-available">ACTIVE</span>';

    return `
      <tr style="${isInactive ? 'opacity: 0.65;' : ''}">
        <td><strong>${App.escapeHTML(w.name)}</strong></td>
        <td><code>${App.escapeHTML(w.employeeId || '-')}</code></td>
        <td>${App.escapeHTML(w.department || '-')}</td>
        <td>${App.escapeHTML(w.phone)}</td>
        <td>${App.escapeHTML(w.email)}</td>
        <td><span class="badge badge-warden">${App.escapeHTML(w.blockName || (w.assignedBlock ? 'Block ' + w.assignedBlock : 'Unassigned'))}</span></td>
        <td><code>${App.escapeHTML(w.username || '-')}</code></td>
        <td>${statusBadge}</td>
        <td style="text-align: right; white-space: nowrap;">
          <button class="btn btn-sm btn-outline" onclick="viewWardenDetails(${w.wardenId})">View</button>
          <button class="btn btn-sm btn-secondary" onclick="openEditWardenModal(${w.wardenId})">Edit</button>
          <button class="btn btn-sm btn-secondary" onclick="resetWardenPassword(${w.wardenId}, '${App.escapeHTML(w.name)}')">🔑 Reset Pwd</button>
          <button class="btn btn-sm ${isInactive ? 'btn-primary' : 'btn-outline'}" onclick="toggleWardenStatus(${w.wardenId}, '${App.escapeHTML(w.name)}', '${w.status || 'ACTIVE'}')">
            ${isInactive ? 'Activate' : 'Deactivate'}
          </button>
          <button class="btn btn-sm btn-outline" style="color: var(--danger);" onclick="deleteWarden(${w.wardenId}, '${App.escapeHTML(w.name)}')">Delete</button>
        </td>
      </tr>
    `;
  }).join('');
}

window.viewWardenDetails = function(id) {
  const w = allWardensCache.find(x => x.wardenId === id);
  if (!w) return;

  document.getElementById('viewWardenName').textContent = w.name;
  document.getElementById('viewWardenEmpId').textContent = w.employeeId || '-';
  document.getElementById('viewWardenDept').textContent = w.department || '-';
  document.getElementById('viewWardenPhone').textContent = w.phone;
  document.getElementById('viewWardenEmail').textContent = w.email;
  document.getElementById('viewWardenBlock').textContent = w.blockName || (w.assignedBlock ? 'Block ' + w.assignedBlock : 'Unassigned');
  document.getElementById('viewWardenAddress').textContent = w.address || 'None provided';
  document.getElementById('viewWardenUsername').textContent = w.username || '-';

  const isInactive = (w.status || '').toUpperCase() === 'INACTIVE';
  document.getElementById('viewWardenStatus').innerHTML = isInactive
    ? '<span class="badge" style="background: rgba(239, 68, 68, 0.15); color: #ef4444;">INACTIVE</span>'
    : '<span class="badge badge-available">ACTIVE</span>';

  App.Modal.open('viewWardenModal');
};

window.openEditWardenModal = async function(id) {
  await loadCommonLookups();
  const w = allWardensCache.find(x => x.wardenId === id);
  if (!w) return;

  document.getElementById('editWardenId').value = w.wardenId;
  document.getElementById('editWardenName').value = w.name;
  document.getElementById('editWardenEmpId').value = w.employeeId || '';
  document.getElementById('editWardenDept').value = w.department || '';
  document.getElementById('editWardenPhone').value = w.phone;
  document.getElementById('editWardenEmail').value = w.email;
  document.getElementById('editWardenBlock').value = w.assignedBlock || '';
  document.getElementById('editWardenAddress').value = w.address || '';

  App.Modal.open('editWardenModal');
};

window.resetWardenPassword = async function(id, name) {
  if (!confirm(`Generate a new temporary password for warden "${name}"?`)) return;
  try {
    const res = await App.API.post(`/api/admin/wardens/${id}/reset-password`, {});
    if (res && res.success && res.data) {
      document.getElementById('resetCredUsername').textContent = res.data.username;
      document.getElementById('resetCredPassword').textContent = res.data.temporaryPassword;

      const credText = `CHINNAMMAL — SMART MANAGEMENT PLATFORM\nWarden: ${name}\nUsername: ${res.data.username}\nNew Temporary Password: ${res.data.temporaryPassword}`;
      const copyBtn = document.getElementById('btnCopyResetCredentials');
      copyBtn.onclick = () => App.copyToClipboard(credText, copyBtn);

      App.Modal.open('resetPasswordModal');
      App.Toast.success('Temporary password generated.');
      loadWardens();
    }
  } catch (err) {
    App.Toast.error(err.message || 'Failed to reset password');
  }
};

window.toggleWardenStatus = async function(id, name, currentStatus) {
  const newStatus = (currentStatus || '').toUpperCase() === 'INACTIVE' ? 'ACTIVE' : 'INACTIVE';
  const actionText = newStatus === 'ACTIVE' ? 'reactivate' : 'deactivate';
  if (!confirm(`Are you sure you want to ${actionText} the account for warden "${name}"?`)) return;

  try {
    await App.API.put(`/api/admin/wardens/${id}/status`, { status: newStatus });
    App.Toast.success(`Warden account marked as ${newStatus}.`);
    loadWardens();
  } catch (err) {
    App.Toast.error(err.message || 'Failed to update account status');
  }
};

window.deleteWarden = async function(id, name = 'this warden') {
  if (!confirm(`Are you sure you want to delete ${name}? This will also delete their login account.`)) return;
  try {
    await App.API.delete(`/api/admin/wardens/${id}`);
    App.Toast.success('Warden deleted successfully');
    loadWardens();
  } catch (err) {
    App.Toast.error(err.message);
  }
};

// ==========================================================
// 5. ROOMS
// ==========================================================
async function loadRooms(blockId = null) {
  const tbody = document.getElementById('roomsTableBody');
  tbody.innerHTML = '<tr><td colspan="6" style="text-align: center;">Loading rooms...</td></tr>';
  try {
    const url = blockId ? `/api/admin/rooms?blockId=${blockId}` : '/api/admin/rooms';
    const res = await App.API.get(url);
    const rooms = res.data || [];
    if (rooms.length === 0) {
      tbody.innerHTML = '<tr><td colspan="6" class="empty-state">No rooms found.</td></tr>';
      return;
    }
    tbody.innerHTML = rooms.map(r => {
      const avail = r.availableBeds ?? (r.capacity - r.occupied);
      const badgeCls = avail === 0 ? 'badge-full' : 'badge-available';
      return `
        <tr>
          <td><strong>${App.escapeHTML(r.roomNumber)}</strong></td>
          <td>${App.escapeHTML(r.blockName || 'Block ' + r.blockId)}</td>
          <td>${r.capacity} Beds</td>
          <td>${r.occupied}</td>
          <td><strong style="color: ${avail > 0 ? 'var(--success)' : 'var(--danger)'};">${avail}</strong></td>
          <td><span class="badge ${badgeCls}">${avail === 0 ? 'FULL' : 'AVAILABLE'}</span></td>
        </tr>
      `;
    }).join('');
  } catch (err) {
    tbody.innerHTML = `<tr><td colspan="6" style="text-align: center; color: var(--danger);">${App.escapeHTML(err.message)}</td></tr>`;
  }
}

// ==========================================================
// 6. FEES
// ==========================================================
async function loadFees(status = 'ALL') {
  const tbody = document.getElementById('feesTableBody');
  tbody.innerHTML = '<tr><td colspan="8" style="text-align: center;">Loading fee records...</td></tr>';
  try {
    const res = await App.API.get('/api/admin/fees');
    let fees = res.data || [];
    if (status === 'PAID') fees = fees.filter(f => f.balance <= 0);
    else if (status === 'PENDING') fees = fees.filter(f => f.balance > 0);

    if (fees.length === 0) {
      tbody.innerHTML = '<tr><td colspan="8" class="empty-state">No fee records found.</td></tr>';
      return;
    }

    tbody.innerHTML = fees.map(f => `
      <tr>
        <td><strong>#${f.feeId}</strong></td>
        <td><strong>${App.escapeHTML(f.studentName || 'Student #' + f.studentId)}</strong></td>
        <td>₹ ${f.totalAmount.toLocaleString('en-IN')}</td>
        <td>₹ ${f.paidAmount.toLocaleString('en-IN')}</td>
        <td><strong style="color: ${f.balance > 0 ? 'var(--danger)' : 'var(--success)'};">₹ ${f.balance.toLocaleString('en-IN')}</strong></td>
        <td>${App.escapeHTML(f.dueDate || '-')}</td>
        <td><span class="badge ${f.balance <= 0 ? 'badge-paid' : 'badge-pending'}">${App.escapeHTML(f.status)}</span></td>
        <td style="text-align: right;">
          ${f.balance > 0 ? `<button class="btn btn-sm btn-primary" onclick="openFeePayModal(${f.feeId}, '${App.escapeHTML(f.studentName || '')}', ${f.balance})">Record Payment</button>` : '<span style="color: var(--success); font-weight: 600; font-size: 0.8rem;">✓ Settled</span>'}
        </td>
      </tr>
    `).join('');
  } catch (err) {
    tbody.innerHTML = `<tr><td colspan="8" style="text-align: center; color: var(--danger);">${App.escapeHTML(err.message)}</td></tr>`;
  }
}

window.openFeePayModal = function(feeId, studentName, balance) {
  document.getElementById('payFeeId').value = feeId;
  document.getElementById('payStudentName').value = studentName;
  document.getElementById('payCurrentBalance').value = balance.toLocaleString('en-IN');
  document.getElementById('payAmount').value = balance;
  document.getElementById('payAmount').max = balance;
  App.Modal.open('feePaymentModal');
};

// ==========================================================
// 7. COMPLAINTS
// ==========================================================
async function loadComplaints(filter = 'ALL') {
  const tbody = document.getElementById('complaintsTableBody');
  tbody.innerHTML = '<tr><td colspan="8" style="text-align: center;">Loading complaints...</td></tr>';
  try {
    const res = await App.API.get('/api/admin/complaints');
    let list = res.data || [];
    if (filter !== 'ALL') {
      list = list.filter(c => (c.status || '').toUpperCase() === filter);
    }
    if (list.length === 0) {
      tbody.innerHTML = '<tr><td colspan="8" class="empty-state">No complaints matching filter.</td></tr>';
      return;
    }

    tbody.innerHTML = list.map(c => `
      <tr>
        <td><strong>#${c.complaintId}</strong></td>
        <td>${App.escapeHTML(c.studentName || 'Student #' + c.studentId)}</td>
        <td><span class="badge badge-warden">${App.escapeHTML(c.roomNumber || 'General')}</span></td>
        <td><strong>${App.escapeHTML(c.category)}</strong></td>
        <td>${App.escapeHTML(c.description)}</td>
        <td><span class="badge ${c.priority === 'HIGH' ? 'badge-full' : 'badge-pending'}">${App.escapeHTML(c.priority)}</span></td>
        <td><span class="badge ${c.status === 'RESOLVED' ? 'badge-paid' : 'badge-pending'}">${App.escapeHTML(c.status)}</span></td>
        <td style="text-align: right;">
          ${c.status !== 'RESOLVED' ? `
            <select class="form-control" style="width: 130px; display: inline-block; padding: 4px 8px; font-size: 0.78rem;" onchange="updateComplaintStatus(${c.complaintId}, this.value)">
              <option value="OPEN" ${c.status === 'OPEN' ? 'selected' : ''}>OPEN</option>
              <option value="IN_PROGRESS" ${c.status === 'IN_PROGRESS' ? 'selected' : ''}>IN PROGRESS</option>
              <option value="RESOLVED">RESOLVED</option>
            </select>
          ` : '<span style="color: var(--success); font-weight: 600;">✓ Closed</span>'}
        </td>
      </tr>
    `).join('');
  } catch (err) {
    tbody.innerHTML = `<tr><td colspan="8" style="text-align: center; color: var(--danger);">${App.escapeHTML(err.message)}</td></tr>`;
  }
}

window.updateComplaintStatus = async function(id, status) {
  try {
    await App.API.put(`/api/admin/complaints/${id}/status`, { status });
    App.Toast.success(`Complaint status set to ${status}`);
    loadComplaints();
    loadOverviewStats();
  } catch (err) {
    App.Toast.error(err.message);
  }
};

// ==========================================================
// 8. LEAVES
// ==========================================================
async function loadLeaves() {
  const tbody = document.getElementById('leavesTableBody');
  tbody.innerHTML = '<tr><td colspan="7" style="text-align: center;">Loading leave requests...</td></tr>';
  try {
    const res = await App.API.get('/api/admin/leaves');
    const list = res.data || [];
    if (list.length === 0) {
      tbody.innerHTML = '<tr><td colspan="7" class="empty-state">No leave applications found.</td></tr>';
      return;
    }

    tbody.innerHTML = list.map(l => {
      let badgeCls = 'badge-pending';
      if (l.status === 'APPROVED') badgeCls = 'badge-paid';
      else if (l.status === 'REJECTED') badgeCls = 'badge-full';

      return `
        <tr>
          <td><strong>#${l.leaveId}</strong></td>
          <td><strong>${App.escapeHTML(l.studentName || 'Student #' + l.studentId)}</strong></td>
          <td>${App.escapeHTML(l.fromDate)} to ${App.escapeHTML(l.toDate)}</td>
          <td>${App.escapeHTML(l.reason)}</td>
          <td>${App.escapeHTML(l.createdAt ? l.createdAt.split('T')[0] : '-')}</td>
          <td><span class="badge ${badgeCls}">${App.escapeHTML(l.status)}</span></td>
          <td style="text-align: right;">
            ${l.status === 'PENDING' ? `
              <button class="btn btn-sm btn-success" onclick="updateLeaveStatus(${l.leaveId}, 'APPROVED')">Approve</button>
              <button class="btn btn-sm btn-outline" style="color: var(--danger);" onclick="updateLeaveStatus(${l.leaveId}, 'REJECTED')">Reject</button>
            ` : `<span style="font-size: 0.8rem; color: var(--text-muted);">${l.status}</span>`}
          </td>
        </tr>
      `;
    }).join('');
  } catch (err) {
    tbody.innerHTML = `<tr><td colspan="7" style="text-align: center; color: var(--danger);">${App.escapeHTML(err.message)}</td></tr>`;
  }
}

window.updateLeaveStatus = async function(id, status) {
  try {
    await App.API.put(`/api/admin/leaves/${id}/status`, { status });
    App.Toast.success(`Leave request ${status.toLowerCase()}!`);
    loadLeaves();
    loadOverviewStats();
  } catch (err) {
    App.Toast.error(err.message);
  }
};

// ==========================================================
// 9. ATTENDANCE
// ==========================================================
async function loadAttendance() {
  const tbody = document.getElementById('attendanceTableBody');
  tbody.innerHTML = '<tr><td colspan="6" style="text-align: center;">Loading attendance...</td></tr>';
  try {
    const res = await App.API.get('/api/admin/attendance');
    const list = res.data || [];
    if (list.length === 0) {
      tbody.innerHTML = '<tr><td colspan="6" class="empty-state">No attendance records.</td></tr>';
      return;
    }

    tbody.innerHTML = list.map(a => {
      let badgeCls = 'badge-paid';
      if (a.status === 'ABSENT') badgeCls = 'badge-full';
      else if (a.status === 'LATE') badgeCls = 'badge-pending';
      else if (a.status === 'ON_LEAVE') badgeCls = 'badge-student';

      return `
        <tr>
          <td><strong>${App.escapeHTML(a.date)}</strong></td>
          <td><strong>${App.escapeHTML(a.studentName || 'Student #' + a.studentId)}</strong></td>
          <td>${App.escapeHTML(a.roomNumber || '-')}</td>
          <td>${App.escapeHTML(a.entryTime || '-')}</td>
          <td>${App.escapeHTML(a.exitTime || '-')}</td>
          <td><span class="badge ${badgeCls}">${App.escapeHTML(a.status)}</span></td>
        </tr>
      `;
    }).join('');
  } catch (err) {
    tbody.innerHTML = `<tr><td colspan="6" style="text-align: center; color: var(--danger);">${App.escapeHTML(err.message)}</td></tr>`;
  }
}

// ==========================================================
// 10. VISITORS
// ==========================================================
async function loadVisitors() {
  const tbody = document.getElementById('visitorsTableBody');
  tbody.innerHTML = '<tr><td colspan="8" style="text-align: center;">Loading visitors...</td></tr>';
  try {
    const res = await App.API.get('/api/admin/visitors');
    const list = res.data || [];
    if (list.length === 0) {
      tbody.innerHTML = '<tr><td colspan="8" class="empty-state">No visitor passes issued.</td></tr>';
      return;
    }

    tbody.innerHTML = list.map(v => `
      <tr>
        <td><strong>#${v.visitorId}</strong></td>
        <td><strong>${App.escapeHTML(v.visitorName)}</strong></td>
        <td>${App.escapeHTML(v.relationship)}</td>
        <td>${App.escapeHTML(v.phone)}</td>
        <td>${App.escapeHTML(v.studentName || 'Student #' + v.studentId)}</td>
        <td>${App.escapeHTML(v.visitDate)} ${App.escapeHTML(v.visitTime || '')}</td>
        <td><span class="badge ${v.status === 'CHECKED_OUT' ? 'badge-paid' : 'badge-pending'}">${App.escapeHTML(v.status)}</span></td>
        <td style="text-align: right;">
          ${v.status !== 'CHECKED_OUT' ? `<button class="btn btn-sm btn-outline" onclick="checkOutVisitor(${v.visitorId})">Check Out</button>` : '<span style="color: var(--text-muted); font-size: 0.8rem;">Completed</span>'}
        </td>
      </tr>
    `).join('');
  } catch (err) {
    tbody.innerHTML = `<tr><td colspan="8" style="text-align: center; color: var(--danger);">${App.escapeHTML(err.message)}</td></tr>`;
  }
}

window.checkOutVisitor = async function(id) {
  try {
    await App.API.put(`/api/admin/visitors/${id}/status`, { status: 'CHECKED_OUT' });
    App.Toast.success('Visitor departure logged');
    loadVisitors();
  } catch (err) {
    App.Toast.error(err.message);
  }
};

// ==========================================================
// 11. SECURITY LOGS
// ==========================================================
async function loadSecurityLogs() {
  const tbody = document.getElementById('securityLogsTableBody');
  tbody.innerHTML = '<tr><td colspan="6" style="text-align: center;">Loading security audit logs...</td></tr>';
  try {
    const res = await App.API.get('/api/admin/security-logs?limit=50');
    const list = res.data || [];
    if (list.length === 0) {
      tbody.innerHTML = '<tr><td colspan="6" class="empty-state">No security logs recorded.</td></tr>';
      return;
    }

    tbody.innerHTML = list.map(l => `
      <tr>
        <td><strong>#${l.logId}</strong></td>
        <td><span class="badge ${l.eventType === 'ENTRY' ? 'badge-available' : 'badge-warden'}">${App.escapeHTML(l.eventType)}</span></td>
        <td>${App.escapeHTML(l.studentName || (l.studentId ? 'Student #' + l.studentId : 'General Entry'))}</td>
        <td>${App.escapeHTML(l.gate)}</td>
        <td>${App.escapeHTML(l.verificationMethod)}</td>
        <td>${App.escapeHTML(l.eventTime ? l.eventTime.replace('T', ' ') : '-')}</td>
      </tr>
    `).join('');
  } catch (err) {
    tbody.innerHTML = `<tr><td colspan="6" style="text-align: center; color: var(--danger);">${App.escapeHTML(err.message)}</td></tr>`;
  }
}

// ==========================================================
// 12. REPORTS
// ==========================================================
async function loadReports() {
  const reportBox = document.getElementById('reportTextContent');
  reportBox.textContent = 'Generating executive analytics report...';
  try {
    const res = await App.API.get('/api/admin/reports');
    if (res && res.data && res.data.reportText) {
      reportBox.textContent = res.data.reportText;
    }
  } catch (err) {
    reportBox.textContent = 'Failed to generate report: ' + err.message;
  }
}

document.getElementById('btnCopyReportText')?.addEventListener('click', (e) => {
  const text = document.getElementById('reportTextContent')?.textContent;
  if (text) {
    App.copyToClipboard(text, e.target);
  }
});

// ==========================================================
// 13. MODAL FORMS SUBMISSIONS
// ==========================================================
function setupForms() {
  // Add Student Form (Dynamic Account Generation)
  const addStudentForm = document.getElementById('addStudentForm');
  if (addStudentForm) {
    addStudentForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const submitBtn = document.getElementById('btnSubmitAddStudent');
      submitBtn.disabled = true;
      submitBtn.textContent = 'Registering & Generating...';

      const payload = {
        name: document.getElementById('addStudentName').value.trim(),
        department: document.getElementById('addStudentDept').value.trim(),
        year: parseInt(document.getElementById('addStudentYear').value),
        phone: document.getElementById('addStudentPhone').value.trim(),
        email: document.getElementById('addStudentEmail').value.trim(),
        roomId: parseInt(document.getElementById('addStudentRoom').value),
        bedNumber: document.getElementById('addStudentBed').value.trim()
      };

      try {
        const res = await App.API.post('/api/admin/students', payload);
        if (res && res.success && res.data) {
          App.Modal.close('addStudentModal');
          const data = res.data;

          // Populate credentials modal
          document.getElementById('credStudentName').textContent = data.student.name;
          document.getElementById('credUsername').textContent = data.username;
          document.getElementById('credPassword').textContent = data.temporaryPassword;

          // Attach copy button action
          const credText = `CHINNAMMAL — SMART MANAGEMENT PLATFORM\nStudent Name: ${data.student.name}\nUsername: ${data.username}\nTemporary Password: ${data.temporaryPassword}`;
          const copyBtn = document.getElementById('btnCopyCredentials');
          copyBtn.onclick = () => App.copyToClipboard(credText, copyBtn);

          App.Modal.open('credentialsModal');
          App.Toast.success('Student registered with dynamic credentials!');

          loadStudents();
          loadOverviewStats();
        }
      } catch (err) {
        App.Toast.error(err.message || 'Registration failed');
      } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = 'Save & Generate Credentials';
      }
    });
  }

  // Add Warden Form (Dynamic Account Generation)
  const addWardenForm = document.getElementById('addWardenForm');
  if (addWardenForm) {
    addWardenForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const submitBtn = document.getElementById('btnSubmitAddWarden');
      submitBtn.disabled = true;
      submitBtn.textContent = 'Saving & Provisioning...';

      const blockVal = document.getElementById('addWardenBlock').value;
      const payload = {
        name: document.getElementById('addWardenName').value.trim(),
        employeeId: document.getElementById('addWardenEmpId').value.trim(),
        department: document.getElementById('addWardenDept').value.trim(),
        phone: document.getElementById('addWardenPhone').value.trim(),
        email: document.getElementById('addWardenEmail').value.trim(),
        assignedBlock: blockVal ? parseInt(blockVal) : null,
        address: document.getElementById('addWardenAddress').value.trim() || null
      };

      try {
        const res = await App.API.post('/api/admin/wardens', payload);
        if (res && res.success && res.data) {
          App.Modal.close('addWardenModal');
          const data = res.data;

          // Populate warden credentials modal
          document.getElementById('credWardenName').textContent = data.warden.name;
          document.getElementById('credWardenEmpId').textContent = data.warden.employeeId;
          document.getElementById('credWardenUsername').textContent = data.username;
          document.getElementById('credWardenPassword').textContent = data.temporaryPassword;

          const credText = `CHINNAMMAL — SMART MANAGEMENT PLATFORM\nWarden: ${data.warden.name}\nEmployee ID: ${data.warden.employeeId}\nUsername: ${data.username}\nTemporary Password: ${data.temporaryPassword}`;
          const copyBtn = document.getElementById('btnCopyWardenCredentials');
          copyBtn.onclick = () => App.copyToClipboard(credText, copyBtn);

          App.Modal.open('wardenCredentialsModal');
          App.Toast.success('Warden created with dynamic credentials!');
          loadWardens();
        }
      } catch (err) {
        App.Toast.error(err.message || 'Failed to create warden');
      } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = 'Save & Generate Credentials';
      }
    });
  }

  // Edit Warden Form
  const editWardenForm = document.getElementById('editWardenForm');
  if (editWardenForm) {
    editWardenForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const id = parseInt(document.getElementById('editWardenId').value);
      const blockVal = document.getElementById('editWardenBlock').value;
      const payload = {
        name: document.getElementById('editWardenName').value.trim(),
        employeeId: document.getElementById('editWardenEmpId').value.trim(),
        department: document.getElementById('editWardenDept').value.trim(),
        phone: document.getElementById('editWardenPhone').value.trim(),
        email: document.getElementById('editWardenEmail').value.trim(),
        assignedBlock: blockVal ? parseInt(blockVal) : null,
        address: document.getElementById('editWardenAddress').value.trim() || null
      };

      try {
        await App.API.put(`/api/admin/wardens/${id}`, payload);
        App.Toast.success('Warden updated successfully');
        App.Modal.close('editWardenModal');
        loadWardens();
      } catch (err) {
        App.Toast.error(err.message || 'Failed to update warden');
      }
    });
  }

  // Add Room Form
  const addRoomForm = document.getElementById('addRoomForm');
  if (addRoomForm) {
    addRoomForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const payload = {
        roomNumber: document.getElementById('addRoomNumber').value.trim(),
        blockId: parseInt(document.getElementById('addRoomBlock').value),
        capacity: parseInt(document.getElementById('addRoomCapacity').value)
      };
      try {
        await App.API.post('/api/admin/rooms', payload);
        App.Toast.success('Room created successfully');
        App.Modal.close('addRoomModal');
        loadRooms();
        loadOverviewStats();
      } catch (err) {
        App.Toast.error(err.message);
      }
    });
  }

  // Record Fee Payment Form
  const feePaymentForm = document.getElementById('feePaymentForm');
  if (feePaymentForm) {
    feePaymentForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const payload = {
        feeId: parseInt(document.getElementById('payFeeId').value),
        amount: parseFloat(document.getElementById('payAmount').value)
      };
      try {
        await App.API.post('/api/admin/fees/pay', payload);
        App.Toast.success('Payment recorded successfully');
        App.Modal.close('feePaymentModal');
        loadFees();
        loadOverviewStats();
      } catch (err) {
        App.Toast.error(err.message);
      }
    });
  }

  // Record Visitor Form
  const addVisitorForm = document.getElementById('addVisitorForm');
  if (addVisitorForm) {
    addVisitorForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const payload = {
        studentId: parseInt(document.getElementById('visitorStudentSelect').value),
        visitorName: document.getElementById('visitorName').value.trim(),
        relationship: document.getElementById('visitorRelation').value.trim(),
        phone: document.getElementById('visitorPhone').value.trim(),
        visitDate: new Date().toISOString().split('T')[0]
      };
      try {
        await App.API.post('/api/admin/visitors', payload);
        App.Toast.success('Visitor entry pass issued');
        App.Modal.close('addVisitorModal');
        loadVisitors();
      } catch (err) {
        App.Toast.error(err.message);
      }
    });
  }

  // Mark Attendance Form
  const markAttendanceForm = document.getElementById('markAttendanceForm');
  if (markAttendanceForm) {
    document.getElementById('attDate').value = new Date().toISOString().split('T')[0];
    markAttendanceForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const payload = {
        studentId: parseInt(document.getElementById('attStudentSelect').value),
        date: document.getElementById('attDate').value,
        status: document.getElementById('attStatus').value
      };
      try {
        await App.API.post('/api/admin/attendance', payload);
        App.Toast.success('Attendance recorded');
        App.Modal.close('markAttendanceModal');
        loadAttendance();
      } catch (err) {
        App.Toast.error(err.message);
      }
    });
  }

  // Log Security Form
  const logSecurityForm = document.getElementById('logSecurityForm');
  if (logSecurityForm) {
    logSecurityForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const stVal = document.getElementById('secStudentSelect').value;
      const payload = {
        studentId: stVal ? parseInt(stVal) : null,
        eventType: document.getElementById('secEventType').value,
        gate: document.getElementById('secGate').value.trim(),
        verificationMethod: document.getElementById('secMethod').value
      };
      try {
        await App.API.post('/api/admin/security-logs', payload);
        App.Toast.success('Security audit log recorded');
        App.Modal.close('logSecurityModal');
        loadSecurityLogs();
      } catch (err) {
        App.Toast.error(err.message);
      }
    });
  }
}
