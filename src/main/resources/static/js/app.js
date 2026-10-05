/**
 * CHINNAMMAL — SMART MANAGEMENT PLATFORM
 * Core Application Framework & Utilities
 */

const App = (() => {
  // Global API Client
  const API = {
    async request(url, options = {}) {
      options.headers = options.headers || {};
      if (options.body && typeof options.body === 'object') {
        options.headers['Content-Type'] = 'application/json';
        options.body = JSON.stringify(options.body);
      }

      try {
        const response = await fetch(url, options);
        if (response.status === 401) {
          if (!window.location.pathname.endsWith('login.html')) {
            window.location.href = '/login.html';
          }
          throw new Error('Session expired. Please log in again.');
        }

        const data = await response.json().catch(() => ({ success: false, message: 'Invalid response from server' }));
        if (!response.ok || (data && data.success === false)) {
          const errMsg = data && data.message ? data.message : `Server error: ${response.status}`;
          throw new Error(errMsg);
        }
        return data;
      } catch (err) {
        console.error('API Error:', err);
        throw err;
      }
    },

    get(url) {
      return this.request(url, { method: 'GET' });
    },

    post(url, body) {
      return this.request(url, { method: 'POST', body });
    },

    put(url, body) {
      return this.request(url, { method: 'PUT', body });
    },

    delete(url) {
      return this.request(url, { method: 'DELETE' });
    }
  };

  // Toast Notification System
  const Toast = {
    container: null,

    init() {
      if (!this.container) {
        this.container = document.createElement('div');
        this.container.className = 'toast-container';
        document.body.appendChild(this.container);
      }
    },

    show(message, type = 'info') {
      this.init();
      const toast = document.createElement('div');
      toast.className = `toast toast-${type}`;

      let icon = 'ℹ️';
      if (type === 'success') icon = '✓';
      if (type === 'error') icon = '✕';

      toast.innerHTML = `
        <span class="toast-icon">${icon}</span>
        <span class="toast-content">${escapeHTML(message)}</span>
        <button class="toast-close">&times;</button>
      `;

      toast.querySelector('.toast-close').addEventListener('click', () => {
        this.remove(toast);
      });

      this.container.appendChild(toast);
      requestAnimationFrame(() => toast.classList.add('show'));

      setTimeout(() => {
        this.remove(toast);
      }, 4000);
    },

    remove(toast) {
      toast.classList.remove('show');
      setTimeout(() => {
        if (toast.parentElement) toast.remove();
      }, 300);
    },

    success(msg) { this.show(msg, 'success'); },
    error(msg) { this.show(msg, 'error'); },
    info(msg) { this.show(msg, 'info'); }
  };

  // Modal Dialog Controller
  const Modal = {
    open(id) {
      const modal = document.getElementById(id);
      if (modal) {
        modal.classList.add('active');
        document.body.style.overflow = 'hidden';
      }
    },

    close(id) {
      const modal = document.getElementById(id);
      if (modal) {
        modal.classList.remove('active');
        document.body.style.overflow = '';
      }
    },

    initTriggers() {
      document.querySelectorAll('[data-modal-close]').forEach(btn => {
        btn.addEventListener('click', (e) => {
          const modal = e.target.closest('.modal-overlay');
          if (modal) Modal.close(modal.id);
        });
      });

      document.querySelectorAll('.modal-overlay').forEach(overlay => {
        overlay.addEventListener('click', (e) => {
          if (e.target === overlay) Modal.close(overlay.id);
        });
      });
    }
  };

  // Clipboard Utility
  const copyToClipboard = async (text, buttonElement) => {
    try {
      await navigator.clipboard.writeText(text);
      if (buttonElement) {
        const originalText = buttonElement.innerHTML;
        buttonElement.innerHTML = '✓ Copied!';
        buttonElement.classList.add('btn-success');
        setTimeout(() => {
          buttonElement.innerHTML = originalText;
          buttonElement.classList.remove('btn-success');
        }, 2000);
      }
      Toast.success('Credentials copied to clipboard!');
    } catch (e) {
      // Fallback
      const ta = document.createElement('textarea');
      ta.value = text;
      document.body.appendChild(ta);
      ta.select();
      document.execCommand('copy');
      document.body.removeChild(ta);
      if (buttonElement) buttonElement.innerHTML = '✓ Copied!';
      Toast.success('Credentials copied to clipboard!');
    }
  };

  // Session & Authentication Controller
  const Auth = {
    currentUser: null,

    async checkSession(requiredRole = null) {
      try {
        const res = await API.get('/api/auth/me');
        if (res && res.data) {
          this.currentUser = res.data;
          this.populateUserProfile(res.data);

          if (requiredRole && res.data.role !== requiredRole) {
            // Redirect to appropriate portal
            if (res.data.role === 'ADMIN') window.location.href = '/admin.html';
            else if (res.data.role === 'WARDEN') window.location.href = '/warden.html';
            else if (res.data.role === 'STUDENT') window.location.href = '/student.html';
            return null;
          }
          return res.data;
        }
      } catch (err) {
        if (!window.location.pathname.endsWith('login.html')) {
          window.location.href = '/login.html';
        }
      }
      return null;
    },

    populateUserProfile(user) {
      const nameEl = document.getElementById('topbarUserName');
      const roleEl = document.getElementById('topbarUserRole');
      const avatarEl = document.getElementById('topbarUserAvatar');

      if (nameEl) nameEl.textContent = user.displayName || user.username;
      if (roleEl) roleEl.textContent = user.role;
      if (avatarEl) {
        const initials = (user.displayName || user.username).substring(0, 2).toUpperCase();
        avatarEl.textContent = initials;
      }
    },

    async logout() {
      try {
        await API.post('/api/auth/logout', {});
      } catch (ignored) {}
      window.location.href = '/login.html';
    }
  };

  function escapeHTML(str) {
    if (!str) return '';
    return String(str)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;');
  }

  // Setup tab navigation
  function setupTabs(defaultTab = 'overview', onTabChange = null) {
    const navItems = document.querySelectorAll('.sidebar-nav .nav-item[data-tab]');
    const sections = document.querySelectorAll('.view-section');

    function activateTab(tabId) {
      navItems.forEach(item => {
        if (item.getAttribute('data-tab') === tabId) {
          item.classList.add('active');
        } else {
          item.classList.remove('active');
        }
      });

      sections.forEach(sec => {
        if (sec.id === `tab-${tabId}`) {
          sec.classList.add('active');
        } else {
          sec.classList.remove('active');
        }
      });

      const heading = document.getElementById('pageHeading');
      const activeItem = document.querySelector(`.sidebar-nav .nav-item[data-tab="${tabId}"]`);
      if (heading && activeItem) {
        const text = activeItem.querySelector('span:not(.nav-icon):not(.nav-badge)');
        if (text) heading.textContent = text.textContent.trim();
      }

      if (typeof onTabChange === 'function') {
        onTabChange(tabId);
      }
    }

    navItems.forEach(item => {
      item.addEventListener('click', (e) => {
        e.preventDefault();
        const tabId = item.getAttribute('data-tab');
        activateTab(tabId);
      });
    });

    activateTab(defaultTab);
  }

  // Setup mobile sidebar toggle
  function setupMobileSidebar() {
    const menuBtn = document.getElementById('mobileMenuBtn');
    const sidebar = document.querySelector('.sidebar');
    if (menuBtn && sidebar) {
      menuBtn.addEventListener('click', () => {
        sidebar.classList.toggle('open');
      });

      document.addEventListener('click', (e) => {
        if (!sidebar.contains(e.target) && !menuBtn.contains(e.target) && sidebar.classList.contains('open')) {
          sidebar.classList.remove('open');
        }
      });
    }
  }

  document.addEventListener('DOMContentLoaded', () => {
    Modal.initTriggers();
    setupMobileSidebar();

    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
      logoutBtn.addEventListener('click', (e) => {
        e.preventDefault();
        Auth.logout();
      });
    }
  });

  return {
    API,
    Toast,
    Modal,
    Auth,
    copyToClipboard,
    escapeHTML,
    setupTabs
  };
})();
