/**
 * CHINNAMMAL — SMART MANAGEMENT PLATFORM
 * Warden Portal Logic
 */

document.addEventListener('DOMContentLoaded', async () => {
  const currentUser = await App.Auth.checkSession('WARDEN');
  if (!currentUser) return;

  if (currentUser.assignedBlockId) {
    const lbl = document.getElementById('sidebarBlockLabel');
    if (lbl) lbl.textContent = `Assigned: Block ${currentUser.assignedBlockId}`;
  }

  App.setupTabs('overview', (tabId) => {
    loadTabData(tabId);
  });

  // Action buttons
  document.getElementById('btnWardenQuickAtt')?.addEventListener('click', () => App.Modal.open('wardenAttModal'));
  document.getElementById('btnWardenQuickVis')?.addEventListener('click', () => App.Modal.open('wardenVisModal'));
  document.getElementById('btnWardenAttModalOpen')?.addEventListener('click', () => App.Modal.open('wardenAttModal'));
  document.getElementById('btnWardenVisModalOpen')?.addEventListener('click', () => App.Modal.open('wardenVisModal'));

  loadWardenStats();
  setupWardenForms();
});

let wardenStudentsCache = [];

function loadTabData(tabId) {
  switch (tabId) {
    case 'overview': loadWardenStats(); break;
    case 'students': loadStudents(); break;
    case 'rooms': loadRooms(); break;
    case 'complaints': loadComplaints(); break;
    case 'leaves': loadLeaves(); break;
    case 'attendance': loadAttendance(); break;
    case 'visitors': loadVisitors(); break;
    case 'security': loadSecurityLogs(); break;
  }
}

async function loadWardenStats() {
  try {
    const res = await App.API.get('/api/warden/stats');
    if (!res || !res.data) return;
    const s = res.data;
    document.getElementById('statWardenStudents').textContent = s.totalStudents ?? 0;
    document.getElementById('statWardenRooms').textContent = s.totalRooms ?? 0;
    document.getElementById('statWardenComplaints').textContent = s.openComplaints ?? 0;
    document.getElementById('statWardenLeaves').textContent = s.pendingLeaves ?? 0;

    document.getElementById('badgeWardenComplaints').textContent = s.openComplaints ?? 0;
    document.getElementById('badgeWardenLeaves').textContent = s.pendingLeaves ?? 0;
  } catch (err) {
    console.error('Failed to load warden stats:', err);
  }
}

async function loadStudents() {
  const tbody = document.getElementById('wardenStudentsTableBody');
  tbody.innerHTML = '<tr><td colspan="8" style="text-align: center;">Loading residents...</td></tr>';
  try {
    const res = await App.API.get('/api/warden/students');
    wardenStudentsCache = res.data || [];
    populateWardenStudentSelects(wardenStudentsCache);

    if (wardenStudentsCache.length === 0) {
      tbody.innerHTML = '<tr><td colspan="8" class="empty-state">No residents assigned to your block.</td></tr>';
      return;
    }
    tbody.innerHTML = wardenStudentsCache.map(s => `
      <tr>
        <td><strong>#${s.studentId}</strong></td>
        <td><strong>${App.escapeHTML(s.name)}</strong></td>
        <td>${App.escapeHTML(s.department)}</td>
        <td>Year ${s.year}</td>
        <td>${App.escapeHTML(s.phone)}</td>
        <td>${App.escapeHTML(s.email)}</td>
        <td><span class="badge badge-student">${App.escapeHTML(s.roomNumber || 'None')}</span></td>
        <td>${App.escapeHTML(s.bedNumber || '-')}</td>
      </tr>
    `).join('');
  } catch (err) {
    tbody.innerHTML = `<tr><td colspan="8" style="text-align: center; color: var(--danger);">${App.escapeHTML(err.message)}</td></tr>`;
  }
}

function populateWardenStudentSelects(students) {
  const selects = ['wAttStudentSelect', 'wVisStudentSelect'];
  selects.forEach(id => {
    const el = document.getElementById(id);
    if (el) {
      el.innerHTML = '<option value="">Select student...</option>';
      students.forEach(s => {
        el.innerHTML += `<option value="${s.studentId}">#${s.studentId} - ${App.escapeHTML(s.name)} (${App.escapeHTML(s.roomNumber || 'No Room')})</option>`;
      });
    }
  });
}

async function loadRooms() {
  const tbody = document.getElementById('wardenRoomsTableBody');
  tbody.innerHTML = '<tr><td colspan="5" style="text-align: center;">Loading rooms...</td></tr>';
  try {
    const res = await App.API.get('/api/warden/rooms');
    const rooms = res.data || [];
    if (rooms.length === 0) {
      tbody.innerHTML = '<tr><td colspan="5" class="empty-state">No rooms found.</td></tr>';
      return;
    }
    tbody.innerHTML = rooms.map(r => {
      const avail = r.availableBeds ?? (r.capacity - r.occupied);
      return `
        <tr>
          <td><strong>${App.escapeHTML(r.roomNumber)}</strong></td>
          <td>${r.capacity} Beds</td>
          <td>${r.occupied}</td>
          <td><strong style="color: ${avail > 0 ? 'var(--success)' : 'var(--danger)'};">${avail}</strong></td>
          <td><span class="badge ${avail === 0 ? 'badge-full' : 'badge-available'}">${avail === 0 ? 'FULL' : 'AVAILABLE'}</span></td>
        </tr>
      `;
    }).join('');
  } catch (err) {
    tbody.innerHTML = `<tr><td colspan="5" style="text-align: center; color: var(--danger);">${App.escapeHTML(err.message)}</td></tr>`;
  }
}

async function loadComplaints() {
  const tbody = document.getElementById('wardenComplaintsTableBody');
  tbody.innerHTML = '<tr><td colspan="8" style="text-align: center;">Loading complaints...</td></tr>';
  try {
    const res = await App.API.get('/api/warden/complaints');
    const list = res.data || [];
    if (list.length === 0) {
      tbody.innerHTML = '<tr><td colspan="8" class="empty-state">No complaints logged.</td></tr>';
      return;
    }
    tbody.innerHTML = list.map(c => `
      <tr>
        <td><strong>#${c.complaintId}</strong></td>
        <td><strong>${App.escapeHTML(c.studentName || '#' + c.studentId)}</strong></td>
        <td>${App.escapeHTML(c.roomNumber || '-')}</td>
        <td><span class="badge badge-admin">${App.escapeHTML(c.category)}</span></td>
        <td>${App.escapeHTML(c.description)}</td>
        <td><strong>${App.escapeHTML(c.priority || 'MEDIUM')}</strong></td>
        <td><span class="badge badge-${(c.status || 'open').toLowerCase()}">${App.escapeHTML(c.status)}</span></td>
        <td style="text-align: right;">
          <select class="form-control" style="width: 130px; display: inline-block; padding: 4px 8px; font-size: 0.8rem;" onchange="updateWardenComplaint(${c.complaintId}, this.value)">
            <option value="OPEN" ${c.status === 'OPEN' ? 'selected' : ''}>OPEN</option>
            <option value="IN_PROGRESS" ${c.status === 'IN_PROGRESS' ? 'selected' : ''}>IN PROGRESS</option>
            <option value="RESOLVED" ${c.status === 'RESOLVED' ? 'selected' : ''}>RESOLVED</option>
            <option value="REJECTED" ${c.status === 'REJECTED' ? 'selected' : ''}>REJECTED</option>
          </select>
        </td>
      </tr>
    `).join('');
  } catch (err) {
    tbody.innerHTML = `<tr><td colspan="8" style="text-align: center; color: var(--danger);">${App.escapeHTML(err.message)}</td></tr>`;
  }
}

window.updateWardenComplaint = async function(id, status) {
  try {
    await App.API.put(`/api/warden/complaints/${id}/status`, { status });
    App.Toast.success(`Complaint #${id} updated to ${status}`);
    loadComplaints();
    loadWardenStats();
  } catch (err) {
    App.Toast.error(err.message);
  }
};

async function loadLeaves() {
  const tbody = document.getElementById('wardenLeavesTableBody');
  tbody.innerHTML = '<tr><td colspan="7" style="text-align: center;">Loading leaves...</td></tr>';
  try {
    const res = await App.API.get('/api/warden/leaves');
    const list = res.data || [];
    if (list.length === 0) {
      tbody.innerHTML = '<tr><td colspan="7" class="empty-state">No leave requests found.</td></tr>';
      return;
    }
    tbody.innerHTML = list.map(l => {
      const isPending = (l.status || '').toUpperCase() === 'PENDING';
      return `
        <tr>
          <td><strong>#${l.leaveId}</strong></td>
          <td><strong>${App.escapeHTML(l.studentName || '#' + l.studentId)}</strong></td>
          <td>${App.escapeHTML(l.fromDate)}</td>
          <td>${App.escapeHTML(l.toDate)}</td>
          <td>${App.escapeHTML(l.reason)}</td>
          <td><span class="badge badge-${(l.status || 'pending').toLowerCase()}">${App.escapeHTML(l.status)}</span></td>
          <td style="text-align: right;">
            ${isPending ? `
              <button class="btn btn-sm btn-success" onclick="authLeave(${l.leaveId}, 'APPROVED')">Approve</button>
              <button class="btn btn-sm btn-danger" onclick="authLeave(${l.leaveId}, 'REJECTED')">Reject</button>
            ` : `<span style="font-size: 0.8rem; color: var(--text-muted);">${App.escapeHTML(l.status)}</span>`}
          </td>
        </tr>
      `;
    }).join('');
  } catch (err) {
    tbody.innerHTML = `<tr><td colspan="7" style="text-align: center; color: var(--danger);">${App.escapeHTML(err.message)}</td></tr>`;
  }
}

window.authLeave = async function(id, status) {
  try {
    await App.API.put(`/api/warden/leaves/${id}/status`, { status });
    App.Toast.success(`Leave #${id} marked as ${status}`);
    loadLeaves();
    loadWardenStats();
  } catch (err) {
    App.Toast.error(err.message);
  }
};

async function loadAttendance() {
  const tbody = document.getElementById('wardenAttTableBody');
  tbody.innerHTML = '<tr><td colspan="5" style="text-align: center;">Loading attendance...</td></tr>';
  try {
    const res = await App.API.get('/api/warden/attendance');
    const list = res.data || [];
    if (list.length === 0) {
      tbody.innerHTML = '<tr><td colspan="5" class="empty-state">No attendance records.</td></tr>';
      return;
    }
    tbody.innerHTML = list.map(a => `
      <tr>
        <td><strong>#${a.attendanceId}</strong></td>
        <td><strong>${App.escapeHTML(a.studentName || '#' + a.studentId)}</strong></td>
        <td>${App.escapeHTML(a.date)}</td>
        <td>${App.escapeHTML(a.entryTime || '-')}</td>
        <td><span class="badge badge-${(a.status || 'present').toLowerCase()}">${App.escapeHTML(a.status)}</span></td>
      </tr>
    `).join('');
  } catch (err) {
    tbody.innerHTML = `<tr><td colspan="5" style="text-align: center; color: var(--danger);">${App.escapeHTML(err.message)}</td></tr>`;
  }
}

async function loadVisitors() {
  const tbody = document.getElementById('wardenVisTableBody');
  tbody.innerHTML = '<tr><td colspan="8" style="text-align: center;">Loading visitors...</td></tr>';
  try {
    const res = await App.API.get('/api/warden/visitors');
    const list = res.data || [];
    if (list.length === 0) {
      tbody.innerHTML = '<tr><td colspan="8" class="empty-state">No visitors logged.</td></tr>';
      return;
    }
    tbody.innerHTML = list.map(v => `
      <tr>
        <td><strong>#${v.visitorId}</strong></td>
        <td><strong>${App.escapeHTML(v.studentName || '#' + v.studentId)}</strong></td>
        <td>${App.escapeHTML(v.visitorName)}</td>
        <td>${App.escapeHTML(v.relationship)}</td>
        <td>${App.escapeHTML(v.phone)}</td>
        <td>${App.escapeHTML(v.visitDate)}</td>
        <td><span class="badge badge-${(v.status || 'pending').toLowerCase()}">${App.escapeHTML(v.status)}</span></td>
        <td style="text-align: right;">
          ${v.status !== 'CHECKED_OUT' ? `<button class="btn btn-sm btn-outline" onclick="checkoutWardenVisitor(${v.visitorId})">Check Out</button>` : '<span style="font-size: 0.75rem; color: var(--text-muted);">Completed</span>'}
        </td>
      </tr>
    `).join('');
  } catch (err) {
    tbody.innerHTML = `<tr><td colspan="8" style="text-align: center; color: var(--danger);">${App.escapeHTML(err.message)}</td></tr>`;
  }
}

window.checkoutWardenVisitor = async function(id) {
  try {
    await App.API.put(`/api/admin/visitors/${id}/status`, { status: 'CHECKED_OUT' });
    App.Toast.success('Visitor check-out completed');
    loadVisitors();
  } catch (err) {
    App.Toast.error(err.message);
  }
};

async function loadSecurityLogs() {
  const tbody = document.getElementById('wardenSecTableBody');
  tbody.innerHTML = '<tr><td colspan="6" style="text-align: center;">Loading security records...</td></tr>';
  try {
    const res = await App.API.get('/api/warden/security-logs');
    const list = res.data || [];
    if (list.length === 0) {
      tbody.innerHTML = '<tr><td colspan="6" class="empty-state">No security logs recorded.</td></tr>';
      return;
    }
    tbody.innerHTML = list.map(s => `
      <tr>
        <td><strong>#${s.logId}</strong></td>
        <td>${s.timestamp ? new Date(s.timestamp).toLocaleTimeString() : '-'}</td>
        <td><strong>${App.escapeHTML(s.studentName || (s.studentId ? '#' + s.studentId : 'General'))}</strong></td>
        <td><span class="badge ${s.eventType === 'CURFEW_VIOLATION' ? 'badge-danger' : 'badge-admin'}">${App.escapeHTML(s.eventType)}</span></td>
        <td>${App.escapeHTML(s.gate)}</td>
        <td><code>${App.escapeHTML(s.verificationMethod)}</code></td>
      </tr>
    `).join('');
  } catch (err) {
    tbody.innerHTML = `<tr><td colspan="6" style="text-align: center; color: var(--danger);">${App.escapeHTML(err.message)}</td></tr>`;
  }
}

function setupWardenForms() {
  const wAttForm = document.getElementById('wardenAttForm');
  if (wAttForm) {
    document.getElementById('wAttDate').value = new Date().toISOString().split('T')[0];
    wAttForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const payload = {
        studentId: parseInt(document.getElementById('wAttStudentSelect').value),
        date: document.getElementById('wAttDate').value,
        status: document.getElementById('wAttStatus').value
      };
      try {
        await App.API.post('/api/warden/attendance', payload);
        App.Toast.success('Attendance recorded');
        App.Modal.close('wardenAttModal');
        loadAttendance();
      } catch (err) {
        App.Toast.error(err.message);
      }
    });
  }

  const wVisForm = document.getElementById('wardenVisForm');
  if (wVisForm) {
    wVisForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const payload = {
        studentId: parseInt(document.getElementById('wVisStudentSelect').value),
        visitorName: document.getElementById('wVisName').value.trim(),
        relationship: document.getElementById('wVisRelation').value.trim(),
        phone: document.getElementById('wVisPhone').value.trim(),
        visitDate: new Date().toISOString().split('T')[0]
      };
      try {
        await App.API.post('/api/warden/visitors', payload);
        App.Toast.success('Visitor pass issued');
        App.Modal.close('wardenVisModal');
        loadVisitors();
      } catch (err) {
        App.Toast.error(err.message);
      }
    });
  }
}
