/**
 * CHINNAMMAL — SMART MANAGEMENT PLATFORM
 * Student Self-Service Portal Logic
 */

document.addEventListener('DOMContentLoaded', async () => {
  const currentUser = await App.Auth.checkSession('STUDENT');
  if (!currentUser) return;

  App.setupTabs('overview', (tabId) => {
    loadTabData(tabId);
  });

  await loadStudentDashboard();
  setupStudentForms();
});

let studentDataCache = null;

async function loadStudentDashboard() {
  try {
    const res = await App.API.get('/api/student/dashboard');
    if (!res || !res.data) return;
    studentDataCache = res.data;
    const d = studentDataCache;
    const s = d.student || {};
    const r = d.room || {};
    const b = d.block || {};
    const f = d.fee || {};

    // 1. Welcome Card
    document.getElementById('welcomeStudentName').textContent = `Welcome back, ${s.name || 'Resident'}!`;
    document.getElementById('welcomeStudentMeta').textContent = `${s.department || ''} • Year ${s.year || 1} • Room ${r.roomNumber || 'Unassigned'} (${b.blockName || ''}) • Bed ${s.bedNumber || '-'}`;
    document.getElementById('sidebarRoomLabel').textContent = `Room: ${r.roomNumber || 'None'}`;

    // 2. Overview Stats
    document.getElementById('statStudentRoom').textContent = r.roomNumber || 'Not assigned';
    document.getElementById('statStudentBed').textContent = `Bed: ${s.bedNumber || '-'}`;

    const feeBal = f.balance ?? 0;
    const feeStatus = f.status || 'PAID';
    document.getElementById('statStudentFeeStatus').textContent = feeStatus;
    document.getElementById('statStudentFeeBalance').textContent = `Balance: ₹ ${feeBal.toLocaleString('en-IN')}`;

    document.getElementById('statStudentAttPct').textContent = `${d.attendancePercentage ?? 100}%`;
    document.getElementById('statStudentComplaints').textContent = (d.complaints || []).length;

    // Roommates in Overview
    renderRoommatesOverview(d.roommates || []);

    // Also populate profile
    renderProfile(s, r, b);
  } catch (err) {
    App.Toast.error('Failed to load dashboard: ' + err.message);
  }
}

function loadTabData(tabId) {
  if (!studentDataCache) return;
  const d = studentDataCache;
  switch (tabId) {
    case 'overview': break;
    case 'room': renderRoomTab(d.student, d.room, d.block, d.roommates); break;
    case 'fees': renderFeesTab(d.fee); break;
    case 'complaints': renderComplaintsTab(d.complaints); break;
    case 'leaves': renderLeavesTab(d.leaves); break;
    case 'attendance': renderAttendanceTab(d.attendance); break;
    case 'visitors': renderVisitorsTab(d.visitors); break;
    case 'profile': renderProfile(d.student, d.room, d.block); break;
  }
}

function renderRoommatesOverview(roommates) {
  const container = document.getElementById('overviewRoommatesList');
  if (!container) return;
  if (roommates.length === 0) {
    container.innerHTML = '<p style="color: var(--text-muted); font-size: 0.85rem;">No other roommates assigned to this room.</p>';
    return;
  }
  container.innerHTML = roommates.map(rm => `
    <div style="display: flex; align-items: center; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid var(--border-light);">
      <div>
        <div style="font-weight: 600; font-size: 0.875rem;">${App.escapeHTML(rm.name)}</div>
        <div style="font-size: 0.75rem; color: var(--text-muted);">${App.escapeHTML(rm.department)} • Year ${rm.year}</div>
      </div>
      <span class="badge badge-student">${App.escapeHTML(rm.bedNumber || 'Bed')}</span>
    </div>
  `).join('');
}

function renderRoomTab(student, room, block, roommates) {
  const infoEl = document.getElementById('roomDetailsInfo');
  if (infoEl) {
    infoEl.innerHTML = `
      <div style="background: var(--bg-main); padding: 14px; border-radius: var(--radius-md); border: 1px solid var(--card-border);">
        <span style="font-size: 0.75rem; color: var(--text-muted); text-transform: uppercase;">Room Number</span>
        <div style="font-size: 1.2rem; font-weight: 700;">${App.escapeHTML(room?.roomNumber || 'None')}</div>
      </div>
      <div style="background: var(--bg-main); padding: 14px; border-radius: var(--radius-md); border: 1px solid var(--card-border);">
        <span style="font-size: 0.75rem; color: var(--text-muted); text-transform: uppercase;">Hostel Block</span>
        <div style="font-size: 1.2rem; font-weight: 700;">${App.escapeHTML(block?.blockName || 'Main')}</div>
      </div>
      <div style="background: var(--bg-main); padding: 14px; border-radius: var(--radius-md); border: 1px solid var(--card-border);">
        <span style="font-size: 0.75rem; color: var(--text-muted); text-transform: uppercase;">Assigned Bed</span>
        <div style="font-size: 1.2rem; font-weight: 700;">${App.escapeHTML(student?.bedNumber || '-')}</div>
      </div>
      <div style="background: var(--bg-main); padding: 14px; border-radius: var(--radius-md); border: 1px solid var(--card-border);">
        <span style="font-size: 0.75rem; color: var(--text-muted); text-transform: uppercase;">Room Capacity</span>
        <div style="font-size: 1.2rem; font-weight: 700;">${room?.capacity || 2} Beds</div>
      </div>
    `;
  }

  const tbody = document.getElementById('roommatesTableBody');
  if (tbody) {
    if (!roommates || roommates.length === 0) {
      tbody.innerHTML = '<tr><td colspan="5" class="empty-state">No other roommates in this room.</td></tr>';
      return;
    }
    tbody.innerHTML = roommates.map(rm => `
      <tr>
        <td><strong>${App.escapeHTML(rm.name)}</strong></td>
        <td>${App.escapeHTML(rm.department)}</td>
        <td>Year ${rm.year}</td>
        <td><span class="badge badge-student">${App.escapeHTML(rm.bedNumber || '-')}</span></td>
        <td>${App.escapeHTML(rm.phone)}</td>
      </tr>
    `).join('');
  }
}

function renderFeesTab(fee) {
  const box = document.getElementById('feeAccountBox');
  if (!box) return;
  if (!fee) {
    box.innerHTML = '<p style="color: var(--text-muted);">No fee invoice record found for your account.</p>';
    return;
  }
  const isPaid = (fee.balance <= 0);
  box.innerHTML = `
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
      <span style="font-size: 0.85rem; font-weight: 600; color: var(--text-muted);">Status</span>
      <span class="badge ${isPaid ? 'badge-paid' : 'badge-pending'}">${isPaid ? 'FULLY PAID' : 'PENDING'}</span>
    </div>
    <div style="display: flex; justify-content: space-between; margin-bottom: 10px;">
      <span style="color: var(--text-secondary); font-size: 0.9rem;">Total Fee Amount:</span>
      <strong>₹ ${fee.totalAmount.toLocaleString('en-IN')}</strong>
    </div>
    <div style="display: flex; justify-content: space-between; margin-bottom: 10px;">
      <span style="color: var(--text-secondary); font-size: 0.9rem;">Paid Amount:</span>
      <strong style="color: var(--success);">₹ ${fee.paidAmount.toLocaleString('en-IN')}</strong>
    </div>
    <div style="display: flex; justify-content: space-between; margin-bottom: 14px; padding-top: 10px; border-top: 1px dashed var(--card-border);">
      <span style="font-size: 1rem; font-weight: 700;">Remaining Balance:</span>
      <strong style="font-size: 1.15rem; color: ${isPaid ? 'var(--success)' : 'var(--danger)'};">₹ ${fee.balance.toLocaleString('en-IN')}</strong>
    </div>
    <div style="font-size: 0.78rem; color: var(--text-muted);">
      Payment Due Date: <strong>${App.escapeHTML(fee.dueDate || 'End of Term')}</strong>
    </div>
  `;
}

function renderComplaintsTab(complaints) {
  const tbody = document.getElementById('studentComplaintsTableBody');
  if (!tbody) return;
  if (!complaints || complaints.length === 0) {
    tbody.innerHTML = '<tr><td colspan="4" class="empty-state">No complaints filed.</td></tr>';
    return;
  }
  tbody.innerHTML = complaints.map(c => `
    <tr>
      <td><span class="badge badge-admin">${App.escapeHTML(c.category)}</span></td>
      <td>${App.escapeHTML(c.description)}</td>
      <td><strong>${App.escapeHTML(c.priority || 'MEDIUM')}</strong></td>
      <td><span class="badge badge-${(c.status || 'open').toLowerCase()}">${App.escapeHTML(c.status)}</span></td>
    </tr>
  `).join('');
}

function renderLeavesTab(leaves) {
  const tbody = document.getElementById('studentLeavesTableBody');
  if (!tbody) return;
  if (!leaves || leaves.length === 0) {
    tbody.innerHTML = '<tr><td colspan="3" class="empty-state">No leave requests submitted.</td></tr>';
    return;
  }
  tbody.innerHTML = leaves.map(l => `
    <tr>
      <td><strong>${App.escapeHTML(l.fromDate)}</strong> to <strong>${App.escapeHTML(l.toDate)}</strong></td>
      <td>${App.escapeHTML(l.reason)}</td>
      <td><span class="badge badge-${(l.status || 'pending').toLowerCase()}">${App.escapeHTML(l.status)}</span></td>
    </tr>
  `).join('');
}

function renderAttendanceTab(attendance) {
  const tbody = document.getElementById('studentAttTableBody');
  if (!tbody) return;
  if (!attendance || attendance.length === 0) {
    tbody.innerHTML = '<tr><td colspan="4" class="empty-state">No attendance records yet.</td></tr>';
    return;
  }
  tbody.innerHTML = attendance.map(a => `
    <tr>
      <td><strong>${App.escapeHTML(a.date)}</strong></td>
      <td>${App.escapeHTML(a.entryTime || '-')}</td>
      <td>${App.escapeHTML(a.exitTime || '-')}</td>
      <td><span class="badge badge-${(a.status || 'present').toLowerCase()}">${App.escapeHTML(a.status)}</span></td>
    </tr>
  `).join('');
}

function renderVisitorsTab(visitors) {
  const tbody = document.getElementById('studentVisTableBody');
  if (!tbody) return;
  if (!visitors || visitors.length === 0) {
    tbody.innerHTML = '<tr><td colspan="5" class="empty-state">No visitors logged for you.</td></tr>';
    return;
  }
  tbody.innerHTML = visitors.map(v => `
    <tr>
      <td><strong>${App.escapeHTML(v.visitorName)}</strong></td>
      <td>${App.escapeHTML(v.relationship)}</td>
      <td>${App.escapeHTML(v.phone)}</td>
      <td>${App.escapeHTML(v.visitDate)}</td>
      <td><span class="badge badge-${(v.status || 'pending').toLowerCase()}">${App.escapeHTML(v.status)}</span></td>
    </tr>
  `).join('');
}

function renderProfile(student, room, block) {
  const box = document.getElementById('studentProfileBox');
  if (!box || !student) return;
  box.innerHTML = `
    <div style="display: flex; flex-direction: column; gap: 12px;">
      <div style="display: flex; justify-content: space-between;">
        <span style="color: var(--text-muted); font-size: 0.85rem;">Student ID</span>
        <strong>#${student.studentId}</strong>
      </div>
      <div style="display: flex; justify-content: space-between;">
        <span style="color: var(--text-muted); font-size: 0.85rem;">Full Name</span>
        <strong>${App.escapeHTML(student.name)}</strong>
      </div>
      <div style="display: flex; justify-content: space-between;">
        <span style="color: var(--text-muted); font-size: 0.85rem;">Department</span>
        <strong>${App.escapeHTML(student.department)}</strong>
      </div>
      <div style="display: flex; justify-content: space-between;">
        <span style="color: var(--text-muted); font-size: 0.85rem;">Academic Year</span>
        <strong>Year ${student.year}</strong>
      </div>
      <div style="display: flex; justify-content: space-between;">
        <span style="color: var(--text-muted); font-size: 0.85rem;">Phone</span>
        <strong>${App.escapeHTML(student.phone)}</strong>
      </div>
      <div style="display: flex; justify-content: space-between;">
        <span style="color: var(--text-muted); font-size: 0.85rem;">Email</span>
        <strong>${App.escapeHTML(student.email)}</strong>
      </div>
    </div>
  `;
}

function setupStudentForms() {
  // Complaint Submit Form
  const compForm = document.getElementById('studentComplaintForm');
  if (compForm) {
    compForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const payload = {
        category: document.getElementById('compCategory').value,
        priority: document.getElementById('compPriority').value,
        description: document.getElementById('compDescription').value.trim()
      };
      try {
        await App.API.post('/api/student/complaints', payload);
        App.Toast.success('Complaint filed successfully');
        compForm.reset();
        await loadStudentDashboard();
        renderComplaintsTab(studentDataCache?.complaints);
      } catch (err) {
        App.Toast.error(err.message);
      }
    });
  }

  // Leave Submit Form
  const leaveForm = document.getElementById('studentLeaveForm');
  if (leaveForm) {
    leaveForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const payload = {
        fromDate: document.getElementById('leaveFrom').value,
        toDate: document.getElementById('leaveTo').value,
        reason: document.getElementById('leaveReason').value.trim()
      };
      try {
        await App.API.post('/api/student/leaves', payload);
        App.Toast.success('Leave request submitted successfully');
        leaveForm.reset();
        await loadStudentDashboard();
        renderLeavesTab(studentDataCache?.leaves);
      } catch (err) {
        App.Toast.error(err.message);
      }
    });
  }

  // Password Change Form
  const changePassForm = document.getElementById('studentChangePasswordForm');
  if (changePassForm) {
    changePassForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const newPassword = document.getElementById('pNewPass').value.trim();
      const confirmPassword = document.getElementById('pConfirmPass').value.trim();

      if (newPassword.length < 8) {
        App.Toast.error('Password must be at least 8 characters long');
        return;
      }
      if (newPassword !== confirmPassword) {
        App.Toast.error('New password and confirmation do not match');
        return;
      }

      try {
        await App.API.post('/api/auth/change-password', {
          newPassword,
          confirmPassword
        });
        App.Toast.success('Password changed successfully');
        changePassForm.reset();
      } catch (err) {
        App.Toast.error(err.message);
      }
    });
  }
}
